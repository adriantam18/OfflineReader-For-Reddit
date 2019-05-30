package atamayo.offlinereader;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.Utils.RedditDownloader;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SubredditService extends Service {
    private final static int NOTIF_ID = 12149;
    private final static String CHANNEL_ID = "OfflineReader Notifications";
    private final static String TAG = "Subreddit_Service";
    public final static String EXTRA_SUBREDDIT = "subreddit";

    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywords;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private RedditDownloader mRedditDownloader;
    private CompositeDisposable mDisposable;
    private int mRunningTasks;

    public SubredditService(){
    }

    @Override
    public void onCreate(){
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Must be called before initializing notification builder
        createNotificationChannel();
        mNotificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true);

        mRepository = ((App) getApplication()).getSubredditsRepository();
        mKeywords = new KeywordsPreference(this);
        mRedditDownloader = new RedditDownloader(this);

        mDisposable = new CompositeDisposable();
        mRunningTasks = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Keep track of running tasks so we know when to stop
        if (mRunningTasks == 0) {
            startForeground(NOTIF_ID, getNotification(getMainActivityIntent(), "", 0, 0,
                    true, NotificationCompat.PRIORITY_DEFAULT));
        }
        ++mRunningTasks;

        List<String> subsToDownload = intent.getStringArrayListExtra(EXTRA_SUBREDDIT);
        if (subsToDownload != null && !subsToDownload.isEmpty()) {
            for (final String subreddit : subsToDownload) {
                List<String> keywords = new ArrayList<>(mKeywords.getKeywords(subreddit));
                mDisposable.add(mRedditDownloader.getThreads(subreddit)
                        .subscribeOn(Schedulers.single())
                        .toObservable()
                        .flatMap(Observable::fromIterable)
                        .concatMap(redditThread -> Observable.just(redditThread).delay(1, TimeUnit.SECONDS))
                        .filter(redditThread -> keywords.isEmpty() || containsKeyword(redditThread.getTitle(), keywords))
                        .filter(mRepository::addRedditThread)
                        .doOnNext(redditThread -> updateNotificationText(redditThread.getTitle()))
                        .map(redditThread -> Pair.create(redditThread, mRedditDownloader.getComments(redditThread.getSubreddit(),
                                redditThread.getThreadId())))
                        .subscribe(pair -> mRepository.addRedditComments(pair.first, pair.second.blockingGet()),
                                this::processError,
                                this::processCompletedTask));
            }
        } else {
            stopSelf();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mDisposable.dispose();

        super.onDestroy();
    }

    /**
     * Notifies the user of an error and stops the service from continuing.
     */
    private void processError(Throwable throwable) {
        mNotificationManager.notify(NOTIF_ID, getNotification(getMainActivityIntent(),
                "Some threads may not have been downloaded", 0, 0, false,
                NotificationCompat.PRIORITY_DEFAULT));
        endService();
    }

    /**
     * This method is responsible for building a notification that
     * lets the user know that downloads have been completed. It also
     * stops this service if appropriate.
     */
    private void processCompletedTask(){
        /*
         * Decrement running tasks to signify completion of a task and check
         * if there are still tasks running so we know whether or not we should
         * send the notification and stop the service.
         */
        if(--mRunningTasks == 0) {
            mNotificationManager.notify(NOTIF_ID, getNotification(getMainActivityIntent(),
                    "New threads have been downloaded", 0, 0, false,
                    NotificationCompat.PRIORITY_DEFAULT));
            endService();
        }
    }

    private void updateNotificationText(String text) {
        mNotificationManager.notify(NOTIF_ID, getNotification(getMainActivityIntent(),
                text, 0, 0, true,
                NotificationCompat.PRIORITY_DEFAULT));
    }

    /**
     * Builds and returns notification for display
     *
     * @param intent Intent to be executed when notification is clicked
     * @param text Text content of notification
     * @param maxProgress Max value for displaying determinate progress
     * @param progress Current value for displaying progress
     * @param indeterminate Whether notification should display indeterminate progress bar or fixed value
     * @param priority Indicates the priority level for notification
     * @return Notification with the values specified
     */
    private Notification getNotification(PendingIntent intent, String text, int maxProgress,
                                         int progress, boolean indeterminate, int priority) {
        return mNotificationBuilder
                .setContentIntent(intent)
                .setContentText(text)
                .setProgress(maxProgress, progress, indeterminate)
                .setPriority(priority)
                .build();
    }

    /**
     * @return PendingIntent that allows user to go to subreddits
     * list when clicking on the notification
     */
    private PendingIntent getMainActivityIntent(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_EXTRA, "SubredditsListing");
        intent.setAction(Long.toString(System.currentTimeMillis()));
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Reddit";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            String description = "Notifications for OfflineReader";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.setSound(null, null);

            mNotificationManager.createNotificationChannel(channel);
        }
    }

    /**
     * Checks to see if a string contains any word from a specified list of keywords.
     *
     * @param title string to be checked
     * @param keywords list of words to check title against
     * @return true if title contains at least one word from the list, false otherwise
     */
    private boolean containsKeyword(String title, List<String> keywords) {
        String[] words = title.split("\\s+");
        for(String word : words){
            for(String keyword : keywords){
                if(word.toLowerCase().contains(keyword.toLowerCase())){
                    return true;
                }
            }
        }

        return false;
    }

    private void endService() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            stopForeground(Service.STOP_FOREGROUND_DETACH);
        else
            stopForeground(false);
        stopSelf();
    }
}

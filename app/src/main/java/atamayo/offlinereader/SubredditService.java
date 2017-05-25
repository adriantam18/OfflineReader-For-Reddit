package atamayo.offlinereader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.Data.FileManager;
import atamayo.offlinereader.Utils.RedditDownloader;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SubredditService extends Service {
    private final static String TAG = "Subreddit_Service";
    public final static String EXTRA_SUBREDDIT = "subreddit";
    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywords;
    private NotificationCompat.Builder mNotificationBuilder;
    private NotificationManager mNotificationManager;
    private RedditDownloader redditDownloader;
    private CompositeDisposable compositeDisposable;
    private int runningTasks;
    private final static int FOREGROUND_ID = 12148;

    public SubredditService(){
    }

    @Override
    public void onCreate(){
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setAutoCancel(true);

        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        mRepository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao(), new FileManager(this));
        mKeywords = new KeywordsPreference(this);
        redditDownloader = new RedditDownloader(this);

        compositeDisposable = new CompositeDisposable();
        runningTasks = 0;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Keep track of running tasks so we know when to stop
        ++runningTasks;
        startForegroundNotif();

        List<String> subsToDownload = intent.getStringArrayListExtra(EXTRA_SUBREDDIT);

        if (subsToDownload != null) {
            for (final String subreddit : subsToDownload) {
                List<String> keywords = new ArrayList<>(mKeywords.getKeywords(subreddit));
                compositeDisposable.add(redditDownloader.getThreads(subreddit, keywords)
                        .subscribeOn(Schedulers.single())
                        .flatMap(redditThreads -> Observable.fromIterable(redditThreads))
                        .concatMap(redditThread ->
                                Observable.just(redditThread)
                                        .delay(2, TimeUnit.SECONDS))
                        .doOnNext(redditThread ->
                                redditThread.setImageBytes(redditDownloader.downloadImage(redditThread, 216, 384).blockingFirst(new byte[]{})))
                        .filter(redditThread -> mRepository.addRedditThread(redditThread))
                        .map(redditThread ->
                                Pair.create(redditThread, redditDownloader.getComments(redditThread.getSubreddit(), redditThread.getThreadId())))
                        .subscribe(pair -> mRepository.addRedditComments(pair.first, pair.second.blockingFirst("")),
                                throwable -> {},
                                this::processCompletedTask));
            }
        }else{
            stopSelf();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();

        super.onDestroy();
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
        if(--runningTasks == 0) {
            Notification notification = mNotificationBuilder
                    .setContentIntent(getMainActivityIntent())
                    .setProgress(0, 0, false)
                    .setContentText("New threads have been downloaded")
                    .build();

            mNotificationManager.notify(FOREGROUND_ID, notification);
            stopSelf();
        }
    }

    /**
     * Builds the ongoing notification and runs this service
     * in the foreground
     */
    private void startForegroundNotif(){
        Notification notification = mNotificationBuilder
                .setContentIntent(getMainActivityIntent())
                .setProgress(0, 0, true)
                .build();

        startForeground(FOREGROUND_ID, notification);
    }

    /**
     * @return PendingIntent that allows user to go to subreddits
     * list when clicking on the notification
     */
    private PendingIntent getMainActivityIntent(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.FRAGMENT_EXTRA, "SubredditsListing");
        return PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}

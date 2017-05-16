package atamayo.offlinereader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.Pair;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.Data.CommentFileManager;
import atamayo.offlinereader.Utils.RedditDownloader;
import io.reactivex.Observable;

public class SubredditService extends IntentService {
    private final static String TAG = "Subreddit_Service";
    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywords;
    private NotificationCompat.Builder mBuilder;
    private RedditDownloader redditDownloader;

    public SubredditService(){
        super("SubredditService");
    }

    @Override
    public void onHandleIntent(Intent intent){
        Log.d(TAG, "Starting download");
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        mRepository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao(), new CommentFileManager(this));
        mKeywords = new KeywordsPreference(this);
        redditDownloader = new RedditDownloader(this);

        setupNotif();

        List<String> subsToDownload = intent.getStringArrayListExtra("subreddits");

        if (subsToDownload != null) {
            for (final String subreddit : subsToDownload) {
                List<String> keywords = new ArrayList<>(mKeywords.getKeywords(subreddit));
                redditDownloader.getThreads(subreddit, keywords)
                        .flatMap(redditThreads -> Observable.fromIterable(redditThreads))
                        .filter(redditThread -> mRepository.addRedditThread(redditThread))
                        .concatMap(redditThread ->
                                Observable.just(Pair.create(redditThread.getFullName(),
                                        redditDownloader.getComments(subreddit, redditThread.getThreadId())))
                                        .delay(1, TimeUnit.SECONDS))
                        .subscribe(pair -> mRepository.addRedditComments(pair.first, pair.second),
                                throwable -> {},
                                this::sendNotification);
            }
        }

        Log.d(TAG, "Finished downloading");
    }

    private void sendNotification(){
        int notificationId = 1001;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId, mBuilder.build());
    }

    private void setupNotif(){
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Downloads completed!")
                .setContentText("You can now view newly downloaded threads")
                .setAutoCancel(true);
        Intent result = new Intent(getApplicationContext(), MainActivity.class);
        result.putExtra("fragment", "SubredditsListing");
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, result, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
    }
}

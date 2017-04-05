package atamayo.offlinereader;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.SubredditsPreference;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditObjectDeserializer;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.Data.CommentFileManager;
import atamayo.offlinereader.Utils.NetworkResponse;
import atamayo.offlinereader.Utils.RedditDownloader;

public class SubredditService extends IntentService {
    private final static String TAG = "Subreddit_Service";
    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywords;
    private NotificationCompat.Builder mBuilder;
    private CommentFileManager commentFileManager;
    private RedditDownloader redditDownloader;

    public SubredditService(){
        super("SubredditService");
    }

    @Override
    public void onHandleIntent(Intent intent){
        Log.d(TAG, "Starting download");
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        commentFileManager = new CommentFileManager(this);
        mRepository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao(), commentFileManager);
        mKeywords = new SubredditsPreference(this);
        redditDownloader = new RedditDownloader(this);

        setupNotif();

        List<String> subsToDownload = intent.getStringArrayListExtra("subreddits");

        if(subsToDownload != null) {
            for (final String subreddit : subsToDownload) {
                List<String> keywords = mKeywords.getKeywords(subreddit);
                redditDownloader.downloadThreads(subreddit, keywords, new NetworkResponse<List<RedditThread>>() {
                    @Override
                    public void onSuccess(List<RedditThread> object) {
                        if(!object.isEmpty()) {
                            downloadComments(subreddit, object);
                        }
                    }

                    @Override
                    public void onError(String message) {

                    }
                });
            }
        }

        Log.d(TAG, "Finished downloading");
    }

    private void downloadComments(String subreddit, List<RedditThread> threads){
            for (final RedditThread thread : threads) {
                if (mRepository.addRedditThread(thread)) {
                    redditDownloader.downloadComments(subreddit, thread.getThreadId(), new NetworkResponse<String>() {
                        @Override
                        public void onSuccess(String object) {
                            commentFileManager.writeToFile(thread.getFullName(), object);
                        }

                        @Override
                        public void onError(String message) {

                        }
                    });
                }
            }
    }

    @Override
    public void onDestroy() {
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

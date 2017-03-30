package atamayo.offlinereddit;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import atamayo.offlinereddit.Data.KeywordsDataSource;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsPreference;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import atamayo.offlinereddit.Data.CommentFileManager;
import atamayo.offlinereddit.Utils.RedditDownloader;

public class SubredditService extends IntentService {
    private final static String TAG = "Subreddit_Service";
    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywords;
    private NotificationCompat.Builder mBuilder;
    private CommentFileManager commentFileManager;

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

        setupNotif();

        List<String> subsToDownload = intent.getStringArrayListExtra("subreddits");

        if(subsToDownload != null) {
            for (String sub : subsToDownload) {
                List<String> keywords = mKeywords.getKeywords(sub);
                List<RedditThread> threads = RedditDownloader.getInstance().downloadThreads(sub, keywords);
                if(!threads.isEmpty()) {
                    for (RedditThread thread : threads) {
                        if(mRepository.addRedditThread(thread)){
                            String comments = RedditDownloader.getInstance().downloadComments(sub, thread.getThreadId());
                            commentFileManager.writeToFile(thread.getFullName(), comments);
                        }
                    }
                }
            }
        }

        Log.d(TAG, "Finished downloading");
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

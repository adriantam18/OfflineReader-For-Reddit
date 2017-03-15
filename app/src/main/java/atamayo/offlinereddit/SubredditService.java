package atamayo.offlinereddit;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import atamayo.offlinereddit.Data.KeywordsDataSource;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsPreference;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditAPI.Subreddit;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import atamayo.offlinereddit.RedditDAO.RedditThreadDao;
import atamayo.offlinereddit.SubThreads.SubThreadsListing;
import atamayo.offlinereddit.Utils.RedditDownloader;

public class SubredditService extends IntentService {
    private final static String TAG = "Subreddit_Service";
    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywords;
    private NotificationCompat.Builder mBuilder;

    public SubredditService(){
        super("SubredditService");
    }

    @Override
    public void onHandleIntent(Intent intent){
        Log.d(TAG, "Starting download");
        DaoSession daoSession = ((App) getApplication()).getDaoSession();
        mRepository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao());
        mKeywords = new SubredditsPreference(this);

        setupNotif();

        List<String> subsToDownload = intent.getStringArrayListExtra("subreddits");

        if(subsToDownload != null) {
            for (String sub : subsToDownload) {
                List<String> keywords = mKeywords.getKeywords(sub);
                List<RedditThread> threads = RedditDownloader.getInstance().startDownload(sub, keywords);

                for (RedditThread thread : threads) {
                    String htmlContents = RedditDownloader.getInstance().downloadThreadComments(thread.getPermalink());
                    if (!htmlContents.isEmpty()) {
                        String filename = genFileName();
                        if (writeToFile(htmlContents, filename)) {
                            thread.setFilename(filename);
                            mRepository.addRedditThread(thread);
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

    private String genFileName(){
        char[] characters = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        Random random = new Random();
        int length = random.nextInt(10) + 5;
        StringBuilder filename = new StringBuilder();
        for(int i = 0; i < length; i++){
            char letter = characters[random.nextInt(characters.length)];
            filename.append(letter);
        }
        return filename.toString();
    }

    private boolean writeToFile(String htmlContents, String filename){
        try {
            FileOutputStream fos = openFileOutput(filename, MODE_PRIVATE);
            fos.write(htmlContents.getBytes());
            fos.close();
            return true;
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }
    }
}

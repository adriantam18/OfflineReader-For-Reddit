package atamayo.offlinereader.Data;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import atamayo.offlinereader.Data.DAO.RedditThreadDao;
import atamayo.offlinereader.Data.DAO.SubredditDao;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

@Database(entities = {Subreddit.class, RedditThread.class}, version = 1)
public abstract class RedditDatabase extends RoomDatabase {
    private static final String DB_NAME = "reddit_database";
    public abstract SubredditDao getSubredditDao();
    public abstract RedditThreadDao getRedditThreadDao();

    private static RedditDatabase INSTANCE;

    public static RedditDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (RedditDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            RedditDatabase.class, DB_NAME).build();
                }
            }
        }

        return INSTANCE;
    }
}

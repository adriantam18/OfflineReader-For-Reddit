package atamayo.offlinereader.Data.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

@Dao
public interface RedditThreadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertRedditThread(RedditThread thread);

    @Query("SELECT * FROM reddit_thread WHERE full_name = :fullName")
    RedditThread loadRedditThreadByFullName(String fullName);

    @Query("SELECT * FROM reddit_thread WHERE subreddit = :subredditName")
    List<RedditThread> loadAllThreadsFromSubreddit(String subredditName);

    @Query("SELECT * FROM reddit_thread WHERE subreddit = :subredditName ORDER BY id DESC " +
            "LIMIT :limit OFFSET :offset")
    List<RedditThread> loadThreadsFromSubreddit(String subredditName, int offset, int limit);

    @Update
    void updateRedditThread(RedditThread... thread);

    @Query("DELETE FROM reddit_thread WHERE full_name = :fullName")
    void deleteRedditThreadByFullName(String fullName);

    @Query("DELETE FROM reddit_thread WHERE subreddit = :subredditName")
    void deleteThreadsFromSubreddit(String subredditName);

    @Query("DELETE FROM reddit_thread")
    void deleteAllRedditThreads();
}

package atamayo.offlinereader.Data.DAO;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

@Dao
public interface SubredditDao {
    @Insert(onConflict = OnConflictStrategy.FAIL)
    long insertSubreddit(Subreddit subreddits);

    @Query("SELECT * FROM subreddit ORDER BY id DESC")
    List<Subreddit> loadSubreddits();

    @Query("SELECT * FROM subreddit WHERE display_name = :displayName")
    Subreddit loadSubredditByDisplayName(String displayName);

    @Query("DELETE FROM subreddit WHERE display_name = :displayName")
    void deleteSubredditByDisplayName(String displayName);

    @Query("DELETE FROM subreddit")
    void deleteAllSubreddits();
}

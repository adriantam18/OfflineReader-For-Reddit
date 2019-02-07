package atamayo.offlinereader.Data;

import android.arch.persistence.room.Room;
import android.database.sqlite.SQLiteConstraintException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereader.Data.DAO.SubredditDao;
import atamayo.offlinereader.Data.RedditDatabase;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

@RunWith(AndroidJUnit4.class)
public class SubredditDaoTest{
    private static final Subreddit SUBREDDIT_NBA = new Subreddit("nba", "r/nba", 123, false);
    private static final Subreddit SUBREDDIT_NFL = new Subreddit("nfl", "r/nfl", 321, false);
    private static final List<Subreddit> SUBREDDITS = Arrays.asList(SUBREDDIT_NBA, SUBREDDIT_NFL);

    private SubredditDao mSubredditDao;
    private RedditDatabase mRedditDatabase;

    @Before
    public void createDb() {
        mRedditDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(),
                RedditDatabase.class)
                .allowMainThreadQueries()
                .build();
        mSubredditDao = mRedditDatabase.getSubredditDao();
    }

    @After
    public void closeDb() throws IOException {
        mRedditDatabase.close();
    }

    @Test
    public void testInsertFailNullName() {
        Subreddit subreddit = new Subreddit();
        try {
            mSubredditDao.insertSubreddit(subreddit);
            Assert.fail("Should throw exception for null subreddit name");
        } catch (SQLiteConstraintException e) {

        }
    }

    @Test
    public void testInsertFailSubredditExists() {
        mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
        try {
            mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
            Assert.fail("Should throw exception for duplicate subreddit");
        } catch (SQLiteConstraintException e) {

        }
    }

    @Test
    public void testLoadEmptySubreddit() {
        List<Subreddit> subreddits = mSubredditDao.loadSubreddits();
        assertTrue(subreddits.isEmpty());
    }

    @Test
    public void testLoadAfterInsert() {
        mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
        mSubredditDao.insertSubreddit(SUBREDDIT_NFL);

        List<Subreddit> subreddits = mSubredditDao.loadSubreddits();
        assertEquals(SUBREDDITS.size(), subreddits.size());
    }

    @Test
    public void testLoadSubredditByDisplayName() {
        mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
        mSubredditDao.insertSubreddit(SUBREDDIT_NFL);

        Subreddit subreddit = mSubredditDao.loadSubredditByDisplayName(SUBREDDIT_NBA.getDisplayName());

        assertEquals(SUBREDDIT_NBA.getDisplayName(), subreddit.getDisplayName());
        assertEquals(SUBREDDIT_NBA.getDisplayNamePrefixed(), subreddit.getDisplayNamePrefixed());
        assertEquals(SUBREDDIT_NBA.getSubscribers(), subreddit.getSubscribers());
        assertEquals(SUBREDDIT_NBA.getOver18(), subreddit.getOver18());
    }

    @Test
    public void testDeleteSubredditByDisplayNameSuccess() {
        mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
        mSubredditDao.insertSubreddit(SUBREDDIT_NFL);

        mSubredditDao.deleteSubredditByDisplayName(SUBREDDIT_NBA.getDisplayName());
        List<Subreddit> subreddits = mSubredditDao.loadSubreddits();
        assertNotEquals(SUBREDDITS.size(), subreddits.size());
    }

    @Test
    public void testDeleteSubredditByDisplayNameFail() {
        mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
        mSubredditDao.insertSubreddit(SUBREDDIT_NFL);

        mSubredditDao.deleteSubredditByDisplayName("Nonexistent");
        List<Subreddit> subreddits = mSubredditDao.loadSubreddits();
        assertEquals(SUBREDDITS.size(), subreddits.size());
    }

    @Test
    public void testDeleteAllSubreddits() {
        mSubredditDao.insertSubreddit(SUBREDDIT_NBA);
        mSubredditDao.insertSubreddit(SUBREDDIT_NFL);

        mSubredditDao.deleteAllSubreddits();
        List<Subreddit> subreddits = mSubredditDao.loadSubreddits();
        assertEquals(0, subreddits.size());
    }
}

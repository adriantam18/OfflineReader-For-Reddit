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

import atamayo.offlinereader.Data.DAO.RedditThreadDao;
import atamayo.offlinereader.Data.DAO.SubredditDao;
import atamayo.offlinereader.Data.RedditDatabase;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

@RunWith(AndroidJUnit4.class)
public class RedditThreadDaoTest {
    private static final String NBA_NAME = "nba";
    private static final String NFl_NAME = "nfl";

    private static final RedditThread NBA_FIRST_THREAD = new RedditThread(false,
            "nbathread1", "nbathread1", NBA_NAME, "First Nba Thread",
            "First Nba Author", 36, 39, 3, "", "", "",
            300, 123L, false, "", "", "");
    private static final RedditThread NBA_SECOND_THREAD = new RedditThread(false,
            "nbathread2", "nbathread2", NBA_NAME, "Second Nba Thread",
            "Second Nba Author", 36, 39, 3, "", "", "",
            300, 123L, false, "", "", "");
    private static final List<RedditThread> NBA_THREADS = Arrays.asList(NBA_FIRST_THREAD, NBA_SECOND_THREAD);

    private static final RedditThread NFL_FIRST_THREAD = new RedditThread(false,
            "nflthread1", "nflthread1", NFl_NAME, "First Nfl Thread",
            "First Nfl Author", 36, 39, 3, "", "", "",
            300, 123L, false, "", "", "");
    private static final RedditThread NFL_SECOND_THREAD = new RedditThread(false,
            "nflthread2", "nflthread2", NFl_NAME, "Second Nfl Thread",
            "Second Nfl Author", 36, 39, 3, "", "", "",
            300, 123L, false, "", "", "");
    private static final RedditThread NFL_THIRD_THREAD = new RedditThread(false,
            "nflthread3", "nflthread3", NFl_NAME, "Third Nfl Thread",
            "Third Nfl Author", 36, 39, 3, "", "", "",
            300, 123L, false, "", "", "");
    private static final List<RedditThread> NFL_THREADS = Arrays.asList(NFL_FIRST_THREAD, NFL_SECOND_THREAD, NFL_THIRD_THREAD);

    private RedditThreadDao mRedditThreadDao;
    private RedditDatabase mRedditDatabase;

    @Before
    public void createDb() {
        mRedditDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getTargetContext(),
                RedditDatabase.class)
                .allowMainThreadQueries()
                .build();
        mRedditThreadDao = mRedditDatabase.getRedditThreadDao();

        SubredditDao subredditDao = mRedditDatabase.getSubredditDao();
        Subreddit nbaSubreddit = new Subreddit();
        nbaSubreddit.setDisplayName(NBA_NAME);
        Subreddit nflSubreddit = new Subreddit();
        nflSubreddit.setDisplayName(NFl_NAME);

        subredditDao.insertSubreddit(nbaSubreddit);
        subredditDao.insertSubreddit(nflSubreddit);
    }

    @After
    public void closeDb() throws IOException {
        mRedditDatabase.close();
    }

    @Test
    public void testInsertFailNullFullName() {
        RedditThread thread = new RedditThread();
        try {
            mRedditThreadDao.insertRedditThread(thread);
            Assert.fail("Should throw exception for null thread full name");
        } catch (SQLiteConstraintException e) {

        }
    }

    @Test
    public void   testReplaceExistingThread() {
        mRedditThreadDao.insertRedditThread(NBA_FIRST_THREAD);

        RedditThread thread = NBA_FIRST_THREAD;
        thread.setSelftext("Modified");
        mRedditThreadDao.insertRedditThread(thread);

        RedditThread insertedThread = mRedditThreadDao.loadRedditThreadByFullName(thread.getFullName());
        assertEquals(thread.getFullName(), insertedThread.getFullName());
        assertEquals(thread.getThreadId(), insertedThread.getThreadId());
        assertEquals(thread.getSubreddit(), insertedThread.getSubreddit());
        assertEquals(thread.getSelftext(), insertedThread.getSelftext());
    }

    @Test
    public void testLoadEmptyThreads() {
        List<RedditThread> threads = mRedditThreadDao.loadAllThreadsFromSubreddit("nba");
        assertEquals(0, threads.size());
    }

    @Test
    public void testLoadAfterInsert() {
        mRedditThreadDao.insertRedditThread(NBA_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NBA_SECOND_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_SECOND_THREAD);

        List<RedditThread> threads = mRedditThreadDao.loadAllThreadsFromSubreddit(NBA_FIRST_THREAD.getSubreddit());
        assertEquals(NBA_THREADS.size(), threads.size());
    }

    @Test
    public void testLoadRecentWithLimitAndOffset() {
        mRedditThreadDao.insertRedditThread(NBA_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NBA_SECOND_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_SECOND_THREAD);

        List<RedditThread> threads = mRedditThreadDao.loadThreadsFromSubreddit(NBA_FIRST_THREAD.getSubreddit(),0, 1);
        assertEquals(1, threads.size());
        assertEquals(NBA_SECOND_THREAD.getFullName(), threads.get(0).getFullName());
        assertEquals(NBA_SECOND_THREAD.getThreadId(), threads.get(0).getThreadId());
        assertEquals(NBA_SECOND_THREAD.getSubreddit(), threads.get(0).getSubreddit());
    }

    @Test
    public void testDeleteThreadByFullName() {
        mRedditThreadDao.insertRedditThread(NBA_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NBA_SECOND_THREAD);
        mRedditThreadDao.deleteRedditThreadByFullName(NBA_SECOND_THREAD.getFullName());

        List<RedditThread> threads = mRedditThreadDao.loadAllThreadsFromSubreddit(NBA_FIRST_THREAD.getSubreddit());
        assertEquals(1, threads.size());
        assertEquals(NBA_FIRST_THREAD.getFullName(), threads.get(0).getFullName());
        assertEquals(NBA_FIRST_THREAD.getThreadId(), threads.get(0).getThreadId());
        assertEquals(NBA_FIRST_THREAD.getSubreddit(), threads.get(0).getSubreddit());
    }

    @Test
    public void testDeleteThreadsFromSubreddit() {
        mRedditThreadDao.insertRedditThread(NBA_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NBA_SECOND_THREAD);
        mRedditThreadDao.deleteThreadsFromSubreddit(NBA_FIRST_THREAD.getSubreddit());

        List<RedditThread> threads = mRedditThreadDao.loadAllThreadsFromSubreddit(NBA_FIRST_THREAD.getSubreddit());
        assertEquals(0, threads.size());
    }

    @Test
    public void testDeleteAllThreads() {
        mRedditThreadDao.insertRedditThread(NBA_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NBA_SECOND_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_FIRST_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_SECOND_THREAD);
        mRedditThreadDao.insertRedditThread(NFL_THIRD_THREAD);
        mRedditThreadDao.deleteAllRedditThreads();

        List<RedditThread> nbaThreads = mRedditThreadDao.loadAllThreadsFromSubreddit(NBA_FIRST_THREAD.getSubreddit());
        List<RedditThread> nflThreads = mRedditThreadDao.loadAllThreadsFromSubreddit(NFL_FIRST_THREAD.getSubreddit());
        assertEquals(0, nbaThreads.size() + nflThreads.size());

    }
}

package atamayo.offlinereddit.Data;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import org.greenrobot.greendao.query.DeleteQuery;

import java.util.List;

import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditAPI.Subreddit;
import atamayo.offlinereddit.RedditDAO.RedditThreadDao;
import atamayo.offlinereddit.RedditDAO.SubredditDao;

public class SubredditsRepository implements SubredditsDataSource {
    private static SubredditsRepository instance = null;
    private RedditThreadDao threadDataDao;
    private SubredditDao subsDao;

    public SubredditsRepository(RedditThreadDao threadDao, SubredditDao subDao){
        threadDataDao = threadDao;
        subsDao = subDao;
    }

    public SubredditsRepository getInstance(RedditThreadDao threadDao, SubredditDao subDao){
        if(instance == null){
            instance = new SubredditsRepository(threadDao, subDao);
        }

        return instance;
    }

    @Override
    public boolean addSubreddit(Subreddit subreddit) {
        try {
            subsDao.save(subreddit);
            return true;
        }catch (SQLiteConstraintException e){
            Log.e("Sub Repository", e.toString());
            return false;
        }
    }

    @Override
    public boolean addRedditThread(RedditThread thread) {
        try {
            threadDataDao.insertOrReplace(thread);
            return true;
        }catch (SQLiteConstraintException e){
            Log.e("Sub Repo", e.toString());
            return false;
        }
    }

    @Override
    public List<Subreddit> getSubreddits() {
        return subsDao.queryBuilder()
                .orderDesc(SubredditDao.Properties.Id)
                .list();
    }

    @Override
    public List<RedditThread> getRedditThreads(String subreddit) {
        return threadDataDao.queryBuilder()
                .where(RedditThreadDao.Properties.Subreddit.eq(subreddit))
                .orderDesc(RedditThreadDao.Properties.Id)
                .list();
    }

    @Override
    public void deleteSubreddit(String subreddit){
        deleteAllThreadsFromSubreddit(subreddit);
        subsDao.queryBuilder()
                .where(SubredditDao.Properties.DisplayName.eq(subreddit))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();

    }

    @Override
    public void deleteAllSubreddits() {
        List<Subreddit> subreddits = subsDao.loadAll();
        for (Subreddit sub : subreddits){
            deleteAllThreadsFromSubreddit(sub.getDisplayName());
        }
        subsDao.deleteAll();
    }

    @Override
    public void deleteAllThreadsFromSubreddit(String subreddit) {
        DeleteQuery deleteQuery = threadDataDao.queryBuilder()
                .where(RedditThreadDao.Properties.Subreddit.eq(subreddit))
                .buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();
    }

    @Override
    public void deleteRedditThread(RedditThread thread) {
        threadDataDao.delete(thread);
    }
}

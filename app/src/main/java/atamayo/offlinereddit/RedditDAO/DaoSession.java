package atamayo.offlinereddit.RedditDAO;

import java.util.Map;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.AbstractDaoSession;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.identityscope.IdentityScopeType;
import org.greenrobot.greendao.internal.DaoConfig;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;

import atamayo.offlinereddit.RedditDAO.RedditThreadDao;
import atamayo.offlinereddit.RedditDAO.SubredditDao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * {@inheritDoc}
 * 
 * @see org.greenrobot.greendao.AbstractDaoSession
 */
public class DaoSession extends AbstractDaoSession {

    private final DaoConfig redditThreadDaoConfig;
    private final DaoConfig subredditDaoConfig;

    private final RedditThreadDao redditThreadDao;
    private final SubredditDao subredditDao;

    public DaoSession(Database db, IdentityScopeType type, Map<Class<? extends AbstractDao<?, ?>>, DaoConfig>
            daoConfigMap) {
        super(db);

        redditThreadDaoConfig = daoConfigMap.get(RedditThreadDao.class).clone();
        redditThreadDaoConfig.initIdentityScope(type);

        subredditDaoConfig = daoConfigMap.get(SubredditDao.class).clone();
        subredditDaoConfig.initIdentityScope(type);

        redditThreadDao = new RedditThreadDao(redditThreadDaoConfig, this);
        subredditDao = new SubredditDao(subredditDaoConfig, this);

        registerDao(RedditThread.class, redditThreadDao);
        registerDao(Subreddit.class, subredditDao);
    }
    
    public void clear() {
        redditThreadDaoConfig.clearIdentityScope();
        subredditDaoConfig.clearIdentityScope();
    }

    public RedditThreadDao getRedditThreadDao() {
        return redditThreadDao;
    }

    public SubredditDao getSubredditDao() {
        return subredditDao;
    }

}

package atamayo.offlinereddit.Data;

import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.query.DeleteQuery;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditObject;
import atamayo.offlinereddit.RedditAPI.RedditObjectDeserializer;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereddit.RedditDAO.RedditThreadDao;
import atamayo.offlinereddit.RedditDAO.SubredditDao;

public class SubredditsRepository implements SubredditsDataSource {
    private static SubredditsRepository instance = null;
    private RedditThreadDao mThreadDao;
    private SubredditDao mSubsDao;
    private CommentFileManager mCommentFileManager;

    public SubredditsRepository(RedditThreadDao threadDao, SubredditDao subDao, CommentFileManager commentFileManager){
        mThreadDao = threadDao;
        mSubsDao = subDao;
        mCommentFileManager = commentFileManager;
    }

    @Override
    public boolean addSubreddit(Subreddit subreddit) {
        try {
            mSubsDao.insert(subreddit);
            return true;
        }catch (SQLiteConstraintException e){
            Log.e("Sub Repository", e.toString());
            return false;
        }
    }

    @Override
    public boolean addRedditThread(RedditThread thread) {
        try {
            mThreadDao.insertOrReplace(thread);
            return true;
        }catch (SQLiteConstraintException e){
            Log.e("Sub Repo", e.toString());
            return false;
        }
    }

    @Override
    public List<Subreddit> getSubreddits() {
        return mSubsDao.queryBuilder()
                .orderDesc(SubredditDao.Properties.Id)
                .list();
    }

    @Override
    public List<RedditThread> getRedditThreads(String subredditName) {
        return mThreadDao.queryBuilder()
                    .where(RedditThreadDao.Properties.Subreddit.eq(subredditName))
                    .orderDesc(RedditThreadDao.Properties.Id)
                    .list();
    }

    @Override
    public List<RedditComment> getCommentsForThread(String threadFullName){
        List<RedditComment> comments = new ArrayList<>();
        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(RedditObject.class, new RedditObjectDeserializer())
                .create();

        String json = mCommentFileManager.loadFile(threadFullName);
        RedditObject[] objects = gson.fromJson(json, RedditObject[].class);

        if(objects[1] instanceof RedditListing){
            RedditListing listings = (RedditListing) objects[1];
            List<RedditObject> commentListings = listings.getChildren();

            for (RedditObject commentListing : commentListings) {
                if (commentListing instanceof RedditComment) {
                    RedditComment comment = (RedditComment) commentListing;
                    comments.add(comment);
                    getReplies(comments, comment);
                }
            }
        }
        return comments;
    }

    private void getReplies(List<RedditComment> commentsList, RedditComment comment){
        if(comment.getReplies() != null){
            RedditListing repliesListing = (RedditListing) comment.getReplies();
            List<RedditObject> replies = repliesListing.getChildren();

            for(RedditObject object : replies){
                if(object instanceof RedditComment) {
                    RedditComment reply = (RedditComment) object;
                    commentsList.add(reply);
                    getReplies(commentsList, reply);
                }
            }
        }
    }

    @Override
    public void deleteAllSubreddits() {
        List<Subreddit> subreddits = mSubsDao.loadAll();
        for (Subreddit sub : subreddits){
            deleteAllThreadsFromSubreddit(sub.getDisplayName());
        }
        mSubsDao.deleteAll();
    }

    @Override
    public void deleteSubreddit(String subredditName){
        deleteAllThreadsFromSubreddit(subredditName);
        mSubsDao.queryBuilder()
                .where(SubredditDao.Properties.DisplayName.eq(subredditName))
                .buildDelete()
                .executeDeleteWithoutDetachingEntities();

    }

    @Override
    public void deleteAllThreadsFromSubreddit(String subredditName) {
        List<RedditThread> threads = getRedditThreads(subredditName);
        for(RedditThread thread : threads){
            deleteAllCommentsFromThread(thread.getFullName());
        }

        DeleteQuery deleteQuery = mThreadDao.queryBuilder()
                .where(RedditThreadDao.Properties.Subreddit.eq(subredditName))
                .buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();
    }

    @Override
    public void deleteRedditThread(String threadFullname) {
        deleteAllCommentsFromThread(threadFullname);

        DeleteQuery deleteQuery = mThreadDao.queryBuilder()
                .where(RedditThreadDao.Properties.FullName.eq(threadFullname))
                .buildDelete();
        deleteQuery.executeDeleteWithoutDetachingEntities();
    }

    @Override
    public void deleteAllCommentsFromThread(String threadFullName){
        mCommentFileManager.deleteFile(threadFullName);
    }
}

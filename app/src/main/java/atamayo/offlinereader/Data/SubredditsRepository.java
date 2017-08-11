package atamayo.offlinereader.Data;

import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.greenrobot.greendao.query.DeleteQuery;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.RedditAPI.DuplicateSubredditException;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import atamayo.offlinereader.RedditAPI.RedditObjectDeserializer;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.RedditDAO.RedditThreadDao;
import atamayo.offlinereader.RedditDAO.SubredditDao;

/**
 * Implementation of SubredditsDataSource that uses a combination of GreenDao
 * and Android's file system to save subreddits, threads, and comments.
 */
public class SubredditsRepository implements SubredditsDataSource {
    private RedditThreadDao mThreadDao;
    private SubredditDao mSubsDao;
    private FileManager mFileManager;

    public SubredditsRepository(RedditThreadDao threadDao, SubredditDao subDao, FileManager fileManager){
        mThreadDao = threadDao;
        mSubsDao = subDao;
        mFileManager = fileManager;
    }

    @Override
    public boolean addSubreddit(Subreddit subreddit) {
        try {
            if(subreddit != null && subreddit.getDisplayName() != null) {
                mSubsDao.insert(subreddit);
                return true;
            }else{
                return false;
            }
        }catch (SQLiteConstraintException e){
            throw new DuplicateSubredditException();
        }
    }

    @Override
    public boolean addRedditThread(RedditThread thread) {
        try {
            if(thread != null && thread.getFullName() != null) {
                thread.setMediaPath(mFileManager.writeToFile(thread.getMediaFileName(), thread.getImageBytes()));
                mThreadDao.insertOrReplace(thread);
                return true;
            }else{
                return false;
            }
        }catch (SQLiteConstraintException e){
            return false;
        }
    }

    @Override
    public boolean addRedditComments(RedditThread thread, String comments){
        if(thread != null && thread.getFullName() != null && comments != null) {
            String path = mFileManager.writeToFile(thread.getCommentFileName(), comments.getBytes());
            thread.setCommentPath(path);
            updateThread(thread);
            return !path.isEmpty();
        }else{
            return false;
        }
    }

    @Override
    public void updateThread(RedditThread thread){
        if(thread != null && thread.getFullName() != null) {
            mThreadDao.update(thread);
        }
    }

    @Override
    public Subreddit getSubreddit(String displayName){
        if(displayName != null) {
            return mSubsDao.queryBuilder()
                    .where(SubredditDao.Properties.DisplayName.eq(displayName))
                    .unique();
        }else{
            return null;
        }
    }

    @Override
    public List<Subreddit> getSubreddits() {
        return mSubsDao.queryBuilder()
                .orderDesc(SubredditDao.Properties.Id)
                .list();
    }

    @Override
    public List<RedditThread> getRedditThreads(String subredditName, int offset, int limit) {
        if(offset >= 0 && limit >= 0 && subredditName != null) {
            return mThreadDao.queryBuilder()
                    .where(RedditThreadDao.Properties.Subreddit.eq(subredditName))
                    .orderDesc(RedditThreadDao.Properties.Id)
                    .offset(offset)
                    .limit(limit)
                    .list();
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public RedditThread getRedditThread(String fullname){
        if(fullname != null) {
            return mThreadDao.queryBuilder()
                    .where(RedditThreadDao.Properties.FullName.eq(fullname))
                    .unique();
        }else{
            return null;
        }
    }

    @Override
    public List<RedditComment> getCommentsForThread(String threadFullName, int offset, int limit){
        List<RedditComment> comments = new ArrayList<>();

        if(offset >= 0 && limit >= 0 && threadFullName != null) {
            RedditThread thread = getRedditThread(threadFullName);

            Gson gson = new GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .registerTypeAdapter(RedditObject.class, new RedditObjectDeserializer())
                    .create();

            String json = mFileManager.loadFile(thread.getCommentFileName());
            RedditObject[] objects = gson.fromJson(json, RedditObject[].class);

            if (objects[1] instanceof RedditListing) {
                RedditListing listings = (RedditListing) objects[1];
                List<RedditObject> commentListings = listings.getChildren();
                int last = limit + offset;

                for (int i = offset; i < last; i++) {
                    try {
                        RedditObject commentListing = commentListings.get(i);
                        if (commentListing instanceof RedditComment) {
                            RedditComment comment = (RedditComment) commentListing;
                            comments.add(comment);
                            getReplies(comments, comment);
                        }
                    } catch (IndexOutOfBoundsException e) {
                        return comments;
                    }
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
        if(subredditName != null) {
            deleteAllThreadsFromSubreddit(subredditName);
            mSubsDao.queryBuilder()
                    .where(SubredditDao.Properties.DisplayName.eq(subredditName))
                    .buildDelete()
                    .executeDeleteWithoutDetachingEntities();
        }

    }

    @Override
    public void deleteAllThreadsFromSubreddit(String subredditName) {
        if(subredditName != null) {
            List<RedditThread> threads = mThreadDao.queryBuilder()
                    .where(RedditThreadDao.Properties.Subreddit.eq(subredditName))
                    .orderDesc(RedditThreadDao.Properties.Id)
                    .list();

            for (RedditThread thread : threads) {
                deleteAllCommentsFromThread(thread.getFullName());
                mFileManager.deleteFile(thread.getMediaFileName());
            }

            DeleteQuery deleteQuery = mThreadDao.queryBuilder()
                    .where(RedditThreadDao.Properties.Subreddit.eq(subredditName))
                    .buildDelete();
            deleteQuery.executeDeleteWithoutDetachingEntities();
        }
    }

    @Override
    public void deleteRedditThread(String threadFullname) {
        if(threadFullname != null) {
            RedditThread thread = getRedditThread(threadFullname);
            mFileManager.deleteFile(thread.getMediaFileName());

            deleteAllCommentsFromThread(threadFullname);

            DeleteQuery deleteQuery = mThreadDao.queryBuilder()
                    .where(RedditThreadDao.Properties.FullName.eq(threadFullname))
                    .buildDelete();
            deleteQuery.executeDeleteWithoutDetachingEntities();
        }
    }

    @Override
    public void deleteAllCommentsFromThread(String threadFullName){
        if(threadFullName != null) {
            RedditThread thread = getRedditThread(threadFullName);
            mFileManager.deleteFile(thread.getCommentFileName());
        }
    }
}

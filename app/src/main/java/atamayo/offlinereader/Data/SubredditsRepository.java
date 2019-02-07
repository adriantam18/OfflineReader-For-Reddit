package atamayo.offlinereader.Data;

import android.database.sqlite.SQLiteConstraintException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import atamayo.offlinereader.RedditAPI.RedditObjectDeserializer;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

/**
 * Implementation of SubredditsDataSource that uses a combination of Room
 * and Android's file system to save subreddits, threads, and comments.
 */
public class SubredditsRepository implements SubredditsDataSource {
    private static SubredditsRepository sInstance;
    private final RedditDatabase mRedditDatabase;
    private final FileManager mFileManager;

    private SubredditsRepository(final RedditDatabase redditDatabase, final FileManager fileManager){
        mRedditDatabase = redditDatabase;
        mFileManager = fileManager;
    }

    public static SubredditsRepository getInstance(final RedditDatabase redditDatabase, final FileManager fileManager) {
        if (sInstance == null) {
            synchronized (SubredditsRepository.class) {
                if (sInstance == null) {
                    sInstance = new SubredditsRepository(redditDatabase, fileManager);
                }
            }
        }

        return sInstance;
    }

    @Override
    public boolean addSubreddit(Subreddit subreddit) {
        try {
            return (subreddit != null && subreddit.getDisplayName() != null)
                    && (mRedditDatabase.getSubredditDao().insertSubreddit(subreddit) >= 0);
        }catch (SQLiteConstraintException e){
            return false;
        }
    }

    @Override
    public boolean addRedditThread(RedditThread thread) {
        try {
            if(thread != null && thread.getFullName() != null) {
                thread.setMediaPath(mFileManager.writeToFile(thread.getMediaFileName(), thread.getImageBytes()));
                return mRedditDatabase.getRedditThreadDao().insertRedditThread(thread) >= 0;
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
            mRedditDatabase.getRedditThreadDao()
                    .updateRedditThread(thread);
        }
    }

    @Override
    public Subreddit getSubreddit(String displayName){
        return mRedditDatabase.getSubredditDao()
                .loadSubredditByDisplayName(displayName);
    }

    @Override
    public List<Subreddit> getSubreddits() {
        return mRedditDatabase.getSubredditDao()
                .loadSubreddits();
    }

    @Override
    public List<RedditThread> getRedditThreads(String subredditName, int offset, int limit) {
        if(offset >= 0 && limit >= 0 && subredditName != null) {
            return  mRedditDatabase.getRedditThreadDao()
                    .loadThreadsFromSubreddit(subredditName, offset, limit);
        }else{
            return new ArrayList<>();
        }
    }

    @Override
    public RedditThread getRedditThread(String fullname){
        return mRedditDatabase.getRedditThreadDao()
                .loadRedditThreadByFullName(fullname);
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
        List<Subreddit> subreddits = mRedditDatabase.getSubredditDao()
                .loadSubreddits();
        for (Subreddit subreddit : subreddits){
            deleteAllThreadsFromSubreddit(subreddit.getDisplayName());
        }
        mRedditDatabase.getSubredditDao()
                .deleteAllSubreddits();
    }

    @Override
    public void deleteSubreddit(String subredditName){
        deleteAllThreadsFromSubreddit(subredditName);
        mRedditDatabase.getSubredditDao()
                .deleteSubredditByDisplayName(subredditName);
    }

    @Override
    public void deleteAllThreadsFromSubreddit(String subredditName) {
        if(subredditName != null) {
            List<RedditThread> threads = mRedditDatabase.getRedditThreadDao()
                    .loadAllThreadsFromSubreddit(subredditName);

            for (RedditThread thread : threads) {
                deleteAllCommentsFromThread(thread.getFullName());
                mFileManager.deleteFile(thread.getMediaFileName());
            }

            mRedditDatabase.getRedditThreadDao()
                    .deleteThreadsFromSubreddit(subredditName);
        }
    }

    @Override
    public void deleteRedditThread(String threadFullname) {
        deleteAllCommentsFromThread(threadFullname);
        mRedditDatabase.getRedditThreadDao()
                .deleteRedditThreadByFullName(threadFullname);
    }

    @Override
    public void deleteAllCommentsFromThread(String threadFullName){
        if(threadFullName != null) {
            RedditThread thread = getRedditThread(threadFullName);

            if (thread != null) {
                mFileManager.deleteFile(thread.getCommentFileName());
            }
        }
    }
}

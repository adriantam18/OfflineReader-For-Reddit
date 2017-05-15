package atamayo.offlinereader.Data;

import java.util.List;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import io.reactivex.Observable;

public interface SubredditsDataSource {
    boolean addSubreddit(Subreddit subreddit);
    boolean addRedditThread(RedditThread thread);
    boolean addRedditComments(String threadFullName, String comments);

    List<Subreddit> getSubreddits();
    List<RedditThread> getRedditThreads(String subredditName);
    RedditThread getRedditThread(String fullName);
    List<RedditComment> getCommentsForThread(String threadFullName, int offset, int limit);
    Observable<List<RedditThread>> getThreads(String subredditName);

    void deleteAllSubreddits();
    void deleteSubreddit(String subredditName);
    void deleteRedditThread(String threadFullName);
    void deleteAllThreadsFromSubreddit(String subredditName);
    void deleteAllCommentsFromThread(String threadFullName);
}

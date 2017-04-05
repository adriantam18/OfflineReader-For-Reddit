package atamayo.offlinereader.Data;

import java.util.List;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

public interface SubredditsDataSource {
    boolean addSubreddit(Subreddit subreddit);
    boolean addRedditThread(RedditThread thread);

    List<Subreddit> getSubreddits();
    List<RedditThread> getRedditThreads(String subredditName);
    RedditThread getRedditThread(String fullName);
    List<RedditComment> getCommentsForThread(String threadFullName, int limit, int offset);

    void deleteAllSubreddits();
    void deleteSubreddit(String subredditName);
    void deleteRedditThread(String threadFullName);
    void deleteAllThreadsFromSubreddit(String subredditName);
    void deleteAllCommentsFromThread(String threadFullName);
}

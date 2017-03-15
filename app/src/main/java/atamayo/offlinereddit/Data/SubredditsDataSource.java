package atamayo.offlinereddit.Data;

import java.util.List;

import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditAPI.Subreddit;

public interface SubredditsDataSource {
    boolean addSubreddit(Subreddit subreddit);
    boolean addRedditThread(RedditThread thread);
    List<Subreddit> getSubreddits();
    List<RedditThread> getRedditThreads(String subreddit);
    void deleteSubreddit(String subreddit);
    void deleteRedditThread(RedditThread thread);
    void deleteAllSubreddits();
    void deleteAllThreadsFromSubreddit(String subreddit);
}

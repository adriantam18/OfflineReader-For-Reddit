package atamayo.offlinereddit.Data;

import java.util.List;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;

public interface SubredditsDataSource {
    boolean addSubreddit(Subreddit subreddit);
    boolean addRedditThread(RedditThread thread);

    List<Subreddit> getSubreddits();
    List<RedditThread> getRedditThreads(String subredditName);
    List<RedditComment> getCommentsForThread(String threadFullName, int limit, int offset);

    void deleteAllSubreddits();
    void deleteSubreddit(String subredditName);
    void deleteRedditThread(String threadFullName);
    void deleteAllThreadsFromSubreddit(String subredditName);
    void deleteAllCommentsFromThread(String threadFullname);
}

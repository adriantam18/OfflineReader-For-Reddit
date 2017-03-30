package atamayo.offlinereddit.Subreddits;

import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;

public interface SubListCallbacks {
    void OnOpenListOfThreads(Subreddit subreddit);
    void OnOpenListOfKeywords(Subreddit subreddit);
    void OnDeleteSubreddit(Subreddit subreddit);
}

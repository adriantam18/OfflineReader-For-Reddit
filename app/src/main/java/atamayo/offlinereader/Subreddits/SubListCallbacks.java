package atamayo.offlinereader.Subreddits;

import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

public interface SubListCallbacks {
    void OnOpenListOfThreads(Subreddit subreddit);
    void OnOpenListOfKeywords(Subreddit subreddit);
    void OnDeleteSubreddit(Subreddit subreddit);
}

package atamayo.offlinereddit.Subreddits;

import java.util.List;

import atamayo.offlinereddit.BaseView;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;

public interface SubredditsContract {
    interface View extends BaseView<Presenter> {
        void showSubreddits(List<Subreddit> subreddits);
        void showAddedSubreddit(Subreddit subreddit);
        void showSubredditThreads(String subredditName);
        void showSubredditKeywords(String subredditName);
        void showClearedSubreddits();
        void showError(String message);
    }

    interface Presenter {
        void initSubredditsList();
        void addIfExists(String subreddit);
        void removeSubreddit(Subreddit subreddit);
        void clearSubreddits();
        void openSubredditKeywords(Subreddit subreddit);
        void openSubredditThreads(Subreddit subreddit);
    }
}

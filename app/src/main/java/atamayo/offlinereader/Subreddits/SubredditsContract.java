package atamayo.offlinereader.Subreddits;

import java.util.List;

import atamayo.offlinereader.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;

public interface SubredditsContract {
    interface View extends BaseView<Presenter> {
        void showSubreddits(List<Subreddit> subreddits);
        void showAddedSubreddit(Subreddit subreddit);
        void showSubredditThreads(String subredditName);
        void showSubredditKeywords(String subredditName);
        void showClearedSubreddits();
        void showError(String message);
        void showLoading(boolean isLoading);
    }

    interface Presenter {
        void initSubredditsList();
        void addIfExists(String subreddit);
        void removeSubreddit(Subreddit subreddit);
        void clearSubreddits();
        void openSubredditKeywords(Subreddit subreddit);
        void openSubredditThreads(Subreddit subreddit);
        void unsubscribe();
    }
}

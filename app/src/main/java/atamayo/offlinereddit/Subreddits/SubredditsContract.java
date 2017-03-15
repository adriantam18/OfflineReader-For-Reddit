package atamayo.offlinereddit.Subreddits;

import java.util.List;

import atamayo.offlinereddit.BaseView;
import atamayo.offlinereddit.RedditAPI.Subreddit;

public interface SubredditsContract {
    interface View extends BaseView<Presenter> {
        void showInitialSubreddits(List<Subreddit> subreddits);
        void showAddedSubreddit(int position);
        void showRemovedSubreddits(int start, int itemCount);
        void showClearedSubreddits();
        void showSubredditThreads(String subreddit);
        void showKeywords(String subreddit);
        void showLoading(boolean isLoading);
        void showError(String message);
        void startDownloadService(List<String> subreddits);
    }

    interface Presenter {
        void initSubredditsList();
        void addSubreddit(String subreddit);
        void removeSubreddit(int position);
        void clearSubreddits();
        void openListOfThreads(int position);
        void openListOfKeywords(int position);
        void downloadThreads();
    }
}

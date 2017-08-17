package atamayo.offlinereader.SubThreads;

import java.util.List;

import atamayo.offlinereader.MVP.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

/**
 * This specifies the contract between the view and presenter for Reddit threads.
 */
public interface SubThreadsContract {
    interface View extends BaseView{
        void showInitialThreads(List<RedditThread> threads);
        void showMoreThreads(List<RedditThread> threads);
        void showEmptyThreads();
        void showLoading(boolean isLoading);
        void showCommentsPage(String threadFullName);
        void startDownloadService(List<String> subreddit);
    }

    interface Presenter{
        void getThreads(boolean firstLoad, int offset, int limit);
        void removeThread(RedditThread thread);
        void removeAllThreads();
        void openCommentsPage(RedditThread thread);
        void downloadThreads();
    }
}

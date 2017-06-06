package atamayo.offlinereader.SubThreads;

import java.util.List;

import atamayo.offlinereader.BasePresenter;
import atamayo.offlinereader.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.ThreadComments.ThreadCommentsContract;

public interface SubThreadsContract {
    interface View extends BaseView<SubThreadsContract.Presenter>{
        void showInitialThreads(List<RedditThread> threads);
        void showMoreThreads(List<RedditThread> threads);
        void showEmptyThreads();
        void showCommentsPage(String threadFullName);
        void startDownloadService(List<String> subreddit);
    }

    interface Presenter extends BasePresenter<SubThreadsContract.View>{
        void initSubThreadsList(String subreddit, int offset, int limit);
        void getMoreThreads(int offset, int limit);
        void removeThread(RedditThread thread);
        void removeAllThreads();
        void openCommentsPage(RedditThread thread);
        void downloadThreads();
    }
}

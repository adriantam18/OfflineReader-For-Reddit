package atamayo.offlinereddit.SubThreads;

import java.util.List;

import atamayo.offlinereddit.BaseView;
import atamayo.offlinereddit.RedditAPI.RedditThread;

public interface SubThreadsContract {
    interface View extends BaseView<SubThreadsContract.Presenter>{
        void showInitialThreads(List<RedditThread> threads);
        void showRemovedThreads(int start, int itemCount);
        void showCommentsPage(RedditThread thread);
        void startDownloadService(List<String> subreddit);
        void showErrorMessage(String message);
    }

    interface Presenter{
        void initSubThreadsList(String subreddit);
        void removeThread(List<Integer> positions);
        void removeAllThreads();
        void openCommentsPage(int position);
        void downloadThreads();
    }
}

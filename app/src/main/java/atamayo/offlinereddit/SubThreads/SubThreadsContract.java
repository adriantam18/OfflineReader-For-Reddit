package atamayo.offlinereddit.SubThreads;

import java.util.List;

import atamayo.offlinereddit.BaseView;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;

public interface SubThreadsContract {
    interface View extends BaseView<SubThreadsContract.Presenter>{
        void showInitialThreads(List<RedditThread> threads);
        void showEmptyThreads();
        void showCommentsPage(String threadFullName);
        void startDownloadService(List<String> subreddit);
    }

    interface Presenter{
        void initSubThreadsList(String subreddit);
        void removeThread(RedditThread thread);
        void removeAllThreads();
        void openCommentsPage(RedditThread thread);
        void downloadThreads();
    }
}

package atamayo.offlinereader.SubThreads;

import java.util.List;

import atamayo.offlinereader.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

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

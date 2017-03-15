package atamayo.offlinereddit.ThreadComments;

import atamayo.offlinereddit.BaseView;
import atamayo.offlinereddit.RedditAPI.RedditThread;

public interface ThreadCommentsContract {
    interface View extends BaseView<Presenter>{
        void showCommentsFromFile(String filename);
        void showCommentsFromUrl(String url);
    }

    interface Presenter{
        void initCommentsView(String filename);
        void onThreadClicked(RedditThread thread);
    }
}

package atamayo.offlinereader.ThreadComments;

import java.util.List;

import atamayo.offlinereader.MVP.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

/**
 * This specifies the contract between the view and presenter for thread comments.
 */
public interface ThreadCommentsContract {
    interface View extends BaseView{
        void showParentThread(RedditThread thread);
        void showInitialComments(List<RedditComment> comments);
        void showMoreComments(List<RedditComment> comments);
        void showEmptyComments();
        void showLoading(boolean isLoading);
    }

    interface Presenter{
        void getParentThread();
        void getComments(boolean firstLoad, int offset, int limit);
    }
}

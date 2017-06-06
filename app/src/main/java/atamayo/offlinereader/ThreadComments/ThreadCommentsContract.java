package atamayo.offlinereader.ThreadComments;

import java.util.List;

import atamayo.offlinereader.BasePresenter;
import atamayo.offlinereader.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

public interface ThreadCommentsContract {
    interface View extends BaseView<Presenter>{
        void showParentThread(RedditThread thread);
        void showInitialComments(List<RedditComment> comments);
        void showMoreComments(List<RedditComment> comments);
        void showLoading(boolean isLoading);
    }

    interface Presenter extends BasePresenter<ThreadCommentsContract.View>{
        void initCommentsView(String threadFullName, int offset, int limit);
        void getMoreComments(int offset, int limit);
    }
}

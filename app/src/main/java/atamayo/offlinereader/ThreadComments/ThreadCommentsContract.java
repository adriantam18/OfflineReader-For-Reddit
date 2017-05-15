package atamayo.offlinereader.ThreadComments;

import java.util.List;

import atamayo.offlinereader.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

public interface ThreadCommentsContract {
    interface View extends BaseView<Presenter>{
        void showParentThread(RedditThread thread);
        void showComments(List<RedditComment> comments);
        void showMoreComments(List<RedditComment> comments);
        void showLoading(boolean isLoading);
    }

    interface Presenter{
        void initCommentsView(String threadFullName);
        void getMoreComments();
        void unsubscribe();
    }
}

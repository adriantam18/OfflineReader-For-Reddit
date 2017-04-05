package atamayo.offlinereader.ThreadComments;

import java.util.List;

import atamayo.offlinereader.BaseView;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

public interface ThreadCommentsContract {
    interface View extends BaseView<Presenter>{
        void showSelfText(RedditThread thread);
        void showComments(List<RedditComment> comments);
        void showMoreComments(List<RedditComment> comments);
    }

    interface Presenter{
        void initCommentsView(String threadFullName);
        void getMoreComments();
    }
}

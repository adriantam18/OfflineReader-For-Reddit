package atamayo.offlinereddit.ThreadComments;

import java.util.List;

import atamayo.offlinereddit.BaseView;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditObject;

public interface ThreadCommentsContract {
    interface View extends BaseView<Presenter>{
        void showComments(List<RedditComment> comments);
        void showMoreComments(List<RedditComment> comments);
    }

    interface Presenter{
        void initCommentsView(String threadFullName);
        void getMoreComments();
    }
}

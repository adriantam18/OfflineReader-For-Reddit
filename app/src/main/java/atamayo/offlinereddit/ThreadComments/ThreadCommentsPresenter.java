package atamayo.offlinereddit.ThreadComments;

import java.util.List;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;

public class ThreadCommentsPresenter implements ThreadCommentsContract.Presenter {
    private ThreadCommentsContract.View mView;
    private SubredditsDataSource mRepository;
    private String mThreadFullName;
    private int mLimit = 15;
    private int mOffset;

    public ThreadCommentsPresenter(SubredditsDataSource dataSource, ThreadCommentsContract.View view){
        mView = view;
        mRepository = dataSource;
        mOffset = 0;
    }

    @Override
    public void initCommentsView(String threadFullName) {
        mThreadFullName = threadFullName;
        mView.showComments(getComments());
    }

    @Override
    public void getMoreComments(){
        mView.showMoreComments(getComments());
    }

    private List<RedditComment> getComments(){
        List<RedditComment> commentList = mRepository.getCommentsForThread(mThreadFullName, mLimit, mOffset);
        mOffset += mLimit;
        return commentList;
    }
}

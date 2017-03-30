package atamayo.offlinereddit.ThreadComments;

import java.util.List;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;

public class ThreadCommentsPresenter implements ThreadCommentsContract.Presenter {
    private ThreadCommentsContract.View mView;
    private SubredditsDataSource mRepository;

    public ThreadCommentsPresenter(SubredditsDataSource dataSource, ThreadCommentsContract.View view){
        mView = view;
        mRepository = dataSource;
    }

    @Override
    public void initCommentsView(String threadFullName) {
        List<RedditComment> commentList = mRepository.getCommentsForThread(threadFullName);
        mView.showComments(commentList);
    }
}

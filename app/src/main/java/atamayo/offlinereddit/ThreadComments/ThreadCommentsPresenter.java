package atamayo.offlinereddit.ThreadComments;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.RedditAPI.RedditThread;

public class ThreadCommentsPresenter implements ThreadCommentsContract.Presenter {
    ThreadCommentsContract.View mView;
    SubredditsDataSource mRepository;

    public ThreadCommentsPresenter(SubredditsDataSource dataSource, ThreadCommentsContract.View view){
        mView = view;
        mRepository = dataSource;
    }

    @Override
    public void initCommentsView(String filename) {
        mView.showCommentsFromFile(filename);
    }

    @Override
    public void onThreadClicked(RedditThread thread){
        mRepository.addRedditThread(thread);
    }
}

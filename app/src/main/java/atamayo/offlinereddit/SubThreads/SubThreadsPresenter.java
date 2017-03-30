package atamayo.offlinereddit.SubThreads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;

public class SubThreadsPresenter implements SubThreadsContract.Presenter {
    private String mSubreddit;
    private SubredditsDataSource mRepository;
    private SubThreadsContract.View mView;

    public SubThreadsPresenter(SubredditsDataSource repository, SubThreadsContract.View view){
        mRepository = repository;
        mView = view;
    }

    @Override
    public void initSubThreadsList(String subreddit) {
        mSubreddit = subreddit;
        List<RedditThread> threads = mRepository.getRedditThreads(subreddit);
        if(!threads.isEmpty()) {
            mView.showInitialThreads(threads);
        }else {
            mView.showEmptyThreads();
        }
    }

    @Override
    public void removeThread(RedditThread thread) {
        mRepository.deleteRedditThread(thread.getFullName());
        mView.showInitialThreads(mRepository.getRedditThreads(mSubreddit));
    }

    @Override
    public void removeAllThreads(){
        mRepository.deleteAllThreadsFromSubreddit(mSubreddit);
        mView.showEmptyThreads();
    }

    @Override
    public void downloadThreads(){
        mView.startDownloadService(new ArrayList<>(Arrays.asList(mSubreddit)));
    }

    @Override
    public void openCommentsPage(RedditThread thread) {
        thread.setWasClicked(true);
        mRepository.addRedditThread(thread);
        mView.showCommentsPage(thread.getFullName());
    }
}

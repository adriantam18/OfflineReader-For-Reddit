package atamayo.offlinereddit.SubThreads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditAPI.Subreddit;

public class SubThreadsPresenter implements SubThreadsContract.Presenter {
    private List<RedditThread> mThreadsList;
    private String mSubreddit;
    private SubredditsDataSource mRepository;
    private SubThreadsContract.View mView;

    public SubThreadsPresenter(SubredditsDataSource repository, SubThreadsContract.View view){
        mThreadsList = new ArrayList<>();
        mRepository = repository;
        mView = view;
    }

    @Override
    public void initSubThreadsList(String subreddit) {
        mSubreddit = subreddit;
        mThreadsList = mRepository.getRedditThreads(subreddit);
        if(!mThreadsList.isEmpty()) {
            mView.showInitialThreads(mThreadsList);
        }else {
            mView.showErrorMessage("No threads to show.");
        }
    }

    @Override
    public void removeThread(List<Integer> positions) {

        int itemCount = mThreadsList.size();
        for(int pos : positions){
            mRepository.deleteRedditThread(mThreadsList.get(pos));
            mThreadsList.remove(pos);
        }

        mView.showRemovedThreads(positions.get(0), itemCount);
    }

    @Override
    public void removeAllThreads(){
        if(!mSubreddit.isEmpty()) {
            mRepository.deleteAllThreadsFromSubreddit(mSubreddit);
            mThreadsList.clear();
        }
        mView.showRemovedThreads(0, 0);
    }

    @Override
    public void downloadThreads(){
        mView.startDownloadService(new ArrayList<>(Arrays.asList(mSubreddit)));
    }

    @Override
    public void openCommentsPage(int position) {
        RedditThread thread = mThreadsList.get(position);
        thread.setWasClicked(true);
        mRepository.addRedditThread(thread);
        mView.showCommentsPage(thread);
    }
}

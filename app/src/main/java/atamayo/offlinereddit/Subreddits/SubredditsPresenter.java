package atamayo.offlinereddit.Subreddits;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.RedditAPI.Subreddit;
import atamayo.offlinereddit.Utils.NetworkResponse;
import atamayo.offlinereddit.Utils.RedditDownloader;

public class SubredditsPresenter implements SubredditsContract.Presenter{
    private final static String ERROR_ADDING = "Failed to add. You might have it already";
    private final static String TAG = "Subreddit Presenter";
    private SubredditsDataSource mRepository;
    private SubredditsContract.View mView;
    private List<Subreddit> mSubreddits;

    public SubredditsPresenter(SubredditsDataSource repository, SubredditsContract.View view){
        mRepository = repository;
        mView = view;
        mSubreddits = new ArrayList<>();
    }


    @Override
    public void initSubredditsList() {
        mView.showLoading(true);

        mSubreddits = mRepository.getSubreddits();
        mView.showInitialSubreddits(mSubreddits);

        mView.showLoading(false);
    }

    @Override
    public void addSubreddit(String subName) {
        if(!isAlreadyAdded(subName)) {
            RedditDownloader.getInstance().isValidSubreddit(subName,
                    new NetworkResponse() {
                        @Override
                        public void onSuccess(Subreddit subreddit) {
                            if (mRepository.addSubreddit(subreddit)) {
                                mSubreddits.add(0, subreddit);
                                mView.showAddedSubreddit(0);
                            } else {
                                mView.showError(ERROR_ADDING);
                            }
                        }

                        @Override
                        public void onError(String message) {
                            mView.showError(message);
                        }
                    });
        }else {
            mView.showError(ERROR_ADDING);
        }
    }

    @Override
    public void removeSubreddit(int position) {
        mView.showLoading(true);

        mRepository.deleteSubreddit(mSubreddits.get(position).getDisplayName());
        mSubreddits.remove(position);
        mView.showRemovedSubreddits(0, 0);

        mView.showLoading(false);
    }

    @Override
    public void clearSubreddits() {
        mView.showLoading(true);

        mRepository.deleteAllSubreddits();
        mView.showClearedSubreddits();

        mView.showLoading(false);
    }

    @Override
    public void openListOfThreads(int position) {
        mView.showSubredditThreads(mSubreddits.get(position).getDisplayName());
    }

    @Override
    public void openListOfKeywords(int position){
        mView.showKeywords(mSubreddits.get(position).getDisplayName());
    }

    @Override
    public void downloadThreads(){
        ArrayList<String> subsToDownload = new ArrayList<>();
        for(Subreddit sub : mSubreddits){
            subsToDownload.add(sub.getDisplayName());
        }
        mView.startDownloadService(subsToDownload);
    }

    private boolean isAlreadyAdded(String subreddit){
        for(Subreddit sub : mSubreddits){
            if(sub.getDisplayName().equals(subreddit)){
                return true;
            }
        }

        return false;
    }
}

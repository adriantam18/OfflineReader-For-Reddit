package atamayo.offlinereddit.Subreddits;

import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereddit.Utils.NetworkResponse;
import atamayo.offlinereddit.Utils.RedditDownloader;

public class SubredditsPresenter implements SubredditsContract.Presenter{
    private final static String TAG = "Subreddit Presenter";
    private SubredditsDataSource mRepository;
    private SubredditsContract.View mView;

    public SubredditsPresenter(SubredditsDataSource repository, SubredditsContract.View view){
        mRepository = repository;
        mView = view;
    }

    @Override
    public void initSubredditsList() {
        mView.showSubreddits(mRepository.getSubreddits());
    }

    @Override
    public void addIfExists(String subName) {
        RedditDownloader.getInstance().isValidSubreddit(subName,
                new NetworkResponse() {
                    @Override
                    public void onSuccess(Subreddit subreddit) {
                        if(mRepository.addSubreddit(subreddit)){
                            mView.showAddedSubreddit(subreddit);
                        }else{
                            String message = "Failed to add. You may already have r/" + subreddit.getDisplayName() + " on your list";
                            mView.showError(message);
                        }
                    }
                    @Override
                    public void onError(String message) {
                            mView.showError(message);
                        }
                });
    }

    @Override
    public void clearSubreddits() {
        mRepository.deleteAllSubreddits();
        mView.showClearedSubreddits();
    }

    @Override
    public void removeSubreddit(Subreddit subreddit) {
        mRepository.deleteSubreddit(subreddit.getDisplayName());
        mView.showSubreddits(mRepository.getSubreddits());
    }

    @Override
    public void openSubredditKeywords(Subreddit subreddit) {
        mView.showSubredditKeywords(subreddit.getDisplayName());
    }

    @Override
    public void openSubredditThreads(Subreddit subreddit) {
        mView.showSubredditThreads(subreddit.getDisplayName());
    }
}

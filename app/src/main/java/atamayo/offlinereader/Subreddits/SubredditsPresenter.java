package atamayo.offlinereader.Subreddits;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.Utils.NetworkResponse;
import atamayo.offlinereader.Utils.RedditDownloader;

public class SubredditsPresenter implements SubredditsContract.Presenter{
    private final static String TAG = "Subreddit Presenter";
    private SubredditsDataSource mRepository;
    private SubredditsContract.View mView;
    private RedditDownloader mDownloader;

    public SubredditsPresenter(SubredditsDataSource repository, SubredditsContract.View view, RedditDownloader redditDownloader){
        mRepository = repository;
        mView = view;
        mDownloader = redditDownloader;
    }

    @Override
    public void initSubredditsList() {
        mView.showSubreddits(mRepository.getSubreddits());
    }

    @Override
    public void addIfExists(String subName) {
        mDownloader.isValidSubreddit(subName,
                new NetworkResponse<Subreddit>() {
                    @Override
                    public void onSuccess(Subreddit object) {
                        if (mRepository.addSubreddit(object)) {
                            mView.showAddedSubreddit(object);
                        } else {
                            String message = "Failed to add. You may already have r/" + object.getDisplayName() + " on your list";
                            mView.showError(message);
                        }
                    }

                    @Override
                    public void onError(String message) {

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

package atamayo.offlinereader.Subreddits;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.DuplicateSubredditException;
import atamayo.offlinereader.RedditAPI.InvalidSubredditException;
import atamayo.offlinereader.RedditAPI.NoConnectionException;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.Utils.RedditDownloader;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SubredditsPresenter implements SubredditsContract.Presenter{
    private final static String TAG = "Subreddit Presenter";
    private SubredditsDataSource mRepository;
    private SubredditsContract.View mView;
    private RedditDownloader mDownloader;
    private CompositeDisposable disposables;

    public SubredditsPresenter(SubredditsDataSource repository, SubredditsContract.View view, RedditDownloader downloader) {
        mRepository = repository;
        mView = view;
        mDownloader = downloader;
        disposables = new CompositeDisposable();
    }

    @Override
    public void initSubredditsList() {
        mView.showSubreddits(mRepository.getSubreddits());
    }

    @Override
    public void addIfExists(String subName) {
        disposables.add(
            mDownloader.checkSubreddit(subName)
                    .subscribeOn(Schedulers.io())
                    .map(subreddit -> mRepository.addSubreddit(subreddit) ? subreddit : null)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::processSubreddit,
                            this::processError,
                            () -> mView.showLoading(false))
        );
    }

    private void processSubreddit(Subreddit subreddit){
        mView.showAddedSubreddit(subreddit);
        mView.showLoading(false);
    }

    private void processError(Throwable throwable){
        if(throwable instanceof NoConnectionException
                || throwable instanceof InvalidSubredditException
                || throwable instanceof DuplicateSubredditException){
            mView.showError(throwable.getMessage());
        }else{
            mView.showError("Failed to add");
        }
        mView.showLoading(false);
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

    @Override
    public void unsubscribe(){
        disposables.clear();
    }
}

package atamayo.offlinereader.Subreddits;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.MVP.BaseRxPresenter;
import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.DuplicateSubredditException;
import atamayo.offlinereader.RedditAPI.InvalidSubredditException;
import atamayo.offlinereader.RedditAPI.NoConnectionException;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.Utils.RedditDownloader;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Observable;

/**
 * Presenter for UI ({@link SubredditsListing}) that displays a list of subreddits.
 */
public class SubredditsPresenter extends BaseRxPresenter<SubredditsContract.View>
        implements SubredditsContract.Presenter {
    private final static String FAILED_TO_ADD = "Failed to add. Subreddit may not exist";
    private final static String FAILED_TO_LOAD = "Failed to load subreddits";
    private final static String FAILED_TO_DELETE = "Failed to delete subreddit";
    private final static String FAILED_TO_CLEAR_ALL = "Failed to clear subreddits";
    private SubredditsDataSource mSubredditsRepository;
    private KeywordsDataSource mKeywordsRepository;
    private RedditDownloader mDownloader;
    private BaseScheduler mScheduler;

    public SubredditsPresenter(SubredditsDataSource repository, RedditDownloader downloader,
                               KeywordsDataSource keywords, BaseScheduler scheduler) {
        mSubredditsRepository = repository;
        mDownloader = downloader;
        mKeywordsRepository = keywords;
        mScheduler = scheduler;
    }

    @Override
    public void getSubreddits() {
        getView().showLoading(true);

        mDisposables.add(Observable.fromCallable(() -> mSubredditsRepository.getSubreddits())
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(subreddits -> getView().showSubreddits(subreddits),
                        throwable -> {
                            getView().showLoading(false);
                            getView().showError(FAILED_TO_LOAD);
                        },
                        () -> getView().showLoading(false)));
    }

    @Override
    public void addIfExists(String subName) {
        getView().showLoading(true);

        if (subName != null) {
            mDisposables.add(mDownloader.checkSubreddit(subName)
                    .subscribeOn(mScheduler.io())
                    .map(subreddit -> mSubredditsRepository.addSubreddit(subreddit) ? subreddit : null)
                    .observeOn(mScheduler.mainThread())
                    .subscribe(this::processAddedSubreddit,
                            this::processAddError,
                            () -> getView().showLoading(false))
            );
        } else {
            getView().showError(FAILED_TO_ADD);
            getView().showLoading(false);
        }
    }

    private void processAddedSubreddit(Subreddit subreddit) {
        if (subreddit != null) {
            getView().showAddedSubreddit(subreddit);
        } else {
            getView().showError(FAILED_TO_ADD);
        }
    }

    private void processAddError(Throwable throwable) {
        if (throwable instanceof NoConnectionException
                || throwable instanceof InvalidSubredditException
                || throwable instanceof DuplicateSubredditException) {
            getView().showError(throwable.getMessage());
        } else {
            getView().showError(FAILED_TO_ADD);
        }
        getView().showLoading(false);
    }

    @Override
    public void clearSubreddits() {
        mDisposables.add(Observable.fromCallable(() -> mSubredditsRepository.getSubreddits())
                .subscribeOn(mScheduler.io())
                .flatMap(Observable::fromIterable)
                .doOnNext(subreddit -> mKeywordsRepository.clearKeywords(subreddit.getDisplayName()))
                .doOnNext(subreddit -> mSubredditsRepository.deleteSubreddit(subreddit.getDisplayName()))
                .observeOn(mScheduler.mainThread())
                .subscribe(sub -> {
                        },
                        throwable -> getView().showError(FAILED_TO_CLEAR_ALL),
                        () -> getView().showSubreddits(new ArrayList<>())));
    }

    @Override
    public void removeSubreddit(Subreddit subreddit) {
        if (subreddit != null && subreddit.getDisplayName() != null) {
            mDisposables.add(Observable.just(subreddit)
                    .subscribeOn(mScheduler.io())
                    .doOnNext(sub -> mKeywordsRepository.clearKeywords(sub.getDisplayName()))
                    .doOnNext(sub -> mSubredditsRepository.deleteSubreddit(sub.getDisplayName()))
                    .observeOn(mScheduler.mainThread())
                    .subscribe(sub -> {
                            },
                            throwable -> getView().showError(FAILED_TO_DELETE),
                            this::getSubreddits));
        }
    }

    @Override
    public void openSubredditKeywords(Subreddit subreddit) {
        if (subreddit != null && subreddit.getDisplayName() != null) {
            getView().showSubredditKeywords(subreddit.getDisplayName());
        }
    }

    @Override
    public void openSubredditThreads(Subreddit subreddit) {
        if (subreddit != null && subreddit.getDisplayName() != null) {
            getView().showSubredditThreads(subreddit.getDisplayName());
        }
    }

    @Override
    protected SubredditsContract.View createFakeView() {
        return new SubredditsContract.View() {
            @Override
            public void showSubreddits(List<Subreddit> subreddits) {

            }

            @Override
            public void showAddedSubreddit(Subreddit subreddit) {

            }

            @Override
            public void showSubredditThreads(String subredditName) {

            }

            @Override
            public void showSubredditKeywords(String subredditName) {

            }

            @Override
            public void showError(String message) {

            }

            @Override
            public void showLoading(boolean isLoading) {

            }
        };
    }
}

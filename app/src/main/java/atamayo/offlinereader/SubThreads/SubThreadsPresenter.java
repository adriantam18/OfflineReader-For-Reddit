package atamayo.offlinereader.SubThreads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereader.MVP.BaseRxPresenter;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Presenter for UI ({@link SubThreadsListing}) that displays a list of
 * Reddit threads. IT allows retrieval and deletion of threads.
 */
public class SubThreadsPresenter extends BaseRxPresenter<SubThreadsContract.View>
        implements SubThreadsContract.Presenter {
    private Subreddit mSubreddit;
    private SubredditsDataSource mRepository;
    private BaseScheduler mScheduler;
    private int mItemsShown;

    public SubThreadsPresenter(String subredditName,
                               SubredditsDataSource repository,
                               BaseScheduler scheduler) {
        mRepository = repository;
        mScheduler = scheduler;
        mItemsShown = 0;
        mSubreddit = mRepository.getSubreddit(subredditName);
    }

    @Override
    public void getThreads(boolean firstLoad, int offset, int limit) {
        getView().showLoading(true);

        Observable<List<RedditThread>> threadsObservable = Observable.fromCallable(() ->
                mRepository.getRedditThreads(mSubreddit.getDisplayName(), offset, limit));

        mDisposables.add(threadsObservable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(threads -> processThreads(threads, firstLoad),
                        throwable -> processError(throwable, firstLoad),
                        () -> {
                        }));
    }

    private void processThreads(List<RedditThread> threads, boolean firstLoad) {
        if (firstLoad) {
            if (!threads.isEmpty()) {
                getView().showInitialThreads(threads);
            } else {
                getView().showEmptyThreads();
            }
            mItemsShown = threads.size();
        } else {
            getView().showMoreThreads(threads);
            mItemsShown += threads.size();
        }

        getView().showLoading(false);
    }

    private void processError(Throwable throwable, boolean firstLoad) {
        if (firstLoad) {
            getView().showEmptyThreads();
        }

        getView().showLoading(false);
    }

    @Override
    public void removeThread(RedditThread thread) {
        --mItemsShown;
        Completable threadCompletable = Completable.fromRunnable(() ->
                mRepository.deleteRedditThread(thread.getFullName()));

        mDisposables.add(threadCompletable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(() -> getThreads(true, 0, mItemsShown)));
    }

    @Override
    public void removeAllThreads() {
        mItemsShown = 0;

        Completable threadCompletable = Completable.fromRunnable(() ->
                mRepository.deleteAllThreadsFromSubreddit(mSubreddit.getDisplayName()));

        mDisposables.add(threadCompletable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe());

        getView().showEmptyThreads();
    }

    @Override
    public void downloadThreads() {
        getView().startDownloadService(new ArrayList<>(Arrays.asList(mSubreddit.getDisplayName())));
    }

    @Override
    public void openCommentsPage(RedditThread thread) {
        thread.setWasClicked(true);

        Completable threadCompletable = Completable.fromRunnable(() ->
                mRepository.updateThread(thread));

        mDisposables.add(threadCompletable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe());

        getView().showCommentsPage(thread.getFullName());
    }

    @Override
    protected SubThreadsContract.View createFakeView() {
        return new SubThreadsContract.View() {
            @Override
            public void showInitialThreads(List<RedditThread> threads) {

            }

            @Override
            public void showMoreThreads(List<RedditThread> threads) {

            }

            @Override
            public void showEmptyThreads() {

            }

            @Override
            public void showLoading(boolean isLoading) {

            }

            @Override
            public void showCommentsPage(String threadFullName) {

            }

            @Override
            public void startDownloadService(List<String> subreddit) {

            }
        };
    }
}

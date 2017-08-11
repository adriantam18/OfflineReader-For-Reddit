package atamayo.offlinereader.SubThreads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Presenter for UI ({@link SubThreadsListing}) that displays a list of
 * Reddit threads. IT allows retrieval and deletion of threads.
 */
public class SubThreadsPresenter implements SubThreadsContract.Presenter {
    private Subreddit mSubreddit;
    private SubredditsDataSource mRepository;
    private SubThreadsContract.View mView;
    private BaseScheduler mScheduler;
    private CompositeDisposable mDisposables;
    private int mItemsShown;

    public SubThreadsPresenter(String subredditName,
                               SubredditsDataSource repository,
                               SubThreadsContract.View view,
                               BaseScheduler scheduler){
        mRepository = repository;
        mView = view;
        mScheduler = scheduler;
        mDisposables = new CompositeDisposable();
        mItemsShown = 0;
        mSubreddit = mRepository.getSubreddit(subredditName);
    }

    @Override
    public void getThreads(boolean firstLoad, int offset, int limit){
        if (mView != null) {
            mView.showLoading(true);
        }

        Observable<List<RedditThread>> threadsObservable = Observable.fromCallable(() ->
            mRepository.getRedditThreads(mSubreddit.getDisplayName(), offset, limit));

        mDisposables.add(threadsObservable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(threads -> processThreads(threads, firstLoad),
                        throwable -> processError(throwable, firstLoad),
                        () -> {}));
    }

    private void processThreads(List<RedditThread> threads, boolean firstLoad){
        if (mView != null) {
            if (firstLoad) {
                if (!threads.isEmpty()) {
                    mView.showInitialThreads(threads);
                } else {
                    mView.showEmptyThreads();
                }
                mItemsShown = threads.size();
            } else {
                mView.showMoreThreads(threads);
                mItemsShown += threads.size();
            }

            mView.showLoading(false);
        }
    }

    private void processError(Throwable throwable, boolean firstLoad){
        if (mView != null) {
            if (firstLoad) {
                mView.showEmptyThreads();
            }

            mView.showLoading(false);
        }
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
    public void removeAllThreads(){
        mItemsShown = 0;

        Completable threadCompletable = Completable.fromRunnable(() ->
                mRepository.deleteAllThreadsFromSubreddit(mSubreddit.getDisplayName()));

        mDisposables.add(threadCompletable
            .subscribeOn(mScheduler.io())
            .observeOn(mScheduler.mainThread())
            .subscribe());

        mView.showEmptyThreads();
    }

    @Override
    public void downloadThreads(){
        mView.startDownloadService(new ArrayList<>(Arrays.asList(mSubreddit.getDisplayName())));
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

        mView.showCommentsPage(thread.getFullName());
    }

    @Override
    public void subscribe(SubThreadsContract.View view) {
        mView = view;
        if (mDisposables.isDisposed()) {
            mDisposables = new CompositeDisposable();
        }
    }

    @Override
    public void unsubscribe() {
        mView = null;
        mDisposables.dispose();
    }
}

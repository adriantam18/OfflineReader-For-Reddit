package atamayo.offlinereader.SubThreads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SubThreadsPresenter implements SubThreadsContract.Presenter {
    private String mSubreddit;
    private SubredditsDataSource mRepository;
    private SubThreadsContract.View mView;
    private CompositeDisposable mDisposables;
    private int mItemsShown;

    public SubThreadsPresenter(SubredditsDataSource repository, SubThreadsContract.View view){
        mRepository = repository;
        mView = view;
        mDisposables = new CompositeDisposable();
        mItemsShown = 0;
    }

    @Override
    public void initSubThreadsList(String subreddit, int offset, int limit) {
        mSubreddit = subreddit;
        getThreads(false, offset, limit);
    }

    @Override
    public void getMoreThreads(int offset, int limit){
        getThreads(true, offset, limit);
    }

    private void getThreads(boolean isMore, int offset, int limit){
        Observable<List<RedditThread>> threadsObservable = Observable.fromCallable(() ->
            mRepository.getRedditThreads(mSubreddit, offset, limit));

        mDisposables.clear();
        mDisposables.add(threadsObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(threads -> processThreads(threads, isMore),
                this::processError, () -> {}));
    }

    private void processThreads(List<RedditThread> threads, boolean isMore){
        if(mView != null) {
            if (isMore) {
                mView.showMoreThreads(threads);
                mItemsShown += threads.size();
            } else {
                mView.showInitialThreads(threads);
                mItemsShown = threads.size();
            }
        }
    }

    private void processError(Throwable throwable){
        if(mView != null){
            mView.showEmptyThreads();
            mItemsShown = 0;
        }
    }

    @Override
    public void removeThread(RedditThread thread) {
        mItemsShown -= 1;
        mRepository.deleteRedditThread(thread.getFullName());
        getThreads(false, 0, mItemsShown);
    }

    @Override
    public void removeAllThreads(){
        mItemsShown = 0;
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
        mRepository.updateThread(thread);
        mView.showCommentsPage(thread.getFullName());
    }

    @Override
    public void subscribe(SubThreadsContract.View view) {
        mView = view;
        if(mDisposables.isDisposed()){
            mDisposables = new CompositeDisposable();
        }
    }

    @Override
    public void unsubscribe() {
        mView = null;
        mDisposables.dispose();
    }
}

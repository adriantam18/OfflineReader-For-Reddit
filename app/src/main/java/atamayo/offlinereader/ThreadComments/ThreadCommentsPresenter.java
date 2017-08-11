package atamayo.offlinereader.ThreadComments;

import java.util.List;

import atamayo.offlinereader.Data.*;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Presenter for UI ({@link ThreadCommentsListing}) that displays comments for
 * a Reddit thread. It can retrieve both thread content and comments list.
 */
public class ThreadCommentsPresenter implements ThreadCommentsContract.Presenter {
    private RedditThread mCurrentThread;
    private SubredditsDataSource mRepository;
    private ThreadCommentsContract.View mView;
    private BaseScheduler mScheduler;
    private CompositeDisposable mDisposables;

    public ThreadCommentsPresenter(String threadFullName,
                                   SubredditsDataSource dataSource,
                                   ThreadCommentsContract.View view,
                                   BaseScheduler scheduler){
        mRepository = dataSource;
        mView = view;
        mScheduler = scheduler;
        mDisposables = new CompositeDisposable();
        mCurrentThread = mRepository.getRedditThread(threadFullName);
    }

    @Override
    public void getParentThread(){
        if (mCurrentThread != null && mView != null) {
            mView.showParentThread(mCurrentThread);
        }
    }

    @Override
    public void getComments(boolean firstLoad, int offset, int limit) {
        mView.showLoading(true);

        Observable<List<RedditComment>> commentObservable =
                Observable.fromCallable(() -> mRepository.getCommentsForThread(mCurrentThread.getFullName(), offset, limit));

        mDisposables.add(commentObservable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(comments -> processComments(comments, firstLoad),
                throwable -> processError(throwable, firstLoad),
                this::processComplete)
        );
    }

    private void processComments(List<RedditComment> comments, boolean firstLoad){
        if (mView != null) {
            if (firstLoad) {
                mView.showInitialComments(comments);
            } else {
                mView.showMoreComments(comments);
            }

            mView.showLoading(false);
        }
    }

    private void processError(Throwable e, boolean firstLoad){
        if (mView != null) {
            if (firstLoad) {
                mView.showEmptyComments();
            }

            mView.showLoading(false);
        }
    }

    private void processComplete(){
        if (mView != null) {
            mView.showLoading(false);
        }
    }

    @Override
    public void subscribe(ThreadCommentsContract.View view){
        mView = view;
        if (mDisposables.isDisposed()) {
            mDisposables = new CompositeDisposable();
        }
    }

    @Override
    public void unsubscribe(){
        mView = null;
        mDisposables.dispose();
    }
}

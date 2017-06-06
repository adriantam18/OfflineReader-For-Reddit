package atamayo.offlinereader.ThreadComments;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.*;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class ThreadCommentsPresenter implements ThreadCommentsContract.Presenter {
    private ThreadCommentsContract.View mView;
    private SubredditsDataSource mRepository;
    private RedditThread mCurrentThread;
    private CompositeDisposable mDisposables;

    public ThreadCommentsPresenter(SubredditsDataSource dataSource, ThreadCommentsContract.View view){
        mView = view;
        mRepository = dataSource;
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void initCommentsView(String threadFullName, int offset, int limit) {
        mCurrentThread = mRepository.getRedditThread(threadFullName);
        mView.showParentThread(mCurrentThread);
        getComments(false, offset, limit);
    }

    @Override
    public void getMoreComments(int offset, int limit){
        getComments(true, offset, limit);
    }

    private void getComments(boolean isMore, int offset, int limit){
        mView.showLoading(true);
        Observable<List<RedditComment>> commentObservable =
                Observable.fromCallable(() -> mRepository.getCommentsForThread(mCurrentThread.getFullName(), offset, limit));

        mDisposables.clear();
        mDisposables.add(commentObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<RedditComment>>(){
                    @Override public void onComplete() {
                    }

                    @Override public void onError(Throwable e) {
                        if(mView != null){
                            mView.showInitialComments(new ArrayList<>());
                            mView.showLoading(false);
                        }
                    }

                    @Override public void onNext(List<RedditComment> comments) {
                        if(mView != null) {
                            if (isMore) {
                                mView.showMoreComments(comments);
                            } else {
                                mView.showInitialComments(comments);
                            }
                            mView.showLoading(false);
                        }
                    }
                })
        );
    }

    @Override
    public void subscribe(ThreadCommentsContract.View view){
        mView = view;
        if(mDisposables.isDisposed()){
            mDisposables = new CompositeDisposable();
        }
    }

    @Override
    public void unsubscribe(){
        mView = null;
        mDisposables.dispose();
    }
}

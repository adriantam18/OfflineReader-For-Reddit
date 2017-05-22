package atamayo.offlinereader.ThreadComments;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

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
    private int mLimit = 10;
    private int mOffset;
    private RedditThread mCurrentThread;
    private CompositeDisposable disposables;

    public ThreadCommentsPresenter(SubredditsDataSource dataSource, ThreadCommentsContract.View view){
        mView = view;
        mRepository = dataSource;
        mOffset = 0;
        disposables = new CompositeDisposable();
    }

    @Override
    public void initCommentsView(String threadFullName) {
        mCurrentThread = mRepository.getRedditThread(threadFullName);
        mView.showParentThread(mCurrentThread);
        getComms(false);
    }

    @Override
    public void getMoreComments(){
        getComms(true);
    }

    private void getComms(boolean isMore){
        mView.showLoading(true);
        Observable<List<RedditComment>> commentObservable = Observable.fromCallable(new Callable<List<RedditComment>>() {
            @Override
            public List<RedditComment> call() throws Exception {
                return mRepository.getCommentsForThread(mCurrentThread.getCommentFileName(), mOffset, mLimit);
            }
        });

        disposables.add(commentObservable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<List<RedditComment>>(){
                    @Override public void onComplete() {
                    }

                    @Override public void onError(Throwable e) {
                        if(mView != null){
                            mView.showComments(new ArrayList<>());
                            mView.showLoading(false);
                        }
                    }

                    @Override public void onNext(List<RedditComment> comments) {
                        if(mView != null) {
                            if (isMore) {
                                mView.showMoreComments(comments);
                            } else {
                                mView.showComments(comments);
                            }
                            mOffset += mLimit;
                            mView.showLoading(false);
                        }
                    }
                })
        );
    }

    public void unsubscribe(){
        mView = null;
        disposables.clear();
    }
}

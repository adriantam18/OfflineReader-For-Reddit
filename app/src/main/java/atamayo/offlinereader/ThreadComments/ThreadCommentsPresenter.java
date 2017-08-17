package atamayo.offlinereader.ThreadComments;

import java.util.List;

import atamayo.offlinereader.MVP.BaseRxPresenter;
import atamayo.offlinereader.Data.*;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Observable;

/**
 * Presenter for UI ({@link ThreadCommentsListing}) that displays comments for
 * a Reddit thread. It can retrieve both thread content and comments list.
 */
public class ThreadCommentsPresenter extends BaseRxPresenter<ThreadCommentsContract.View>
        implements ThreadCommentsContract.Presenter {
    private SubredditsDataSource mRepository;
    private BaseScheduler mScheduler;
    private RedditThread mCurrentThread;

    public ThreadCommentsPresenter(String threadFullName,
                                   SubredditsDataSource dataSource,
                                   BaseScheduler scheduler) {
        mRepository = dataSource;
        mScheduler = scheduler;
        mCurrentThread = mRepository.getRedditThread(threadFullName);
    }

    @Override
    public void getParentThread() {
        getView().showParentThread(mCurrentThread);
    }

    @Override
    public void getComments(boolean firstLoad, int offset, int limit) {
        getView().showLoading(true);

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

    private void processComments(List<RedditComment> comments, boolean firstLoad) {
        if (firstLoad) {
            getView().showInitialComments(comments);
        } else {
            getView().showMoreComments(comments);
        }

        getView().showLoading(false);
    }

    private void processError(Throwable e, boolean firstLoad) {
        if (firstLoad) {
            getView().showEmptyComments();
        }

        getView().showLoading(false);
    }

    private void processComplete() {
        getView().showLoading(false);
    }

    @Override
    protected ThreadCommentsContract.View createFakeView() {
        return new ThreadCommentsContract.View() {
            @Override
            public void showParentThread(RedditThread thread) {

            }

            @Override
            public void showInitialComments(List<RedditComment> comments) {

            }

            @Override
            public void showMoreComments(List<RedditComment> comments) {

            }

            @Override
            public void showEmptyComments() {

            }

            @Override
            public void showLoading(boolean isLoading) {

            }
        };
    }
}

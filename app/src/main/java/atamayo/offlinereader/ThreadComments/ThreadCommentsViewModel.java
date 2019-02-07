package atamayo.offlinereader.ThreadComments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.SingleLiveEvent;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class ThreadCommentsViewModel extends ViewModel {
    private static final String ERROR_LOADING_THREAD = "Failed to load thread";
    private static final String ERROR_LOADING_COMMENTS = "Failed to load comments";

    private final MutableLiveData<RedditThread> mThreadObservable = new MutableLiveData<>();
    private final MutableLiveData<List<RedditComment>> mCommentsObservable = new MutableLiveData<>();
    private final SingleLiveEvent<String> mMessageObservable = new SingleLiveEvent<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private SubredditsDataSource mRepository;
    private BaseScheduler mScheduler;
    private String mThreadFullName;

    public ThreadCommentsViewModel(SubredditsDataSource subredditsDataSource, BaseScheduler scheduler, String threadFullName, int numItems) {
        mRepository = subredditsDataSource;
        mScheduler = scheduler;
        mThreadFullName = threadFullName;

        loadRedditThread(mThreadFullName);
        loadComments(numItems);
    }

    @Override
    protected void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    public LiveData<RedditThread> getThreadObservable() {
        return mThreadObservable;
    }

    public LiveData<List<RedditComment>> getCommentsObservable() {
        return mCommentsObservable;
    }

    public SingleLiveEvent<String> getMessageObservable() {
        return mMessageObservable;
    }

    public void getComments(int numItems) {
        loadComments(numItems);
    }

    private void loadRedditThread(String threadFullName) {
        mDisposable.add(Single.fromCallable(() -> mRepository.getRedditThread(threadFullName))
            .subscribeOn(mScheduler.io())
            .observeOn(mScheduler.mainThread())
            .subscribe(mThreadObservable::setValue,
                    throwable -> mMessageObservable.setValue(ERROR_LOADING_THREAD))
        );
    }

    private void loadComments(int numItems) {
        mDisposable.add(Single.fromCallable(() -> mRepository.getCommentsForThread(mThreadFullName, 0, numItems))
            .subscribeOn(mScheduler.io())
            .observeOn(mScheduler.mainThread())
            .subscribe(mCommentsObservable::setValue,
                    throwable -> mMessageObservable.setValue(ERROR_LOADING_COMMENTS))
        );
    }
}

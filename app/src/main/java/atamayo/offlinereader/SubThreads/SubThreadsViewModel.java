package atamayo.offlinereader.SubThreads;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.SingleLiveEvent;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class SubThreadsViewModel extends ViewModel {
    private static final String ERROR_LOADING = "Failed to load threads";
    private static final String ERROR_DELETING = "Failed to delete threads";

    private final MutableLiveData<List<RedditThread>> mThreadsObservable = new MutableLiveData<>();
    private final SingleLiveEvent<String> mMessageObservable = new SingleLiveEvent<>();
    private final SingleLiveEvent<String> mSubredditObservable = new SingleLiveEvent<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private SubredditsDataSource mRepository;
    private BaseScheduler mScheduler;
    private String mSubredditName;
    private int mItemsLoaded;

    public SubThreadsViewModel(SubredditsDataSource subredditsDataSource, BaseScheduler scheduler, String subredditName, int numItems) {
        mRepository = subredditsDataSource;
        mScheduler = scheduler;
        mSubredditName = subredditName;

        loadThreads(numItems);
    }

    @Override
    protected void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    public LiveData<List<RedditThread>> getRedditThreadsObservable() {
        return mThreadsObservable;
    }

    public SingleLiveEvent<String> getMessageObservable() {
        return mMessageObservable;
    }

    public SingleLiveEvent<String> getSubredditObservable() {
        return mSubredditObservable;
    }

    public void getThreads(int numItems) {
        loadThreads(numItems);
    }

    public void removeThread(RedditThread thread) {
        mDisposable.add(Completable.fromRunnable(() -> mRepository.deleteRedditThread(thread.getFullName()))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(() -> loadThreads(mItemsLoaded),
                        throwable -> {
                            mMessageObservable.setValue(ERROR_DELETING);
                            loadThreads(mItemsLoaded);
                        }));
    }

    public void clearThreads() {
        mDisposable.add(Completable.fromRunnable(() -> mRepository.deleteAllThreadsFromSubreddit(mSubredditName))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(() -> mThreadsObservable.setValue(new ArrayList<>()),
                        throwable -> {
                            mMessageObservable.setValue(ERROR_DELETING);
                            loadThreads(mItemsLoaded);
                        }));
    }

    public void updateSelectedThread(RedditThread thread) {
        thread.setWasClicked(true);
        mDisposable.add(Completable.fromRunnable(() -> mRepository.updateThread(thread))
                .subscribeOn(mScheduler.io())
                .subscribe()
        );
    }

    public void getCurrentSubreddit() {
        mSubredditObservable.setValue(mSubredditName);
    }

    private void loadThreads(int numItems) {
        mDisposable.add(Single.fromCallable(() -> mRepository.getRedditThreads(mSubredditName, 0, numItems))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(redditThreads -> {
                            mThreadsObservable.setValue(redditThreads);
                            mItemsLoaded = redditThreads.size();
                        },
                        throwable -> mMessageObservable.setValue(ERROR_LOADING)));
    }
}

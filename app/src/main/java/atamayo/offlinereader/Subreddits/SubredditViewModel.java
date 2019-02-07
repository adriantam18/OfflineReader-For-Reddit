package atamayo.offlinereader.Subreddits;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.SingleLiveEvent;
import atamayo.offlinereader.Utils.RedditDownloader;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class SubredditViewModel extends ViewModel {
    private static final String ERROR_ADDING = "Failed to add";
    private static final String ERROR_LOADING = "Failed to load";
    private static final String ERROR_DELETING = "Failed to delete";

    private final MutableLiveData<List<Subreddit>> mSubredditsObservable = new MutableLiveData<>();
    private final SingleLiveEvent<String> mMessageObservable = new SingleLiveEvent<>();
    private CompositeDisposable mDisposable = new CompositeDisposable();
    private SubredditsDataSource mRepository;
    private KeywordsDataSource mKeywordsRepository;
    private RedditDownloader mDownloader;
    private BaseScheduler mScheduler;

    public SubredditViewModel(SubredditsDataSource subredditsDataSource, KeywordsDataSource  keywordsDataSource,
                              RedditDownloader downloader, BaseScheduler scheduler) {
        mRepository = subredditsDataSource;
        mKeywordsRepository = keywordsDataSource;
        mDownloader = downloader;
        mScheduler = scheduler;

        loadSubreddits();
    }

    @Override
    protected void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    public LiveData<List<Subreddit>> getSubredditsObservable() {
        return mSubredditsObservable;
    }

    public SingleLiveEvent<String> getMessageObservable() {
        return mMessageObservable;
    }

    public void getSubreddits() {
        loadSubreddits();
    }

    public void addSubreddit(final String subredditName) {
        mDisposable.add(Single.fromCallable(() -> mDownloader.checkSubreddit(subredditName))
                .filter(subredditSingle -> mRepository.addSubreddit(subredditSingle.blockingGet()))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(subredditSingle -> loadSubreddits(),
                        throwable -> mMessageObservable.setValue(ERROR_ADDING)));
    }

    public void deleteSubreddit(final String subredditName) {
        mDisposable.add(Completable.fromRunnable(() -> mRepository.deleteSubreddit(subredditName))
                        .andThen(Completable.fromRunnable(() -> mKeywordsRepository.clearKeywords(subredditName)))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(this::loadSubreddits,
                        throwable -> {
                            mMessageObservable.setValue(ERROR_DELETING);
                            loadSubreddits();
                        }));
    }

    public void clearSubreddits() {
        mDisposable.add(Completable.fromRunnable(() -> mRepository.deleteAllSubreddits())
                        .andThen(Completable.fromRunnable(() -> mKeywordsRepository.clearAllKeywords()))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(this::loadSubreddits,
                        throwable -> {
                            mMessageObservable.setValue(ERROR_DELETING);
                            loadSubreddits();
                        }));
    }

    private void loadSubreddits() {
        mDisposable.add(Single.fromCallable(() -> mRepository.getSubreddits())
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(mSubredditsObservable::setValue,
                        throwable -> mMessageObservable.setValue(ERROR_LOADING)));
    }
}

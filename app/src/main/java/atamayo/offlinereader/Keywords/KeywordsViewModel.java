package atamayo.offlinereader.Keywords;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Collections;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.SingleLiveEvent;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

public class KeywordsViewModel extends ViewModel {
    private static final String ERROR_ADDING = "Failed to add";
    private static final String ERROR_LOADING = "Failed to load";
    private static final String ERROR_DELETING = "Failed to delete";

    private final MutableLiveData<List<String>> mKeywordsObservable = new MutableLiveData<>();
    private final SingleLiveEvent<String> mMessageObservable = new SingleLiveEvent<>();
    private CompositeDisposable mDisposable;
    private KeywordsDataSource mRepository;
    private BaseScheduler mScheduler;
    private String mSubredditName;

    public KeywordsViewModel(KeywordsDataSource keywordsDataSource, BaseScheduler scheduler, String subredditName) {
        mDisposable = new CompositeDisposable();
        mRepository = keywordsDataSource;
        mScheduler = scheduler;
        mSubredditName = subredditName;

        loadKeywords();
    }

    @Override
    public void onCleared() {
        mDisposable.dispose();
        super.onCleared();
    }

    public LiveData<List<String>> getKeywordsObservable() {
        return mKeywordsObservable;
    }

    public SingleLiveEvent<String> getMessageObservable() {
        return mMessageObservable;
    }

    public void addKeyword(String keyword) {
        mDisposable.add(Single.fromCallable(() -> mRepository.addKeyword(mSubredditName, keyword))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(added -> {
                    if (added) {
                        loadKeywords();
                    } else {
                        mMessageObservable.setValue(ERROR_ADDING);
                    }
                }, throwable -> mMessageObservable.setValue(ERROR_ADDING)));
    }

    public void removeKeyword(String keyword) {
        mDisposable.add(Completable.fromRunnable(() -> mRepository.deleteKeyword(mSubredditName, keyword))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(this::loadKeywords,
                        throwable -> {
                            mMessageObservable.setValue(ERROR_DELETING);
                            loadKeywords();
                        }));
    }

    public void clearKeywords() {
        mDisposable.add(Completable.fromRunnable(() -> mRepository.clearKeywords(mSubredditName))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(this::loadKeywords,
                        throwable -> {
                            mMessageObservable.setValue(ERROR_DELETING);
                            loadKeywords();
                        }));
    }

    private void loadKeywords() {
        mDisposable.add(Single.fromCallable(() -> mRepository.getKeywords(mSubredditName))
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(keywords -> {
                            Collections.reverse(keywords);
                            mKeywordsObservable.setValue(keywords);
                        },
                        throwable -> mMessageObservable.setValue(ERROR_LOADING)));
    }
}

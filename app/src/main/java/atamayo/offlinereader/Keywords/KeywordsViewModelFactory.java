package atamayo.offlinereader.Keywords;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;

public class KeywordsViewModelFactory implements ViewModelProvider.Factory {
    private final KeywordsDataSource mRepository;
    private final BaseScheduler mScheduler;
    private final String mSubredditName;

    public KeywordsViewModelFactory(KeywordsDataSource keywordsDataSource, BaseScheduler scheduler, String subredditName) {
        mRepository = keywordsDataSource;
        mScheduler = scheduler;
        mSubredditName = subredditName;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(KeywordsViewModel.class)) {
            return (T) new KeywordsViewModel(mRepository, mScheduler, mSubredditName);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

package atamayo.offlinereader.SubThreads;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;

public class SubThreadsViewModelFactory implements ViewModelProvider.Factory {

    private final SubredditsDataSource mRepository;
    private final BaseScheduler mScheduler;
    private final String mSubredditName;
    private final int mNumItems;

    public SubThreadsViewModelFactory(SubredditsDataSource subredditsDataSource, BaseScheduler scheduler, String subredditName, int numItems) {
        mRepository = subredditsDataSource;
        mScheduler = scheduler;
        mSubredditName = subredditName;
        mNumItems = numItems;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(SubThreadsViewModel.class)) {
            return (T) new SubThreadsViewModel(mRepository, mScheduler, mSubredditName, mNumItems);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

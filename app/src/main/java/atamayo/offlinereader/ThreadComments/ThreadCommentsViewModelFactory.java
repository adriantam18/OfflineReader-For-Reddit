package atamayo.offlinereader.ThreadComments;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;

public class ThreadCommentsViewModelFactory implements ViewModelProvider.Factory {
    private final SubredditsDataSource mRepository;
    private final BaseScheduler mScheduler;
    private final String mThreadFullName;
    private final int mNumItems;

    public ThreadCommentsViewModelFactory(SubredditsDataSource subredditsDataSource, BaseScheduler scheduler, String threadFullName, int numItems) {
        mRepository = subredditsDataSource;
        mScheduler = scheduler;
        mThreadFullName = threadFullName;
        mNumItems = numItems;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ThreadCommentsViewModel.class)) {
            return (T) new ThreadCommentsViewModel(mRepository, mScheduler, mThreadFullName, mNumItems);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

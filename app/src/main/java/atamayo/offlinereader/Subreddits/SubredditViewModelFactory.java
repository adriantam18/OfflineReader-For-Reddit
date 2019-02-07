package atamayo.offlinereader.Subreddits;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Utils.RedditDownloader;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;

public class SubredditViewModelFactory implements ViewModelProvider.Factory{
    private final SubredditsDataSource mRepository;
    private final KeywordsDataSource mKeywordsRepository;
    private final RedditDownloader mDownloader;
    private final BaseScheduler mScheduler;

    public SubredditViewModelFactory(SubredditsDataSource subredditsDataSource, KeywordsDataSource keywordsDataSource,
                                     RedditDownloader downloader, BaseScheduler scheduler) {
        mRepository = subredditsDataSource;
        mKeywordsRepository = keywordsDataSource;
        mDownloader = downloader;
        mScheduler = scheduler;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if(modelClass.isAssignableFrom(SubredditViewModel.class)) {
            return (T) new SubredditViewModel(mRepository, mKeywordsRepository, mDownloader, mScheduler);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}

package atamayo.offlinereader.Subreddits;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.TestScheduler;
import atamayo.offlinereader.Utils.RedditDownloader;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubredditsViewModelTest {
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private List<Subreddit> mSubreddits;
    private String mSubredditName;

    @Mock
    SubredditsDataSource mMockSubredditsRepository;

    @Mock
    KeywordsDataSource mMockKeywordsRepository;

    @Mock
    RedditDownloader mMockDownloader;

    @Mock
    Observer<List<Subreddit>> mSubredditsObserver;

    @Mock
    Observer<String> mMessageObserver;

    private SubredditViewModel mSubredditsViewModel;

    @Before
    public void setUp() {
        mSubreddits = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Subreddit subreddit = new Subreddit();
            subreddit.setDisplayName("Sample" + i);
            subreddit.setDisplayNamePrefixed("r/Sample" + i);
            mSubreddits.add(subreddit);
        }
        mSubredditName = "Sample";

        mSubredditsViewModel = new SubredditViewModel(mMockSubredditsRepository, mMockKeywordsRepository,
                mMockDownloader, new TestScheduler());
    }

    @Test
    public void testGetSubredditsSuccess() {
        when(mMockSubredditsRepository.getSubreddits())
                .thenReturn(mSubreddits);

        mSubredditsViewModel.getSubreddits();
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        Assert.assertEquals(mSubreddits.size(), mSubredditsViewModel.getSubredditsObservable().getValue().size());
    }

    @Test
    public void testGetSubredditsFail() {
        when(mMockSubredditsRepository.getSubreddits())
                .thenReturn(null);

        mSubredditsViewModel.getSubreddits();
        mSubredditsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
    }

    @Test
    public void testAddSubredditsSuccess() {
        Subreddit sample = new Subreddit();
        when(mMockDownloader.checkSubreddit(anyString()))
                .thenReturn(Single.just(sample));
        when(mMockSubredditsRepository.addSubreddit(sample))
                .thenReturn(true);
        when(mMockSubredditsRepository.getSubreddits())
                .thenReturn(mSubreddits);

        mSubredditsViewModel.addSubreddit(mSubredditName);
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        Assert.assertEquals(mSubreddits.size(), mSubredditsViewModel.getSubredditsObservable().getValue().size());
    }

    @Test
    public void testAddSubredditsFail() {
        when(mMockDownloader.checkSubreddit(anyString()))
                .thenReturn(null);

        mSubredditsViewModel.addSubreddit(mSubredditName);
        mSubredditsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
    }

    @Test
    public void testDeleteSubredditSuccess() {
        when(mMockSubredditsRepository.getSubreddits())
                .thenReturn(mSubreddits);

        mSubredditsViewModel.deleteSubreddit(mSubredditName);
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        verify(mMockSubredditsRepository).deleteSubreddit(mSubredditName);
        verify(mMockKeywordsRepository).clearKeywords(mSubredditName);
        verify(mMockSubredditsRepository, times(2)).getSubreddits();
        Assert.assertEquals(mSubreddits.size(), mSubredditsViewModel.getSubredditsObservable().getValue().size());
    }

    @Test
    public void testDeleteSubredditRepoFail() {
        doThrow(new RuntimeException()).when(mMockSubredditsRepository).deleteSubreddit(mSubredditName);

        mSubredditsViewModel.deleteSubreddit(mSubredditName);
        mSubredditsViewModel.getMessageObservable().observeForever(mMessageObserver);
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        verify(mMessageObserver).onChanged(anyString());
        verify(mSubredditsObserver, times(1)).onChanged(any());
    }

    @Test
    public void testDeleteSubredditKeywordsFail() {
        doThrow(new RuntimeException()).when(mMockKeywordsRepository).clearKeywords(mSubredditName);

        mSubredditsViewModel.deleteSubreddit(mSubredditName);
        mSubredditsViewModel.getMessageObservable().observeForever(mMessageObserver);
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        verify(mMessageObserver).onChanged(anyString());
        verify(mSubredditsObserver, times(1)).onChanged(any());
    }

    @Test
    public void testClearSubredditsSuccess() {
        when(mMockSubredditsRepository.getSubreddits())
                .thenReturn(new ArrayList<>());

        mSubredditsViewModel.clearSubreddits();
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        verify(mMockSubredditsRepository).deleteAllSubreddits();
        verify(mMockKeywordsRepository).clearAllKeywords();
        Assert.assertEquals(0, mSubredditsViewModel.getSubredditsObservable().getValue().size());
    }

    @Test
    public void testClearSubredditsRepoFail() {
        doThrow(new RuntimeException()).when(mMockSubredditsRepository).deleteAllSubreddits();

        mSubredditsViewModel.clearSubreddits();
        mSubredditsViewModel.getMessageObservable().observeForever(mMessageObserver);
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        verify(mMessageObserver).onChanged(anyString());
        verify(mSubredditsObserver, times(1)).onChanged(any());
    }

    @Test
    public void testClearSubredditsKeywordsFail() {
        doThrow(new RuntimeException()).when(mMockKeywordsRepository).clearAllKeywords();

        mSubredditsViewModel.clearSubreddits();
        mSubredditsViewModel.getMessageObservable().observeForever(mMessageObserver);
        mSubredditsViewModel.getSubredditsObservable().observeForever(mSubredditsObserver);

        verify(mMessageObserver).onChanged(anyString());
        verify(mSubredditsObserver, times(1)).onChanged(any());
    }
}

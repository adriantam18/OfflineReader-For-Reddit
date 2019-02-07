package atamayo.offlinereader.SubThreads;

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

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.TestScheduler;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubThreadsViewModelTest {
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private List<RedditThread> mRedditThreads;
    private String mSubredditName;
    private int mNumItems;

    @Mock
    SubredditsDataSource mMockSubredditsRepository;

    @Mock
    Observer<List<RedditThread>> mThreadsObserver;

    @Mock
    Observer<String> mSubredditObserver;

    @Mock
    Observer<String> mMessageObserver;

    private SubThreadsViewModel mSubThreadsViewModel;

    @Before
    public void setUp() {
        mRedditThreads = new ArrayList<>();
        mNumItems = 3;
        for (int i = 0; i < mNumItems; i++) {
            RedditThread thread = new RedditThread();
            thread.setFullName("Sample " + i);
            mRedditThreads.add(thread);
        }
        mSubredditName = "Sample";

        mSubThreadsViewModel = new SubThreadsViewModel(mMockSubredditsRepository, new TestScheduler(),
                mSubredditName, mNumItems);
    }

    @Test
    public void testGetThreadsSuccess() {
        when(mMockSubredditsRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(mRedditThreads);

        mSubThreadsViewModel.getThreads(mNumItems);
        mSubThreadsViewModel.getRedditThreadsObservable().observeForever(mThreadsObserver);

        Assert.assertEquals(mRedditThreads.size(), mSubThreadsViewModel.getRedditThreadsObservable().getValue().size());
    }

    @Test
    public void testGetThreadsFail() {
        when(mMockSubredditsRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(null);

        mSubThreadsViewModel.getThreads(mNumItems);
        mSubThreadsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
    }

    @Test
    public void testRemoveThreadSuccess() {
        when(mMockSubredditsRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(mRedditThreads);

        RedditThread thread = new RedditThread();
        thread.setFullName("Sample");
        mSubThreadsViewModel.removeThread(thread);
        mSubThreadsViewModel.getRedditThreadsObservable().observeForever(mThreadsObserver);

        verify(mMockSubredditsRepository).deleteRedditThread(thread.getFullName());
        Assert.assertEquals(mRedditThreads.size(), mSubThreadsViewModel.getRedditThreadsObservable().getValue().size());
    }

    @Test
    public void testRemoveThreadFail() {
        RedditThread thread = new RedditThread();
        thread.setFullName("Sample");

        doThrow(new RuntimeException()).when(mMockSubredditsRepository).deleteRedditThread(thread.getFullName());
        when(mMockSubredditsRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(mRedditThreads);

        mSubThreadsViewModel.removeThread(thread);
        mSubThreadsViewModel.getRedditThreadsObservable().observeForever(mThreadsObserver);
        mSubThreadsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
        Assert.assertEquals(mRedditThreads.size(), mSubThreadsViewModel.getRedditThreadsObservable().getValue().size());
    }

    @Test
    public void testClearThreadsSuccess() {
        when(mMockSubredditsRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());

        mSubThreadsViewModel.clearThreads();
        mSubThreadsViewModel.getRedditThreadsObservable().observeForever(mThreadsObserver);

        verify(mMockSubredditsRepository).deleteAllThreadsFromSubreddit(mSubredditName);
        Assert.assertEquals(0, mSubThreadsViewModel.getRedditThreadsObservable().getValue().size());
    }

    @Test
    public void testClearThreadsFail() {
        doThrow(new RuntimeException()).when(mMockSubredditsRepository).deleteAllThreadsFromSubreddit(mSubredditName);
        when(mMockSubredditsRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(mRedditThreads);

        mSubThreadsViewModel.clearThreads();
        mSubThreadsViewModel.getRedditThreadsObservable().observeForever(mThreadsObserver);
        mSubThreadsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMockSubredditsRepository).deleteAllThreadsFromSubreddit(mSubredditName);
        verify(mMessageObserver).onChanged(anyString());
        Assert.assertEquals(mRedditThreads.size(), mSubThreadsViewModel.getRedditThreadsObservable().getValue().size());
    }

    @Test
    public void testUpdateThreadSuccess() {
        RedditThread thread = new RedditThread();

        mSubThreadsViewModel.updateSelectedThread(thread);

        verify(mMockSubredditsRepository).updateThread(thread);
    }

    @Test
    public void testGetCurrentSubreddit() {
        mSubThreadsViewModel.getCurrentSubreddit();
        mSubThreadsViewModel.getSubredditObservable().observeForever(mSubredditObserver);

        verify(mSubredditObserver).onChanged(mSubredditName);
    }
}

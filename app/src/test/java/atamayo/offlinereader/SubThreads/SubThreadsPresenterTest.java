package atamayo.offlinereader.SubThreads;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.TestScheduler;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubThreadsPresenterTest {
    private List<RedditThread> initialList;
    private List<RedditThread> additionalList;

    @Mock
    private SubThreadsContract.View mockView;

    @Mock
    private SubredditsDataSource mockRepository;

    private Subreddit currentSubreddit;

    private SubThreadsPresenter presenter;

    @Before
    public void setUp() {
        String sampleName = "Sample subreddit name";
        currentSubreddit = new Subreddit();
        currentSubreddit.setDisplayName(sampleName);
        when(mockRepository.getSubreddit(anyString()))
                .thenReturn(currentSubreddit);

        presenter = new SubThreadsPresenter(sampleName, mockRepository, new TestScheduler());
        presenter.attachView(mockView);

        int initialListSize = 3;
        initialList = new ArrayList<>(initialListSize);
        for (int i = 0; i < initialListSize; i++) {
            RedditThread thread = new RedditThread();
            thread.setFullName("Sample " + Integer.toString(i));
            initialList.add(thread);
        }

        int additionalListSize = 3;
        additionalList = new ArrayList<>(additionalListSize);
        for (int i = 0; i < additionalListSize; i++) {
            RedditThread thread = new RedditThread();
            thread.setFullName("Additional " + Integer.toString(i));
            additionalList.add(thread);
        }
    }

    @Test
    public void testGetThreadsSuccessFirstLoad() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(initialList);
        presenter.getThreads(true, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showInitialThreads(initialList);
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetThreadsEmptyFirstLoad() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());
        presenter.getThreads(true, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showEmptyThreads();
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetThreadsErrorFirstLoad() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException());
        presenter.getThreads(true, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showEmptyThreads();
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetThreadsSuccessNotFirstLoad() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(additionalList);
        presenter.getThreads(false, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showMoreThreads(additionalList);
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetThreadsEmptyNotFirstLoad() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(new ArrayList<>());
        presenter.getThreads(false, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView, never()).showInitialThreads(anyListOf(RedditThread.class));
        verify(mockView, never()).showEmptyThreads();
        verify(mockView).showMoreThreads(new ArrayList<>());
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetThreadsErrorNotFirstLoad() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException());
        presenter.getThreads(false, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView, never()).showEmptyThreads();
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetThreadsNullView() {
        presenter.attachView(null);

        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(initialList);
        presenter.getThreads(true, 0, 10);
        presenter.getThreads(false, 0, 10);

        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException());
        presenter.getThreads(true, 0, 10);
        presenter.getThreads(false, 0, 10);

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testRemoveThread() {
        when(mockRepository.getRedditThreads(anyString(), anyInt(), anyInt()))
                .thenReturn(initialList);

        RedditThread threadToDelete = new RedditThread();
        threadToDelete.setFullName("Sample full name");

        presenter.removeThread(threadToDelete);

        verify(mockRepository).deleteRedditThread(threadToDelete.getFullName());
        verify(mockView).showInitialThreads(initialList);
    }

    @Test
    public void testRemoveThreadNullView() {
        presenter.attachView(null);

        RedditThread threadToDelete = new RedditThread();
        threadToDelete.setFullName("Sample full name");

        presenter.removeThread(threadToDelete);

        verify(mockRepository).deleteRedditThread(threadToDelete.getFullName());
        verifyZeroInteractions(mockView);
    }

    @Test
    public void testRemoveAllThreads() {
        presenter.removeAllThreads();

        verify(mockRepository).deleteAllThreadsFromSubreddit(currentSubreddit.getDisplayName());
        verify(mockView).showEmptyThreads();
    }

    @Test
    public void testRemoveAllThreadsNullView() {
        presenter.attachView(null);
        presenter.removeAllThreads();

        verify(mockRepository).deleteAllThreadsFromSubreddit(currentSubreddit.getDisplayName());
        verifyZeroInteractions(mockView);
    }

    @Test
    public void testDownloadThreads() {
        presenter.downloadThreads();

        verify(mockView).startDownloadService(new ArrayList<>(Arrays.asList(currentSubreddit.getDisplayName())));
    }

    @Test
    public void testDownloadThreadsNullView() {
        presenter.attachView(null);
        presenter.downloadThreads();

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testOpenCommentsPage() {
        RedditThread threadToUpdate = new RedditThread();
        threadToUpdate.setFullName("Sample full name");

        presenter.openCommentsPage(threadToUpdate);

        verify(mockRepository).updateThread(threadToUpdate);
        verify(mockView).showCommentsPage(threadToUpdate.getFullName());
    }

    @Test
    public void testOpenCommentsPageNullView() {
        RedditThread threadToUpdate = new RedditThread();
        threadToUpdate.setFullName("Sample full name");

        presenter.attachView(null);
        presenter.openCommentsPage(threadToUpdate);

        verifyZeroInteractions(mockView);
    }
}

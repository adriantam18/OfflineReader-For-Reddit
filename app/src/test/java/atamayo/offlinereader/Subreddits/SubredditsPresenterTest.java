package atamayo.offlinereader.Subreddits;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.DuplicateSubredditException;
import atamayo.offlinereader.RedditAPI.InvalidSubredditException;
import atamayo.offlinereader.RedditAPI.NoConnectionException;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.TestScheduler;
import atamayo.offlinereader.Utils.RedditDownloader;
import io.reactivex.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SubredditsPresenterTest {
    private List<Subreddit> subreddits;

    @Mock
    private SubredditsContract.View mockView;

    @Mock
    private SubredditsDataSource mockRepository;

    @Mock
    private RedditDownloader mockDownloader;

    @Mock
    private KeywordsDataSource mockKeywords;

    private SubredditsPresenter presenter;

    @Before
    public void setUp() {
        presenter = new SubredditsPresenter(mockRepository, mockDownloader, mockKeywords, new TestScheduler());
        presenter.attachView(mockView);

        int sampleSize = 3;
        subreddits = new ArrayList<>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            Subreddit subreddit = new Subreddit();
            subreddit.setDisplayName("Sample " + Integer.toString(i));
            subreddits.add(subreddit);
        }
    }

    @Test
    public void testGetSubredditsSuccess() {
        when(mockRepository.getSubreddits())
                .thenReturn(subreddits);
        presenter.getSubreddits();

        verify(mockView).showLoading(true);
        verify(mockView).showSubreddits(subreddits);
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetSubredditsNullView() {
        presenter.attachView(null);

        when(mockRepository.getSubreddits())
                .thenReturn(subreddits);
        presenter.getSubreddits();

        when(mockRepository.getSubreddits())
                .thenThrow(new RuntimeException());
        presenter.getSubreddits();

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testAddIfExistsSuccess() {
        String subName = "Sample name";
        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName(subName);

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.just(subreddit));
        when(mockRepository.addSubreddit(any()))
                .thenReturn(true);
        presenter.addIfExists(subName);

        verify(mockView).showLoading(true);
        verify(mockView).showAddedSubreddit(subreddit);
        verify(mockView).showLoading(false);
    }

    @Test
    public void testAddIfExistsNullSubredditName() {
        presenter.addIfExists(null);

        verify(mockView).showLoading(true);
        verify(mockDownloader, times(0)).checkSubreddit(anyString());
        verify(mockRepository, times(0)).addSubreddit(any());
        verify(mockView).showError(anyString());
        verify(mockView).showLoading(false);
    }

    @Test
    public void testAddIfExistsNoConnection() {
        String subName = "Sample name";
        NoConnectionException exception = new NoConnectionException();

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.error(exception));
        presenter.addIfExists(subName);

        verify(mockView).showError(exception.getMessage());
    }

    @Test
    public void testAddIfExistsInvalidSubreddit() {
        String subName = "Sample name";
        InvalidSubredditException exception = new InvalidSubredditException();

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.error(exception));
        presenter.addIfExists(subName);

        verify(mockView).showError(exception.getMessage());
    }

    @Test
    public void testAddIfExistsDuplicateSubreddit() {
        String subName = "Sample name";
        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName(subName);
        DuplicateSubredditException exception = new DuplicateSubredditException();

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.just(subreddit));
        when(mockRepository.addSubreddit(any()))
                .thenThrow(exception);
        presenter.addIfExists(subName);

        verify(mockRepository).addSubreddit(subreddit);
        verify(mockView).showError(exception.getMessage());
    }

    @Test
    public void testAddIfExistsNullView() {
        presenter.attachView(null);

        String subName = "Sample name";
        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName(subName);

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.just(subreddit));
        when(mockRepository.addSubreddit(any()))
                .thenReturn(true);
        presenter.addIfExists(subName);

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.just(subreddit));
        when(mockRepository.addSubreddit(any()))
                .thenThrow(new RuntimeException());
        presenter.addIfExists(subName);

        when(mockDownloader.checkSubreddit(anyString()))
                .thenReturn(Observable.error(new RuntimeException()));
        presenter.addIfExists(subName);

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testClearSubreddits() {
        when(mockRepository.getSubreddits())
                .thenReturn(subreddits);
        presenter.clearSubreddits();

        verify(mockKeywords, times(subreddits.size())).clearKeywords(anyString());
        verify(mockRepository, times(subreddits.size())).deleteSubreddit(anyString());
        verify(mockView).showSubreddits(new ArrayList<>());
    }

    @Test
    public void testClearSubredditsNullView() {
        presenter.attachView(null);

        when(mockRepository.getSubreddits())
                .thenReturn(subreddits);
        presenter.clearSubreddits();

        verify(mockKeywords, times(subreddits.size())).clearKeywords(anyString());
        verify(mockRepository, times(subreddits.size())).deleteSubreddit(anyString());
        verifyZeroInteractions(mockView);
    }

    @Test
    public void testRemoveSubredditSuccess() {
        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName("Sample name");

        when(mockRepository.getSubreddits())
                .thenReturn(subreddits);
        presenter.removeSubreddit(subreddit);

        verify(mockKeywords, times(1)).clearKeywords(subreddit.getDisplayName());
        verify(mockRepository, times(1)).deleteSubreddit(subreddit.getDisplayName());
        verify(mockView).showSubreddits(subreddits);
    }

    @Test
    public void testRemoveSubredditNull() {
        presenter.removeSubreddit(null);

        verify(mockKeywords, times(0)).clearKeywords(anyString());
        verify(mockRepository, times(0)).deleteSubreddit(anyString());
        verify(mockView, times(0)).showSubreddits(subreddits);
    }

    @Test
    public void testRemoveSubredditDisplayNameNull() {
        Subreddit subreddit = new Subreddit();

        presenter.removeSubreddit(subreddit);

        verify(mockKeywords, times(0)).clearKeywords(anyString());
        verify(mockRepository, times(0)).deleteSubreddit(anyString());
        verify(mockView, times(0)).showSubreddits(subreddits);
    }

    @Test
    public void testRemoveSubredditNullView() {
        presenter.attachView(null);

        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName("Sample name");

        when(mockRepository.getSubreddits())
                .thenReturn(subreddits);
        presenter.removeSubreddit(subreddit);

        verify(mockKeywords, times(1)).clearKeywords(subreddit.getDisplayName());
        verify(mockRepository, times(1)).deleteSubreddit(subreddit.getDisplayName());
        verifyZeroInteractions(mockView);
    }

    @Test
    public void testOpenSubredditKeywordsSuccess() {
        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName("Sample name");

        presenter.openSubredditKeywords(subreddit);

        verify(mockView).showSubredditKeywords(subreddit.getDisplayName());
    }

    @Test
    public void testOpenSubredditKeywordsSubredditNull() {
        presenter.openSubredditKeywords(null);

        verify(mockView, times(0)).showSubredditKeywords(anyString());
    }

    @Test
    public void testOpenSubredditKeywordsDisplayNameNull() {
        Subreddit subreddit = new Subreddit();

        presenter.openSubredditKeywords(subreddit);

        verify(mockView, times(0)).showSubredditKeywords(anyString());
    }

    @Test
    public void testOpenSubredditKeywordsNullView() {
        presenter.attachView(null);

        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName("Sample name");

        presenter.openSubredditKeywords(subreddit);

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testOpenSubredditThreadsSuccess() {
        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName("Sample name");

        presenter.openSubredditThreads(subreddit);

        verify(mockView).showSubredditThreads(subreddit.getDisplayName());
    }

    @Test
    public void testOpenSubredditThreadsSubredditNull() {
        presenter.openSubredditThreads(null);

        verify(mockView, times(0)).showSubredditThreads(anyString());
    }

    @Test
    public void testOpenSubredditThreadsDisplayNameNull() {
        Subreddit subreddit = new Subreddit();

        presenter.openSubredditThreads(subreddit);

        verify(mockView, times(0)).showSubredditThreads(anyString());
    }

    @Test
    public void testOpenSubredditThreadsNullView() {
        presenter.attachView(null);

        Subreddit subreddit = new Subreddit();
        subreddit.setDisplayName("Sample name");

        presenter.openSubredditThreads(subreddit);

        verifyZeroInteractions(mockView);
    }
}

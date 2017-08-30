package atamayo.offlinereader.ThreadComments;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.TestScheduler;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThreadCommentsPresenterTest {
    private List<RedditComment> initialList;
    private List<RedditComment> additionalList;

    @Mock
    private ThreadCommentsContract.View mockView;

    @Mock
    private SubredditsDataSource mockRepository;

    private RedditThread currentThread;

    private ThreadCommentsPresenter presenter;

    @Before
    public void setUp() {

        String sampleName = "Sample full name";
        currentThread = new RedditThread();
        currentThread.setFullName(sampleName);
        when(mockRepository.getRedditThread(anyString()))
                .thenReturn(currentThread);

        presenter = new ThreadCommentsPresenter(sampleName, mockRepository, new TestScheduler());
        presenter.attachView(mockView);

        int initialListSize = 3;
        initialList = new ArrayList<>(initialListSize);
        for (int i = 0; i < initialListSize; i++) {
            RedditComment comment = new RedditComment();
            comment.setCommentFullname("Sample " + Integer.toString(i));
            initialList.add(comment);
        }

        int additionalListSize = 3;
        additionalList = new ArrayList<>(additionalListSize);
        for (int i = 0; i < additionalListSize; i++) {
            RedditComment comment = new RedditComment();
            comment.setCommentFullname("Additional " + Integer.toString(i));
            additionalList.add(comment);
        }
    }

    @Test
    public void testGetParentThread() {
        presenter.getParentThread();

        verify(mockView).showParentThread(currentThread);
    }

    @Test
    public void testGetParentThreadNullView() {
        presenter.attachView(null);
        presenter.getParentThread();

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testGetCommentsSuccessFirstLoad() {
        when(mockRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenReturn(initialList);
        presenter.getComments(true, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showInitialComments(initialList);
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetCommentsErrorFirstLoad() {
        when(mockRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException());
        presenter.getComments(true, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showEmptyComments();
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetCommentsSuccessNotFirstLoad() {
        when(mockRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenReturn(additionalList);
        presenter.getComments(false, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView).showMoreComments(additionalList);
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetCommentsErrorNotFirstLoad() {
        when(mockRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException());
        presenter.getComments(false, 0, 10);

        verify(mockView).showLoading(true);
        verify(mockView, never()).showMoreComments(anyListOf(RedditComment.class));
        verify(mockView, never()).showEmptyComments();
        verify(mockView).showLoading(false);
    }

    @Test
    public void testGetCommentsNullView() {
        presenter.attachView(null);

        when(mockRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenReturn(initialList);
        presenter.getComments(true, 0, 10);
        presenter.getComments(false, 0, 10);

        when(mockRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException());
        presenter.getComments(true, 0, 10);
        presenter.getComments(false, 0, 10);

        verifyZeroInteractions(mockView);
    }
}

package atamayo.offlinereader.ThreadComments;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

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
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.TestScheduler;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ThreadCommentsViewModelTest {
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private List<RedditComment> mComments;
    private String mThreadFullName;
    private RedditThread mThread;
    private int mNumItems;

    @Mock
    SubredditsDataSource mMockSubredditsRepository;

    @Mock
    Observer<RedditThread> mThreadObserver;

    @Mock
    Observer<List<RedditComment>> mCommentsObserver;

    @Mock
    Observer<String> mMessageObserver;

    private ThreadCommentsViewModel mCommentsviewModel;

    @Before
    public void setUp() {
        mComments = new ArrayList<>();
        mThreadFullName = "Sample";
        mThread = new RedditThread();
        mThread.setFullName(mThreadFullName);
        mNumItems = 3;
        for (int i = 0; i < mNumItems; i++) {
            RedditComment comment = new RedditComment();
            comment.setCommentFullname("Comment" + i);
            comment.setCommentThreadId(mThreadFullName);
            mComments.add(comment);
        }

        mCommentsviewModel = new ThreadCommentsViewModel(mMockSubredditsRepository, new TestScheduler(), mThreadFullName, mNumItems);
    }

    @Test
    public void testGetRedditThreadSuccess() {
        when(mMockSubredditsRepository.getRedditThread(anyString()))
                .thenReturn(mThread);

        mCommentsviewModel = new ThreadCommentsViewModel(mMockSubredditsRepository, new TestScheduler(), mThreadFullName, mNumItems);
        mCommentsviewModel.getThreadObservable().observeForever(mThreadObserver);

        verify(mThreadObserver).onChanged(mThread);
    }

    @Test
    public void testGetRedditThreadFail() {
        doThrow(new RuntimeException()).when(mMockSubredditsRepository).getRedditThread(anyString());

        mCommentsviewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
    }

    @Test
    public void testGetCommentsSuccess() {
        when(mMockSubredditsRepository.getCommentsForThread(anyString(), anyInt(), anyInt()))
                .thenReturn(mComments);

        mCommentsviewModel.getComments(mNumItems);
        mCommentsviewModel.getCommentsObservable().observeForever(mCommentsObserver);

        verify(mCommentsObserver).onChanged(mComments);
    }

    @Test
    public void testGetCommentsFail() {
        doThrow(new RuntimeException()).when(mMockSubredditsRepository).getCommentsForThread(anyString(), anyInt(), anyInt());

        mCommentsviewModel.getComments(mNumItems);
        mCommentsviewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
    }
}

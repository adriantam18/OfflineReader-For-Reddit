package atamayo.offlinereader.Keywords;

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
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.TestScheduler;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeywordsViewModelTest {
    @Rule
    public TestRule rule = new InstantTaskExecutorRule();

    private List<String> mKeywords;
    private String mSubredditName;
    private String mSampleKeyword;

    @Mock
    KeywordsPreference mMockRepository;

    @Mock
    Observer<List<String>> mKeywordsObserver;

    @Mock
    Observer<String> mMessageObserver;

    private KeywordsViewModel mKeywordsViewModel;

    @Before
    public void setUp() {
        mSubredditName = "TestSubreddit";
        mSampleKeyword = "Sample";

        mKeywordsViewModel = new KeywordsViewModel(mMockRepository, new TestScheduler(), mSubredditName);
        mKeywords = Arrays.asList("sample 1", "sample 2", "sample 3");
    }

    @Test
    public void getKeywordsSuccess() {
        when(mMockRepository.getKeywords(anyString()))
                .thenReturn(mKeywords);

        mKeywordsViewModel = new KeywordsViewModel(mMockRepository, new TestScheduler(), mSubredditName);
        mKeywordsViewModel.getKeywordsObservable().observeForever(mKeywordsObserver);

        Assert.assertEquals(mKeywords.size(), mKeywordsViewModel.getKeywordsObservable().getValue().size());
    }

    @Test
    public void getKeywordsFail() {
        when(mMockRepository.getKeywords(anyString()))
                .thenReturn(null);

        mKeywordsViewModel = new KeywordsViewModel(mMockRepository, new TestScheduler(), mSubredditName);
        mKeywordsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMessageObserver).onChanged(anyString());
    }

    @Test
    public void addKeywordSuccess() {
        when(mMockRepository.addKeyword(mSubredditName, mSampleKeyword))
                .thenReturn(true);
        when(mMockRepository.getKeywords(anyString()))
                .thenReturn(mKeywords);

        mKeywordsViewModel.addKeyword(mSampleKeyword);
        mKeywordsViewModel.getKeywordsObservable().observeForever(mKeywordsObserver);

        //2 times because of initial loading and after adding
        Assert.assertEquals(mKeywords.size(), mKeywordsViewModel.getKeywordsObservable().getValue().size());
    }

    @Test
    public void addKeywordFail() {
        when(mMockRepository.addKeyword(mSubredditName, mSampleKeyword))
                .thenReturn(false);

        mKeywordsViewModel.addKeyword(mSampleKeyword);
        mKeywordsViewModel.getMessageObservable().observeForever(mMessageObserver);

        verify(mMockRepository, times(1)).getKeywords(mSubredditName);
        verify(mMessageObserver).onChanged(anyString());
    }

    @Test
    public void removeKeywordSuccess() {
        when(mMockRepository.getKeywords(anyString()))
                .thenReturn(mKeywords);

        mKeywordsViewModel.getKeywordsObservable().observeForever(mKeywordsObserver);
        mKeywordsViewModel.clearKeywords();

        Assert.assertEquals(mKeywords.size(), mKeywordsViewModel.getKeywordsObservable().getValue().size());
    }

    @Test
    public void clearKeywordsSuccess() {
        when(mMockRepository.getKeywords(anyString()))
                .thenReturn(new ArrayList<>());

        mKeywordsViewModel.getKeywordsObservable().observeForever(mKeywordsObserver);
        mKeywordsViewModel.clearKeywords();

        Assert.assertEquals(0, mKeywordsViewModel.getKeywordsObservable().getValue().size());
    }
}

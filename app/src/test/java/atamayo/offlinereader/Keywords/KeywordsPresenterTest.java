package atamayo.offlinereader.Keywords;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.TestScheduler;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KeywordsPresenterTest {
    private List<String> keywords;

    @Mock
    private KeywordsContract.View mockView;

    @Mock
    private KeywordsDataSource mockRepository;

    private String subredditName;

    private KeywordsPresenter presenter;

    @Before
    public void setUp() {
        subredditName = "Sample name";

        presenter = new KeywordsPresenter(subredditName, mockRepository, new TestScheduler());
        presenter.attachView(mockView);
        
        int sampleSize = 3;
        keywords = new ArrayList<>(sampleSize);
        for (int i = 0; i < sampleSize; i++) {
            keywords.add("Sample " + Integer.toString(i));
        }
    }

    @Test
    public void testGetKeywordsSuccess() {
        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        presenter.getKeywords();

        Collections.reverse(keywords);
        verify(mockView).showKeywordsList(keywords);
    }

    @Test
    public void testGetKeywordsError() {
        when(mockRepository.getKeywords(anyString()))
                .thenThrow(new RuntimeException());
        presenter.getKeywords();

        verify(mockView).showMessage(anyString(), anyString());
    }

    @Test
    public void testGetKeywordsNullView() {
        presenter.attachView(null);

        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        presenter.getKeywords();

        when(mockRepository.getKeywords(anyString()))
                .thenThrow(new RuntimeException());
        presenter.getKeywords();

        verifyZeroInteractions(mockView);
    }

    @Test
    public void testAddKeywordSuccess() {
        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        when(mockRepository.addKeyword(anyString(), anyString()))
                .thenReturn(true);
        presenter.addKeyword("keyword");

        verify(mockView).showKeywordsList(keywords);
    }

    @Test
    public void testAddKeywordError() {
        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        when(mockRepository.addKeyword(anyString(), anyString()))
                .thenReturn(false);
        presenter.addKeyword("keyword");

        verify(mockView).showMessage(anyString(), anyString());
    }

    @Test
    public void testAddKeywordExceptionThrown() {
        when(mockRepository.addKeyword(anyString(), anyString()))
                .thenThrow(new RuntimeException());
        presenter.addKeyword("keyword");

        verify(mockView).showMessage(anyString(), anyString());
    }

    @Test
    public void testAddKeywordNullView() {
        presenter.attachView(null);

        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        when(mockRepository.addKeyword(anyString(), anyString()))
                .thenReturn(true);
        presenter.addKeyword("keyword");

        when(mockRepository.addKeyword(anyString(), anyString()))
                .thenReturn(false);
        presenter.addKeyword("keyword");

        when(mockRepository.addKeyword(anyString(), anyString()))
                .thenThrow(new RuntimeException());
        presenter.addKeyword("keyword");


        verifyZeroInteractions(mockView);
    }

    @Test
    public void testRemoveKeywordsSuccess() {
        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        presenter.removeKeyword("Sample");

        verify(mockRepository).deleteKeyword(anyString(), anyString());
        verify(mockView).showKeywordsList(keywords);
    }

    @Test
    public void testRemoveKeywordsError() {
        when(mockRepository.getKeywords(anyString()))
                .thenThrow(new RuntimeException());
        presenter.removeKeyword("Sample");

        verify(mockRepository).deleteKeyword(anyString(), anyString());
        verify(mockView).showMessage(anyString(), anyString());
    }

    @Test
    public void testRemoveKeywordsNullView() {
        presenter.attachView(null);

        when(mockRepository.getKeywords(anyString()))
                .thenReturn(keywords);
        presenter.removeKeyword("Sample");

        when(mockRepository.getKeywords(anyString()))
                .thenThrow(new RuntimeException());
        presenter.removeKeyword("Sample");

        verify(mockRepository, times(2)).deleteKeyword(anyString(), anyString());
        verifyZeroInteractions(mockView);
    }

    @Test
    public void testClearKeywordsSuccess() {
        presenter.clearKeywords();

        verify(mockRepository).clearKeywords(anyString());
        verify(mockView).showKeywordsList(new ArrayList<>());
    }

    @Test
    public void testClearKeywordsError() {
        doThrow(new RuntimeException()).when(mockRepository).clearKeywords(anyString());
        presenter.clearKeywords();

        verify(mockView).showMessage(anyString(), anyString());
    }

    @Test
    public void testClearKeywordsNullView() {
        presenter.attachView(null);
        presenter.clearKeywords();

        doThrow(new RuntimeException()).when(mockRepository).clearKeywords(anyString());
        presenter.clearKeywords();

        verifyZeroInteractions(mockView);
    }
}

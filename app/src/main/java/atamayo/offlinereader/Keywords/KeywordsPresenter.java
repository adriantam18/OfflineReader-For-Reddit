package atamayo.offlinereader.Keywords;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import atamayo.offlinereader.Data.KeywordsDataSource;

public class KeywordsPresenter implements KeywordsContract.Presenter {
    private KeywordsDataSource mKeywordsSource;
    private List<String> mKeywords;
    private Set<String> mKeywordsSet;
    private String mSubreddit;
    private KeywordsContract.View mView;

    public KeywordsPresenter(KeywordsDataSource dataSource, KeywordsContract.View view){
        mKeywordsSource = dataSource;
        mView = view;
        mKeywords = new ArrayList<>();
        mKeywordsSet = new HashSet<>();
    }

    @Override
    public void initKeywordsList(String subreddit) {
        mKeywordsSet.addAll(mKeywordsSource.getKeywords(subreddit));
        mKeywords.addAll(mKeywordsSet);
        mView.showKeywordsList(mKeywords);
        mSubreddit = subreddit;
    }

    @Override
    public void addKeyword(String keyword) {
        if(!mKeywordsSet.contains(keyword)) {
            mKeywordsSet.add(keyword);
            mKeywords.add(0, keyword);
            mView.showKeywordsList(mKeywords);
        }else{
            mView.showMessage("", "You already have this keyword");
        }
    }

    @Override
    public void removeKeyword(String keyword) {
        mKeywordsSet.remove(keyword);
        mKeywords.remove(keyword);
        mView.showKeywordsList(mKeywords);
    }

    @Override
    public void clearKeywords(){
        if(!mSubreddit.isEmpty()) {
            mKeywordsSource.clearKeywords(mSubreddit);
            mKeywords.clear();
            mKeywordsSet.clear();
            mView.showKeywordsList(mKeywords);
        }
    }

    @Override
    public void persistKeywords(){
        if(!mSubreddit.isEmpty())
            mKeywordsSource.updateKeywords(mSubreddit, mKeywords);
    }
}

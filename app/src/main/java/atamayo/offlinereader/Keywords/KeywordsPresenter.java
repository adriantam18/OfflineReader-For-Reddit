package atamayo.offlinereader.Keywords;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.Data.KeywordsDataSource;

/**
 * Created by apdt_18 on 11/15/2016.
 */
public class KeywordsPresenter implements KeywordsContract.Presenter {
    KeywordsDataSource mKeywordsSource;
    List<String> mKeywords;
    String mSubreddit;
    KeywordsContract.View mView;

    public KeywordsPresenter(KeywordsDataSource dataSource, KeywordsContract.View view){
        mKeywordsSource = dataSource;
        mView = view;
        mKeywords = new ArrayList<>();
    }

    @Override
    public void initKeywordsList(String subreddit) {
        mKeywords = mKeywordsSource.getKeywords(subreddit);
        mView.showKeywordsList(mKeywords);
        mSubreddit = subreddit;
    }

    @Override
    public void addKeyword(String keyword) {
        mKeywords.add(keyword);
        mView.updateKeywordsList();
    }

    @Override
    public void removeKeyword(int position) {
        mKeywords.remove(position);
        mView.updateKeywordsList();
    }

    @Override
    public void clearKeywords(){
        if(!mSubreddit.isEmpty()) {
            mKeywordsSource.clearKeywords(mSubreddit);
            mKeywords.clear();
        }
    }

    @Override
    public void persistKeywords(){
        if(!mSubreddit.isEmpty())
            mKeywordsSource.updateKeywords(mSubreddit, mKeywords);
    }
}

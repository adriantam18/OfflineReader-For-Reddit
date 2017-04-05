package atamayo.offlinereader.Keywords;

import java.util.List;

import atamayo.offlinereader.BaseView;

public interface KeywordsContract {
    interface View extends BaseView<KeywordsContract.Presenter>{
        void showKeywordsList(List<String> keywords);
        void updateKeywordsList();
    }

    interface Presenter{
        void initKeywordsList(String subreddit);
        void addKeyword(String keyword);
        void removeKeyword(int position);
        void clearKeywords();
        void persistKeywords();
    }
}
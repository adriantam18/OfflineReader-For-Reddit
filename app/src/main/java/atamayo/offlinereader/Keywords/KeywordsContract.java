package atamayo.offlinereader.Keywords;

import java.util.List;

import atamayo.offlinereader.BaseView;

public interface KeywordsContract {
    interface View extends BaseView<KeywordsContract.Presenter>{
        void showKeywordsList(List<String> keywords);
        void showMessage(String title, String message);
    }

    interface Presenter{
        void initKeywordsList(String subreddit);
        void addKeyword(String keyword);
        void removeKeyword(String keyword);
        void clearKeywords();
        void persistKeywords();
    }
}

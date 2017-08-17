package atamayo.offlinereader.Keywords;

import java.util.List;

import atamayo.offlinereader.MVP.BaseView;

/**
 * This specifies the contract between the view and the presenter for keywords.
 */
public interface KeywordsContract {
    interface View extends BaseView {
        void showKeywordsList(List<String> keywords);
        void showMessage(String title, String message);
    }

    interface Presenter {
        void getKeywords();
        void addKeyword(String keyword);
        void removeKeyword(String keyword);
        void clearKeywords();
    }
}

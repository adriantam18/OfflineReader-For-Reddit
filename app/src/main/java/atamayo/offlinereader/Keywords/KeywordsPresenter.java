package atamayo.offlinereader.Keywords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import atamayo.offlinereader.MVP.BaseRxPresenter;
import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Utils.Schedulers.BaseScheduler;
import io.reactivex.Completable;
import io.reactivex.Observable;

/**
 * Presenter for UI ({@link KeywordsListing}) that displays keywords for
 * a subreddit.
 */
public class KeywordsPresenter extends BaseRxPresenter<KeywordsContract.View>
        implements KeywordsContract.Presenter {
    private static final String FAILED_TO_ADD = "Failed to add. You may have it already.";
    private static final String FAILED_TO_LOAD = "Failed to load keywords.";
    private static final String FAILED_TO_DELETE = "Failed to delete keyword(s). Try again later.";
    private String mSubredditName;
    private KeywordsDataSource mKeywordsSource;
    private BaseScheduler mScheduler;

    public KeywordsPresenter(String subreddit,
                             KeywordsDataSource dataSource,
                             BaseScheduler scheduler) {
        mSubredditName = subreddit;
        mKeywordsSource = dataSource;
        mScheduler = scheduler;
    }

    @Override
    public void getKeywords() {
        Observable<List<String>> keywordsObservable = Observable.fromCallable(() ->
                mKeywordsSource.getKeywords(mSubredditName));

        mDisposables.add(keywordsObservable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(keywords -> {
                            Collections.reverse(keywords);
                            getView().showKeywordsList(keywords);
                        },
                        throwable -> getView().showMessage("", FAILED_TO_LOAD),
                        () -> {
                        }));
    }

    @Override
    public void addKeyword(String keyword) {
        Observable<Boolean> keywordsObservable = Observable.fromCallable(() ->
                mKeywordsSource.addKeyword(mSubredditName, keyword));

        mDisposables.add(keywordsObservable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(this::processAddKeyword,
                        throwable -> getView().showMessage("", FAILED_TO_ADD),
                        () -> {
                        }));
    }

    private void processAddKeyword(boolean success) {
        if (success) {
            getKeywords();
        } else {
            getView().showMessage("", FAILED_TO_ADD);
        }
    }

    @Override
    public void removeKeyword(String keyword) {
        Completable keywordsCompletable = Completable.fromRunnable(() ->
                mKeywordsSource.deleteKeyword(mSubredditName, keyword));

        mDisposables.add(keywordsCompletable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(this::getKeywords,
                        throwable -> getView().showMessage("", FAILED_TO_DELETE)));
    }

    @Override
    public void clearKeywords() {
        Completable keywordsCompletable = Completable.fromRunnable(() ->
                mKeywordsSource.clearKeywords(mSubredditName));

        mDisposables.add(keywordsCompletable
                .subscribeOn(mScheduler.io())
                .observeOn(mScheduler.mainThread())
                .subscribe(() -> getView().showKeywordsList(new ArrayList<>()),
                        throwable -> getView().showMessage("", FAILED_TO_DELETE)));
    }

    @Override
    protected KeywordsContract.View createFakeView() {
        return new KeywordsContract.View() {
            @Override
            public void showKeywordsList(List<String> keywords) {

            }

            @Override
            public void showMessage(String title, String message) {

            }
        };
    }
}

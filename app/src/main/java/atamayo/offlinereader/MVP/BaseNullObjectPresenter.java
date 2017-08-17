package atamayo.offlinereader.MVP;

/**
 * Base class for a presenter that implements Null Object pattern to deal with
 * situations in which a view is null.
 *
 * @param <V> type of ({@link BaseView})
 */
public abstract class BaseNullObjectPresenter<V extends BaseView> implements BasePresenter<V> {
    private V mRealView;
    private V mFakeView;

    public BaseNullObjectPresenter() {
        mFakeView = createFakeView();
    }

    public void attachView(V view) {
        mRealView = view;
    }

    public void detachView() {
        mRealView = null;
    }

    protected abstract V createFakeView();

    protected V getView() {
        if (mRealView != null) {
            return mRealView;
        } else {
            if (mFakeView == null) {
                mFakeView = createFakeView();
            }

            return mFakeView;
        }
    }
}

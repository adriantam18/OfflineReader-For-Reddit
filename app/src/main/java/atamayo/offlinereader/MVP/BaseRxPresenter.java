package atamayo.offlinereader.MVP;

import io.reactivex.disposables.CompositeDisposable;

public abstract class BaseRxPresenter<V extends BaseView> extends BaseNullObjectPresenter<V> {
    protected CompositeDisposable mDisposables;

    public BaseRxPresenter() {
        super();
        mDisposables = new CompositeDisposable();
    }

    @Override
    public void attachView(V view) {
        super.attachView(view);
        if (mDisposables == null || mDisposables.isDisposed()) {
            mDisposables = new CompositeDisposable();
        }
    }

    @Override
    public void detachView() {
        super.detachView();
        mDisposables.dispose();
    }
}

package atamayo.offlinereader.MVP;

public interface BasePresenter<V extends BaseView> {
    void attachView(V view);
    void detachView();
}

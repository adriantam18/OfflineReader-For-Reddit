package atamayo.offlinereader;

public interface BasePresenter<T> {
    void subscribe(T view);
    void unsubscribe();
}

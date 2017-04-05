package atamayo.offlinereader.Utils;

public interface NetworkResponse<T> {
    void onSuccess(T object);
    void onError(String message);
}

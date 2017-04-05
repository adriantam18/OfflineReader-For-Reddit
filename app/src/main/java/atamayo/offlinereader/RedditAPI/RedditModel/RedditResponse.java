package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

public class RedditResponse<T> {
    @Expose
    T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

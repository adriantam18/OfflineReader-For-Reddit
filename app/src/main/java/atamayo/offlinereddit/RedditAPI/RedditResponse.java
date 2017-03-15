package atamayo.offlinereddit.RedditAPI;

import com.google.gson.annotations.Expose;

public class RedditResponse {
    @Expose
    String kind;

    @Expose
    RedditResponseData data;

    public RedditResponseData getData() {
        return data;
    }

    public void setData(RedditResponseData data) {
        this.data = data;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }
}

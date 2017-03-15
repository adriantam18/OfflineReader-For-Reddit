package atamayo.offlinereddit.RedditAPI;

import com.google.gson.annotations.Expose;

public class RedditChildData {
    @Expose
    String kind;

    @Expose
    RedditThread data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public RedditThread getData() {
        return data;
    }

    public void setData(RedditThread data) {
        this.data = data;
    }
}

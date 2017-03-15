package atamayo.offlinereddit.RedditAPI;

import com.google.gson.annotations.Expose;

public class AboutSubredditResponse {
    @Expose
    String kind;

    @Expose
    Subreddit data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public Subreddit getData() {
        return data;
    }

    public void setData(Subreddit data) {
        this.data = data;
    }
}

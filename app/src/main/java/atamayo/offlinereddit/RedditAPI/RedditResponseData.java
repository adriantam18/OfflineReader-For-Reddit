package atamayo.offlinereddit.RedditAPI;

import com.google.gson.annotations.Expose;

import java.util.List;

public class RedditResponseData {
    @Expose
    String modhash;

    @Expose
    String after;

    @Expose
    String before;

    @Expose
    List<RedditChildData> children;

    public String getModhash() {
        return modhash;
    }

    public void setModhash(String modhash) {
        this.modhash = modhash;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public List<RedditChildData> getChildren() {
        return children;
    }

    public void setChildren(List<RedditChildData> children) {
        this.children = children;
    }
}

package atamayo.offlinereddit.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

import java.util.List;

public class RedditListing extends RedditObject {
    @Expose
    String modhash;

    @Expose
    String after;

    @Expose
    String before;

    @Expose
    List<RedditObject> children;

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

    public List<RedditObject> getChildren() {
        return children;
    }

    public void setChildren(List<RedditObject> children) {
        this.children = children;
    }
}

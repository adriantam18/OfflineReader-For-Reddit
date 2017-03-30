package atamayo.offlinereddit.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditObject;

@Entity(
        nameInDb = "SUBREDDITS_LIST"
)
public class Subreddit extends RedditObject {
    @Id
    Long id;

    @Expose
    @SerializedName("display_name")
    @Index(unique = true)
    String displayName;

    @Expose
    int susbscribers;

    @Expose
    boolean over18;

    @Generated(hash = 1278174245)
    public Subreddit(Long id, String displayName, int susbscribers,
            boolean over18) {
        this.id = id;
        this.displayName = displayName;
        this.susbscribers = susbscribers;
        this.over18 = over18;
    }

    @Generated(hash = 2105214667)
    public Subreddit() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getSusbscribers() {
        return susbscribers;
    }

    public void setSusbscribers(int susbscribers) {
        this.susbscribers = susbscribers;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public boolean getOver18() {
        return this.over18;
    }
}

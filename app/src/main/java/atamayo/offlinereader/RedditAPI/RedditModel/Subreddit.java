package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

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
    @SerializedName("display_name_prefixed")
    String displayNamePrefixed;

    @Expose
    int susbscribers;

    @Expose
    boolean over18;

    public Subreddit(Long id, String displayName, int susbscribers,
            boolean over18) {
        this.id = id;
        this.displayName = displayName;
        this.susbscribers = susbscribers;
        this.over18 = over18;
    }

    public Subreddit() {
    }

    @Generated(hash = 914230089)
    public Subreddit(Long id, String displayName, String displayNamePrefixed,
            int susbscribers, boolean over18) {
        this.id = id;
        this.displayName = displayName;
        this.displayNamePrefixed = displayNamePrefixed;
        this.susbscribers = susbscribers;
        this.over18 = over18;
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

    public String getDisplayNamePrefixed() {
        return displayNamePrefixed;
    }

    public void setDisplayNamePrefixed(String displayNamePrefixed) {
        this.displayNamePrefixed = displayNamePrefixed;
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

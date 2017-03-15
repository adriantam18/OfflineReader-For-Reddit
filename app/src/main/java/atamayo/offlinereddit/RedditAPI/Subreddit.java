package atamayo.offlinereddit.RedditAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

@Entity(
        nameInDb = "SUBREDDITS_LIST"
)
public class Subreddit{
    @Id
    Long id;

    @Expose
    @SerializedName("display_name")
    @Index(unique = true)
    String displayName;

    @Expose
    int susbscribers;

    @Generated(hash = 1901327986)
    public Subreddit(Long id, String displayName, int susbscribers) {
        this.id = id;
        this.displayName = displayName;
        this.susbscribers = susbscribers;
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
}

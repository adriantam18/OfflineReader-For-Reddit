package atamayo.offlinereader.RedditAPI.RedditModel;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

@Entity(
        tableName = "subreddit",
        indices = {@Index(value = "display_name", unique = true)}
)
public class Subreddit extends RedditObject {
    @PrimaryKey(autoGenerate = true)
    long id;

    @Expose
    @SerializedName("display_name")
    @ColumnInfo(name = "display_name")
    @NonNull
    String displayName;

    @Expose
    @SerializedName("display_name_prefixed")
    @ColumnInfo(name = "display_name_prefixed")
    String displayNamePrefixed;

    @Expose
    int subscribers;

    @Expose
    boolean over18;

    public Subreddit(String displayName, String displayNamePrefixed, int subscribers,
            boolean over18) {
        this.displayName = displayName;
        this.displayNamePrefixed = displayNamePrefixed;
        this.subscribers = subscribers;
        this.over18 = over18;
    }

    public Subreddit() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public int getSubscribers() {
        return subscribers;
    }

    public void setSubscribers(int subscribers) {
        this.subscribers = subscribers;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public boolean getOver18() {
        return this.over18;
    }
}

package atamayo.offlinereddit.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditObject;

@Entity(
        nameInDb = "REDDIT_THREADS"
)
public class RedditThread extends RedditObject {
    @Id
    Long id;

    boolean wasClicked = false;

    @Expose
    @SerializedName("name")
    @Index(unique = true)
    String fullName;

    @Expose
    @SerializedName("id")
    String threadId;

    @Expose
    String subreddit;

    @Expose
    String title;

    @Expose
    int score;

    @Expose
    int ups;

    @Expose
    int downs;

    @Expose
    String permalink;

    @SerializedName("num_comments")
    @Expose
    int numComments;

    @Expose
    @SerializedName("created_utc")
    long createdUTC;

    @Expose
    boolean over18;

    @Generated(hash = 710051809)
    public RedditThread(Long id, boolean wasClicked, String fullName,
            String threadId, String subreddit, String title, int score, int ups,
            int downs, String permalink, int numComments, long createdUTC,
            boolean over18) {
        this.id = id;
        this.wasClicked = wasClicked;
        this.fullName = fullName;
        this.threadId = threadId;
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
        this.ups = ups;
        this.downs = downs;
        this.permalink = permalink;
        this.numComments = numComments;
        this.createdUTC = createdUTC;
        this.over18 = over18;
    }

    @Generated(hash = 1439624015)
    public RedditThread() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean getWasClicked() {
        return this.wasClicked;
    }

    public void setWasClicked(boolean wasClicked) {
        this.wasClicked = wasClicked;
    }

    public String getFullName() {
        return this.fullName;
    }

    public void setFullName(String name) {
        this.fullName = name;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getUps() {
        return ups;
    }

    public void setUps(int ups) {
        this.ups = ups;
    }

    public int getDowns() {
        return downs;
    }

    public void setDowns(int downs) {
        this.downs = downs;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public long getCreatedUTC() {
        return createdUTC;
    }

    public void setCreatedUTC(long createdUTC) {
        this.createdUTC = createdUTC;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public boolean getOver18() {
        return this.over18;
    }
}

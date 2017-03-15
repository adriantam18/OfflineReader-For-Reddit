package atamayo.offlinereddit.RedditAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

@Entity(
        nameInDb = "REDDIT_THREADS"
)
public class RedditThread {
    @Id
    Long id;

    String filename;

    boolean wasClicked = false;

    @Expose
    String subreddit;

    @Expose
    @Index(unique = true)
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

    @Generated(hash = 1062357037)
    public RedditThread(Long id, String filename, boolean wasClicked,
            String subreddit, String title, int score, int ups, int downs,
            String permalink, int numComments) {
        this.id = id;
        this.filename = filename;
        this.wasClicked = wasClicked;
        this.subreddit = subreddit;
        this.title = title;
        this.score = score;
        this.ups = ups;
        this.downs = downs;
        this.permalink = permalink;
        this.numComments = numComments;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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

    public boolean getWasClicked() {
        return this.wasClicked;
    }

    public void setWasClicked(boolean wasClicked) {
        this.wasClicked = wasClicked;
    }
}

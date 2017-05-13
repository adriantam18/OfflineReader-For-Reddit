package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;

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
    String author;

    @Expose
    int score;

    @Expose
    int ups;

    @Expose
    int downs;

    @Expose
    String selftext;

    @Expose
    @SerializedName("selftext_html")
    String selftextHtml;

    @Expose
    String permalink;

    @Expose
    @SerializedName("num_comments")
    int numComments;

    @Expose
    @SerializedName("created_utc")
    long createdUTC;

    @Expose
    boolean over18;

    @Generated(hash = 154724876)
    public RedditThread(Long id, boolean wasClicked, String fullName,
            String threadId, String subreddit, String title, String author,
            int score, int ups, int downs, String selftext, String selftextHtml,
            String permalink, int numComments, long createdUTC, boolean over18) {
        this.id = id;
        this.wasClicked = wasClicked;
        this.fullName = fullName;
        this.threadId = threadId;
        this.subreddit = subreddit;
        this.title = title;
        this.author = author;
        this.score = score;
        this.ups = ups;
        this.downs = downs;
        this.selftext = selftext;
        this.selftextHtml = selftextHtml;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
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

    public String getSelftext() {
        return selftext;
    }

    public void setSelftext(String selftext) {
        this.selftext = selftext;
    }

    public String getSelftextHtml() {
        return selftextHtml;
    }

    public void setSelftextHtml(String selftextHtml) {
        this.selftextHtml = selftextHtml;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int numComments) {
        this.numComments = numComments;
    }

    public String getFormattedTime(){
        long seconds = (System.currentTimeMillis() - (this.getCreatedUTC() * 1000)) / 1000;
        if(seconds < 60){
            return Long.toString(seconds) + " second(s) ago";
        }

        long minutes = seconds / 60;
        if(minutes < 60){
            return Long.toString(minutes) + " minute(s) ago";
        }

        long hours = minutes / 60;
        if(hours < 24){
            return Long.toString(hours) + " hour(s) ago";
        }

        long days = hours / 24;
        if(days < 7){
            return Long.toString(days) + " day(s) ago";
        }

        long weeks = days / 7;
        if(weeks < 4){
            return Long.toString(weeks) + " week(s) ago";
        }

        long months = weeks / 4;
        if(months < 12){
            return Long.toString(months) + " month(s) ago";
        }

        long years = months / 12;
        return Long.toString(years) + " year(s) ago";
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

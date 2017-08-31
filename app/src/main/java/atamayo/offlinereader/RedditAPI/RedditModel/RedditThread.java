package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;

@Entity(
        nameInDb = "REDDIT_THREADS"
)
public class RedditThread extends RedditObject {
    @Transient
    private static final String MEDIA_PREFIX = "media_";

    @Transient
    private static final String COMMENT_PREFIX = "comment_";

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

    @Transient
    @Expose
    Preview preview;

    @Expose
    String thumbnail;

    @Transient
    byte[] imageBytes;

    String mediaPath;

    String commentPath;

    @Generated(hash = 273131024)
    public RedditThread(Long id, boolean wasClicked, String fullName, String threadId,
            String subreddit, String title, String author, int score, int ups, int downs,
            String selftext, String selftextHtml, String permalink, int numComments,
            long createdUTC, boolean over18, String thumbnail, String mediaPath,
            String commentPath) {
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
        this.thumbnail = thumbnail;
        this.mediaPath = mediaPath;
        this.commentPath = commentPath;
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
        return RedditTimeFormatter.format(this.createdUTC);
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

    public Preview getPreview() {
        return preview;
    }

    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    public void setImageBytes(byte[] imageBytes) {
        this.imageBytes = imageBytes;
    }

    public String getMediaPath(){
        return mediaPath;
    }

    public void setMediaPath(String mediaPath){
        this.mediaPath = mediaPath;
    }

    public String getCommentPath(){
        return commentPath;
    }

    public void setCommentPath(String commentPath){
        this.commentPath = commentPath;
    }

    public String getMediaFileName(){
        return MEDIA_PREFIX + fullName;
    }

    public String getCommentFileName(){
        return COMMENT_PREFIX + fullName;
    }
}

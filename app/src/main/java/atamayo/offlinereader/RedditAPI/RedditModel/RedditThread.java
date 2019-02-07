package atamayo.offlinereader.RedditAPI.RedditModel;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(
        tableName = "reddit_thread",
        indices = {@Index(value = "subreddit"), @Index(value = "full_name", unique = true)},
        foreignKeys = @ForeignKey(entity = Subreddit.class,
                                  parentColumns = "display_name",
                                  childColumns = "subreddit",
                                  onDelete = CASCADE)
)
public class RedditThread extends RedditObject {
    @Ignore
    private static final String MEDIA_PREFIX = "media_";

    @Ignore
    private static final String COMMENT_PREFIX = "comment_";

    boolean wasClicked = false;

    @PrimaryKey(autoGenerate = true)
    long id;

    @Expose
    @SerializedName("name")
    @ColumnInfo(name = "full_name")
    @NonNull
    String fullName;

    @Expose
    @SerializedName("id")
    @ColumnInfo(name = "thread_id")
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
    @ColumnInfo(name = "selftext_html")
    String selftextHtml;

    @Expose
    String permalink;

    @Expose
    @SerializedName("num_comments")
    @ColumnInfo(name = "num_comments")
    int numComments;

    @Expose
    @SerializedName("created_utc")
    @ColumnInfo(name = "created_utc")
    long createdUTC;

    @Expose
    boolean over18;

    @Ignore
    @Expose
    Preview preview;

    @Expose
    String thumbnail;

    @Ignore
    byte[] imageBytes;

    String mediaPath;

    String commentPath;

    public RedditThread(boolean wasClicked, String fullName, String threadId,
            String subreddit, String title, String author, int score, int ups, int downs,
            String selftext, String selftextHtml, String permalink, int numComments,
            long createdUTC, boolean over18, String thumbnail, String mediaPath,
            String commentPath) {
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

    public RedditThread() {
    }

    public boolean getWasClicked() {
        return this.wasClicked;
    }

    public void setWasClicked(boolean wasClicked) {
        this.wasClicked = wasClicked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

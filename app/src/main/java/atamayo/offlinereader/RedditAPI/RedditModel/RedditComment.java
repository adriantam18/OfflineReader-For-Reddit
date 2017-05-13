package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.greenrobot.greendao.annotation.Index;

public class RedditComment extends RedditObject {

    @Expose
    RedditObject replies;

    @Expose
    @SerializedName("id")
    String commentId;

    @Expose
    String author;

    @Expose
    String body;

    @Expose
    @SerializedName("body_html")
    String bodyHtml;

    @Expose
    int ups;

    @Expose
    int downs;

    @Expose
    int score;

    @Expose
    int depth;

    @Expose
    @SerializedName("name")
    @Index(unique = true)
    String commentFullname;

    @Expose
    @SerializedName("link_id")
    String commentThreadId;

    @Expose
    @SerializedName("parent_id")
    String parentId;

    @Expose
    @SerializedName("created_utc")
    long createdUTC;

    public RedditObject getReplies() {
        return replies;
    }

    public void setReplies(RedditObject replies) {
        this.replies = replies;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
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

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public String getCommentFullname() {
        return commentFullname;
    }

    public void setCommentFullname(String commentFullname) {
        this.commentFullname = commentFullname;
    }

    public String getCommentThreadId() {
        return commentThreadId;
    }

    public void setCommentThreadId(String commentThreadId) {
        this.commentThreadId = commentThreadId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
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
}

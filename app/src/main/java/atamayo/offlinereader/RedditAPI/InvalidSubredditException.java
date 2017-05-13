package atamayo.offlinereader.RedditAPI;

public class InvalidSubredditException extends IllegalArgumentException {
    @Override
    public String getMessage(){
        return "Could not find subreddit";
    }
}

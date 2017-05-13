package atamayo.offlinereader.RedditAPI;

public class DuplicateSubredditException extends IllegalArgumentException {
    @Override
    public String getMessage(){
        return "Subreddit had already been added";
    }
}

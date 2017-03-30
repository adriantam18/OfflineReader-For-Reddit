package atamayo.offlinereddit.Utils;

import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;

public interface NetworkResponse {
    void onSuccess(Subreddit subreddit);
    void onError(String message);
}

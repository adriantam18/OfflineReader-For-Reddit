package atamayo.offlinereader.RedditAPI;


import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditOAuthService {
    @GET("r/{subreddit}/.json")
    Single<RedditResponse<RedditListing>> listThreads(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}/comments/{threadId}.json")
    Single<ResponseBody> listCommentsJson(@Path("subreddit") String subreddit, @Path("threadId") String id);

    @GET("r/{subreddit}/about.json")
    Single<RedditResponse<Subreddit>> checkSubreddit(@Path("subreddit") String subreddit);
}

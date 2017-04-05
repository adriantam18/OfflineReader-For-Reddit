package atamayo.offlinereader.RedditAPI;

import java.util.List;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditOAuthInterface {
    @GET("r/{subreddit}/about.json")
    Call<RedditResponse<Subreddit>> showAbout(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}.json")
    Call<RedditResponse<RedditListing>> listThreads(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}/comments/{threadId}.json")
    Call<ResponseBody> listCommentsJson(@Path("subreddit") String subreddit, @Path("threadId") String id);
}

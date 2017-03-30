package atamayo.offlinereddit.RedditAPI;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface RedditApiInterface {
    @GET("r/{subreddit}/about.json")
    Call<RedditResponse<Subreddit>> showAbout(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}.json")
    Call<RedditResponse<RedditListing>> listThreads(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}/comments/{threadId}.json")
    Call<ResponseBody> listCommentsJson(@Path("subreddit") String subreddit, @Path("threadId") String id);
}

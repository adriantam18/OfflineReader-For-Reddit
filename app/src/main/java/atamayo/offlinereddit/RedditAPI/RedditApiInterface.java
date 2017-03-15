package atamayo.offlinereddit.RedditAPI;

import org.greenrobot.greendao.annotation.NotNull;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface RedditApiInterface {
    @GET("r/{subreddit}/about.json")
    Call<AboutSubredditResponse> showAbout(@Path("subreddit") String subreddit);

    @GET("r/{subreddit}/.json")
    Call<RedditResponse> listThreads(@Path("subreddit") String subreddit);

    @GET
    Call<ResponseBody> listComments(@Url @NotNull String permalink);
}

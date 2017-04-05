package atamayo.offlinereader.RedditAPI;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface RedditApiInterface {
    @POST("api/v1/access_token")
    Call<ResponseBody> getToken(@Body RequestBody body);

    @POST("api/v1/revoke_token")
    Call<ResponseBody> revokeToken(@Body RequestBody body);
}

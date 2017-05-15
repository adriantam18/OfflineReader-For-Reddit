package atamayo.offlinereader.RedditAPI;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RedditDefaultService {
    @POST("api/v1/access_token")
    Call<ResponseBody> getToken(@Body RequestBody body);

    @POST("api/v1/revoke_token")
    Call<ResponseBody> revokeToken(@Body RequestBody body);
}

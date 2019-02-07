package atamayo.offlinereader.RedditAPI;

import io.reactivex.Single;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RedditDefaultService {
    @POST("api/v1/access_token")
    Single<ResponseBody> getToken(@Body RequestBody body);

    @POST("api/v1/revoke_token")
    Single<ResponseBody> revokeToken(@Body RequestBody body);
}

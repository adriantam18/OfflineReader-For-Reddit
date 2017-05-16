package atamayo.offlinereader.RedditAPI;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RedditDefaultService {
    @POST("api/v1/access_token")
    Observable<ResponseBody> getToken(@Body RequestBody body);

    @POST("api/v1/revoke_token")
    Observable<ResponseBody> revokeToken(@Body RequestBody body);
}

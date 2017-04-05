package atamayo.offlinereader.RedditAPI;

import java.io.IOException;

import atamayo.offlinereader.Utils.RedditDownloader;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthenticationInterceptor implements Interceptor {
    private String mCredentials;

    public AuthenticationInterceptor(String credentials){
        mCredentials = credentials;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header(RedditDownloader.AUTHORIZATION, mCredentials)
                .header(RedditDownloader.USER_AGENT, RedditDownloader.CUSTOM_USER_AGENT);

        Request request = builder.build();
        return chain.proceed(request);
    }
}

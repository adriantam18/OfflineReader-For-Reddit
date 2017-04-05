package atamayo.offlinereader.RedditAPI;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RedditOAuthClient {
    public static final String OAUTH_URL = "https://oauth.reddit.com";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(RedditObject.class, new RedditObjectDeserializer())
            .create();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(OAUTH_URL)
            .addConverterFactory(GsonConverterFactory.create(gson));

    private static Retrofit retrofit = builder.build();

    public static <S> S createClass(Class<S> service, final String auth){
        if(!TextUtils.isEmpty(auth)){
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(auth);

            if(!httpClient.interceptors().contains(interceptor)){
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        return retrofit.create(service);
    }
}



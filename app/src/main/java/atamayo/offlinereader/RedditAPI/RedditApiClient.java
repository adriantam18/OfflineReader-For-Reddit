package atamayo.offlinereader.RedditAPI;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RedditApiClient {
    public static final String API_BASE_URL = "https://www.reddit.com/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(RedditObject.class, new RedditObjectDeserializer())
            .create();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson));

    private static Retrofit retrofit = builder.build();

    public static <S> S createClass(Class<S> service){
        return retrofit.create(service);
    }

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

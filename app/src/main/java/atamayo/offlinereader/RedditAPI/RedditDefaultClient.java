package atamayo.offlinereader.RedditAPI;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RedditDefaultClient {
    public static final String API_BASE_URL = "https://www.reddit.com/";

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(RedditObject.class, new RedditObjectDeserializer())
            .create();

    private static Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create());

    private static Retrofit retrofit = builder.build();

    public static <S> S createClass(Class<S> service){
        return retrofit.create(service);
    }

    public static <S> S createClass(Class<S> service, final String auth, Context context){
        if(!TextUtils.isEmpty(auth)){
            AuthenticationInterceptor interceptor = new AuthenticationInterceptor(auth);
            ConnectionInterceptor connectionInterceptor = new ConnectionInterceptor(context);

            httpClient.addInterceptor(connectionInterceptor);

            if(!httpClient.interceptors().contains(interceptor)){
                httpClient.addInterceptor(interceptor);

                builder.client(httpClient.build());
                retrofit = builder.build();
            }
        }

        return retrofit.create(service);
    }
}

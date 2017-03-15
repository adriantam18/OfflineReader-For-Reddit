package atamayo.offlinereddit.RedditAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by apdt_18 on 3/15/2017.
 */

public class RedditMobileApiClient {
    public static final String API_BASE_URL = "https://m.reddit.com/";
    private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public static <S> S createClass(Class<S> service){
        return retrofit.create(service);
    }
}

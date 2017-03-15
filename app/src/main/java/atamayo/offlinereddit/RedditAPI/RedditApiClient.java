package atamayo.offlinereddit.RedditAPI;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RedditApiClient {
    public static final String API_BASE_URL = "https://www.reddit.com/";
    private static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public static <S> S createClass(Class<S> service){
        return retrofit.create(service);
    }
}

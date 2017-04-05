package atamayo.offlinereader.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import atamayo.offlinereader.RedditAPI.RedditApiClient;
import atamayo.offlinereader.RedditAPI.RedditApiInterface;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditObject;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.RedditAPI.RedditOAuthClient;
import atamayo.offlinereader.RedditAPI.RedditOAuthInterface;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Header;

public class RedditDownloader {
    private final static String TAG = "REDDIT DOWNLOADER";

    public final static String CLIENT_ID = "YOUR CLIENT ID";
    public final static String CLIENT_SECRET = "YOUR CLIENT SECRET";
    public final static String INSTALLED_CLIENT_GRANT = Html.escapeHtml("https://oauth.reddit.com/grants/installed_client");
    public final static String DEVICE_ID = "YOUR DEVICE ID";
    public final static String CUSTOM_USER_AGENT = "YOUR USER AGENT";
    public final static String BEARER = "bearer ";
    public final static String BASIC = "Basic ";

    public final static String AUTHORIZATION = "Authorization";
    public final static String USER_AGENT = "User-Agent";

    public final static String QUERY_GRANT_TYPE = "grant_type";
    public final static String QUERY_DEVICE_ID = "device_id";

    public final static String TOKEN = "token";
    public final static String ACCESS_TOKEN = "access_token";
    public final static String TOKEN_TYPE = "token_type";
    public final static String TOKEN_TYPE_HINT = "token_type_hint";
    public final static String EXPIRES_IN = "expires_in";

    public final static String REDDIT_OAUTH_KEY = "reddit_oauth_key";

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private RedditApiInterface redditApi;
    private RedditOAuthInterface redditOauth;

    public RedditDownloader(Context context){
        preferences = context.getSharedPreferences(REDDIT_OAUTH_KEY, Context.MODE_PRIVATE);
        preferencesEditor = preferences.edit();

        initDefaultClient();
        initOauthClient();
    }

    private void initDefaultClient(){
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String auth = BASIC + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        redditApi = RedditApiClient.createClass(RedditApiInterface.class, auth);
    }

    private void initOauthClient(){
        if(needsToken()) {
            getToken(new NetworkResponse<Boolean>() {
                @Override
                public void onSuccess(Boolean object) {
                    if (object) {
                        redditOauth = RedditOAuthClient.createClass(RedditOAuthInterface.class, getAuthHeader());
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }else{
            redditOauth = RedditOAuthClient.createClass(RedditOAuthInterface.class, getAuthHeader());
        }
    }

    private String getAuthHeader(){
        String accessToken = preferences.getString(ACCESS_TOKEN, "");
        return BEARER + accessToken;
    }

    private void getToken(final NetworkResponse<Boolean> callback){
        RequestBody body = new FormBody.Builder()
                .add(QUERY_GRANT_TYPE, INSTALLED_CLIENT_GRANT)
                .add(QUERY_DEVICE_ID, DEVICE_ID)
                .build();

        if(needsToken()) {
            Call<ResponseBody> call = redditApi.getToken(body);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body().string());
                            String accessToken = jsonResponse.getString(ACCESS_TOKEN);
                            long timeExpire = System.currentTimeMillis() + (jsonResponse.getLong(EXPIRES_IN) * 1000);

                            if (!TextUtils.isEmpty(accessToken)) {
                                preferencesEditor.putString(ACCESS_TOKEN, accessToken);
                                preferencesEditor.putLong(EXPIRES_IN, timeExpire);

                                callback.onSuccess(preferencesEditor.commit());
                            }
                        } catch (JSONException e) {
                            Log.e(TAG, e.toString());
                        } catch (IOException e) {
                            Log.e(TAG, e.toString());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {

                }
            });
        }
    }

    private void revokeToken(){
        RequestBody body = new FormBody.Builder()
                .add(TOKEN, preferences.getString(ACCESS_TOKEN, ""))
                .add(TOKEN_TYPE_HINT, ACCESS_TOKEN)
                .build();

        Call<ResponseBody> call = redditApi.revokeToken(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    private boolean needsToken(){
        return (TextUtils.isEmpty(preferences.getString(ACCESS_TOKEN, "")) ||
                System.currentTimeMillis() > preferences.getLong(EXPIRES_IN, System.currentTimeMillis() - 1000));
    }

    private boolean shouldDownload(){
        Random random = new Random();
        int determinant = random.nextInt(100);
        return determinant < 25;
    }

    private boolean containsKeyword(String title, List<String> keywords){
        String[] words = title.split("\\s+");
        for(String word : words){
            for(String keyword : keywords){
                if(word.toLowerCase().contains(keyword.toLowerCase())){
                    return true;
                }
            }
        }

        return false;
    }

    private void executeThreadsRequest(String subreddit, final List<String> keywords, final NetworkResponse<List<RedditThread>> callback){
        Call<RedditResponse<RedditListing>> call = redditOauth.listThreads(subreddit);
        call.enqueue(new Callback<RedditResponse<RedditListing>>() {
            @Override
            public void onResponse(Call<RedditResponse<RedditListing>> call, Response<RedditResponse<RedditListing>> response) {
                if(response.isSuccessful()) {
                    List<RedditThread> threadsList = new ArrayList<>();
                    RedditResponse<RedditListing> listing = response.body();

                    for (RedditObject object : listing.getData().getChildren()) {
                        RedditThread thread = (RedditThread) object;

                        if (containsKeyword(thread.getTitle(), keywords) || shouldDownload()) {
                            threadsList.add(thread);
                        }
                    }

                    callback.onSuccess(threadsList);
                }
            }

            @Override
            public void onFailure(Call<RedditResponse<RedditListing>> call, Throwable t) {

            }
        });
    }

    public void downloadThreads(final String subreddit, final List<String> keywords, final NetworkResponse<List<RedditThread>> callback){
        if(!needsToken()){
            executeThreadsRequest(subreddit, keywords, callback);
        }else{
            getToken(new NetworkResponse<Boolean>() {
                @Override
                public void onSuccess(Boolean object) {
                    if(object){
                        redditOauth = RedditOAuthClient.createClass(RedditOAuthInterface.class, getAuthHeader());
                        executeThreadsRequest(subreddit, keywords, callback);
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void executeCommentsRequest(String subreddit, String threadId, final NetworkResponse<String> callback){
        Call<ResponseBody> call = redditOauth.listCommentsJson(subreddit, threadId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String body = (response != null) ? response.body().string() : "";
                    callback.onSuccess(body);
                }catch (IOException e){
                    Log.e(TAG, e.toString());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    public void downloadComments(final String subreddit, final String threadId, final NetworkResponse<String> callback){
        if(!needsToken()){
            executeCommentsRequest(subreddit, threadId, callback);
        }else{
            getToken(new NetworkResponse<Boolean>() {
                @Override
                public void onSuccess(Boolean object) {
                    if(object){
                        redditOauth = RedditOAuthClient.createClass(RedditOAuthInterface.class, getAuthHeader());
                        executeCommentsRequest(subreddit, threadId, callback);
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }

    private void executeIsValidSubredditRequest(String subreddit, final NetworkResponse<Subreddit> callback){
        Call<RedditResponse<Subreddit>> call = redditOauth.showAbout(subreddit);
        call.enqueue(new Callback<RedditResponse<Subreddit>>() {
            @Override
            public void onResponse(Call<RedditResponse<Subreddit>> call, Response<RedditResponse<Subreddit>> response) {
                RedditResponse<Subreddit> listing = response.body();
                if (listing != null) {
                    Subreddit sub = listing.getData();
                    if (sub.getDisplayName() != null) {
                        callback.onSuccess(sub);
                    } else {
                        callback.onError("Failed to add");
                    }
                } else {
                    callback.onError("Subreddit might be non-existent");
                }
            }

            @Override
            public void onFailure(Call<RedditResponse<Subreddit>> call, Throwable t) {
                callback.onError("Failed to add.");
            }
        });
    }

    public void isValidSubreddit(final String subreddit, final NetworkResponse<Subreddit> callback){
        if(!needsToken()){
            executeIsValidSubredditRequest(subreddit, callback);
        }else{
            getToken(new NetworkResponse<Boolean>() {
                @Override
                public void onSuccess(Boolean object) {
                    if(object){
                        redditOauth = RedditOAuthClient.createClass(RedditOAuthInterface.class, getAuthHeader());
                        executeIsValidSubredditRequest(subreddit, callback);
                    }
                }

                @Override
                public void onError(String message) {

                }
            });
        }
    }
}

package atamayo.offlinereader.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.RedditAPI.InvalidSubredditException;
import atamayo.offlinereader.RedditAPI.RedditDefaultClient;
import atamayo.offlinereader.RedditAPI.RedditDefaultService;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.RedditAPI.RedditOAuthClient;
import atamayo.offlinereader.RedditAPI.RedditOAuthService;
import io.reactivex.Observable;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * This class will be responsible for querying Reddit's API to fetch
 * subreddits, threads, and comments. Clients of the class must call init()
 * before being able to make requests.
 */
public class RedditDownloader {
    private final static String TAG = "REDDIT DOWNLOADER";

    public final static String CLIENT_ID = "YOUR CLIENT ID";
    public final static String CLIENT_SECRET = "YOUR CLIENT SECRET";
    public final static String DEVICE_ID = "YOUR DEVICE ID";
    public final static String CUSTOM_USER_AGENT = "YOUR USER AGENT";
    public final static String INSTALLED_CLIENT_GRANT = Html.escapeHtml("https://oauth.reddit.com/grants/installed_client");
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

    private final static String REDDIT_OAUTH_KEY = "reddit_oauth_key";

    private SharedPreferences preferences;
    private SharedPreferences.Editor preferencesEditor;
    private RedditDefaultService redditDefault;
    private RedditOAuthService redditOauth;
    private Context context;

    public RedditDownloader(Context context){
        this.context = context;
        preferences = context.getSharedPreferences(REDDIT_OAUTH_KEY, Context.MODE_PRIVATE);
        preferencesEditor = preferences.edit();
    }

    /**
     * Initializes the default Reddit service and the service that requires oauth.
     * @return true if both default and oauth service have been initialized, false otherwise
     */
    public boolean init(){
        if(redditDefault == null)
            initDefaultService();

        if(redditDefault != null && redditOauth == null)
            initOauthService();

        return (redditDefault != null && redditOauth != null);
    }

    /**
     * Initializes default service with a Basic Authorization header.
     */
    private void initDefaultService(){
        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String auth = BASIC + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        redditDefault = RedditDefaultClient.createClass(RedditDefaultService.class, auth, context);
    }

    /**
     * Initializes oauth service with an access token if a token is available,
     * otherwise it won't be initialized.
     */
    private void initOauthService(){
        if(needsToken()) {
            if(getToken()){
                redditOauth = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), context);
            }
        }else{
            redditOauth = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), context);
        }
    }

    /**
     * @return Bearer token string.
     */
    private String getAuthHeader(){
        String accessToken = preferences.getString(ACCESS_TOKEN, "");
        return BEARER + accessToken;
    }

    /**
     * Checks to see if access token is valid by checking if it's empty or if it has expired.
     * @return true if access token needs to be refreshed, false otherwise
     */
    private boolean needsToken(){
        return (TextUtils.isEmpty(preferences.getString(ACCESS_TOKEN, "")) ||
                System.currentTimeMillis() > preferences.getLong(EXPIRES_IN, System.currentTimeMillis() - 1000));
    }

    /**
     * Uses the default service to retrieve an access token.
     * @return true if access token was successfully saved, false otherwise
     */
    private boolean getToken(){
        RequestBody body = new FormBody.Builder()
                .add(QUERY_GRANT_TYPE, INSTALLED_CLIENT_GRANT)
                .add(QUERY_DEVICE_ID, DEVICE_ID)
                .build();

        if(needsToken()) {
            try {
                Call<ResponseBody> call = redditDefault.getToken(body);
                Response<ResponseBody> response = call.execute();
                if(response.isSuccessful() && response.body() != null){
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    String accessToken = jsonResponse.getString(ACCESS_TOKEN);
                    long timeExpire = System.currentTimeMillis() + (jsonResponse.getLong(EXPIRES_IN) * 1000);

                    if (!TextUtils.isEmpty(accessToken)) {
                        preferencesEditor.putString(ACCESS_TOKEN, accessToken);
                        preferencesEditor.putLong(EXPIRES_IN, timeExpire);

                        return preferencesEditor.commit();
                    }
                }
            }catch (IOException | JSONException | NullPointerException e){
                Log.e(TAG, e.toString());
                return false;
            }
        }else{
            return true;
        }

        return false;
    }

    /**
     * Revokes the currently saved access token.
     */
    private void revokeToken(){
        RequestBody body = new FormBody.Builder()
                .add(TOKEN, preferences.getString(ACCESS_TOKEN, ""))
                .add(TOKEN_TYPE_HINT, ACCESS_TOKEN)
                .build();

        try {
            Call<ResponseBody> call = redditDefault.revokeToken(body);
            call.execute();
        }catch (IOException e){
            Log.e(TAG, e.toString());
        }
    }

    /**
     * Checks to see if a string contains any word from a specified list of keywords.
     * @param title string to be checked
     * @param keywords list of words to check title against
     * @return true if title contains at least one word from the list, false otherwise
     */
    private boolean containsKeyword(String title, List<String> keywords){
        title = title.toLowerCase();
        for(String keyword : keywords){
            if(title.contains(keyword.toLowerCase()))
                return true;
        }

        return false;
    }

    /**
     * This method is responsible for executing the request to fetch comments from Reddit.
     * @param subreddit name of subreddit where thread for comments are posted
     * @param threadId id of thread where comments are posted
     * @return json string from reddit or empty string if request failed
     */
    private String executeCommentsRequest(String subreddit, String threadId){
        String comments = "";
        try{
            Call<ResponseBody> call = redditOauth.listCommentsJson(subreddit, threadId);
            Response<ResponseBody> response = call.execute();
            comments = (response != null && response.body() != null) ? response.body().string() : "";
        }catch(IOException e){
            Log.e(TAG, e.toString());
        }

        return comments;
    }

    /**
     * This method is responsible for fetching comments from Reddit. It first checks if oauth service
     * is not null and if access token is needed before proceeding to execute the request.
     * @param subreddit name of subreddit where thread for comments are posted
     * @param threadId id of thread where comments are posted
     * @return json string from reddit or empty string if request failed or service could not be initialized
     */
    public String getComments(String subreddit, String threadId) {
        if(init()){
            if(!needsToken()){
                return executeCommentsRequest(subreddit, threadId);
            }else if (getToken()){
                redditOauth = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), context);
                return executeCommentsRequest(subreddit, threadId);
            }
        }

        return "";
    }

    /**
     * This method is responsible for executing the request to fetch a list of threads from Reddit.
     * @param subreddit name of subreddit to fetch threads from
     * @param keywords list of keywords to filter threads with
     * @return list of threads that have been filtered
     */
    private Observable<List<RedditThread>> executeThreadsRequest(String subreddit, List<String> keywords){
        Observable<RedditResponse<RedditListing>> observable = redditOauth.listThreads(subreddit);
        return observable.flatMap(listing -> Observable.fromIterable(listing.getData().getChildren()))
                .filter(redditObject -> redditObject instanceof RedditThread)
                .map(redditObject -> (RedditThread) redditObject)
                .filter(thread -> keywords.isEmpty() || containsKeyword(thread.getTitle(), keywords))
                .toList()
                .toObservable();
    }

    /**
     * This method is responsible for fetching threads from Reddit. It first checks if oauth service
     * is not null and if access token is needed before proceeding to execute the request.
     * @param subreddit name of subreddit to fetch threads from
     * @param keywords list of keywords to filter threads with
     * @return list of threads that have been filtered
     */
    public Observable<List<RedditThread>> getThreads(String subreddit, List<String> keywords){
        if(init()){
            if (!needsToken()){
                return executeThreadsRequest(subreddit, keywords);
            }else if (getToken()){
                redditOauth = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), context);
                return executeThreadsRequest(subreddit, keywords);
            }
        }

        return Observable.fromIterable(new ArrayList<>());
    }

    /**
     * This method is responsible for executing the request to fetch information about a subreddit.
     * @param subreddit name of subreddit to get information for
     * @return a subreddit object
     */
    private Observable<Subreddit> executeSubredditsRequest(String subreddit){
        return redditOauth.checkSubreddit(subreddit)
                .flatMap(response -> response.getData().getDisplayName() != null
                        ? Observable.just(response.getData())
                        : Observable.error(new InvalidSubredditException()));
    }

    /**
     * This method is responsible for fetching information about a subreddit. It first checks if oauth service
     * is not null and if access token is needed before proceeding to execute the request.
     * @param subreddit name of subreddit to get information for
     * @return list of threads that have been filtered
     */
    public Observable<Subreddit> checkSubreddit(String subreddit){
        if(init()){
            if(!needsToken()){
                return executeSubredditsRequest(subreddit);
            }else if (getToken()){
                redditOauth = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), context);
                return executeSubredditsRequest(subreddit);
            }
        }

        return Observable.just(new Subreddit());
    }
}

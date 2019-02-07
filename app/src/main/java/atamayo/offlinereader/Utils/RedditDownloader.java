package atamayo.offlinereader.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import atamayo.offlinereader.RedditAPI.InvalidSubredditException;
import atamayo.offlinereader.RedditAPI.RedditDefaultClient;
import atamayo.offlinereader.RedditAPI.RedditDefaultService;
import atamayo.offlinereader.RedditAPI.RedditModel.Gif;
import atamayo.offlinereader.RedditAPI.RedditModel.Image;
import atamayo.offlinereader.RedditAPI.RedditModel.Preview;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.RedditAPI.RedditModel.Variant;
import atamayo.offlinereader.RedditAPI.RedditOAuthClient;
import atamayo.offlinereader.RedditAPI.RedditOAuthService;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * This class is responsible for querying Reddit's API to fetch
 * subreddits, threads, and comments.
 */
public class RedditDownloader {
    private final static String TAG = "REDDIT DOWNLOADER";
    private final static byte[] EMPTY_MEDIA = {};

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

    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mPreferencesEditor;
    private RedditDefaultService mRedditDefaultService;
    private RedditOAuthService mRedditOAuthService;
    private Context mContext;

    public RedditDownloader(Context context) {
        mContext = context;
        mPreferences = context.getSharedPreferences(REDDIT_OAUTH_KEY, Context.MODE_PRIVATE);
        mPreferencesEditor = mPreferences.edit();

        String credentials = CLIENT_ID + ":" + CLIENT_SECRET;
        String auth = BASIC + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        mRedditDefaultService = RedditDefaultClient.createClass(RedditDefaultService.class, auth, context);

        mRedditOAuthService = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), context);
    }

    /**
     * @return Bearer token string.
     */
    private String getAuthHeader() {
        String accessToken = mPreferences.getString(ACCESS_TOKEN, "");
        return BEARER + accessToken;
    }

    /**
     * Checks to see if access token needs to be refreshed by checking if it's empty or if it has expired.
     *
     * @return true if access token needs to be refreshed, false otherwise
     */
    private boolean needsToken() {
        return (TextUtils.isEmpty(mPreferences.getString(ACCESS_TOKEN, "")) ||
                System.currentTimeMillis() > mPreferences.getLong(EXPIRES_IN, System.currentTimeMillis() - 1000));
    }

    /**
     * Uses the default service to retrieve an access token.
     *
     * @return true if access token was successfully saved, false otherwise
     */
    private Single<Boolean> getToken() {
        RequestBody body = new FormBody.Builder()
                .add(QUERY_GRANT_TYPE, INSTALLED_CLIENT_GRANT)
                .add(QUERY_DEVICE_ID, DEVICE_ID)
                .build();

        if (needsToken()) {
            Single<ResponseBody> response = mRedditDefaultService.getToken(body);
            return response.map(responseBody -> {
                JSONObject jsonResponse = new JSONObject(responseBody.string());
                String accessToken = jsonResponse.getString(ACCESS_TOKEN);
                long timeExpire = System.currentTimeMillis() + (jsonResponse.getLong(EXPIRES_IN) * 1000);

                if (!TextUtils.isEmpty(accessToken)) {
                    mPreferencesEditor.putString(ACCESS_TOKEN, accessToken);
                    mPreferencesEditor.putLong(EXPIRES_IN, timeExpire);
                }

                return mPreferencesEditor.commit();
            });
        } else {
            return Single.just(true);
        }
    }

    /**
     * Revokes the currently saved access token.
     */
    private void revokeToken() {
        RequestBody body = new FormBody.Builder()
                .add(TOKEN, mPreferences.getString(ACCESS_TOKEN, ""))
                .add(TOKEN_TYPE_HINT, ACCESS_TOKEN)
                .build();

        Single<ResponseBody> response = mRedditDefaultService.revokeToken(body);
        response.subscribe();
    }

    /**
     * Checks to see if a string contains any word from a specified list of keywords.
     *
     * @param title string to be checked
     * @param keywords list of words to check title against
     * @return true if title contains at least one word from the list, false otherwise
     */
    private boolean containsKeyword(String title, List<String> keywords) {
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

    /**
     * This method is responsible for executing the request to fetch comments from Reddit.
     *
     * @param subreddit name of subreddit where thread is posted
     * @param threadId id of thread where comments are posted
     * @return json string from reddit or empty string if request failed
     */
    private Single<String> executeCommentsRequest(String subreddit, String threadId) {
        Single<ResponseBody> response = mRedditOAuthService.listCommentsJson(subreddit, threadId);
        return response.map(responseBody -> responseBody.string());
    }

    /**
     * Public facing method responsible for fetching comments from Reddit. It first checks if OAuth service
     * needs access token before proceeding accordingly.
     *
     * @param subreddit name of subreddit where thread is posted
     * @param threadId id of thread where comments are posted
     * @return json string from reddit or empty string if request failed or service could not be initialized
     */
    public Single<String> getComments(String subreddit, String threadId) {
        if (!needsToken()) {
            return executeCommentsRequest(subreddit, threadId);
        } else {
            return getToken()
                    .flatMap(result -> {
                        if (result) {
                            mRedditOAuthService = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), mContext);
                            return executeCommentsRequest(subreddit, threadId);
                        } else {
                            return Single.just("");
                        }
                    });
        }
    }

    /**
     * This method is responsible for executing the request to fetch a list of threads from Reddit.
     *
     * @param subreddit name of subreddit to fetch threads from
     * @param keywords list of keywords to filter threads with
     * @return list of threads that have been filtered
     */
    private Single<List<RedditThread>> executeThreadsRequest(String subreddit, List<String> keywords) {
        Single<RedditResponse<RedditListing>> response = mRedditOAuthService.listThreads(subreddit);
        return response.flatMapObservable(listing -> Observable.fromIterable(listing.getData().getChildren()))
                .filter(redditObject -> redditObject instanceof RedditThread)
                .map(redditObject -> (RedditThread) redditObject)
                .filter(thread -> keywords.isEmpty() || containsKeyword(thread.getTitle(), keywords))
                .toList();
    }

    /**
     * Public facing method responsible for fetching threads from Reddit. It first checks if OAuth service
     * needs access token before proceeding accordingly.
     *
     * @param subreddit name of subreddit to fetch threads from
     * @param keywords list of keywords to filter threads with
     * @return list of threads that have been filtered
     */
    public Single<List<RedditThread>> getThreads(String subreddit, List<String> keywords) {
        if (!needsToken()) {
            return executeThreadsRequest(subreddit, keywords);
        } else {
            return getToken()
                    .flatMap(result -> {
                        if (result) {
                            mRedditOAuthService = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), mContext);
                            return executeThreadsRequest(subreddit, keywords);
                        } else {
                            return Single.error(new IOException());
                        }
                    });
        }
    }

    /**
     * This method is responsible for executing the request to fetch information about a subreddit.
     *
     * @param subreddit name of subreddit to get information for
     * @return a subreddit object or error if subreddit does not exist
     */
    private Single<Subreddit> executeSubredditsRequest(String subreddit) {
        return mRedditOAuthService.checkSubreddit(subreddit)
                .flatMap(response -> response.getData().getDisplayName() != null
                        ? Single.just(response.getData())
                        : Single.error(new InvalidSubredditException()));
    }

    /**
     * Public facing method that is responsible for fetching information about a subreddit. It first checks if
     * OAuth service needs access token before proceeding accordingly.
     *
     * @param subreddit name of subreddit to get information for
     * @return subreddit object if subreddit exists. Error otherwise.
     */
    public Single<Subreddit> checkSubreddit(String subreddit) {
        if (!needsToken()) {
            return executeSubredditsRequest(subreddit);
        } else {
            return getToken()
                    .flatMap(result -> {
                        if (result) {
                            mRedditOAuthService = RedditOAuthClient.createClass(RedditOAuthService.class, getAuthHeader(), mContext);
                            return executeSubredditsRequest(subreddit);
                        } else {
                            return Single.error(new IOException());
                        }
                    });
        }
    }

    /**
     * Downloads the image from a given url.
     *
     * @param mediaUrl url to download image from
     * @param targetWidth desired width of the image
     * @param targetHeight desired height of the image
     * @return byte array with contents of the image
     */
    private Single<byte[]> downloadImage(String mediaUrl, int targetWidth, int targetHeight){
        GlideUrl url = new GlideUrl(mediaUrl,
                new LazyHeaders.Builder()
                        .addHeader(AUTHORIZATION, getAuthHeader())
                        .addHeader(USER_AGENT, CUSTOM_USER_AGENT)
                        .build());
        try {
            return Single.just(Glide.with(mContext).load(url)
                    .asBitmap()
                    .toBytes()
                    .into(targetWidth, targetHeight)
                    .get());
        }catch (Exception e){
            Log.d(TAG, e.toString());
            return Single.just(EMPTY_MEDIA);
        }
    }

    /**
     * Public facing method for downloading an image from reddit.
     *
     * @param thread Reddit thread to download image from
     * @param targetWidth desired width of the image
     * @param targetHeight desired height of the image
     * @return byte array with contents of the image
     */
    public Single<byte[]> downloadImage(RedditThread thread, int targetWidth, int targetHeight){
        Preview preview = thread.getPreview();

        if(preview != null) {
            Image image = preview.getImages().get(0);
            Variant variant = image.getVariants();
            Gif gif = variant.getGif();

            if (gif == null) {
                if (!needsToken()) {
                    return downloadImage(image.getSource().getUrl(), targetWidth, targetHeight);
                } else {
                    return getToken().flatMap(result -> {
                        if (result)
                            return downloadImage(image.getSource().getUrl(), targetWidth, targetHeight);
                        else
                            return Single.just(EMPTY_MEDIA);
                    });
                }
            }
        }

        return Single.just(EMPTY_MEDIA);
    }
}

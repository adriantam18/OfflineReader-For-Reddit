package atamayo.offlinereddit.Utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import atamayo.offlinereddit.RedditAPI.AboutSubredditResponse;
import atamayo.offlinereddit.RedditAPI.RedditApiClient;
import atamayo.offlinereddit.RedditAPI.RedditApiInterface;
import atamayo.offlinereddit.RedditAPI.RedditChildData;
import atamayo.offlinereddit.RedditAPI.RedditMobileApiClient;
import atamayo.offlinereddit.RedditAPI.RedditResponse;
import atamayo.offlinereddit.RedditAPI.RedditResponseData;
import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditAPI.Subreddit;
import okhttp3.HttpUrl;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedditDownloader {
    private final static String TAG = "REDDIT DOWNLOADER";
    private static RedditDownloader mInstance;
    private static RedditApiInterface redditApi;
    private static RedditApiInterface redditMobileApi;

    private RedditDownloader(){
        redditApi = RedditApiClient.createClass(RedditApiInterface.class);
        redditMobileApi = RedditMobileApiClient.createClass(RedditApiInterface.class);
    }

    public static RedditDownloader getInstance(){
        if(mInstance == null){
            mInstance = new RedditDownloader();
        }

        return mInstance;
    }

    public List<RedditThread> startDownload(String subreddit, List<String> keywords){
        List<RedditChildData> children = downloadThreads(subreddit);
        return filter(children, keywords);
    }

    public String downloadThreadComments(String permalink){
        try {
            Call<ResponseBody> call = redditMobileApi.listComments(permalink);
            HttpUrl url = call.request().url();
            Response<ResponseBody> response = call.execute();
            if(response.isSuccessful()) {
                ResponseBody responseBody = response.body();
                InputStream inputStream = responseBody.byteStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                StringBuilder builder = new StringBuilder();
                String contents;
                while ((contents = reader.readLine()) != null) {
                    builder.append(contents);
                }
                reader.close();

                return builder.toString();
            }else {
                return "";
            }
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return "";
        }
    }

    private static List<RedditChildData> downloadThreads(String subreddit){
        List<RedditChildData> children = new ArrayList<>();
        try {
            Call<RedditResponse> call = redditApi.listThreads(subreddit);
            Response<RedditResponse> response = call.execute();
            if(response != null) {
                RedditResponse responseBody = response.body();
                RedditResponseData responseData = responseBody.getData();
                children = responseData.getChildren();
            }
        }catch (IOException e){
            Log.e(TAG, e.toString());
        }

        return children;
    }

    private static List<RedditThread> filter(List<RedditChildData> children, List<String> keywords){
        List<RedditThread> threadData = new ArrayList<>();
        for(RedditChildData child : children){
            RedditThread thread = child.getData();
            if(!keywords.isEmpty()) {
                if (containsKeyword(thread.getTitle(), keywords)) {
                    threadData.add(thread);
                }
            }else {
                if(shouldDownload()){
                    threadData.add(thread);
                }
            }
        }

        return threadData;
    }

    private static boolean containsKeyword(String title, List<String> keywords){
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

    private static boolean shouldDownload(){
        Random random = new Random();
        int determinant = random.nextInt(100);
        return determinant < 25;
    }

    public void isValidSubreddit(final String subreddit, final NetworkResponse responseCallback){
        try {
            Call<AboutSubredditResponse> call = redditApi.showAbout(subreddit);
            call.enqueue(new Callback<AboutSubredditResponse>() {
                @Override
                public void onResponse(Call<AboutSubredditResponse> call, Response<AboutSubredditResponse> response) {
                    if(response.isSuccessful()) {
                        AboutSubredditResponse subredditResponse = response.body();
                        Subreddit sub = subredditResponse.getData();
                        if (sub.getDisplayName() != null) {
                            responseCallback.onSuccess(sub);
                        } else {
                            responseCallback.onError("Subreddit does not exist or does not have any content");
                        }
                    } else {
                        responseCallback.onError("Subreddit cannot be found");
                    }
                }

                @Override
                public void onFailure(Call<AboutSubredditResponse> call, Throwable t) {
                    responseCallback.onError("Failed to download subreddit");
                }
            });
        }catch (Exception e){
            Log.e(TAG, e.toString());
            responseCallback.onError("Couldn't process request");
        }
    }
}

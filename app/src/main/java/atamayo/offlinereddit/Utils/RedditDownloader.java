package atamayo.offlinereddit.Utils;

import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import atamayo.offlinereddit.RedditAPI.RedditApiClient;
import atamayo.offlinereddit.RedditAPI.RedditApiInterface;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditObject;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditResponse;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditListing;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RedditDownloader {
    private final static String TAG = "REDDIT DOWNLOADER";
    private static RedditDownloader mInstance;
    private static RedditApiInterface redditApi;

    private RedditDownloader(){
        redditApi = RedditApiClient.createClass(RedditApiInterface.class);
    }

    public static RedditDownloader getInstance(){
        if(mInstance == null){
            mInstance = new RedditDownloader();
        }

        return mInstance;
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

    public List<RedditThread> downloadThreads(String subreddit, List<String> keywords){
        List<RedditThread> threadsList = new ArrayList<>();
        try {
            Call<RedditResponse<RedditListing>> call = redditApi.listThreads(subreddit);
            Response<RedditResponse<RedditListing>> response = call.execute();
            RedditResponse<RedditListing> listing = response.body();

            for (RedditObject object : listing.getData().getChildren()){
                RedditThread thread = (RedditThread) object;

                if(containsKeyword(thread.getTitle(), keywords)){
                    threadsList.add(thread);
                }else if(shouldDownload()){
                    threadsList.add(thread);
                }
            }
        }catch (IOException e){
            Log.e(TAG, e.toString());
        }catch (ClassCastException e){
            Log.e(TAG, e.toString());
        }

        return threadsList;
    }

    public String downloadComments(String subreddit, String threadId){
        try {
            Call<ResponseBody> call = redditApi.listCommentsJson(subreddit, threadId);
            Response<ResponseBody> response = call.execute();

            return response != null ? response.body().string() : "";
        }catch (IOException e){
            Log.e(TAG, e.toString());
            return "";
        }catch (NullPointerException e){
            Log.e(TAG, e.toString());
            return "";
        }
    }

    public void isValidSubreddit(final String subreddit, final NetworkResponse responseCallback){
        try {
            Call<RedditResponse<Subreddit>> call = redditApi.showAbout(subreddit);
            call.enqueue(new Callback<RedditResponse<Subreddit>>() {
                @Override
                public void onResponse(Call<RedditResponse<Subreddit>> call, Response<RedditResponse<Subreddit>> response) {
                    RedditResponse<Subreddit> listing = response.body();
                    if(listing != null) {
                        Subreddit sub = listing.getData();
                        if (sub.getDisplayName() != null) {
                            responseCallback.onSuccess(sub);
                        } else {
                            responseCallback.onError("Failed to add");
                        }
                    }else {
                        responseCallback.onError("Subreddit might be non-existent");
                    }
                }

                @Override
                public void onFailure(Call<RedditResponse<Subreddit>> call, Throwable t) {
                    responseCallback.onError("Failed to add.");
                }
            });
        }catch (NullPointerException e){
            Log.e(TAG, e.toString());
            responseCallback.onError("Couldn't process request");
        }
    }
}

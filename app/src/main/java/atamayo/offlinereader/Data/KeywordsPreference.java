package atamayo.offlinereader.Data;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of KeywordsDataSource interface that uses Android's SharedPreferences
 * to save keywords.
 */
public class KeywordsPreference implements KeywordsDataSource {
    private static final String PREFS_NAME = "subreddits";
    private static final String KEYWORD_SUFFIX = "_keywords";
    private SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;

    public KeywordsPreference(Context context) {
        mPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mPreferences.edit();
    }

    @Override
    public List<String> getKeywords(String subreddit) {
        String subredditKey = getKeywordsKey(subreddit);
        Gson gson = new Gson();
        List<String> keywords = new ArrayList<>();

        if (mPreferences.contains(subredditKey)) {
            String json = mPreferences.getString(subredditKey, "");
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            keywords = gson.fromJson(json, listType);
        }

        return keywords;
    }

    @Override
    public boolean addKeyword(String subreddit, String keyword) {
        String subredditKey = getKeywordsKey(subreddit);
        Gson gson = new Gson();
        Set<String> keywords = new LinkedHashSet<>(getKeywords(subreddit));

        if (keywords.add(keyword)) {
            String json = gson.toJson(keywords);
            mEditor.putString(subredditKey, json);
            return mEditor.commit();
        } else {
            return false;
        }
    }

    @Override
    public void deleteKeyword(String subreddit, String keyword) {
        String subredditKey = getKeywordsKey(subreddit);
        Gson gson = new Gson();
        Set<String> keywords = new LinkedHashSet<>(getKeywords(subreddit));

        if (keywords.remove(keyword)) {
            String json = gson.toJson(keywords);
            mEditor.putString(subredditKey, json);
            mEditor.commit();
        }
    }

    @Override
    public void clearKeywords(String subreddit) {
        String subredditKey = getKeywordsKey(subreddit);
        mEditor.remove(subredditKey);
        mEditor.commit();
    }

    private String getKeywordsKey(String subreddit) {
        return subreddit + KEYWORD_SUFFIX;
    }
}

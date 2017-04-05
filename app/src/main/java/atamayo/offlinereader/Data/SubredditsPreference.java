package atamayo.offlinereader.Data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubredditsPreference implements KeywordsDataSource {
    private static final String PREFS_NAME = "subreddits";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    public SubredditsPreference(Context context){
        super();
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    @Override
    public List<String> getKeywords(String subKeywords){
        List<String> keywords = new ArrayList<>();
        Set<String> keywordsSet = new HashSet<>();

        if(preferences.contains(subKeywords)){
            keywordsSet = preferences.getStringSet(subKeywords, null);
        }

        if(keywordsSet != null){
            keywords.addAll(keywordsSet);
        }

        return keywords;
    }

    @Override
    public void clearKeywords(String subreddit){
        editor.remove(subreddit);
        editor.apply();
    }

    @Override
    public void updateKeywords(String subreddit, List<String> keywords){
        Set<String> keywordsSet = new HashSet<>(keywords);
        editor.putStringSet(subreddit, keywordsSet);
        editor.apply();
    }
}

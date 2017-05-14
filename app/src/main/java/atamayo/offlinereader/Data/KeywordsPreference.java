package atamayo.offlinereader.Data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation of KeywordsDataSource interface that uses Android's SharedPreferences
 * to save keywords.
 */
public class KeywordsPreference implements KeywordsDataSource {
    private static final String PREFS_NAME = "subreddits";
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Set<String> mCurrkeywords;

    public KeywordsPreference(Context context){
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        mCurrkeywords = new HashSet<>();
    }

    @Override
    public Set<String> getKeywords(String subName){
        if(preferences.contains(subName)){
            mCurrkeywords = preferences.getStringSet(subName, new HashSet<>());
        }

        return mCurrkeywords;
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

package atamayo.offlinereader.RedditAPI;

import com.google.gson.JsonElement;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditType;

/**
 * Credits: https://github.com/jacobtabak/droidcon
 */

public class RedditObjectWrapper {
    RedditType kind;
    JsonElement data;

    public RedditType getKind(){
        return kind;
    }

    public JsonElement getJsonData(){
        return data;
    }
}

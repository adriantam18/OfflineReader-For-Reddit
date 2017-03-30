package atamayo.offlinereddit.RedditAPI;

import com.google.gson.JsonElement;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditType;

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

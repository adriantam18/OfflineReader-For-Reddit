package atamayo.offlinereddit.RedditAPI;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditObject;

/**
 * Credits: https://github.com/jacobtabak/droidcon
 */
public class RedditObjectDeserializer implements JsonDeserializer<RedditObject> {
    public static final String KIND = "kind";

    @Override
    public RedditObject deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.isJsonObject()){
            return null;
        }

        try{
            RedditObjectWrapper wrapper = new Gson().fromJson(json, RedditObjectWrapper.class);
            return context.deserialize(wrapper.getJsonData(), wrapper.getKind().getDerivedClass());
        }catch (JsonParseException e){
            Log.e("RedditDeserializer", e.toString());
            return null;
        }catch (NullPointerException e){
            Log.e("RedditDeserializer", e.toString());
            return null;
        }
    }
}

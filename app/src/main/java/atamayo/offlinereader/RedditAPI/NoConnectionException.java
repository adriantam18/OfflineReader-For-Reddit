package atamayo.offlinereader.RedditAPI;

import java.io.IOException;

public class NoConnectionException extends IOException {
    @Override
    public String getMessage(){
        return "No internet connection";
    }
}

package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

public class Variant {

    @Expose
    Gif gif;

    public Gif getGif() {
        return gif;
    }

    public void setGif(Gif gif) {
        this.gif = gif;
    }
}

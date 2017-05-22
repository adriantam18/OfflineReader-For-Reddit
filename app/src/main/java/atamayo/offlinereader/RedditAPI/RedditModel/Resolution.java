package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

public class Resolution {

    @Expose
    String url;

    @Expose
    int width;


    @Expose
    int height;


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}

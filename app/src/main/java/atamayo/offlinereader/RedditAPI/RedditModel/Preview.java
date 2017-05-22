package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Preview {

    @Expose
    List<Image> images;

    @Expose
    boolean enabled;

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}

package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Image {

    @Expose
    Resolution source;

    @Expose
    List<Resolution> resolutions;

    @Expose
    Variant variants;

    @Expose
    String id;

    public Resolution getSource() {
        return source;
    }

    public void setSource(Resolution source) {
        this.source = source;
    }

    public List<Resolution> getResolutions() {
        return resolutions;
    }

    public void setResolutions(List<Resolution> resolutions) {
        this.resolutions = resolutions;
    }

    public Variant getVariants() {
        return variants;
    }

    public void setVariants(Variant variants) {
        this.variants = variants;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

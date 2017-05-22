package atamayo.offlinereader.RedditAPI.RedditModel;

import com.google.gson.annotations.Expose;

import java.util.List;

public class Gif {

    @Expose
    Resolution source;

    @Expose
    List<Resolution> resolutions;

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
}

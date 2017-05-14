package atamayo.offlinereader.Data;

import java.util.List;
import java.util.Set;

public interface KeywordsDataSource {
    Set<String> getKeywords(String subreddit);
    void updateKeywords(String subreddit, List<String> keywords);
    void clearKeywords(String subreddit);
}

package atamayo.offlinereddit.Data;

import java.util.List;

public interface KeywordsDataSource {
    List<String> getKeywords(String subreddit);
    void updateKeywords(String subreddit, List<String> keywords);
    void clearKeywords(String subreddit);
}

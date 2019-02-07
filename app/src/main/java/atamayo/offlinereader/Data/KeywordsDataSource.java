package atamayo.offlinereader.Data;

import java.util.List;

public interface KeywordsDataSource {
    boolean addKeyword(String subreddit, String keyword);

    List<String> getKeywords(String subreddit);

    void deleteKeyword(String subreddit, String keyword);
    void clearKeywords(String subreddit);
    void clearAllKeywords();
}

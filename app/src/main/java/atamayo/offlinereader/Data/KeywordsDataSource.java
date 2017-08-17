package atamayo.offlinereader.Data;

import java.util.List;

public interface KeywordsDataSource {
    List<String> getKeywords(String subreddit);
    boolean addKeyword(String subreddit, String keyword);
    void deleteKeyword(String subreddit, String keyword);
    void clearKeywords(String subreddit);
}

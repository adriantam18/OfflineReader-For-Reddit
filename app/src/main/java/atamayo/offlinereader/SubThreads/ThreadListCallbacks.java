package atamayo.offlinereader.SubThreads;

import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;

public interface ThreadListCallbacks {
    void OnOpenCommentsPage(RedditThread thread);
    void OnDeleteThread(RedditThread thread);
}

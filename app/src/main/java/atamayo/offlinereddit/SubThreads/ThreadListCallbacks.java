package atamayo.offlinereddit.SubThreads;

import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;

public interface ThreadListCallbacks {
    void OnOpenCommentsPage(RedditThread thread);
    void OnDeleteThread(RedditThread thread);
}

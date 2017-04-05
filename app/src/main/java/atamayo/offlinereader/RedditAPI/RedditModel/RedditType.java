package atamayo.offlinereader.RedditAPI.RedditModel;

public enum RedditType {
    Listing(RedditListing.class),
    more(RedditMore.class),
    t1(RedditComment.class),
    t3(RedditThread.class),
    t5(Subreddit.class);

    private final Class cls;

    RedditType(Class cls){
        this.cls = cls;
    }

    public Class getDerivedClass(){
        return this.cls;
    }
}

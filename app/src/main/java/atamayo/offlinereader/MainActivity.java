package atamayo.offlinereader;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import atamayo.offlinereader.Keywords.KeywordsListing;
import atamayo.offlinereader.SubThreads.SubThreadsListing;
import atamayo.offlinereader.Subreddits.SubredditsListing;
import atamayo.offlinereader.ThreadComments.ThreadCommentsListing;

public class MainActivity extends AppCompatActivity
    implements SubredditsListing.OnSubredditSelectedListener,
        SubThreadsListing.OnThreadSelectedListener{
    public static final String FRAGMENT_EXTRA = "extra_fragment";
    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);

        fragmentManager = getSupportFragmentManager();

        SubredditsListing subredditsListing = (SubredditsListing) getSupportFragmentManager()
                .findFragmentByTag(SubredditsListing.TAG);

        if(subredditsListing == null) {
            subredditsListing = new SubredditsListing();

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.fragment_container, subredditsListing, SubredditsListing.TAG)
                    .commit();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        return false;
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        if(intent.hasExtra(FRAGMENT_EXTRA)){
            String tag = intent.getStringExtra(FRAGMENT_EXTRA);
            Fragment fragment = fragmentManager.findFragmentByTag(tag);

            if(fragment != null && !fragment.isVisible()){
                int numFragments = fragmentManager.getBackStackEntryCount();
                for(int i = 0; i < numFragments; i++){
                    fragmentManager.popBackStack();
                }

                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.fragment_container, fragment, fragment.getTag())
                        .commit();
            }
        }
    }

    @Override
    public void launchThreadsListing(Bundle args) {
        SubThreadsListing threadsListing = new SubThreadsListing();
        threadsListing.setArguments(args);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, threadsListing, SubThreadsListing.TAG)
                .addToBackStack(null).commit();
    }

    @Override
    public void launchKeywordsListing(Bundle args) {
        Intent intent = new Intent(this, KeywordsListing.class);
        intent.putExtras(args);
        startActivity(intent);
    }

    @Override
    public void launchCommentsPage(Bundle args) {
        ThreadCommentsListing commentsListing = new ThreadCommentsListing();
        commentsListing.setArguments(args);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, commentsListing, ThreadCommentsListing.TAG)
                .addToBackStack(null).commit();
    }
}

package atamayo.offlinereddit;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import atamayo.offlinereddit.Keywords.KeywordsListing;
import atamayo.offlinereddit.SubThreads.SubThreadsListing;
import atamayo.offlinereddit.Subreddits.SubredditsListing;
import atamayo.offlinereddit.ThreadComments.ThreadComments;

public class MainActivity extends AppCompatActivity
    implements SubredditsListing.OnSubredditSelectedListener,
        SubThreadsListing.OnThreadSelectedListener{
    private FragmentManager fragmentManager;
    public static final String EXTRA_SUBREDDIT = "subreddit";
    public static final String EXTRA_THREAD = "thread";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);

        fragmentManager = getFragmentManager();

        if(savedInstanceState != null){
            return;
        }

        Fragment fragment = new SubredditsListing();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.fragment_container, fragment, "SubredditsListing")
                .commit();
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
        if(intent.hasExtra("fragment")){
            String tag = intent.getStringExtra("fragment");
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
        transaction.replace(R.id.fragment_container, threadsListing, null)
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
        Fragment fragment = new ThreadComments();
        fragment.setArguments(args);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment, "ThreadComments")
                .addToBackStack(null).commit();
    }
}

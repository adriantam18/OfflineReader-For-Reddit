package atamayo.offlinereddit;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import atamayo.offlinereddit.Keywords.KeywordsListing;
import atamayo.offlinereddit.SubThreads.SubThreadsListing;
import atamayo.offlinereddit.Subreddits.SubredditsListing;
import atamayo.offlinereddit.ThreadComments.ThreadCommentsListing;

public class MainActivity extends AppCompatActivity
    implements SubredditsListing.OnSubredditSelectedListener,
        SubThreadsListing.OnThreadSelectedListener{
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
    public void onBackPressed(){
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if(count > 0){
            getSupportFragmentManager().popBackStack();
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

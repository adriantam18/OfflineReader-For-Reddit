package atamayo.offlinereader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.navigation.Navigation;

public class MainActivity extends AppCompatActivity {
    public static final String FRAGMENT_EXTRA = "extra_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddits);
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
        if (intent.hasExtra(FRAGMENT_EXTRA)) {
            Navigation.findNavController(this, R.id.nav_fragment).popBackStack(R.id.subreddits_dest, false);
        }
    }
}

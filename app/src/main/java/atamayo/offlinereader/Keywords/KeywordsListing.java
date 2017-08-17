package atamayo.offlinereader.Keywords;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.R;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Displays the list of keywords for a subreddit. It allows users to add
 * and remove keywords.
 */
public class KeywordsListing extends AppCompatActivity implements KeywordsContract.View,
        KeywordsListCallback, ConfirmDialogListener {
    public static final String TAG = "KeywordsListing";
    public static final String SUBREDDIT = "subreddit";
    private static final String LIST_STATE = "List_State";
    private KeywordsPresenter mPresenter;
    private KeywordsAdapter mAdapter;
    private Parcelable mListState;

    @BindView(R.id.keywords_list)
    RecyclerView mKeywordsRecyclerView;
    @BindView(R.id.enter_keyword)
    EditText mUserInput;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitle;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_keywords);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        String subreddit = args.getString(SUBREDDIT) != null
                ? args.getString(SUBREDDIT) : "";

        mAdapter = new KeywordsAdapter(new ArrayList<>(), this);
        mKeywordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mKeywordsRecyclerView.setAdapter(mAdapter);

        KeywordsDataSource dataSource = new KeywordsPreference(this);
        mPresenter = new KeywordsPresenter(subreddit, dataSource, new AppScheduler());

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText(subreddit);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.attachView(this);
        mPresenter.getKeywords();
    }

    @Override
    public void onPause() {
        super.onPause();
        mListState = mKeywordsRecyclerView.getLayoutManager().onSaveInstanceState();
        mPresenter.detachView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keywords_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mPresenter.clearKeywords();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_STATE, mListState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mListState = savedInstanceState.getParcelable(LIST_STATE);
    }

    @Override
    public void showKeywordsList(List<String> keywords) {
        mAdapter.replaceData(keywords);
        mUserInput.setText("");

        if (mListState != null) {
            mKeywordsRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
            mListState = null;
        }
    }

    @Override
    public void showMessage(String title, String message) {
        showDialog(title, message);
    }

    @Override
    public void OnDeleteKeyword(String keyword) {
        mPresenter.removeKeyword(keyword);
    }

    @Override
    public void onConfirmClick(String action) {

    }

    @OnClick(R.id.btn_add_keyword)
    public void onAddKeywordClicked(View view) {
        String keyword = mUserInput.getText().toString();
        if (!TextUtils.isEmpty(keyword)) {
            mPresenter.addKeyword(keyword);
        } else {
            mUserInput.setError("Please enter a keyword");
        }
    }

    private void showDialog(String title, String message) {
        DialogFragment dialog = ConfirmDialog.newInstance(title, message, "");
        dialog.show(getSupportFragmentManager(), "Dialog");
    }
}

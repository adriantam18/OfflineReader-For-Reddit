package atamayo.offlinereader.Keywords;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeywordsListing extends AppCompatActivity implements KeywordsContract.View,
        KeywordsListCallback, ConfirmDialogListener{
    public static final String TAG = "KeywordsListing";
    public static final String SUBREDDIT = "subreddit";
    private KeywordsContract.Presenter mPresenter;
    private KeywordsAdapter mAdapter;

    @BindView(R.id.keywords_list) RecyclerView mKeywordsRecyclerView;
    @BindView(R.id.enter_keyword) EditText mUserInput;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;

    @OnClick(R.id.btn_add_keyword)
    public void onAddKeywordClicked(View view){
        String keyword = mUserInput.getText().toString();
        if(!keyword.isEmpty()){
            mPresenter.addKeyword(keyword);
        }else {
            mUserInput.setError("Please enter a keyword");
        }
    }

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_keywords);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        String subreddit = args.getString(SUBREDDIT) != null
                ? args.getString(SUBREDDIT) : "";


        mAdapter = new KeywordsAdapter(new ArrayList<String>(), this);
        mKeywordsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mKeywordsRecyclerView.setAdapter(mAdapter);

        KeywordsDataSource dataSource = new KeywordsPreference(this);
        mPresenter = new KeywordsPresenter(dataSource, this);
        mPresenter.initKeywordsList(subreddit);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText(subreddit);
    }

    @Override
    public void onPause(){
        super.onPause();
        mPresenter.persistKeywords();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.keywords_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_delete:
                mPresenter.clearKeywords();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public void showKeywordsList(List<String> keywords) {
        mAdapter.replaceData(keywords);
        mUserInput.setText("");
    }

    @Override
    public void showMessage(String title, String message){
        showDialog(title, message);
    }

    @Override
    public void setPresenter(KeywordsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void OnDeleteKeyword(String keyword){
        mPresenter.removeKeyword(keyword);
    }

    @Override
    public void onConfirmClick(String action){

    }

    private void showDialog(String title, String message){
        DialogFragment dialog = ConfirmDialog.newInstance(title, message, "");
        dialog.show(getSupportFragmentManager(), "Dialog");
    }
}

package atamayo.offlinereddit.Keywords;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereddit.Data.KeywordsDataSource;
import atamayo.offlinereddit.Data.SubredditsPreference;
import atamayo.offlinereddit.MainActivity;
import atamayo.offlinereddit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class KeywordsListing extends AppCompatActivity implements KeywordsContract.View, KeywordsListCallback{
    public static final String TAG = "KeywordsListing";
    public static final String SUBREDDIT = "subreddit";
    private KeywordsContract.Presenter mPresenter;
    private KeywordsAdapter mAdapter;

    @BindView(R.id.keywords_list) RecyclerView mKeywordsRecyclerView;
    @BindView(R.id.enter_keyword) AutoCompleteTextView mUserInput;
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

        KeywordsDataSource dataSource = new SubredditsPreference(this);
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
    }

    @Override
    public void updateKeywordsList() {
        mAdapter.notifyDataSetChanged();
        mUserInput.setText("");
    }

    @Override
    public void setPresenter(KeywordsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void OnDeleteKeyword(int position){
        mPresenter.removeKeyword(position);
    }
}

package atamayo.offlinereddit.Subreddits;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereddit.App;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.MainActivity;
import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.Subreddit;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import atamayo.offlinereddit.SubredditService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SubredditsListing extends Fragment
        implements SubredditsContract.View, SubListCallbacks {

    private final static String TAG = "SubredditsListing";
    private Unbinder unbinder;
    private SubredditsAdapter mAdapter;
    private OnSubredditSelectedListener mCallback;
    private SubredditsContract.Presenter mPresenter;
    @BindView(R.id.enter_item) AutoCompleteTextView mEnterItem;
    @BindView(R.id.subs_list) RecyclerView mSubsRecyclerView;
    @BindView(R.id.show_loading) ProgressBar mProgessBar;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;

    public interface OnSubredditSelectedListener{
        void launchThreadsListing(Bundle args);
        void launchKeywordsListing(Bundle args);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnSubredditSelectedListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        DaoSession daoSession = ((App) (getActivity().getApplication())).getDaoSession();
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao());
        mPresenter = new SubredditsPresenter(repository, this);
        mAdapter = new SubredditsAdapter(new ArrayList<Subreddit>(0), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_subreddits, container, false);

        unbinder = ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText("Subreddits");

        mSubsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubsRecyclerView.setAdapter(mAdapter);

        mPresenter.initSubredditsList();

        return view;
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        RefWatcher refWatcher = App.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.subreddits_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_download:
                mPresenter.downloadThreads();
                return true;
            case R.id.action_delete:
                //TODO: Delete subreddits
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void setPresenter(SubredditsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void showInitialSubreddits(List<Subreddit> subreddits) {
        mAdapter.replaceData(subreddits);
    }

    @Override
    public void showAddedSubreddit(int position) {
        mAdapter.notifyItemInserted(position);
        mSubsRecyclerView.smoothScrollToPosition(position);
        mEnterItem.setText("");
    }

    @Override
    public void showRemovedSubreddits(int start, int itemCount) {
        //mAdapter.notifyItemRangeRemoved(start, itemCount);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showClearedSubreddits() {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showSubredditThreads(String subreddit) {
        Bundle args = new Bundle();
        args.putString(MainActivity.EXTRA_SUBREDDIT, subreddit);

        mCallback.launchThreadsListing(args);
    }

    @Override
    public void showKeywords(String subreddit){
        Bundle args = new Bundle();
        args.putString(MainActivity.EXTRA_SUBREDDIT, subreddit);

        mCallback.launchKeywordsListing(args);
    }

    @Override
    public void showLoading(boolean isLoading) {
        if(isLoading){
            mProgessBar.setVisibility(View.VISIBLE);
        }else {
            mProgessBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void showError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void startDownloadService(List<String> subreddits){
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra("subreddits", (ArrayList<String>) subreddits);
        getActivity().startService(intent);
    }

    @Override
    public void OnOpenListOfThreads(int position) {
        mPresenter.openListOfThreads(position);
    }

    @Override
    public void OnOpenListOfKeywords(int position) {
        mPresenter.openListOfKeywords(position);
    }

    @Override
    public void OnDeleteSubreddit(int position) {
        mPresenter.removeSubreddit(position);
    }

    @OnClick(R.id.btn_add_subreddit)
    public void onAddButtonClicked(View view){
        String subreddit = mEnterItem.getText().toString();
        if(!subreddit.isEmpty()){
            mPresenter.addSubreddit(subreddit);
        }else {
            mEnterItem.setError("Enter a subreddit to add");
        }
    }
}


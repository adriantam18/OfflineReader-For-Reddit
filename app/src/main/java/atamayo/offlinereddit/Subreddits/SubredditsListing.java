package atamayo.offlinereddit.Subreddits;

import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereddit.App;
import atamayo.offlinereddit.ConfirmDialog;
import atamayo.offlinereddit.ConfirmDialogListener;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.Keywords.KeywordsListing;
import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import atamayo.offlinereddit.SubThreads.SubThreadsListing;
import atamayo.offlinereddit.SubredditService;
import atamayo.offlinereddit.Data.CommentFileManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SubredditsListing extends Fragment
        implements SubredditsContract.View, SubListCallbacks, ConfirmDialogListener {

    public static final String TAG = "SubredditsListing";
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
        CommentFileManager commentFileManager = new CommentFileManager(getActivity());
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao(), commentFileManager);
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
        menu.clear();
        inflater.inflate(R.menu.subreddits_menu, menu);

        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setBackgroundColor(Color.WHITE);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_download:
                startDownloadService();
                return true;
            case R.id.action_delete:
                mPresenter.clearSubreddits();
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
    public void showSubreddits(List<Subreddit> subreddits) {
        mAdapter.replaceData(subreddits);
    }

    @Override
    public void showAddedSubreddit(Subreddit subreddit) {
        mSubsRecyclerView.smoothScrollToPosition(mAdapter.addData(subreddit));
        mEnterItem.setText("");
    }

    @Override
    public void showClearedSubreddits() {
        mAdapter.clearData();
    }

    @Override
    public void showSubredditThreads(String subredditName) {
        Bundle args = new Bundle();
        args.putString(SubThreadsListing.SUBREDDIT, subredditName);

        mCallback.launchThreadsListing(args);
    }

    @Override
    public void showSubredditKeywords(String subredditName) {
        Bundle args = new Bundle();
        args.putString(KeywordsListing.SUBREDDIT, subredditName);

        mCallback.launchKeywordsListing(args);
    }

    @Override
    public void showError(String message) {
        showConfirmDialog("ERROR", message, "");
    }

    @Override
    public void OnOpenListOfThreads(Subreddit subreddit) {
        mPresenter.openSubredditThreads(subreddit);
    }

    @Override
    public void OnOpenListOfKeywords(Subreddit subreddit) {
        mPresenter.openSubredditKeywords(subreddit);
    }

    @Override
    public void OnDeleteSubreddit(Subreddit subreddit) {
        mPresenter.removeSubreddit(subreddit);
    }

    @OnClick(R.id.btn_add_subreddit)
    public void onAddButtonClicked(View view){
        String subreddit = mEnterItem.getText().toString();
        if(!subreddit.isEmpty()){
            mPresenter.addIfExists(subreddit);
        }else {
            mEnterItem.setError("Enter a subreddit to add");
        }
    }

    @Override
    public void onConfirmClick(String action){

    }

    private void startDownloadService(){
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra("subreddits", (ArrayList<String>) mAdapter.getSubsToDownload());
        getActivity().startService(intent);
    }

    private void showConfirmDialog(String title, String message, String action){
        ConfirmDialog dialog = ConfirmDialog.newInstance(title, message, action);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "DIALOG");
    }
}


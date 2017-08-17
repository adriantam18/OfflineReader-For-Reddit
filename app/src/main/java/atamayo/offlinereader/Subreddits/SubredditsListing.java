package atamayo.offlinereader.Subreddits;

import android.graphics.Color;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.App;
import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.KeywordsDataSource;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.Keywords.KeywordsListing;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.SubThreads.SubThreadsListing;
import atamayo.offlinereader.SubredditService;
import atamayo.offlinereader.Data.FileManager;
import atamayo.offlinereader.Utils.RedditDownloader;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Displays a list of subreddits. It allows users to add and remove subreddits.
 * Clicking on a subreddit will take the user to a list of threads for that subreddit.
 */
public class SubredditsListing extends Fragment
        implements SubredditsContract.View, SubListCallbacks, ConfirmDialogListener {

    public static final String TAG = "SubredditsListing";
    private static final String LIST_STATE = "CurrListState";
    private Unbinder unbinder;
    private SubredditsAdapter mAdapter;
    private OnSubredditSelectedListener mCallback;
    private SubredditsPresenter mPresenter;
    private Parcelable mListState;

    @BindView(R.id.enter_item)
    EditText mEnterItem;
    @BindView(R.id.subs_list)
    RecyclerView mSubsRecyclerView;
    @BindView(R.id.show_loading)
    ProgressBar mProgessBar;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.btn_add_subreddit)
    ImageButton mBtnAdd;

    public interface OnSubredditSelectedListener {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        DaoSession daoSession = ((App) (getActivity().getApplication())).getDaoSession();
        FileManager commentFileManager = new FileManager(getActivity());
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao(), commentFileManager);
        RedditDownloader downloader = new RedditDownloader(getActivity());
        KeywordsDataSource keywordsDataSource = new KeywordsPreference(getActivity());
        AppScheduler scheduler = new AppScheduler();

        mPresenter = new SubredditsPresenter(repository, downloader,
                keywordsDataSource, scheduler);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddits, container, false);

        unbinder = ButterKnife.bind(this, view);

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE);
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new SubredditsAdapter(new ArrayList<>(), this);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText("Subreddits");

        mSubsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubsRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.attachView(this);
        mPresenter.getSubreddits();
    }

    @Override
    public void onPause() {
        super.onPause();

        mListState = mSubsRecyclerView.getLayoutManager().onSaveInstanceState();
        mPresenter.detachView();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEnterItem.getWindowToken(), 0);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_STATE, mListState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.subreddits_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    searchView.setQuery("", false);
                    searchItem.collapseActionView();
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
    public void showSubreddits(List<Subreddit> subreddits) {
        mAdapter.replaceData(subreddits);

        if (mListState != null) {
            mSubsRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
            mListState = null;
        }
    }

    @Override
    public void showAddedSubreddit(Subreddit subreddit) {
        mAdapter.addData(subreddit, 0);
        mSubsRecyclerView.smoothScrollToPosition(0);
        mEnterItem.setText("");
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
    public void showLoading(boolean isLoading) {
        if (isLoading) {
            mProgessBar.setVisibility(View.VISIBLE);
            mBtnAdd.setEnabled(false);
        } else {
            mProgessBar.setVisibility(View.GONE);
            mBtnAdd.setEnabled(true);
        }
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
    public void onAddButtonClicked(View view) {
        String subreddit = mEnterItem.getText().toString();
        if (!TextUtils.isEmpty(subreddit)) {
            mPresenter.addIfExists(subreddit);
        } else {
            mEnterItem.setError("Enter a subreddit to add");
        }
    }

    @Override
    public void onConfirmClick(String action) {

    }

    private void startDownloadService() {
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra(SubredditService.EXTRA_SUBREDDIT, (ArrayList<String>) mAdapter.getSubsToDownload());
        getActivity().startService(intent);

        Snackbar.make(getActivity().findViewById(android.R.id.content), "Download started", Snackbar.LENGTH_SHORT).show();
    }

    private void showConfirmDialog(String title, String message, String action) {
        ConfirmDialog dialog = ConfirmDialog.newInstance(title, message, action);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "DIALOG");
    }
}


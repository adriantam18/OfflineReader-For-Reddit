package atamayo.offlinereader.Subreddits;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;

import androidx.navigation.fragment.NavHostFragment;
import atamayo.offlinereader.App;
import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Keywords.KeywordsListing;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import atamayo.offlinereader.SubThreads.SubThreadsListing;
import atamayo.offlinereader.SubredditService;
import atamayo.offlinereader.Utils.RedditDownloader;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import atamayo.offlinereader.YesNoDialog;
import atamayo.offlinereader.YesNoDialogListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Displays a list of subreddits. It allows users to add and remove subreddits.
 * Clicking on a subreddit will take the user to a list of threads for that subreddit.
 */
public class SubredditsListing extends Fragment
        implements SubListCallbacks,
                ConfirmDialogListener,
                YesNoDialogListener {
    public static final String TAG = "SubredditsListing";
    private static final String LIST_STATE = "CurrListState";
    private static final String DELETE_ALL_CLICK = "DeleteAllClick";

    @BindView(R.id.enter_item) EditText mEnterItem;
    @BindView(R.id.content_view) RecyclerView mSubsRecyclerView;
    @BindView(R.id.loading_view) ProgressBar mProgessBar;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.btn_add_subreddit) ImageButton mBtnAdd;

    /**
     * Listens to changes on the adapter so the recyclerview can perform any
     * needed operations
     */
    private final RecyclerView.AdapterDataObserver ADAPTER_OBSERVER = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mListState != null) {
                mSubsRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
                mListState = null;
            } else {
                if (positionStart == 0) {
                    mSubsRecyclerView.smoothScrollToPosition(0);
                }
            }
        }
    };
    private Parcelable mListState;
    private Unbinder mUnbinder;
    private SubredditsAdapter mAdapter;
    private SubredditViewModel mViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE);
        }
        setHasOptionsMenu(true);

        SubredditsDataSource repository = ((App) getActivity().getApplication()).getSubredditsRepository();
        SubredditViewModelFactory factory = new SubredditViewModelFactory(repository, new KeywordsPreference(getActivity()),
                new RedditDownloader(getActivity()), new AppScheduler());
        mViewModel = ViewModelProviders.of(this, factory).get(SubredditViewModel.class);
        mAdapter = new SubredditsAdapter(this);

        setObservers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subreddits, container, false);
        mUnbinder = ButterKnife.bind(this, view);
        mAdapter.registerAdapterDataObserver(ADAPTER_OBSERVER);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText("Subreddits");

        mSubsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubsRecyclerView.setAdapter(mAdapter);
        mSubsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();

        mListState = mSubsRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEnterItem.getWindowToken(), 0);

        mAdapter.unregisterAdapterDataObserver(ADAPTER_OBSERVER);
        mUnbinder.unbind();
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
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                startDownloadService();
                return true;
            case R.id.action_delete:
                showYesNoDialog("", "Delete all subreddits, threads, comments, and keywords?", DELETE_ALL_CLICK);
                return true;
            case R.id.action_third_party:
                new LibsBuilder()
                        .withFields(R.string.class.getFields())
                        .start(getActivity());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnOpenListOfThreads(Subreddit subreddit) {
        Bundle bundle = new Bundle();
        bundle.putString(SubThreadsListing.SUBREDDIT_DISPLAY_NAME, subreddit.getDisplayName());
        NavHostFragment.findNavController(this).navigate(R.id.action_subreddits_dest_to_subThreads_dest, bundle);
    }

    @Override
    public void OnOpenListOfKeywords(Subreddit subreddit) {
        Bundle bundle = new Bundle();
        bundle.putString(KeywordsListing.SUBREDDDIT_DISPLAY_NAME, subreddit.getDisplayName());
        NavHostFragment.findNavController(this).navigate(R.id.action_subreddits_dest_to_keywordsListing, bundle);
    }

    @Override
    public void OnDeleteSubreddit(Subreddit subreddit) {
        mViewModel.deleteSubreddit(subreddit.getDisplayName());
    }

    @OnClick(R.id.btn_add_subreddit)
    public void onAddButtonClicked(View view) {
        String subredditName = mEnterItem.getText().toString();
        if (!TextUtils.isEmpty(subredditName)) {
            mViewModel.addSubreddit(subredditName);
            showLoading(true);
        } else {
            mEnterItem.setError("Enter a subreddit to add");
        }
    }

    @Override
    public void onConfirmClick(String action) {}

    @Override
    public void onYesClick(String action) {
        switch (action) {
            case DELETE_ALL_CLICK:
                mViewModel.clearSubreddits();
                break;
            default:
                break;
        }
    }

    @Override
    public void onNoClick(String action) {}

    private void setObservers() {
        mViewModel.getSubredditsObservable().observe(this, subreddits -> {
            mAdapter.submitList(subreddits);
            mEnterItem.setText("");
            showLoading(false);
        });

        mViewModel.getMessageObservable().observe(this, message ->{
            showMessageDialog("", message, "");
            showLoading(false);
        });
    }

    private void showYesNoDialog(String title, String message, String action) {
        YesNoDialog fragment = YesNoDialog.newInstance(title, message, action);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), YesNoDialog.TAG);
    }

    private void startDownloadService() {
        ArrayList<String> subsToDownload = (ArrayList<String>) mAdapter.getSubsToDownload();
        if (!subsToDownload.isEmpty()) {
            Intent intent = new Intent(getActivity(), SubredditService.class);
            intent.putExtra(SubredditService.EXTRA_SUBREDDIT, subsToDownload);
            getActivity().startService(intent);

            Snackbar.make(getActivity().findViewById(android.R.id.content), "Download started", Snackbar.LENGTH_SHORT).show();
        } else {
            showMessageDialog("", "No subreddits to download threads for", "");
        }

    }

    private void showMessageDialog(String title, String message, String action) {
        ConfirmDialog dialog = ConfirmDialog.newInstance(title, message, action);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), ConfirmDialog.TAG);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            mProgessBar.setVisibility(View.VISIBLE);
            mBtnAdd.setEnabled(false);
        } else {
            mProgessBar.setVisibility(View.GONE);
            mBtnAdd.setEnabled(true);
        }
    }
}


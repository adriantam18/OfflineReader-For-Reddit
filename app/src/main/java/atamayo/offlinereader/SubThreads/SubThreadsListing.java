package atamayo.offlinereader.SubThreads;

import android.arch.lifecycle.ViewModelProviders;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;

import androidx.navigation.fragment.NavHostFragment;
import atamayo.offlinereader.App;
import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.SubredditService;
import atamayo.offlinereader.ThreadComments.ThreadCommentsListing;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import atamayo.offlinereader.YesNoDialog;
import atamayo.offlinereader.YesNoDialogListener;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Displays a list of Reddit threads. Clicking on a thread will bring
 * the user to the comments page for that thread.
 */
public class SubThreadsListing extends Fragment
        implements ThreadListCallbacks,
                OnLoadMoreItems,
                ConfirmDialogListener,
                YesNoDialogListener {
    public static final String TAG = "SubThreadsListing";
    public static final String SUBREDDIT_DISPLAY_NAME = "Subreddit";
    private static final int ITEMS_PER_PAGE = 10;
    private static final String EMPTY_THREADS_MESSAGE = "No threads to show";
    private static final String ACTION_DELETE_ALL_THREADS = "Delete all threads";

    @BindView(R.id.sub_threads_list) RecyclerView mSubThreadsList;
    @BindView(R.id.error_msg) TextView mErrorMessage;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.refresh_list_layout) SwipeRefreshLayout mRefresh;
    @BindView(R.id.threads_progress_bar) ProgressBar mProgressBar;

    private Unbinder mUnbinder;
    private SubThreadsAdapter mAdapter;
    private SubThreadsViewModel mViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        SubredditsDataSource repository = ((App) getActivity().getApplication()).getSubredditsRepository();
        String subredditDisplayName = getArguments().getString(SUBREDDIT_DISPLAY_NAME);
        SubThreadsViewModelFactory factory = new SubThreadsViewModelFactory(repository, new AppScheduler(), subredditDisplayName, ITEMS_PER_PAGE);
        mViewModel = ViewModelProviders.of(this, factory).get(SubThreadsViewModel.class);
        mAdapter = new SubThreadsAdapter(new ArrayList<>(), this, this);

        setObservers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sub_threads, container, false);

        mUnbinder = ButterKnife.bind(this, view);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        mSubThreadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubThreadsList.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mSubThreadsList);

        mRefresh.setOnRefreshListener(() -> mViewModel.getThreads(ITEMS_PER_PAGE));

        String subredditDisplayName = getArguments().getString(SUBREDDIT_DISPLAY_NAME);
        String title = subredditDisplayName != null ? subredditDisplayName : "subreddit";
        mTitle.setText(title);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = App.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.subthreads_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_download:
                mViewModel.getCurrentSubreddit();
                return true;
            case R.id.action_delete:
                showYesNoDialog("Delete all?", "This will delete all threads and their comments",
                        ACTION_DELETE_ALL_THREADS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void OnOpenCommentsPage(RedditThread thread) {
        mViewModel.updateSelectedThread(thread);
        Bundle bundle = new Bundle();
        bundle.putString(ThreadCommentsListing.THREAD_FULL_NAME, thread.getFullName());
        NavHostFragment.findNavController(this).navigate(R.id.action_subThreads_dest_to_threadComments_dest, bundle);
    }

    @Override
    public void OnDeleteThread(RedditThread thread) {
        mViewModel.removeThread(thread);
    }

    @Override
    public void onConfirmClick(String action) {}

    @Override
    public void onYesClick(String action) {
        if (action.equals(ACTION_DELETE_ALL_THREADS)) {
            mViewModel.clearThreads();
        }
    }

    @Override
    public void onNoClick(String action) {}

    @Override
    public void loadMore() {
        showLoading(true);
        mViewModel.getThreads(mAdapter.getDataSize() + ITEMS_PER_PAGE);
    }

    private void setObservers() {
        mViewModel.getRedditThreadsObservable().observe(this, redditThreads -> {
            if (!redditThreads.isEmpty()) {
                if (mSubThreadsList.getVisibility() == View.GONE) {
                    mSubThreadsList.setVisibility(View.VISIBLE);
                    mErrorMessage.setVisibility(View.GONE);
                }
                mAdapter.submitList(redditThreads);
            } else {
                showEmptyThreads();
            }
            showLoading(false);
        });

        mViewModel.getMessageObservable().observe(this, message -> {
            showMessageDialog("", message, "");
            showLoading(false);
        });

        mViewModel.getSubredditObservable().observe(this, subreddit -> {
            ArrayList<String> subreddits = new ArrayList<>();
            subreddits.add(subreddit);
            startDownloadService(subreddits);
        });
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            mAdapter.showLoading(true);
        } else {
            mAdapter.showLoading(false);
            mRefresh.setRefreshing(false);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void startDownloadService(ArrayList<String> subreddits) {
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra(SubredditService.EXTRA_SUBREDDIT, subreddits);
        getActivity().startService(intent);

        Snackbar.make(getActivity().findViewById(android.R.id.content), "Download started", Snackbar.LENGTH_LONG).show();
    }

    private void showMessageDialog(String title, String message, String action) {
        ConfirmDialog dialog = ConfirmDialog.newInstance(title, message, action);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "DIALOG");
    }

    private void showYesNoDialog(String title, String message, String action) {
        YesNoDialog fragment = YesNoDialog.newInstance(title, message, action);
        fragment.setTargetFragment(this, 0);
        fragment.show(getFragmentManager(), YesNoDialog.TAG);
    }

    private void showEmptyThreads() {
        mSubThreadsList.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText(EMPTY_THREADS_MESSAGE);
        mRefresh.setRefreshing(false);
    }
}

package atamayo.offlinereader.SubThreads;

import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.leakcanary.RefWatcher;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.App;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.SubredditService;
import atamayo.offlinereader.Data.FileManager;
import atamayo.offlinereader.ThreadComments.ThreadCommentsListing;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Displays a list of Reddit threads. Clicking on a thread will bring
 * the user to the comments page for that thread.
 */
public class SubThreadsListing extends Fragment
        implements SubThreadsContract.View, ThreadListCallbacks,
        OnLoadMoreItems {

    public static final String TAG = "SubThreadsListing";
    public static final String SUBREDDIT = "Subreddit";
    private static final String LIST_STATE = "Liststate";
    private static final String NUM_REQUESTED_THREADS = "NumRequestedThreads";
    private static final int ITEMS_PER_PAGE = 10;
    private Unbinder unbinder;
    private SubThreadsAdapter mAdapter;
    private SubThreadsContract.Presenter mPresenter;
    private OnThreadSelectedListener onThreadSelectedListener;
    private Parcelable mListState;
    private int mNumRequestedThreads;

    @BindView(R.id.sub_threads_list) RecyclerView mSubThreadsList;
    @BindView(R.id.error_msg) TextView mErrorMessage;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.refresh_list_layout) SwipeRefreshLayout mRefresh;

    public interface OnThreadSelectedListener{
        void launchCommentsPage(Bundle args);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            onThreadSelectedListener = (OnThreadSelectedListener) context;
        } catch (ClassCastException e) {
            Log.e(TAG, e.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        DaoSession daoSession = ((App) (getActivity().getApplication())).getDaoSession();
        FileManager commentFileManager = new FileManager(getActivity());
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao(), commentFileManager);
        mPresenter = new SubThreadsPresenter(getArguments().getString(SUBREDDIT, ""), repository,
                this, new AppScheduler());
        mAdapter = new SubThreadsAdapter(new ArrayList<>(), this, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sub_threads, container, false);

        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        final String subreddit = (bundle != null && bundle.getString(SUBREDDIT) != null)
                ? bundle.getString(SUBREDDIT) : "";

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText(subreddit);

        mSubThreadsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSubThreadsList.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mSubThreadsList);

        mRefresh.setOnRefreshListener(() ->
            mPresenter.getThreads(true, 0, ITEMS_PER_PAGE));

        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE);
            mNumRequestedThreads = savedInstanceState.getInt(NUM_REQUESTED_THREADS, ITEMS_PER_PAGE);
        } else {
            mNumRequestedThreads = ITEMS_PER_PAGE;
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        super.onViewCreated(view, savedInstanceState);

        mPresenter.getThreads(true, 0, mNumRequestedThreads);
    }

    @Override
    public void onResume(){
        super.onResume();
        mPresenter.subscribe(this);
    }

    @Override
    public void onPause(){
        super.onPause();

        mListState = mSubThreadsList.getLayoutManager().onSaveInstanceState();
        mPresenter.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putParcelable(LIST_STATE, mListState);
        outState.putInt(NUM_REQUESTED_THREADS, mAdapter.getNumberOfThreads());

        super.onSaveInstanceState(outState);
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
        inflater.inflate(R.menu.subthreads_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.action_download:
                mPresenter.downloadThreads();
                return true;
            case R.id.action_delete:
                mPresenter.removeAllThreads();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showInitialThreads(List<RedditThread> threads) {
        mRefresh.setRefreshing(false);

        mAdapter.replaceData(threads);

        mSubThreadsList.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.GONE);

        if (mListState != null) {
            mSubThreadsList.getLayoutManager().onRestoreInstanceState(mListState);
            mListState = null;
        }
    }

    @Override
    public void showMoreThreads(List<RedditThread> threads){
        mAdapter.addData(threads);
    }

    @Override
    public void showEmptyThreads(){
        mSubThreadsList.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText("No threads to show");
        mRefresh.setRefreshing(false);
    }

    @Override
    public void showLoading(boolean isLoading){
        mAdapter.showLoading(isLoading);
    }

    @Override
    public void showCommentsPage(String threadFullName) {
        Bundle args = new Bundle();
        args.putString(ThreadCommentsListing.THREAD_FULL_NAME, threadFullName);

        onThreadSelectedListener.launchCommentsPage(args);
    }

    @Override
    public void startDownloadService(List<String> subreddits){
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra(SubredditService.EXTRA_SUBREDDIT, (ArrayList<String>) subreddits);
        getActivity().startService(intent);

        Snackbar.make(getActivity().findViewById(android.R.id.content), "Download started", Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(SubThreadsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void OnOpenCommentsPage(RedditThread thread){
        mPresenter.openCommentsPage(thread);
    }

    @Override
    public void OnDeleteThread(RedditThread thread){
        mPresenter.removeThread(thread);
    }

    @Override
    public void loadMore(){
        mPresenter.getThreads(false, mAdapter.getNumberOfThreads(), ITEMS_PER_PAGE);
    }
}

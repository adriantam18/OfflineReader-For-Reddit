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
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SubThreadsListing extends Fragment
        implements SubThreadsContract.View, ThreadListCallbacks,
        OnLoadMoreItems {

    public static final String TAG = "SubThreadsListing";
    public static final String SUBREDDIT = "Subreddit";
    private static final String LIST_STATE = "Liststate";
    private static final String THREADS_IN_VIEW = "ThreadsInView";
    private static final String STATE_BUNDLE = "StateBundle";
    private static final int ITEMS_PER_PAGE = 10;
    private Unbinder unbinder;
    private SubThreadsAdapter mAdapter;
    private SubThreadsContract.Presenter mPresenter;
    private OnThreadSelectedListener onThreadSelectedListener;
    private Parcelable mListState;
    private int mCurrentThreadsInView;
    private Bundle stateToSave;

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
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao(), commentFileManager);
        mPresenter = new SubThreadsPresenter(repository, this);
        mAdapter = new SubThreadsAdapter(new ArrayList<RedditThread>(), this, this);

        if(savedInstanceState != null){
            stateToSave = savedInstanceState.getBundle(STATE_BUNDLE);
            mListState = stateToSave.getParcelable(LIST_STATE);
            mCurrentThreadsInView = stateToSave.getInt(THREADS_IN_VIEW);
        }else{
            mCurrentThreadsInView = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sub_threads, container, false);

        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        final String subreddit = bundle.getString(SUBREDDIT) != null
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
            mPresenter.initSubThreadsList(subreddit, 0, ITEMS_PER_PAGE));

        if(savedInstanceState == null) {
            mPresenter.initSubThreadsList(subreddit, 0, ITEMS_PER_PAGE);
        }else{
            mPresenter.initSubThreadsList(subreddit, 0, mCurrentThreadsInView);
        }

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
        mPresenter.subscribe(this);
    }

    @Override
    public void onPause(){
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putBundle(STATE_BUNDLE, (stateToSave != null) ? stateToSave : saveState());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView(){
        stateToSave = saveState();

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
        switch (item.getItemId()){
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

        if(mListState != null) {
            mSubThreadsList.getLayoutManager().onRestoreInstanceState(mListState);
            mListState = null;
        }

        mCurrentThreadsInView = threads.size();
    }

    @Override
    public void showMoreThreads(List<RedditThread> threads){
        mAdapter.addData(threads);
        mCurrentThreadsInView += threads.size();
    }

    @Override
    public void showEmptyThreads(){
        mSubThreadsList.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText("No threads to show");
        mRefresh.setRefreshing(false);
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
        mPresenter.getMoreThreads(mCurrentThreadsInView, ITEMS_PER_PAGE);
    }

    private Bundle saveState(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(LIST_STATE, mSubThreadsList.getLayoutManager().onSaveInstanceState());
        bundle.putInt(THREADS_IN_VIEW, mCurrentThreadsInView);

        return bundle;
    }
}

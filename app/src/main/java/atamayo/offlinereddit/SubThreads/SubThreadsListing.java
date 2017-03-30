package atamayo.offlinereddit.SubThreads;

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

import atamayo.offlinereddit.App;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.MainActivity;
import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import atamayo.offlinereddit.SubredditService;
import atamayo.offlinereddit.Data.CommentFileManager;
import atamayo.offlinereddit.ThreadComments.ThreadCommentsListing;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SubThreadsListing extends Fragment implements SubThreadsContract.View, ThreadListCallbacks {

    public static final String TAG = "SubThreadsListing";
    public static final String SUBREDDIT = "subreddit";
    private Unbinder unbinder;
    private SubThreadsAdapter mAdapter;
    private SubThreadsContract.Presenter mPresenter;
    private OnThreadSelectedListener mCallback;

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
            mCallback = (OnThreadSelectedListener) context;
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
        mPresenter = new SubThreadsPresenter(repository, this);
        mAdapter = new SubThreadsAdapter(new ArrayList<RedditThread>(), this);
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

        mPresenter.initSubThreadsList(subreddit);

        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPresenter.initSubThreadsList(subreddit);
            }
        });

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
        mAdapter.replaceData(threads);

        mRefresh.setVisibility(View.VISIBLE);
        mErrorMessage.setVisibility(View.GONE);

        mRefresh.setRefreshing(false);
    }

    @Override
    public void showEmptyThreads(){
        mRefresh.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mErrorMessage.setText("No threads to show");
    }

    @Override
    public void showCommentsPage(String threadFullName) {
        Bundle args = new Bundle();
        args.putString(ThreadCommentsListing.THREAD_FULL_NAME, threadFullName);

        mCallback.launchCommentsPage(args);
    }

    @Override
    public void startDownloadService(List<String> subreddits){
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra("subreddits", (ArrayList<String>) subreddits);
        getActivity().startService(intent);
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
}

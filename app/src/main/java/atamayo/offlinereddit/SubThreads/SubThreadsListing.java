package atamayo.offlinereddit.SubThreads;

import android.app.Fragment;
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
import java.util.Arrays;
import java.util.List;

import atamayo.offlinereddit.App;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.MainActivity;
import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import atamayo.offlinereddit.SubredditService;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SubThreadsListing extends Fragment implements SubThreadsContract.View, ThreadListCallbacks {

    private static final String TAG = "Sub Threads Fragment";
    private Unbinder unbinder;
    private SubThreadsAdapter mAdapter;
    private SubThreadsContract.Presenter mPresenter;
    private OnThreadSelectedListener mCallback;
    @BindView(R.id.sub_threads_list) RecyclerView mSubThreadsList;
    @BindView(R.id.error_msg) TextView mErrorMessage;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.refresh) SwipeRefreshLayout mRefresh;

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

        DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao());
        mPresenter = new SubThreadsPresenter(repository, this);
        mAdapter = new SubThreadsAdapter(new ArrayList<RedditThread>(), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.fragment_sub_threads, container, false);

        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        final String subreddit = bundle.getString(MainActivity.EXTRA_SUBREDDIT) != null
                ? bundle.getString(MainActivity.EXTRA_SUBREDDIT) : "";

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
            case R.id.action_refresh:
                //TODO add refresh
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showInitialThreads(List<RedditThread> threads) {
        mAdapter.replaceData(threads);

        if(mSubThreadsList.getVisibility() == View.GONE){
            mSubThreadsList.setVisibility(View.VISIBLE);
        }

        mRefresh.setRefreshing(false);
    }

    @Override
    public void showRemovedThreads(int start, int itemCount) {
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showCommentsPage(RedditThread thread) {
        Bundle args = new Bundle();
        args.putString("filename", thread.getFilename());

        mCallback.launchCommentsPage(args);
    }

    @Override
    public void startDownloadService(List<String> subreddits){
        Intent intent = new Intent(getActivity(), SubredditService.class);
        intent.putExtra("subreddits", (ArrayList<String>) subreddits);
        getActivity().startService(intent);
    }

    @Override
    public void showErrorMessage(String message){
        mSubThreadsList.setVisibility(View.GONE);
        mErrorMessage.setText(message);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void setPresenter(SubThreadsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void OnOpenCommentsPage(int position){
        mPresenter.openCommentsPage(position);
    }

    @Override
    public void OnDeleteThread(int position){
        mPresenter.removeThread(new ArrayList<>(Arrays.asList(position)));
    }
}

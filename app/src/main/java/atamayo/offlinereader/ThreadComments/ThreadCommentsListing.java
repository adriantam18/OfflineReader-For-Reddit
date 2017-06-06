package atamayo.offlinereader.ThreadComments;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.App;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.Data.FileManager;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ThreadCommentsListing extends Fragment
        implements ThreadCommentsContract.View, OnLoadMoreItems {
    public static final String TAG = "ThreadCommentsListing";
    public static final String THREAD_FULL_NAME = "ThreadFullName";
    private static final String TOP_COMMENTS_IN_VIEW = "TopCommentsInView";
    private static final String LIST_STATE = "CurrListState";
    private static final String STATE_BUNDLE = "StateBundle";
    private static final int ITEMS_PER_PAGE = 10;
    private int mCurrentTopCommentsInView;
    private Parcelable mListState;
    private Bundle stateToSave;

    @BindView(R.id.comments_list) RecyclerView mCommentsList;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;
    ThreadCommentsAdapter mAdapter;
    ThreadCommentsContract.Presenter mPresenter;
    Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FileManager fileManager = new FileManager(getActivity());
        DaoSession daoSession = ((App) (getActivity().getApplication())).getDaoSession();
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao(), fileManager);
        mPresenter = new ThreadCommentsPresenter(repository, this);
        mAdapter = new ThreadCommentsAdapter(new ArrayList<>(0), this, getActivity());

        if(savedInstanceState != null){
            stateToSave = savedInstanceState.getBundle(STATE_BUNDLE);
            mListState = stateToSave.getParcelable(LIST_STATE);
            mCurrentTopCommentsInView = stateToSave.getInt(TOP_COMMENTS_IN_VIEW);
        }else{
            mCurrentTopCommentsInView = 0;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancestate) {

        View view = inflater.inflate(R.layout.fragment_thread_comments, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        String threadFullName = bundle.getString(THREAD_FULL_NAME);

        mCommentsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsList.setAdapter(mAdapter);

        if (savedInstancestate == null) {
            mPresenter.initCommentsView(threadFullName, 0, ITEMS_PER_PAGE);
        } else {
            mPresenter.initCommentsView(threadFullName, 0,
                    mCurrentTopCommentsInView);
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.subscribe(this);
    }

    @Override
    public void onDestroyView() {
        stateToSave = saveState();
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        outState.putBundle(STATE_BUNDLE, (stateToSave != null) ? stateToSave : saveState());

        super.onSaveInstanceState(outState);
    }

    @Override
    public void showParentThread(RedditThread thread){
        mAdapter.addThread(thread);
    }

    @Override
    public void showInitialComments(List<RedditComment> comments) {
        mAdapter.replaceData(comments);

        if(mListState != null){
            mCommentsList.getLayoutManager().onRestoreInstanceState(mListState);
            mListState = null;
        }

        mCurrentTopCommentsInView = ITEMS_PER_PAGE;
    }

    @Override
    public void showMoreComments(List<RedditComment> comments) {
        mAdapter.addData(comments);

        mCurrentTopCommentsInView += ITEMS_PER_PAGE;
    }

    @Override
    public void showLoading(boolean isLoading){
        if(isLoading){
            mProgressBar.setVisibility(View.VISIBLE);
        }else{
            mProgressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void setPresenter(ThreadCommentsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadMore() {
        mPresenter.getMoreComments(mCurrentTopCommentsInView, ITEMS_PER_PAGE);
    }

    private Bundle saveState(){
        Bundle bundle = new Bundle();
        bundle.putParcelable(LIST_STATE, mCommentsList.getLayoutManager().onSaveInstanceState());
        bundle.putInt(TOP_COMMENTS_IN_VIEW, mCurrentTopCommentsInView);

        return bundle;
    }
}

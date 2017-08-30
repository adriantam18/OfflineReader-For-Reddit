package atamayo.offlinereader.ThreadComments;

import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.App;
import atamayo.offlinereader.Data.FileManager;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Displays content and comments list for a Reddit thread.
 */
public class ThreadCommentsListing extends Fragment
        implements ThreadCommentsContract.View, OnLoadMoreItems {
    public static final String TAG = "ThreadCommentsListing";
    public static final String THREAD_FULL_NAME = "ThreadFullName";
    private static final String PARENT_COMMENTS_IN_VIEW = "ParentCommentsInView";
    private static final String LIST_STATE = "CurrListState";
    private static final int ITEMS_PER_PAGE = 10;

    private int mNumRequestedParentComments;
    private Parcelable mCommentsListState;

    @BindView(R.id.error_msg)
    TextView mErrorMessage;
    @BindView(R.id.comments_list)
    RecyclerView mCommentsList;
    ThreadCommentsAdapter mAdapter;
    ThreadCommentsPresenter mPresenter;
    Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String threadFullName = args != null ? args.getString(THREAD_FULL_NAME, "")
                : "";

        DaoSession daoSession = ((App) (getActivity().getApplication())).getDaoSession();
        FileManager commentFileManager = new FileManager(getActivity());
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(),
                daoSession.getSubredditDao(), commentFileManager);
        mPresenter = new ThreadCommentsPresenter(threadFullName, repository, new AppScheduler());
        mAdapter = new ThreadCommentsAdapter(new ArrayList<>(0), this, getActivity());

        if (savedInstanceState != null) {
            mCommentsListState = savedInstanceState.getParcelable(LIST_STATE);
            mNumRequestedParentComments = savedInstanceState.getInt(PARENT_COMMENTS_IN_VIEW, ITEMS_PER_PAGE);
        } else {
            mNumRequestedParentComments = ITEMS_PER_PAGE;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_thread_comments, container, false);
        unbinder = ButterKnife.bind(this, view);

        mCommentsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsList.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        mPresenter.attachView(this);
        mPresenter.getParentThread();
        mPresenter.getComments(true, 0, mNumRequestedParentComments);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPause() {
        super.onPause();

        mCommentsListState = mCommentsList.getLayoutManager().onSaveInstanceState();
        mPresenter.detachView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_STATE, mCommentsListState);
        outState.putInt(PARENT_COMMENTS_IN_VIEW, mNumRequestedParentComments);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void showParentThread(RedditThread thread) {
        mAdapter.addThread(thread);
    }

    @Override
    public void showInitialComments(List<RedditComment> comments) {
        if (!comments.isEmpty()) {
            mErrorMessage.setVisibility(View.GONE);
            mCommentsList.setVisibility(View.VISIBLE);
            mAdapter.replaceData(comments);

            if (mCommentsListState != null) {
                mCommentsList.getLayoutManager().onRestoreInstanceState(mCommentsListState);
                mCommentsListState = null;
            }
        } else {
            showEmptyComments();
        }
    }

    @Override
    public void showMoreComments(List<RedditComment> comments) {
        mAdapter.addData(comments);
    }

    @Override
    public void showEmptyComments() {
        mCommentsList.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLoading(boolean isLoading) {
        mAdapter.showLoading(isLoading);
    }

    @Override
    public void loadMore() {
        mPresenter.getComments(false, mNumRequestedParentComments, ITEMS_PER_PAGE);
        mNumRequestedParentComments += ITEMS_PER_PAGE;
    }
}

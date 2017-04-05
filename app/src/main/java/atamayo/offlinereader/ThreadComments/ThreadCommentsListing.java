package atamayo.offlinereader.ThreadComments;

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
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.Data.SubredditsRepository;
import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.RedditDAO.DaoSession;
import atamayo.offlinereader.Data.CommentFileManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ThreadCommentsListing extends Fragment
        implements ThreadCommentsContract.View, LoadCommentsCallback {
    public static final String TAG = "ThreadCommentsListing";
    public static final String THREAD_FULL_NAME = "ThreadFullName";

    @BindView(R.id.self_text) TextView mSelfText;
    @BindView(R.id.comments_list) RecyclerView mCommentsList;
    ThreadCommentsAdapter mAdapter;
    ThreadCommentsContract.Presenter mPresenter;
    Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        CommentFileManager fileManager = new CommentFileManager(getActivity());
        DaoSession daoSession = ((App) (getActivity().getApplication())).getDaoSession();
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao(), fileManager);
        mPresenter = new ThreadCommentsPresenter(repository, this);
        mAdapter = new ThreadCommentsAdapter(new ArrayList<RedditComment>(0), this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancestate){

        View view = inflater.inflate(R.layout.fragment_thread_comments, container, false);
        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = getArguments();
        String threadFullName = bundle.getString(THREAD_FULL_NAME);

        mCommentsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsList.setAdapter(mAdapter);

        mPresenter.initCommentsView(threadFullName);

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void showSelfText(RedditThread thread){
        //mSelfText.setText(selfText);
        mAdapter.addThread(thread);
    }

    @Override
    public void showComments(List<RedditComment> comments) {
        mAdapter.replaceData(comments);
    }

    @Override
    public void showMoreComments(List<RedditComment> comments){
        mAdapter.addData(comments);
    }

    @Override
    public void setPresenter(ThreadCommentsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void loadMore(){
        mPresenter.getMoreComments();
    }
}
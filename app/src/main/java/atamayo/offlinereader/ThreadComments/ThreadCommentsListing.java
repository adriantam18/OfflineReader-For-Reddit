package atamayo.offlinereader.ThreadComments;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import atamayo.offlinereader.App;
import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.SubredditsDataSource;
import atamayo.offlinereader.R;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Displays content and comments list for a Reddit thread.
 */
public class ThreadCommentsListing extends Fragment
        implements OnLoadMoreItems,
                ConfirmDialogListener {
    public static final String THREAD_FULL_NAME = "ThreadFullName";
    private static final int ITEMS_PER_PAGE = 10;

    @BindView(R.id.error_msg) TextView mErrorMessage;
    @BindView(R.id.comments_list) RecyclerView mCommentsList;

    private ThreadCommentsViewModel mViewModel;
    private ThreadCommentsAdapter mAdapter;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SubredditsDataSource repository = ((App) getActivity().getApplication()).getSubredditsRepository();
        String threadFullName = getArguments().getString(THREAD_FULL_NAME);
        ThreadCommentsViewModelFactory factory = new ThreadCommentsViewModelFactory(repository, new AppScheduler(), threadFullName, ITEMS_PER_PAGE);
        mViewModel = ViewModelProviders.of(this, factory).get(ThreadCommentsViewModel.class);
        mAdapter = new ThreadCommentsAdapter(new ArrayList<>(0), this, getActivity());

        setObservers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_thread_comments, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        mCommentsList.setLayoutManager(new LinearLayoutManager(getActivity()));
        mCommentsList.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void loadMore() {
        mAdapter.showLoading(true);
        mViewModel.getComments(mAdapter.getDataSize() + ITEMS_PER_PAGE);
    }

    @Override
    public void onConfirmClick(String action) {}

    private void setObservers() {
        mViewModel.getThreadObservable().observe(this, thread -> {
            mAdapter.addThread(thread);
        });

        mViewModel.getCommentsObservable().observe(this, comments -> {
            mAdapter.submitList(comments);
            mAdapter.showLoading(false);
        });

        mViewModel.getMessageObservable().observe(this, message -> {
            showMessageDialog("", message, "");
            mAdapter.showLoading(false);
        });
    }

    private void showMessageDialog(String title, String message, String action) {
        ConfirmDialog dialog = ConfirmDialog.newInstance(title, message, action);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "DIALOG");
    }
}

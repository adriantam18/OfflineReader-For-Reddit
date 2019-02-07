package atamayo.offlinereader.Keywords;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
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
import android.widget.EditText;
import android.widget.TextView;

import atamayo.offlinereader.ConfirmDialog;
import atamayo.offlinereader.ConfirmDialogListener;
import atamayo.offlinereader.Data.KeywordsPreference;
import atamayo.offlinereader.R;
import atamayo.offlinereader.Utils.Schedulers.AppScheduler;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Displays the list of keywords for a subreddit. It allows users to add
 * and remove keywords.
 */
public class KeywordsListing extends Fragment
        implements KeywordsListCallback,
                ConfirmDialogListener {
    public static final String TAG = "KeywordsListing";
    public static final String SUBREDDDIT_DISPLAY_NAME = "Subreddit";
    private static final String LIST_STATE = "List_State";

    @BindView(R.id.keywords_list) RecyclerView mKeywordsRecyclerView;
    @BindView(R.id.enter_keyword) EditText mUserInput;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.title) TextView mTitle;

    /**
     * Listens to changes on the adapter so the recyclerview can perform any
     * needed operations
     */
    private final RecyclerView.AdapterDataObserver ADAPTER_OBSERVER = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mListState != null) {
                mKeywordsRecyclerView.getLayoutManager().onRestoreInstanceState(mListState);
                mListState = null;
            } else {
                if (positionStart == 0) {
                    mKeywordsRecyclerView.smoothScrollToPosition(0);
                }
            }
        }
    };
    private KeywordsViewModel mViewModel;
    private KeywordsAdapter mAdapter;
    private Parcelable mListState;
    private Unbinder mUnbinder;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mListState = savedInstanceState.getParcelable(LIST_STATE);
        }
        setHasOptionsMenu(true);

        KeywordsPreference repository = new KeywordsPreference(getActivity());
        String subredditName = getArguments().getString(SUBREDDDIT_DISPLAY_NAME);
        KeywordsViewModelFactory factory = new KeywordsViewModelFactory(repository, new AppScheduler(), subredditName);
        mViewModel = ViewModelProviders.of(this, factory).get(KeywordsViewModel.class);
        mAdapter = new KeywordsAdapter(this);

        setObservers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_keywords, container, false);

        mUnbinder = ButterKnife.bind(this, view);
        mAdapter.registerAdapterDataObserver(ADAPTER_OBSERVER);

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        mTitle.setText(getArguments().getString(SUBREDDDIT_DISPLAY_NAME));

        mKeywordsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mKeywordsRecyclerView.setAdapter(mAdapter);
        mKeywordsRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        mListState = mKeywordsRecyclerView.getLayoutManager().onSaveInstanceState();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mAdapter.unregisterAdapterDataObserver(ADAPTER_OBSERVER);
        mUnbinder.unbind();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.keywords_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mViewModel.clearKeywords();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(LIST_STATE, mListState);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void OnDeleteKeyword(String keyword) {
        mViewModel.removeKeyword(keyword);
    }

    @Override
    public void onConfirmClick(String action) {

    }

    @OnClick(R.id.btn_add_keyword)
    public void onAddKeywordClicked(View view) {
        String keyword = mUserInput.getText().toString();
        if (!TextUtils.isEmpty(keyword)) {
            mViewModel.addKeyword(keyword);
        } else {
            mUserInput.setError("Please enter a keyword");
        }
    }

    private void setObservers() {
        mViewModel.getKeywordsObservable().observe(this, keywords -> {
            mAdapter.submitList(keywords);
            mUserInput.setText("");
        });

        mViewModel.getMessageObservable().observe(this, message -> {
            showDialog("", message, "");
        });
    }

    private void showDialog(String title, String message, String action) {
        DialogFragment dialog = ConfirmDialog.newInstance(title, message, action);
        dialog.setTargetFragment(this, 0);
        dialog.show(getFragmentManager(), "Dialog");
    }
}

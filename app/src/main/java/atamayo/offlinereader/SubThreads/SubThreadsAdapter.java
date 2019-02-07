package atamayo.offlinereader.SubThreads;

import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubThreadsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        implements ItemTouchHelperAdapter {
    private static final int VIEW_ITEM = R.layout.sub_threads_list_item;
    private static final int VIEW_FOOTER = R.layout.thread_footer;

    private List<RedditThread> mThreads;
    private ThreadListCallbacks mThreadListCallbacks;
    private OnLoadMoreItems mLoadMoreCallback;
    private boolean showProgressBar;

    public SubThreadsAdapter(List<RedditThread> threads, ThreadListCallbacks callbacks,
                             OnLoadMoreItems loadMoreItemsCallback) {
        mThreads = threads;
        mThreadListCallbacks = callbacks;
        mLoadMoreCallback = loadMoreItemsCallback;
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Nullable
        @BindView(R.id.thread_card) CardView mCardView;
        @Nullable
        @BindView(R.id.score) TextView mScoreView;
        @Nullable
        @BindView(R.id.thread_title) TextView mTitleView;
        @Nullable
        @BindView(R.id.num_comments) TextView mNumCommentsView;
        @Nullable
        @BindView(R.id.author_and_time) TextView mAuthorView;
        @BindColor(R.color.white) int mDefaultColor;
        @BindColor(R.color.red_500) int mClickedColor;

        public ThreadViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mThreadListCallbacks.OnOpenCommentsPage(mThreads.get(getAdapterPosition()));
        }

        public void bind(RedditThread thread) {
            String authorAndTime = thread.getAuthor() + " \u2022 " + thread.getFormattedTime();
            String numComments = String.valueOf(thread.getNumComments()) + " comments";
            String score;
            if (thread.getScore() >= 10000) {
                score = String.valueOf(thread.getScore() / 1000) + "k";
            } else {
                score = String.valueOf(thread.getScore());
            }

            mScoreView.setText(score);
            mTitleView.setText(thread.getTitle());
            mNumCommentsView.setText(numComments);
            mAuthorView.setText(authorAndTime);

            if (thread.getWasClicked()) {
                mTitleView.setTextColor(mClickedColor);
            } else {
                mTitleView.setTextColor(mDefaultColor);
            }
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.btn_load_more_threads) ImageButton mBtnLoadMore;
        @BindView(R.id.progress_bar) ProgressBar mProgressBar;

        public FooterViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.btn_load_more_threads)
        public void onLoadMoreClicked(View view) {
            mLoadMoreCallback.loadMore();
        }

        public void bind(boolean showProgressBar) {
            if (showProgressBar) {
                mBtnLoadMore.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.VISIBLE);
            } else {
                mProgressBar.setVisibility(View.GONE);
                mBtnLoadMore.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onItemDismiss(int position) {
        mThreadListCallbacks.OnDeleteThread(mThreads.get(position));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_FOOTER:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.thread_footer, parent, false);
                return new FooterViewHolder(itemView);
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.sub_threads_list_item, parent, false);
                return new ThreadViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_ITEM:
                ((ThreadViewHolder) holder).bind(mThreads.get(position));
                break;
            case VIEW_FOOTER:
                ((FooterViewHolder) holder).bind(showProgressBar);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        //There's a footer so there's an extra item in the list
        return mThreads.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mThreads.size()) {
            return VIEW_FOOTER;
        } else {
            return VIEW_ITEM;
        }
    }

    public void submitList(List<RedditThread> newList) {
        mThreads.clear();
        mThreads.addAll(newList);
        notifyDataSetChanged();
    }

    public void showLoading(boolean isLoading) {
        showProgressBar = isLoading;
        notifyItemChanged(mThreads.size());
    }

    public int getDataSize() {
        return mThreads.size();
    }
}

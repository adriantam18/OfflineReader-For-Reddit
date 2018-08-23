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
    private List<RedditThread> mThreadDataList;
    private ThreadListCallbacks mThreadListCallbacks;
    private OnLoadMoreItems mLoadMoreCallback;
    private int mNumRecentlyLoaded;
    private boolean shouldShowLoading;
    private static final int VIEW_ITEM = R.layout.sub_threads_list_item;
    private static final int VIEW_FOOTER = R.layout.thread_footer;

    public SubThreadsAdapter(List<RedditThread> threadDataList, ThreadListCallbacks callbacks,
                             OnLoadMoreItems loadMoreItemsCallback) {
        mThreadDataList = threadDataList;
        mThreadListCallbacks = callbacks;
        mLoadMoreCallback = loadMoreItemsCallback;
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @Nullable
        @BindView(R.id.thread_card)
        CardView cardView;
        @Nullable
        @BindView(R.id.score)
        TextView scoreView;
        @Nullable
        @BindView(R.id.thread_title)
        TextView titleView;
        @Nullable
        @BindView(R.id.num_comments)
        TextView numCommentsView;
        @Nullable
        @BindView(R.id.author_and_time)
        TextView authorView;
        @BindColor(R.color.white)
        int defaultColor;
        @BindColor(R.color.red_500)
        int clickedColor;

        public ThreadViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mThreadListCallbacks.OnOpenCommentsPage(mThreadDataList.get(getAdapterPosition()));
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.btn_load_more_threads)
        ImageButton btnLoadMore;
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        public FooterViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.btn_load_more_threads)
        public void onLoadMoreClicked(View view) {
            mLoadMoreCallback.loadMore();
        }
    }

    public void onItemDismiss(int position) {
        mThreadListCallbacks.OnDeleteThread(mThreadDataList.get(position));
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
        switch (holder.getItemViewType()) {
            case VIEW_ITEM:
                ThreadViewHolder threadViewHolder = (ThreadViewHolder) holder;
                configureItemView(threadViewHolder, position);
                break;
            case VIEW_FOOTER:
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                configureFooterView(footerViewHolder);
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        return mThreadDataList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mThreadDataList.size()) {
            return VIEW_FOOTER;
        } else {
            return VIEW_ITEM;
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private void configureItemView(ThreadViewHolder holder, int position) {
        if (!mThreadDataList.isEmpty()) {
            final RedditThread threadData = mThreadDataList.get(position);

            String authorAndTime = threadData.getAuthor() + " \u2022 " + threadData.getFormattedTime();
            String numComments = String.valueOf(threadData.getNumComments()) + " comments";
            String score;
            if (threadData.getScore() >= 10000) {
                score = String.valueOf(threadData.getScore() / 1000) + "k";
            } else {
                score = String.valueOf(threadData.getScore());
            }

            holder.scoreView.setText(score);
            holder.titleView.setText(threadData.getTitle());
            holder.numCommentsView.setText(numComments);
            holder.authorView.setText(authorAndTime);

            if (threadData.getWasClicked()) {
                holder.titleView.setTextColor(holder.clickedColor);
            } else {
                holder.titleView.setTextColor(holder.defaultColor);
            }
        }
    }

    private void configureFooterView(FooterViewHolder holder) {
        if (shouldShowLoading) {
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.btnLoadMore.setVisibility(View.GONE);
        } else {
            holder.progressBar.setVisibility(View.GONE);

            if (mNumRecentlyLoaded > 0) {
                holder.btnLoadMore.setVisibility(View.VISIBLE);
            } else {
                holder.btnLoadMore.setVisibility(View.GONE);
            }
        }
    }

    public void replaceData(List<RedditThread> newList) {
        mNumRecentlyLoaded = newList.size();
        mThreadDataList.clear();
        mThreadDataList.addAll(newList);
        notifyDataSetChanged();
    }

    public void addData(List<RedditThread> threads) {
        mNumRecentlyLoaded = threads.size();
        mThreadDataList.addAll(threads);
        notifyDataSetChanged();
    }

    public void showLoading(boolean isLoading) {
        shouldShowLoading = isLoading;
        notifyItemChanged(mThreadDataList.size());
    }

    public int getNumberOfThreads() {
        return mThreadDataList.size();
    }
}

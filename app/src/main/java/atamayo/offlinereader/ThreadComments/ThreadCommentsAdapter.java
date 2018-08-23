package atamayo.offlinereader.ThreadComments;

import android.content.Context;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.XMLReader;

import java.util.List;

import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import atamayo.offlinereader.Utils.OnLoadMoreItems;
import butterknife.BindArray;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ThreadCommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RedditComment> mCommentsList;
    private int mNumRecentlyLoaded;
    private OnLoadMoreItems mLoadMoreCallback;
    private RedditThread mThread;
    private static final int VIEW_HEADER = R.layout.comments_header;
    private static final int VIEW_FOOTER = R.layout.comments_footer;
    private static final int VIEW_ITEM = R.layout.comments_list_item;
    private Context mContext;
    private boolean shouldShowLoading;

    public ThreadCommentsAdapter(List<RedditComment> comments, OnLoadMoreItems callback, Context context) {
        mCommentsList = comments;
        mNumRecentlyLoaded = 0;
        mLoadMoreCallback = callback;
        mContext = context;
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.title_view)
        TextView titleView;
        @Nullable
        @BindView(R.id.time_author_view)
        TextView timeAuthorView;
        @Nullable
        @BindView(R.id.image)
        ImageView imageView;
        @Nullable
        @BindView(R.id.self_text_view)
        TextView selftextView;
        @BindColor(R.color.white)
        int defaultColor;
        @BindColor(R.color.red_500)
        int clickedColor;

        public ThreadViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.depth_marker)
        View depthMarker;
        @Nullable
        @BindView(R.id.info_text_view)
        TextView infoView;
        @Nullable
        @BindView(R.id.comment_body_view)
        TextView commentBodyView;
        @Nullable
        @BindView(R.id.divider)
        View divider;
        @BindDimen(R.dimen.depth_marker_width)
        float depthMarkerWidth;
        @BindArray(R.array.depth_colors)
        int[] colors;

        public CommentViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.btn_load_more_comments)
        Button btnLoadMore;
        @Nullable
        @BindView(R.id.progress_bar)
        ProgressBar progressBar;

        public FooterViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.btn_load_more_comments)
        public void onLoadMoreClicked(View view) {
            mLoadMoreCallback.loadMore();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        switch (viewType) {
            case VIEW_HEADER:
                itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_HEADER, parent, false);
                return new ThreadViewHolder(itemView);
            case VIEW_FOOTER:
                itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_FOOTER, parent, false);
                return new FooterViewHolder(itemView);
            default:
                itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_ITEM, parent, false);
                return new CommentViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_HEADER:
                ThreadViewHolder threadViewHolder = (ThreadViewHolder) holder;
                configureHeaderView(threadViewHolder);
                break;
            case VIEW_ITEM:
                CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
                configureCommentViews(commentViewHolder, position - 1);
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
        //We added a header and a footer so we have 2 extra items aside
        //from our comments list
        return mCommentsList.size() + 2;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_HEADER;
        } else if (position == mCommentsList.size() + 1) {
            return VIEW_FOOTER;
        } else {
            return VIEW_ITEM;
        }
    }

    private void configureHeaderView(ThreadViewHolder holder) {
        if (mThread != null) {
            holder.titleView.setText(mThread.getTitle());
            holder.titleView.setTextColor(holder.clickedColor);

            String timeAuthor = mThread.getFormattedTime() + " by " + mThread.getAuthor();
            holder.timeAuthorView.setText(timeAuthor);

            String selftextHtml = mThread.getSelftextHtml();
            if (TextUtils.isEmpty(selftextHtml)) {
                holder.selftextView.setVisibility(View.GONE);
            } else {
                holder.selftextView.setVisibility(View.VISIBLE);
                Spanned html = fromHtml(selftextHtml);
                holder.selftextView.setText(trim(html, 0, html.length()));
            }

            if (holder.imageView.getVisibility() == View.GONE) {
                holder.imageView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(mThread.getMediaPath())
                        .fitCenter()
                        .crossFade()
                        .into(holder.imageView);
            }
        }
    }

    private void configureCommentViews(CommentViewHolder holder, int position) {
        RedditComment comment = mCommentsList.get(position);

        String points = Integer.toString(comment.getScore()) + " points";
        String time = comment.getFormattedTime();
        String info = comment.getAuthor() + " \u2022 " + points + " \u2022 " + time;
        holder.infoView.setText(info);

        String bodyHtml = comment.getBodyHtml();
        if (!TextUtils.isEmpty(bodyHtml)) {
            Spanned spanned = fromHtml(comment.getBodyHtml());
            holder.commentBodyView.setText(trim(spanned, 0, spanned.length()));
        } else {
            holder.commentBodyView.setText(bodyHtml);
        }

        if (position == getItemCount()) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        int paddingStart = 0;
        if (comment.getDepth() > 0) {
            holder.depthMarker.setVisibility(View.VISIBLE);
            paddingStart = Math.round(holder.depthMarkerWidth) * (comment.getDepth() - 1);
        } else {
            holder.depthMarker.setVisibility(View.GONE);
        }
        holder.itemView.setPadding(paddingStart, 0, 0, 0);

        int colorIndex = comment.getDepth() % holder.colors.length;
        int depthMarkerColor = holder.colors[colorIndex];
        holder.depthMarker.setBackgroundColor(depthMarkerColor);
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

    public void addThread(RedditThread thread) {
        mThread = thread;
    }

    public void replaceData(List<RedditComment> newList) {
        mNumRecentlyLoaded = newList.size();
        mCommentsList.clear();
        mCommentsList.addAll(newList);
        notifyItemRangeInserted(mCommentsList.size() + 1, mNumRecentlyLoaded);
    }

    public void addData(List<RedditComment> moreComments) {
        mNumRecentlyLoaded = moreComments.size();
        mCommentsList.addAll(moreComments);
        notifyItemRangeInserted(mCommentsList.size() + 1, mNumRecentlyLoaded);
    }

    public void showLoading(boolean isLoading) {
        shouldShowLoading = isLoading;
        notifyItemChanged(mCommentsList.size() + 1);
    }

    private Spanned fromHtml(String htmlText) {
        String formatted = StringEscapeUtils.unescapeHtml4(htmlText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(formatted, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST | Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM);
        } else {
            return Html.fromHtml(formatted, null, new TagHandler());
        }
    }

    private CharSequence trim(CharSequence s, int start, int end) {
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    private class TagHandler implements Html.TagHandler {

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if (tag.equals("ul") && !opening) {
                output.append("\n");
            }

            if (tag.equals("li") && opening) {
                output.append("\n* ");
            }
        }
    }
}

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
import android.widget.ImageButton;
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
    private static final int VIEW_HEADER = R.layout.comments_header;
    private static final int VIEW_FOOTER = R.layout.comments_footer;
    private static final int VIEW_ITEM = R.layout.comments_list_item;

    private List<RedditComment> mComments;
    private OnLoadMoreItems mLoadMoreCallback;
    private RedditThread mThread;
    private Context mContext;
    private boolean showProgressBar;

    public ThreadCommentsAdapter(List<RedditComment> comments, OnLoadMoreItems callback, Context context) {
        mComments = comments;
        mLoadMoreCallback = callback;
        mContext = context;
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.title_view) TextView mTitleView;
        @Nullable
        @BindView(R.id.time_author_view) TextView mTimeAuthorView;
        @Nullable
        @BindView(R.id.image) ImageView mImageView;
        @Nullable
        @BindView(R.id.self_text_view) TextView mSelfTextView;
        @BindColor(R.color.white) int mDefaultColor;
        @BindColor(R.color.red_500) int mClickedColor;

        public ThreadViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void bind(RedditThread thread) {
            mTitleView.setText(thread.getTitle());
            mTitleView.setTextColor(mClickedColor);

            String timeAuthor = thread.getFormattedTime() + " by " + thread.getAuthor();
            mTimeAuthorView.setText(timeAuthor);

            String selftextHtml = thread.getSelftextHtml();
            if (TextUtils.isEmpty(selftextHtml)) {
                mSelfTextView.setVisibility(View.GONE);
            } else {
                mSelfTextView.setVisibility(View.VISIBLE);
                Spanned html = fromHtml(selftextHtml);
                mSelfTextView.setText(trim(html, 0, html.length()));
            }

            if (mImageView.getVisibility() == View.GONE) {
                mImageView.setVisibility(View.VISIBLE);
                Glide.with(mContext).load(thread.getMediaPath())
                        .fitCenter()
                        .crossFade()
                        .into(mImageView);
            }
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.depth_marker) View mDepthMarker;
        @Nullable
        @BindView(R.id.info_text_view) TextView mInfoView;
        @Nullable
        @BindView(R.id.comment_body_view) TextView mCommentBodyView;
        @Nullable
        @BindView(R.id.divider) View mDivider;
        @BindDimen(R.dimen.depth_marker_width) float mDepthMarkerWidth;
        @BindArray(R.array.depth_colors) int[] mColors;

        public CommentViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        public void bind(RedditComment comment, boolean showDivider) {
            String points = Integer.toString(comment.getScore()) + " points";
            String time = comment.getFormattedTime();
            String info = comment.getAuthor() + " \u2022 " + points + " \u2022 " + time;
            mInfoView.setText(info);

            String bodyHtml = comment.getBodyHtml();
            if (!TextUtils.isEmpty(bodyHtml)) {
                Spanned spanned = fromHtml(comment.getBodyHtml());
                mCommentBodyView.setText(trim(spanned, 0, spanned.length()));
            } else {
                mCommentBodyView.setText(bodyHtml);
            }

            if (showDivider) {
                mDivider.setVisibility(View.GONE);
            } else {
                mDivider.setVisibility(View.VISIBLE);
            }

            int paddingStart = 0;
            if (comment.getDepth() > 0) {
                mDepthMarker.setVisibility(View.VISIBLE);
                paddingStart = Math.round(mDepthMarkerWidth) * (comment.getDepth() - 1);
            } else {
                mDepthMarker.setVisibility(View.GONE);
            }
            itemView.setPadding(paddingStart, 0, 0, 0);

            int colorIndex = comment.getDepth() % mColors.length;
            int depthMarkerColor = mColors[colorIndex];
            mDepthMarker.setBackgroundColor(depthMarkerColor);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder {
        @Nullable
        @BindView(R.id.btn_load_more_comments) ImageButton mBtnLoadMore;
        @Nullable
        @BindView(R.id.progress_bar) ProgressBar mProgressBar;

        public FooterViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.btn_load_more_comments)
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
                if (mThread != null) {
                    ((ThreadViewHolder) holder).bind(mThread);
                }
                break;
            case VIEW_ITEM:
                ((CommentViewHolder) holder).bind(mComments.get(position - 1), (position == getItemCount()));
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
        //We added a header and a footer so we have 2 extra items aside
        //from our comments list
        return mComments.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_HEADER;
        } else if (position == mComments.size() + 1) {
            return VIEW_FOOTER;
        } else {
            return VIEW_ITEM;
        }
    }

    public void addThread(RedditThread thread) {
        mThread = thread;
        notifyItemChanged(0);
    }

    public void submitList(List<RedditComment> newList) {
        mComments.clear();
        mComments.addAll(newList);
        notifyItemRangeChanged(1, mComments.size());
    }

    public int getDataSize() {
        return mComments.size();
    }

    public void showLoading(boolean isLoading) {
        showProgressBar = isLoading;
        notifyItemChanged(mComments.size() + 1);
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

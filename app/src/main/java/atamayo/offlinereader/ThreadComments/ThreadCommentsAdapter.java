package atamayo.offlinereader.ThreadComments;

import android.graphics.Color;
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
import android.widget.TextView;

import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.XMLReader;

import java.util.List;

import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditComment;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import butterknife.BindArray;
import butterknife.BindColor;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadCommentsAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<RedditComment> mCommentsList;
    private LoadCommentsCallback mCallback;
    private RedditThread thread;
    private static final int VIEW_HEADER = R.layout.comments_header;
    private static final int VIEW_FOOTER = R.layout.comments_footer;
    private static final int VIEW_ITEM = R.layout.comments_list_item;

    public ThreadCommentsAdapter(List<RedditComment> comments, LoadCommentsCallback callback){
        mCommentsList = comments;
        mCallback = callback;
    }

    public class ThreadViewHolder extends RecyclerView.ViewHolder {
        @Nullable @BindView(R.id.title_view) TextView titleView;
        @Nullable @BindView(R.id.time_author_view) TextView timeAuthorView;
        @Nullable @BindView(R.id.self_text_view) TextView selftextView;
        @BindColor(R.color.thread_title_color) int defaultColor;
        @BindColor(R.color.thread_title_clicked_color) int clickedColor;

        public ThreadViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        @Nullable @BindView(R.id.depth_marker) View depthMarker;
        @Nullable @BindView(R.id.info_text_view) TextView infoView;
        @Nullable @BindView(R.id.comment_body_view) TextView commentBodyView;
        @Nullable @BindView(R.id.divider) View divider;
        @BindDimen(R.dimen.depth_marker_width) float depthMarkerWidth;
        @BindArray(R.array.depth_colors) int[] colors;

        public CommentViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    public class FooterViewHolder extends RecyclerView.ViewHolder{
        @Nullable @BindView(R.id.btn_load_more) Button btnLoadMore;

        public FooterViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        if(viewType == VIEW_HEADER){
            itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_HEADER, parent, false);
            return new ThreadViewHolder(itemView);
        }else if(viewType == VIEW_FOOTER){
            itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_FOOTER, parent, false);
            return new FooterViewHolder(itemView);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_ITEM, parent, false);
            return new CommentViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()){
            case VIEW_HEADER:
                ThreadViewHolder threadViewHolder = (ThreadViewHolder) holder;
                configureHeaderView(threadViewHolder);
                break;
            case VIEW_ITEM:
                CommentViewHolder commentViewHolder = (CommentViewHolder) holder;
                configureCommentViews(commentViewHolder, position);
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
        return mCommentsList.size() + 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position){
        if(position == 0){
            return VIEW_HEADER;
        }else if(position == mCommentsList.size()){
            return VIEW_FOOTER;
        }else {
            return VIEW_ITEM;
        }
    }

    private void configureHeaderView(ThreadViewHolder holder){
        String timeAuthor = thread.getFormattedTime() + " by " + thread.getAuthor();
        String selftextHtml = thread.getSelftextHtml();
        if(TextUtils.isEmpty(selftextHtml)){
            holder.selftextView.setVisibility(View.GONE);
        }else{
            holder.selftextView.setVisibility(View.VISIBLE);
            Spanned html = fromHtml(selftextHtml);
            holder.selftextView.setText(trim(html, 0, html.length()));
        }

        holder.titleView.setText(thread.getTitle());
        if(thread.getWasClicked()){
            holder.titleView.setTextColor(holder.clickedColor);
        }else{
            holder.titleView.setTextColor(holder.defaultColor);
        }

        holder.timeAuthorView.setText(timeAuthor);
    }

    private void configureCommentViews(CommentViewHolder holder, int position){
        RedditComment comment = mCommentsList.get(position - 1);
        String points = Integer.toString(comment.getScore()) + " points";
        String time = comment.getFormattedTime();
        String info = comment.getAuthor() + " " + points + " " + time;

        holder.infoView.setText(info);
        if(!comment.getBodyHtml().isEmpty()) {
            Spanned spanned = Html.fromHtml(StringEscapeUtils.unescapeHtml4(comment.getBodyHtml()));
            holder.commentBodyView.setText(trim(spanned, 0, spanned.length()));
        }

        if (position == getItemCount()) {
            holder.divider.setVisibility(View.GONE);
        } else {
            holder.divider.setVisibility(View.VISIBLE);
        }

        if (comment.getDepth() > 0) {
            holder.depthMarker.setVisibility(View.VISIBLE);
            int paddingStart = Math.round(holder.depthMarkerWidth) * (comment.getDepth() - 1);
            holder.itemView.setPadding(paddingStart, 0, 0, 0);
        } else {
            holder.itemView.setPadding(0, 0, 0, 0);
            holder.depthMarker.setVisibility(View.GONE);
        }

        int colorIndex = comment.getDepth() % holder.colors.length;
        int depthMarkerColor = holder.colors[colorIndex];
        holder.depthMarker.setBackgroundColor(depthMarkerColor);
    }

    private void configureFooterView(FooterViewHolder holder){
        holder.btnLoadMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.loadMore();
            }
        });
    }

    public void addThread(RedditThread thread){
        this.thread = thread;
    }

    public void replaceData(List<RedditComment> newList){
        mCommentsList.clear();
        mCommentsList.addAll(newList);
        notifyDataSetChanged();
    }

    public void addData(List<RedditComment> moreComments){
        mCommentsList.addAll(moreComments);
        notifyDataSetChanged();
    }

    private Spanned fromHtml(String htmlText){
        String formatted = StringEscapeUtils.unescapeHtml4(htmlText);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return Html.fromHtml(formatted, Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST | Html.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM);
        }else{
            return Html.fromHtml(formatted, null, new TagHandler());
        }
    }

    private CharSequence trim(CharSequence s, int start, int end){
        while (start < end && Character.isWhitespace(s.charAt(start))) {
            start++;
        }

        while (end > start && Character.isWhitespace(s.charAt(end - 1))) {
            end--;
        }

        return s.subSequence(start, end);
    }

    private class TagHandler implements Html.TagHandler{

        @Override
        public void handleTag(boolean opening, String tag, Editable output, XMLReader xmlReader) {
            if(tag.equals("ul") && !opening){
                output.append("\n");
            }

            if(tag.equals("li") && opening){
                output.append("\n* ");
            }
        }
    }
}

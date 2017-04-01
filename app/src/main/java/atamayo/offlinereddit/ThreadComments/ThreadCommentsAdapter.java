package atamayo.offlinereddit.ThreadComments;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;
import butterknife.BindArray;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Optional;

public class ThreadCommentsAdapter  extends RecyclerView.Adapter<ThreadCommentsAdapter.ViewHolder> {
    private List<RedditComment> mCommentsList;
    private LoadCommentsCallback mCallback;
    private static final int VIEW_FOOTER = R.layout.comments_footer;
    private static final int VIEW_ITEM = R.layout.comments_list_item;

    public ThreadCommentsAdapter(List<RedditComment> comments, LoadCommentsCallback callback){
        mCommentsList = comments;
        mCallback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @Nullable @BindView(R.id.depth_marker) View depthMarker;
        @Nullable @BindView(R.id.info_text_view) TextView infoView;
        @Nullable @BindView(R.id.comment_body_view) TextView commentBodyView;
        @Nullable @BindView(R.id.divider) View divider;
        @Nullable @BindView(R.id.btn_load_more) Button btnLoadMore;
        @BindDimen(R.dimen.depth_marker_width) float depthMarkerWidth;
        @BindArray(R.array.depth_colors) int[] colors;

        public ViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
        }

        @Optional
        @OnClick(R.id.btn_load_more)
        public void onLoadMoreClicked(){
            mCallback.loadMore();
        }
    }

    @Override
    public ThreadCommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;

        if(viewType == VIEW_ITEM){
            itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_ITEM, parent, false);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).inflate(VIEW_FOOTER, parent, false);
        }

        return new ThreadCommentsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ThreadCommentsAdapter.ViewHolder holder, int position) {
        if(position < mCommentsList.size()) {
            RedditComment comment = mCommentsList.get(position);
            String points = Integer.toString(comment.getScore()) + " points";
            String time = getTimeString(comment.getCreatedUTC());
            String info = comment.getAuthor() + " " + points + " " + time;

            holder.infoView.setText(info);
            holder.commentBodyView.setText(comment.getBody());

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
        }else{
            holder.btnLoadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.loadMore();
                }
            });
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
        return position == mCommentsList.size() ? VIEW_FOOTER : VIEW_ITEM;
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

    private String getTimeString(long commentPostTime){

        long seconds = (System.currentTimeMillis() / 1000) - commentPostTime;
        if(seconds < 60){
            return Long.toString(seconds) + " second(s) ago";
        }

        long minutes = seconds / 60;
        if(minutes < 60){
            return Long.toString(minutes) + " minute(s) ago";
        }

        long hours = minutes / 60;
        if(hours < 24){
            return Long.toString(hours) + " hour(s) ago";
        }

        long days = hours / 24;
        if(days < 7){
            return Long.toString(days) + " day(s) ago";
        }

        long weeks = days / 7;
        if(weeks < 4){
            return Long.toString(weeks) + " week(s) ago";
        }

        long months = weeks / 4;
        if(months < 12){
            return Long.toString(months) + " month(s) ago";
        }

        long years = months / 12;
        return Long.toString(years) + " year(s) ago";
    }
}

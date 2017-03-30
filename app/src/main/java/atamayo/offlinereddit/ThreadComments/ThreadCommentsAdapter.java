package atamayo.offlinereddit.ThreadComments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditModel.RedditComment;
import butterknife.BindArray;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ThreadCommentsAdapter  extends RecyclerView.Adapter<ThreadCommentsAdapter.ViewHolder> {
    private List<RedditComment> mCommentsList;

    public ThreadCommentsAdapter(List<RedditComment> comments){
        mCommentsList = comments;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.depth_marker) View depthMarker;
        @BindView(R.id.author_text_view) TextView authorView;
        @BindView(R.id.score_amount_view) TextView scoreView;
        @BindView(R.id.time_passed_view) TextView timeView;
        @BindView(R.id.comment_body_view) TextView commentBodyView;
        @BindView(R.id.divider) View divider;
        @BindDimen(R.dimen.depth_marker_width) float depthMarkerWidth;
        @BindArray(R.array.depth_colors) int[] colors;

        public ViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
        }
    }

    @Override
    public ThreadCommentsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comments_list_item, null);
        return new ThreadCommentsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ThreadCommentsAdapter.ViewHolder holder, int position) {
        RedditComment comment = mCommentsList.get(position);
        String points = Integer.toString(comment.getScore()) + " points";
        String time = getTimeString(comment.getCreatedUTC());

        if(comment.getDepth() > 0){
            holder.depthMarker.setVisibility(View.VISIBLE);
            int paddingStart = Math.round(holder.depthMarkerWidth) * (comment.getDepth() - 1);
            holder.itemView.setPadding(paddingStart, 0, 0, 0);
        }else{
            holder.depthMarker.setVisibility(View.GONE);
        }

        int colorIndex = comment.getDepth() % holder.colors.length;
        int depthMarkerColor = holder.colors[colorIndex];

        holder.depthMarker.setBackgroundColor(depthMarkerColor);
        holder.authorView.setText(comment.getAuthor());
        holder.scoreView.setText(points);
        holder.timeView.setText(time);
        holder.commentBodyView.setText(comment.getBody());

        if(position == getItemCount()){
            holder.divider.setVisibility(View.GONE);
        }else{
            holder.divider.setVisibility(View.VISIBLE);
        }
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

    @Override
    public int getItemCount() {
        return mCommentsList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void replaceData(List<RedditComment> newList){
        mCommentsList.clear();
        mCommentsList.addAll(newList);
        notifyDataSetChanged();
    }
}
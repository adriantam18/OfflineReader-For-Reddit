package atamayo.offlinereader.SubThreads;

import android.os.Build;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.RedditThread;
import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubThreadsAdapter extends RecyclerView.Adapter<SubThreadsAdapter.ViewHolder>
    implements ItemTouchHelperAdapter{
    private List<RedditThread> mThreadDataList;
    private ThreadListCallbacks mThreadListCallbacks;

    public SubThreadsAdapter(List<RedditThread> threadDataList, ThreadListCallbacks callbacks){
        mThreadDataList = threadDataList;
        mThreadListCallbacks = callbacks;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        @BindView(R.id.thread_card) CardView cardView;
        @BindView(R.id.score) TextView scoreView;
        @BindView(R.id.thread_title) TextView titleView;
        @BindView(R.id.num_comments) TextView numCommentsView;
        @BindView(R.id.author_and_time) TextView authorView;
        @BindColor(R.color.thread_title_color) int defaultColor;
        @BindColor(R.color.thread_title_clicked_color) int clickedColor;

        public ViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            mThreadListCallbacks.OnOpenCommentsPage(mThreadDataList.get(getAdapterPosition()));
        }
    }

    @Override
    public void onItemDismiss(int position){
        mThreadListCallbacks.OnDeleteThread(mThreadDataList.get(position));
    }

    @Override
    public SubThreadsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.sub_threads_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SubThreadsAdapter.ViewHolder holder, int position) {
        final RedditThread threadData = mThreadDataList.get(position);

        String authorAndTime = threadData.getAuthor() + " \u2022 " + threadData.getFormattedTime();
        String numComments = String.valueOf(threadData.getNumComments()) + " comments";
        String score;
        if(threadData.getScore() >= 10000){
            score = String.valueOf(threadData.getScore() / 1000) + "k";
        }else{
            score = String.valueOf(threadData.getScore());
        }

        holder.scoreView.setText(score);
        holder.titleView.setText(threadData.getTitle());
        holder.numCommentsView.setText(numComments);
        holder.authorView.setText(authorAndTime);

        if(threadData.getWasClicked()){
            holder.titleView.setTextColor(holder.clickedColor);
        }else {
            holder.titleView.setTextColor(holder.defaultColor);
        }
    }

    @Override
    public int getItemCount() {
        return mThreadDataList.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView){
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void replaceData(List<RedditThread> newList){
        mThreadDataList.clear();
        mThreadDataList.addAll(newList);
        notifyDataSetChanged();
    }
}

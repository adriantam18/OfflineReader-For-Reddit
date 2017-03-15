package atamayo.offlinereddit.SubThreads;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditThread;
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
        @BindView(R.id.label_comments) TextView commentsView;
        @BindView(R.id.num_comments) TextView numCommentsView;

        public ViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            mThreadListCallbacks.OnOpenCommentsPage(getAdapterPosition());
        }
    }

    @Override
    public void onItemDismiss(int position){
        mThreadListCallbacks.OnDeleteThread(position);
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

        holder.scoreView.setText(Integer.toString(threadData.getScore()));
        holder.titleView.setText(threadData.getTitle());
        holder.numCommentsView.setText(Integer.toString(threadData.getNumComments()));

        if(threadData.getWasClicked()){
            holder.titleView.setTextColor(Color.parseColor("#FF2626"));
        }else {
            holder.titleView.setTextColor(Color.WHITE);
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
        mThreadDataList = newList;
        notifyDataSetChanged();
    }
}
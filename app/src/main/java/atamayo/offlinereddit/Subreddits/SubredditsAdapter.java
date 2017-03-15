package atamayo.offlinereddit.Subreddits;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.Subreddit;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubredditsAdapter extends RecyclerView.Adapter<SubredditsAdapter.ViewHolder> {
    private List<Subreddit> mSubredditsList;
    private SubListCallbacks mSubListCallbacks;

    public SubredditsAdapter(List<Subreddit> subreddits, SubListCallbacks callbacks){
        mSubredditsList = subreddits;
        mSubListCallbacks = callbacks;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.subreddit_name) TextView mSubredditsName;
        @BindView(R.id.remove_subreddit) ImageButton mRemoveSubreddit;

        public ViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.remove_subreddit)
        public void removeSubreddit(View v){
            mSubListCallbacks.OnDeleteSubreddit(getAdapterPosition());
        }

        @Override
        public void onClick(View v) {
            mSubListCallbacks.OnOpenListOfThreads(getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            mSubListCallbacks.OnOpenListOfKeywords(getAdapterPosition());
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subreddits_list_item, null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.mSubredditsName.setText(mSubredditsList.get(position).getDisplayName());
    }

    @Override
    public int getItemCount() {
        return mSubredditsList.size();
    }

    public void replaceData(List<Subreddit> newList){
        mSubredditsList = newList;
        notifyDataSetChanged();
    }
}

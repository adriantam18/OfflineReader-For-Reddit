package atamayo.offlinereader.Subreddits;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubredditsAdapter extends RecyclerView.Adapter<SubredditsAdapter.ViewHolder>
        implements Filterable {
    private List<Subreddit> mOriginalList = null;
    private List<Subreddit> mFilteredList = null;
    private SubListCallbacks mSubListCallbacks;

    public SubredditsAdapter(List<Subreddit> subreddits, SubListCallbacks callbacks) {
        mOriginalList = subreddits;
        mFilteredList = new ArrayList<>(mOriginalList);
        mSubListCallbacks = callbacks;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {
        @BindView(R.id.nsfw_marker)
        TextView mNSFWMarker;
        @BindView(R.id.subreddit_name)
        TextView mSubredditsName;
        @BindView(R.id.remove_subreddit)
        ImageButton mRemoveSubreddit;

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.remove_subreddit)
        public void removeSubreddit(View v) {
            mSubListCallbacks.OnDeleteSubreddit(mFilteredList.get(getAdapterPosition()));
        }

        @Override
        public void onClick(View v) {
            mSubListCallbacks.OnOpenListOfThreads(mFilteredList.get(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            mSubListCallbacks.OnOpenListOfKeywords(mFilteredList.get(getAdapterPosition()));
            return true;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subreddits_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mFilteredList.get(position).getOver18()) {
            holder.mNSFWMarker.setVisibility(View.VISIBLE);
        } else {
            holder.mNSFWMarker.setVisibility(View.GONE);
        }
        holder.mSubredditsName.setText(mFilteredList.get(position).getDisplayNamePrefixed());
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                ArrayList<Subreddit> filteredArray = new ArrayList<>();

                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < mOriginalList.size(); i++) {
                    String subName = mOriginalList.get(i).getDisplayName();
                    if (subName.toLowerCase().startsWith(constraint.toString())) {
                        filteredArray.add(mOriginalList.get(i));
                    }
                }

                filterResults.count = filteredArray.size();
                filterResults.values = filteredArray;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mFilteredList = (List<Subreddit>) results.values;
                notifyDataSetChanged();
            }
        };

        return filter;
    }

    public void replaceData(List<Subreddit> newList) {
        mFilteredList.clear();
        mFilteredList.addAll(newList);
        mOriginalList.clear();
        mOriginalList.addAll(newList);
        notifyDataSetChanged();
    }

    public void addData(Subreddit subreddit, int position) {
        mOriginalList.add(position, subreddit);
        mFilteredList.add(position, subreddit);
        notifyDataSetChanged();
    }

    public void clearData() {
        mOriginalList.clear();
        mFilteredList.clear();
        notifyDataSetChanged();
    }

    public List<Subreddit> getData() {
        return mFilteredList;
    }

    public List<String> getSubsToDownload() {
        List<String> subsToDownload = new ArrayList<>();
        for (Subreddit subreddit : mFilteredList) {
            subsToDownload.add(subreddit.getDisplayName());
        }

        return subsToDownload;
    }
}

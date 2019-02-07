package atamayo.offlinereader.Subreddits;

import android.support.annotation.NonNull;
import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import atamayo.offlinereader.R;
import atamayo.offlinereader.RedditAPI.RedditModel.Subreddit;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubredditsAdapter extends ListAdapter<Subreddit, SubredditsAdapter.ViewHolder> {
    private SubListCallbacks mSubListCallbacks;

    public static final DiffUtil.ItemCallback<Subreddit> DIFF_CALLBACK = new DiffUtil.ItemCallback<Subreddit>() {
        @Override
        public boolean areItemsTheSame(Subreddit oldItem, Subreddit newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(Subreddit oldItem, Subreddit newItem) {
            try {
                return (oldItem.getId() == newItem.getId())
                        && (oldItem.getDisplayName().equals(newItem.getDisplayName()))
                        && (oldItem.getDisplayNamePrefixed().equals(newItem.getDisplayNamePrefixed()))
                        && (oldItem.getOver18() == newItem.getOver18());
            } catch (NullPointerException e) {
                return false;
            }
        }
    };

    public SubredditsAdapter(SubListCallbacks callbacks) {
        super(DIFF_CALLBACK);
        mSubListCallbacks = callbacks;
    }

    public class ViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener,
                    View.OnLongClickListener {
        @BindView(R.id.nsfw_marker) TextView mNSFWMarker;
        @BindView(R.id.subreddit_name) TextView mSubredditsName;
        @BindView(R.id.remove_subreddit) ImageButton mRemoveSubreddit;

        public ViewHolder(View view) {
            super(view);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.remove_subreddit)
        public void removeSubreddit(View v) {
            mSubListCallbacks.OnDeleteSubreddit(getItem(getAdapterPosition()));
        }

        @Override
        public void onClick(View v) {
            mSubListCallbacks.OnOpenListOfThreads(getItem(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            mSubListCallbacks.OnOpenListOfKeywords(getItem(getAdapterPosition()));
            return true;
        }

        public void bind(Subreddit subreddit) {
            if (subreddit.getOver18()) {
                mNSFWMarker.setVisibility(View.VISIBLE);
            } else {
                mNSFWMarker.setVisibility(View.GONE);
            }
            mSubredditsName.setText(subreddit.getDisplayNamePrefixed());
        }
    }

    @NonNull
    @Override
    public SubredditsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subreddits_list_item, parent, false);
        return new SubredditsAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SubredditsAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    @Override
    public void submitList(List<Subreddit> newList) {
        super.submitList(newList);
    }

    public List<String> getSubsToDownload() {
        List<String> subsToDownload = new ArrayList<>();

        for (int i = 0; i < getItemCount(); i++) {
            Subreddit subreddit = getItem(i);
            subsToDownload.add(subreddit.getDisplayName());
        }

        return subsToDownload;
    }
}

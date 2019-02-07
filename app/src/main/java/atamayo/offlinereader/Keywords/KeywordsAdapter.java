package atamayo.offlinereader.Keywords;

import android.support.v7.recyclerview.extensions.ListAdapter;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import atamayo.offlinereader.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeywordsAdapter extends ListAdapter<String, KeywordsAdapter.ViewHolder> {
    private KeywordsListCallback mCallback;

    public static final DiffUtil.ItemCallback<String> DIFF_CALLBACK = new DiffUtil.ItemCallback<String>() {
        @Override
        public boolean areItemsTheSame(String oldItem, String newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areContentsTheSame(String oldItem, String newItem) {
            return oldItem.equals(newItem);
        }
    };

    public KeywordsAdapter(KeywordsListCallback callback) {
        super(DIFF_CALLBACK);
        mCallback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.keyword) TextView mKeyword;
        @BindView(R.id.btn_remove_keyword) ImageButton mRemoveKeyword;

        public ViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.btn_remove_keyword)
        public void removeKeyword(View view) {
            mCallback.OnDeleteKeyword(getItem(getAdapterPosition()));
        }

        public void bind(String keyword) {
            mKeyword.setText(keyword);
        }
    }

    public KeywordsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.keywords_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(KeywordsAdapter.ViewHolder holder, int position) {
        holder.bind(getItem(position));
    }
}

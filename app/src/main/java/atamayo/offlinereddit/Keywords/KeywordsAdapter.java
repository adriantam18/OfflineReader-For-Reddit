package atamayo.offlinereddit.Keywords;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import atamayo.offlinereddit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class KeywordsAdapter extends RecyclerView.Adapter<KeywordsAdapter.ViewHolder> {
    private List<String> mKeywords;
    private KeywordsListCallback mCallback;

    public KeywordsAdapter(List<String> keywords, KeywordsListCallback callback) {
        mKeywords = keywords;
        mCallback = callback;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.keyword) TextView mKeyword;
        @BindView(R.id.btn_remove_keyword) ImageButton mRemoveKeyword;

        public ViewHolder(View view){
            super(view);

            ButterKnife.bind(this, view);
        }

        @OnClick(R.id.btn_remove_keyword)
        public void removeKeyword(View view){
            mCallback.OnDeleteKeyword(getAdapterPosition());
        }
    }

    public KeywordsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.keywords_list_item, null);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(KeywordsAdapter.ViewHolder holder, int position) {
        holder.mKeyword.setText(mKeywords.get(position));
    }

    @Override
    public int getItemCount() {
        return mKeywords.size();
    }

    public void replaceData(List<String> keywords){
        mKeywords = keywords;
        notifyDataSetChanged();
    }
}

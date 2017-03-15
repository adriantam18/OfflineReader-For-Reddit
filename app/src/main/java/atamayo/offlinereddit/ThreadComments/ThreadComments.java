package atamayo.offlinereddit.ThreadComments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import atamayo.offlinereddit.App;
import atamayo.offlinereddit.Data.SubredditsDataSource;
import atamayo.offlinereddit.Data.SubredditsRepository;
import atamayo.offlinereddit.R;
import atamayo.offlinereddit.RedditAPI.RedditThread;
import atamayo.offlinereddit.RedditDAO.DaoSession;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ThreadComments extends Fragment implements ThreadCommentsContract.View {
    @BindView(R.id.comments_view) WebView mWebView;
    ThreadCommentsContract.Presenter mPresenter;
    Unbinder unbinder;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        DaoSession daoSession = ((App) getActivity().getApplication()).getDaoSession();
        SubredditsDataSource repository = new SubredditsRepository(daoSession.getRedditThreadDao(), daoSession.getSubredditDao());
        mPresenter = new ThreadCommentsPresenter(repository, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstancestate){
        View view = inflater.inflate(R.layout.fragment_thread_comments, container, false);

        unbinder = ButterKnife.bind(this, view);
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.setWebViewClient(new WebViewClient());

        Bundle bundle = getArguments();
        String filename = bundle.getString("filename");
        mPresenter.initCommentsView(filename);

        return view;
    }

    @Override
    public void onDestroyView(){
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void showCommentsFromFile(String filename) {
        try {
            FileInputStream threadLocalFile = getActivity().openFileInput(filename);
            BufferedReader reader = new BufferedReader(new InputStreamReader(threadLocalFile));
            StringBuilder html_builder = new StringBuilder();
            String contents;
            while ((contents = reader.readLine()) != null) {
                html_builder.append(contents);
            }
            threadLocalFile.close();
            String html = html_builder.toString();
            mWebView.loadData(html, "text/html; charset=utf-8", null);
        }catch (IOException e){
            Log.e("Webview", e.toString());
        }
    }

    @Override
    public void showCommentsFromUrl(String url) {

    }

    @Override
    public void setPresenter(ThreadCommentsContract.Presenter presenter) {
        mPresenter = presenter;
    }

}

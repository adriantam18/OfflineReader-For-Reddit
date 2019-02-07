package atamayo.offlinereader;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import atamayo.offlinereader.Data.FileManager;
import atamayo.offlinereader.Data.RedditDatabase;
import atamayo.offlinereader.Data.SubredditsRepository;

public class App extends Application {

    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;
    private FileManager fileManager;

    @Override
    public void onCreate(){
        super.onCreate();

        if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }

        refWatcher = LeakCanary.install(this);
    }

    public RedditDatabase getDatabase() {
        return RedditDatabase.getDatabase(this);
    }

    public FileManager getFileManager() {
        if (fileManager == null) {
            fileManager = new FileManager(this);
        }

        return fileManager;
    }

    public SubredditsRepository getSubredditsRepository() {
        return SubredditsRepository.getInstance(getDatabase(), getFileManager());
    }
}

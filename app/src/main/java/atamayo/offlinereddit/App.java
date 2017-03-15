package atamayo.offlinereddit;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import org.greenrobot.greendao.database.Database;

import atamayo.offlinereddit.RedditDAO.DaoMaster;
import atamayo.offlinereddit.RedditDAO.DaoSession;

public class App extends Application {
    private DaoSession daoSession;
    private DaoMaster daoMaster;

    public static RefWatcher getRefWatcher(Context context) {
        App application = (App) context.getApplicationContext();
        return application.refWatcher;
    }

    private RefWatcher refWatcher;

    @Override
    public void onCreate(){
        super.onCreate();

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "subreddits-db");
        Database db = helper.getWritableDb();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        if(LeakCanary.isInAnalyzerProcess(this)){
            return;
        }

        refWatcher = LeakCanary.install(this);
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }
}

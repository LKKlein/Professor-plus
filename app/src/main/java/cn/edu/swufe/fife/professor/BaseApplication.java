package cn.edu.swufe.fife.professor;

import android.app.Application;
import android.util.Log;

import com.mob.MobSDK;
import com.tencent.smtt.sdk.QbSdk;

import org.greenrobot.greendao.database.Database;

import cn.edu.swufe.fife.professor.bean.DaoMaster;
import cn.edu.swufe.fife.professor.bean.DaoSession;

public class BaseApplication extends Application {
    private DaoSession daoSession;

    @Override
    public void onCreate() {
        super.onCreate();

        //搜集本地tbs内核信息并上报服务器，服务器返回结果决定使用哪个内核。
        QbSdk.PreInitCallback cb = new QbSdk.PreInitCallback() {

            @Override
            public void onViewInitFinished(boolean arg0) {
                Log.d("app", " onViewInitFinished is " + arg0);
            }

            @Override
            public void onCoreInitFinished() {
            }
        };
        QbSdk.initX5Environment(this, cb);

        MobSDK.init(this, "20408462ccdb8", "3d10dcb298685b1a44a665bfa3e31239");

        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(this, "professor.db");
        Database db = helper.getWritableDb();
        daoSession = new DaoMaster(db).newSession();
    }

    public DaoSession getDaoSession() {
        return daoSession;
    }
}
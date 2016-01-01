
package cn.com.nd.momo.activity;

import android.app.Application;
import android.content.Context;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.MyDatabaseHelper;

import com.flurry.android.FlurryAgent;

public class MyApplication extends Application {

    protected static final String TAG = "MyApplication";

    private static MyApplication sMyApp = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        sMyApp = this;

        MyDatabaseHelper.initDatabase(getApplicationContext());
        Context context = getApplicationContext();

        GlobalUserInfo.setAppContext(getApplicationContext());
        Utils.saveGlobleContext(context);
        GlobalUserInfo.checkLoginStatus(context);
        FlurryAgent.onStartSession(this, GlobalUserInfo.FLURRY_STRING);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        MyDatabaseHelper.getInstance().close();
        FlurryAgent.onEndSession(this);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    synchronized public static MyApplication getApplication() {
        return sMyApp;
    }
}

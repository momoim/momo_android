
package cn.com.nd.momo.activity;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.MyDatabaseHelper;

import com.android.mms.data.RecipientIdCache;
import com.android.mms.layout.LayoutManager;
import com.android.mms.util.DownloadManager;
import com.android.mms.util.DraftCache;
import com.flurry.android.FlurryAgent;

public class MyApplication extends Application {

    protected static final String TAG = "MyApplication";

    private static MyApplication sMyApp = null;

    private TelephonyManager mTelephonyManager;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        sMyApp = this;

        MyDatabaseHelper.initDatabase(getApplicationContext());
        Context context = getApplicationContext();

        // init global class
        DownloadManager.init(context);
        RecipientIdCache.init(context);
        DraftCache.init(context);
        DraftCache.registerContentObserver(context);

        LayoutManager.init(context);
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

    /**
     * @return Return the TelephonyManager.
     */
    public TelephonyManager getTelephonyManager() {
        if (mTelephonyManager == null) {
            mTelephonyManager = (TelephonyManager)getApplicationContext()
                    .getSystemService(Context.TELEPHONY_SERVICE);
        }
        return mTelephonyManager;
    }

}

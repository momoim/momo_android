
package cn.com.nd.momo.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import cn.com.nd.momo.activity.ForceUpgradeActivity;
import cn.com.nd.momo.activity.MainActivity;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.UpgradeInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;

public class UpgradeMgr {

    private static final String TAG = "UpgradeMgr";

    private static UpgradeMgr mInstance = null;

    public static UpgradeMgr GetInstance() {
        if (mInstance == null) {
            mInstance = new UpgradeMgr();
        }

        return mInstance;
    }

    public UpgradeInfo postUpgrade(String version) {
        try {
            return MoMoHttpApi.getUpgradeInfo(version);
        } catch (MoMoException e) {
            Log.i(TAG, "" + e.toString());
        }
        return null;
    }

    public static void down(Context context, String url) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(i);
    }

    private static boolean isForceUpgrade(String uid, String currentVersion) {
        try {
            MoMoHttpApi.getUserCardByID(Long.valueOf(GlobalUserInfo.getUID()));
        } catch (MoMoException e) {
            if (e.getCode() == 412) {
                return true;
            }
        }
        return false;
    }

    public static void checkUpgradeThread(final Activity activity, final Handler handler,
            final String uid, final String currentVersion) {
        new Thread() {
            @Override
            public void run() {
                if (isForceUpgrade(uid, currentVersion)) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            activity
                                    .startActivity(new Intent(activity, ForceUpgradeActivity.class));
                            activity.finish();
                        }
                    });
                } else {
                    String skipVersion = ConfigHelper.getInstance(activity).loadKey(
                            ConfigHelper.CONFIG_KEY_SKIP_VERSION);
                    final UpgradeInfo uinfo = UpgradeMgr.GetInstance().postUpgrade(currentVersion);
                    // if exit new verstion
                    if (uinfo != null
                            && (uinfo.currentVersion != null && !uinfo.currentVersion
                                    .equals(skipVersion))) {
                        Message msg = new Message();
                        msg.what = MainActivity.MSG_SHOW_UPGRADE_DIALOG;
                        msg.obj = uinfo;
                        handler.sendMessage(msg);
                    }
                }
            }
        }.start();

    }
}


package cn.com.nd.momo.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.LoginSearchSMSThread;

import com.flurry.android.FlurryAgent;

/**
 * 启动初始activity
 * 
 * @author jiaolei
 */
public class LauncherActivity extends Activity {
    private String TAG = "LauncherActivity";

    private Bundle mBundle = null;

    private boolean mIsCalledFrom91U = false;

    private String m91USessionId = "";

    private AsyncTask<Void, Void, Intent> mTask = new AsyncTask<Void, Void, Intent>() {
        protected Intent doInBackground(Void... params) {
            Intent intent = null;
            try {
                OAuthInfo oauthInfo = MoMoHttpApi.checkOap(m91USessionId);
                if (oauthInfo.isBindedMobile()) {
                    OAuthInfo finalOauthInfo = MoMoHttpApi.tokenLogin(m91USessionId,
                            oauthInfo.getUid(), oauthInfo.getFinalKey());
                    // tokenLogin自动登录完信息保存
                    String finalKey = oauthInfo.getFinalKey();
                    String finalSecret = oauthInfo.getFinalSecret();
                    GlobalUserInfo.setOAuthToken(finalOauthInfo.getUid(), finalKey,
                            finalSecret, finalOauthInfo.getUserName(),
                            finalOauthInfo.getAvatarName(), finalOauthInfo.getQueueName(),
                            finalOauthInfo.getStatus(), finalOauthInfo.getZoneCode(),
                            finalOauthInfo.getMobile());
                    // set login status
                    ConfigHelper configHelper = ConfigHelper.getInstance(getApplicationContext());
                    GlobalUserInfo.setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
                    configHelper.saveKey(ConfigHelper.CONFIG_SID, m91USessionId);
                    configHelper.saveKey(ConfigHelper.CONFIG_KEY_SYNC_MODE,
                            ConfigHelper.SYNC_MODE_LOCAL_ONLY);

                    intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.putExtras(mBundle);
                } else {
                    intent = new Intent(getApplicationContext(), BindMobileActivity.class);
                    intent.putExtras(mBundle);
                }
            } catch (MoMoException e) {
                e.printStackTrace();
                intent = new Intent(getApplicationContext(), LoginPreActivity.class);
            }

            return intent;
        }

        @Override
        protected void onPostExecute(Intent intent) {
            if (intent == null) {
                intent = new Intent(getApplicationContext(), LoginPreActivity.class);
            }
            startActivity(intent);
            finish();
        }
    };

    private Handler hHandle = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LoginSearchSMSThread.HTTP_LOGIN_FOR_URL_SUCCESS:
                    Bundle b = msg.getData();
                    Intent i = new Intent(LauncherActivity.this, LauncherSelectActivity.class);
                    i.putExtras(b);
                    startActivity(i);
                    finish();
                    break;

                case LoginSearchSMSThread.HTTP_LOGIN_FOR_URL_FAILED:
                    startApp();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.welcome);

        FlurryAgent.logEvent("momo启动");

        // 这段代码只在第一次安装的时候执行一次
        if (isFirstRun()) {
            installShortCut();
        }

        // 获取调用参数
        Intent intent = getIntent();
        if (intent != null) {
            mBundle = intent.getExtras();
            if (mBundle != null) {
                String param = mBundle.getString("callFrom91U");
                if (param != null && !param.equals("")) {
                    try {
                        JSONObject json = new JSONObject(param);
                        mIsCalledFrom91U = true;
                        m91USessionId = json.getString("sid");
                    } catch (JSONException e) {
                        // 91U调用参数错误，不做特殊处理
                        e.printStackTrace();
                    }
                }
            }
        }

        if (Utils.getActiveNetWorkName(this) != null && !mIsCalledFrom91U
                && !GlobalUserInfo.hasLogined()) {
            LoginSearchSMSThread rt = new LoginSearchSMSThread(this, hHandle, false);
            rt.start();
        } else {
            startApp();
        }
    }

    private boolean isFirstRun() {
        return ConfigHelper.getInstance(this).loadBooleanKey(ConfigHelper.CONFIG_KEY_IS_FIRST_RUN,
                true);
    }

    private void installShortCut() {
        Intent shortcutIntent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.app_name));
        // 是否可以有多个快捷方式的副本，参数如果是true就可以生成多个快捷方式，如果是false就不会重复添加
        shortcutIntent.putExtra("duplicate", false);
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mainIntent.setClass(this, this.getClass());

        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, mainIntent);
        shortcutIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource
                .fromContext(this, R.drawable.ts_icon));
        sendBroadcast(shortcutIntent);
        ConfigHelper.getInstance(this).saveBooleanKey(ConfigHelper.CONFIG_KEY_IS_FIRST_RUN, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        startApp();
    }

    private void startApp() {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                Intent i = null;
                GlobalUserInfo.checkLoginStatus(getApplicationContext());
                ConfigHelper configHelper = ConfigHelper.getInstance(getApplicationContext());
                String strSyncMode = configHelper.loadKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
                String sid = configHelper.loadKey(ConfigHelper.CONFIG_SID);
                boolean importAccounts = configHelper.loadBooleanKey(
                        ConfigHelper.CONFIG_KEY_IMPORT_ACCOUNTS, false);
                // 判断是否从91U过来
                if (mIsCalledFrom91U) {

                    // TODO zgx 1 判断是否已经登录

                    // TODO zgx 1.1 已经登录，判断sid和本地保存的是否相同
                    // TODO zgx 1.1.1 sid相同，打开相应页面
                    // TODO zgx 1.1.2 sid不同，清空数据，执行1.2

                    // TODO zgx 1.2 未登录，调用MoMoHttpApi.checkOap接口
                    // TODO zgx 1.2.1 返回的 OAuthInfo.mIsBindedMobile=
                    // True，表明已经绑定手机号，执行tokenLogin接口完成自动登录
                    // TODO zgx 1.2.2 返回的 OAuthInfo.mIsBindedMobile=
                    // False，表明已经绑定手机号，执行绑定手机号流程（类似注册）
                    // TODO zgx 1.2... 登录或绑定成功后，除保存验证信息外，还需保存sid，并打开相应页面

                    if (GlobalUserInfo.hasLogined()) {
//                        if (sid.equals(m91USessionId)) {
                            i = new Intent(getApplicationContext(), MainActivity.class);
                            i.putExtras(mBundle);
                            startActivity(i);
                            finish();
                            return;
//                        } else {
//                            GlobalUserInfo.logout(getApplicationContext());
//                        }
                    }
                    mTask.execute();
                } else {
                    if (!GlobalUserInfo.hasLogined()) {
                        Log.d(TAG, "login called");
                        i = new Intent(getApplicationContext(), LoginPreActivity.class);
                    } else if (GlobalUserInfo.getUserStatus() < GlobalUserInfo.STATUS_VERIFY_USER) {
                        i = new Intent(getApplicationContext(), RegInfoActivity.class);
                    } else if ("".equals(strSyncMode)) {
                        // clear momo database
                        SyncContactApi.getInstance(getApplicationContext())
                                .deleteMoMoDatabaseContacts();
                        // start lead to sync activity
                        i = new Intent(getApplicationContext(), LeadToSyncActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    } else if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(strSyncMode)
                            && !importAccounts) {
                        // clear momo database
                        SyncContactApi.getInstance(getApplicationContext())
                                .deleteMoMoDatabaseContacts();
                        // start lead to import account
                        i = new Intent(getApplicationContext(), AccountsBindActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    } 
                    /*
                    else if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(strSyncMode)
                            && !Utils.isBindedAccountExist(Utils.getCurrentAccount())) {
                        Utils.resetAccount(LauncherActivity.this);
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        i.putExtra(MainActivity.DELETE_MOMO_DB, true);
                    } 
                    */
                    else {
                        i = new Intent(getApplicationContext(), MainActivity.class);
                    }
                    startActivity(i);
                    Log.d(TAG, "launcher end");
                    finish();
                }
            }
        };

        View welcomeContainer = this.findViewById(R.id.welcome_container);
        welcomeContainer.post(r);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.welcome_container));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup)view).getChildAt(i));
            }
            ((ViewGroup)view).removeAllViews();
        }
    }

}

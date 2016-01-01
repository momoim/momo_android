
package cn.com.nd.momo.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.guide.GuideActivity;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.types.UpgradeInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.CardManager;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.RobotManager;
import cn.com.nd.momo.manager.UpgradeMgr;
import cn.com.nd.momo.mention.model.MentionInfo;


public class MainActivity extends ActivityGroup {
    private static final String TAG = "MainActivity";

    private static final int CODE_RET = 1;

    public final int MSG_SMS_COUNT = 100;

    public static final int MSG_SHOW_UPGRADE_DIALOG = 101;

    private TabHost mTabHost;

    public static String TAB_CONTACTS = "contacts";

    public static String TAB_IM = "im";

    public static String TAB_DYNAMIC = "dynamic";

    public static String TAG_OPTION = "option";

    public static String ALL_ACCOUNT_NAME;

    public static String MOBILE_ACCOUNT_NAME;

    public static String DELETE_MOMO_DB = "delete_momo_db";

    // this is for tab toggle status
    public static int TAB_NORMAL = 1;

    public static int TAB_TOGGLED = 2;

    private boolean mHasInited = false;

    private AlertDialog mUpgradeDialog = null;

    private int mUnReadMentionCount;

    ConfigHelper ch = ConfigHelper.getInstance(MainActivity.this);

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        Log.d(TAG, "intent action:" + intent.getAction());
        if (intent.getAction() != null
                && intent.getAction().equals(getString(R.string.action_message_income))) {
            ConfigHelper ch = ConfigHelper.getInstance(getApplicationContext());
            String strSyncMode = ch.loadKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
            boolean importAccounts = ch.loadBooleanKey(ConfigHelper.CONFIG_KEY_IMPORT_ACCOUNTS,
                    false);
            if (GlobalUserInfo.getUserStatus() < GlobalUserInfo.STATUS_VERIFY_USER) {
                Intent i = new Intent(this, RegInfoActivity.class);
                startActivity(i);
                finish();
                return;
            } else if ("".equals(strSyncMode)) {
                Intent i = new Intent(this, LeadToSyncActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            } else if ((ConfigHelper.SYNC_MODE_TWO_WAY.equals(strSyncMode) && (!Utils
                    .isBindedAccountExist(Utils.getCurrentAccount()) || !importAccounts))) {
                // 非体验者用户，并且未选择同步帐号跳转至引导同步页面
                Intent i = new Intent(this, AccountsBindActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);

        // 检查升级
        checkUpgrade();

        int tabIndex = ConfigHelper.getInstance(this).loadIntKey(ConfigHelper.CONFIG_KEY_LAST_TAB, 0);

        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                String param = bundle.getString("callFrom91U");
                if (param != null && !param.equals("")) {
                    try {
                        JSONObject json = new JSONObject(param);
                        // 目前支持3个页面调用 私聊 "im"，联系人 "contact"，分享 "statuses"
                        String func = json.getString("func");
                        if (func.equalsIgnoreCase("im")) {
                            tabIndex = 0;
                        } else if (func.equalsIgnoreCase("contact")) {
                            tabIndex = 1;
                        } else if (func.equalsIgnoreCase("statuses")) {
                            tabIndex = 2;
                        }
                    } catch (JSONException e) {
                        // 91U调用参数错误，不做特殊处理
                        e.printStackTrace();
                    }
                }
            }
        }

        // 初始视图
        initActivity(tabIndex);

        Log.d(TAG, "step4: init MQ service");

        // if not logined, show login activity
        if (!GlobalUserInfo.hasLogined()) {
            Log.d(TAG, "login called");
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        } else {
            sendBroadcast(new Intent(getString(R.string.action_message_launch)));
        }

        // 启动时刷新应用机器人列表
        RobotManager.getInstance(this.getApplicationContext()).reloadRobotList();

        Log.d(TAG, "On Create end");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // // make sure service is running
        // sendBroadcast(new Intent(getString(R.string.action_message_launch)));

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CODE_RET) {
                checkUpgrade();
            }
            initActivity(0);
        } else {
            finish();
        }
    }

    public void hideTab(boolean bHide) {
        if (bHide) {
            mTabHost.getTabWidget().setVisibility(View.GONE);
        } else {
            mTabHost.getTabWidget().setVisibility(View.VISIBLE);
        }
    }

    public void setAboutMeCount(int nCount) {

    }

    public void setIMCount(int nCount) {
        View v = mTabHost.getTabWidget().getChildTabViewAt(0);
        if (v != null) {
            TextView txt = (TextView)v.findViewById(R.id.txt_about_me_cout);

            txt.setVisibility(View.GONE);
            /*
             * if (nCount > 0) { txt.setVisibility(View.VISIBLE);
             * txt.setText(String.valueOf(nCount)); } else {
             * txt.setVisibility(View.GONE); }
             */
        }
    }

    private void initActivity(int tabIndex) {
        this.setVisible(true);
        Log.d(TAG, "initActivity called");

        if (mHasInited) {
            return;
        }

        mTabHost = (TabHost)findViewById(R.id.tabhost);
        mTabHost.setup(this.getLocalActivityManager());


        // add statuses
        TabSpec mTab = mTabHost.newTabSpec(TAB_DYNAMIC);
        mTab.setIndicator(inflaterTab(getResources().getDrawable(R.drawable.ic_dynamic),
                getResources().getString(R.string.tab_txt_dynamic)));
        mTab.setContent(new Intent(this, Statuses_Activity.class));
        mTabHost.addTab(mTab);

        // add option
        mTab = mTabHost.newTabSpec(TAG_OPTION);
        mTab.setIndicator(inflaterTab(getResources().getDrawable(R.drawable.ic_option),
                getResources().getString(R.string.tab_txt_option)));
        mTab.setContent(new Intent(this, OptionActivity.class));
        mTabHost.addTab(mTab);

        View view = mTabHost.getTabWidget().getChildTabViewAt(1);
        view.setOnTouchListener(new TabHost.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final EditText searchBar = (EditText)mTabHost.getCurrentView().findViewById(
                        R.id.txt_search);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ListView contactListView = (ListView)mTabHost.getCurrentView()
                            .findViewById(
                                    R.id.listContacts);

                    if (searchBar != null && contactListView != null) {
                        contactListView.setSelection(0);
                        View view = contactListView.getFocusedChild();
                        if (view != null) {
                            contactListView.clearChildFocus(view);
                        }
                        contactListView.clearFocus();
                        searchBar.clearFocus();
                        cn.com.nd.momo.util.Utils.showKeyboard(getApplicationContext(), searchBar);
                        searchBar.requestFocus();
                        return true;
                    } else {
                        Log.i(TAG, "seach edittext not find");
                    }
                }

                return false;
            }
        });

        mTabHost.setCurrentTab(tabIndex);
        
        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				int tabNow = mTabHost.getCurrentTab();
				ConfigHelper.getInstance(getApplication()).saveIntKey(ConfigHelper.CONFIG_KEY_LAST_TAB, tabNow);
			}});

        Log.w(TAG, "begin bind service task");

        mHasInited = true;
        Intent intent = getIntent();
        final boolean needDelete;
        if (intent != null && intent.hasExtra(DELETE_MOMO_DB)) {
            needDelete = intent.getBooleanExtra(DELETE_MOMO_DB, false);
        } else {
            needDelete = false;
        }
        // do sync when application start
        if (!SyncContactApi.getInstance(getApplicationContext()).isSyncInProgress()) {
            // begin sync
            Thread tSync = new Thread() {

                @Override
                public void run() {
                    Log.d(TAG, "start sync");
                    if (needDelete) {
                        SyncContactApi.getInstance(getApplicationContext())
                                .deleteMoMoDatabaseContacts();
                    }
                    String syncMode = GlobalUserInfo.getSyncMode(getApplicationContext());
                    if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(syncMode)) {
                        if (null != Utils.getActiveNetWorkName(Utils.getContext())) {
                            SyncContactApi.getInstance(getApplicationContext()).serverSync();
                        } else {
                            Account currentAccount = Utils.getCurrentAccount();
                            Account momoAccount = Utils.getMoMoAccount();
                            if(currentAccount != null 
                                && momoAccount != null 
                                && momoAccount.equals(currentAccount)
                                && !Utils.isBindedAccountExist(currentAccount)) {
                                Log.i(TAG, "add momo account");
                                //会写momo自定义帐号联系人
                                try {
                                    String accountName = GlobalUserInfo.getPhoneNumber();
                                    if(!TextUtils.isEmpty(accountName)) {
                                        Utils.addAccount(null);
                                        Log.i(TAG, "begin back add momo account sync");
                                        try {
                                            //等待一秒创建完成momo帐号
                                            sleep(1000);
                                        } catch(Exception e) {
                                            e.printStackTrace();
                                        }
                                        Log.i(TAG, "back add momo account sync");
                                        SyncContactApi.getInstance(getApplicationContext()).backAddMoMoAcountSync();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                    
                                }
                            }
                        }
                    } else if (ConfigHelper.SYNC_MODE_LOCAL_ONLY.equals(syncMode)) {
                        SyncContactApi.getInstance(getApplicationContext()).localSync();
                    }
                    GlobalContactList.getInstance().loadDisplayContactList();
                    CardManager.getInstance().batchGetCard(getApplicationContext());
                }

            };
            // tSync.start();
            GlobalUserInfo.startSyncThread(tSync);
        }
    }

    public void toGuide(String flag) {
        Boolean isFirst = ch.loadBooleanKey(flag, true);
        if (isFirst) {
            Intent intent = new Intent(MainActivity.this, GuideActivity.class);
            intent.putExtra("flag", flag);
            startActivity(intent);
            ch.saveBooleanKey(flag, false);
            ch.commit();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent called");
        super.onNewIntent(intent);

        ConfigHelper ch = ConfigHelper.getInstance(getApplicationContext());
        String strSyncMode = ch.loadKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
        boolean importAccounts = ch.loadBooleanKey(ConfigHelper.CONFIG_KEY_IMPORT_ACCOUNTS,
                false);

        if (GlobalUserInfo.getUserStatus() < GlobalUserInfo.STATUS_VERIFY_USER) {
            Intent i = new Intent(getApplicationContext(), RegInfoActivity.class);
            startActivity(i);
            finish();
        } else if ("".equals(strSyncMode)) {
            Intent i = new Intent(this, LeadToSyncActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        } else if ((ConfigHelper.SYNC_MODE_TWO_WAY.equals(strSyncMode) && (!Utils
                .isBindedAccountExist(Utils.getCurrentAccount()) || !importAccounts))) {

            // clear momo database
            SyncContactApi.getInstance(getApplicationContext()).deleteMoMoDatabaseContacts();
            // start lead to sync activity
            Intent i = new Intent(getApplicationContext(), AccountsBindActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }

    private View inflaterTab(Drawable icon, String strText) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_layout, null);
        ViewGroup layout = (ViewGroup)view.findViewById(R.id.tab_bar_layout);
        layout.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.tab_selector));
        ImageView imageView = (ImageView)view.findViewById(R.id.tab_imageView);
        imageView.setImageDrawable(icon);
        TextView textView = (TextView)view.findViewById(R.id.tab_textView);
        textView.setText(strText);

        return view;
    }

    public void setToggleIcon(int nToggleState) {

        if (mTabHost.getCurrentTab() == 0) {
            ViewGroup layout = (ViewGroup)mTabHost.getCurrentTabView().findViewById(
                    R.id.tab_bar_layout);
            if (layout != null) {
                if (nToggleState == MainActivity.TAB_TOGGLED) {
                    layout.setBackgroundResource(R.drawable.tab_selector_toggled);
                } else {
                    layout.setBackgroundResource(R.drawable.tab_selector);
                }
            }

        } else if (mTabHost.getCurrentTab() == 2) {
            ViewGroup layout = (ViewGroup)mTabHost.getCurrentTabView().findViewById(
                    R.id.tab_bar_layout);
            if (layout != null) {
                if (nToggleState == MainActivity.TAB_TOGGLED) {
                    layout.setBackgroundResource(R.drawable.tab_selector_toggled);
                } else {
                    layout.setBackgroundResource(R.drawable.tab_selector);
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();

        if (mUpgradeDialog != null && mUpgradeDialog.isShowing()) {
            mUpgradeDialog.dismiss();
        }

        System.gc();
    }

    private ArrayList<IMentionInitCallback> lstCallback = new ArrayList<IMentionInitCallback>();

    /**
     * 注册一个回调，程序初始化的的时候去网络更新数据，当数据更新完并写入数据库时调用此回调， 用于更新关于我的界面，以及TabHost显示的关于我的数量
     * 
     * @param callback
     */
    public void registerMentionInitCallback(IMentionInitCallback callback) {
        lstCallback.add(callback);
    }

    public interface IMentionInitCallback {
        void onMessage(ArrayList<MentionInfo> lstMentionInfo);
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SMS_COUNT:
                    try {
                        TextView headerView = (TextView)mTabHost.getCurrentView().findViewById(
                                R.id.conversation_history_list_header);
                        if (headerView != null) {
                            Bundle bundle = msg.getData();
                            int count = bundle.getInt("sms_count");
                            String headerTip = String.format(getString(R.string.click_to_reg),
                                    count);
                            headerView.setText(headerTip);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case MainActivity.MSG_SHOW_UPGRADE_DIALOG:
                    UpgradeInfo uinfo = (UpgradeInfo)msg.obj;
                    showUpgradeDialog(uinfo);
                    break;
                default:
                    break;
            }
        }
    };

    private void checkUpgrade() {
        Log.d(TAG, "check update");
        int versionCode = 0;
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
            UpgradeMgr.checkUpgradeThread(this, mHandler, GlobalUserInfo.getUID(), String
                    .valueOf(versionCode));
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void showUpgradeDialog(final UpgradeInfo uinfo) {
        String message = getString(R.string.txt_option_upgrade_size)
                + ": "
                + cn.com.nd.momo.util.Utils.formatSize2String(Long
                        .valueOf(uinfo.fileSize))
                + "\n"
                + getString(R.string.txt_option_upgrade_version)
                + ": "
                + uinfo.currentVersion
                + "\n"
                + getString(R.string.txt_option_upgrade_remark)
                + ": "
                + uinfo.remark
                + "\n"
                + getString(R.string.txt_option_upgrade_date)
                + ": "
                + cn.com.nd.momo.util.Utils.formatDateToNormalRead(String
                        .valueOf(Long.valueOf(uinfo.publishDate) * 1000));
        try {
            // show dialog for upgrade
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.txt_option_version_yes));
            builder.setMessage(message);
            builder.setIcon(android.R.drawable.ic_dialog_info);
            builder.setPositiveButton("跳过此版本",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            ConfigHelper
                                    .getInstance(MainActivity.this)
                                    .saveKey(
                                            ConfigHelper.CONFIG_KEY_SKIP_VERSION,
                                            uinfo.currentVersion);
                            dialog.dismiss();
                        }
                    });
            builder.setNeutralButton("稍后提醒我",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton("下载并安装",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(
                                DialogInterface dialog,
                                int which) {
                            UpgradeMgr.down(MainActivity.this, uinfo.downloadUrl);
                            dialog.dismiss();
                        }
                    });
            mUpgradeDialog = builder.create();
            if (!isFinishing()) {
                mUpgradeDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}


package cn.com.nd.momo.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.RequestUrl;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.view.MarqueeTextView;

/**
 * 设置页面activity
 * 
 * @author jiaolei
 */
public class OptionActivity extends TabActivity implements OnClickListener {

    static private final String TAG = "OptionActivity";

    private final int MSG_LOGOUT_END = 1;

    private final int ACCOUNT_BIND_REQUEST_ID = 100;

    private final int ACCOUNT_BIND_SYNC_REQUEST_ID = 101;

    private static final int MSG_GET_CONTACT_COUNT_BY_ACCOUNT = 500;

    private TextView m_txtLoginUser;

    // name
    private ViewGroup m_btnMyHomePage;

    // change mobile
    private ViewGroup mBtnChangeMobile;

    // change password
    private ViewGroup mBtnChangePassword;

    // sync
    private ViewGroup m_btnSync;

    // account
    private ViewGroup m_btnAccount;

    private ViewGroup m_btnImportContacts;

    // about
    private ViewGroup m_btnAbout;

    // help
    private ViewGroup m_btnHelp;

    // feed back
    private ViewGroup m_btnFeedBack;

    // ring
    private ViewGroup m_btnMsgRing;

    // vibrate
    private ViewGroup m_btnMsgVibrate;

    private ViewGroup m_btnImAutoPlay;

    // system sound
    private ViewGroup m_btnMsgSystemSound;

    private CheckBox mChkAutoSync;

    private CheckBox mChkAudioAutoPlay;

    // 拦截所有短信选项
    private ViewGroup mOptInterceptAll;

    private CheckBox mChkInterceptAll;

    // 配置文件里已经设置的值——是否拦截所有短信
    private boolean mIsInterceptAll;


    // 拦截mo短信选项
    private CheckBox mChkInterceptMomo;
    private ViewGroup mOptInterceptMomo;
    
    //上传用源文件
    private CheckBox mChkFullSize;
    private ViewGroup mOptFullSize;
    private boolean mIsFullSize;

    private ViewGroup mOptRobot;

    // 配置文件里已经设置的值——是否拦截MO短信
    private boolean mIsInterceptMomo;

    // quit
    private Button m_btnQuit;

    private boolean m_msgRing = true;

    private boolean m_msgVibrate = false;

    private boolean m_msgSystemSound = true;

    private boolean m_audioAutoPlay = true;

    private ConfigHelper configHelper = null;

    private Handler mHandler = new Handler();

    private ProgressDialog dlgLogout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.option_activity);

        m_txtLoginUser = (TextView)findViewById(R.id.txt_option_login_user_info);
        // name
        m_btnMyHomePage = (ViewGroup)findViewById(R.id.btn_opt_my_home_page);
        m_btnMyHomePage.setOnClickListener(this);
        // Message ring
        m_btnMsgRing = (ViewGroup)findViewById(R.id.btn_opt_msg_ring);
        m_btnMsgRing.setOnClickListener(this);
        // Message vibrate
        m_btnMsgVibrate = (ViewGroup)findViewById(R.id.btn_opt_msg_vibrate);
        m_btnMsgVibrate.setOnClickListener(this);
        // Message vibrate
        m_btnMsgSystemSound = (ViewGroup)findViewById(R.id.btn_opt_msg_system_sound);
        m_btnMsgSystemSound.setOnClickListener(this);
        // new audio im auto play
        m_btnImAutoPlay = (ViewGroup)findViewById(R.id.btn_opt_im_auto_play);
        m_btnImAutoPlay.setOnClickListener(this);
        // change mobile
        mBtnChangeMobile = (ViewGroup)findViewById(R.id.btn_opt_change_mobile);
        mBtnChangeMobile.setOnClickListener(this);
        // change password
        mBtnChangePassword = (ViewGroup)findViewById(R.id.btn_opt_change_password);
        mBtnChangePassword.setOnClickListener(this);
        // sync
        m_btnSync = (ViewGroup)findViewById(R.id.layout_sync_mode_check);
        m_btnSync.setOnClickListener(this);
        // account
        m_btnAccount = (ViewGroup)findViewById(R.id.btn_opt_account);
        m_btnAccount.setClickable(false);
        // import contact
        m_btnImportContacts = (ViewGroup)findViewById(R.id.btn_opt_import_contacts);
        m_btnImportContacts.setOnClickListener(this);
        // about
        m_btnAbout = (ViewGroup)findViewById(R.id.btn_opt_about);
        m_btnAbout.setOnClickListener(this);

        // help
        m_btnHelp = (ViewGroup)findViewById(R.id.btn_opt_help);
        m_btnHelp.setOnClickListener(this);
        // quit
        m_btnQuit = (Button)findViewById(R.id.btn_quit);
        m_btnQuit.setOnClickListener(this);
        // feedback
        m_btnFeedBack = (ViewGroup)findViewById(R.id.btn_opt_feed_back);
        m_btnFeedBack.setOnClickListener(this);
        // upgrade
        findViewById(R.id.btn_opt_upgrade).setOnClickListener(this);

        configHelper = ConfigHelper.getInstance(this);

        initAutoSyncConfigure();

        // mo短信拦截
        mOptInterceptAll = (ViewGroup)findViewById(R.id.sms_opt_all_intercep);
        mOptInterceptAll.setOnClickListener(this);

        mChkInterceptAll = (CheckBox)findViewById(R.id.sms_chk_option_all);

        mOptRobot = (ViewGroup)findViewById(R.id.layout_robot);
        mOptRobot.setOnClickListener(this);

        mOptInterceptMomo = (ViewGroup)findViewById(R.id.sms_opt_momo_intercep);
        mOptInterceptMomo.setOnClickListener(this);
        mChkInterceptMomo = (CheckBox)findViewById(R.id.sms_chk_option_momo);
        
        mIsInterceptAll = configHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_ALL,
                true);
        mChkInterceptAll.setChecked(mIsInterceptAll);
        mIsInterceptMomo = configHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_MOMO,
                true);
        mChkInterceptMomo.setChecked(mIsInterceptMomo);

        mOptFullSize = (ViewGroup)findViewById(R.id.upload_opt_full_size);
        mOptFullSize.setOnClickListener(this);
        mChkFullSize= (CheckBox)findViewById(R.id.upload_chk_option_full_size);
        
        mIsFullSize = configHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_FULL_SIZE,
                false);
        mChkFullSize.setChecked(mIsFullSize);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        m_msgRing = configHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_MESSAGE_RING, true);
        CheckBox messageRing = (CheckBox)findViewById(R.id.chk_option_ring);
        messageRing.setChecked(m_msgRing);
        m_msgVibrate = configHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_MESSAGE_VIBRATE, false);
        CheckBox messageVibrate = (CheckBox)findViewById(R.id.chk_option_vibrator);
        messageVibrate.setChecked(m_msgVibrate);
        m_msgSystemSound = configHelper.loadBooleanKey(
                ConfigHelper.CONFIG_KEY_MESSAGE_SYSTEM_SOUND, true);
        CheckBox msgSystemSound = (CheckBox)findViewById(R.id.chk_option_system_sound);
        msgSystemSound.setChecked(m_msgSystemSound);
        if (m_msgRing) {
            m_btnMsgSystemSound.setVisibility(View.VISIBLE);
        } else {
            m_btnMsgSystemSound.setVisibility(View.GONE);
        }
        m_audioAutoPlay = configHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_IM_AUTO_PLAY,
                true);
        mChkAudioAutoPlay = (CheckBox)findViewById(R.id.chk_option_im_auto_play);
        mChkAudioAutoPlay.setChecked(m_audioAutoPlay);
        String syncModel = configHelper.loadKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
        mChkAutoSync = (CheckBox)findViewById(R.id.chk_auto_sync);
        resetSyncOptionView(syncModel, false);

        // change login status string and button enable due to the login status
        if (GlobalUserInfo.getName() != null && GlobalUserInfo.getName().length() != 0) {
            m_txtLoginUser.setText(GlobalUserInfo.getName() + "：设置个人名片 ");
        } else {
            m_txtLoginUser.setText("体验者：设置个人名片");
        }


    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    private Handler hLogout = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_LOGOUT_END:
                    if (dlgLogout != null && dlgLogout.isShowing()) {
                        dlgLogout.dismiss();
                    }

                    // after logout lead to login activity
                    Intent iLogin = new Intent(OptionActivity.this, LoginActivity.class);
                    startActivity(iLogin);
                    finish();

                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {
            case R.id.btn_opt_change_mobile:
                Toast.makeText(this, "请访问http://momo.im/user/changephone更换手机号", Toast.LENGTH_LONG)
                        .show();
                Log.d("btn_opt_change_mobile");
                break;
            case R.id.btn_opt_my_home_page:
                viewContactFragmentActivity(
                        Long.parseLong(GlobalUserInfo.getUID()),
                        GlobalUserInfo.getName(), this,
                        null);
                break;
            case R.id.btn_opt_msg_ring:
                Log.d("btn_opt_msg_ring");
                m_msgRing = !m_msgRing;
                CheckBox messageRing = (CheckBox)findViewById(R.id.chk_option_ring);
                messageRing.setChecked(m_msgRing);
                configHelper.saveBooleanKey(ConfigHelper.CONFIG_KEY_MESSAGE_RING, m_msgRing);
                configHelper.commit();
                if (m_msgRing) {
                    m_msgSystemSound = configHelper.loadBooleanKey(
                            ConfigHelper.CONFIG_KEY_MESSAGE_SYSTEM_SOUND, true);
                    CheckBox msgSystemSound = (CheckBox)findViewById(R.id.chk_option_system_sound);
                    msgSystemSound.setChecked(m_msgSystemSound);
                    m_btnMsgSystemSound.setVisibility(View.VISIBLE);
                } else {
                    m_btnMsgSystemSound.setVisibility(View.GONE);
                }
                break;
            case R.id.btn_opt_msg_vibrate:
                m_msgVibrate = !m_msgVibrate;
                CheckBox messageVibrate = (CheckBox)findViewById(R.id.chk_option_vibrator);
                messageVibrate.setChecked(m_msgVibrate);
                configHelper.saveBooleanKey(ConfigHelper.CONFIG_KEY_MESSAGE_VIBRATE, m_msgVibrate);
                configHelper.commit();
                break;
            case R.id.btn_opt_msg_system_sound:
                m_msgSystemSound = !m_msgSystemSound;
                CheckBox msgSystemSound = (CheckBox)findViewById(R.id.chk_option_system_sound);
                msgSystemSound.setChecked(m_msgSystemSound);
                configHelper.saveBooleanKey(ConfigHelper.CONFIG_KEY_MESSAGE_SYSTEM_SOUND,
                        m_msgSystemSound);
                configHelper.commit();
                break;
            case R.id.btn_opt_im_auto_play:
                m_audioAutoPlay = !m_audioAutoPlay;
                mChkAudioAutoPlay.setChecked(m_audioAutoPlay);
                configHelper.saveBooleanKey(ConfigHelper.CONFIG_KEY_IM_AUTO_PLAY, m_audioAutoPlay);
                configHelper.commit();
                break;
            case R.id.btn_opt_change_password:
                break;
            case R.id.btn_opt_account:
                break;
            case R.id.btn_opt_import_contacts:

                Log.d(TAG, "go to AccountsBindActivity end");
                break;
            case R.id.btn_opt_about:
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;
            case R.id.btn_opt_help:
                GlobalUserInfo.openMoMoUrl(this, RequestUrl.HELP_URL, false);
                break;
            case R.id.btn_quit:
                if (GlobalUserInfo.isNetSyncDoing()) {
                    String tips = getString(R.string.msg_exit_wait_sync);
                    cn.com.nd.momo.api.util.Utils.displayToast(tips, 0);
                    break;
                }
                dlgLogout = ProgressDialog.show(this, "退出登录", "正在退出登录，请稍等...");
                dlgLogout.setCancelable(false);
                Thread tQuit = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        GlobalUserInfo.logout(getApplicationContext());
                        hLogout.sendEmptyMessage(MSG_LOGOUT_END);
                    }
                };
                tQuit.start();
                break;
            case R.id.btn_opt_feed_back:

                break;
            case R.id.layout_robot:

                break;
            case R.id.layout_sync_mode_check:

                break;
            case R.id.btn_opt_upgrade:

                break;
            case R.id.sms_opt_all_intercep:
                mIsInterceptAll = !mIsInterceptAll;
                mChkInterceptAll.setChecked(mIsInterceptAll);
                configHelper
                        .saveBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_ALL, mIsInterceptAll);
                if (mIsInterceptAll && !mIsInterceptMomo) {
                    mIsInterceptMomo = true;
                    mChkInterceptMomo.setChecked(mIsInterceptMomo);
                    configHelper
                            .saveBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_MOMO,
                                    mIsInterceptMomo);
                }
                configHelper.commit();
                break;
            case R.id.sms_opt_momo_intercep:
                mIsInterceptMomo = !mIsInterceptMomo;
                mChkInterceptMomo.setChecked(mIsInterceptMomo);
                configHelper
                        .saveBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_MOMO, mIsInterceptMomo);
                configHelper.commit();
                break;
            case R.id.upload_opt_full_size:
            	mIsFullSize = !mIsFullSize;
            	mChkFullSize.setChecked(mIsFullSize);
            	configHelper.saveBooleanKey(ConfigHelper.CONFIG_KEY_FULL_SIZE, mIsFullSize);
            	configHelper.commit();
            	break;
        }
    }

    /**
     * 初始化同步方式
     */
    private void initAutoSyncConfigure() {
        mChkAutoSync = (CheckBox)findViewById(R.id.chk_auto_sync);
        String strKey = ConfigHelper.getInstance(getApplicationContext()).loadKey(
                ConfigHelper.CONFIG_KEY_SYNC_MODE);
        if (null == strKey || "".equals(strKey)) {
            strKey = ConfigHelper.SYNC_MODE_LOCAL_ONLY;
        }
        changeSyncMode(strKey);
        findViewById(R.id.layout_sync_mode_check).setOnClickListener(this);
    }

    /**
     * 切换同步与不同步
     * 
     * @param strModeKey
     */
    private void changeSyncMode(String strModeKey) {
        ConfigHelper.getInstance(getApplicationContext()).saveKey(
                ConfigHelper.CONFIG_KEY_SYNC_MODE, strModeKey);
        ConfigHelper.getInstance(getApplicationContext()).commit();
    }

    private void resetSyncOptionView(String strModeKey, boolean showContactCount) {
        int paddingLeft = (int)getResources().getDimension(
                R.dimen.option_sync_padding_left_or_right);
        int paddingTop = (int)getResources().getDimension(
                R.dimen.option_sync_padding_top_or_bottom);
        if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(strModeKey)) {
            mChkAutoSync.setChecked(true);
            MarqueeTextView accountView = (MarqueeTextView)findViewById(R.id.txt_opt_account);
            accountView.setText("同步帐号:" + getCurrentAccountName(showContactCount, ""));
            m_btnAccount.setVisibility(View.VISIBLE);
            m_btnImportContacts.setVisibility(View.VISIBLE);
            m_btnSync.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.mm_frame_set_up));
        } else if (ConfigHelper.SYNC_MODE_LOCAL_ONLY.equals(strModeKey)) {
            mChkAutoSync.setChecked(false);
            m_btnImportContacts.setVisibility(View.GONE);
            m_btnAccount.setVisibility(View.GONE);
            m_btnSync.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.mm_frame_set_whole));
        }
        m_btnSync.setPadding(paddingLeft, paddingTop, paddingLeft, paddingTop);
    }

    private void viewContactFragmentActivity(long uid, String name, Context context,
            String action) {

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (dlgLogout != null && dlgLogout.isShowing()) {
            dlgLogout.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dlgLogout != null && dlgLogout.isShowing()) {
            dlgLogout.dismiss();
        }
    }

    private String getCurrentAccountName(boolean showCount, String suffix) {
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}

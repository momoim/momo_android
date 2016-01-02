
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

    private TextView m_txtLoginUser;

    // name
    private ViewGroup m_btnMyHomePage;

    // change mobile
    private ViewGroup mBtnChangeMobile;

    // change password
    private ViewGroup mBtnChangePassword;


    // about
    private ViewGroup m_btnAbout;

    // help
    private ViewGroup m_btnHelp;

    // feed back
    private ViewGroup m_btnFeedBack;

    // quit
    private Button m_btnQuit;



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

        mBtnChangeMobile = (ViewGroup)findViewById(R.id.btn_opt_change_mobile);
        mBtnChangeMobile.setOnClickListener(this);
        // change password
        mBtnChangePassword = (ViewGroup)findViewById(R.id.btn_opt_change_password);
        mBtnChangePassword.setOnClickListener(this);

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // change login status string and button enable due to the login status
        if (GlobalUserInfo.getName() != null && GlobalUserInfo.getName().length() != 0) {
            m_txtLoginUser.setText(GlobalUserInfo.getName() + "：设置个人名片 ");
        } else {
            m_txtLoginUser.setText("设置个人名片");
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

            case R.id.btn_opt_change_password:
                break;

            case R.id.btn_opt_about:
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;
            case R.id.btn_opt_help:
                GlobalUserInfo.openMoMoUrl(this, RequestUrl.HELP_URL, false);
                break;
            case R.id.btn_quit:
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


            case R.id.btn_opt_upgrade:
                break;
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

}

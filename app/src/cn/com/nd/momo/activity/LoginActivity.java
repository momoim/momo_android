
package cn.com.nd.momo.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.Country;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.LoginThread;

import com.flurry.android.FlurryAgent;

/**
 * 登录activity
 * 
 * @author jiaolei
 */
public class LoginActivity extends Activity {

    public static final int HTTP_LOGIN_OK = 200;

    private static final String TAG = "LoginActivity";

    private static final int REQ_COUNTRY_CODE = 9;

    private static final int REQ_FORGET_PASSWORD = 10;

    private static final int MSG_LOGIN_RET = 0x101;

    // flurry logs
    private static final String FLURRY_LOGIN_SUCCESS = "登录成功";

    private Button m_btnLogin;

    private Button mBtnSelectCountry;

    private EditText m_txtUser;

    private EditText m_txtPwd;

    private ProgressDialog m_progressDlg = null;

    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "m_Handler handleMessage");
            Bundle b = msg.getData();
            int nRet = b.getInt("http_ret");
            String strRet = b.getString("http_response");

            switch (msg.what) {
                case MSG_LOGIN_RET:
                    switch (nRet) {
                        case HTTP_LOGIN_OK:
                            Log.i(TAG, "login ok");
                            FlurryAgent.logEvent(FLURRY_LOGIN_SUCCESS);
                            // let loginPreActivity to start main activity
                            setResult(RESULT_OK);
                            finish();
                            break;
                        default:
                            if (strRet == null || "".equals(strRet)) {
                                cn.com.nd.momo.api.util.Utils.displayToast(
                                        getString(R.string.msg_login_unknow_error),
                                        Toast.LENGTH_SHORT);
                            } else {
                                cn.com.nd.momo.api.util.Utils.displayToast(strRet,
                                        Toast.LENGTH_SHORT);
                            }
                            break;
                    }
                    // dismiss login progress dialog
                    if (m_progressDlg != null) {
                        m_progressDlg.dismiss();
                        m_progressDlg = null;
                    }
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        // 初始化国家区号初始值
        GlobalUserInfo.initDefaultZoneCode();
        mBtnSelectCountry = (Button)findViewById(R.id.btn_select_country);
        mBtnSelectCountry.setOnClickListener(new OnCountryClick());

        // login UI part
        m_btnLogin = (Button)findViewById(R.id.btn_login);
        m_btnLogin.setOnClickListener(new OnLoginClick());

        m_txtUser = (EditText)findViewById(R.id.txt_name);
        m_txtPwd = (EditText)findViewById(R.id.txt_pwd);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle data = intent.getExtras();
            if (data.containsKey("zone_code")) {
                String zoneCode = data.getString("zone_code");
                if (!"".equals(zoneCode)) {
                    String countryName = "";
                    ArrayList<Country> countryList = Country
                            .getCountryList(getApplicationContext());
                    for (Country country : countryList) {
                        if (zoneCode.equals(country.getZoneCode())) {
                            countryName = country.getCnName();
                            mBtnSelectCountry.setText(countryName + "(+" + zoneCode + ")");
                            GlobalUserInfo.setZoneCode(zoneCode);
                            break;
                        }
                    }
                    countryList.clear();
                    countryList = null;
                }
            }
            if (data.containsKey("mobile")) {
                m_txtUser.setText(data.getString("mobile"));
            }
        }

        TextView forgetPsw = (TextView)findViewById(R.id.txt_forget_pwd);
        forgetPsw.setTextColor(Color.BLUE);
        forgetPsw.setText(Html.fromHtml("<u>" + getString(R.string.txt_forget_pwd) + "</u> "));
        forgetPsw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, ForgetPasswordActivity.class);
                intent.putExtra(ForgetPasswordActivity.EXTRA_ZONE_CODE, GlobalUserInfo
                        .getZoneCode());
                intent
                        .putExtra(ForgetPasswordActivity.EXTRA_MOBILE, m_txtUser.getText()
                                .toString());
                startActivityForResult(intent, REQ_FORGET_PASSWORD);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 用户登录
     * 
     * @param strUser
     * @param strPwd
     * @param bReg
     */
    private void login(String strUser, String strPwd, boolean bReg) {
        // show waiting dialog
        m_progressDlg = ProgressDialog.show(LoginActivity.this, getResources().getText(
                R.string.msg_login_progress_title), getResources().getText(
                R.string.msg_login_progress_info));

        if (null == Utils.getActiveNetWorkName(this)) {
            cn.com.nd.momo.api.util.Utils.displayToast(getString(R.string.error_net_work), 0);
            m_progressDlg.dismiss();
            return;
        }

        LoginThread t = new LoginThread(LoginActivity.this, m_progressDlg, m_Handler);
        t.setLoginInfo(strUser, strPwd, bReg);
        t.start();

    }

    private class OnLoginClick implements OnClickListener {
        @Override
        public void onClick(View v) {

            // check user name not null
            if (m_txtUser.getText().length() == 0) {
                Toast.makeText(v.getContext(),
                        getResources().getText(R.string.msg_login_phone_num_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // check password not null
            if (m_txtPwd.getText().length() == 0) {
                Toast.makeText(v.getContext(),
                        getResources().getText(R.string.msg_login_password_empty),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            cn.com.nd.momo.util.Utils.hideKeyboard(LoginActivity.this, m_txtUser);
            cn.com.nd.momo.util.Utils.hideKeyboard(LoginActivity.this, m_txtPwd);

            login(m_txtUser.getText().toString(), m_txtPwd.getText().toString(), false);
        }
    }

    private class OnCountryClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            Intent ic = new Intent(LoginActivity.this, CountrySelectActivity.class);
            startActivityForResult(ic, REQ_COUNTRY_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {

            case REQ_COUNTRY_CODE:
                if (data != null && data.hasExtra("country") && data.hasExtra("code")) {
                    String strCountry = data.getStringExtra("country");
                    String strCode = data.getStringExtra("code");
                    if (!"".equals(strCode)) {
                        mBtnSelectCountry.setText(strCountry + "(+" + strCode + ")");
                        GlobalUserInfo.setZoneCode(strCode);
                    }
                }
                break;
            case REQ_FORGET_PASSWORD:
                if (data != null) {
                    String zoneCode = data.getStringExtra(ForgetPasswordActivity.EXTRA_ZONE_CODE);
                    String mobile = data.getStringExtra(ForgetPasswordActivity.EXTRA_MOBILE);
                    if (zoneCode == null || zoneCode.length() < 1) {
                        zoneCode = GlobalUserInfo.DEFAULT_ZONE_CODE;
                    }
                    String countryName = "";
                    ArrayList<Country> countryList = Country
                            .getCountryList(getApplicationContext());
                    for (Country country : countryList) {
                        if (zoneCode.equals(country.getZoneCode())) {
                            countryName = country.getCnName();
                            mBtnSelectCountry.setText(countryName + "(+" + zoneCode + ")");
                            GlobalUserInfo.setZoneCode(zoneCode);
                            break;
                        }
                    }
                    countryList.clear();
                    countryList = null;
                    m_txtUser.setText(mobile);
                }
                break;

            default:
                break;
        }
    }

}

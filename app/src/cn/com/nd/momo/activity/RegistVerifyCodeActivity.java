
package cn.com.nd.momo.activity;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.CardManager;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.RegistThread;

import com.flurry.android.FlurryAgent;

public class RegistVerifyCodeActivity extends Activity implements OnClickListener {
    private static final String TAG = "RegistVerifyCodeActivity";

    // flurry logs
    private static final String FLURRY_LOGIN_SUCCESS = "注册成功";

    private final int REQ_COUNTRY_CODE = 1;

    private EditText mEditVerifyCode;

    private Button mBtnVerify;

    private TextView mTxtVerifyCodeResponse;

    // dialog
    private ProgressDialog m_progressDlg = null;

    private String mMobile = "";

    private String mverifyCode = "";

    // private static final int REQUEST_REG_CODE = 10;

    // handler to process message from http response
    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            int nRet = b.getInt("http_ret");
            String strRet = b.getString("http_response");

            switch (msg.what) {
                case RegistThread.HTTP_GET_VERIFY:
                    // show message
                    if (nRet != RegistThread.HTTP_GET_VERIFY_OK) {
                        if (nRet == 400117) {
                            Intent data = new Intent();
                            data.putExtra(RegistSendVerifyActivity.EXTRA_HAD_REGIST, true);
                            data.putExtra(RegistSendVerifyActivity.EXTRA_REGIST_ZONE_CODE,
                                    GlobalUserInfo.getZoneCode());
                            data.putExtra(RegistSendVerifyActivity.EXTRA_REGIST_MOBILE, mMobile);
                            setResult(RESULT_OK, data);
                            Utils.displayToast("手机号码已注册，请登录", 0);
                            finish();
                        } else {
                            mTxtVerifyCodeResponse.setVisibility(View.VISIBLE);
                            mTxtVerifyCodeResponse.setText(strRet);
                        }
                    } else {
                        ConfigHelper.getInstance(getApplicationContext()).removeKey(
                                ConfigHelper.CONFIG_KEY_SYNC_MODE);
                        ConfigHelper.getInstance(getApplicationContext()).commit();
                        Intent intent = new Intent();
                        intent.putExtra(RegistSendVerifyActivity.EXTRA_REGIST_COMPLETE, true);
                        FlurryAgent.logEvent(FLURRY_LOGIN_SUCCESS);
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reg_verify_code);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(RegistSendVerifyActivity.EXTRA_REGIST_MOBILE)) {
            Utils.displayToast(getString(R.string.msg_login_phone_num_empty), 0);
            finish();
            return;
        }
        mMobile = intent.getStringExtra(RegistSendVerifyActivity.EXTRA_REGIST_MOBILE);
        if (mMobile == null || mMobile.length() < 1) {
            Utils.displayToast(getString(R.string.msg_login_phone_num_empty), 0);
            finish();
            return;
        }

        mEditVerifyCode = (EditText)findViewById(R.id.txt_reg_verify_code);
        mBtnVerify = (Button)findViewById(R.id.btn_verify);
        mBtnVerify.setOnClickListener(this);

        mTxtVerifyCodeResponse = (TextView)findViewById(R.id.txt_reg_verify_code_response);

        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        if (m_progressDlg != null && m_progressDlg.isShowing()) {
            m_progressDlg.dismiss();
        }
        switch (v.getId()) {
            case R.id.btn_select_country:
                Intent ic = new Intent(this, CountrySelectActivity.class);
                startActivityForResult(ic, REQ_COUNTRY_CODE);
                break;

            case R.id.btn_verify:
                mTxtVerifyCodeResponse.setVisibility(View.GONE);
                mverifyCode = mEditVerifyCode.getText().toString();
                if (mverifyCode.length() < 1) {
                    Utils.displayToast(getString(R.string.msg_reg_info_verify_code_error), 0);
                    return;
                }
                if (null == Utils.getActiveNetWorkName(this)) {
                    cn.com.nd.momo.api.util.Utils.displayToast(getString(R.string.error_net_work),
                            Toast.LENGTH_SHORT);
                    return;
                }
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditVerifyCode);
                regist();
                break;
        }
    }

    private void regist() {
        // show waiting dialog
        m_progressDlg = ProgressDialog.show(this,
                getString(R.string.msg_reg_info_process_title),
                getString(R.string.msg_reg_info_do_active));
        m_progressDlg.setCancelable(false);
        new Thread() {
            @Override
            public void run() {
                int nRet = 0;
                String exceptMsg = "";

                try {
                    OAuthInfo mOAuthInfo = MoMoHttpApi.registerVerify(
                            GlobalUserInfo.getZoneCode(), mMobile, mverifyCode);
                    if (mOAuthInfo != null) {
                        nRet = HttpStatus.SC_OK;
                        GlobalUserInfo.setOAuthToken(mOAuthInfo);

                        GlobalUserInfo.setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
                        ConfigHelper configHelper = ConfigHelper
                                .getInstance(getApplicationContext());
                        // clear try once user
                        configHelper.removeKey(ConfigHelper.LAST_TIME_UPDATE_USER_ID);
                        configHelper.commit();
                        CardManager.getInstance().deleteAllUserCache();
                    }
                } catch (MoMoException ex) {
                    nRet = ex.getCode();
                    exceptMsg = ex.getSimpleMsg();
                } catch (Exception ex) {
                    nRet = 0;
                    exceptMsg = ex.getMessage();
                }
                // send UI message to show result information
                Message msg = new Message();
                msg.what = RegistThread.HTTP_GET_VERIFY;
                Bundle b = new Bundle();
                b.putInt("http_ret", nRet);
                b.putString("http_response", exceptMsg);
                msg.setData(b);
                m_Handler.sendMessage(msg);
                if (m_progressDlg != null && m_progressDlg.isShowing()) {
                    m_progressDlg.dismiss();
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_progressDlg != null && m_progressDlg.isShowing()) {
            m_progressDlg.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        // show message box to ensure quit
        Builder b = new AlertDialog.Builder(this);
        b.setTitle("确定返回");
        b.setMessage("返回后需要重新发送验证码，原先验证码失效");
        b.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(RESULT_CANCELED);
                RegistVerifyCodeActivity.this.finish();
                return;
            }
        });
        b.setNegativeButton(getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        b.show();
    }

}


package cn.com.nd.momo.activity;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
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
import cn.com.nd.momo.api.http.HttpTool;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.AvatarManager;
import cn.com.nd.momo.manager.GlobalUserInfo;

public class NewPasswordActivity extends Activity implements OnClickListener {
    private static final String TAG = "NewPasswordActivity";

    private EditText mEditPassword;

    private Button mBtnSubmit;

    private TextView mTxtError;

    private final int MSG_RESET_PASSWORD_RESULT = 101;

    public final static String EXTRA_RESET_PASSWORD_COMPLETE = "reset_password_complete";

    public final static String EXTRA_RESET_RELOGIN = "relogin";

    // dialog
    private ProgressDialog m_progressDlg = null;

    private String mMobile = "";

    private String mPassword = "";

    // private static final int REQUEST_REG_CODE = 10;

    // handler to process message from http response
    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            int nRet = b.getInt("http_ret");
            String strRet = b.getString("http_response");

            switch (msg.what) {
                case MSG_RESET_PASSWORD_RESULT:
                    if (nRet == HttpStatus.SC_OK) {
                        Utils.displayToast("新密码修改成功", 0);
                        Intent data = new Intent();
                        data.putExtra(EXTRA_RESET_PASSWORD_COMPLETE, true);
                        setResult(RESULT_OK, data);
                        finish();
                    } else if (nRet == HttpTool.OAUTH_DISABLED) {
                        Utils.displayToast("授权失效，请重新登录", 0);
                        Intent data = new Intent();
                        data.putExtra(EXTRA_RESET_RELOGIN, true);
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        mTxtError.setVisibility(View.VISIBLE);
                        mTxtError.setText(strRet);
                        if (strRet != null && strRet.length() > 0) {
                            Utils.displayToast(strRet, 0);
                        }
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
        setContentView(R.layout.new_password_activity);
        GlobalUserInfo.cancelResetPassword();

        mEditPassword = (EditText)findViewById(R.id.txt_password);
        mBtnSubmit = (Button)findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);

        mTxtError = (TextView)findViewById(R.id.txt_error);

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

            case R.id.btn_submit:
                mTxtError.setVisibility(View.GONE);
                mPassword = mEditPassword.getText().toString();
                if (mPassword.length() < 1) {
                    Utils.displayToast(getString(R.string.msg_reg_info_verify_code_error), 0);
                    return;
                }
                if (null == Utils.getActiveNetWorkName(this)) {
                    cn.com.nd.momo.api.util.Utils.displayToast(getString(R.string.error_net_work),
                            Toast.LENGTH_SHORT);
                    return;
                }
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditPassword);
                changePassword();
                break;
        }
    }

    private void changePassword() {
        // show waiting dialog
        m_progressDlg = ProgressDialog.show(this,
                getString(R.string.txt_change_pwd),
                getString(R.string.txt_change_pwd_waiting));
        m_progressDlg.setCancelable(false);
        new Thread() {
            @Override
            public void run() {
                int code = 0;
                String error = "";

                try {
                    OAuthInfo mOAuthInfo = MoMoHttpApi.resetPwd(GlobalUserInfo.getZoneCode(),
                            GlobalUserInfo.getPhoneNumber(), mPassword);
                    if (mOAuthInfo != null) {
                        GlobalUserInfo.setOAuthToken(mOAuthInfo.getUid(), mOAuthInfo.getFinalKey(),
                                mOAuthInfo.getFinalSecret(), mOAuthInfo.getUserName(),
                                mOAuthInfo.getAvatarName(), mOAuthInfo.getQueueName(),
                                mOAuthInfo.getStatus(), mOAuthInfo.getZoneCode(),
                                mOAuthInfo.getMobile());

                        Bitmap bmp = AvatarManager.getAvaterBitmap(
                                Long.valueOf(mOAuthInfo.getUid()), mOAuthInfo.getAvatarName());
                        GlobalUserInfo.setMyAvatar(bmp);

                        code = HttpStatus.SC_OK;

                        // set login status
                        GlobalUserInfo.setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
                    }
                } catch (MoMoException ex) {
                    code = ex.getCode();
                    error = ex.getSimpleMsg();
                }

                // send UI message to show result information
                Message msg = new Message();
                msg.what = MSG_RESET_PASSWORD_RESULT;
                Bundle b = new Bundle();
                b.putInt("http_ret", code);
                b.putString("http_response", error);
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}

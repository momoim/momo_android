
package cn.com.nd.momo.activity;

import org.apache.http.HttpStatus;

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
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;

public class ResetPasswordActivity extends Activity implements OnClickListener {
    private static final String TAG = "ResetPasswordActivity";

    private EditText mEditOldPassword;

    private EditText mEditNewPassword;

    private Button mBtnSubmit;

    private TextView mTxtError;

    private final int MSG_RESET_PASSWORD_RESULT = 101;

    private final String EXTRA_RESET_PASSWORD_COMPLETE = "reset_password_complete";

    // dialog
    private ProgressDialog m_progressDlg = null;

    private static final int REQ_FORGET_PASSWORD = 10;

    private String mMobile = "";

    private String mOldPassword = "";

    private String mNewPassword = "";

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
        setContentView(R.layout.reset_password_activity);

        mEditOldPassword = (EditText)findViewById(R.id.txt_old_password);
        mEditNewPassword = (EditText)findViewById(R.id.txt_new_password);
        mBtnSubmit = (Button)findViewById(R.id.btn_submit);
        mBtnSubmit.setOnClickListener(this);

        mTxtError = (TextView)findViewById(R.id.txt_error);

        TextView forgetPsw = (TextView)findViewById(R.id.txt_forget_pwd);
        forgetPsw.setTextColor(Color.BLUE);
        forgetPsw.setText(Html.fromHtml("<u>" + getString(R.string.txt_forget_pwd) + "</u> "));
        forgetPsw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResetPasswordActivity.this, ForgetPasswordActivity.class);
                intent.putExtra(ForgetPasswordActivity.EXTRA_ZONE_CODE, GlobalUserInfo
                        .getZoneCode());
                startActivityForResult(intent, REQ_FORGET_PASSWORD);
            }
        });

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
                mOldPassword = mEditOldPassword.getText().toString();
                mNewPassword = mEditNewPassword.getText().toString();
                if (mOldPassword.length() < 1) {
                    Utils.displayToast(getString(R.string.txt_old_password_tip), 0);
                    return;
                }
                if (mNewPassword.length() < 1) {
                    Utils.displayToast(getString(R.string.txt_new_password_tip), 0);
                    return;
                } else if (mNewPassword.length() < 6) {
                    Utils.displayToast(getString(R.string.txt_new_password_lenght_tip), 0);
                    return;
                }
                if (null == Utils.getActiveNetWorkName(this)) {
                    cn.com.nd.momo.api.util.Utils.displayToast(getString(R.string.error_net_work),
                            Toast.LENGTH_SHORT);
                    return;
                }
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditOldPassword);
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditNewPassword);
                resetPassword();
                break;
        }
    }

    private void resetPassword() {
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
                    MoMoHttpApi.resetPwd(mOldPassword, mNewPassword);
                    code = HttpStatus.SC_OK;
                } catch (MoMoException e) {
                    code = e.getCode();
                    error = e.getSimpleMsg();
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

}

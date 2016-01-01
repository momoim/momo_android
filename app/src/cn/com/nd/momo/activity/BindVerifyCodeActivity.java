
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.oauth.OAuthHelper;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;

import com.flurry.android.FlurryAgent;

public class BindVerifyCodeActivity extends Activity implements OnClickListener {

    // flurry logs
    private static final String FLURRY_BIND_MOBILE_SUCCESS = "91u跳转过来绑定手机号成功";

    public static final String EXTRA_BIND_MOBILE = "mobile";

    private EditText mEditVerifyCode;

    private Button mBtnVerify;

    // dialog
    private ProgressDialog mProgressDlg = null;

    private String mMobile = "";

    private static final int MSG_CHECK_VERIFYCODE_SUCCESS = 1;

    private static final int MSG_CHECK_VERIFYCODE_FAILED = 2;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mProgressDlg != null && mProgressDlg.isShowing()) {
                mProgressDlg.dismiss();
            }
            switch (msg.what) {
                case MSG_CHECK_VERIFYCODE_SUCCESS:
                    FlurryAgent.logEvent(FLURRY_BIND_MOBILE_SUCCESS);
                    setResult(RESULT_OK);
                    finish();
                    break;
                case MSG_CHECK_VERIFYCODE_FAILED:
                    String error = "";
                    if (msg.getData() != null) {
                        Bundle b = msg.getData();
                        error = b.getString("error");
                    }
                    if (error == null || error.length() < 1) {
                        error = getString(R.string.verify_code_failed);
                    }
                    Utils.showMessageDialog(BindVerifyCodeActivity.this,
                            getString(R.string.bind_mobile), error);
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
        setContentView(R.layout.bind_verify_code_activity);

        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(EXTRA_BIND_MOBILE)) {
            Utils.displayToast(getString(R.string.msg_login_phone_num_empty), 0);
            finish();
            return;
        }
        mMobile = intent.getStringExtra(EXTRA_BIND_MOBILE);
        if (mMobile == null || mMobile.length() < 1) {
            Utils.displayToast(getString(R.string.msg_login_phone_num_empty), 0);
            finish();
            return;
        }

        mEditVerifyCode = (EditText)findViewById(R.id.edit_verify_code);
        mBtnVerify = (Button)findViewById(R.id.btn_verify);
        mBtnVerify.setOnClickListener(this);

        // getWindow().setSoftInputMode(
        // android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
        }
        switch (v.getId()) {
            case R.id.btn_verify:
                String verifyCode = mEditVerifyCode.getText().toString();
                if (verifyCode.length() < 1) {
                    Utils.displayToast(getString(R.string.msg_reg_info_verify_code_error), 0);
                    return;
                }
                if (null == Utils.getActiveNetWorkName(this)) {
                    Utils.displayToast(getString(R.string.error_net_work), 0);
                    return;
                }
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditVerifyCode);
                verifyCode(verifyCode);
                break;
        }
    }

    private void verifyCode(final String verifyCode) {
        // show waiting dialog
        mProgressDlg = ProgressDialog.show(this,
                getString(R.string.bind_mobile),
                getString(R.string.verify_code_waiting));
        mProgressDlg.setCancelable(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    OAuthInfo oauthInfo = MoMoHttpApi.bindMobile(OAuthHelper.CONSUMER_KEY, mMobile,
                            verifyCode);
                    GlobalUserInfo.setOAuthToken(oauthInfo);
                    GlobalUserInfo.setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
                    mHandler.sendEmptyMessage(MSG_CHECK_VERIFYCODE_SUCCESS);
                } catch (MoMoException e) {
                    e.printStackTrace();

                    Message msg = new Message();
                    msg.what = MSG_CHECK_VERIFYCODE_FAILED;
                    Bundle data = new Bundle();
                    data.putString("error", e.getMessage());
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
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
                BindVerifyCodeActivity.this.finish();
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

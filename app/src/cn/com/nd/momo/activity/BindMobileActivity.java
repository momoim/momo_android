
package cn.com.nd.momo.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
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
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Utils;

public class BindMobileActivity extends Activity implements OnClickListener {

    public static final String EXTRA_REGIST_MOBILE = "mobile";

    private final int REQ_VERIFY_CODE_CODE = 2;

    private Bundle mBundle = null;

    private EditText mEditMobile;

    private Button mBtnNextStep;

    // dialog
    private ProgressDialog mProgressDlg = null;

    private static final int MSG_APPLY_VERIFYCODE_SUCCESS = 1;

    private static final int MSG_APPLY_VERIFYCODE_FAILED = 2;

    // handler to process message from http response
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent intent = null;
            if (mProgressDlg != null && mProgressDlg.isShowing()) {
                mProgressDlg.dismiss();
            }
            switch (msg.what) {
                case MSG_APPLY_VERIFYCODE_SUCCESS:
                    intent = new Intent(BindMobileActivity.this, BindVerifyCodeActivity.class);
                    intent.putExtra(BindVerifyCodeActivity.EXTRA_BIND_MOBILE, mEditMobile.getText()
                            .toString().trim());
                    startActivityForResult(intent, REQ_VERIFY_CODE_CODE);
                    break;
                case MSG_APPLY_VERIFYCODE_FAILED:
                    String error = "";
                    if (msg.getData() != null) {
                        Bundle b = msg.getData();
                        error = b.getString("error");
                    }
                    if (error == null || error.length() < 1) {
                        error = getString(R.string.bind_mobile_failed);
                    }
                    Utils.showMessageDialog(BindMobileActivity.this,
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
        setContentView(R.layout.bind_mobile_activity);

        Intent intent = getIntent();
        if (intent != null) {
            mBundle = intent.getExtras();
        }

        mEditMobile = (EditText)findViewById(R.id.edit_mobile);
        mBtnNextStep = (Button)findViewById(R.id.btn_next_step);
        mBtnNextStep.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_next_step:
                String mobile = mEditMobile.getText().toString().trim();
                if (mobile == null || mobile.length() < 1) {
                    mEditMobile.requestFocus();
                    Utils.displayToast(getString(R.string.msg_reg_info_mobile_error), 0);
                    return;
                }

                if (null == Utils.getActiveNetWorkName(this)) {
                    Utils.displayToast(getString(R.string.error_net_work), 0);
                    return;
                }
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditMobile);
                applyVerifyCode(mobile);
                break;
        }
    }

    private void applyVerifyCode(final String mobile) {
        // show waiting dialog
        mProgressDlg = ProgressDialog.show(BindMobileActivity.this,
                        getString(R.string.bind_mobile),
                        getString(R.string.bind_mobile_waiting));
        mProgressDlg.setCancelable(false);

        new Thread() {

            @Override
            public void run() {

                try {
                    MoMoHttpApi.applyVerifyCode(OAuthHelper.CONSUMER_KEY, mobile);
                    mHandler.sendEmptyMessage(MSG_APPLY_VERIFYCODE_SUCCESS);
                } catch (MoMoException e) {
                    e.printStackTrace();

                    Message msg = new Message();
                    msg.what = MSG_APPLY_VERIFYCODE_FAILED;
                    Bundle data = new Bundle();
                    data.putString("error", e.getMessage());
                    msg.setData(data);
                    mHandler.sendMessage(msg);
                }
            }

        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_VERIFY_CODE_CODE) {
            if (resultCode == RESULT_OK) {
                // 绑定手机号成功
                if (mBundle != null) {

                    String param = mBundle.getString("callFrom91U");
                    if (param != null && !param.equals("")) {
                        try {
                            JSONObject json = new JSONObject(param);
                            String sid = json.getString("sid");
                            ConfigHelper ch = ConfigHelper.getInstance(getApplicationContext());
                            ch.saveKey(ConfigHelper.CONFIG_SID, sid);
                        } catch (JSONException e) {
                            // 91U调用参数错误，不做特殊处理
                            e.printStackTrace();
                        }
                    }
                }
                ConfigHelper configHelper = ConfigHelper.getInstance(getApplicationContext());
                configHelper.saveKey(ConfigHelper.CONFIG_KEY_SYNC_MODE,
                        ConfigHelper.SYNC_MODE_LOCAL_ONLY);
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtras(mBundle);
                startActivity(i);
                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
        }
    }

}

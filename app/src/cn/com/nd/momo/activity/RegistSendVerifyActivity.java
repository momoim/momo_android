
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
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
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.RegistThread;

public class RegistSendVerifyActivity extends Activity implements OnClickListener {
    private static final String TAG = "RegistSendVerifyActivity";

    public static final String EXTRA_HAD_REGIST = "had_regist";

    public static final String EXTRA_REGIST_COMPLETE = " regist_complete";

    public static final String EXTRA_REGIST_ZONE_CODE = "zone_code";

    public static final String EXTRA_REGIST_MOBILE = "mobile";

    private static String ZONE_CODE = GlobalUserInfo.DEFAULT_ZONE_CODE;

    private final int REQ_COUNTRY_CODE = 1;

    private final int REQ_VERIFY_CODE_CODE = 2;

    private EditText mEditPhoneNum;

    private Button mBtnVerify;

    private TextView mTxtResponse;

    private Button mBtnSelectCountry;

    // dialog
    private ProgressDialog m_progressDlg = null;

    // regist thread
    private RegistThread mThread = null;

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
                            data.putExtra(EXTRA_HAD_REGIST, true);
                            data.putExtra(EXTRA_REGIST_ZONE_CODE, GlobalUserInfo.getZoneCode());
                            data.putExtra(EXTRA_REGIST_MOBILE, mEditPhoneNum.getText().toString());
                            setResult(RESULT_OK, data);
                            Utils.displayToast("手机号码已注册，请登录", 0);
                            RegistSendVerifyActivity.this.finish();
                            return;
                        }
                        mTxtResponse.setVisibility(View.VISIBLE);
                        mTxtResponse.setText(strRet);
                    } else {
                        // ConfigHelper.getInstance(getApplicationContext()).removeKey(
                        // ConfigHelper.CONFIG_KEY_SYNC_MODE);
                        // ConfigHelper.getInstance(getApplicationContext()).commit();
                        // FlurryAgent.logEvent(FLURRY_LOGIN_SUCCESS);
                        // setResult(RESULT_OK);
                        // finish();
                        Intent intent = new Intent(RegistSendVerifyActivity.this,
                                RegistVerifyCodeActivity.class);
                        intent.putExtra(EXTRA_REGIST_MOBILE, mEditPhoneNum.getText().toString());
                        startActivityForResult(intent, REQ_VERIFY_CODE_CODE);
                    }
                    break;

                case RegistThread.BEGIN_SEARCH_SMS:
                    // change progress dialog message
                    m_progressDlg.setMessage(getResources().getString(
                            R.string.msg_reg_info_sennding_sms));
                    break;

                case RegistThread.FIND_VERIFY_TIME_OUT:
                    if (m_progressDlg != null && m_progressDlg.isShowing()) {
                        m_progressDlg.dismiss();
                    }
                    cn.com.nd.momo.api.util.Utils.displayToast(
                            getString(R.string.msg_reg_info_find_verify_timeout),
                            Toast.LENGTH_SHORT);
                    break;

                case RegistThread.DLG_BACK_KEY_ENABLE:
                    m_progressDlg.setCancelable(true);
                    break;

                case RegistThread.DLG_BACK_KEY_DISABLE:
                    m_progressDlg.setCancelable(false);
                    break;

                default:
                    Log.e(TAG, "handleMessage: get an message " + msg.what);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reg_send_verify);

        mEditPhoneNum = (EditText)findViewById(R.id.txt_reg_info_phone);
        mBtnVerify = (Button)findViewById(R.id.btn_verify);
        mBtnVerify.setOnClickListener(this);
        // 初始化国家区号初始值
        GlobalUserInfo.initDefaultZoneCode();
        ZONE_CODE = GlobalUserInfo.DEFAULT_ZONE_CODE;
        mBtnSelectCountry = (Button)findViewById(R.id.btn_select_country);
        mBtnSelectCountry.setOnClickListener(this);

        mTxtResponse = (TextView)findViewById(R.id.txt_reg_info_response);

        getWindow().setSoftInputMode(
                android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onResume() {
        super.onResume();

        GlobalUserInfo.setZoneCode(ZONE_CODE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_select_country:
                Intent ic = new Intent(this, CountrySelectActivity.class);
                startActivityForResult(ic, REQ_COUNTRY_CODE);
                break;

            case R.id.btn_verify:
                mTxtResponse.setVisibility(View.GONE);
                if (!CheckTextField(true, null)) {
                    ShowMsg(R.string.msg_reg_info_mobile_error);
                    return;
                }

                if (null == Utils.getActiveNetWorkName(this)) {
                    cn.com.nd.momo.api.util.Utils.displayToast(
                            getString(R.string.error_net_work), Toast.LENGTH_SHORT);
                    return;
                }

                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditPhoneNum);

                // show waiting dialog
                m_progressDlg = ProgressDialog.show(RegistSendVerifyActivity.this,
                        getResources()
                                .getText(R.string.msg_reg_info_process_title), getResources()
                                .getText(
                                        R.string.msg_reg_info_get_verify_code));
                m_progressDlg.setCancelable(false);
                // begin to regist
                mThread = new RegistThread(v.getContext(), m_progressDlg, m_Handler);
                mThread.setRegistInfo(mEditPhoneNum.getText().toString());
                mThread.start();

                // when thread is searching sms, we allow user to press back
                // key
                m_progressDlg.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // show information to user and go to active UI
                        AlertDialog MsgAlert = new AlertDialog.Builder(
                                RegistSendVerifyActivity.this).create();
                        MsgAlert.setMessage(getResources().getString(
                                R.string.msg_reg_info_find_verify_timeout));
                        MsgAlert.setButton(getResources().getString(R.string.btn_reg_info_OK),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });

                        MsgAlert.show();
                    }
                });

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_COUNTRY_CODE) {
            if (data != null && data.hasExtra("country") && data.hasExtra("code")) {
                String strCountry = data.getStringExtra("country");
                String strCode = data.getStringExtra("code");
                if (!"".equals(strCode)) {
                    mBtnSelectCountry.setText(strCountry + "(+" + strCode + ")");
                    GlobalUserInfo.setZoneCode(strCode);
                    ZONE_CODE = strCode;
                }
            }
        } else if (requestCode == REQ_VERIFY_CODE_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    boolean isRegist = data.getBooleanExtra(EXTRA_HAD_REGIST, false);
                    boolean registComplete = data.getBooleanExtra(EXTRA_REGIST_COMPLETE, false);
                    if (isRegist || registComplete) {
                        setResult(resultCode, data);
                        finish();
                        return;
                    }
                }
            }
            mTxtResponse.setVisibility(View.GONE);
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    private boolean CheckTextField(boolean bCheckAll, View v) {
        boolean bRet = true;

        // check phone number
        if ((bCheckAll) || (mEditPhoneNum.equals(v))) {
            if (mEditPhoneNum.length() < 1) {
                // mEditPhoneNum.setError(getResources().getString(R.string.msg_reg_info_phone_null));
                mEditPhoneNum.requestFocus();
                bRet = false;
            }
        }

        return bRet;
    }

    private void ShowMsg(int nResId) {
        Toast.makeText(this, getString(nResId), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_progressDlg != null && m_progressDlg.isShowing()) {
            m_progressDlg.dismiss();
        }
    }

}

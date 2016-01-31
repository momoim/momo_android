
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;
import org.w3c.dom.Text;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.util.Timer;
import cn.com.nd.momo.manager.GlobalUserInfo;

import static android.os.SystemClock.uptimeMillis;

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

    private long timestamp;
    private int beginQuery;
    private Timer queryTimer;

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
                    Toast.makeText(this, getString(R.string.error_net_work),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditPhoneNum);

                // show waiting dialog
                m_progressDlg = ProgressDialog.show(RegistSendVerifyActivity.this,
                        getResources().getText(R.string.msg_reg_info_process_title),
                        getResources().getText(R.string.msg_reg_info_get_verify_code));
                m_progressDlg.setCancelable(false);

                getVerifyCode(mEditPhoneNum.getText().toString());

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

    private String matchSMS(String body) {
        //sms template "尊敬的用户,您的注册验证码是773322,感谢您使用momo！";
        if (body.indexOf("momo") != -1 && body.indexOf("您的注册验证码是") != -1) {
            Pattern p = Pattern.compile("您的注册验证码是([0-9]{6})", Pattern.CASE_INSENSITIVE);
            Matcher match = p.matcher(body);
            if (!match.find()) {
                return "";
            } else {
                String code = match.group(1);
                Log.d(TAG, "code:" + code);
                return code;
            }
        }
        return "";
    }

    private String getVerifyCodeFromSMS() {
        long timestamp = this.timestamp*1000;
        Uri inboxURI = Uri.parse("content://sms/inbox");
        Log.d(TAG, "search timestamp:" + timestamp);
        Cursor c = getContentResolver().query(inboxURI,
                null,
                "date>?",
                new String[]{
                        String.valueOf(timestamp)
                },
                null);

        if (c == null || c.getCount() < 1) {
            return "";
        }

        if (c.moveToFirst()) {
            do {
                String strBody = c.getString(c.getColumnIndex("body"));
                String code = matchSMS(strBody);
                if (!TextUtils.isEmpty(code)) {
                    return code;
                }
            } while (c.moveToNext());
        }
        return "";
    }

    private void getVerifyCode(final String mPhoneNum) {
        timestamp = cn.com.nd.momo.util.Utils.getNow();

        new AsyncTask<Void, Integer, Integer>() {
            private String exceptMsg = "";

            @Override
            protected Integer doInBackground(Void... urls) {
                int nRet = 0;
                try {
                    MoMoHttpApi.register(GlobalUserInfo.getZoneCode(), mPhoneNum);
                    nRet = 200;
                } catch (MoMoException e) {
                    nRet = e.getCode();
                    exceptMsg = e.getSimpleMsg();
                } catch (Exception e) {
                    nRet = 0;
                    exceptMsg = "注册失败";
                }
                return nRet;
            }
            @Override
            protected void onPostExecute(Integer result) {
                if (result != 200) {
                    mTxtResponse.setVisibility(View.VISIBLE);
                    mTxtResponse.setText(exceptMsg);
                } else {
                    searchSMS();
                }
            }
        }.execute();
    }

    private void searchSMS() {
        queryTimer = new Timer() {
            @Override
            protected void fire() {
                long now = cn.com.nd.momo.util.Utils.getNow();
                if (now - beginQuery >= 60) {
                    Log.i(TAG, "read verify code from sms timeout");
                    m_progressDlg.dismiss();
                    queryTimer.suspend();

                    Intent intent = new Intent(RegistSendVerifyActivity.this,
                            RegistVerifyCodeActivity.class);
                    intent.putExtra(EXTRA_REGIST_MOBILE, mEditPhoneNum.getText().toString());
                    startActivityForResult(intent, REQ_VERIFY_CODE_CODE);

                    return;
                }

                String verifyCode = RegistSendVerifyActivity.this.getVerifyCodeFromSMS();
                if (!TextUtils.isEmpty(verifyCode)) {
                    queryTimer.suspend();
                    regist(mEditPhoneNum.getText().toString(), verifyCode);
                }
            }
        };
        beginQuery = cn.com.nd.momo.util.Utils.getNow();
        queryTimer.setTimer(uptimeMillis(), 1000);
        queryTimer.resume();
    }

    private void regist(final String mobile, final String verifyCode) {
        // show waiting dialog
        m_progressDlg.setMessage(getString(R.string.msg_reg_info_do_active));
        m_progressDlg.setCancelable(false);

        new AsyncTask<Void, Integer, Integer>() {
            private String exceptMsg = "";
            @Override
            protected Integer doInBackground(Void... urls) {
                int nRet = 0;

                try {
                    OAuthInfo mOAuthInfo = MoMoHttpApi.registerVerify(
                            GlobalUserInfo.getZoneCode(), mobile, verifyCode);
                    if (mOAuthInfo != null) {
                        nRet = HttpStatus.SC_OK;
                        GlobalUserInfo.setOAuthInfo(mOAuthInfo);
                    }
                } catch (MoMoException ex) {
                    nRet = ex.getCode();
                    exceptMsg = ex.getSimpleMsg();
                } catch (Exception ex) {
                    nRet = 0;
                    exceptMsg = ex.getMessage();
                }
                return nRet;
            }
            @Override
            protected void onPostExecute(Integer result) {
                if (m_progressDlg != null && m_progressDlg.isShowing()) {
                    m_progressDlg.dismiss();
                }

                if (result != 200) {
                    mTxtResponse.setVisibility(View.VISIBLE);
                    mTxtResponse.setText(exceptMsg);
                    return;
                } else {
                    {
                        Intent intent = new Intent(RegistSendVerifyActivity.this, RegInfoActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    Intent intent = new Intent();
                    intent.putExtra(RegistSendVerifyActivity.EXTRA_REGIST_COMPLETE, true);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        }.execute();
    }
}

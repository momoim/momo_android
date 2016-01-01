
package cn.com.nd.momo.activity;

import java.util.ArrayList;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.ProgressDialog;
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
import cn.com.nd.momo.api.types.Country;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;

public class ForgetPasswordActivity extends Activity implements OnClickListener {
    public static final String EXTRA_ZONE_CODE = "zone_code";

    public static final String EXTRA_MOBILE = "mobile";

    private static String ZONE_CODE = GlobalUserInfo.DEFAULT_ZONE_CODE;

    private final int REQ_COUNTRY_CODE = 1;

    private final int MSG_FORGET_PASSWORD_RESULT = 101;

    private EditText mEditPhoneNum;

    private Button mBtnGetPassword;

    private TextView mTxtResponse;

    private Button mBtnSelectCountry;

    // dialog
    private ProgressDialog m_progressDlg = null;

    private String mMobile = "";

    // private static final int REQUEST_REG_CODE = 10;

    // handler to process message from http response
    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle b = msg.getData();
            int nRet = b.getInt("http_ret");
            String strRet = b.getString("http_response");
            if (m_progressDlg != null && m_progressDlg.isShowing()) {
                m_progressDlg.dismiss();
            }
            switch (msg.what) {
                case MSG_FORGET_PASSWORD_RESULT:
                    if (nRet == HttpStatus.SC_OK) {
                        Utils.displayToast("密码即将以短信的形式下发到您的手机，请查收", 1);
                        Intent data = new Intent();
                        data.putExtra(EXTRA_ZONE_CODE, GlobalUserInfo.getZoneCode());
                        data.putExtra(EXTRA_MOBILE, mMobile);
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        mTxtResponse.setVisibility(View.VISIBLE);
                        mTxtResponse.setText(strRet);
                        if (strRet != null && strRet.length() > 0) {
                            Utils.displayToast(strRet, 0);
                        }
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.forget_password_activity);

        mEditPhoneNum = (EditText)findViewById(R.id.txt_reg_info_phone);
        mBtnGetPassword = (Button)findViewById(R.id.btn_submit);
        mBtnGetPassword.setOnClickListener(this);
        // 初始化国家区号初始值
        GlobalUserInfo.initDefaultZoneCode();
        ZONE_CODE = GlobalUserInfo.DEFAULT_ZONE_CODE;
        mBtnSelectCountry = (Button)findViewById(R.id.btn_select_country);
        mBtnSelectCountry.setOnClickListener(this);

        mTxtResponse = (TextView)findViewById(R.id.txt_reg_info_response);

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
                            ZONE_CODE = zoneCode;
                            break;
                        }
                    }
                    countryList.clear();
                    countryList = null;
                }
            }
            if (data.containsKey("mobile")) {
                mEditPhoneNum.setText(data.getString("mobile"));
            }
        }

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

            case R.id.btn_submit:
                mTxtResponse.setVisibility(View.GONE);
                mMobile = mEditPhoneNum.getText().toString();
                if (mMobile.length() < 1) {
                    Utils.displayToast(getString(R.string.msg_reg_info_mobile_error), 0);
                    return;
                }
                if (null == Utils.getActiveNetWorkName(this)) {
                    cn.com.nd.momo.api.util.Utils.displayToast(
                            getString(R.string.error_net_work), Toast.LENGTH_SHORT);
                    return;
                }
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mEditPhoneNum);
                getPassword();
                break;
        }
    }

    private void getPassword() {
        // show waiting dialog
        m_progressDlg = ProgressDialog.show(this,
                getString(R.string.msg_get_sms_password),
                getString(R.string.msg_get_sms_password_request));
        m_progressDlg.setCancelable(false);
        new Thread() {
            @Override
            public void run() {
                int code = 0;
                String error = "";
                try {
                    MoMoHttpApi.getPwdBySms(GlobalUserInfo.getZoneCode(), mMobile);
                    code = HttpStatus.SC_OK;
                } catch (MoMoException e) {
                    code = e.getCode();
                    error = e.getSimpleMsg();
                }

                // send UI message to show result information
                Message msg = new Message();
                msg.what = MSG_FORGET_PASSWORD_RESULT;
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
        } else {
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_progressDlg != null && m_progressDlg.isShowing()) {
            m_progressDlg.dismiss();
        }
    }

}

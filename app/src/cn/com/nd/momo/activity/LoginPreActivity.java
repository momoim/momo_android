
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.manager.GlobalUserInfo;

/**
 * 选择已注册用户登录或体验者登录activity
 * 
 * @author jiaolei
 */
public class LoginPreActivity extends Activity {

    public static final String INTENT_EXTRA_AUTO_LOGIN_BY_SMS = "auto_login";

    public static final int REGIST_REQUEST_CODE = 100;

    public static final int RESET_PASSWORD_REQUEST_CODE = 101;

    public static final int HTTP_LOGIN_OK = 200;

    public static final int HTTP_GET_SMSCONTENT_OK = 200;

    public static final int HTTP_REGISTER_HAS_MOMO_USER = 201;

    public static final int HTTP_GET_SMSCONTENT_ERROR = 400;

    private static final String TAG = "LoginPreActivity";

    private static final int MSG_REGISTER_SUCCESS_RET = 0x101;

    private static final int MSG_REGISTER_FAILED_RET = 0x102;

    private static final int MSG_REGISTER_FAILED_HAD_LOGIN_RET = 0x103;

    // private final byte TryTimes = 6;

    // login part
    private Button m_btnLogin;

    private TextView m_TxtInternationalRegist;

    private Button m_btnChinaRegist;

    private ProgressDialog m_progressDlg = null;

    // private String uniqid = null;
    //
    // private int simOperator = 0;

    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Intent i = null;
            switch (msg.what) {
                case MSG_REGISTER_FAILED_HAD_LOGIN_RET:
                    if (m_progressDlg != null && m_progressDlg.isShowing()) {
                        m_progressDlg.dismiss();
                    }
                    cn.com.nd.momo.api.util.Utils.displayToast("您已经注册,请登录", 0);
                    i = new Intent(LoginPreActivity.this, LoginActivity.class);
                    Bundle data = msg.getData();
                    if (data != null) {
                        i.putExtras(data);
                    }
                    startActivityForResult(i, LOGIN_PRE_LOGIN);
                    break;
                case MSG_REGISTER_FAILED_RET:
                    if (m_progressDlg != null && m_progressDlg.isShowing()) {
                        m_progressDlg.dismiss();
                    }
                    cn.com.nd.momo.api.util.Utils.displayToast("一键注册失败,请您手动注册", 0);
                    i = new Intent(LoginPreActivity.this, RegistSendVerifyActivity.class);
                    startActivityForResult(i, REGIST_REQUEST_CODE);
                    break;
                case MSG_REGISTER_SUCCESS_RET:
                    if (m_progressDlg != null && m_progressDlg.isShowing()) {
                        m_progressDlg.dismiss();
                    }
                    sendBroadcast(new Intent(getString(R.string.action_message_login)));
                    i = new Intent(getApplicationContext(), RegInfoActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                    finish();
                    break;
            }
        }
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(INTENT_EXTRA_AUTO_LOGIN_BY_SMS)) {
            boolean autoLogined = intent.getBooleanExtra(INTENT_EXTRA_AUTO_LOGIN_BY_SMS, false);
            if (autoLogined) {
                Intent i = new Intent(this, LeadToSyncActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login_pre);

        // login UI part
        m_btnLogin = (Button)findViewById(R.id.btn_login_pre);
        m_btnLogin.setOnClickListener(new OnLoginClick());

        m_TxtInternationalRegist = (TextView)findViewById(R.id.txt_internation_regist);
        m_TxtInternationalRegist.setText(Html.fromHtml("<u>"
                + getString(R.string.txt_international_regist) + "</u> "));
        m_TxtInternationalRegist.setOnClickListener(new OnRegisterClick());

        m_btnChinaRegist = (Button)findViewById(R.id.btn_china_regist);
        m_btnChinaRegist.setOnClickListener(new OnRegisterClick());

    }

    private class OnRegisterClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(LoginPreActivity.this, RegistSendVerifyActivity.class);
            startActivityForResult(i, REGIST_REQUEST_CODE);
        }
    }

    // private class OnOneKeyRegisterClick implements OnClickListener {
    // @Override
    // public void onClick(View v) {
    // if (null == HttpToolkit.getActiveNetWorkName(LoginPreActivity.this)) {
    // cn.com.nd.momo.api.util.Utils.displayToast(getString(R.string.error_net_work),
    // 0);
    // return;
    // }
    // TelephonyManager telManager =
    // (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
    // String imsi = telManager.getSubscriberId();
    // Log.d(TAG, "imsi:" + imsi);
    // simOperator = 0;
    // if (imsi != null) {
    // if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
    // simOperator = 1;
    // } else if (imsi.startsWith("46001")) {
    // simOperator = 2;
    // } else if (imsi.startsWith("46003")) {
    // simOperator = 3;
    // } else {
    // simOperator = 0;
    // }
    // } else {
    // simOperator = 0;
    // }
    // Context context = LoginPreActivity.this;
    // AlertDialog.Builder builder = new AlertDialog.Builder(context);
    // builder.setTitle(getString(R.string.txt_one_key_register_button));
    // LayoutInflater mInflater = (LayoutInflater)context
    // .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    // View contentview = mInflater
    // .inflate(R.layout.one_key_register_notice, null);
    // if (simOperator == 0) {
    // View view = contentview.findViewById(R.id.select_operator_layout);
    // view.setVisibility(View.VISIBLE);
    // RadioGroup rg =
    // (RadioGroup)contentview.findViewById(R.id.mobile_operator);
    // final RadioButton rbChinaMobile = (RadioButton)contentview
    // .findViewById(R.id.china_mobile);
    // final RadioButton rgChinaUnicom = (RadioButton)contentview
    // .findViewById(R.id.china_unicom);
    // final RadioButton rgChinaTelecom = (RadioButton)contentview
    // .findViewById(R.id.china_telecom);
    // simOperator = 1;
    //
    // rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
    //
    // @Override
    // public void onCheckedChanged(RadioGroup group, int checkedId) {
    // if (checkedId == rbChinaMobile.getId()) {
    // simOperator = 1;
    // } else if (checkedId == rgChinaUnicom.getId()) {
    // simOperator = 2;
    // } else if (checkedId == rgChinaTelecom.getId()) {
    // simOperator = 3;
    // } else {
    // simOperator = 0;
    // }
    // }
    // });
    // }
    // builder.setView(contentview);
    // builder.setPositiveButton(R.string.txt_ok,
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // LoginPreActivity.this.registerReceiver(mBroadcastReceiver,
    // new IntentFilter("momo.sms.action.send"));
    //
    // m_progressDlg = ProgressDialog.show(LoginPreActivity.this,
    // getResources()
    // .getText(R.string.msg_reg_info_process_title),
    // getResources().getText(
    // R.string.msg_reg_info_process_info));
    // m_progressDlg.setCancelable(false);
    //
    // new Thread() {
    // public void run() {
    // final Message msg = new Message();
    // int nRet = 0;
    // HttpToolkit http = new HttpToolkit(
    // RequestUrl.REGIST_SMSCONTENT);
    // String strResponse = null;
    // JSONObject jRet = null;
    // JSONObject param = new JSONObject();
    // try {
    // param.put("device_id", GlobalUserInfo.getDeviceIMEI());
    // param.put("source", GlobalUserInfo.MOMO_ANDROID_SOURCE_ID);
    // } catch (JSONException e) {
    // e.printStackTrace();
    // msg.what = MSG_REGISTER_FAILED_RET;
    // m_Handler.sendMessage(msg);
    // return;
    // }
    // nRet = http.DoPost(param);
    // Log.d(TAG, "register sms param:" + param.toString());
    // strResponse = http.GetResponse();
    // Log.d(TAG, "register sms:response code:" + nRet + " response:"
    // + strResponse);
    // if (nRet != HTTP_GET_SMSCONTENT_OK) {
    // // TODO 错误内容获取
    // msg.what = MSG_REGISTER_FAILED_RET;
    // m_Handler.sendMessage(msg);
    // return;
    // }
    // try {
    // jRet = new JSONObject(strResponse);
    // String message = jRet.optString("message");
    // JSONObject object = jRet.optJSONObject("mobile");
    // String chinaMobile = object.optString("china_mobile");
    // String chinaUnicom = object.optString("china_unicom");
    // String chinaTelecom = object.optString("china_telecom");
    // uniqid = jRet.optString("uniqid");
    // final String mobile;
    // switch (simOperator) {
    // case 1:
    // mobile = chinaMobile;
    // break;
    // case 2:
    // mobile = chinaUnicom;
    // break;
    // case 3:
    // mobile = chinaTelecom;
    // break;
    // default:
    // mobile = "";
    // }
    // if (mobile.length() < 1 || message.length() < 1) {
    // msg.what = MSG_REGISTER_FAILED_RET;
    // m_Handler.sendMessage(msg);
    // return;
    // }
    //
    // Log.i(TAG, "send sms--mobile:" + mobile + " message:"
    // + message);
    // MessageManager.sendSMs(getApplicationContext(), mobile,
    // message, 0, null);
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // }
    // }.start();
    // }
    // });
    // builder.setNegativeButton(R.string.txt_cancel,
    // new DialogInterface.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // Intent intent = new Intent(LoginPreActivity.this,
    // RegistSendVerifyActivity.class);
    // startActivityForResult(intent, REGIST_REQUEST_CODE);
    // }
    // });
    // AlertDialog alertDialog = builder.create();
    // alertDialog.show();
    // }
    // }

    // private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
    //
    // @Override
    // public void onReceive(Context context,
    // Intent intent) {
    // // TODO Auto-generated
    // // method stub
    // final int resultCode = getResultCode();
    // Log.i(TAG, "receiver code:" + resultCode);
    // new Thread() {
    // public void run() {
    // final Message msg = new Message();
    // if (resultCode == Activity.RESULT_OK) {
    // getRegisterQuery();
    // if (m_progressDlg != null &&
    // m_progressDlg.isShowing()) {
    // m_progressDlg.dismiss();
    // }
    // } else {
    // Log.e(TAG, "发送短信失败");
    // msg.what = MSG_REGISTER_FAILED_RET;
    // m_Handler.sendMessage(msg);
    // }
    // LoginPreActivity.this.unregisterReceiver(mBroadcastReceiver);
    // }
    // }.start();
    // }
    // };

    // private void getRegisterQuery() {
    // int nRet = 0;
    // String url = RequestUrl.REGIST_QUERY + uniqid;
    // HttpToolkit http = new HttpToolkit(url);
    // String strResponse = null;
    // JSONObject jRet = null;
    // Message message = new Message();
    // for (int requestTimes = 0; requestTimes < TryTimes; requestTimes++) {
    // nRet = http.DoGet(60000);// 请求超时设置1分钟
    // strResponse = http.GetResponse();
    // Log.d(TAG, "register query:response code:" + nRet
    // + " response:"
    // + strResponse);
    // try {
    // if (nRet == HTTP_REGISTER_HAS_MOMO_USER) {
    // jRet = new JSONObject(strResponse);
    // String mobile = jRet.optString("mobile");
    // String zoneCode = jRet.optString("zone_code");
    // Bundle bundle = new Bundle();
    // bundle.putString("mobile", mobile);
    // bundle.putString("zone_code", zoneCode);
    // message.what = MSG_REGISTER_FAILED_HAD_LOGIN_RET;
    // message.setData(bundle);
    // m_Handler.sendMessage(message);
    // return;
    // } else if (nRet == HTTP_GET_SMSCONTENT_OK) {
    // jRet = new JSONObject(strResponse);
    // String userId = jRet.optString("uid");
    // String oauthToken = jRet.optString("oauth_token");
    // String oauthTokenSecret = jRet
    // .optString("oauth_token_secret");
    // String userStatus = jRet.optString("user_status");
    // String zoneCode = jRet.optString("zone_code");
    // String mobile = jRet.optString("mobile");
    // String qname = jRet.getString("qname");
    // GlobalUserInfo
    // .setOAuthToken(userId,
    // oauthToken,
    // oauthTokenSecret,
    // null, null, qname, userStatus,
    // zoneCode, mobile);
    // GlobalUserInfo
    // .setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
    // ConfigHelper configHelper = ConfigHelper
    // .getInstance(getApplicationContext());
    // configHelper
    // .removeKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
    // configHelper
    // .removeKey(ConfigHelper.LAST_TIME_UPDATE_USER_ID);
    // configHelper.commit();
    //
    // message.what = MSG_REGISTER_SUCCESS_RET;
    // m_Handler.sendMessage(message);
    // return;
    // } else {
    // Log.e(TAG, strResponse);
    // continue;
    // }
    // } catch (JSONException e) {
    // e.printStackTrace();
    // }
    // }
    // message.what = MSG_REGISTER_FAILED_RET;
    // m_Handler.sendMessage(message);

    // }

    public final int LOGIN_PRE_LOGIN = 1;

    private class OnLoginClick implements OnClickListener {
        @Override
        public void onClick(View v) {
            Intent intent_login = new Intent(v.getContext(), LoginActivity.class);
            startActivityForResult(intent_login, LOGIN_PRE_LOGIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case RESET_PASSWORD_REQUEST_CODE:
                if (data != null) {
                    boolean success = data.getBooleanExtra(
                            NewPasswordActivity.EXTRA_RESET_PASSWORD_COMPLETE, false);
                    boolean relogin = data.getBooleanExtra(
                            NewPasswordActivity.EXTRA_RESET_RELOGIN, false);
                    if (success) {
                        // clear momo database
                        SyncContactApi.getInstance(getApplicationContext())
                                .deleteMoMoDatabaseContacts();
                        // start lead to sync activity
                        Intent i = new Intent(LoginPreActivity.this, LeadToSyncActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    } else if (relogin) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivityForResult(intent, LOGIN_PRE_LOGIN);
                    }
                }
                break;
            case LOGIN_PRE_LOGIN:
                // tell service to start consume
                int resetPassword = GlobalUserInfo.getNeedResetPassword();
                if (resetPassword != 1) {
                    sendBroadcast(new Intent(getString(R.string.action_message_login)));
                } else {
                    Intent i = new Intent(LoginPreActivity.this, NewPasswordActivity.class);
                    startActivityForResult(i, RESET_PASSWORD_REQUEST_CODE);
                    break;
                }

                if (GlobalUserInfo.getUserStatus() < GlobalUserInfo.STATUS_VERIFY_USER) {
                    Intent i = new Intent(LoginPreActivity.this, RegInfoActivity.class);
                    startActivity(i);
                } else {
                    // clear momo database
                    SyncContactApi.getInstance(getApplicationContext())
                            .deleteMoMoDatabaseContacts();
                    // start lead to sync activity
                    Intent i = new Intent(LoginPreActivity.this, LeadToSyncActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
                finish();
                break;
            case REGIST_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    boolean isRegist = data.getBooleanExtra(
                            RegistSendVerifyActivity.EXTRA_HAD_REGIST, false);
                    boolean registComplete = data.getBooleanExtra(
                            RegistSendVerifyActivity.EXTRA_REGIST_COMPLETE, false);
                    // sendBroadcast(new
                    // Intent(getString(R.string.action_message_login)));
                    if (isRegist) {
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        intent.putExtras(data);
                        startActivityForResult(intent, LOGIN_PRE_LOGIN);
                    } else if (registComplete) {
                        Intent i = new Intent(getApplicationContext(), RegInfoActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_progressDlg != null && m_progressDlg.isShowing()) {
            m_progressDlg.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent");
        super.onNewIntent(intent);
        if (intent != null && intent.hasExtra(INTENT_EXTRA_AUTO_LOGIN_BY_SMS)) {
            boolean autoLogined = intent.getBooleanExtra(INTENT_EXTRA_AUTO_LOGIN_BY_SMS, false);
            if (autoLogined) {
                Intent i = new Intent(this, LeadToSyncActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                finish();
                return;
            }
        }
    }

}


package cn.com.nd.momo.manager;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.util.Log;

public class RegistThread extends Thread {
    private static String TAG = "RegistThread";

    // message define
    public static final int HTTP_GET_VERIFY = 1; // get verify code

    public static final int HTTP_REG_INFO_ACTIVE = 2; // do verify code active

    public static final int BEGIN_SEARCH_SMS = 3; // begin searching sms box

    public static final int BEGIN_ACTIVE = 4; // begin active

    public static final int FIND_VERIFY_TIME_OUT = 5; // searching sms box time

    // out

    public static final int DLG_BACK_KEY_ENABLE = 6; // enable back key when

    // showing progress

    public static final int DLG_BACK_KEY_DISABLE = 7; // disable back key when

    // showing progress

    // return code for get verify code
    public static final int HTTP_GET_VERIFY_OK = 200;

    public static final int HTTP_GET_VERIFY_NULL = 401; // 手机号码格式为空

    public static final int HTTP_GET_VERIFY_FORMAT = 402; // 手机号码格式不对

    public static final int HTTP_GET_VERIFY_NAME_ERR = 403; // 姓名不是中文或者长度不合法

    public static final int HTTP_GET_VERIFY_PWD_NULL = 405; // 密码为空

    public static final int HTTP_GET_VERIFY_ALREADY_EXIST = 406; // 该手机号码已注册

    public static final int HTTP_GET_VERIFY_TOO_MUCH_TRY = 407; // 一天的验证码请求不能超过3次

    public static final int HTTP_GET_VERIFY_SERVER_BUSY = 408; // 系统异常，请稍候再试

    public static final int HTTP_GET_VERIFY_SMS_FAIL = 409; // 短信发送失败

    // return code for regist
    public static final int HTTP_REG_INFO_OK = 200;

    public static final int HTTP_REG_INFO_VERIFY_NULL = 401; // 验证码为空

    public static final int HTTP_REG_INFO_PHONE_NULL = 402; // 手机号码为空

    public static final int HTTP_REG_INFO_PHONE_FORMAT = 403; // 手机号码格式不对

    public static final int HTTP_REG_INFO_ALREADY_EXIST = 404; // 该手机号码已注册过

    public static final int HTTP_REG_INFO_ALREADY_CHECKED = 405; // 该手机号码未注册，不能激活

    public static final int HTTP_REG_INFO_ALREADY_VERIFIED = 406; // 该手机号码已验证过

    public static final int HTTP_REG_INFO_VERIFY_FORMAT = 407; // 验证码不正确

    public static final int HTTP_REG_INFO_VERIFY_EXPIRED = 408; // 该验证码已过期

    public static final int HTTP_REG_INFO_NO_VERIFY_FOUND = 409; // 未查到该手机号对于的验证码

    public static final int HTTP_REG_INFO_REGIST_FAILED = 410; // MOMO帐号激活失败

    public static final int OTHER_ERROR = 1000;

    // regist step define
    private int mProgressStep = STEP_GET_VERIFY;

    public static final int STEP_GET_VERIFY = 0;

    public static final int STEP_FIND_VERIFY = 1;

    public static final int STEP_ACTIVE_VERIFY = 2;

    public static final int STEP_ALL_COMPLETE = 3;

    private Context mContext = null;

    private ProgressDialog mDlg = null;

    private Handler mHandler = null;

    private String mPhoneNum = null;

    private int mStartStep = STEP_GET_VERIFY;

    private String mRegUrl = null;

    public RegistThread(Context c, ProgressDialog dlg, Handler h) {
        this.mContext = c;
        this.mDlg = dlg;
        this.mHandler = h;
    }

    public void setRegistInfo(String phoneNum) {
        this.mPhoneNum = phoneNum;
    }

    public void setStartStep(int nStartStep, String strPhoneNum, String strVerifyCode) {
        this.mStartStep = nStartStep;
        this.mPhoneNum = strPhoneNum;
        this.mRegUrl = strVerifyCode;
    }

    public void setStartStep(int nStartStep, String strVerifyCode) {
        this.mStartStep = nStartStep;
        this.mRegUrl = strVerifyCode;
    }

    public void setProgressStep(int nProgress) {
        mProgressStep = nProgress;
    }

    @Override
    public void run() {
        int nRet = 0;
        // step 1: get verify code from server
        // -----------------------------------------------
        if (mStartStep <= STEP_GET_VERIFY) {
            mProgressStep = STEP_GET_VERIFY;

            try {
                MoMoHttpApi.register(GlobalUserInfo.getZoneCode(), mPhoneNum);
                exitWithErrorCode(HTTP_GET_VERIFY, HTTP_GET_VERIFY_OK, "");
            } catch (MoMoException e) {
                exitWithErrorCode(HTTP_GET_VERIFY, e.getCode(), e.getSimpleMsg());
            } catch (Exception e) {
                exitWithErrorCode(HTTP_GET_VERIFY, nRet, "注册失败");
            }
        }

        // step 2: find verify code from sms box or from function param
        // -----------------------
        // if (mStartStep <= STEP_FIND_VERIFY) {
        // mProgressStep = STEP_FIND_VERIFY;
        // // send message to notify process dialog to change message
        // sendMessage(BEGIN_SEARCH_SMS);
        // // sendMessage(DLG_BACK_KEY_ENABLE);
        //
        // // get verify code from sms box
        // if (mRegUrl == null) {
        // mRegUrl = getRegUrlFromSMS();
        // }
        //
        // // check verify code
        // if (mRegUrl == null || mRegUrl.length() == 0) {
        // exitWithErrorCode(FIND_VERIFY_TIME_OUT, 0, null);
        // } else {
        // exitWithErrorCode(HTTP_GET_VERIFY, HTTP_GET_VERIFY_OK, mRegUrl);
        // }
        //
        // // exit thread
        // return;
        // }
    }

    //
    // private String getRegUrlFromSMS() {
    // long timestamp = 0;
    //
    // Cursor c = mContext.getContentResolver().query(Sms.CONTENT_URI,
    // null,
    // null,
    // null,
    // "date desc limit 1");
    // if (c.moveToNext()) {
    // timestamp = c.getLong(c.getColumnIndex("date"));
    // }
    // Log.d(TAG, "search timestamp:" + timestamp);
    // c = mContext.getContentResolver().query(Sms.CONTENT_URI,
    // null,
    // "date>?",
    // new String[] {
    // String.valueOf(timestamp)
    // },
    // null);
    //
    // String strUrl = "";
    //
    // if (c == null) {
    // return strUrl;
    // }
    //
    // try {
    //
    // // wait for 1 minutes to receive active message
    // for (int i = 0; i < 180; i++) {
    // if (mProgressStep == STEP_ALL_COMPLETE) {
    // break;
    // }
    //
    // // search sms box each seconds
    // try {
    // Thread.sleep(1000);
    // } catch (InterruptedException e) {
    // Log.e("getVerifyCodeFromSMS", e.getMessage());
    // }
    //
    // // search sms from server
    // c.requery();
    // if (c.getCount() < 1) {
    // continue;
    // }
    //
    // if (c.moveToFirst()) {
    // do {
    // String strBody = c.getString(c.getColumnIndex("body"));
    //
    // // find momo.im first
    // Pattern p = Pattern.compile("momo\\.im/l/", Pattern.CASE_INSENSITIVE);
    // Matcher match = p.matcher(strBody);
    // if (!match.find()) {
    // continue;
    // }
    //
    // // find url
    // p = Pattern.compile("(http://){1}[0-9A-Za-z\\./]+",
    // Pattern.CASE_INSENSITIVE);
    // match = p.matcher(strBody);
    // if (!match.find()) {
    // continue;
    // } else {
    // strUrl = match.group();
    // Log.d(TAG, strUrl);
    // }
    //
    // if (strUrl.length() < 1) {
    // continue;
    // }
    //
    // // sendMessage(BEGIN_ACTIVE);
    //
    // // regist this url
    // HttpToolkit http = new HttpToolkit(RequestUrl.REGIST_AUTO_VERIFY_BY_URL);
    //
    // JSONObject param = new JSONObject();
    // JSONObject jRet = null;
    // int nRet = 0;
    // String strResponse = null;
    //
    // try {
    // param.put("url", strUrl);
    //
    // } catch (JSONException e) {
    // Log.e(TAG, e.toString());
    // return "";
    // }
    //
    // // get response
    // nRet = http.DoPost(param);
    //
    // // when success, send massage
    // // else show error message
    // if (nRet == HttpToolkit.SERVER_SUCCESS) {
    //
    // strResponse = http.GetResponse();
    // Log.d(TAG, strResponse);
    // jRet = new JSONObject(strResponse);
    //
    // String strUID = jRet.getString("uid");
    // String strOauthToken = jRet.getString("oauth_token");
    // String strOauthTokenSecret = jRet.getString("oauth_token_secret");
    // String strStatus = jRet.getString("user_status");
    // String strMobile = jRet.getString("mobile");
    // String strzoneCode = jRet.getString("zone_code");
    // String strQName = jRet.getString("qname");
    //
    // Log.d(TAG, " uid: " + strUID +
    // " oauth_token: " + strOauthToken +
    // " oauth_token_secret: " + strOauthTokenSecret +
    // " qname: " + strQName +
    // " zoneCode: " + strzoneCode +
    // " mobile: " + strMobile +
    // " status: " + strStatus);
    //
    // GlobalUserInfo.setOAuthToken(strUID,
    // strOauthToken,
    // strOauthTokenSecret,
    // null, null, strQName, strStatus, strzoneCode, strMobile);
    //
    // GlobalUserInfo.setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
    // ConfigHelper configHelper = ConfigHelper.getInstance(mContext);
    // configHelper.removeKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
    // configHelper.removeKey(ConfigHelper.LAST_TIME_UPDATE_USER_ID);
    // configHelper.commit();
    // CardManager.getInstance().deleteAllUserCache();
    // mProgressStep = STEP_ALL_COMPLETE;
    // break;
    //
    // } else {
    // Log.d(TAG, "exp_auto_verify failed: " + http.GetResponse());
    // continue;
    // }
    //
    // } while (c.moveToNext());
    // }
    // }
    //
    // return strUrl;
    //
    // } catch (Exception e) {
    // Log.e(TAG, e.toString());
    // return "";
    // } finally {
    //
    // if (c != null && !c.isClosed()) {
    // c.close();
    // }
    // }
    // }
    //
    // private void sendMessage(int msgWhat) {
    // if (mHandler != null) {
    // Message msg = new Message();
    // msg.what = msgWhat;
    // mHandler.sendMessage(msg);
    //
    // } else {
    // Log.e(TAG, "sendMessage: mHandler is null");
    //
    // }
    // }

    // send error message and exit progress dialog
    private void exitWithErrorCode(int msgWhat, int nRet, String strRet) {
        // destroy progress dialog
        if (mDlg != null) {
            mDlg.dismiss();
        }

        if (mHandler != null) {
            Message msg = new Message();
            msg.what = msgWhat;
            Bundle b = new Bundle();
            b.putInt("http_ret", nRet);
            b.putString("http_response", strRet);
            msg.setData(b);
            mHandler.sendMessage(msg);

        } else {
            Log.e(TAG, "exitWithErrorCode: mHandler is null");

        }
    }
}

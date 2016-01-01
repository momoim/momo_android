
package cn.com.nd.momo.manager;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class LoginSearchSMSThread extends Thread {

    public static final int HTTP_LOGIN_FOR_URL_SUCCESS = 1;

    public static final int HTTP_LOGIN_FOR_URL_FAILED = 2;

    public static final String UID = "uid";

    public static final String NAME = "name";

    public static final String ZONE_CODE = "zone_code";

    public static final String MOBILE = "mobile";

    public static final String AVATAR = "avatar";

    public static final String OTOKEN = "oauth_token";

    public static final String OSECRET = "oauth_token_secret";

    public static final String QNAME = "qname";

    public static final String STATUS = "user_status";

    private Context mContext = null;

    private Handler mHandler = null;

    private Bundle mBundle = null;

    private Boolean mAutoLogin = false;

    public LoginSearchSMSThread(Context c, Handler h, Boolean autoLogin) {
        this.mContext = c;
        this.mHandler = h;
        mAutoLogin = autoLogin;
    }

    @Override
    public void run() {
        AutoLoginManager autoLoginMgr = AutoLoginManager.GetInstance(mContext
                .getApplicationContext(), mAutoLogin);
        Boolean result = autoLoginMgr.startLogin();
        if (result) {
            mBundle = autoLoginMgr.getBundle();
        }
        onResult();
    }

    private void onResult() {
        if (mHandler != null) {
            if (mBundle != null) {
                Message msg = mHandler.obtainMessage(HTTP_LOGIN_FOR_URL_SUCCESS);
                msg.setData(mBundle);
                mHandler.sendMessage(msg);
            } else {
                mHandler.sendEmptyMessage(HTTP_LOGIN_FOR_URL_FAILED);
            }
        }
    }

}

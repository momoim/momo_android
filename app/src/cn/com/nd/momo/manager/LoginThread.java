
package cn.com.nd.momo.manager;

import org.apache.http.HttpStatus;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.OAuthInfo;

/**
 * 登录线程
 * 
 * @author jiaolei
 */
public class LoginThread extends Thread {

    public static final int MSG_LOGIN_RET = 0x101;

    private Context mContext = null;

    private Handler mHandler = null;

    private String mPhoneNum = null;

    private String mPWD = null;

    public LoginThread(Context c, ProgressDialog dlg, Handler h) {
        this.mContext = c;
        this.mHandler = h;
    }

    public void setLoginInfo(String phoneNum, String pwd, boolean bReg) {
        this.mPhoneNum = phoneNum;
        this.mPWD = pwd;
    }

    @Override
    public void run() {
        int nRet = 0;
        String exceptMsg = "";
        // 需保存认证信息，处理重置密码
        try {
            OAuthInfo mOAuthInfo = MoMoHttpApi.login(GlobalUserInfo.getZoneCode(), mPhoneNum,
                    mPWD);
            if (mOAuthInfo != null) {
                // 不需要重置密码
                GlobalUserInfo.setOAuthToken(mOAuthInfo);
                // set login status
                GlobalUserInfo.setLoginStatus(GlobalUserInfo.LOGIN_STATUS_LOGINED);
                nRet = HttpStatus.SC_OK;
            }
        } catch (MoMoException ex) {
            nRet = ex.getCode();
            exceptMsg = ex.getSimpleMsg();
        }

        // send UI message to show result information
        Message msg = new Message();
        msg.what = MSG_LOGIN_RET;
        Bundle b = new Bundle();
        b.putInt("http_ret", nRet);
        b.putString("http_response", exceptMsg);
        msg.setData(b);
        mHandler.sendMessage(msg);
    }
}

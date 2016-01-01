
package cn.com.nd.momo.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;

/**
 * 自动登录管理类，提供为软件启动和短信拦截时使用
 * 
 * @author jiaolei
 */
public class AutoLoginManager {

    private static final String TAG = "AutoLoginManager";

    public static final String UID = "uid";

    public static final String NAME = "name";

    public static final String ZONE_CODE = "zone_code";

    public static final String MOBILE = "mobile";

    public static final String AVATAR = "avatar";

    public static final String OTOKEN = "oauth_token";

    public static final String OSECRET = "oauth_token_secret";

    public static final String QNAME = "qname";

    public static final String STATUS = "user_status";

    private Boolean mAutoLogin = false;

    private Boolean mResult = false;

    private Bundle mBundle = null;

    private Context mContext;

    private static AutoLoginManager mInstance = null;

    public static AutoLoginManager GetInstance(Context context, Boolean autoLogin) {
        if (mInstance == null) {
            mInstance = new AutoLoginManager(context, autoLogin);
        }
        mInstance.mAutoLogin = autoLogin;
        mInstance.mResult = false;
        mInstance.mBundle = null;
        return mInstance;
    }

    private AutoLoginManager(Context context, Boolean autoLogin) {
        mContext = context;
        mAutoLogin = autoLogin;
        if (context == null)
            Log.e(TAG, "the context point is null");
    }

    public Bundle getBundle() {
        return mBundle;
    }

    /**
     * 开始自动登录
     * 
     * @return
     */
    public Boolean startLogin() {
        new AssetAutoLogin(mContext,
                new AssetAutoLogin.OnAssetAutoLoginListener() {

                    @Override
                    public void onAssetAutoLoginGot(String url) {

                        if (!mResult) {
                            // smsSearch();
                        }
                    }

                    @Override
                    public void onAssetAutoLoginFail() {
                        // smsSearch();
                    }
                });
        return mResult;
    }

    //
    // /**
    // * 扫描前100条短信
    // */
    // private void smsSearch() {
    // Log.d(TAG, "search SMS begin");
    // Cursor c = mContext.getContentResolver().query(
    // Uri.parse("content://sms//inbox"), null, null, null, null);
    // if (c == null) {
    // return;
    // }
    // int i = 0;
    // try {
    // if (c.moveToFirst()) {
    // do {
    // // check first 100 SMS
    // if (i < 100) {
    // i++;
    // } else {
    // break;
    // }
    // String strBody = c.getString(c.getColumnIndex("body"));
    //
    // // find momo.im first
    // Pattern p = Pattern.compile("m\\.momo\\.im",
    // Pattern.CASE_INSENSITIVE);
    // Matcher match = p.matcher(strBody);
    // if (!match.find()) {
    // continue;
    // }
    //
    // // find url
    // String strUrl = "";
    // p = Pattern.compile("(http://){1}[0-9A-Za-z\\./]+",
    // Pattern.CASE_INSENSITIVE);
    // match = p.matcher(strBody);
    // if (!match.find()) {
    // continue;
    // } else {
    // strUrl = match.group();
    // }
    // if (strUrl.length() < 1) {
    // continue;
    // }
    //
    // // regist this url
    // login(strUrl);
    // if (mResult) {
    // // 读取到对应包含MOMO短信退出读短信过程
    // break;
    // } else {
    // continue;
    // }
    // } while (c.moveToNext());
    // }
    // } catch (Exception e) {
    // Log.e(TAG, e.toString());
    // }
    // if (c != null && !c.isClosed()) {
    // c.close();
    // }
    // Log.d(TAG, "search SMS count: " + (i + 1));
    // Log.d(TAG, "search SMS end");
    // }


}

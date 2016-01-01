
package cn.com.nd.momo.manager;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.telephony.TelephonyManager;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.WebViewActivity;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.model.DraftMgr;
import cn.com.nd.momo.model.DynamicDB;
import cn.com.nd.momo.model.DynamicMgr;
import cn.com.nd.momo.util.IMUtil;

/**
 * 用户信息全局配置管理类
 * 
 * @author jiaolei
 */
/** 
 * TODO(这里用一句话描述这个类的作用) 
 * @author 曾广贤 (muroqiu@qq.com)
 *
 */
public class GlobalUserInfo {
    private static final String TAG = "GlobalUserInfo";

    public static final String FLURRY_STRING = "L5MR8QTKQ49K5MHLA424";

    public static final String DEFAULT_ZONE_CODE = "86";

    // application context
    static public Context mAppContext = null;

    // contact id for myself
    static public long MY_CONTACT_ID = 0;

    // my avatar
    private static Bitmap mAvatarMap = null;

    static private String mPhoneNumber = ""; // login phone number

    static private String mUID = ""; // uid

    static private String mNAME = ""; // name

    static private String mAvatarName = ""; // avatar


    static private String mAccessToken;
    static private String mRefreshToken;
    static private int mExpireTS;


    static private String mSessionID = ""; // session id

    static private String mZoneCode = DEFAULT_ZONE_CODE;

    static public int STATUS_UNACTIVE = 1;

    static public int STATUS_NORMAL_USER = 2;

    static public int STATUS_VERIFY_USER = 3;

    static private String mStatus = ""; // value should be: 1 or 2 or 3

    // parameters for login
    static public final int LOGIN_STATUS_UNLOGIN = 0;

    static public final int LOGIN_STATUS_LOGINED = 1;

    static public volatile int mLoginStatus = 0; // read only, unlogin 0,


    public static final int STATUSES_IMAGE_MODE_SMALL = 0;
    public static final int STATUSES_IMAGE_MODE_BIG = 1;
    public static final int STATUSES_IMAGE_MODE_NO = -1;
    /**
     * 分享图片预览模式 
     */
    public static int statuses_image_mode = STATUSES_IMAGE_MODE_SMALL;
    

    public static String getPhoneNumber() {
        if (mPhoneNumber == null || mPhoneNumber.length() < 1) {
            mPhoneNumber = ConfigHelper.getInstance(mAppContext).loadKey(
                    ConfigHelper.CONFIG_KEY_PHONE_NUMBER);
        }
        return mPhoneNumber;
    }

    public static void setPhoneNumber(String mPhoneNumber) {
        GlobalUserInfo.mPhoneNumber = mPhoneNumber;
        ConfigHelper cHelper = ConfigHelper.getInstance(mAppContext);
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_PHONE_NUMBER, GlobalUserInfo.mPhoneNumber);
        cHelper.commit();
    }

    public static String getUID() {
        return mUID;
    }

    public static void setUID(String mUID) {
        GlobalUserInfo.mUID = mUID;
    }

    public static String getName() {
        if ("".equals(mNAME)) {
            mNAME = ConfigHelper.getInstance(mAppContext).loadKey(ConfigHelper.CONFIG_KEY_REALNAME);
        }
        return mNAME;
    }

    public static void setName(String mName) {
        GlobalUserInfo.mNAME = mName;
        ConfigHelper cHelper = ConfigHelper.getInstance(mAppContext);
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_REALNAME, GlobalUserInfo.mNAME);
        cHelper.commit();
    }

    public static String getAvatar() {
        if (IMUtil.isEmptyString(mAvatarName)) {
            mAvatarName = ConfigHelper.getInstance(mAppContext).loadKey(
                    ConfigHelper.CONFIG_KEY_AVATAR);
        }
        return mAvatarName;
    }

    public static void setAvatar(String avatar) {
        GlobalUserInfo.mAvatarName = avatar;
        ConfigHelper cHelper = ConfigHelper.getInstance(mAppContext);
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_AVATAR, GlobalUserInfo.mAvatarName);
        cHelper.commit();
    }

    public static int getUserStatus() {
        if (mStatus == null || "".equals(mStatus)) {
            return -1;
        } else {
            return Integer.valueOf(mStatus);
        }

    }

    public static void setUserStatus(int status) {
        mStatus = String.valueOf(status);
    }

    public static String getZoneCode() {
        return mZoneCode;
    }

    public static void setZoneCode(String code) {
        mZoneCode = code;
    }

    public static void initDefaultZoneCode() {
        mZoneCode = DEFAULT_ZONE_CODE;
    }


    public static String getAccessToken() {
        return mAccessToken;
    }

    public static String getRefreshToken() {
        return mRefreshToken;
    }

    public static int getExpireTS() {
        return mExpireTS;
    }

    public static void setOAuthToken(String accessToken, String refreshToken, int expireTS) {
        mAccessToken = accessToken;
        mRefreshToken = refreshToken;
        mExpireTS = expireTS;
        ConfigHelper cHelper = ConfigHelper.getInstance(mAppContext);
        cHelper.saveKey(ConfigHelper.CONFIG_ACCESS_TOKEN, mAccessToken);
        cHelper.saveKey(ConfigHelper.CONFIG_REFRESH_TOKEN, mRefreshToken);
        cHelper.saveIntKey(ConfigHelper.CONFIG_ACCESS_TOKEN_EXPIRE, mExpireTS);
        cHelper.commit();
    }

    public static void setOAuthInfo(OAuthInfo authInfo) {
        ConfigHelper cHelper = ConfigHelper.getInstance(mAppContext);

        cHelper.saveKey(ConfigHelper.CONFIG_ACCESS_TOKEN, authInfo.mAccessToken);
        cHelper.saveKey(ConfigHelper.CONFIG_REFRESH_TOKEN, authInfo.mRefreshToken);
        cHelper.saveIntKey(ConfigHelper.CONFIG_ACCESS_TOKEN_EXPIRE, authInfo.mExpireTS);
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_PHONE_NUMBER, authInfo.getMobile());
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_ZONE_CODE, authInfo.getZoneCode());
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_UID, authInfo.mUid);
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_REALNAME, authInfo.getUserName());
        cHelper.saveKey(ConfigHelper.CONFIG_KEY_AVATAR, authInfo.getAvatarName());

        mNAME = authInfo.getUserName();
        mAvatarName = authInfo.getAvatarName();
        mUID = authInfo.mUid;
        cHelper.commit();
    }

    // TODO WAITIING FOR DELETE
    public static String getSessionID() {
        if (mSessionID == null) {
            mSessionID = "";
        }
        return mSessionID;
    }

    // check configuration to see if user has logined
    // do this when program started
    public synchronized static int checkLoginStatus(Context context) {
        ConfigHelper cHelper = ConfigHelper.getInstance(context);
        String strStatus = cHelper.loadKey(ConfigHelper.CONFIG_KEY_LOGIN_STATUS);
        Log.d(TAG, "login status: " + strStatus);

        // if user logined, we get all config out
        if (strStatus.equals(String.valueOf(LOGIN_STATUS_LOGINED))) {
            mLoginStatus = LOGIN_STATUS_LOGINED;

            mAccessToken = cHelper.loadKey(ConfigHelper.CONFIG_ACCESS_TOKEN);
            mRefreshToken = cHelper.loadKey(ConfigHelper.CONFIG_REFRESH_TOKEN);
            mExpireTS = cHelper.loadIntKey(ConfigHelper.CONFIG_ACCESS_TOKEN_EXPIRE, 0);

            mPhoneNumber = cHelper.loadKey(ConfigHelper.CONFIG_KEY_PHONE_NUMBER);
            mZoneCode = cHelper.loadKey(ConfigHelper.CONFIG_KEY_ZONE_CODE);
            mNAME = cHelper.loadKey(ConfigHelper.CONFIG_KEY_REALNAME);
            mUID = cHelper.loadKey(ConfigHelper.CONFIG_KEY_UID);
            mAvatarName = cHelper.loadKey(ConfigHelper.CONFIG_KEY_AVATAR);


            mStatus = cHelper.loadKey(ConfigHelper.CONFIG_USER_STATUS);

            // 已成功登录，需设置Api认证信息
            OAuthInfo oAuthInfo = new OAuthInfo();
            oAuthInfo.setUid(mUID);
            oAuthInfo.setUserName(mNAME);
            oAuthInfo.setAvatarName(mAvatarName);
            oAuthInfo.mAccessToken = mAccessToken;
            oAuthInfo.mRefreshToken = mRefreshToken;
            oAuthInfo.mExpireTS = mExpireTS;
            oAuthInfo.setMobile(mPhoneNumber);
            oAuthInfo.setZoneCode(mZoneCode);

            MoMoHttpApi.setOAuthInfo(oAuthInfo);

        } else {
            mLoginStatus = LOGIN_STATUS_UNLOGIN;
        }
        return mLoginStatus;
    }

    public synchronized static void setLoginStatus(int nStatus) {
        ConfigHelper.getInstance(mAppContext).saveKey(ConfigHelper.CONFIG_KEY_LOGIN_STATUS,
                String.valueOf(LOGIN_STATUS_LOGINED));
        ConfigHelper.getInstance(mAppContext).commit();
        mLoginStatus = nStatus;
    }

    /**
     * check login status
     * 
     * @return
     */
    public synchronized static boolean hasLogined() {
        Log.d(TAG, "haslogined called: " + mLoginStatus);
        if (mLoginStatus == LOGIN_STATUS_UNLOGIN) {
            return false;
        } else {
            return true;
        }
    }



    /**
     * 获取当前登录用户手机区号
     * 
     * @param context
     * @return 手机区号
     */
    public static String getCurrentZoneCode(Context context) {
        String zoneCode = GlobalUserInfo.DEFAULT_ZONE_CODE;
        ConfigHelper configHelper = ConfigHelper.getInstance(context);
        zoneCode = configHelper.loadKey(ConfigHelper.CONFIG_KEY_ZONE_CODE);
        if (zoneCode.equals("")) {
            zoneCode = GlobalUserInfo.DEFAULT_ZONE_CODE;
        }
        return zoneCode;
    }


    public static boolean logout(Context context) {
        Log.d(TAG, "logout");

        // clear parameters
        mLoginStatus = LOGIN_STATUS_UNLOGIN;
        mUID = "";
        mPhoneNumber = "";
        mZoneCode = DEFAULT_ZONE_CODE;
        mNAME = "";
        mAvatarName = "";

        mStatus = "";
        mAvatarMap = null;


        // clear all configuration
        Log.d(TAG, "logout step1");
        ConfigHelper cHelper = ConfigHelper.getInstance(context);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_UID);
        cHelper.removeKey(ConfigHelper.CONFIG_OAUTH_KEY);
        cHelper.removeKey(ConfigHelper.CONFIG_OAUTH_SECRET);

        cHelper.saveKey(ConfigHelper.CONFIG_KEY_LOGIN_STATUS, String.valueOf(LOGIN_STATUS_UNLOGIN));
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_BINDED_ACCOUNT_NAME);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_BINDED_ACCOUNT_TYPE);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_REALNAME);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_AVATAR);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_ZONE_CODE);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_PHONE_NUMBER);
        cHelper.removeKey(ConfigHelper.CONFIG_USER_STATUS);
        cHelper.removeKey(ConfigHelper.CONFIG_QNAME);

        cHelper.removeKey(ConfigHelper.CONFIG_KEY_MESSAGE_RING);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_MESSAGE_SYSTEM_SOUND);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_MESSAGE_VIBRATE);
        cHelper.removeKey(ConfigHelper.CONFIG_KEY_GPRS_IMAGE);

        cHelper.removeKey(ConfigHelper.CONFIG_ACCESS_TOKEN);
        cHelper.removeKey(ConfigHelper.CONFIG_REFRESH_TOKEN);
        cHelper.removeKey(ConfigHelper.CONFIG_ACCESS_TOKEN_EXPIRE);
        // 去除主引导
        cHelper.removeKey(ConfigHelper.TAB_CONTACTS);
        cHelper.removeKey(ConfigHelper.TAB_IM);
        cHelper.removeKey(ConfigHelper.GUIDE_ISFIRST);

        cHelper.removeKey(ConfigHelper.SMS_COUNT);

        cHelper.removeKey(ConfigHelper.CONFIG_KEY_SKIP_VERSION);

        cHelper.removeKey(ConfigHelper.CONFIG_KEY_IM_AUTO_PLAY);

        cHelper.removeKey(ConfigHelper.LAST_TIME_UPDATE_USER_ID);

        cHelper.removeKey(ConfigHelper.CONFIG_KEY_IMPORT_ACCOUNTS);
        
        cHelper.removeKey(ConfigHelper.CONFIG_SID);

        cHelper.commit();

        // tell service to stop
        Log.d(TAG, "logout step2");
        context.sendBroadcast(new Intent(context.getString(R.string.action_message_destroy)));

        // clear statuses cache
        DynamicDB.initInstance(mAppContext);
        DynamicDB.instance().reset();
        DynamicMgr.getInstance().reset();        
        DraftMgr.instance().deleteAll();
        


        // clear database
        Log.d(TAG, "logout step5");
        //SyncManager.getInstance().emptyAllMoMoDatabase();



        return true;
    }

    public static void forceLogout(Context context) {
        Log.d(TAG, "logout");
        // clear parameters
        mLoginStatus = LOGIN_STATUS_UNLOGIN;
        mUID = "";
        mPhoneNumber = "";
        mZoneCode = DEFAULT_ZONE_CODE;
        mNAME = "";
        mAvatarName = "";

        mStatus = "";
        mAvatarMap = null;

        // clear all configuration
        Log.d(TAG, "logout step1");
        ConfigHelper cHelper = ConfigHelper.getInstance(context);
        cHelper.clearkeys();

        // tell service to stop
        Log.d(TAG, "logout step2");
        context.sendBroadcast(new Intent(context.getString(R.string.action_message_destroy)));
    }






    public static void setAppContext(Context c) {
        mAppContext = c.getApplicationContext();
    }

    public static Bitmap getMyAvatarInMemory() {
        return mAvatarMap;
    }

    public static void setMyAvatar(Bitmap imgAvatar) {
        mAvatarMap = imgAvatar;
    }

    /**
     * 内置浏览器打开momo相关网页
     * 
     * @param context
     * @param url
     * @param needTitle
     */
    public static void openMoMoUrl(Context context, String url, boolean needTitle) {
        Log.i(TAG, "openMoMoUrl: " + url);
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(WebViewActivity.EXTARS_WEBVIEW_URL, url);
        intent.putExtra(WebViewActivity.EXTARS_WEBVIEW_NEED_TITLE, needTitle);
        context.startActivity(intent);
    }

    public final static String PARAM_STATUSES_ID = "STATUSES_ID";
}

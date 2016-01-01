
package cn.com.nd.momo.model;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import android.graphics.Bitmap;
import android.graphics.Color;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.statuses.StatusesManager;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.AbsSdk.SdkResult;

public final class DynamicMgr {
    public static final int HTTP_OK = 200;

    public static final int MSG_BASE = 90000;
    public static final int MSG_POST_LOGIN = 100;

    public static final int MSG_UPLOAD_PREPARE = 110;

    public static final int MSG_UPLOAD_PROCESS = 111;

    public static final int MSG_UPLOAD_DONE = 112;

    public static final int MSG_UPLOAD_FAIL = 113;

    public static final int MSG_GET_BROADCAST = 120;

    public static final int MSG_GET_SINGLE_BROADCAST = 121;

    public static final int MSG_GETCOMMENT = 122;

    public static final int MSG_GETCOMMENT_CACHE = 123;

    public static final int MSG_GETMESSAGE_NEW = 124;

    public static final int MSG_GETABOUT_ME = 125;

    public static final int MSG_GET_GROUPS = 126;

    public static final int MSG_POST_BROADCAST = MSG_BASE + 130;

    public static final int MSG_POST_COMMENT = MSG_BASE + 131;

    public static final int MSG_POST_REPLY = MSG_BASE + 132;

    public static final int MSG_POST_PRISE = MSG_BASE + 134;

    public static final int MSG_POST_FAV = 135;

    public static final int MSG_POST_DELETE_DYNAMIC = 136;

    public static final int MSG_POST_DELETE_COMMENT = 137;

    public static final int MSG_POST_BROADCAST_PROCESS = 138;

    public static final int MSG_POST_BROADCAST_FAIL = 139;
    
    public static final int MSG_POST_HIDE_DYNAMIC  = 140;

    public static final int MSG_POST_FAV_DYNAMIC  = 141;
    
    public static final int MSG_DOWNLOAD_AVATAR = 170;

    public static final int MSG_DOWNLOAD_IMG = 171;

    public static final int MSG_DOWNLOAD_HOME_AVATAR = 172;

    public static final String APPID = "1";

    public static final int SIZE_AVATAR = 60;

    private static DynamicMgr mInstance = null;

    public synchronized static DynamicMgr getInstance() {
        if (mInstance == null) {
            mInstance = new DynamicMgr();
        }
        return mInstance;
    }

    public synchronized void reset() {
        isLoaded = false;
    }

    private static Object mRefreshLock = new Object();

    /**
     * <br>
     * Description: 刷新 好友跟群组列表 <br>
     * Author:hexy <br>
     * Date:2011-4-11下午06:04:05
     * 
     * @return
     */

    private boolean isLoaded = false;

    public boolean isLoaded() {
        return isLoaded;
    }

    public void refresh() {
        refreshGroup();
        isLoaded = true;
    }

    /**
     * <br>
     * Description:刷新群列表 <br>
     * Author:hexy <br>
     * Date:2011-4-11下午06:05:14
     */
    public void refreshGroup() {
        synchronized (mRefreshLock) {
            mGroupList = getGroupListFromNet();
        }
    }

    ArrayList<GroupInfo> mGroupList = new ArrayList<GroupInfo>();

    public void addItem(GroupInfo info) {
        mGroupList.add(info);
    }

    public GroupInfo getGroupItem(long id) {
        if (mGroupList == null)
            return null;
        for (int i = 0; i < mGroupList.size(); i++) {
            if (mGroupList.get(i).getGroupID() == id) {
                return mGroupList.get(i);
            }
        }
        return null;
    }

    public ArrayList<GroupInfo> getGroups() {
        return mGroupList;
    }

    private ArrayList<GroupInfo> getGroupListFromNet() {
        ArrayList<GroupInfo> groupList = new ArrayList<GroupInfo>();
        try {
            groupList.clear();
            groupList = StatusesManager.getGroupList();
        } catch (MoMoException e) {
            e.printStackTrace();
        }

        return groupList;
    }

    /**
     * <br>
     * Description:根据服务端返回值.显示错误信息 <br>
     * Author:hexy <br>
     * Date:2011-7-21上午10:38:17
     * 
     * @param result
     * @return
     * @throws JSONException
     */
    public static String getErrorCode(SdkResult result) throws JSONException {
        String errorMsg = "请求错误";

        if (result.ret == HttpStatus.SC_NOT_FOUND) {
            errorMsg = "资源不存在";
        }

        return errorMsg;
    }

    /**
     * <br>
     * Description:传入用户id 跟头像地址.获取头像位图 <br>
     * Author:hexy <br>
     * Date:2011-7-21下午03:00:51
     * 
     * @param uid 从数据库里面获取头像
     * @param avatarUrl 可传可不传, 如果数据库里面没有头像 1 如果有传 avatarUrl. 则使用此url 到服务端获取头像 2
     *            如过没有传avatarurl,则使用id获取头像地址后.再从服务端获取头像
     * @return 圆角位图
     */
    public Bitmap getAvaterBitmapOrigin(long uid, String avatarUrl) {
        return null;
    }

    public Bitmap getAvaterBitmapWithFrame(long uid, String avatarUrl) {
        Bitmap bmp = getAvaterBitmapOrigin(uid, avatarUrl);
        if (bmp != null) {
            bmp = BitmapToolkit.compress(bmp, ConfigHelper.SZIE_AVATAR);
            bmp = BitmapToolkit.cornerBitmap(bmp, 8, Color.TRANSPARENT);
        }

        return bmp;
    }

    public Bitmap getAvaterBitmap(long uid, String avatarUrl) {
        Bitmap bmp = getAvaterBitmapOrigin(uid, avatarUrl);

        if (bmp != null) {
            bmp = BitmapToolkit.compress(bmp, ConfigHelper.SZIE_AVATAR);
            bmp = BitmapToolkit.cornerBitmap(bmp, 8);
        }

        return bmp;
    }

    private String getAvatarUrl(long userID) {
        // ToDo Zgx 20120807
        // DynamicSdk sdk = new DynamicSdk();
        // SdkResult result = sdk.getUserCard(userID);

        String avatarUrl = null;
        // if (result.ret == HTTP_OK) {
        // try {
        // JSONObject obj = new JSONObject(result.response);
        // avatarUrl = obj.optString("avatar");
        // } catch (JSONException e) {
        // // TODO: handle exception
        // }
        // }
        return avatarUrl;
    }
}


package cn.com.nd.momo.manager;

import android.graphics.Bitmap;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;

public class AvatarManager {

    private static final String TAG = "AvatarManager";

    public static Bitmap getAvaterBitmap(long uid, String avatarUrl) {
        if(avatarUrl != null) {
            avatarUrl = avatarUrl.replace("_48.jpg", "_130.jpg");
        }
        Bitmap bmp = getAvaterBitmapOrigin(uid, avatarUrl);

        if (bmp != null) {
            bmp = BitmapToolkit.compress(bmp, ConfigHelper.SZIE_AVATAR);
            bmp = BitmapToolkit.cornerBitmap(bmp, 8);
        }

        return bmp;
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
    public static Bitmap getAvaterBitmapOrigin(long uid, String avatarUrl) {
        Bitmap bmp = null;
        String error = "";
        do {
            // load from remote
            if (avatarUrl == null || avatarUrl.length() < 5) {
                avatarUrl = getAvatarUrl(uid);
            }

            Log.i(TAG, "load avatar url:" + avatarUrl);
            if (avatarUrl != null && avatarUrl.length() > 0) {
                BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_PHOTO, avatarUrl, "",
                        ".small.avatar");
                bmp = bt.loadBitmapNetOrLocalScale(ConfigHelper.SZIE_AVATAR);
                if (bmp == null) {
                    error = "getAvatarBitmapFromNet(avatarUrl) return null";
                }
            }
        } while (false);

        if (bmp == null) {
            Log.e(TAG, "\ngetAvaterBitmap" + uid + "\nurl" + avatarUrl + "\nerror" + error);
        }

        return bmp;
    }

    /**
     * @param 使用此url到服务端获取头像
     * @return 头像位图
     */
    public static Bitmap getBigAvaterBitmapOrigin(String avatarUrl) {
        Bitmap bmp = null;
        String error = "";
        Log.i(TAG, "load avatar url:" + avatarUrl);
        if (avatarUrl != null && avatarUrl.length() > 0) {
            BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_PHOTO, avatarUrl, "",
                    ".big.avatar");
            bmp = bt.loadBitmapNetOrLocalScale(780);
            if (bmp == null) {
                Log.e(TAG, "\ngetAvaterBitmap \nurl" + avatarUrl + "\nerror" + error);
            }
        }
        return bmp;
    }

    private static String getAvatarUrl(long userID) {
        String avatarUrl = null;
        try {
            cn.com.nd.momo.api.types.Contact contact = MoMoHttpApi.getUserCardByID(userID);

            avatarUrl = contact.getAvatar().getServerAvatarURL();
        } catch (MoMoException e) {
            //
        }
        return avatarUrl;
    }
}

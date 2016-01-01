
package cn.com.nd.momo.util;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;

import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.util.StartForResults.PickData;

/**
 * @author wuyq
 */
public class IMUtil {
    private static final String TAG = "IMUtil";

    private static final String REGEX_HTML = "(?s)<[oO]{1}[ptinPTINOo]{5}[^>]*>|<[oO]{1}[lL]{1}[^>]*>|(?!<[oO]{1})<[^oO<\\w=@;:.]*[\\w\\/]+.*?>";

    public static String noHTML(String text) {
        if (text == null)
            return "";
        else
            return text.replaceAll("\\<[\\w]+.*?>", "");
    }

    /**
     * 过滤掉html文本，但表情不过滤
     * 
     * @param text
     * @return
     */
    public static String noHTMLButSmiley(String text) {
        if (text == null)
            return "";
        return text.replaceAll(REGEX_HTML, "");
    }

    public static boolean isNetworkAvaliable(Context ctx, boolean show) {
        boolean result = false;
        String net = Utils.getActiveNetWorkName(ctx);
        if (net != null) {
            // Log.i(TAG, "network is ok: " + net);
            result = true;
        } else {
            Log.w(TAG, "there is no network connection");
            if (show)
                Toast.makeText(ctx, "未找到可用的网络链接。", 5000).show();
            result = false;
        }
        return result;
    }

    public static boolean isNetworkAvaliable(Context ctx) {
        return isNetworkAvaliable(ctx, false);
    }





    /**
     * 把字符窜转为unicode
     * 
     * @param str
     * @return
     */
    public static String convertToUnicode(String str) {
        StringBuffer ostr = new StringBuffer();

        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            if ((ch >= 0x0020) && (ch <= 0x007e)) // Does the char need to be
            // converted to unicode?
            {
                ostr.append(ch); // No.
            } else // Yes.
            {
                ostr.append("\\u"); // standard unicode format.
                String hex = Integer.toHexString(str.charAt(i) & 0xFFFF); // Get
                // hex
                // value
                // of
                // the
                // char.
                for (int j = 0; j < 4 - hex.length(); j++)
                    // Prepend zeros because unicode requires 4 digits
                    ostr.append("0");
                ostr.append(hex.toLowerCase()); // standard unicode format.
                // ostr.append(hex.toLowerCase(Locale.ENGLISH));
            }
        }

        return (new String(ostr)); // Return the stringbuffer cast as a string.

    }

    // FIXME:maybe use TextUtils.isEmpty(String str) to instead
    public static boolean isNotEmptyString(String str) {
        return !isEmptyString(str);
    }

    public static boolean isEmptyString(String str) {
        return str == null || str.length() == 0 || str.equals("null");
    }

    public static String emptyIfNull(String str) {
        return str != null ? str : "";
    }



    /**
     * size 0的时候表示原图
     * 
     * @param url
     * @param size
     * @return
     */
    public static String pictureUrl(String url, int size) {
        return url.replaceFirst("(?s)_\\d{2,4}.jpg", size == 0 ? ".jpg" : "_"
                + size + ".jpg");
    }

    /**
     * 构造
     * 
     * @return
     */
    public static User myself() {
        User sender = new User();
        sender.setId(GlobalUserInfo.getUID());
        sender.setName(GlobalUserInfo.getName());
        sender.setAvatar(GlobalUserInfo.getAvatar());
        sender.setMobile(GlobalUserInfo.getPhoneNumber());
        sender.setZoneCode(GlobalUserInfo.getCurrentZoneCode(Utils.getContext()));
        return sender;
    }


    /**
     * 附加状态信息到json对象里
     */
    public static JSONObject addInfoToMQMessage(JSONObject json, int state,
            long timestamp) {
        if (json != null) {
            try {
                json.getJSONObject("data").put("state", state);
                if (timestamp != 0)
                    json.getJSONObject("data").put("timestamp", timestamp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json;
    }

    /**
     * 本地cache工具辅助
     * 
     * @param url
     * @return
     */
    public static BitmapToolkit getAudioBitmapToolkit(String url) {
        BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_IM_AUDIO,
                url, "", ".momo.audio");
        bt.mkdirsIfNotExist();
        return bt;
    }

    public static BitmapToolkit getPictureBitmapToolkit(String url) {
        BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_IM_PICTURE,
                url, "", ".momo.pic");
        bt.mkdirsIfNotExist();
        return bt;
    }

    public static BitmapToolkit getLocationBitmapToolkit(String url) {
        BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_IM_MAP,
                url, "", ".momo.map");
        bt.mkdirsIfNotExist();
        return bt;
    }

    /**
     * 是否本地文件
     * 
     * @param url
     * @return
     */
    public static boolean isLocalUrl(String url) {
        return IMUtil.isEmptyString(url) || !(url.startsWith("http"));
    }



    /**
     * 文件大小显示
     */
    private static final long K = 1024;

    private static final long M = K * K;

    private static final long G = M * K;

    private static final long T = G * K;

    public static String convertToStringRepresentation(final long value) {
        final long[] dividers = new long[] {
                T, G, M, K, 1
        };
        final String[] units = new String[] {
                "TB", "GB", "MB", "KB", "B"
        };
        if (value < 1)
            throw new IllegalArgumentException("Invalid file size: " + value);
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            final long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(final long value, final long divider,
            final String unit) {
        final double result = divider > 1 ? (double)value / (double)divider
                : (double)value;
        return String.format("%.1f %s", Double.valueOf(result), unit);
    }



    /**
     * 推送短信频繁提醒配置
     */
    private static final String IM_SMS_TIME_SPLIT = "_";

    private static String getSMSTimeKEY(String other) {
        return ConfigHelper.CONFIG_KEY_IM_LAST_SMS_TIME + IM_SMS_TIME_SPLIT
                + GlobalUserInfo.getUID() + IM_SMS_TIME_SPLIT + other;
    }



    public static void clearSmsTime(Context context, String other) {
        ConfigHelper configHelper = ConfigHelper.getInstance(context);
        configHelper.removeKey(getSMSTimeKEY(other));
        configHelper.commit();
    }

    public static void setSmsTime(Context context, String other) {
        ConfigHelper configHelper = ConfigHelper.getInstance(context);
        configHelper.saveLongKey(getSMSTimeKEY(other), new Date().getTime());
        configHelper.commit();
    }

    /**
     * 显示浮动提醒栏
     * 
     * @param anchor
     * @param msg
     */
    public static void showFloating(View anchor, String msg) {
        try {
            Context mContext = anchor.getContext();

            final PopupWindow win = new PopupWindow(mContext);

            TextView mRootView = new TextView(mContext);
            mRootView.setText(msg);
            mRootView.setTextColor(0xff246a7e);
            int height = Utils.dipToPx(mContext, 45);
            mRootView.setWidth(WindowManager.LayoutParams.FILL_PARENT);
            mRootView.setHeight(height);
            mRootView.setGravity(Gravity.CENTER);
            mRootView.setBackgroundResource(R.drawable.dynamic_list_new_bg);

            win.setContentView(mRootView);
            win.setWidth(WindowManager.LayoutParams.FILL_PARENT);
            win.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            Rect anchorRect = new Rect(location[0], location[1], location[0]
                    + anchor.getWidth(), location[1] + anchor.getHeight());

            mRootView.measure(WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);

            int rootHeight = mRootView.getMeasuredHeight();

            int rootWidth = mRootView.getMeasuredWidth();

            WindowManager mWindowManager = (WindowManager)mContext
                    .getSystemService(Context.WINDOW_SERVICE);

            int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
            int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

            int xPos, yPos;

            // automatically get X coord of popup (top left)
            if ((anchorRect.left + rootWidth) > screenWidth) {
                xPos = anchorRect.left - (rootWidth - anchor.getWidth());
                xPos = (xPos < 0) ? 0 : xPos;
            } else {
                if (anchor.getWidth() > rootWidth) {
                    xPos = anchorRect.centerX() - (rootWidth / 2);
                } else {
                    xPos = anchorRect.left;
                }
            }

            int dyTop = anchorRect.top;
            int dyBottom = screenHeight - anchorRect.bottom;

            boolean onTop = (dyTop > dyBottom) ? true : false;

            if (onTop) {
                if (rootHeight > dyTop) {
                    yPos = 15;
                } else {
                    yPos = anchorRect.top - rootHeight;
                }
            } else {
                yPos = anchorRect.bottom;

            }

            win.setAnimationStyle(R.style.Animations_FloatingNotify);

            win.showAtLocation(anchor, Gravity.NO_GRAVITY, 0, yPos);

            // set windows bg to alpha 0
            View parent = (View)mRootView.getParent();
            if (parent != null) {
                parent.setBackgroundColor(mContext.getResources().getColor(
                        R.color.transparent));
            }

            new Timer().schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        win.dismiss();
                    } catch (Exception e) {
                    }
                }
            }, 3000);
        } catch (Exception e) {
        }
    }

    /**
     * 根据客户端id获取客户端名字
     * 
     * @param clientid
     * @return
     */
    public static String getClientName(int clientid) {
        switch (clientid) {
            case 0:
            default:
                return "momo.im网站";
            case 1:
                return "Android版";
            case 2:
                return "iPhone版";
            case 3:
                return "Windows Mobile版";
            case 4:
                return "S60v3版";
            case 5:
                return "S60v5版";
            case 6:
                return "Java版";
            case 7:
                return "webOS版";
            case 8:
                return "BlackBerry版";
            case 9:
                return "iPad版";
            case 10:
                return "网站手机版";
            case 11:
                return "手机触屏版";
            case 12:
                return "手机短信";
        }
    }

    public static interface CheckNameListener {
        public void onComplete();

        public void onCancel();
    }




    /**
     * 打开momo需要认证的url
     * 
     * @param context
     * @param url
     */
    public static void openMomoUrl(Context context, String url) {
        String all = url + (url.indexOf("?") >= 0 ? "&" : "?") + "oauth_token="
                + GlobalUserInfo.getOAuthKey()
                + "&oauth_token_secret="
                + GlobalUserInfo.getOAuthSecret() + "&timestamp="
                + (new Date().getTime() / 1000);
        Log.i(TAG, "show momo url" + all);
        GlobalUserInfo.openMoMoUrl(context, all, false);
    }


    /**
     * 显示地图详细信息
     * 
     * @param mContext
     * @param content
     */
    private static final String FLURRY_IM_MAP_FAIL = "MQ发送消息失败";

    private static final String LOCATION_AMAP = "高德导航";

    private static final String LOCATION_GMAP = "谷歌导航";

    private static final String LOCATION_OTHER = "其他地图";

    private static final String LOCATION_KCODE = "查看K码";

    private static final CharSequence[] locationItems = new CharSequence[] {
            LOCATION_AMAP, LOCATION_GMAP, LOCATION_OTHER, LOCATION_KCODE
    };




    /**
     * 拍照选照片完统一处理生成本地缩略图
     * 
     * @param activity
     * @param requestCode
     * @param resultCode
     * @param paramIntent
     * @return
     */
    public static String onPhotoData(final Activity activity,
            int requestCode,
            int resultCode, Intent paramIntent) {
        // 取得图片数据
        final PickData pickedPhotoData = StartForResults.onRresult(
                activity, requestCode, resultCode, paramIntent);
        if (pickedPhotoData == null) {
            return null;
        }

        /**
         * step 1 生成缩略图先放在缓存里，不需要再从网络取缩略图
         */
        Bitmap thumb = pickedPhotoData.getThumbBitmap(130);
        if (thumb != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            thumb.compress(Bitmap.CompressFormat.JPEG, 80, stream);
            byte[] bytes = stream.toByteArray();

            final String oPath = IMUtil.getPictureBitmapToolkit(
                    pickedPhotoData.localPath).getAbsolutePath();

            FileOutputStream fileOutPutStream = null;
            try {
                fileOutPutStream = new FileOutputStream(oPath);
                fileOutPutStream.write(bytes, 0, bytes.length);
            } catch (Exception e) {
                Log.e(TAG, "saveBitmap" + e.toString());
            }

            try {
                if (fileOutPutStream != null)
                    fileOutPutStream.close();
            } catch (Exception e) {
            }
        }

        return pickedPhotoData.localPath;
    }

    /**
     * 优先打开海豚浏览器
     * 
     * @param context
     * @param url
     */
    public static void openUrl(Context context, String url) {
        Log.i(TAG, "openUrl: " + url);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 默认用海豚浏览器打开
        intent.setClassName("com.dolphin.browser.cn", "mobi.mgeek.TunnyBrowser.BrowserActivity");
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            // 海豚浏览器没有安装，让用户选择浏览器去打开链接
            intent.setComponent(null);
            try {
                context.startActivity(intent);
            } catch (Exception e2) {
                // 没有安装任何的浏览器或者链接有问题
            }
        }
    }



    /**
     * 调用系统播放声音或者视频
     * 
     * @param context
     * @param uri
     */
    public static void playVideoOrAudio(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }



    /**
     * K码开始--------------------------------------------------
     */
    private static final String KCodeArea = "0123456789abcdefghijkmnpqrstuvwxyz";

    public static String numberToKCode(double degree) {
        String result = "";
        double second = degree * 60 * 60;
        int minSecond = (int)(second * 10 + 0.5);

        while (minSecond > 0) {
            int number = minSecond % 34;
            result += KCodeArea.charAt(number);
            minSecond = minSecond / 34;
        }
        return result;
    }

    public static String gpsToKCode(double lat, double lng) {
        String result = "";
        if (lng < 70 || lng > 140) {
            return "";
        }
        if (lat < 5 || lat > 75) {
            return result;
        }
        if (lng >= 105) {
            if (lat >= 40) {
                result = "5";
            } else {
                result = "8";
            }
        } else {
            if (lat >= 40) {
                result = "6";
            } else {
                result = "7";
            }
        }

        double lngDelta = lng - 70;
        if (lngDelta > 35) {
            lngDelta = lngDelta - 35;
        }

        double latDelta = lat - 5;
        if (latDelta > 35) {
            latDelta = latDelta - 35;
        }

        result += (numberToKCode(lngDelta) + numberToKCode(latDelta));
        if (result.length() == 9) {
            String split = " - ";
            return result.substring(0, 3) + split + result.substring(3, 6) + split
                    + result.substring(6, 9);
        }
        return result;
    }

}


package cn.com.nd.momo.util;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import cn.com.nd.momo.api.util.FastDateFormat;
import cn.com.nd.momo.manager.GlobalUserInfo;

public final class Utils {

    public static void hideKeyboard(Context ctx, View view) {
        if (null == view)
            return;
        InputMethodManager imm = (InputMethodManager)ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void showKeyboard(final Context context, final EditText txt) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ((InputMethodManager)context
                        .getSystemService(Context.INPUT_METHOD_SERVICE))
                        .showSoftInput(txt, 0);
            }
        }, 100);
    }

    /**
     * 显示/隐藏 对话框
     */
    private static AlertDialog mAD = null;

    public static void hideWaitDialog() {
        if (mAD != null) {
            mAD.dismiss();
            mAD = null;
        }
    }

    public static void showWaitDialog(String txt, Context context) {
        if (mAD == null) {
            mAD = new AlertDialog.Builder(context).setTitle(txt)
                    .setIcon(android.R.drawable.ic_dialog_info).show();
        } else {
            mAD.dismiss();
            mAD = null;
        }
    }

    public static void DialogNetWork(final Activity context) {
        new AlertDialog.Builder(context)
                .setTitle("网络未连接,请设置网络")
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("设置网络",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                context
                                        .startActivityForResult(
                                                new Intent(
                                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS),
                                                0);
                            }
                        })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 压缩获取本地图片, size为边界
     */
    public static Bitmap getLocalBitmap(String path, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.outHeight = size;

        options.inJustDecodeBounds = true;

        // 获取bitmap宽高
        Bitmap bm = BitmapFactory.decodeFile(path, options);

        options.inJustDecodeBounds = false;
        int be = options.outHeight / (size / 10);
        if (be % 10 != 0)
            be += 10;

        be = be / 10;
        if (be <= 0)
            be = 1;

        options.inSampleSize = be;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }

    /**
     * 将时间数值转化为YYYY/MM/DD HH:MM:SS格式
     */
    public static String formatDateToNormalRead(String milliseconds) {
        String style = "yyyy/MM/dd HH:mm:ss";
        long value = Long.parseLong(milliseconds);
        FastDateFormat df = FastDateFormat.getInstance(style, null, null);
        return df.format(new Date(value));
    }

    public static String formatSize2String(long size) {
        DecimalFormat df = new DecimalFormat("0.00");
        String mysize = "";
        if (size > 1024 * 1024) {
            mysize = df.format(size / 1024f / 1024f) + "M";
        } else if (size > 1024) {
            mysize = df.format(size / 1024f) + "K";
        } else {
            mysize = size + "B";
        }
        return mysize;
    }

    /**
     * 拨打电话
     * 
     * @param number 电话号码
     */
    public static void placeCall(final String number, final Context context) {
        if (null == number || number.length() < 1)
            return;
        Intent intent = new Intent(Intent.ACTION_VIEW,// ACTION_CALL_PRIVILEGED
                Uri.fromParts("tel", number, null));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean mIsContactCount = false;

    public static String getMOMOFilePath(int pathType) {
        String absPath = Environment.getExternalStorageDirectory().getPath()
                + "/momo/" + GlobalUserInfo.getUID();
        switch (pathType) {
            case PATH_PHOTO:
                absPath += "/photo/";
                break;
            case PATH_AUDIO:
                absPath += "/audio/";
                break;
            case PATH_FIEL:
                absPath += "/file/";
                break;
            case PATH_MAP:
                absPath += "/map/";
                break;
            case PATH_VIDEO:
                absPath += "/video/";
                break;
            default:
                ;
        }
        File destDir = new File(absPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return absPath;
    }

    public final static int PATH_PHOTO = 0;

    public final static int PATH_AUDIO = 1;

    public final static int PATH_FIEL = 2;

    public final static int PATH_MAP = 3;

    public final static int PATH_VIDEO = 4;

    /**
     * 将epoch time stamp 时间转换为易读的字符串
     * 
     * @param timeValue standard epoch time
     * @return
     */
    public static String formateDateToEasyReadForSecUse(long timeValue) {
        return formateDateToEasyReadForIMUse(Long.toString(timeValue) + "000");
    }

    /**
     * @param timeValue
     * @return 格式化后的时间
     */
    public static String formateDateToEasyReadForIMUse(String timeValue) {
        long callTime = Long.parseLong(timeValue);
        SimpleDateFormat hmf = new SimpleDateFormat("HH:mm");
        String result = "";
        Calendar currentDate = Calendar.getInstance();
        Calendar callDate = Calendar.getInstance();
        callDate.setTimeInMillis(callTime);
        int secondsPast = currentDate.get(Calendar.SECOND)
                - callDate.get(Calendar.SECOND);
        int minutePast = currentDate.get(Calendar.MINUTE)
                - callDate.get(Calendar.MINUTE);
        int hourPast = currentDate.get(Calendar.HOUR_OF_DAY)
                - callDate.get(Calendar.HOUR_OF_DAY);
        int dayPast = currentDate.get(Calendar.DAY_OF_YEAR)
                - callDate.get(Calendar.DAY_OF_YEAR);
        int yearPast = currentDate.get(Calendar.YEAR)
                - callDate.get(Calendar.YEAR);

        if (yearPast > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            result = sdf.format(callDate.getTime());
        } else if (dayPast >= 7) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm");
            result = sdf.format(callDate.getTime());

        } else if (dayPast >= 3 && (dayPast < 7)) {
            result = new StringBuilder().append(dayPast).append("天前")
                    .append(hmf.format(callDate.getTime())).toString();
        } else if (dayPast >= 2 && dayPast < 3) {
            result = new StringBuilder("前天").append(
                    hmf.format(callDate.getTime())).toString();
        } else if (dayPast >= 1 && dayPast < 2) {
            result = new StringBuilder("昨天").append(
                    hmf.format(callDate.getTime())).toString();
        } else if (hourPast > 0) {
            result = hourPast + "小时前";
        } else if (minutePast > 0) {
            result = minutePast + "分钟前";
        } else if (secondsPast > 0) {
            result = secondsPast + "秒前";
        } else {
            result = "刚刚";
        }

        return result;
    }

}

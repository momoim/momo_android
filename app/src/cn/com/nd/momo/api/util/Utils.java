
package cn.com.nd.momo.api.util;

import java.io.File;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.BaseColumns;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.util.TypedValue;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.UserList;
import cn.com.nd.momo.manager.GlobalUserInfo;

/**
 * 通用工具类，以static方式提供，调用者不能继承使用，也不能实例化。
 * 
 * @author chenjp
 */
public final class Utils {


    private static ConfigHelper config;

    private static Context mContext;
    
    private static Bundle mBundle = null;

    private Utils() {
        // 不让调用者继承来调用，以保护类的封装
    }

    public static void saveGlobleContext(Context context) {
        mContext = context;
        config = ConfigHelper.getInstance(mContext);
    }

    public static Context getContext() {
        return mContext;
    }

    /**
     * 如果字串对象为NULL,则让其返回空串
     * 
     * @param str
     * @return
     */
    public static String setStringToBlankIfNull(String str) {
        return (null == str ? "" : str);
    }

    public static String getMD5OfBytes(byte[] buffer) {
        String result = "";

        MessageDigest complete;
        try {
            complete = MessageDigest.getInstance("MD5");
            byte[] b = complete.digest(buffer);
            for (int i = 0; i < b.length; i++) {
                result += Integer.toString((b[i] & 0xff) + 0x100, 16)
                        .substring(1);
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String stringToMD5(String str) {
        String result = "";
        byte[] bytes = str.getBytes();
        MessageDigest complete;
        try {
            complete = MessageDigest.getInstance("MD5");
            byte[] digests = complete.digest(bytes);
            BigInteger bi = new BigInteger(1, digests);
            result = bi.toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 这个方法来自apache langs
     */
    public static String replaceChars(String str, String searchChars,
            String replaceChars) {
        if (isEmpty(str) || isEmpty(searchChars)) {
            return str;
        }
        if (replaceChars == null) {
            replaceChars = EMPTY;
        }
        boolean modified = false;
        int replaceCharsLength = replaceChars.length();
        int strLength = str.length();
        StringBuilder buf = new StringBuilder(strLength);
        for (int i = 0; i < strLength; i++) {
            char ch = str.charAt(i);
            int index = searchChars.indexOf(ch);
            if (index >= 0) {
                modified = true;
                if (index < replaceCharsLength) {
                    buf.append(replaceChars.charAt(index));
                }
            } else {
                buf.append(ch);
            }
        }
        if (modified) {
            return buf.toString();
        }
        return str;
    }

    /**
     * 这个方法来自apache langs
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 这个方法来自apache langs
     * 
     * @since 2.0
     */
    private static final String EMPTY = "";

    /**
     * 简单实现字符串的分隔,摘自apache lang里的StringUtils.java，但修改了排除空字符
     */
    public static String[] split(final String strToSplit, final char separator) {
        if (strToSplit.length() < 1)
            return new String[0];
        List<String> matchList = new ArrayList<String>();
        int len = strToSplit.length();
        int i = 0, start = 0;
        while (i < len) {
            if (strToSplit.charAt(i) == separator) {
                String subStr = strToSplit.substring(start, i);
                if (subStr.length() > 0)
                    matchList.add(subStr);
                start = ++i;
                continue;
            }
            i++;
        }
        if (start < len) {
            String subStr = strToSplit.substring(start, i);
            if (subStr.length() > 0)
                matchList.add(subStr);
        }
        return matchList.toArray(new String[matchList.size()]);
    }


    private static String generateAccountFilterString(String accountName,
            String accountType) {
        if (null == accountName || null == accountType)
            return "";
        StringBuilder filter = new StringBuilder(100);
        filter.append("((account_name = '")
                .append(accountName)
                .append("' AND account_type = '")
                .append(accountType)
                .append("') OR (account_name IS NULL AND account_type IS NULL))");
        return filter.toString();
    }



    public static boolean checkVerdor(String accountName, String accountType) {
        String[] projection = {
                BaseColumns._COUNT
        };
        String selection = RawContacts.ACCOUNT_NAME + "= ? AND "
                + RawContacts.ACCOUNT_TYPE + "=?";
        boolean ret = false;
        String[] selectionArgs = {
                accountName, accountType
        };
        Cursor cursor = mContext.getContentResolver().query(
                RawContacts.CONTENT_URI, projection, selection, selectionArgs,
                null);
        if (null != cursor && cursor.moveToNext()) {
            ret = (cursor.getInt(0) == 0) ? false : true;
            cursor.close();
            cursor = null;
        }
        return ret;
    }










    // android 2.2 begin
    private static final int FLAG_EXTERNAL_STORAGE = 0x00040000;

    public static boolean isInstallOnSDCard() {
        PackageManager pm = mContext.getPackageManager();
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(mContext.getPackageName(), 0);
            int code = ApplicationInfo.FLAG_HAS_CODE;
            Log.d("Utils", "code:" + code);
            /*
             * //for android 2.2 if((appInfo.flags &
             * ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) { return true; }
             */
            Log.d("Utils", "application source directory:" + appInfo.sourceDir);
            int sysVersion = Integer.parseInt(VERSION.SDK);
            if (sysVersion > 7 && (appInfo.flags & FLAG_EXTERNAL_STORAGE) != 0) {
                return true;
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 去调与电话号码无关的字符
     * 
     * @param phoneNumber
     * @return 注意：为了效率起见，调用者需自己判断参数的长度大于0
     */
    public static String stripCharsInculdePlus(String phoneNumber) {
        if (null == phoneNumber || phoneNumber.length() < 1)
            return "";
        String phoneNumberAfterRetriped = PhoneNumberUtils
                .stripSeparators(phoneNumber);
        return phoneNumberAfterRetriped;
    }

    /**
     * @return is SD card full storage
     */
    public static boolean isFullStorage() {
        if (!isMediaMounted())
            return true;

        File path = Environment.getExternalStorageDirectory();
        // 取得sdcard文件路径
        StatFs statfs = new StatFs(path.getPath());
        // 获取block的SIZE
        long blocSize = statfs.getBlockSize();
        // 己使用的Block的数量
        long availaBlock = statfs.getAvailableBlocks();
        return availaBlock * blocSize < 1048576;
    }

    /**
     * @return is SD card available
     */
    public static boolean isMediaMounted() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static int dipToPx(Context context, float dip) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dip, context.getResources().getDisplayMetrics());
    }

    /**
     * 发送短信
     * 
     * @param phoneNumber
     */
    public static void sendMsg(String phoneNumber) {
        if (phoneNumber.length() > 0) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("sms:"
                    + phoneNumber));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else {
            displayToast("短信接收方号码为空！", 0);
        }
    }

    /**
     * 显示toast
     * 
     * @param ，0为短显示，1为长显示
     */
    public static void displayToast(final String strToDisplay, int delay) {
        Toast.makeText(Utils.mContext, strToDisplay,
                delay == 0 ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG).show();
    }

    /**
     * 显示toast
     * 
     * @param ，0为短显示，1为长显示
     */
    public static void displayToast(final String strToDisplay) {
        displayToast(strToDisplay, 1);
    }

    /**
     * 私聊群发列表对应的ID
     * 
     * @param receiver
     * @return
     */
    public static String getIMGroupID(UserList receiver) {
        if (receiver.size() > 1) {
            String md5 = stringToMD5(receiver.getId());
            return md5;
        } else {
            return receiver.getId();
        }
    }

    /**
     * 判断网络是否可用
     * 
     * @param context
     * @return
     */
    public static String getActiveNetWorkName(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        String result = null;
        do {
            if (connectivity == null) {
                break;
            }

            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {

                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        result = info[i].getTypeName();
                    }
                }
            }
        } while (false);
        Log.i("getActiveNetWorkName : " + result);
        return result;
    }

    /**
     * 判断wifi是否可用
     * 
     * @param context
     * @return
     */
    public static boolean isWifiEnable(Context context) {
        WifiManager wifiManager = (WifiManager)context
                .getSystemService(Context.WIFI_SERVICE);

        Log.i("wifiEnable" + wifiManager.isWifiEnabled());
        return wifiManager.isWifiEnabled();
    }
    
    public static void showMessageDialog(Context context, String title, String message) {
        showMessageDialog(context, title, message, null);
    }

    public static void showMessageDialog(Context context, String title, String message,
            final OnDialogDismissListener listener) {
        Builder builder = new AlertDialog.Builder(context);
        if (title != null && title.length() > 0) {
            builder.setTitle(title);
        }
        builder.setMessage(message);
        builder.setPositiveButton(context.getString(R.string.txt_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (listener != null) {
                            listener.onOk();
                        }
                    }
                });
        builder.create().show();
    }
    
    public interface OnDialogDismissListener {
        void onOk();
    }
    
    public static boolean hasIceCream() {
        return Build.VERSION.SDK_INT >= 14;
    }
}

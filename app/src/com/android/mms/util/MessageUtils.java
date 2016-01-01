
package com.android.mms.util;

import java.util.HashMap;

import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.format.Time;
import cn.com.nd.momo.activity.MyApplication;

import com.android.mms.data.WorkingMessage;
import com.android.mms.model.SlideModel;
import com.android.mms.model.SlideshowModel;
import com.android.mms.pdu.EncodedStringValue;
import com.android.mms.pdu.PduPersister;

public class MessageUtils {
    static String sLocalNumber;

    /**
     * MMS address parsing data structures
     */
    // allowable phone number separators
    private static final char[] NUMERIC_CHARS_SUGAR = {
            '-', '.', ',', '(', ')', ' ', '/', '\\', '*', '#', '+'
    };

    protected static final String TAG = "MessageUtils";

    private static HashMap<Character, Character> numericSugarMap = new HashMap<Character, Character>(
            NUMERIC_CHARS_SUGAR.length);

    static {
        for (int i = 0; i < NUMERIC_CHARS_SUGAR.length; i++) {
            numericSugarMap.put(NUMERIC_CHARS_SUGAR[i], NUMERIC_CHARS_SUGAR[i]);
        }
    }

    public static String getLocalNumber() {
        if (null == sLocalNumber) {
            sLocalNumber = MyApplication.getApplication().getTelephonyManager().getLine1Number();
        }

        return sLocalNumber;
    }

    public static boolean isLocalNumber(String number) {
        if (number == null) {
            return false;
        }

        // we don't use Mms.isEmailAddress() because it is too strict for
        // comparing addresses like
        // "foo+caf_=6505551212=tmomail.net@gmail.com", which is the 'from'
        // address from a forwarded email
        // message from Gmail. We don't want to treat
        // "foo+caf_=6505551212=tmomail.net@gmail.com" and
        // "6505551212" to be the same.
        if (number.indexOf('@') >= 0) {
            return false;
        }

        return PhoneNumberUtils.compare(number, getLocalNumber());

    }

    public static boolean isAlias(String string) {
        return false;
    }

    public static boolean isEmailAddress(String numberOrEmail) {
        return false;
    }

    public static boolean isAlphaNumeric(String s) {
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            if ((c >= 'a') && (c <= 'z')) {
                continue;
            }
            if ((c >= 'A') && (c <= 'Z')) {
                continue;
            }
            if ((c >= '0') && (c <= '9')) {
                continue;
            }
            return false;
        }
        return true;
    }

    public static String extractEncStrFromCursor(Cursor cursor,
            int columnRawBytes, int columnCharset) {
        // String rawBytes = cursor.getString(columnRawBytes);
        // int charset = cursor.getInt(columnCharset);
        //
        // if (TextUtils.isEmpty(rawBytes)) {
        // return "";
        // } else if (charset == CharacterSets.ANY_CHARSET) {
        // return rawBytes;
        // } else {
        // return new EncodedStringValue(charset,
        // PduPersister.getBytes(rawBytes)).getString();
        // }
        return null;
    }

    public static String formatTimeStampString(Context context, long when) {
        return formatTimeStampString(context, when, false);
    }

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT |
                DateUtils.FORMAT_ABBREV_ALL |
                DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make
        // showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }

    /**
     * Given a phone number, return the string without syntactic sugar, meaning
     * parens, spaces, slashes, dots, dashes, etc. If the input string contains
     * non-numeric non-punctuation characters, return null.
     */
    private static String parsePhoneNumberForMms(String address) {
        StringBuilder builder = new StringBuilder();
        int len = address.length();

        for (int i = 0; i < len; i++) {
            char c = address.charAt(i);

            // accept the first '+' in the address
            if (c == '+' && builder.length() == 0) {
                builder.append(c);
                continue;
            }

            if (Character.isDigit(c)) {
                builder.append(c);
                continue;
            }

            if (numericSugarMap.get(c) == null) {
                return null;
            }
        }
        return builder.toString();
    }

    /**
     * Returns true if the address passed in is a valid MMS address.
     */
    public static boolean isValidMmsAddress(String address) {
        String retVal = parseMmsAddress(address);
        return (retVal != null);
    }

    /**
     * parse the input address to be a valid MMS address. - if the address is an
     * email address, leave it as is. - if the address can be parsed into a
     * valid MMS phone number, return the parsed number. - if the address is a
     * compliant alias address, leave it as is.
     */
    public static String parseMmsAddress(String address) {
        // if it's a valid Email address, use that.
        if (isEmailAddress(address)) {
            return address;
        }

        // if we are able to parse the address to a MMS compliant phone number,
        // take that.
        String retVal = parsePhoneNumberForMms(address);
        if (retVal != null) {
            return retVal;
        }

        // if it's an alias compliant address, use that.
        if (isAlias(address)) {
            return address;
        }

        // it's not a valid MMS address, return null
        return null;
    }

    /**
     * 发送已读报告
     * 
     * @param context
     * @param threadId 会话id
     * @param status
     * @param callback
     */
    public static void handleReadReport(final Context context,
            final long threadId,
            final int status,
            final Runnable callback) {
        // String selection = Mms.MESSAGE_TYPE + " = " +
        // PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF
        // + " AND " + Mms.READ + " = 0"
        // + " AND " + Mms.READ_REPORT + " = " + PduHeaders.VALUE_YES;
        //
        // if (threadId != -1) {
        // selection = selection + " AND " + Mms.THREAD_ID + " = " + threadId;
        // }
        //
        // final Cursor c = context.getContentResolver().query(
        // Mms.Inbox.CONTENT_URI, new String[] {
        // Mms._ID, Mms.MESSAGE_ID
        // },
        // selection, null, null);
        //
        // if (c == null) {
        // return;
        // }
        //
        // final Map<String, String> map = new HashMap<String, String>();
        // try {
        // if (c.getCount() == 0) {
        // if (callback != null) {
        // callback.run();
        // }
        // return;
        // }
        //
        // while (c.moveToNext()) {
        // Uri uri = ContentUris.withAppendedId(Mms.CONTENT_URI, c.getLong(0));
        // map.put(c.getString(1), AddressUtils.getFrom(context, uri));
        // }
        // } finally {
        // c.close();
        // }
        //
        // OnClickListener positiveListener = new OnClickListener() {
        // public void onClick(DialogInterface dialog, int which) {
        // for (final Map.Entry<String, String> entry : map.entrySet()) {
        // Log.d(TAG, "send READrEC");
        // }
        // // MmsMessageSender.sendReadRec(context, entry.getValue(),
        // // entry.getKey(), status);
        // // }
        //
        // if (callback != null) {
        // callback.run();
        // }
        // }
        // };
        //
        // OnClickListener negativeListener = new OnClickListener() {
        // public void onClick(DialogInterface dialog, int which) {
        // if (callback != null) {
        // callback.run();
        // }
        // }
        // };
        //
        // OnCancelListener cancelListener = new OnCancelListener() {
        // public void onCancel(DialogInterface dialog) {
        // if (callback != null) {
        // callback.run();
        // }
        // }
        // };
        //
        // confirmReadReportDialog(context, positiveListener,
        // negativeListener,
        // cancelListener);
    }

    private static void confirmReadReportDialog(Context context,
            OnClickListener positiveListener, OnClickListener negativeListener,
            OnCancelListener cancelListener) {
        // AlertDialog.Builder builder = new AlertDialog.Builder(context);
        // builder.setCancelable(true);
        // builder.setTitle(R.string.confirm);
        // builder.setMessage(R.string.message_send_read_report);
        // builder.setPositiveButton(R.string.yes, positiveListener);
        // builder.setNegativeButton(R.string.no, negativeListener);
        // builder.setOnCancelListener(cancelListener);
        // builder.show();
    }

    /**
     * TODO: 完成此函数 转换编码格式
     * 
     * @param snippet 内容
     * @param charset 字符编码
     */
    public static String extractEncStr(String snippet, int charset) {
        if (TextUtils.isEmpty(snippet)) {
            return "";
        } else if (charset == CharacterSets.ANY_CHARSET) {
            return snippet;
        } else {
            return new EncodedStringValue(charset, PduPersister.getBytes(snippet)).getString();
        }
    }

    /**
     * The quality parameter which is used to compress JPEG images.
     */
    public static final int IMAGE_COMPRESSION_QUALITY = 80;

    /**
     * The minimum quality parameter which is used to compress JPEG images.
     */
    public static final int MINIMUM_IMAGE_COMPRESSION_QUALITY = 50;

    public static final int getAttachmentType(SlideshowModel model) {
        if (model == null) {
            return WorkingMessage.TEXT;
        }

        int numberOfSlides = model.size();

        if (numberOfSlides > 1) {
            return WorkingMessage.SLIDESHOW;
        } else if (numberOfSlides == 1) {
            // only one slide in the slide-show
            SlideModel slide = model.get(0);
            if (slide.hasVideo()) {
                return WorkingMessage.VIDEO;
            }

            if (slide.hasAudio() && slide.hasImage()) {
                return WorkingMessage.SLIDESHOW;
            }

            if (slide.hasAudio()) {
                return WorkingMessage.AUDIO;
            }

            if (slide.hasImage()) {
                return WorkingMessage.IMAGE;
            }

            if (slide.hasText()) {
                return WorkingMessage.TEXT;
            }
        }

        return WorkingMessage.TEXT;
    }

}


package cn.com.nd.momo.im.buss;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.WholeImageActivity;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.RequestUrl;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.http.HttpToolkit;
import cn.com.nd.momo.api.parsers.json.ChatContentParser;
import cn.com.nd.momo.api.parsers.json.ChatParser;
import cn.com.nd.momo.api.parsers.json.UserParser;
import cn.com.nd.momo.api.types.Chat;
import cn.com.nd.momo.api.types.ChatContent;
import cn.com.nd.momo.api.types.ChatContent.Audio;
import cn.com.nd.momo.api.types.ChatContent.Card;
import cn.com.nd.momo.api.types.ChatContent.File;
import cn.com.nd.momo.api.types.ChatContent.Location;
import cn.com.nd.momo.api.types.ChatContent.LongText;
import cn.com.nd.momo.api.types.ChatContent.Picture;
import cn.com.nd.momo.api.types.ChatContent.Text;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.util.Base64;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

import cn.com.nd.momo.im.types.ChatLocal;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.util.StartForResults;
import cn.com.nd.momo.util.StartForResults.PickData;


import com.flurry.android.FlurryAgent;

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

    public static long getJSONTime(JSONObject json, String prop) {
        long timesent = 0;
        if (json.has(prop)) {
            try {
                timesent = json.getLong(prop);
            } catch (JSONException e) {
                Log.e(TAG, "json timesent is not a long number");
                e.printStackTrace();
            }
        }
        if (timesent == 0)
            timesent = new Date().getTime();
        return timesent;
    }

    public static long getJSONTimeSent(JSONObject json, boolean system) {
        return getJSONTime(json,  "addtime");
    }

    public static long getJSONTimeSent(JSONObject json) {
        return getJSONTimeSent(json, false);
    }

    public static void putJSONTimeSent(JSONObject json, long timesent) {

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
     * 把地理信息转化为地图图片地址
     */
    public static String gmapUrl(Location info, int width, int height) {
        String location = info.getLatitude() + "," + info.getLongitude();
        String result = "http://maps.googleapis.com/maps/api/staticmap?center="
                + location + "&markers=color:blue|" + location
                + "&zoom=16&size=" + width + "x" + height + "&sensor=false";
        return result;
    }

    public static String gmapUrl(Location info, Context context) {
        Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        return gmapUrl(info, width * 2, height * 2);
    }

    public static String gmapUrl(Location info) {
        return gmapUrl(info, 300, 200);
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
     * 聊天对话列表显示状态的时候文字修改颜色
     * 
     * @param context
     * @param info
     * @return
     */
    public static SpannableStringBuilder chatContentToSpannable(
            Context context, ChatLocal info, final boolean hasDraft) {
        String draft = "";
        if (hasDraft) {
            draft = "[草稿] ";
        }
        String state = "";
        if (info.getState() == ChatLocal.STATE_FAILED) {
            state = "[失败] ";
        } else if (info.isOut() && info.getState() == ChatLocal.STATE_SENDING) {
            state = "[发送中] ";
        }
        SpannableStringBuilder sb = TextViewUtil.addEmojiSmileySpans(context, draft + state
                + chatContentToString(context, info), ImageSpan.ALIGN_BOTTOM);
        // SpannableStringBuilder sb = new SpannableStringBuilder(draft + state
        // + chatContentToString(context, info));
        if (hasDraft) {
            final ForegroundColorSpan fcs = new ForegroundColorSpan(
                    IMUtil.getStateColor(context, ChatLocal.STATE_DRAFT));
            sb.setSpan(fcs, 0, draft.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        if (IMUtil.isNotEmptyString(state)) {
            final ForegroundColorSpan fcs = new ForegroundColorSpan(
                    IMUtil.getStateColor(context, info.getState()));
            sb.setSpan(fcs, draft.length(), draft.length() + state.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        }
        return sb;
    }

    /**
     * 发送内容提醒各种类型转文字说明
     * 
     * @param context
     * @param info
     * @return
     */
    public static String chatContentToString(Context context, ChatLocal info) {
        ChatContent content = info.getContent();
        String text = "";
        String senderAlias = info.isOut() ? "你" : "给您";
        if (content instanceof Picture) {
            text = senderAlias + "发送了一张照片";
        } else if (content instanceof Location) {
            text = senderAlias + "发送了地理位置";
        } else if (content instanceof Audio) {
            text = senderAlias + "发送了一段语音";
        } else if (content instanceof File) {
            text = senderAlias + "发送了一个文件";
        } else if (content instanceof Card) {
            text = senderAlias + "发送了一个名片";
        } else if (content instanceof ChatContent.Contact) {
            text = senderAlias + "发送了联系人信息";
        } else if (content instanceof ChatContent.MobileModify) {
            text = "变更了联系方式";
        } else if (content instanceof LongText) {
            text = ((LongText)content).getText();
        } else if (content instanceof Text) {
            if (info.isSecretary()
                    && (info.getContent() == null || IMUtil
                            .isEmptyString(((Text)content).getText()))) {
                text = "Hi，我是小秘，如需帮助请留言。";
            } else {
                text = ((Text)content).getText();
            }
        } else {
            text = senderAlias + "发送了"
                    + context.getString(R.string.im_unsupported);
        }
        return noHTMLButSmiley(text);
    }


    public static String getStateLocale(int state) {
        switch (state) {
            case ChatLocal.STATE_DRAFT:
                return "";// "草稿";
            case ChatLocal.STATE_FAILED:
                return "失败";
            case ChatLocal.STATE_RECEIVED:
                return "已读";
            case ChatLocal.STATE_SENDING:
                return "发送中";
            case ChatLocal.STATE_SENT:
                return "";// "已发";
            case ChatLocal.STATE_INBOXED:
                return "";// "送达";
            case ChatLocal.STATE_SMS_SENDING:
            case ChatLocal.STATE_SMS_SENT:
                return "短信";
            case ChatLocal.STATE_SMS_FAIL:
                return "短信";
            case ChatLocal.STATE_UNREAD:
                // return "未读";
            case ChatLocal.STATE_READ:
            default:
                return "";
        }
    }

    public static String getStateLocaleEn(int state) {
        switch (state) {
            case ChatLocal.STATE_DRAFT:
                return "draft";// "草稿";
            case ChatLocal.STATE_FAILED:
                return "fail";
            case ChatLocal.STATE_RECEIVED:
                return "other read";
            case ChatLocal.STATE_SENDING:
                return "sending";
            case ChatLocal.STATE_SENT:
                return "sent";
            case ChatLocal.STATE_INBOXED:
                return "inboxed";
            case ChatLocal.STATE_SMS_SENDING:
                return "sms sending";
            case ChatLocal.STATE_SMS_SENT:
                return "sms sent";
            case ChatLocal.STATE_SMS_FAIL:
                return "sms fail";
            case ChatLocal.STATE_UNREAD:
                return "unread";
            case ChatLocal.STATE_READ:
            default:
                return "read";
        }
    }

    public static int getStateColor(Context context, int state) {
        switch (state) {
            case ChatLocal.STATE_INBOXED:
                return context.getResources().getColor(R.color.im_state_yellow);
            case ChatLocal.STATE_RECEIVED:
                return context.getResources().getColor(R.color.im_grey);
            case ChatLocal.STATE_FAILED:
            case ChatLocal.STATE_SMS_FAIL:
            case ChatLocal.STATE_DRAFT:
                return context.getResources().getColor(R.color.im_state_red);
            case ChatLocal.STATE_SENT:
            case ChatLocal.STATE_SMS_SENDING:
            case ChatLocal.STATE_SMS_SENT:
            default:
                return context.getResources().getColor(R.color.im_state_green);
            case ChatLocal.STATE_SENDING:
                return context.getResources().getColor(R.color.im_state_blue);
        }
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
     * 察看长文本内容
     * 
     * @param context
     * @param info
     */
    public static void showLongText(Context context, ChatLocal info) {
        String url = RequestUrl.URL_CONVERSATION_LONGTEXT
                + "&id=" + info.getId();
        Log.i(TAG, "on long text click:" + url);
        openMomoUrl(context, url);
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
     * 显示大图
     * 
     * @param context
     * @param picture
     */
    public static void showWholeImage(Context context, ChatContent.Picture picture) {
        Intent i = new Intent(context,
                WholeImageActivity.class);
        ArrayList<String> images = new ArrayList<String>();
        Log.i(TAG, "on image click " + picture.getUrl()
                + " user " + GlobalUserInfo.getUID());
        if (IMUtil.isLocalUrl(picture.getUrl())) {
            i.setAction(WholeImageActivity.ACION_VIEW_LOACL);
            images.add(picture.getUrl());
        } else {
            i.setAction(WholeImageActivity.ACION_VIEW_NET);
            images.add(IMUtil.pictureUrl(
                    picture.getUrl(), 780));
        }
        ;
        i.putExtra(WholeImageActivity.EXTRAL_IMAGES,
                images);
        i.putExtra(WholeImageActivity.EXTRAL_INDEX, 0);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * 获取真实位置信息
     * 
     * @param raw
     * @return
     */
    public static ChatContent.Location getFixedLocation(ChatContent.Location raw) {
        ChatContent.Location result = new ChatContent.Location();

        final String url = "http://search1.mapabc.com/sisserver?config=BSPS&resType=json&imei="
                + GlobalUserInfo.getDeviceIMEI()
                + "&gps=1&glong="
                + raw.getLongitude()
                + "&glat="
                + raw.getLatitude()
                + "&cdma=0&sid=14136&nid=0&bid=8402&lon=0&lat=0&macs=&a_k=c2b0f58a6f09cafd1503c06ef08ac7aeb7ddb91a3ce48789b37a6c2da3a69309fec4cfad868b6c21";
        HttpToolkit http = new HttpToolkit(url);
        int ret = http.DoGet(null, null);
        String response = http.GetResponse();

        boolean fix = false;
        try {
            if (ret == 200) {
                // Log.i(TAG, "getFixedLocation:" + response);
                JSONObject json = new JSONObject(response);
                JSONArray list = json.getJSONArray("list");
                if (list.length() > 0) {
                    JSONObject item = list.getJSONObject(0);
                    if (item.getInt("ceny") == 0 && item.getInt("cenx") == 0) {
                        // no need to fix (outside china)
                        return raw;
                    }
                    result.setLatitude(item.getDouble("ceny"));
                    result.setLongitude(item.getDouble("cenx"));
                    fix = true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (fix) {
            return result;
        } else {
            return null;
        }
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



    public static void showLocationSwitcher(final Context context, final ChatLocal info,
            final boolean isLocationFixedOK, final ChatContent.Location other) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("选择操作");
        builder.setItems(locationItems, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which) {
                CharSequence item = locationItems[which];
                if (LOCATION_AMAP.equals(item)) {
                    try {
                        Location l = (Location)info.getContent();
                        Intent intent = new Intent(
                                "com.autonavi.minimap.ACTION",
                                Uri.parse("navi:" + l.getLatitude() + ","
                                        + l.getLongitude()
                                        + "," + (l.isCorrect() ? 0 : 1) + ",2,"
                                        + info.getSender().getName()));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // context.startActivity(intent);
                        ((Activity)context).startActivityForResult(intent, 1011);
                    } catch (Exception error) {
                        Toast.makeText(context, "抱歉，您可能尚未安装高德地图", 5000).show();
                        try {
                            Intent iInstall = new Intent(
                                    Intent.ACTION_VIEW,
                                    Uri
                                            .parse("http://market.android.com/details?id=com.autonavi.minimap"));
                            context.startActivity(iInstall);
                        } catch (Exception noMarket) {
                        }
                    }
                } else if (LOCATION_GMAP.equals(item)) {
                    Location l;
                    if (isLocationFixedOK && other != null) {
                        l = other;
                    } else {
                        l = (Location)info.getContent();
                    }
                    try {
                        String uri = "http://maps.google.com/?daddr=" +
                                l.getLatitude() + "," + l.getLongitude();
                        Intent myIntent = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(uri));
                        myIntent.setClassName("com.google.android.apps.maps",
                                "com.google.android.maps.MapsActivity");
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(myIntent);
                    } catch (Exception error) {
                        Toast.makeText(context, "抱歉，您可能尚未安装谷歌地图", 5000).show();
                    }
                } else if (LOCATION_KCODE.equals(item)) {
                    Location l;
                    if (isLocationFixedOK && other != null) {
                        l = other;
                    } else {
                        l = (Location)info.getContent();
                    }
                    AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                    alertDialog.setTitle("K码是："
                            + IMUtil.gpsToKCode(l.getLatitude(), l.getLongitude()));
                    alertDialog.setButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                } else {
                    try {
                        Location l;
                        if (isLocationFixedOK && other != null) {
                            l = other;
                        } else {
                            l = (Location)info.getContent();
                        }
                        String location = l.getLatitude() + ","
                                + l.getLongitude();
                        Intent intent = new Intent(
                                android.content.Intent.ACTION_VIEW,
                                Uri.parse("geo:" + location + "?q="
                                        + location + "("
                                        + info.getSender().getName()
                                        + ")" + "&markers=color:blue|"
                                        + location));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(context, "抱歉，您可能尚未安装地图软件", 5000).show();
                        FlurryAgent.logEvent(FLURRY_IM_MAP_FAIL);
                    }
                }
            }
        }).show();
    }

    /**
     * 调用浏览器下载文件
     * 
     * @param mContext
     * @param file
     */
    public static void showFile(Context context, File file) {
        Log.i(TAG, "on file click:" + file.getUrl());
        Intent i = new Intent(Intent.ACTION_VIEW, Uri
                .parse(file.getUrl()));
        context.startActivity(i);
    }

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
     * @param chat
     * @return
     */
    public static boolean isSmsAble(ChatLocal chat) {
        return false;
        // return chat.isOut()
        // && chat.getReceiver().isSingle()
        // && (chat.getState() == ChatLocal.STATE_INBOXED
        // || chat.getState() == ChatLocal.STATE_SENT || chat
        // .getState() == ChatLocal.STATE_SMS_FAIL);
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

    /**
     * K码结束--------------------------------------------------
     */

    public static ChatContent.Location convertGoogleToBaidu(ChatContent.Location l) {
        ChatContent.Location result;
        String url = "http://api.map.baidu.com/ag/coord/convert?from=2&to=4&x=" + l.getLongitude()
                + "&y=" + l.getLatitude();
        HttpToolkit http = new HttpToolkit(url);
        int ret = http.DoGet(null, null);
        String response = http.GetResponse();

        Log.i(TAG, "convertGoogleToBaidu:" + response);
        try {
            if (ret == 200) {
                JSONObject json = new JSONObject(response);
                if (json.optInt("error", -1) == 0) {
                    String baseX = json.optString("x");
                    String baseY = json.optString("y");
                    result = new ChatContent.Location();
                    try {
                        double lat = Double.parseDouble(new String(Base64.decode(baseY)));
                        double lng = Double.parseDouble(new String(Base64.decode(baseX)));
                        Log.i(TAG, "LAT GOT: " + lat + " LNG " + lng);
                        result.setLatitude(lat);
                        result.setLongitude(lng);
                        return result;
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }
}

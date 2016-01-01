
package cn.com.nd.momo.dynamic;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import cn.com.nd.momo.activity.Statuses_Comment_Activity;
import cn.com.nd.momo.api.parsers.json.UserParser;
import cn.com.nd.momo.api.types.User;

public class DynamicItemInfo extends DynamicInfo {
    public final static int SEND_DONE = 0;

    public final static int SEND_PROCESS = 1;

    public final static int SEND_FAIL = 2;

    public Boolean isdownloading = false;

    public Boolean isdownloadingImages = false;

    public Boolean isQuickBtnsExpanded = false;

    public int sendStatus = SEND_DONE;

    public Bitmap avatarBmp = null;

    public ArrayList<Bitmap> imageBmps = new ArrayList<Bitmap>();

    public long draftID = -1;

    public String date;
    
    /**
     * 是否已经删除 
     */
    public boolean isDeleted = false;

    public DynamicItemInfo() {
    }

    public DynamicItemInfo(DynamicInfo info) {
        super(info);
        date = getTime(createAt * 1000);
    }

    public DynamicItemInfo(String info) throws JSONException {
        super(info);
        date = getTime(createAt * 1000);
    }

    public DynamicItemInfo(JSONObject info) throws JSONException {
        super(info);
        date = getTime(createAt * 1000);
    }

    public static void viewContactFragmentActivity(long uid, String name, Context context,
            String action) {
    }

    public static void viewContactFragmentActivity(long uid, String name, Context context) {
    }

    public void viewLongText(Context context) {
        // ToDo Zgx 20120731
        // // String url = GlobalUserInfo.API + "/statuses/long_text.json";
        // String url = GlobalUserInfo.API + "/transfer/apiserver.php";
        // //?class=statuses&method=show_text&source={source}&id={id}
        // url = GlobalUserInfo.webReqestLongText(id) ;
        // Intent i = new Intent(context, WebViewActivity.class);
        // i.putExtra(WebViewActivity.EXTARS_WEBVIEW_URL, url);
        // context.startActivity(i);
    }
}


package cn.com.nd.momo.manager;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.util.EncodingUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

/**
 * 自动检测assets/auth.json是否存在，试图用auth.json中的内容自动登陆
 * 
 * @date Dec 1, 2011
 * @author Tsung Wu <tsung.bz@gmail.com>
 */

public class AssetAutoLogin {
    private static final String TAG = "AssetAutoLogin";

    public AssetAutoLogin(Context context, OnAssetAutoLoginListener l) {
        String jsonString = "";
        try {
            InputStream in = context.getResources().getAssets()
                    .open("auth.json");
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            jsonString = EncodingUtils.getString(buffer, "UTF-8");

            try {
                JSONObject json = new JSONObject(jsonString);
                if (json.has("autoLogin")) {
                    l.onAssetAutoLoginGot(json.getString("autoLogin"));
                } else {
                    l.onAssetAutoLoginFail();
                }
            } catch (JSONException e) {
                l.onAssetAutoLoginFail();
            }
        } catch (IOException e) {
            l.onAssetAutoLoginFail();
        }
    }

    public interface OnAssetAutoLoginListener {
        void onAssetAutoLoginGot(String url);

        void onAssetAutoLoginFail();
    }
}

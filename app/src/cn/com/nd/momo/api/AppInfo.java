
package cn.com.nd.momo.api;

import android.content.Context;
import cn.com.nd.momo.api.oauth.OAuthToken;
import cn.com.nd.momo.api.types.OAuthInfo;

/**
 * 应用信息封装
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public final class AppInfo {
    private static OAuthInfo m_OAuthInfo = null;

    private static Context m_Context = null;

    public static Context getContext() {
        return m_Context;
    }

    public static void setContext(Context context) {
        m_Context = context;
    }

    public static OAuthInfo getOAuthInfo() {
        return m_OAuthInfo;
    }

    public static void setOAuthInfo(OAuthInfo oAuthInfo) {
        m_OAuthInfo = oAuthInfo;
    }
}

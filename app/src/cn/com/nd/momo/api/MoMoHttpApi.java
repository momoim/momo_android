
package cn.com.nd.momo.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.http.HttpTool;
import cn.com.nd.momo.api.oauth.OAuthHelper;
import cn.com.nd.momo.api.parsers.json.ContactParser;
import cn.com.nd.momo.api.types.Attachment;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.types.UserList;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

/**
 * MoMo Http API 封装类
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public final class MoMoHttpApi {

    public static int APP_ID = 1;


    /**
     * 设置认证资料
     * 
     * @param oauthToken
     */
    public static final void setOAuthInfo(OAuthInfo oAuthInfo) {
        AppInfo.setOAuthInfo(oAuthInfo);
    }



    /**
     * 根据手机号 + 密码登陆
     * 
     * @param zoneCode
     * @param phoneNum
     * @param pwd
     * @return OAuthInfo
     */
    public static OAuthInfo login(String zoneCode, String phoneNum, String pwd)
            throws MoMoException {
        return OAuthHelper.login(zoneCode, phoneNum, pwd);
    }


    /**
     * 发送注册信息
     * 
     * @param mobile
     * @return
     * @throws MoMoException
     */
    public static void register(String zoneCode, String mobile) throws MoMoException {
        OAuthHelper.register(zoneCode, mobile);
    }

    /**
     * 完成注册校验
     * 
     * @param zoneCode
     * @param mobile
     * @param verifyCode
     * @return OAuthInfo
     */
    public static OAuthInfo registerVerify(String zoneCode, String mobile, String verifyCode)
            throws MoMoException {
        return OAuthHelper.registerVerify(zoneCode, mobile, verifyCode);
    }

    public static OAuthInfo refreshAccessToken(String refreshToken)
            throws MoMoException {
        return OAuthHelper.refreshAccessToken(refreshToken);
    }

    /**
     * 完善个人信息
     * 
     * @param realName
     * @param pwd
     * @return 用户状态
     * @throws MoMoException
     */
    public static int updateUserInfo(String realName, String pwd) throws MoMoException {
        int result = 0;

        HttpTool http = new HttpTool(RequestUrl.USER_UPDATE);
        JSONObject param = new JSONObject();
        try {
            param.put("username", realName);
            param.put("password", pwd);
            int statusCode = http.DoPost(param);
            result = statusCode;
        } catch (JSONException e) {
            throw new MoMoException(e);
        }
        return result;
    }

    /**
     * 重置密码
     * 
     * @param oldPwd 旧密码
     * @param newPwd 新密码
     * @return
     * @throws MoMoException
     */
    public static void resetPwd(String oldPwd, String newPwd) throws MoMoException {
        OAuthHelper.resetPwd(oldPwd, newPwd);
    }

    /**
     * 重置密码（不需提供原始密码，需登录后才能做）
     * 
     * @param newPwd 新密码
     * @return OAuthInfo
     * @throws MoMoException
     */
    public static OAuthInfo resetPwd(String zoneCode, String mobile, String newPwd)
            throws MoMoException {
        return OAuthHelper.resetPwd(zoneCode, mobile, newPwd);
    }

    /**
     * 通过短信重发密码
     * 
     * @param zoneCode
     * @param mobile
     * @return
     * @throws MoMoException
     */
    public static void getPwdBySms(String zoneCode, String mobile) throws MoMoException {
        OAuthHelper.getPwdBySms(zoneCode, mobile);
    }

    /**
     * 获取可用短信条数
     * 
     * @return 可用短信条数
     * @throws MoMoException
     */
    public static int getSmsCount() throws MoMoException {
        return OAuthHelper.getSmsCount();
    }

    /**
     * 根据手机号码批量获取用户ID
     * 
     * @param userMap
     * @return
     * @throws MoMoException
     */
    public static List<User> getIDList(Map<String, User> userMap) throws MoMoException {
        return OAuthHelper.getIDList(userMap);
    }

    /**
     * 绑定微博
     * 
     * @param site
     * @param accountName
     * @param accountPassword
     * @param followMoMo
     * @return
     * @throws MoMoException
     */
    public static void bindWeibo(String site, String accountName, String accountPassword,
            boolean followMoMo) throws MoMoException {
        OAuthHelper.bindWeibo(site, accountName, accountPassword, followMoMo);
    }


    /**
     * 更新头像
     * 
     * @param bmpAvatar 大头像
     * @param bmpAvatarBig 原图
     * @return 图片url地址
     * @throws MoMoException
     */
    public static String uploadMyAvatar(Bitmap bmpAvatar, Bitmap bmpAvatarBig) throws MoMoException {
        String result = null;
        ByteArrayOutputStream streamImg = new ByteArrayOutputStream();
        ByteArrayOutputStream streamImgBig = new ByteArrayOutputStream();
        if (!bmpAvatar.compress(Bitmap.CompressFormat.JPEG, 100, streamImg)) {
            Log.e("UploadBitmap: get small bmp compress data failed");
            throw new MoMoException("图像压缩错误");
        }
        if (!bmpAvatarBig.compress(Bitmap.CompressFormat.JPEG, 80, streamImgBig)) {
            Log.e("UploadBitmap: get big bmp compress data failed");
            throw new MoMoException("图像压缩错误");
        }

        String strData = new String(Base64.encodeBase64(streamImg.toByteArray()));
        String strDataBig = new String(Base64.encodeBase64(streamImgBig.toByteArray()));

        try {
            streamImg.close();
            streamImgBig.close();
        } catch (IOException e) {
            Log.e(e.getMessage());
        }

        HttpTool http = new HttpTool(RequestUrl.IMAGE_URL);
        JSONObject param = new JSONObject();

        try {
            param.put("middle_content", strData);
            param.put("original_content", strDataBig);

            http.DoPost(param);
            JSONObject jsonResponse = new JSONObject(http.GetResponse());
            result = jsonResponse.optString("src");
        } catch (JSONException e) {
            Log.e("uploadBitmap: " + e.getMessage());
            throw new MoMoException(e);
        }

        return result;
    }

    /**
     * 上传图片文件
     * 
     * @param bytes
     * @return 图片文件url
     * @throws MoMoException
     */
    public static Attachment upLoadPhoto(byte[] bytes) throws MoMoException {
        return PhotoUpLoad.upLoadPhotoByte(bytes);
    }
    
    public static Attachment upLoadPhoto(String file) throws MoMoException {
        return PhotoUpLoad.uploadPhotoFile(file);
    }

    /**
     * 上传音频文件
     * 
     * @param bytes
     * @return 音频文件url
     * @throws MoMoException
     */
    public static String upLoadAutioFile(byte[] bytes) throws MoMoException {
        // 音频文件类型为1，不用传文件名
        return upLoadFile(bytes, 1, null);
    }

    /**
     * 上传文件
     * 
     * @param bytes
     * @param fileType
     * @param fileName
     * @return 文件url
     * @throws MoMoException
     */
    private static String upLoadFile(byte[] bytes, int fileType, String fileName)
            throws MoMoException {
        return FileUpLoad.upLoadFileByte(bytes, fileType, fileName);
    }

    /**
     * 下载文件
     * 
     * @param url
     * @return 文件内容byte[]
     * @throws MoMoException
     */
    public static byte[] DownLoadBytes(String url) throws MoMoException {
        String headers = OAuthHelper.getAuthHeader(url, "GET");
        return HttpTool.DownLoadBytes(url, headers);
    }

    /**
     * 根据用户id获取用户名片
     * 
     * @param uid
     * @return
     * @throws MoMoException
     */
    public static Contact getUserCardByID(long uid) throws MoMoException {
        Contact result = null;
        HttpTool http = new HttpTool(new StringBuilder(RequestUrl.RETRIEVE_USER_CARD_URL)
                .append(uid).append(".json").toString());

        try {
            http.DoGet();
            JSONObject jsonResponse = new JSONObject(http.GetResponse());
            result = new ContactParser().parse(jsonResponse);

        } catch (Exception ex) {
            throw new MoMoException(ex);
        }

        return result;
    }

    /**
     * 根据用户名、手机号码获取名片
     * 
     * @param name
     * @param mobile
     * @return
     * @throws MoMoException
     */
    public static Contact getUserCardByMobile(String name, String mobile) throws MoMoException {
        Contact result = null;
        HttpTool http = new HttpTool(RequestUrl.RETRIEVE_USER_CARD_BY_MOBILE_URL);
        JSONObject param = new JSONObject();

        try {
            param.put("name", name);
            param.put("mobile", mobile);

            http.DoPost(param);
            JSONObject jsonResponse = new JSONObject(http.GetResponse());
            result = new ContactParser().parse(jsonResponse);

        } catch (Exception ex) {
            throw new MoMoException(ex);
        }

        return result;
    }

    /**
     * 根据手机号码批量获取名片
     * 
     * @param mobiles
     * @return 名片列表
     * @throws MoMoException
     */
    public static List<Contact> getUserCardListByMobile(List<String> mobileList)
            throws MoMoException {
        List<Contact> result = new ArrayList<Contact>();
        JSONArray paramArray = new JSONArray();

        try {
            JSONArray jsonArray = new JSONArray();
            for (String mobile : mobileList) {
                jsonArray.put(mobile);
                // 每次批量请求上限100笔
                if (jsonArray.length() == 100) {
                    paramArray.put(jsonArray);
                    jsonArray = new JSONArray();
                }

            }
            if (jsonArray.length() > 0) {
                paramArray.put(jsonArray);
            }

            for (int i = 0; i < paramArray.length(); i++) {
                HttpTool http = new HttpTool(RequestUrl.BATCH_GET_CARD_LIST);
                http.DoPostArray(paramArray.optJSONArray(i));
                String responseContent = http.GetResponse();
                JSONObject jsonResponse = new JSONObject(responseContent);
                Iterator<?> keyIter = jsonResponse.keys();
                while (keyIter.hasNext()) {
                    String mobile = keyIter.next().toString();
                    JSONObject jsObj = jsonResponse.optJSONObject(mobile);
                    Contact contact = null;
                    contact = new ContactParser().parse(jsObj);
                    contact.setMainPhone(mobile);
                    result.add(contact);
                }

            }

        } catch (Exception ex) {
            throw new MoMoException(ex);
        }

        return result;
    }

    /**
     * 修改名片
     * 
     * @param contact
     * @return
     * @throws MoMoException
     */
    public static void updateUserCard(Contact contact) throws MoMoException {
        HttpTool http = new HttpTool(RequestUrl.UPDATE_USER_CARD_URL);

        JSONObject param = new JSONObject();
        try {
            param = new ContactParser().toJSONObject(contact);

            http.DoPost(param);
        } catch (Exception ex) {
            throw new MoMoException(ex);
        }
    }


    /**
     * 获取长文本内容
     * @param id
     * @return
     * @throws MoMoException
     */
    public static String getFeedLongText(String id) throws MoMoException {
        HttpTool http = new HttpTool(RequestUrl.STATUSES_LONG_TEXT + "?statuses_id=" + id);
        int code = http.DoGet();
        if(code == 200) {
        	try {
        		return new JSONObject(http.GetResponse()).optString("text");
        	} catch (JSONException e) {
        		throw new MoMoException(e);
        	}
        } else {
        	throw new MoMoException(http.GetResponse());
        }
    }
}

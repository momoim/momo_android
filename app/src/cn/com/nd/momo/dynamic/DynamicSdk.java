
package cn.com.nd.momo.dynamic;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.nd.momo.api.RequestUrl;

public class DynamicSdk extends AbsSdk {
    public static final int MSG_GET_SHOP = 100;

    public static final int MSG_POST_SHOP = 500;

    public DynamicSdk() {
    }

    // public static final String FUNCTION_FEED = "/feed";
    // public static final String FUNCTION_FEED_ABOUTME = "/feed/aboutme";
    // public static final String FUNCTION_MESSAGE_NEW= "/message/newmsg";
    // public static final String FUNCTION_FRIEND = "/comment";

    // public SdkResult getDynamic(String filter, int pagesize, String
    // timeDirect, long lasttime) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
    // if (filter.length() == 0) {
    // hw.setFunction(GlobalUserInfo.FEED_URL + filter);
    // }
    // // // 活动动态
    // // else if (filter.contains("action_id")) {
    // // hw.setFunction(GlobalUserInfo.ACTION_URL + filter);
    // // }
    // // 群动态
    // else if (filter.contains("group_id")) {
    // hw.setFunction(GlobalUserInfo.GROUP_URL + filter);
    // }
    // // // 用户动态
    // // else if (filter.contains("user_id")) {
    // // hw.setFunction(GlobalUserInfo.USER_URL + filter);
    // // }
    // // // 按类型获取动态
    // // else if (filter.contains("type_id")) {
    // // hw.setFunction(GlobalUserInfo.FEED_URL_TYPE + filter);
    // // }
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // // urlParams.put("sid", sid);
    // urlParams.put("pagesize", String.valueOf(pagesize));
    //
    // if (lasttime != -1) {
    // urlParams.put(timeDirect, String.valueOf(lasttime));
    // }
    //
    // hw.setUrlParamMap(urlParams);
    // return hw.doHttpMethod();
    // }
    //
    // public SdkResult getDynamicAboutMe(String sid, int newFlag, int pagesize)
    // {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
    // hw.setFunction(GlobalUserInfo.FEED_URL + "/aboutme");
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // // urlParams.put("sid", sid);
    // urlParams.put("new", String.valueOf(newFlag));
    // urlParams.put("pagesize", String.valueOf(pagesize));
    // hw.setUrlParamMap(urlParams);
    // return hw.doHttpMethod();
    // }
    //
    // public SdkResult getDynamicMessageNew(String sid) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
    // hw.setFunction(GlobalUserInfo.MESSAGE_NEW_URL);
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // // urlParams.put("sid", sid);
    // hw.setUrlParamMap(urlParams);
    // return hw.doHttpMethod();
    // }

    // public SdkResult getDynamicComment(int pagesize, String loadFlag, String
    // startID, String id) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
    // hw.setFunction(GlobalUserInfo.COMMENT_URL);
    // Map<String, String> urlParams = new HashMap<String, String>();
    // urlParams.put("pagesize", String.valueOf(pagesize));
    //
    // startID = (startID == null ? "0" : startID);
    // urlParams.put(loadFlag, String.valueOf(startID));
    // urlParams.put("statuses_id", id);
    //
    // hw.setUrlParamMap(urlParams);
    // return hw.doHttpMethod();
    // }

    /**
     * <br>
     * Description:获取单个动态的详细信息 <br>
     * Author:hexy <br>
     * Date:2011-5-6上午11:09:39
     * 
     * @param sid
     * @param typeid
     * @param objid
     * @return
     */
    // public SdkResult getDynamicContent(String sid, long typeid, String
    // objid){
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
    // hw.setFunction(GlobalUserInfo.API+"/im");
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // // urlParams.put("sid", sid);
    // urlParams.put("typeid", String.valueOf(typeid));
    // urlParams.put("objid", objid);
    // urlParams.put("new_format", "1");
    // hw.setUrlParamMap(urlParams);
    // return hw.doHttpMethod();
    // }

    /**
     * <br>
     * Description:获取单个动态详细信息, 先从数据库后从服务端获取 <br>
     * Author:hexy <br>
     * Date:2011-5-11下午02:00:40
     * 
     * @param sid
     * @return
     * @throws Exception
     * @throws JSONException
     */
    public SdkResult getDynamicContentOpt(String sid, String id) throws JSONException {
        SdkResult result = new SdkResult();
        result.ret = HttpStatus.SC_OK;

        DynamicInfo info = DynamicDB.instance().queryDynamic(id);

        boolean isCached = info != null && info.id != null && info.id.length() != 0;
        if (!isCached) {
            HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
            hw.setFunction(RequestUrl.URL_API + "/statuses/im/:id.json".replace(":id", id));

            Map<String, String> urlParams = new HashMap<String, String>();
            // urlParams.put("sid", sid);
            urlParams.put("id", id);
            hw.setUrlParamMap(urlParams);
            result = hw.doHttpMethod();
            if (result.ret == HttpStatus.SC_OK) {
                DynamicItemInfo itemInfo = new DynamicItemInfo(new JSONObject(result.response));
                result.object = itemInfo;
//                DynamicDB.instance().insertDynamic(itemInfo, true);
            }
        }
        else {
            result.object = new DynamicItemInfo(info);
        }

        return result;
    }

    /**
     * <br>
     * Description:删除动态 <br>
     * Author:hexy <br>
     * Date:2011-5-6上午10:51:03
     * 
     * @param sid
     * @param typeid
     * @param objid
     * @return
     */
    // public SdkResult postDynamicDelete(String sid, String id) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_GET);
    // hw.setFunction(GlobalUserInfo.STATUSES_DESTORY_URL.replace(":id", id));
    // return hw.doHttpMethod();
    // }

    /**
     * <br>
     * Description:发送广播 <br>
     * Author:hexy <br>
     * Date:2011-5-9下午02:25:58
     * 
     * @param sid
     * @param typeid
     * @param objid
     * @return
     */
    // public SdkResult postDynamic(String sid,String content,ArrayList<String>
    // images , long gid, long retweetid){
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_POST);
    // hw.setFunction(GlobalUserInfo.RECORD_URL+"?sid=" + sid);
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    //
    // // save images
    // JSONArray jsonImages = new JSONArray();
    // for (String image:images)
    // jsonImages.put(image);
    //
    // // put images
    // if (jsonImages.length()>0)
    // urlParams.put("images", jsonImages.toString());
    //
    // // put group
    // if (gid!=-1) {
    // urlParams.put("group", String.valueOf(gid));
    // }
    //
    // // put retweet id
    // if (retweetid != -1){
    // urlParams.put("retweet_id", String.valueOf(retweetid));
    // JSONArray retweetImages = new JSONArray();
    // // put retweet images
    // for (String image:DynamicActivity.mDynamicItemInfo.images)
    // retweetImages.put(image);
    // if (retweetImages.length()>0)
    // urlParams.put("images", jsonImages.toString());
    // }
    //
    // return hw.doHttpMethod();
    // }
    /**
     * <br>
     * Description:对动态点赞 <br>
     * Author:hexy <br>
     * Date:2011-5-6上午10:50:32
     * 
     * @param sid
     * @param uid
     * @param typeid
     * @param objid
     * @return
     */
    // public SdkResult postDynamicPrise(long uid, String id) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_POST);
    // hw.setFunction(GlobalUserInfo.PRAISE_URL);
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // urlParams.put("statuses_id", String.valueOf(id));
    // urlParams.put("uid", String.valueOf(uid));
    // hw.setJsonParamMap(urlParams);
    // return hw.doHttpMethod();
    // }

    public SdkResult postDynamicSms(String id) {
        // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_POST);
        // hw.setFunction(GlobalUserInfo.SMS_URL);
        //
        // Map<String, String> urlParams = new HashMap<String, String>();
        // urlParams.put("statuses_id", String.valueOf(id));
        // hw.setJsonParamMap(urlParams);
        // return hw.doHttpMethod();
        return null;
    }

    // public SdkResult postDynamicHide(String id, int hide) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_POST);
    // hw.setFunction(GlobalUserInfo.HIDE_URL);
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // urlParams.put("id", String.valueOf(id));
    // urlParams.put("act", hide == 0 ? "add" : "del");
    // hw.setJsonParamMap(urlParams);
    // return hw.doHttpMethod();
    // }

    /**
     * <br>
     * Description:收藏或是取消收藏 <br>
     * Author:hexy <br>
     * Date:2011-5-6上午10:49:27
     * 
     * @param sid
     * @param faved 当前收藏状态 0=未收藏,1已收藏
     * @param typeid
     * @param objid
     * @return
     */
    // public SdkResult postDynamicFav(String sid, int faved, String id) {
    // HttpWrap hw = new HttpWrap(HttpWrap.HTTP_POST);
    // hw.setFunction(GlobalUserInfo.FAV_URL);
    //
    // Map<String, String> urlParams = new HashMap<String, String>();
    // urlParams.put("id", id);
    // urlParams.put("act", faved == 0 ? "add" : "del");
    // hw.setJsonParamMap(urlParams);
    // return hw.doHttpMethod();
    // }
}

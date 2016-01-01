
package cn.com.nd.momo.mention;

import java.util.ArrayList;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.dynamic.AbsSdk.SdkResult;
import cn.com.nd.momo.dynamic.DynamicInfo;
import cn.com.nd.momo.dynamic.DynamicItemInfo;
import cn.com.nd.momo.dynamic.DynamicSdk;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.mention.model.MentionInfo;

public class Util {
    private static final String TAG = "Util";

    /**
     * 解析MQ 推送下来的JSON数据
     * 
     * @param content 推先来的原文JSON字符串
     * @return 解析后的关于我的信息
     * @throws JSONException
     */
    public static MentionInfo decondePushMention(final String content) throws JSONException {
        Log.i(TAG, "push mention json: " + content);
        JSONObject jsonObj = new JSONObject(content);
        String kind = jsonObj.getString("kind");
        if (!kind.equals("aboutme")) {
            return null;
        }
        JSONObject jsonData = jsonObj.getJSONObject("data");
        return decodeMention(jsonData);
    }

    /**
     * 解析JSON字符串，数据区
     * 
     * @param content
     * @return
     */
    public static MentionInfo decodeMention(final String content)
            throws JSONException {
        JSONObject jsonObj = new JSONObject(content);
        return decodeMention(jsonObj);
    }

    /**
     * 解析JSON字符串，数据区
     * 
     * @param jsonObj
     * @return
     */
    public static MentionInfo decodeMention(final JSONObject jsonObj)
            throws JSONException {
        return decodeMention(jsonObj, false);
    }

    public static MentionInfo decodeMention(final JSONObject jsonObj, boolean isMoMe)
            throws JSONException {
        MentionInfo info = new MentionInfo();

        int kind = jsonObj.getInt("kind");

        info.setId(jsonObj.getString("id"));
        info.setFeedId(jsonObj.getString("statuses_id"));

        JSONObject gjson = jsonObj.optJSONObject("group");
        if (gjson != null && gjson.has("name")) {
            info.setGroupName(gjson.optString("name"));
            info.setGroupId(gjson.optInt("id"));
        }
        
        // 本条"关于我的"的发布者的信息
        JSONObject userObj = jsonObj.getJSONObject("user");
        info.setCommentUserId(userObj.getLong("id"));
        info.setCommentUserName(userObj.getString("name"));
        info.setCommentUserAvatar(userObj.getString("avatar"));

        info.setDateline(jsonObj.getLong("created_at"));
        info.setKind(kind);
        info.setSource(jsonObj.getString("source"));
        info.setRead(jsonObj.optString("new").equals("false"));
        JSONObject optObj = jsonObj.optJSONObject("opt");
        switch (info.getKind()) {
            case MentionInfo.KIND_COMMENT:
            case MentionInfo.KIND_COMMENT_MENTION:
                JSONObject comment = optObj.getJSONObject("comment");
                // 评论id
                info.setCommentId(comment.getString("id"));
                // 评论内容
                info.setComment(DynamicInfo.decodeAT(comment, comment
                        .getString("text")));
                info.setSrcComment(getFeedContent(info.getFeedId())); // 原动态

                // 我Mo的内容
                if (isMoMe) {
                    info.setComment("我在评论中提到" + info.getCommentUserName() + ":"
                            + DynamicInfo.decodeAT(comment, comment
                                    .getString("text")));
                }
                break;
            case MentionInfo.KIND_MESSAGE:
                JSONObject message = optObj.getJSONObject("message");
                // 留言内容
                info.setSrcComment(DynamicInfo.decodeAT(message, message
                        .getString("text")));

                info.setComment("给你留言了");

                // 我Mo的内容
                if (isMoMe) {
                    info.setComment("我给" + info.getCommentUserName() + "留言了");
                }
                break;
            case MentionInfo.KIND_LIKE:
                info.setComment("觉得很赞");
                info.setSrcComment(getFeedContent(info.getFeedId()));
                break;
            case MentionInfo.KIND_FEED_MENTION:
                JSONObject statuses = optObj.getJSONObject("statuses");
                // 动态内容
                info.setSrcComment(DynamicInfo.decodeAT(statuses, statuses
                        .getString("text")));
                info.setComment("在分享中提到你");

                // 我Mo的内容
                if (isMoMe) {
                    info.setComment("我在分享中提到" + info.getCommentUserName());
                }

                break;
            case MentionInfo.KIND_REPLY:
                JSONObject replySource = optObj.getJSONObject("reply_source");
                // 源评论id
                info.setSrcCommentId(replySource.getString("id"));
                // 源评论
                info.setSrcComment("我:" + DynamicInfo.decodeAT(replySource,
                        replySource.getString("text")));
                JSONObject replycomment = optObj.getJSONObject("comment");
                // 评论id
                info.setCommentId(replycomment.getString("id"));
                // 评论内容
                info.setComment(DynamicInfo.decodeAT(replycomment, replycomment
                        .getString("text")));
                break;
            default:
                Log.e(TAG, "error kind of mention");

        }
        return info;
    }

    public static ArrayList<MentionInfo> decodeMentionList(final String content)
            throws JSONException {
        return decodeMentionList(content, false);
    }

    public static ArrayList<MentionInfo> decodeMentionList(final String content, boolean isMoMe)
            throws JSONException {
        Log.i(TAG, "decode mention list json: " + content);
        ArrayList<MentionInfo> lstMentionInfo = new ArrayList<MentionInfo>();
        JSONArray jsonArray = new JSONArray(content);
        for (int i = 0, len = jsonArray.length(); i < len; i++) {
            MentionInfo mentionInfo = decodeMention(jsonArray.getJSONObject(i), isMoMe);
            lstMentionInfo.add(mentionInfo);
        }
        return lstMentionInfo;
    }

    public static String getFeedContent(String feedId) {
        String content = "";
        try {
            DynamicSdk dynamicSdk = new DynamicSdk();
            SdkResult result = dynamicSdk.getDynamicContentOpt(GlobalUserInfo
                    .getSessionID(), feedId);
            if (result.ret == HttpStatus.SC_OK) {
                DynamicItemInfo item = (DynamicItemInfo)result.object;
                content = item.text;
            }
        } catch (JSONException e) {
            Log.e(TAG, "获取动态时网络json数据解析错误");
        } finally {
        }
        if (TextUtils.isEmpty(content)) {
            content = "获取分享失败，请检查网络，或者分享已被删除";
        }
        return content;
    }

}

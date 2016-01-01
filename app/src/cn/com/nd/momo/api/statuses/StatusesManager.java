
package cn.com.nd.momo.api.statuses;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.nd.momo.activity.Statuses_Comment_Activity.CommentItemInfo;
import cn.com.nd.momo.api.RequestUrl;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.http.HttpTool;
import cn.com.nd.momo.api.parsers.json.GroupInfoParser;
import cn.com.nd.momo.api.parsers.json.GroupMemberParser;
import cn.com.nd.momo.api.parsers.json.GroupParser;
import cn.com.nd.momo.api.types.Attachment;
import cn.com.nd.momo.api.types.Group;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.types.GroupMember;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.dynamic.DynamicInfo;
import cn.com.nd.momo.dynamic.DynamicItemInfo;
import cn.com.nd.momo.dynamic.DynamicPoster.DynamicPostInfo;

public class StatusesManager {

    /**
     * 赞美
     * 
     * @param ID
     * @throws MoMoException
     */
    public static void praise(String ID) throws MoMoException {
        JSONObject param = new JSONObject();
        try {
            param.put("statuses_id", ID);
            HttpTool.post(RequestUrl.STATUSES_PRAISE, param);
        } catch (JSONException e) {
            throw new MoMoException(e);
        }
    }

    /**
     * 收藏
     * 
     * @param ID
     * @param isFaved
     * @throws MoMoException
     */
    public static void fav(String ID, int isFaved) throws MoMoException {
        JSONObject param = new JSONObject();
        try {
            param.put("id", ID);
            param.put("act", isFaved == 0 ? "add" : "del");
            HttpTool.post(RequestUrl.STATUSES_STORE, param);
        } catch (JSONException e) {
            throw new MoMoException(e);
        }
    }

    /**
     * 取消收藏
     * 
     * @param ID
     * @param isFaved
     * @throws MoMoException
     */
    public static void unFav(String ID) throws MoMoException {
        JSONObject param = new JSONObject();
        try {
            param.put("id", ID);
            param.put("act", "del");
            HttpTool.post(RequestUrl.STATUSES_STORE, param);
        } catch (JSONException e) {
            throw new MoMoException(e);
        }
    }

    /**
     * 隐藏
     * 
     * @param ID
     * @param isFaved
     * @throws MoMoException
     */
    public static void hide(String ID, int isHided) throws MoMoException {
        JSONObject param = new JSONObject();
        try {
            param.put("id", ID);
            // param.put("act", "add");
            param.put("act", isHided == 0 ? "add" : "del");
            HttpTool.post(RequestUrl.STATUSES_HIDE, param);
        } catch (JSONException e) {
            throw new MoMoException(e);
        }
    }

    /**
     * 取消隐藏
     * 
     * @param ID
     * @param isFaved
     * @throws MoMoException
     */
    public static void unHide(String ID) throws MoMoException {
        JSONObject param = new JSONObject();
        try {
            param.put("id", ID);
            param.put("act", "del");
            HttpTool.post(RequestUrl.STATUSES_HIDE, param);
        } catch (JSONException e) {
            throw new MoMoException(e);
        }
    }

    /**
     * 删除分享
     * 
     * @param ID
     * @throws MoMoException
     */
    public static void delStatuses(String ID) throws MoMoException {
        HttpTool.get(RequestUrl.STATUSES_DEl + ID + ".json");
    }

    /**
     * 获取动态列表
     * 
     * @param filter
     * @param pagesize
     * @param timeDirect
     * @param lasttime
     * @return
     * @throws MoMoException
     */
    public static ArrayList<DynamicItemInfo> getStatuses(int pagesize,
            String timeDirect, long lasttime, String filter) throws MoMoException {
        ArrayList<DynamicItemInfo> result = new ArrayList<DynamicItemInfo>();
        String url = "";
        if (filter == null || filter.length() == 0) {
            // 全部动态
            url = new StringBuffer(RequestUrl.STATUSES_GET)
                    .append("?pagesize=").append(pagesize)
                    .append("&").append(timeDirect).append("=").append(lasttime)
                    .toString();
        } else if (filter.indexOf("group_id") > 0) {
            // 群组动态
            url = new StringBuffer(RequestUrl.STATUSES_GROUP)
                    .append(filter)
                    .append("&pagesize=").append(pagesize)
                    .append("&").append(timeDirect).append("=").append(lasttime)
                    .toString();
        } else if (filter.indexOf("user_id") > 0) {
            // 个人动态
            url = new StringBuffer(RequestUrl.STATUSES_USER)
                    .append(filter)
                    .append("&pagesize=").append(pagesize)
                    .append("&").append(timeDirect).append("=").append(lasttime)
                    .toString();
        } else {
            return null;
        }
        
        String strResponse = HttpTool.get(url);
        try {
            JSONObject jsonResponse = new JSONObject(strResponse);
            JSONArray jsonArray = jsonResponse.optJSONArray("data");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                DynamicItemInfo statuses = new DynamicItemInfo(jsonObj);

                result.add(statuses);
            }

            // 已删除的动态
            JSONArray jsonArray_del = jsonResponse.optJSONArray("delete");
            if (jsonArray_del != null && jsonArray_del.length() > 0) {
                for (int i = 0; i < jsonArray_del.length(); ++i) {
                    JSONObject jsonObj = jsonArray_del.getJSONObject(i);
                    DynamicItemInfo statuses = new DynamicItemInfo();
                    statuses.id = jsonObj.optString("id");
                    statuses.isDeleted = true;

                    result.add(statuses);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new MoMoException(e);
        }
        return result;
    }

    /**
     * 发分享
     * 
     * @param statusesPostInfo
     * @return
     * @throws MoMoException
     */
    public static String postStatuses(DynamicPostInfo statusesPostInfo) throws MoMoException {
        String result = "";
        JSONObject param = new JSONObject();
        try {
            param.put("text", statusesPostInfo.mParamContent);
            param.put("sync", statusesPostInfo.mParamIsSyncSina ? 1 : 0);
            // param.put("location", statusesPostInfo.mParamContent);
            // param.put("group_type", statusesPostInfo.mParamContent);
            if (statusesPostInfo.mParamGroupInfo != null && statusesPostInfo.mParamGroupInfo.getGroupID() > 0) {
                param.put("group_id", statusesPostInfo.mParamGroupInfo.getGroupID());
            }
            if (statusesPostInfo.mParamRetweetId != null) {
                param.put("retweet_id", statusesPostInfo.mParamRetweetId);
            }
            JSONArray jsonArrayImages = new JSONArray();
            for (Attachment attachment : statusesPostInfo.attachmentList)
                jsonArrayImages.put(new JSONObject("{\"id\":\"" + attachment.getID() + "\"}"));
            if (jsonArrayImages.length() > 0) {
                JSONObject accessery = new JSONObject();
                accessery.put("image", jsonArrayImages);
                param.put("accessery", accessery);
            }

            Log.i(param.toString());

            String strResponse = HttpTool.post(RequestUrl.STATUSES_CREATE, param);
            JSONObject jsonResponse = new JSONObject(strResponse);
            result = jsonResponse.optString("id");
        } catch (JSONException e) {
            throw new MoMoException(e);
        }

        return result;

    }

    /**
     * 获取评论
     * 
     * @param statusesID
     * @param loadFlag
     * @param commentID_start
     * @param pagesize
     * @throws MoMoException
     */
    public static ArrayList<CommentItemInfo> getComments(String statusesID, String loadFlag,
            String commentID_start, int pagesize) throws MoMoException {
        ArrayList<CommentItemInfo> result = new ArrayList<CommentItemInfo>();
        String url = new StringBuffer(RequestUrl.COMMENT_GET)
                .append("?statuses_id=").append(statusesID)
                .append("&pagesize=").append(pagesize)
                .append("&").append(loadFlag).append("=").append(commentID_start)
                .toString();
        String response = HttpTool.get(url);
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject jsonObj = jsonArray.getJSONObject(i);
                CommentItemInfo comment = new CommentItemInfo();

                comment.id = jsonObj.optString("id");
                comment.createTime = jsonObj.optLong("created_at");

                JSONObject userJson = jsonObj.optJSONObject("user");
                if (userJson != null) {
                    comment.uid = userJson.optLong("id");
                    comment.realName = userJson.optString("name");
                    comment.avatar = userJson.optString("avatar");
                }
                comment.content = jsonObj.optString("text");
                comment.content = DynamicInfo.decodeAT(jsonObj, comment.content);
                comment.date = DynamicInfo.getTime(comment.createTime / 10);
                comment.client = jsonObj.optString("source_name");

                result.add(comment);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            throw new MoMoException(e);
        }
        return result;
    }

    /**
     * 发评论
     * 
     * @param statusesID 动态ID
     * @param content 评论内容
     * @param commentID 回复的评论ID
     * @return
     * @throws MoMoException
     */
    public static String postComment(String statusesID, String content, String commentID)
            throws MoMoException {
        String result = "";
        JSONObject param = new JSONObject();
        try {
            param.put("statuses_id", statusesID);
            param.put("text", content);
            if (commentID != null && !commentID.equals("")) {
                // 评论ID不为空，表明是回复某评论，需加上原评论ID
                param.put("comment_id", commentID);
            }
            String strResponse = HttpTool.post(RequestUrl.COMMENT_CREATE, param);
            JSONObject jsonResponse = new JSONObject(strResponse);
            result = jsonResponse.optString("id");
        } catch (JSONException e) {
            throw new MoMoException(e);
        }

        return result;
    }

    /**
     * 删除评论
     * 
     * @param commentID 评论ID
     * @throws MoMoException
     */
    public static void delComment(String commentID) throws MoMoException {
        HttpTool.get(RequestUrl.COMMENT_DEL + commentID + ".json");
    }

    /**
     * 获取群组列表
     * 
     * @return
     * @throws MoMoException
     */
    public static Group<GroupInfo> getGroupList() throws MoMoException {
        return getGroupList(0);
    }

    /**
     * 获取指定类型的群组列表
     * 
     * @param type 群类型(0 所有群，1公开群, 2私密群。默认值0)
     * @return
     * @throws MoMoException
     */
    public static Group<GroupInfo> getGroupList(int type) throws MoMoException {
        Group<GroupInfo> result = null;
        String url = new StringBuffer(RequestUrl.GROUP_LIST).append(type).toString();
        String response = HttpTool.get(url);

        try {
            JSONArray jsonArray = new JSONArray(response);
            result = new GroupParser<GroupInfo>(new GroupInfoParser()).parse(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new MoMoException(e);
        }

        return result;
    }

    /**
     * 获取群成员列表
     * 
     * @param groupID 群ID
     * @return
     * @throws MoMoException
     */
    public static Group<GroupMember> getGroupMemberList(int groupID) throws MoMoException {
        Group<GroupMember> result = null;
        String url = RequestUrl.GROUP_MEMBER_LIST.replace(":id", "" + groupID);
        String response = HttpTool.get(url);

        try {
            JSONArray jsonArray = new JSONArray(response);
            result = new GroupParser<GroupMember>(new GroupMemberParser()).parse(jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new MoMoException(e);
        }

        return result;
    }

    /**
     * 退出群
     * 
     * @param groupID 群ID
     * @throws MoMoException
     */
    public static void quitGroup(int groupID) throws MoMoException {
        String url = RequestUrl.GROUP_QUIT.replace(":id", "" + groupID);
        HttpTool.post(url, null);
    }

    /**
     * 解散群
     * 
     * @param groupID 群ID
     * @throws MoMoException
     */
    public static void delGroup(int groupID) throws MoMoException {
        String url = RequestUrl.GROUP_DESTROY.replace(":id", "" + groupID);
        HttpTool.post(url, null);
    }

    // 获取动态

    // 动态长文本

    // 发布动态

    // 删除动态

    // 收藏动态

    // 获取个人动态

    // 获取MO我的动态列表

    // MO我的设为已读

}

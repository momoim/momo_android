
package cn.com.nd.momo.model;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.nd.momo.api.types.Attachment;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.util.IMUtil;
import cn.com.nd.momo.manager.GlobalUserInfo;

public class DynamicInfo {
    private final static String TAG = "DynamicInfo";

    public final static String TYPE_PIC = "pic";

    public final static String TYPE_TEXT = "text";

    public final static String TYPE_FILE = "file";

    public String id;

    public String typeId;

    int allowRt;

    int allowComment;

    public int allowPraise;

    int allowDel;

    int allowHide;

    public String rtStatusId;

    public String text = "";

    public boolean isLongText = false;

    // user
    public long uid;

    public String realname = "";

    public String avatar = "";

    public long createAt = 0;

    public long modifiedAt;

    public int storaged;

    public String gname = "";

    public int gtype = 0;

    public int gid = 0;

    public String sourceName = "";

    public int commentCount;

    public String commentLast = "";

    public int liked;

    public int likeCount;

    public String likeList = "";

    // sync
    public String syncName;

    public int synced = -1;

    // application
    public String applicationId;

    public String applicationTitle;

    public String applicationUrl;

    public int hided;

    public String json = "";

    /**
     * 附件 
     */
    public ArrayList<Attachment> attachmentList = new ArrayList<Attachment>();
    
    public static class UserInfo {
        public String name;

        public String uid;

        public UserInfo(String n, String id) {
            int index = n.indexOf(" ");
            if (index != -1) {
                n = n.substring(0, index);
            }

            name = n;
            uid = id;
        }

        public UserInfo() {
        }
    }

    public ArrayList<UserInfo> usersAted = new ArrayList<UserInfo>();

    public String[] getUserAtedString() {
        ArrayList<UserInfo> usersAtedNotRepeat = new ArrayList<UserInfo>();

        Log.i(TAG, "getUserAtedString()" + usersAted.size());
        String[] usersArray = null;
        if (usersAted.size() > 0) {
            usersArray = new String[usersAted.size()];
        }

        for (int i = 0; i < usersAted.size(); i++) {

            String strId = usersAted.get(i).uid;

            if (strId.length() == 0) {
                usersAtedNotRepeat.add(usersAted.get(i));
                continue;
            }

            boolean isRepeat = false;
            for (int j = i + 1; j < usersAted.size(); j++) {
                if (strId.equalsIgnoreCase(usersAted.get(j).uid)) {
                    isRepeat = true;
                    break;
                }
            }

            if (!isRepeat) {
                usersAtedNotRepeat.add(usersAted.get(i));
            }
        }

        usersAted = usersAtedNotRepeat;

        for (int i = 0; i < usersAted.size(); i++) {
            UserInfo uinfo = usersAted.get(i);
            usersArray[i] = uinfo.uid.length() == 0 ? uinfo.name : "@" + uinfo.name;
        }

        return usersArray;
    }

    // images
    public ArrayList<String> images = new ArrayList<String>();

    public ArrayList<MOMOFile> files = new ArrayList<MOMOFile>();

    public class MOMOFile {
        public String type;

        public String id;

        public String title;

        public String url;

        public Meta meta;

        public MOMOFile(JSONObject jsonObj) {
            type = jsonObj.optString("type");
            id = jsonObj.optString("id");
            title = jsonObj.optString("title");
            url = jsonObj.optString("url");
            meta = new Meta(jsonObj.optJSONObject("meta"));
        }
    }

    public class Meta {
        public long size;

        public String mime;

        public Meta(JSONObject jobj) {
            size = jobj.optLong("size");
            mime = jobj.optString("mime");
        }
    }

    public ArrayList<String> getBigImages() {
        ArrayList<String> imagesBig = new ArrayList<String>();
        // 转换成大图url
        for (String imageUrl : images) {
            String url = imageUrl.replace("_80", "_780");
            url = url.replace("_160", "_780");
            url = url.replace("_130", "_780");
            imagesBig.add(url);
        }

        return imagesBig;
    }

    public DynamicInfo() {
    }

    public static String decodeAT(JSONObject info, String content, ArrayList<UserInfo> outArrayList)
            throws JSONException {
        // String pre =
        // "<a style=\"color:#3685BD\" target=\"_blank\" href=\"http://momo.im/user/164\">";//"<a style=\"color:#3685BD\" target=\"_blank\" href=\"http://momo.im/user/164\">";
        // String end = "</a>";
        // String pre = "<font color=\"#3685BD\"><u>";
        // String end = "</u></font>";
        // get at
        if (info.has("at")) {
            JSONArray atArray = info.optJSONArray("at");
            for (int j = 0; atArray != null && j < atArray.length(); ++j) {
                JSONObject atItem = (JSONObject)atArray.opt(j);
                String name = atItem.optString("name");
                String id = atItem.optString("id");
                String pre = ConfigHelper.PATTERN_USER_PRE + id + "/" + name
                        + "\">";
                String end = "</A></font>";
                content = content.replace("[@" + j + "]",
                        pre + "@" + atItem.optString("name") + end);

                if (outArrayList != null) {
                    Log.i(TAG, "add at " + name + id);
                    outArrayList.add(0, new UserInfo(name, id));
                }

            }
        }

        if (outArrayList != null) {

            String contentNoHtml = IMUtil.noHTML(content);

            Pattern pt = Pattern.compile(ConfigHelper.PATTERN_URL);
            Matcher mt = pt.matcher(contentNoHtml);

            while (mt.find()) {
                outArrayList.add(new UserInfo(mt.group(), ""));
            }

            // if (!isFound) {
            // regex="http://([^']+)";
            // pt=Pattern.compile(regex);
            // mt=pt.matcher(contentNoHtml);
            // while(mt.find()) {
            // outArrayList.add(new UserInfo(mt.group(), ""));
            // }
            // }
        }
        return content;
    }

    public static String decodeAT(JSONObject info, String content) throws JSONException {
        return decodeAT(info, content, null);
    }

    public static String decodeLikeList(JSONObject info) throws JSONException {
        return decodeLikeList(info, null);
    }

    public static String decodeLikeList(JSONObject info, ArrayList<UserInfo> outArrayList)
            throws JSONException {
        String likeList = "";
        boolean liked = false;
        if (info.has("like_list")) {
            JSONArray likesArray = info.optJSONArray("like_list");
            int likecount = info.optInt("like_count");

            String name;
            String uid;
            likeList = "";

            for (int j = 0; likesArray != null && j < likesArray.length() && j < 2; ++j) {
                name = likesArray.optJSONObject(j).optString("name");
                uid = likesArray.optJSONObject(j).optString("uid");
                String pre = ConfigHelper.PATTERN_USER_PRE + uid + "/" + name + "\">";
                String end = "</A></font>";

                if (uid.equals(GlobalUserInfo.getUID())) {
                    liked = true;
                    continue;
                }

                likeList += ((likeList.length() == 0) ? "" : "和") + pre + name
                        + end;

                if (outArrayList != null) {
                    Log.i(TAG, "add at " + name + uid);
                    outArrayList.add(0, new UserInfo(name, uid));
                }
            }

            if (liked) {
                likeList = "我" + ((likeList.length() == 0) ? "" : "和")
                        + likeList;
            }

            if (likesArray.length() > 2) {
                likeList += "和其他" + (likecount - 2) + "人";
            }

            if (likeList.length() != 0)
                likeList += "觉得这挺赞的";

        }

        return likeList;
    }

    public static String getErrorMsg(JSONObject jobj) throws JSONException {
        String error = "";
        try {
            error += jobj.optString("error");
        } catch (Exception e) {
        }
        return error;
    }

    public static String getErrorMsg(String response) {
        String error = "未知错误";
        if (response == null || response.length() == 0) {
            return error;
        }
        try {
            JSONObject jobj = new JSONObject(response);
            error = getErrorMsg(jobj);
        } catch (Exception e) {
        } finally {

        }
        return error;
    }

    public static String getTimeFormat(String format, long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date curDate = new Date(time);// 获取当前时间
        return formatter.format(curDate);
    }

    public static String getTime(long timeStamp) {
        return getTimeFormat("MM-dd HH:mm",
                timeStamp + TimeZone.getTimeZone("GMT+8:00 ").getRawOffset()
                        - TimeZone.getDefault().getRawOffset());
    }

    public DynamicInfo(DynamicInfo info) {
        super();
        this.id = info.id;
        this.typeId = info.typeId;
        this.allowRt = info.allowRt;
        this.allowComment = info.allowComment;
        this.allowPraise = info.allowPraise;
        this.allowDel = info.allowDel;
        this.allowHide = info.allowHide;
        this.rtStatusId = info.rtStatusId;
        this.text = info.text;
        this.uid = info.uid;
        this.realname = info.realname;
        this.avatar = info.avatar;
        this.createAt = info.createAt;
        this.modifiedAt = info.modifiedAt;
        this.storaged = info.storaged;
        this.gname = info.gname;
        this.gtype = info.gtype;
        this.gid = info.gid;
        this.sourceName = info.sourceName;
        this.commentCount = info.commentCount;
        this.commentLast = info.commentLast;
        this.liked = info.liked;
        this.likeCount = info.likeCount;
        this.likeList = info.likeList;
        this.syncName = info.syncName;
        this.synced = info.synced;
        this.applicationId = info.applicationId;
        this.applicationTitle = info.applicationTitle;
        this.applicationUrl = info.applicationUrl;
        this.hided = info.hided;
        this.json = info.json;
        this.images = info.images;
        this.usersAted = info.usersAted;
        this.isLongText = info.isLongText;
        this.files = info.files;
        this.attachmentList = info.attachmentList;
    }

    public DynamicInfo(String strInfo) throws JSONException {
        JSONObject info = new JSONObject(strInfo);
        decode(info);
    }

    public DynamicInfo(JSONObject info) throws JSONException {
        decode(info);
    }

    private void decode(JSONObject info) throws JSONException {
        json = info.toString();

        id = info.optString("id");
        typeId = info.optString("typeid");
        allowRt = info.optInt("allow_rt");
        allowComment = info.optInt("allow_comment");
        allowPraise = info.optInt("allow_praise");
        allowDel = info.optInt("allow_del");
        allowHide = info.optInt("allow_hide");
        rtStatusId = info.optString("rt_status_id");

        JSONObject userInfo = info.optJSONObject("user");
        if (userInfo != null) {
            uid = userInfo.optLong("id");
            avatar = userInfo.optString("avatar");
            realname = userInfo.optString("name");
        }

        createAt = info.optLong("created_at");

        storaged = info.optString("storaged").equalsIgnoreCase("true") ? 1 : 0;

        modifiedAt = info.optLong("modified_at");

        commentCount = info.optInt("comment_count");

        JSONObject gjson = info.optJSONObject("group");
        if (gjson != null && gjson.has("name")) {
            gname = gjson.optString("name");
            gid = gjson.optInt("id");
            gtype = gjson.optInt("app_id");
        }

        sourceName = info.optString("source_name");

        text = info.optString("text");
        if (text != null) {
            text = decodeAT(info, text, usersAted);
        }

        isLongText = info.optString("is_long_text").equalsIgnoreCase("1");

        likeCount = info.optInt("like_count");
        liked = info.optString("liked").equalsIgnoreCase("true") ? 1 : 0;
        likeList = decodeLikeList(info, usersAted);

        JSONArray syncObj = info.optJSONArray("sync");
        if (syncObj != null && syncObj.length() > 0) {
            syncName = syncObj.optJSONObject(0).optString("name");
            synced = syncObj.optJSONObject(0).optInt("is_sync");
        }

        JSONObject applicationObj = info.optJSONObject("application");
        if (applicationObj != null) {
            applicationId = applicationObj.optString("id");
            applicationTitle = applicationObj.optString("title");
            applicationUrl = applicationObj.optString("url");
        }

        JSONArray accessoryJson = info.optJSONArray("accessory");
        if (accessoryJson != null) {
            if (images.size() == 0) {
                for (int j = 0; accessoryJson != null && j < accessoryJson.length(); ++j) {
                    JSONObject accessoryObj = accessoryJson.optJSONObject(j);

                    String typeID = accessoryObj.optString("type");

                    if (typeID.equals(TYPE_FILE)) {
                        files.add(new MOMOFile(accessoryObj));
                    }

                    if (typeID.equals(TYPE_PIC)) {
                        String imageurl = accessoryObj.optString("url");
                        boolean isOutLink = accessoryObj.optInt("id") == 0;
                        imageurl += isOutLink ? "" : "?momolink=0";
                        images.add(imageurl);
                        
                        // 临时存储图片宽高，以后可以考虑存储整个附件信息
                        JSONObject jsonMeta = accessoryObj.optJSONObject("meta");
                        if (jsonMeta != null) {
                            Attachment attachment = new Attachment();
                            attachment.setWidth(jsonMeta.optInt("width", 240));
                            attachment.setHeight(jsonMeta.optInt("height", 240));
                            attachmentList.add(attachment);
                        }
                    }
                }
            }
        }

        int commentCount = info.optInt("comment_count");
        if (commentCount > 0) {
            JSONObject commentObj = null;

            if (info.optJSONArray("comment_list") != null) {
                JSONArray commentArray = info.optJSONArray("comment_list");
                commentObj = commentArray.optJSONObject(0);
            } else {
                commentObj = info.optJSONObject("comment_list");
            }

            if (commentObj != null) {
                JSONObject jsonUser = commentObj.optJSONObject("user");
                if (jsonUser != null) {
                    commentLast = jsonUser.optString("name") + ":" + commentObj.optString("text");
                    commentLast = decodeAT(commentObj, commentLast, usersAted);
                }
            }
        }
    }

}

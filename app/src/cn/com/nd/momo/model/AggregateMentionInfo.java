
package cn.com.nd.momo.model;

import java.util.ArrayList;

/**
 * "关于我的" 相对于动态的合集
 * 
 * @author caimk
 */
public class AggregateMentionInfo {

    public String getFeedId() {
        return feedId;
    }

    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    /**
     * @return the feedUid
     */
    public long getFeedUid() {
        return feedUid;
    }

    /**
     * @param feedUid the feedUid to set
     */
    public void setFeedUid(long feedUid) {
        this.feedUid = feedUid;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public void setFeedUrl(String feedUrl) {
        this.feedUrl = feedUrl;
    }

    /**
     * @return the client
     */
    public String getClient() {
        return client;
    }

    /**
     * @param clien the client to set
     */
    public void setClient(String client) {
        this.client = client;
    }

    /**
     * @return the content
     */
    public String getContent() {
        return content == null ? "" : content;
    }

    /**
     * @param content the content to set
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * @return the dateline
     */
    public long getDateline() {
        return dateline;
    }

    /**
     * @param dateline the dateline to set
     */
    public void setDateline(long dateline) {
        this.dateline = dateline;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the realName
     */
    public String getRealName() {
        return realName;
    }

    /**
     * @param realName the realName to set
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * @return the lstInfo
     */
    public ArrayList<MentionInfo> getLstInfo() {
        return lstInfo;
    }

    /**
     * @param lstInfo the lstInfo to set
     */
    public void setLstInfo(ArrayList<MentionInfo> lstInfo) {
        this.lstInfo = lstInfo;
    }

    private String feedId;

    private long feedUid; // 动态发布者的uid

    private String feedUrl; // 动态发布者的头像

    private String client; // 动态发布的平台

    private String content; // 动态

    private long dateline; // 动态发布时间

    private int count; // 动态里未读的"关于我的"数量

    private String realName; // 动态发布者的名字

    // 在所有动态合集列表中的时候是"关于我的"最后一条
    // 在单条动态合集中是所有的"关于我的"
    private ArrayList<MentionInfo> lstInfo;
}


package cn.com.nd.momo.mention.model;

/**
 * 单条"关于我的"的数据结构
 * 
 * @author caimk
 */
public class MentionInfo {

    private String id; // 关于我的唯一标识

    private String feedId; // 源动态Id

    private String commentId; // 回复标识id，用于回复这条回复

    private long commentUserId; // 回复者的id

    private String commentUserName; // 回复者的名字

    private String commentUserAvatar; // 回复者的头像

    private String Comment; // 回复的内容

    private String srcCommentId; // 原评论id
    // private long srcCommentUserId; // 原评论者的id
    // private String srcCommentUserName; // 原评论者的名字
    // private String srcCommentUserAvatar; // 原评论的头像

    private String srcComment; // 原评论内容

    private long dateline; // 回复的时间

    private boolean isRead; // 是否已读, true-读过了 ,false-未读

    private boolean isReply; // 是否已回复, true-回复过了，false-未回复

    private int kind; // 关于我的类型

    private String source; // "来自客户端类型:手机web端、iphone客户端、android客户端等，来自web端的该值默认为空"

    /*
     * "关于我的"有如下几种类型 1表示评论，2表示留言，3表示评论中提到我，4表示赞，5广播中提到，6表示回复
     */
    public final static int KIND_COMMENT = 1;

    public final static int KIND_MESSAGE = 2;

    public final static int KIND_COMMENT_MENTION = 3;

    public final static int KIND_LIKE = 4;

    public final static int KIND_FEED_MENTION = 5;

    public final static int KIND_REPLY = 6;
    
    private String gname = "";

    private long gid = 0;
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the feedId
     */
    public String getFeedId() {
        return feedId;
    }

    /**
     * @return the commentId
     */
    public String getCommentId() {
        return commentId;
    }

    /**
     * @return the commentUserId
     */
    public long getCommentUserId() {
        return commentUserId;
    }

    /**
     * @return the commentUserName
     */
    public String getCommentUserName() {
        return commentUserName;
    }

    /**
     * @return the commentUserAvatar
     */
    public String getCommentUserAvatar() {
        return commentUserAvatar;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return Comment;
    }

    /**
     * @return the srcCommentId
     */
    public String getSrcCommentId() {
        return srcCommentId;
    }

    // /**
    // * @return the srcCommentUserId
    // */
    // public long getSrcCommentUserId() {
    // return srcCommentUserId;
    // }
    // /**
    // * @return the srcCommentUserName
    // */
    // public String getSrcCommentUserName() {
    // return srcCommentUserName;
    // }
    // /**
    // * @return the srcCommentUserAvatar
    // */
    // public String getSrcCommentUserAvatar() {
    // return srcCommentUserAvatar;
    // }
    /**
     * @return the srcComment
     */
    public String getSrcComment() {
        return srcComment;
    }

    /**
     * @return the dateline
     */
    public long getDateline() {
        return dateline;
    }

    /**
     * @return the isRead
     */
    public boolean isRead() {
        return isRead;
    }

    /**
     * @return the isReply
     */
    public boolean isReply() {
        return isReply;
    }

    /**
     * @return the kind
     */
    public int getKind() {
        return kind;
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @param feedId the feedId to set
     */
    public void setFeedId(String feedId) {
        this.feedId = feedId;
    }

    /**
     * @param commentId the commentId to set
     */
    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    /**
     * @param commentUserId the commentUserId to set
     */
    public void setCommentUserId(long commentUserId) {
        this.commentUserId = commentUserId;
    }

    /**
     * @param commentUserName the commentUserName to set
     */
    public void setCommentUserName(String commentUserName) {
        this.commentUserName = commentUserName;
    }

    /**
     * @param commentUserAvatar the commentUserAvatar to set
     */
    public void setCommentUserAvatar(String commentUserAvatar) {
        this.commentUserAvatar = commentUserAvatar;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        Comment = comment;
    }

    /**
     * @param srcCommentId the srcCommentId to set
     */
    public void setSrcCommentId(String srcCommentId) {
        this.srcCommentId = srcCommentId;
    }

    // /**
    // * @param srcCommentUserId the srcCommentUserId to set
    // */
    // public void setSrcCommentUserId(long srcCommentUserId) {
    // this.srcCommentUserId = srcCommentUserId;
    // }
    // /**
    // * @param srcCommentUserName the srcCommentUserName to set
    // */
    // public void setSrcCommentUserName(String srcCommentUserName) {
    // this.srcCommentUserName = srcCommentUserName;
    // }
    // /**
    // * @param srcCommentUserAvatar the srcCommentUserAvatar to set
    // */
    // public void setSrcCommentUserAvatar(String srcCommentUserAvatar) {
    // this.srcCommentUserAvatar = srcCommentUserAvatar;
    // }
    /**
     * @param srcComment the srcComment to set
     */
    public void setSrcComment(String srcComment) {
        this.srcComment = srcComment;
    }

    /**
     * @param dateline the dateline to set
     */
    public void setDateline(long dateline) {
        this.dateline = dateline;
    }

    /**
     * @param isRead the isRead to set
     */
    public void setRead(boolean isRead) {
        this.isRead = isRead;
    }

    /**
     * @param isReply the isReply to set
     */
    public void setReply(boolean isReply) {
        this.isReply = isReply;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(int kind) {
        this.kind = kind;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * @return the gname
     */
    public String getGroupName() {
        return gname;
    }

    /**
     * @param gname the gname to set
     */
    public void setGroupName(String gname) {
        this.gname = gname;
    }

    /**
     * @return the gid
     */
    public long getGroupId() {
        return gid;
    }

    /**
     * @param gid the gid to set
     */
    public void setGroupId(long gid) {
        this.gid = gid;
    }

}

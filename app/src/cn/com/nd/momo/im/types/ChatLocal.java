
package cn.com.nd.momo.im.types;

import android.graphics.Bitmap;
import android.net.Uri;
import cn.com.nd.momo.api.types.Chat;
import cn.com.nd.momo.api.types.ChatContent;
import cn.com.nd.momo.api.types.IChat;
import cn.com.nd.momo.api.types.MomoType;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.types.UserList;


/**
 * 本地数据库映射
 * 
 * @author tsung
 */
public class ChatLocal implements Comparable<ChatLocal>, IChat, MomoType {
    public ChatLocal(Chat chat) {
        setChat(chat);
    }

    private Chat chat;

    private ChatLocal setChat(Chat chat) {
        this.chat = chat;
        return this;
    }

    public Chat getChat() {
        return chat;
    }

    /**
     * 内存数据字段开始
     */

    // 地图照片声音需要下载状态
    public boolean isDownloading = false;

    public boolean isDownloadfail = false;

    public boolean isSelected = false;

    public boolean isDetail = false;

    public Bitmap bitmapImage;

    public String localFilePath;

    public byte[] bytes;

    // 短信相关

    // 是否本地短信
    public long threadID = 0;

    public boolean isSms = false;

    public boolean isMms = false;

    public boolean isMmsSms() {
        return isSms || isMms;
    }

    // 用于删除某条短信
    public Uri uri;



    /**
     * 内存数据字段结束
     */

    @Override
    public int compareTo(ChatLocal other) {
        long self = chat.getTimestamp();
        if (self < 10000000000L) {
            self = self * 1000;
        }
        long them = other.getChat().getTimestamp();
        if (them < 10000000000L) {
            them = them * 1000;
        }
        if (self > them) {
            return isDetail ? 1 : -1;
        } else if (self < them) {
            return isDetail ? -1 : 1;
        } else {
            return 0;
        }
    }

    @Override
    public boolean isSecretary() {
        return chat.isSecretary();
    }

    @Override
    public boolean isOut() {
        return chat.isOut();
    }

    @Override
    public String getKind() {
        return chat.getKind();
    }

    @Override
    public UserList getOther() {
        return chat.getOther();
    }

    @Override
    public String getId() {
        return chat.getId();
    }

    @Override
    public ChatLocal setId(String id) {
        chat.setId(id);
        return this;
    }

    @Override
    public User getSender() {
        return chat.getSender();
    }

    @Override
    public ChatLocal setSender(User sender) {
        chat.setSender(sender);
        return this;
    }

    @Override
    public UserList getReceiver() {
        return chat.getReceiver();
    }

    @Override
    public ChatLocal setReceiver(UserList receiver) {
        chat.setReceiver(receiver);
        return this;
    }

    @Override
    public long getTimestamp() {
        return chat.getTimestamp();
    }

    @Override
    public ChatLocal setTimestamp(long timestamp) {
        chat.setTimestamp(timestamp);
        return this;
    }

    @Override
    public int getClientid() {
        return chat.getClientid();
    }

    @Override
    public ChatLocal setClientid(int clientid) {
        chat.setClientid(clientid);
        return this;
    }

    @Override
    public ChatContent getContent() {
        return chat.getContent();
    }

    @Override
    public ChatLocal setContent(ChatContent content) {
        chat.setContent(content);
        return this;
    }

    @Override
    public ChatLocal setState(int state) {
        chat.setState(state);
        return this;
    }

    @Override
    public int getState() {
        return chat.getState();
    }
}

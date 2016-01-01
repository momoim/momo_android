
package cn.com.nd.momo.api;

import java.util.List;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import cn.com.nd.momo.api.sync.ContactDatabaseHelper;
import cn.com.nd.momo.api.sync.LocalContactsManager;
import cn.com.nd.momo.api.sync.MoMoContactsManager;
import cn.com.nd.momo.api.sync.SyncManager;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.util.ConfigHelper;

public final class SyncContactApi {
    private static Context mContext = null;

    private static volatile SyncContactApi mInstance = null;

    public static final int MSG_SYNC_PROCESS_START = 1;

    public static final int MSG_SYNC_PROCESS_PART_FINISHED = MSG_SYNC_PROCESS_START + 1;

    public static final int MSG_SYNC_PROCESS_FINISHED = MSG_SYNC_PROCESS_START + 2;

    public static final int MSG_MQ_PROCESS_FINISHED = MSG_SYNC_PROCESS_START + 3;

    public static final int MSG_SYNC_NO_NET_WORK = MSG_SYNC_PROCESS_START + 4;
    
    public static final int MSG_SYNC_IN_PROCESS = MSG_SYNC_PROCESS_START + 5;
    
    private SyncContactApi() {

    }

    public static synchronized SyncContactApi getInstance(Context context) {
        if (null == mInstance) {
            mContext = context.getApplicationContext();
            ContactDatabaseHelper.initDatabase(mContext);
            mInstance = new SyncContactApi();
        }
        return mInstance;
    }

    /**
     * 本地手机联系人和MoMo服务器联系人同步
     */
    public void serverSync() {
        SyncManager.getInstance().serverSync();
    }
    
    public void backAddMoMoAcountSync() {
        SyncManager.getInstance().backAddMoMoAcountSync();
    }

    /**
     * 不同步，加载本地手机联系人
     */
    public void localSync() {
        SyncManager.getInstance().localSync();
    }

    /**
     * 上传本地手机所有联系人到服务器
     * 
     * @return
     */
    public boolean uploadLocalContactsToServer() {
        return SyncManager.getInstance().uploadLocalContactsToServer();
    }

    /**
     * 停止同步
     */
    public synchronized void stopSync() {
        SyncManager.getInstance().stopSync();
    }

    /**
     * 设置同步接收handler，加载本地手机联系人和本地联系人与服务器联系人同步，可设置handler接收Message查看或显示同步过程情况，
     * Message.what可存在下列类型：MSG_SYNC_PROCESS_START(同步开始)，
     * MSG_SYNC_PROCESS_PART_FINISHED(同步过程一部分完成)，
     * MSG_SYNC_PROCESS_FINISHED(同步完成)， MSG_MQ_PROCESS_FINISHED(mq消息请求的同步完成), MSG_SYNC_NO_NET_WORK(没有找到可用网络)，
     * MSG_SYNC_IN_PROCESS(同步已经正在进行中)
     * 
     * @param handler
     */
    public void setHandler(Handler handler) {
        SyncManager.getInstance().setHandler(handler);
    }

    /**
     * 设置附加同步接收hanlder，同setHandler，只接收Message.what为MSG_SYNC_PROCESS_START(同步开始)，
     * MSG_SYNC_PROCESS_FINISHED(同步完成)的消息，, MSG_SYNC_NO_NET_WORK(没有找到可用网络)，MSG_SYNC_IN_PROCESS(同步已经正在进行中)
     * 
     * @param handler
     */
    public void setAdditionalHandler(Handler handler) {
        SyncManager.getInstance().setAdditionalHandler(handler);
    }

    /**
     * 检查当前是否正在进行联系人同步(包括正在加载本地联系人(不同步)和本地联系人与服务器联系人正在同步)
     * 
     * @return boolean
     */
    public boolean isSyncInProgress() {
        return SyncManager.getInstance().isSyncInProgress();
    }

    /**
     * 删除同步之后的momo数据库联系人,如果正在同步中，不允许删除联系人，可先停止同步再进行删除
     * 
     * @return boolean
     */
    public boolean deleteMoMoDatabaseContacts() {
        if (isSyncInProgress()) {
            return false;
        } else {
            return MoMoContactsManager.getInstance().deleteAllContacts();
        }
    }

    /**
     * 根据服务器联系人Id获取联系人信息
     * 
     * @param contactId MoMoContactsManager.getInstance().
     * @return Contact
     */
    public Contact getContactById(long contactId) {
        return MoMoContactsManager.getInstance().getContactById(contactId);
    }
    
    /**
     * 获取同步方式
     * 
     * @return
     */
    public String getSyncMode() {
        ConfigHelper config = ConfigHelper.getInstance(mContext);
        String syncMode = config.loadKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
        return syncMode;
    }

    /**
     * 获取手机sim卡上的联系人总数
     * 
     * @return 联系人数量
     */
    public int getSimContactsCount() {
        return LocalContactsManager.getInstance().getSimContactsCount();
    }
    
    /**
     * 获取手机sim卡上所有联系人
     * 
     * @return 联系人列表
     */
    public List<Contact> getSimContactList() {
        return LocalContactsManager.getInstance().getSimContacts();
    }
    
    /**
     * 根据手机联系人帐号获取对用帐号所有联系人
     * 
     * @param account 手机帐号
     * @return 联系人列表
     */
    public List<Contact> getLocalContactListByAccount(Account account) {
        return LocalContactsManager.getInstance().getAllContactsListByAccount(account);
    }
    
    /**
     * 批量添加联系人到手机指定的帐号(包括空帐号)
     * 
     * @param contactList 联系人列表
     */
    public void addContactListToAccount(List<Contact> contactList, Account account) {
        LocalContactsManager.getInstance().batchAddContacts(contactList, account);
    }
    
    /**
     * 获取要显示的联系人列表（只有contactId, formatName, namePinyin三个字段，只为显示列表和搜索使用）
     * 
     * @return 联系人列表
     */
    public List<Contact> getDisplayContactList() {
        return MoMoContactsManager.getInstance().getAllContactsBriefInfo();
    }
    
}

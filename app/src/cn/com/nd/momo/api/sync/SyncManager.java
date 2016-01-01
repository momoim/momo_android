
package cn.com.nd.momo.api.sync;

import android.os.Handler;
import android.os.Message;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.util.Utils;

/**
 * 同步管理器
 * 
 * @author chenjp
 */
public class SyncManager {

    private static volatile SyncManager instance = null;

    private static ContactSyncManager contactSyncManager;

    private volatile boolean mNeedSyncToLocal = false;

    private volatile boolean mNeedSyncToServer = false;

    private volatile boolean isSyncInProgress = false;

    public volatile boolean needStopSync = false;

    private Handler mHandler;

    private Handler mAdditionalHandler;

    private SyncManager() {

    }

    public static synchronized SyncManager getInstance() {
        if (null == instance) {
            ContactDatabaseHelper.initDatabase(Utils.getContext());
            instance = new SyncManager();
            contactSyncManager = ContactSyncManager.getInstance();
        }
        return instance;
    }

    public void backAddMoMoAcountSync() {
        android.util.Log.i("MQService", "isSyncInProgress:" + isSyncInProgress);
        if (isSyncInProgress) {
            return;
        }
        isSyncInProgress = true;
        contactSyncManager.backAddMoMoAcountSync();
        isSyncInProgress = false;
    }
    
    public void serverSync() {
        if (isSyncInProgress) {
            sendMsg(SyncContactApi.MSG_SYNC_IN_PROCESS);
            sendAdditionalMsg(SyncContactApi.MSG_SYNC_IN_PROCESS);
            return;
        }
        
        if (null == Utils.getActiveNetWorkName(Utils.getContext())) {
            sendMsg(SyncContactApi.MSG_SYNC_NO_NET_WORK);
            sendAdditionalMsg(SyncContactApi.MSG_SYNC_IN_PROCESS);
            return;
        }
        
        ContactSyncConfigHelper config = ContactSyncConfigHelper.getInstance(Utils.getContext());
        final String syncMode = config.loadKey(ContactSyncConfigHelper.CONFIG_KEY_SYNC_MODE);
        if (!ContactSyncConfigHelper.SYNC_MODE_TWO_WAY.equals(syncMode)) {
            emptyAllMoMoDatabase();
            config.saveKey(ContactSyncConfigHelper.CONFIG_KEY_SYNC_MODE,
                    ContactSyncConfigHelper.SYNC_MODE_TWO_WAY);
        }
        
        // 双向
        needStopSync = false;
        mNeedSyncToLocal = true;
        mNeedSyncToServer = true;
        isSyncInProgress = true;
        executeSync();
    }

    /**
     * execute sync
     */
    private void executeSync() {
        sendMsg(SyncContactApi.MSG_SYNC_PROCESS_START);
        sendAdditionalMsg(SyncContactApi.MSG_SYNC_PROCESS_START);
        contactSyncManager.syncContacts();
        sendMsg(SyncContactApi.MSG_SYNC_PROCESS_FINISHED);
        sendAdditionalMsg(SyncContactApi.MSG_SYNC_PROCESS_FINISHED);
        isSyncInProgress = false;
    }

    /**
     * 当用户选择不同步时，执行本地同步，将本地库中的联系人导入到ｍｏｍｏ表中。
     */
    public void localSync() {
        if (isSyncInProgress) {
            sendMsg(SyncContactApi.MSG_SYNC_IN_PROCESS);
            sendAdditionalMsg(SyncContactApi.MSG_SYNC_IN_PROCESS);
            return;
        }

        ContactSyncConfigHelper config = ContactSyncConfigHelper.getInstance(Utils.getContext());
        final String syncMode = config.loadKey(ContactSyncConfigHelper.CONFIG_KEY_SYNC_MODE);
        if (!ContactSyncConfigHelper.SYNC_MODE_LOCAL_ONLY.equals(syncMode)) {
            emptyAllMoMoDatabase();
            config.saveKey(ContactSyncConfigHelper.CONFIG_KEY_SYNC_MODE,
                    ContactSyncConfigHelper.SYNC_MODE_LOCAL_ONLY);
        }

        isSyncInProgress = true;
        needStopSync = false;
        sendMsg(SyncContactApi.MSG_SYNC_PROCESS_START);
        sendAdditionalMsg(SyncContactApi.MSG_SYNC_PROCESS_START);
        contactSyncManager.syncLocalToMoMo();
        isSyncInProgress = false;
        sendMsg(SyncContactApi.MSG_SYNC_PROCESS_FINISHED);
        sendAdditionalMsg(SyncContactApi.MSG_SYNC_PROCESS_FINISHED);
    }

    public boolean uploadLocalContactsToServer() {
        if (isSyncInProgress) {
            return false;
        }
        isSyncInProgress = true;
        needStopSync = false;
        boolean result = contactSyncManager.uploadLocalContactsToServer();
        isSyncInProgress = false;
        return result;
    }

    /**
     * 清空所有MOMO表数据
     */
    public boolean emptyAllMoMoDatabase() {
        if (isSyncInProgress) {
            return false;
        } else {
            return MoMoContactsManager.getInstance().deleteAllContacts();
        }
    }

    public void sendMsg(int what) {
        if (null == mHandler)
            return;
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    public void sendAdditionalMsg(int what) {
        if (null == mAdditionalHandler)
            return;
        Message msg = new Message();
        msg.what = what;
        mAdditionalHandler.sendMessage(msg);
    }

    public boolean needSyncToLocal() {
        return mNeedSyncToLocal;
    }

    public boolean needSyncToServer() {
        return mNeedSyncToServer;
    }

    public synchronized void setSyncInProgress(boolean bInProgress) {
        isSyncInProgress = bInProgress;
    }

    public boolean isSyncInProgress() {
        return isSyncInProgress;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void setAdditionalHandler(Handler handler) {
        this.mAdditionalHandler = handler;
    }

    public synchronized void stopSync() {
        needStopSync = true;
    }

}

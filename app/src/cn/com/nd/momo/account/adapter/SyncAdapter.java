/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package cn.com.nd.momo.account.adapter;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

public class SyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = "SyncAdapter";

    private Context mContext = null;

    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.d(TAG, "SyncAdapter");
        mContext = context;
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "sync contacts!");
        /*
         * ConfigHelper configHelper = ConfigHelper.getInstance(mContext);
         * configHelper.setContext(mContext);
         * GlobalUserInfo.checkLoginStatus(mContext.getApplicationContext()); if
         * (!GlobalUserInfo.hasLogined()) { Log.d(TAG, "not login!"); return; }
         * final boolean isTryOnce =
         * configHelper.loadBooleanKey(ConfigHelper.CONFIG_TRY_ONCE_USER,
         * false); if (isTryOnce) { Log.d(TAG, "trye once"); return; } if
         * (GlobalUserInfo.getUserStatus() < GlobalUserInfo.STATUS_VERIFY_USER)
         * { Log.d(TAG, "unverified user!"); return; } final String syncMode =
         * configHelper.loadKey(ConfigHelper.CONFIG_KEY_SYNC_MODE); if
         * (syncMode.length() < 1 ||
         * !ConfigHelper.SYNC_MODE_TWO_WAY.equals(syncMode)) { Log.d(TAG,
         * "not sync"); return; } Log.d(TAG, "account name:" + account.name +
         * " account type:" + account.type); Account accountConfig =
         * Utils.getCurrentAccount(); if (account == null || accountConfig ==
         * null || !Utils.isBindedAccountExist(accountConfig) ||
         * !account.name.equals(accountConfig.name) ||
         * !account.type.equals(accountConfig.type)) { Log.d(TAG,
         * "account illegal"); return; } boolean importAccounts =
         * configHelper.loadBooleanKey( ConfigHelper.CONFIG_KEY_IMPORT_ACCOUNTS,
         * false); if (!importAccounts) { Log.d(TAG, "not import accounts");
         * return; } if (!SyncManager.getInstance().isSyncInProgress()) { //TODO
         * SyncManager.getInstance()不一致？ // begin sync Thread tSync = new
         * Thread() {
         * @Override public void run() { Log.d(TAG, "start sync"); Log.d(TAG,
         * "sync mode:" + syncMode); if (isTryOnce) {
         * SyncManager.getInstance().syncLocal(false); } else {
         * SyncManager.getInstance().sync(syncMode); }
         * CardManager.getInstance().updateUserIdListCache(mContext, false); }
         * }; // tSync.start(); GlobalUserInfo.startSyncThread(tSync); }
         */
    }
}

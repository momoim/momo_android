
package com.android.mms.util;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.GlobalContactList;
import cn.com.nd.momo.api.util.Log;

import com.android.mms.data.Telephony.Mms;
import com.android.mms.exception.MmsException;
import com.android.mms.pdu.EncodedStringValue;
import com.android.mms.pdu.NotificationInd;
import com.android.mms.pdu.PduPersister;

/**
 * 当处于漫游状态的时候，自动下载关闭，其余时候均自动下载
 * 
 * @author mk
 */
public class DownloadManager {
    private static final String TAG = "DownloadManager";

    private static final boolean DEBUG = false;

    private static final String ACTION_SERVICE_STATE_CHANGED = "android.intent.action.SERVICE_STATE";

    private static final int DEFERRED_MASK = 0x04;

    public static final int STATE_UNSTARTED = 0x80;

    public static final int STATE_DOWNLOADING = 0x81;

    public static final int STATE_TRANSIENT_FAILURE = 0x82;

    public static final int STATE_PERMANENT_FAILURE = 0x87;

    private final Context mContext;

    private final Handler mHandler;

    private boolean mAutoDownload;

    private final BroadcastReceiver mRoamingStateListener =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (ACTION_SERVICE_STATE_CHANGED.equals(intent.getAction())) {
                        Log.v(TAG, "Service state changed: " + intent.getExtras());
                        boolean isRoaming = intent.getBooleanExtra("roaming", false);
                        Log.d(TAG, "roaming ------> " + isRoaming);
                        synchronized (sInstance) {
                            mAutoDownload = !isRoaming(mContext);
                        }
                    }
                }
            };

    private static DownloadManager sInstance;

    private DownloadManager(Context context) {
        mContext = context;
        mHandler = new Handler();

        context.registerReceiver(
                mRoamingStateListener,
                new IntentFilter(ACTION_SERVICE_STATE_CHANGED));

        mAutoDownload = !isRoaming(mContext); // 如果处于漫游状态，将不自动下载
    }

    public boolean isAuto() {
        return mAutoDownload;
    }

    public static void init(Context context) {
        if (sInstance != null) {
            Log.w(TAG, "Already initialized.");
        }
        sInstance = new DownloadManager(context);
    }

    public static DownloadManager getInstance() {
        if (sInstance == null) {
            throw new IllegalStateException("Uninitialized.");
        }
        return sInstance;
    }

    static boolean isRoaming(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.isNetworkRoaming();
    }

    public void markState(final Uri uri, int state) {
        // Notify user if the message has expired.
        try {
            NotificationInd nInd = (NotificationInd)PduPersister.getPduPersister(mContext)
                    .load(uri);
            if ((nInd.getExpiry() < System.currentTimeMillis() / 1000L)
                    && (state == STATE_DOWNLOADING)) {
                mHandler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(mContext, R.string.dl_expired_notification,
                                Toast.LENGTH_LONG).show();
                    }
                });
                mContext.getContentResolver().delete(uri, null, null);
                return;
            }
        } catch (MmsException e) {
            Log.e(TAG, e.getMessage(), e);
            return;
        }

        // Notify user if downloading permanently failed.
        if (state == STATE_PERMANENT_FAILURE) {
            mHandler.post(new Runnable() {
                public void run() {
                    try {
                        Toast.makeText(mContext, getMessage(uri),
                                Toast.LENGTH_LONG).show();
                    } catch (MmsException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            });
        } else if (!mAutoDownload) {
            state |= DEFERRED_MASK;
        }

        // Use the STATUS field to store the state of downloading process
        // because it's useless for M-Notification.ind.
        ContentValues values = new ContentValues(1);
        values.put(Mms.STATUS, state);
        mContext.getContentResolver().update(uri, values, null, null);
    }

    public void showErrorCodeToast(int errorStr) {
        final int errStr = errorStr;
        mHandler.post(new Runnable() {
            public void run() {
                try {
                    Toast.makeText(mContext, errStr, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e(TAG, "Caught an exception in showErrorCodeToast");
                }
            }
        });
    }

    private String getMessage(Uri uri) throws MmsException {
        NotificationInd ind = (NotificationInd)PduPersister
                .getPduPersister(mContext).load(uri);

        EncodedStringValue v = ind.getSubject();
        String subject = (v != null) ? v.getString()
                : mContext.getString(R.string.no_subject);

        v = ind.getFrom();
        String from = (v != null)
                ? GlobalContactList.getInstance().getContactNameByMobile(v.getString())
                : mContext.getString(R.string.unknown_sender);

        return mContext.getString(R.string.dl_failure_notification, subject, from);
    }

    public int getState(Uri uri) {
        Cursor cursor = mContext.getContentResolver().query(uri,
                new String[] {
                    Mms.STATUS
                },
                null,
                null,
                null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0) & ~DEFERRED_MASK;
                }
            } finally {
                cursor.close();
            }
        }
        return STATE_UNSTARTED;
    }
}

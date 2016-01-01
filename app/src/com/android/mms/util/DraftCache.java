
package com.android.mms.util;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import cn.com.nd.momo.api.util.Log;

import com.android.mms.data.Telephony.MmsSms;
import com.android.mms.data.Telephony.Sms.Conversations;

/**
 * Cache for information about draft messages on conversations.
 */
public class DraftCache {
    private static final String TAG = "Mms/draft";

    private static DraftCache sInstance;

    private final Context mContext;

    private HashSet<Long> mDraftSet = new HashSet<Long>(4);

    private final HashSet<OnDraftChangedListener> mChangeListeners = new HashSet<OnDraftChangedListener>(
            1);

    public interface OnDraftChangedListener {
        void onDraftChanged(long threadId, boolean hasDraft);
    }

    private DraftCache(Context context) {
        mContext = context;
        refresh();
    }

    static final String[] DRAFT_PROJECTION = new String[] {
            Conversations.THREAD_ID
            // 0
    };

    static final int COLUMN_DRAFT_THREAD_ID = 0;

    /**
     * To be called whenever the draft state might have changed. Dispatches work
     * to a thread and returns immediately.
     */
    public void refresh() {
        new Thread(new Runnable() {
            public void run() {
                rebuildCache();
            }
        }).start();
    }

    /**
     * Does the actual work of rebuilding the draft cache.
     */
    private synchronized void rebuildCache() {
        Log.d(TAG, "rebuildCache");
        HashSet<Long> oldDraftSet = mDraftSet;
        HashSet<Long> newDraftSet = new HashSet<Long>(oldDraftSet.size());
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(
                MmsSms.CONTENT_DRAFT_URI,
                DRAFT_PROJECTION, null, null, null);
        try {
            if (cursor.moveToFirst()) {
                for (; !cursor.isAfterLast(); cursor.moveToNext()) {
                    long threadId = cursor.getLong(COLUMN_DRAFT_THREAD_ID);
                    newDraftSet.add(threadId);
                }
            }
        } finally {
            cursor.close();
        }

        mDraftSet = newDraftSet;

        // If nobody's interested in finding out about changes,
        // just bail out early.
        if (mChangeListeners.size() < 1) {
            return;
        }

        // Find out which drafts were removed and added and notify
        // listeners.
        Set<Long> added = new HashSet<Long>(newDraftSet);
        added.removeAll(oldDraftSet);
        Set<Long> removed = new HashSet<Long>(oldDraftSet);
        removed.removeAll(newDraftSet);

        for (OnDraftChangedListener l : mChangeListeners) {
            for (long threadId : added) {
                l.onDraftChanged(threadId, true);
            }
            for (long threadId : removed) {
                l.onDraftChanged(threadId, false);
            }
        }
    }

    /**
     * Updates the has-draft status of a particular thread on a piecemeal basis,
     * to be called when a draft has appeared or disappeared.
     */
    public synchronized void setDraftState(long threadId, boolean hasDraft) {
        if (threadId <= 0) {
            return;
        }

        boolean changed;
        if (hasDraft) {
            changed = mDraftSet.add(threadId);
        } else {
            changed = mDraftSet.remove(threadId);
        }

        // Notify listeners if there was a change.
        if (changed) {
            for (OnDraftChangedListener l : mChangeListeners) {
                l.onDraftChanged(threadId, hasDraft);
            }
        }
    }

    /**
     * Returns true if the given thread ID has a draft associated with it, false
     * if not.
     */
    public synchronized boolean hasDraft(long threadId) {
        return mDraftSet.contains(threadId);
    }

    public synchronized void addOnDraftChangedListener(OnDraftChangedListener l) {
        mChangeListeners.add(l);
    }

    public synchronized void removeOnDraftChangedListener(OnDraftChangedListener l) {
        mChangeListeners.remove(l);
    }

    /**
     * Initialize the global instance. Should call only once.
     */
    public static void init(Context context) {
        sInstance = new DraftCache(context);
    }

    /**
     * Get the global instance.
     */
    public static DraftCache getInstance() {
        return sInstance;
    }

    public void dump() {
        Log.i(TAG, "dump:");
        for (Long threadId : mDraftSet) {
            Log.i(TAG, "  tid: " + threadId);
        }
    }

    public static void registerContentObserver(Context context) {
        ContentObserver observer = sInstance.new DraftContentObserver();
        context.getContentResolver().registerContentObserver(MmsSms.CONTENT_DRAFT_URI,
                false,
                observer);

    }

    public class DraftContentObserver extends ContentObserver {

        public DraftContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            getInstance().refresh();
        }
    }

}

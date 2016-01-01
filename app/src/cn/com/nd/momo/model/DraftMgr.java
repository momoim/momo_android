
package cn.com.nd.momo.model;

import java.util.ArrayList;

import cn.com.nd.momo.model.DynamicDB.DraftInfo;

public class DraftMgr {
    private static DraftMgr mInstance = null;

    public static DraftMgr instance() {
        if (mInstance == null) {
            mInstance = new DraftMgr();
            mInstance.initDraft();
        }
        return mInstance;
    }

    private ArrayList<DraftInfo> mArrayDraftInfo = new ArrayList<DraftInfo>();

    public synchronized long insertDraft(DraftInfo dinfo) {
        long id = DynamicDB.instance().insertDraft(dinfo);
        dinfo.id = id;
        mArrayDraftInfo.add(0, dinfo);
        return id;
    }

    public synchronized void deleteAll() {
        DynamicDB.instance().deleteAllDraft();
        mArrayDraftInfo.clear();
    }

    public synchronized void deleteDraft(long id) {
        DynamicDB.instance().deleteDraft(id);

        for (int i = 0; i < mArrayDraftInfo.size(); i++) {
            if (mArrayDraftInfo.get(i).id == id) {
                mArrayDraftInfo.remove(i);
            }
        }
    }

    public ArrayList<DraftInfo> getDraft() {
        return mArrayDraftInfo;
    }

    public ArrayList<DraftInfo> getDraftFromDB() {
        initDraft();
        return mArrayDraftInfo;
    }

    private ArrayList<DraftInfo> initDraft() {
        mArrayDraftInfo = DynamicDB.instance().queryDraft();
        return mArrayDraftInfo;
    }

    public synchronized int exist(long id) {
        for (int i = 0; i < mArrayDraftInfo.size(); i++) {
            if (mArrayDraftInfo.get(i).id == id) {
                return i;
            }
        }
        return -1;
    }

    public DraftInfo getDraft(long id) {
        int index = exist(id);
        if (index != -1) {
            return mArrayDraftInfo.get(index);
        }
        return null;
    }

    public static String DRAFT_ID = "DraftID";

    public static String DRAFT_PROCESS = "Process";
}

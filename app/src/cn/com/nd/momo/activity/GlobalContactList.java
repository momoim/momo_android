
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.com.nd.momo.api.sync.MoMoContactsManager;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.util.PinYinComparator;
import cn.com.nd.momo.api.util.PinyinHelper;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.CardManager;
import cn.com.nd.momo.manager.CardManager.IgnoreMobilePrefix;
import cn.com.nd.momo.manager.GlobalUserInfo;

public class GlobalContactList {
    private static GlobalContactList mInstance = null;

    private Map<Long, Contact> mGlobalContactMap = new ConcurrentHashMap<Long, Contact>();

    private Map<String, String> mGlobalContactNameMap = new ConcurrentHashMap<String, String>();

    private Map<String, Long> mGlobalContacIdMap = new ConcurrentHashMap<String, Long>();

    private List<Contact> mGlobalContactList = new ArrayList<Contact>();

    private List<Contact> mGlobalMoContactList = new ArrayList<Contact>();

    private GlobalContactList() {

    }

    public static GlobalContactList getInstance() {
        if (mInstance == null) {
            mInstance = new GlobalContactList();
        }
        return mInstance;
    }

    public Map<String, String> getContactNameMap() {
        return mGlobalContactNameMap;
    }

    public synchronized void loadDisplayContactList() {
        MoMoContactsManager mContactManager = MoMoContactsManager.getInstance();
        // mContactManager.getAllDisplayContactsList(mGlobalContactList,
        // mGlobalMoContactList);
        mGlobalContactList = mContactManager.getAllDisplayContactsList();
        convertNameToPinyin(mGlobalContactList);
        sortContactByPinyin(mGlobalContactList);
        // convertNameToPinyin(mGlobalMoContactList);
        // sortContactByPinyin(mGlobalMoContactList);
        mGlobalContactMap.clear();

        for (Contact contact : mGlobalContactList) {
            if (null != contact) {
                long contactId = contact.getContactId();
                if (contactId > 0)
                    mGlobalContactMap.put(contactId, contact);
            }
        }
        // for (Contact contact : mGlobalMoContactList) {
        // if (null != contact) {
        // long contactId = contact.getContactId();
        // if (contactId > 0)
        // mGlobalContactMap.put(contactId, contact);
        // }
        // }
    }

    public String getContactNameByMobile(String mobile) {
        String name = "";
        if (mobile == null || mobile.length() < 1) {
            return name;
        }
        String zoneCode = GlobalUserInfo.getCurrentZoneCode(Utils.getContext());
        IgnoreMobilePrefix ignoreMobilePrefix = null;
        ignoreMobilePrefix = CardManager.getInstance().getIgnoreObject(Utils.getContext());
        mobile = ignoreMobilePrefix.ignore(zoneCode, mobile);
        name = mGlobalContactNameMap.get(mobile);
        if (name == null) {
            name = "";
        }
        return name;
    }

    public Contact getContactByMobile(String mobile) {
        Contact contact = null;
        if (mobile == null || mobile.length() < 1) {
            return contact;
        }
        String zoneCode = GlobalUserInfo.getCurrentZoneCode(Utils.getContext());
        IgnoreMobilePrefix ignoreMobilePrefix = null;
        ignoreMobilePrefix = CardManager.getInstance().getIgnoreObject(Utils.getContext());
        mobile = ignoreMobilePrefix.ignore(zoneCode, mobile);
        Long contactId = mGlobalContacIdMap.get(mobile);
        if (contactId == null) {
            return null;
        }
        if (contactId > 1) {
            contact = MoMoContactsManager.getInstance().getContactById(contactId);
        }
        return contact;
    }

    private synchronized void convertNameToPinyin(List<Contact> contactsList) {
        for (Contact displayContact : contactsList) {
            String name = displayContact.getFormatName();
            if (null == name) {
                name = "";
                continue;
            }
            String[][] originalPinyinArray = PinyinHelper
                    .convertChineseToPinyinArray(name);
            displayContact.setNamePinyin(originalPinyinArray);
        }
    }

    public synchronized void sortContactByPinyin(List<Contact> srcArray) {
        Collections.sort(srcArray, PinYinComparator.getInstance());
    }

    public Map<Long, Contact> getContactMap() {
        return mGlobalContactMap;
    }

    public List<Contact> getContactList() {
        return mGlobalContactList;
    }

    public List<Contact> getMoContactList() {
        return mGlobalMoContactList;
    }

}

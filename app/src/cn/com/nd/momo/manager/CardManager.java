
package cn.com.nd.momo.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import cn.com.nd.momo.activity.GlobalContactList;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.parsers.json.ContactParser;
import cn.com.nd.momo.api.sync.LocalContactsManager;
import cn.com.nd.momo.api.sync.MoMoContactsManager;
import cn.com.nd.momo.api.types.Avatar;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.types.Weibo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

/**
 * 用户名片管理类
 * 
 * @author jiaolei
 */
public class CardManager {
    private static String TAG = "CardManager";

    private static CardManager instance = null;

    private SQLiteDatabase mDB = null;

    public long MOMO_USER = 3;

    public static String KEY_COUNT = "count";

    public static String KEY_START = "start";

    public static String KEY_POS = "pos";

    public static String KEY_COMMON_COUNT = "common_count";

    public static String KEY_DATA = "data";

    public static String KEY_COMMON_DATA = "common_data";

    public static String KEY_RESPONSE_STATUS = "response_status";

    public static String KEY_RESPONSE_EXCPETION_TEXT = "response_exception_text";

    public static String KEY_RESPONSE_CONTENT = "response_content";

    public static String KEY_USER_ID = "user_id";

    public static String KEY_NAME = "name";

    public static String KEY_NOTE = "note";

    public static String KEY_ORGANIZATION = "organization";

    public static String KEY_WEIBO_URLS = "urls";

    public static String KEY_USER_LINK = "user_link";

    public static String KEY_IN_MY_CONTACT = "in_my_contact";

    public static String KEY_EMAILS = "emails";

    public static String KEY_TELS = "tels";

    public static String KEY_USER_STATUS = "user_status";

    public static String KEY_COMPLETED = "completed";

    public static String KEY_SEND_CARD_COUNT = "send_card_count";

    public static String KEY_REVIEWED = "reviewed";

    public static String KEY_NICKNAME = "nickname";

    public static String KEY_GENDER = "gender";

    public static String KEY_IS_FRIEND = "is_friend";

    public static String KEY_SIGN = "sign";

    public static String KEY_BIRTHDAY = "birthday";

    public static String KEY_IS_HIDE_YEAR = "is_hide_year";

    public static String KEY_LUNAR_BDAY = "lunar_bday";

    public static String KEY_IS_LUNAR = "is_lunar";

    public static String KEY_ANIMAL_SIGN = "animal_sign";

    public static String KEY_ZODIAC = "zodiac";

    public static String KEY_BLOOD = "blood";

    public static String KEY_RESIDENCE = "residence";

    public static String KEY_BIRTHPLACE = "birthplace";

    public static String KEY_DESCRIPTION = "description";

    public static String KEY_COMPANY = "company";

    public static String KEY_DEPARTMENT = "department";

    public static String KEY_TITLE = "title";

    public static String KEY_FRIENDS_COUNT = "friends_count";

    public static String KEY_CONMFRI_COUNT = "commfri_count";

    public static String KEY_AVATAR = "avatar";

    public static String KEY_CREATED_AT = "created_at";

    public static String KEY_ITEM_TYPE = "type";

    public static String KEY_ITEM_VALUE = "value";

    public static String KEY_PHONE_ITEM_PREF = "pref";

    public static String TABLE_CARD = "card";

    public static String TABLE_CARD_USER = "card_user";

    public static String KEY_CARD_MOBILE = "mobile";

    public static String KEY_CARD_USER_ID = "user_id";

    public static String KEY_CARD_CONTENT = "content";

    public static String KEY_CARD_VALIDITY = "validity";

    public static String KEY_CARD_MOBILE_VALID = "mobile_valid";

    public static String KEY_CARD_MOBILE_ERROR = "error";

    public static String MOBILE_ILLEGAL_DEFAULT = "{'error':'手机号码格式不对'}";

    private static final long ILLEGAL_USER_ID = -1;

    volatile boolean isUpdateUserId = false;

    volatile boolean isUpdateUserCard = false;

    private Handler mHandler;

    public static final int MSG_UPDATE_CARD_CACHE_FINISH = 1986;

    private Map<String, String> avatarUrlCache = new ConcurrentHashMap<String, String>();

    private CardManager() {
    }

    public static CardManager getInstance() {
        if (null == instance) {
            instance = new CardManager();
        }
        return instance;
    }

    public boolean isUpdateUserCard() {
        return isUpdateUserCard;
    }

    public Map<String, String> getCardCache() {
        return this.avatarUrlCache;
    }

    public void clearCardCache() {
        this.avatarUrlCache.clear();
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public void sendMsg(int what) {
        if (null == mHandler)
            return;
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * 获取名片
     * 
     * @param uid
     * @return
     */
    public Map<String, Object> retrieveUserCard(long uid) {
        Map<String, Object> userCardMap = new HashMap<String, Object>();

        try {
            Contact contact = MoMoHttpApi.getUserCardByID(uid);
            userCardMap.put(KEY_RESPONSE_STATUS, HttpStatus.SC_OK);
            userCardMap.put(KEY_DATA, contact);
            try {
                // 更新本地数据库
                updateCardInfo2Database(uid, new ContactParser().toJSONObject(contact).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (MoMoException e) {
            userCardMap.put(KEY_RESPONSE_STATUS, e.getCode());
            userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, e.getSimpleMsg());
        }

        // StringBuilder params = new
        // StringBuilder(RequestUrl.RETRIEVE_USER_CARD_URL)
        // .append(uid).append(".json");
        // HttpToolkit httpTool = new HttpToolkit(params.toString());
        // int responseCode = httpTool.DoGet();
        // String response = httpTool.GetResponse();
        // Log.d(TAG, "code:" + responseCode + " response:" + response);
        // userCardMap.put(KEY_RESPONSE_STATUS, responseCode);
        // if (HttpToolkit.SERVER_SUCCESS == responseCode) {
        // Contact contact = new Contact();
        // try {
        // contact.setUid(uid);
        // JSONObject jRet = new JSONObject(response);
        // contact.setName(jRet.optString(KEY_NAME));
        // contact.setGender(jRet.optInt(KEY_GENDER));
        // contact.setAnimalSign(jRet.optString(KEY_ANIMAL_SIGN));
        // contact.setZodiac(jRet.optString(KEY_ZODIAC));
        // contact.setResidence(jRet.optString(KEY_RESIDENCE));
        // contact.setNote(jRet.optString(KEY_NOTE));
        // contact.setOrganization(jRet.optString(KEY_ORGANIZATION));
        // String strAvatar = jRet.optString(KEY_AVATAR);
        // Avatar avatar = new Avatar(0, strAvatar, null);
        // contact.setAvatar(avatar);
        // JSONArray weiboArray = jRet.optJSONArray(KEY_WEIBO_URLS);
        // for (int i = 0; i < weiboArray.length(); ++i) {
        // JSONObject object = weiboArray.optJSONObject(i);
        // String weiboType = object.optString(KEY_ITEM_TYPE);
        // String weiboValue = object.optString(KEY_ITEM_VALUE);
        // contact.getWebsiteLabelList().add(weiboType);
        // contact.getWebsiteList().add(weiboValue);
        // }
        // contact.setUserLink(jRet.optInt(KEY_USER_LINK));
        // contact.setInMyContacts(jRet.optBoolean(KEY_IN_MY_CONTACT));
        // if (!jRet.isNull(KEY_EMAILS)) {
        // JSONArray emailsArray = jRet.optJSONArray(KEY_EMAILS);
        // for (int i = 0; i < emailsArray.length(); ++i) {
        // JSONObject email = emailsArray.optJSONObject(i);
        // String emailLabel = email.optString(KEY_ITEM_TYPE);
        // String emailValue = email.optString(KEY_ITEM_VALUE);
        // contact.getEmailLabelList().add(emailLabel);
        // contact.getEmailList().add(emailValue);
        // }
        // }
        // if (!jRet.isNull(KEY_TELS)) {
        // JSONArray telsArray = jRet.optJSONArray(KEY_TELS);
        // for (int i = 0; i < telsArray.length(); ++i) {
        // JSONObject tel = telsArray.optJSONObject(i);
        // String teLabel = tel.optString(KEY_ITEM_TYPE);
        // String telValue = tel.optString(KEY_ITEM_VALUE);
        // boolean telPref = tel.optBoolean(KEY_PHONE_ITEM_PREF);
        // if (telPref) {
        // contact.getPhoneLabelList().add(0, teLabel);
        // contact.getPhoneList().add(0, telValue);
        // contact.getPrefPhoneList().add(0, telPref);
        // } else {
        // contact.getPhoneLabelList().add(teLabel);
        // contact.getPhoneList().add(telValue);
        // contact.getPrefPhoneList().add(telPref);
        // }
        // }
        // }
        // if (!jRet.isNull(KEY_BIRTHDAY)) {
        // String birthday = jRet.optString(KEY_BIRTHDAY);
        // if (null != birthday && birthday.length() > 0) {
        // contact.setBirthday(birthday);
        // }
        // }
        // if (!jRet.isNull(KEY_IS_HIDE_YEAR)) {
        // contact.setIsHideYear(jRet.optBoolean(KEY_IS_HIDE_YEAR));
        // }
        // if (!jRet.isNull(KEY_LUNAR_BDAY)) {
        // contact.setLunarBirthDay(jRet.optString(KEY_LUNAR_BDAY));
        // }
        // if (!jRet.isNull(KEY_IS_LUNAR)) {
        // contact.setNeedLunarBirthDay(jRet.optBoolean(KEY_IS_LUNAR));
        // }
        // if (!jRet.isNull(KEY_USER_STATUS)) {
        // contact.setUserStatus(jRet.optInt(KEY_USER_STATUS));
        // }
        // if (!jRet.isNull(KEY_COMPLETED)) {
        // contact.setCompleted(jRet.optInt(KEY_COMPLETED));
        // }
        // if (!jRet.isNull(KEY_SEND_CARD_COUNT)) {
        // contact.setSendCardCount(jRet.optInt(KEY_SEND_CARD_COUNT));
        // }
        // } catch (Exception e) {
        // Log.e(TAG, e.toString());
        // } finally {
        // userCardMap.put(KEY_DATA, contact);
        // updateCardInfo2Database(uid, response);
        // }
        // }
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, httpTool.GetResponse());
        return userCardMap;
    }

    /**
     * 修改名片
     * 
     * @param contact
     * @return
     */
    public Map<String, Object> updateUserCard(Contact contact) {
        Map<String, Object> userCardMap = new HashMap<String, Object>();
        if (contact == null) {
            userCardMap.put(KEY_RESPONSE_STATUS, 0);
            userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
            return userCardMap;
        }

        try {
            MoMoHttpApi.updateUserCard(contact);
            userCardMap.put(KEY_RESPONSE_STATUS, HttpStatus.SC_OK);
            userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
            userCardMap.put(KEY_DATA, contact);
        } catch (MoMoException e) {
            Log.e(TAG, "updateUserCard error:" + e.toString());
            userCardMap.put(KEY_RESPONSE_STATUS, e.getCode());
            userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, e.getSimpleMsg());
        }

        return userCardMap;
        // String url = RequestUrl.UPDATE_USER_CARD_URL;
        // HttpToolkit httpTool = new HttpToolkit(url);
        // JSONObject queryParams = new JSONObject();
        // try {
        // queryParams.put(KEY_NAME, contact.getName());
        // queryParams.put(KEY_GENDER, contact.getGender());
        // queryParams.put(KEY_BIRTHDAY, contact.getBirthday());
        // queryParams.put(KEY_IS_HIDE_YEAR, contact.isHideYear());
        // queryParams.put(KEY_IS_LUNAR, contact.isNeedLunarBirthDay());
        // queryParams.put(KEY_RESIDENCE, contact.getResidence());
        // queryParams.put(KEY_ORGANIZATION, contact.getOrganization());
        // queryParams.put(KEY_NOTE, contact.getNote());
        // JSONArray emailsArray = new JSONArray();
        // List<String> emailList = contact.getEmailList();
        // List<String> emailLabelList = contact.getEmailLabelList();
        // if (emailList.size() != emailLabelList.size()) {
        // Log.e(TAG, "emailList.size() != emailLabelList.size()");
        // userCardMap.put(KEY_RESPONSE_STATUS, 0);
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
        // return userCardMap;
        // }
        // for (int i = 0; i < emailList.size(); i++) {
        // JSONObject obj = new JSONObject();
        // obj.put(KEY_ITEM_TYPE, emailLabelList.get(i));
        // obj.put(KEY_ITEM_VALUE, emailList.get(i));
        // emailsArray.put(obj);
        // }
        // queryParams.put(KEY_EMAILS, emailsArray);
        // JSONArray telsArray = new JSONArray();
        // List<String> phoneList = contact.getPhoneList();
        // List<String> phoneLabelList = contact.getPhoneLabelList();
        // List<Boolean> prefPhoneList = contact.getPrefPhoneList();
        // if (phoneList.size() != phoneLabelList.size()
        // || phoneList.size() != prefPhoneList.size()) {
        // Log.e(TAG, "phoneList.size() != phoneLabelList).size()");
        // userCardMap.put(KEY_RESPONSE_STATUS, 0);
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
        // return userCardMap;
        // }
        // for (int i = 0; i < phoneList.size(); i++) {
        // if (!prefPhoneList.get(i)) {
        // JSONObject obj = new JSONObject();
        // obj.put(KEY_ITEM_TYPE, phoneLabelList.get(i));
        // obj.put(KEY_ITEM_VALUE, phoneList.get(i));
        // telsArray.put(obj);
        // }
        // }
        // queryParams.put(KEY_TELS, telsArray);
        //
        // Log.d(TAG, "update data:" + queryParams.toString());
        // int responseCode = httpTool.DoPost(queryParams);
        // Log.d(TAG, "update result:" + " responseCode = " + responseCode +
        // " response = "
        // + httpTool.GetResponse());
        // userCardMap.put(KEY_RESPONSE_STATUS, responseCode);
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, httpTool.GetResponse());
        // userCardMap.put(KEY_DATA, contact);
        // return userCardMap;
        //
        // } catch (JSONException e) {
        // Log.e(TAG, "updateUserCard error:" + e.toString());
        // userCardMap.put(KEY_RESPONSE_STATUS, 0);
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
        // return userCardMap;
        // }
    }

    public void updateUserIdListCache(Context context, boolean forceUpdate) {
        Log.d(TAG, "updateUserIdListCache");
        if (null == Utils.getActiveNetWorkName(Utils.getContext())) {
            Log.d(TAG, "没有网络链接");
            return;
        }
        if (isUpdateUserId) {
            return;
        }
        ConfigHelper configHelper = ConfigHelper.getInstance(Utils.getContext());
        configHelper.setContext(context);
        long nowTime = System.currentTimeMillis();
        long lastTime = configHelper.loadLongKey(ConfigHelper.LAST_TIME_UPDATE_USER_ID, 0);
        Log.d(TAG, "nowTime:" + nowTime + " lastTime:" + lastTime + " config time:"
                + GlobalUserInfo.USER_CACHE_TIME);
        if (!forceUpdate && lastTime + GlobalUserInfo.USER_CACHE_TIME > nowTime) {
            return;
        }
        String zoneCode = GlobalUserInfo.getCurrentZoneCode(context);
        IgnoreMobilePrefix ignoreObject = CardManager.getInstance().getIgnoreObject(context);
        isUpdateUserId = true;
        Map<String, User> userMap = new HashMap<String, User>();
        JSONArray listJsonArray = new JSONArray();
        JSONArray jsonArray = new JSONArray();
        List<Contact> contactList = MoMoContactsManager.getInstance().getAllContactsWithMobile();
        for (Contact contact : contactList) {
            for (String mobile : contact.getPhoneList()) {
                mobile = ignoreObject.ignore(zoneCode, mobile);
                if (mobile != null && mobile.length() > 0 && !userMap.containsKey(mobile)) {
                    User user = new User();
                    user.setMobile(mobile);
                    user.setName(contact.getFormatName());
                    userMap.put(mobile, user);
                    JSONObject object = new JSONObject();
                    try {
                        object.put("name", user.getName());
                        object.put("mobile", mobile);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    jsonArray.put(object);
                    if (jsonArray.length() == 100) {
                        listJsonArray.put(jsonArray);
                        jsonArray = new JSONArray();
                    }
                }
            }
        }
        if (jsonArray.length() > 0) {
            listJsonArray.put(jsonArray);
        }
        contactList.clear();
        contactList = null;
        Map<String, Long> replaceMap = new HashMap<String, Long>();
        for (int i = 0; i < listJsonArray.length(); i++) {
            batchGetCreateAt(userMap, replaceMap, listJsonArray.optJSONArray(i));
        }
        Log.d(TAG, "cache size:" + replaceMap.size());
        // 写缓存入数据库
        batchAddUserIdList(replaceMap);
        configHelper.saveLongKey(ConfigHelper.LAST_TIME_UPDATE_USER_ID, System.currentTimeMillis());
        configHelper.commit();
        isUpdateUserId = false;
    }

    public Collection<User> getUserIdList(Context context, ArrayList<User> pairList,
            boolean onlyCache) {
        Map<String, User> userMap = new HashMap<String, User>();
        if (pairList == null || pairList.size() < 1) {
            return userMap.values();
        }
        Map<String, User> mapList = new HashMap<String, User>();
        Map<String, ArrayList<String>> mobileMap = new HashMap<String, ArrayList<String>>();

        String defaultUserId = "0";
        String separator = "";
        StringBuilder sb = new StringBuilder();
        String zoneCode = GlobalUserInfo.getCurrentZoneCode(context);
        IgnoreMobilePrefix ignoreObject = CardManager.getInstance().getIgnoreObject(context);
        Map<String, String> nameMap = GlobalContactList.getInstance().getContactNameMap();
        for (User pair : pairList) {
            String mobile = pair.getMobile();
            pair.setId(defaultUserId);
            if (!userMap.containsKey(mobile)) {
                userMap.put(mobile, pair);
                String converMobile = ignoreObject.ignore(zoneCode, mobile);
                if (converMobile.length() < 1) {
                    if (nameMap.containsKey(mobile)) {
                        pair.setName(nameMap.get(mobile));
                    }
                    continue;
                }
                if (nameMap.containsKey(converMobile)) {
                    pair.setName(nameMap.get(converMobile));
                }
                mapList.put(mobile, pair);

                if (mobileMap.containsKey(converMobile)) {
                    mobileMap.get(converMobile).add(mobile);
                } else {
                    ArrayList<String> mobileList = new ArrayList<String>();
                    mobileList.add(mobile);
                    mobileMap.put(converMobile, mobileList);
                }
                sb.append(separator).append("'").append(converMobile).append("'");
                separator = ",";
            }
        }
        String selectionString = sb.toString();
        Log.d(TAG, "mobile list:" + selectionString);
        if (selectionString.length() < 1) {
            return userMap.values();
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select user_id, mobile from card_user where mobile in ("
                    + selectionString + ")";
            String[] selectionArgs = null;
            cursor = mDB.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                long userId = cursor.getLong(cursor.getColumnIndex(KEY_CARD_USER_ID));
                String mobile = cursor.getString(cursor.getColumnIndex(KEY_CARD_MOBILE));
                for (String originalMobile : mobileMap.get(mobile)) {
                    userMap.get(originalMobile).setId(String.valueOf(userId));
                }
                Log.d(TAG, "cache mobile:" + mobile + " user_id:" + userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        if (onlyCache) {
            return userMap.values();
        }
        JSONArray listJsonArray = new JSONArray();
        JSONArray jsonArray = new JSONArray();
        for (String mobile : userMap.keySet()) {
            if (userMap.get(mobile).getId().equals(defaultUserId)) {
                User user = mapList.get(mobile);
                JSONObject object = new JSONObject();
                try {
                    object.put("name", user.getName());
                    object.put("mobile", mobile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(object);
                if (jsonArray.length() == 100) {
                    listJsonArray.put(jsonArray);
                    jsonArray = new JSONArray();
                }
            }
        }
        if (jsonArray.length() > 0) {
            listJsonArray.put(jsonArray);
        }
        Map<String, Long> replaceMap = new HashMap<String, Long>();
        for (int i = 0; i < listJsonArray.length(); i++) {
            batchGetCreateAt(userMap, replaceMap, listJsonArray.optJSONArray(i));
        }
        // 写缓存入数据库
        batchAddUserIdList(replaceMap);

        return userMap.values();
    }

    private void batchGetCreateAt(Map<String, User> userMap, Map<String, Long> replaceMap,
            JSONArray jsonArray) {
        if (userMap == null || userMap.size() < 1 || jsonArray == null || jsonArray.length() < 1) {
            return;
        } else if (replaceMap == null) {
            replaceMap = new HashMap<String, Long>();
        }
        try {
            List<User> userList = MoMoHttpApi.getIDList(userMap);
            int size = userList.size();
            for (int i = 0; i < size; i++) {
                String mobile = userList.get(i).getMobile();
                long userId = Long.valueOf(userList.get(i).getId());
                if (!replaceMap.containsKey(mobile)
                        && (userId == Contact.DEFAULT_USER_ID_INVALID || userId > 0)) {
                    replaceMap.put(mobile, userId);
                }
                if (userMap.containsKey(mobile)) {
                    userMap.get(mobile).setId(String.valueOf(userId));
                }
            }
        } catch (MoMoException e) {
            Log.e(TAG, e.getSimpleMsg());
        }
        // Log.d(TAG, "request uid list params:" + jsonArray.toString());
        // String url = RequestUrl.GET_CREATE_AT_LIST;
        // HttpToolkit httpTool = new HttpToolkit(url);
        // int responseCode = httpTool.DoPostArray(jsonArray);
        // String response = httpTool.GetResponse();
        // Log.d(TAG, "responseCode:" + responseCode + " response:" + response);
        // if (responseCode == HttpStatus.SC_OK) {
        // JSONArray jArray;
        // try {
        // jArray = new JSONArray(response);
        // for (int i = 0; i < jArray.length(); i++) {
        // JSONObject object = jArray.optJSONObject(i);
        // long userId = object.optLong(KEY_CARD_USER_ID);
        // String mobile = object.optString(KEY_CARD_MOBILE);
        // String error = object.optString(KEY_CARD_MOBILE_ERROR);
        // if (error.contains("400116")) {
        // userId = Contact.DEFAULT_USER_ID_INVALID;
        // } else if (mobile.length() < 1) {
        // continue;
        // }
        // if (!replaceMap.containsKey(mobile)
        // && (userId == Contact.DEFAULT_USER_ID_INVALID || userId > 0)) {
        // replaceMap.put(mobile, userId);
        // }
        // if (userMap.containsKey(mobile)) {
        // userMap.get(mobile).setId(String.valueOf(userId));
        // }
        //
        // }
        // } catch (JSONException e) {
        // e.printStackTrace();
        // }
        // } else {
        // Log.e(TAG, response);
        // }
    }

    @SuppressWarnings("unchecked")
    public void batchGetCard(Context context) {
        Log.i(TAG, "begin batchGetCard");
        if (isUpdateUserCard) {
            return;
        }
        isUpdateUserCard = true;
        // String syncMode = GlobalUserInfo.getSyncMode(context);
        // if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(syncMode)) {
        //
        // }
        // List<Contact> contactList = new ArrayList<Contact>();
        // if (userList == null || userList.size() < 1) {
        // return contactList;
        // }

        String zoneCode = GlobalUserInfo.getCurrentZoneCode(context);
        IgnoreMobilePrefix ignoreObject = CardManager.getInstance().getIgnoreObject(context);

        HashSet<String> phoneHashSet = LocalContactsManager.getInstance().getAllPhones();
        StringBuilder sb = new StringBuilder();
        String separator = "";
        HashMap<String, Long> map = new HashMap<String, Long>();
        for (String phone : phoneHashSet) {
            phone = ignoreObject.ignore(zoneCode, phone);
            if (phone == null || phone.length() < 1) {
                continue;
            }
            sb.append(separator).append("'").append(phone).append("'");
            map.put(phone, Contact.DEFAULT_USER_ID_NOT_EXIST);
            separator = ",";
        }

        String selectionString = sb.toString();
        Log.d(TAG, "mobile list:" + selectionString);
        if (selectionString.length() > 1) {
            Cursor cursor = null;
            try {
                long nowTime = System.currentTimeMillis();
                long mostValidity = 0;
                // 有网络的情况下，考虑名片有效期
                if (Utils.getActiveNetWorkName(context) != null) {
                    mostValidity = nowTime - GlobalUserInfo.USER_CACHE_TIME;
                    ;
                }
                mDB = MyDatabaseHelper.getInstance();
                String sql = "select card.user_id, mobile, validity, content from card_user left outer join card on card_user.user_id = card.user_id where (card.user_id = -1  or (card.user_id > 0 and card.validity > "
                        + mostValidity + ")) and mobile in (" + selectionString + ")";
                String[] selectionArgs = null;
                cursor = mDB.rawQuery(sql, selectionArgs);
                while (cursor.moveToNext()) {
                    long userId = cursor.getLong(cursor.getColumnIndex(KEY_CARD_USER_ID));
                    long validity = cursor.getLong(cursor.getColumnIndex(KEY_CARD_VALIDITY));
                    String mobile = cursor.getString(cursor.getColumnIndex(KEY_CARD_MOBILE));
                    if (userId < 0) {
                        map.put(mobile, Contact.DEFAULT_USER_ID_INVALID);
                    } else if (validity > mostValidity) {
                        String content = cursor.getString(cursor.getColumnIndex(KEY_CARD_CONTENT));
                        String avatarUrl = parseAvatarUrlFromJsonString(content);
                        if (avatarUrl != null) {
                            avatarUrlCache.put(mobile, avatarUrl);
                        }
                        map.put(mobile, userId);
                    }
                    Log.d(TAG, "cache mobile:" + mobile);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != cursor && !cursor.isClosed()) {
                    cursor.close();
                }
            }
        }

        if (Utils.getActiveNetWorkName(context) != null) {
            List<String> mobileList = new ArrayList<String>();
            for (String mobile : map.keySet()) {
                if (map.get(mobile) == Contact.DEFAULT_USER_ID_NOT_EXIST) {
                    mobileList.add(mobile);
                }
            }
            try {
                List<Contact> result = MoMoHttpApi.getUserCardListByMobile(mobileList);
                int size = result.size();
                for (int i = 0; i < size; i++) {
                    Contact contact = result.get(i);
                    String mobile = contact.getMainPhone();
                    long userId = contact.getUid();
                    map.put(mobile, userId);
                    try {
                        if (userId == 0) {
                            replaceCardInfo2Database(mobile, ILLEGAL_USER_ID, new ContactParser()
                                    .toJSONObject(contact).toString());
                        } else {
                            replaceCardInfo2Database(mobile, userId, new ContactParser()
                                    .toJSONObject(contact).toString());
                            String avatarUrl = contact.getAvatar().getServerAvatarURL();
                            if (avatarUrl != null) {
                                avatarUrlCache.put(mobile, avatarUrl);
                            }
                        }
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } catch (MoMoException e) {
                Log.d(TAG, "get card error:" + e.getSimpleMsg());
            }
            // JSONArray mobileBatchArray = new JSONArray();
            // JSONArray mobileArray = new JSONArray();
            // for (String mobile : map.keySet()) {
            // if (map.get(mobile) == Contact.DEFAULT_USER_ID_NOT_EXIST) {
            // mobileArray.put(mobile);
            // }
            // if (mobileArray.length() == 100) {
            // mobileBatchArray.put(mobileArray);
            // mobileArray = new JSONArray();
            // }
            // }
            // if (mobileArray.length() > 0) {
            // mobileBatchArray.put(mobileArray);
            // }
            // for (int i = 0; i < mobileBatchArray.length(); i++) {
            // String url = RequestUrl.BATCH_GET_CARD_LIST;
            // HttpToolkit httpTool = new HttpToolkit(url);
            // Log.i(TAG, "url:" + url);
            // Log.i(TAG, "params:" +
            // mobileBatchArray.optJSONArray(i).toString());
            // int responseCode =
            // httpTool.DoPostArray(mobileBatchArray.optJSONArray(i));
            // String response = httpTool.GetResponse();
            // Log.d(TAG, "responseCode:" + responseCode + " response:" +
            // response);
            // if (responseCode == HttpStatus.SC_OK) {
            // try {
            // JSONObject jsonObject = new JSONObject(response);
            // Iterator keyIter = jsonObject.keys();
            // while (keyIter.hasNext()) {
            // String mobile = keyIter.next().toString();
            // JSONObject object = jsonObject.optJSONObject(mobile);
            // long userId = object.optLong(KEY_CARD_USER_ID);
            // map.put(mobile, userId);
            // if (userId == 0) {
            // replaceCardInfo2Database(mobile, ILLEGAL_USER_ID,
            // object.toString());
            // } else {
            // replaceCardInfo2Database(mobile, userId, object.toString());
            // String avatarUrl = parseAvatarUrlFromJson(object);
            // if (avatarUrl != null) {
            // avatarUrlCache.put(mobile, avatarUrl);
            // }
            // }
            // }
            // } catch (JSONException e) {
            // e.printStackTrace();
            // }
            // } else {
            // Log.d(TAG, "get card error:" + response);
            // }
            // }
        }

        map.clear();
        map = null;
        Log.i(TAG, "end batchGetCard");
        sendMsg(MSG_UPDATE_CARD_CACHE_FINISH);
        isUpdateUserCard = false;
    }

    public Map<String, Object> getUserCardByMobile(Context context, String name, String mobile) {
        Map<String, Object> userCardMap = new HashMap<String, Object>();
        mobile = GlobalUserInfo.getCutMobile(context, mobile);
        if (mobile == null || mobile.length() < 1) {
            userCardMap.put(KEY_RESPONSE_STATUS, 0);
            userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
            return userCardMap;
        }

        try {
            Contact contact = MoMoHttpApi.getUserCardByMobile(name, mobile);
            userCardMap.put(KEY_RESPONSE_STATUS, HttpStatus.SC_OK);
            userCardMap.put(KEY_RESPONSE_CONTENT, contact);
            if (contact != null && contact.getUid() > 0) {
                try {
                    replaceCardInfo2Database(mobile, contact.getUid(), new ContactParser()
                            .toJSONObject(contact).toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (MoMoException e) {
            userCardMap.put(KEY_RESPONSE_STATUS, e.getCode());
            userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, e.getSimpleMsg());
        }
        // String url = RequestUrl.RETRIEVE_USER_CARD_BY_MOBILE_URL;
        // JSONObject postObject = new JSONObject();
        // try {
        // postObject.put("name", name);
        // postObject.put("mobile", mobile);
        // } catch (JSONException e) {
        // e.printStackTrace();
        // userCardMap.put(KEY_RESPONSE_STATUS, 0);
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, "");
        // return userCardMap;
        // }
        // Log.d(TAG, "post:" + postObject.toString());
        // HttpToolkit httpTool = new HttpToolkit(url);
        // int responseCode = httpTool.DoPost(postObject);
        // String response = httpTool.GetResponse();
        // Log.d(TAG, "code:" + responseCode + " response:" + response);
        // userCardMap.put(KEY_RESPONSE_STATUS, responseCode);
        // String excpetionString = "";
        //
        // if (HttpToolkit.SERVER_SUCCESS == responseCode) {
        // Contact contact = parseContactFromJsonString(response);
        // userCardMap.put(KEY_RESPONSE_CONTENT, contact);
        // if (contact != null && contact.getUid() > 0) {
        // replaceCardInfo2Database(mobile, contact.getUid(), response);
        // }
        // } else {
        // excpetionString = response;
        // if (responseCode == 400) {
        // replaceCardInfo2Database(mobile, ILLEGAL_USER_ID, response);
        // }
        // }
        // userCardMap.put(KEY_RESPONSE_EXCPETION_TEXT, excpetionString);
        return userCardMap;
    }

    private Contact parseContactFromJsonString(String response) {
        if (null == response || response.length() == 0) {
            return null;
        }
        Log.d(TAG, response);
        Contact contact = new Contact();
        try {
            JSONObject jRet = new JSONObject(response);
            contact.setUid(jRet.optLong(KEY_USER_ID));
            contact.setName(jRet.optString(KEY_NAME));
            contact.setGender(jRet.optInt(KEY_GENDER));
            contact.setAnimalSign(jRet.optString(KEY_ANIMAL_SIGN));
            contact.setZodiac(jRet.optString(KEY_ZODIAC));
            contact.setResidence(jRet.optString(KEY_RESIDENCE));
            contact.setNote(jRet.optString(KEY_NOTE));
            contact.setOrganization(jRet.optString(KEY_ORGANIZATION));
            String strAvatar = jRet.optString(KEY_AVATAR);
            Avatar avatar = new Avatar(0, strAvatar, null);
            contact.setAvatar(avatar);
            JSONArray weiboArray = jRet.optJSONArray(KEY_WEIBO_URLS);
            for (int i = 0; i < weiboArray.length(); ++i) {
                JSONObject object = weiboArray.optJSONObject(i);
                String weiboType = object.optString(KEY_ITEM_TYPE);
                String weiboValue = object.optString(KEY_ITEM_VALUE);
                contact.getWebsiteLabelList().add(weiboType);
                contact.getWebsiteList().add(weiboValue);
                Weibo weibo = new Weibo(weiboType, weiboValue);
                contact.getWeiboList().add(weibo);
            }
            contact.setUserLink(jRet.optInt(KEY_USER_LINK));
            contact.setInMyContacts(jRet.optBoolean(KEY_IN_MY_CONTACT));
            if (!jRet.isNull(KEY_EMAILS)) {
                JSONArray emailsArray = jRet.optJSONArray(KEY_EMAILS);
                for (int i = 0; i < emailsArray.length(); ++i) {
                    JSONObject email = emailsArray.optJSONObject(i);
                    String emailLabel = email.optString(KEY_ITEM_TYPE);
                    String emailValue = email.optString(KEY_ITEM_VALUE);
                    contact.getEmailLabelList().add(emailLabel);
                    contact.getEmailList().add(emailValue);
                }
            }
            if (!jRet.isNull(KEY_TELS)) {
                JSONArray telsArray = jRet.optJSONArray(KEY_TELS);
                for (int i = 0; i < telsArray.length(); ++i) {
                    JSONObject tel = telsArray.optJSONObject(i);
                    String teLabel = tel.optString(KEY_ITEM_TYPE);
                    String telValue = tel.optString(KEY_ITEM_VALUE);
                    boolean telPref = tel.optBoolean(KEY_PHONE_ITEM_PREF);
                    if (telPref) {
                        contact.getPhoneLabelList().add(0, teLabel);
                        contact.getPhoneList().add(0, telValue);
                        contact.setPrimePhoneNumber(telValue);
                        contact.getPrefPhoneList().add(0, telPref);
                    } else {
                        contact.getPhoneLabelList().add(teLabel);
                        contact.getPhoneList().add(telValue);
                        contact.getPrefPhoneList().add(telPref);
                    }
                }
            }
            if (!jRet.isNull(KEY_BIRTHDAY)) {
                String birthday = jRet.optString(KEY_BIRTHDAY);
                if (null != birthday && birthday.length() > 0) {
                    contact.setBirthday(birthday);
                }
            }
            if (!jRet.isNull(KEY_IS_HIDE_YEAR)) {
                contact.setIsHideYear(jRet.optBoolean(KEY_IS_HIDE_YEAR));
            }
            if (!jRet.isNull(KEY_LUNAR_BDAY)) {
                contact.setLunarBirthDay(jRet.optString(KEY_LUNAR_BDAY));
            }
            if (!jRet.isNull(KEY_IS_LUNAR)) {
                contact.setNeedLunarBirthDay(jRet.optBoolean(KEY_IS_LUNAR));
            }
            if (!jRet.isNull(KEY_USER_STATUS)) {
                contact.setUserStatus(jRet.optInt(KEY_USER_STATUS));
            }
            if (!jRet.isNull(KEY_COMPLETED)) {
                contact.setCompleted(jRet.optInt(KEY_COMPLETED));
            }
            if (!jRet.isNull(KEY_SEND_CARD_COUNT)) {
                contact.setSendCardCount(jRet.optInt(KEY_SEND_CARD_COUNT));
            }
        } catch (Exception e) {
            e.printStackTrace();
            contact = null;
        } finally {

        }
        return contact;
    }

    private String parseAvatarUrlFromJsonString(String response) {
        String avatarUrl = null;
        if (null == response || response.length() == 0) {
            return avatarUrl;
        }
        Log.d(TAG, response);
        try {
            JSONObject jRet = new JSONObject(response);
            int userStatus = jRet.optInt(KEY_USER_STATUS);
            if (userStatus == 3) {
                avatarUrl = jRet.optString(KEY_AVATAR);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return avatarUrl;
    }

    //
    // private String parseAvatarUrlFromJson(JSONObject json) {
    // String avatarurl = null;
    // if (null == json) {
    // return avatarurl;
    // }
    // Log.d(TAG, json.toString());
    // try {
    // int userStatus = json.optInt(KEY_USER_STATUS);
    // if (userStatus == 3) {
    // avatarurl = json.optString(KEY_AVATAR);
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // } finally {
    // }
    // return avatarurl;
    // }

    private boolean batchAddUserIdList(Map<String, Long> userList) {
        if (userList == null || userList.size() < 1) {
            return false;
        }
        Cursor cursor = null;
        try {
            boolean illegalUser = false;
            mDB = MyDatabaseHelper.getInstance();
            mDB.beginTransaction();
            for (String mobile : userList.keySet()) {
                ContentValues values = new ContentValues();
                long userId = userList.get(mobile);
                if (userId == ILLEGAL_USER_ID) {
                    illegalUser = true;
                }
                values.put(KEY_CARD_USER_ID, userId);
                values.put(KEY_CARD_MOBILE, mobile);
                String[] whereArgs = new String[] {
                        mobile
                };
                mDB.delete(TABLE_CARD_USER, "mobile = ?", whereArgs);
                whereArgs = new String[] {
                        mobile,
                        String.valueOf(userId)
                };
                mDB.insert(TABLE_CARD_USER, null, values);
            }
            if (illegalUser) {
                ContentValues values = new ContentValues();
                values.put(KEY_CARD_USER_ID, ILLEGAL_USER_ID);
                values.put(KEY_CARD_CONTENT, MOBILE_ILLEGAL_DEFAULT);
                values.put(KEY_CARD_VALIDITY, System.currentTimeMillis());
                mDB.delete(TABLE_CARD, "user_id = ?", new String[] {
                        String.valueOf(ILLEGAL_USER_ID)
                });
                mDB.insert(TABLE_CARD, null, values);
            }
            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            if (mDB.inTransaction()) {
                mDB.endTransaction();
            }
            return false;
        } finally {
            if (mDB.inTransaction()) {
                mDB.endTransaction();
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return true;
    }

    private boolean replaceCardInfo2Database(String mobile, long userId, String content) {
        if (mobile == null || mobile.length() < 1) {
            return false;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            mDB.beginTransaction();
            ContentValues values = new ContentValues();
            values.put(KEY_CARD_USER_ID, userId);
            values.put(KEY_CARD_CONTENT, content);
            values.put(KEY_CARD_VALIDITY, System.currentTimeMillis());
            if (userId > 0) {
                mDB.replace(TABLE_CARD, null, values);
            } else if (userId == ILLEGAL_USER_ID) {
                String sql = "select user_id from card_user where user_id = -1";
                cursor = mDB.rawQuery(sql, null);
                if (!cursor.moveToNext()) {
                    values.clear();
                    values.put(KEY_CARD_USER_ID, userId);
                    values.put(KEY_CARD_CONTENT, MOBILE_ILLEGAL_DEFAULT);
                    values.put(KEY_CARD_VALIDITY, System.currentTimeMillis());
                    mDB.insert(TABLE_CARD, null, values);
                }
            }
            String[] whereArgs = new String[] {
                    mobile
            };
            mDB.delete(TABLE_CARD_USER, "mobile = ?", whereArgs);
            values.clear();
            values.put(KEY_CARD_MOBILE, mobile);
            values.put(KEY_CARD_USER_ID, userId);
            mDB.insert(TABLE_CARD_USER, null, values);

            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            if (mDB.inTransaction()) {
                mDB.endTransaction();
            }
            return false;
        } finally {
            if (mDB.inTransaction()) {
                mDB.endTransaction();
            }
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return true;
    }

    private boolean updateCardInfo2Database(long userId, String content) {
        if (userId < 1 || content == null || content.length() < 1) {
            return false;
        }
        try {
            mDB = MyDatabaseHelper.getInstance();
            ContentValues values = new ContentValues();
            values.put(KEY_CARD_USER_ID, userId);
            values.put(KEY_CARD_CONTENT, content);
            values.put(KEY_CARD_VALIDITY, System.currentTimeMillis());
            mDB.replace(TABLE_CARD, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public Contact getCardFromDababase(String mobile) {
        Contact contact = null;
        if (mobile == null || mobile.length() < 1) {
            return contact;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select card.user_id, card.content, card.validity from card_user left join card on card_user.user_id = card.user_id where card_user.mobile = ?";
            String[] selectionArgs = {
                    mobile
            };
            cursor = mDB.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                long userId = cursor.getLong(cursor.getColumnIndex(KEY_CARD_USER_ID));
                if (userId == ILLEGAL_USER_ID) {
                    contact = new Contact();
                    contact.setUid(userId);
                } else {
                    String content = cursor.getString(cursor.getColumnIndex(KEY_CARD_CONTENT));
                    contact = parseContactFromJsonString(content);
                }
                long validity = cursor.getLong(cursor.getColumnIndex(KEY_CARD_VALIDITY));
                contact.setModifyDate(validity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return contact;
    }

    public Contact getCardFromDababaseByUserId(long userId) {
        Contact contact = null;
        if (userId < 1) {
            return contact;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select validity, content from card where user_id = ?";
            String[] selectionArgs = {
                    String.valueOf(userId)
            };
            cursor = mDB.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                String content = cursor.getString(cursor.getColumnIndex(KEY_CARD_CONTENT));
                long validity = cursor.getLong(cursor.getColumnIndex(KEY_CARD_VALIDITY));
                contact = parseContactFromJsonString(content);
                if (contact != null) {
                    contact.setModifyDate(validity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return contact;
    }

    /**
     * 批量根据用户id从数据库缓存中获取手机号码
     * 
     * @param userId
     * @return
     */
    public void getMobileListFromDababase(List<User> userList) {
        if (userList == null || userList.size() < 1) {
            return;
        }
        String separator = "";
        StringBuilder sb = new StringBuilder();
        HashMap<Long, ArrayList<User>> userMap = new HashMap<Long, ArrayList<User>>();
        for (User user : userList) {
            long userId = 0;
            try {
                userId = Long.valueOf(user.getId());
            } catch (NumberFormatException e) {
                e.printStackTrace();
                continue;
            }
            if (userId > 0) {
                if (!userMap.containsKey(userId)) {
                    ArrayList<User> ul = new ArrayList<User>();
                    ul.add(user);
                    userMap.put(userId, ul);
                } else {
                    userMap.get(userId).add(user);
                }
                sb.append(separator).append(userId);
                separator = ",";
            }
        }
        String selectionString = sb.toString();
        Log.d(TAG, "mobile list:" + selectionString);
        if (selectionString.length() < 1) {
            return;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select mobile, user_id from card_user where user_id in("
                    + selectionString + ")";
            String[] selectionArgs = null;
            cursor = mDB.rawQuery(sql, selectionArgs);
            while (cursor.moveToNext()) {
                String mobile = cursor.getString(cursor.getColumnIndex(KEY_CARD_MOBILE));
                long userId = cursor.getLong(cursor.getColumnIndex(KEY_CARD_USER_ID));
                for (User user : userMap.get(userId)) {
                    user.setMobile(mobile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

    /**
     * 根据用户id从数据库缓存中获取手机号码
     * 
     * @param userId
     * @return
     */
    public String getMobileFromDababaseByUserId(long userId) {
        String mobile = "";
        if (userId < 1) {
            return mobile;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select mobile from card_user where user_id = ?";
            String[] selectionArgs = {
                    String.valueOf(userId)
            };
            cursor = mDB.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                mobile = cursor.getString(cursor.getColumnIndex(KEY_CARD_MOBILE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return mobile;
    }

    public static boolean checkChinaMobileValid(String originalMobile, String convertMobile) {
        if (originalMobile == null) {
            return false;
        }
        Pattern pattern = Pattern.compile("^[^\\+]\\S+|^\\+86\\d+");
        Matcher matcher = pattern.matcher(originalMobile);
        if (matcher.find()) {
            if (convertMobile.length() != 11) {
                return false;
            } else {
                return true;
            }
        } else if (convertMobile.length() >= 7 && convertMobile.length() <= 14) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkInternationalMobileValid(String originalMobile, String convertMobile) {
        if (originalMobile == null) {
            return false;
        }
        if (originalMobile.length() >= 4) {
            return true;
        } else {
            return false;
        }
    }

    public static String ignoreChinaMobilePrefix(String mobile) {
        if (mobile == null) {
            return "";
        } else if (mobile.length() < 8) {
            return mobile;
        }
        Pattern pattern = Pattern.compile("^12520\\d{14}");
        Matcher matcher = pattern.matcher(mobile);
        if (matcher.find()) {
            return mobile.substring(8);
        }
        pattern = Pattern.compile("^\\+86|^0086|^086|^\\(86\\)|^86");
        matcher = pattern.matcher(mobile);
        mobile = matcher.replaceFirst("");
        pattern = Pattern
                .compile("^12593|^17951|^17911|^17910|^17909|^10131|^10193|^96531|^193|^12520|^11808|^17950");
        matcher = pattern.matcher(mobile);
        mobile = matcher.replaceFirst("");
        return mobile;
    }

    public static String ignoreInternationalMobilePrefix(String zoneCode, String mobile) {
        String oldMobile = mobile;
        if (mobile == null || zoneCode == null) {
            return "";
        } else if (mobile.length() < 8 || zoneCode.length() < 1) {
            return mobile;
        }
        Pattern pattern = Pattern.compile("^\\+" + zoneCode);
        Matcher matcher = pattern.matcher(mobile);
        mobile = matcher.replaceFirst("");
        Log.d(TAG, "oldMobile:" + oldMobile + " newMobile:" + mobile);
        return mobile;
    }

    public boolean deleteAllUserCache() {
        try {
            mDB = MyDatabaseHelper.getInstance();
            mDB.beginTransaction();
            mDB.delete(TABLE_CARD, null, null);
            mDB.delete(TABLE_CARD_USER, null, null);
            mDB.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            mDB.endTransaction();
        }
        return true;

    }

    public IgnoreMobilePrefix getIgnoreObject(Context context) {
        String zoneCode = GlobalUserInfo.getCurrentZoneCode(Utils.getContext());
        IgnoreMobilePrefix ignoreMobilePrefix = null;
        if (zoneCode.equals(GlobalUserInfo.DEFAULT_ZONE_CODE)) {
            ignoreMobilePrefix = new IgnoreMobilePrefix() {
                @Override
                public String ignore(String zoneCode, String mobile) {
                    return CardManager.ignoreChinaMobilePrefix(mobile);
                }

                @Override
                public boolean vaild(String zoneCode, String mobile) {
                    String convertMobile = ignore(zoneCode, mobile);
                    return CardManager.checkChinaMobileValid(mobile, convertMobile);
                }
            };
        } else {
            ignoreMobilePrefix = new IgnoreMobilePrefix() {
                @Override
                public String ignore(String zoneCode, String mobile) {
                    return CardManager.ignoreInternationalMobilePrefix(zoneCode, mobile);
                }

                @Override
                public boolean vaild(String zoneCode, String mobile) {
                    String convertMobile = ignore(zoneCode, mobile);
                    return CardManager.checkInternationalMobileValid(mobile, convertMobile);
                }
            };
        }
        return ignoreMobilePrefix;
    }

    public interface IgnoreMobilePrefix {
        String ignore(String zoneCode, String mobile);

        boolean vaild(String zoneCode, String mobile);
    }

}

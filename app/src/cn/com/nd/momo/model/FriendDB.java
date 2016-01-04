package cn.com.nd.momo.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.util.Utils;

public class FriendDB {

    private static FriendDB mInstance = null;
    public static synchronized  FriendDB instance() {
        if (mInstance == null) {
            mInstance = new FriendDB();
        }
        return mInstance;
    }

    public void setFriends(ArrayList<User> friends) {
        try {
            JSONArray array = new JSONArray();
            for (User u : friends) {
                JSONObject obj = new JSONObject();
                obj.put("uid", u.getId());
                obj.put("name", u.getName());
                array.put(obj);
            }
            String s = array.toString();
            //获取已经创建的ConfigHelper实例,不需要context参数
            ConfigHelper.getInstance(null).saveKey(ConfigHelper.CONFIG_KEY_FRIENDS, s);
            ConfigHelper.getInstance(null).saveIntKey(ConfigHelper.CONFIG_KEY_FRIENDS_UPDATE_TIMESTAMP, Utils.getNow());
            ConfigHelper.getInstance(null).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<User> getFriends() {
        try {
            String s = ConfigHelper.getInstance(null).loadKey(ConfigHelper.CONFIG_KEY_FRIENDS);
            JSONArray array = new JSONArray(s);
            ArrayList<User> users = new ArrayList<User>();
            for (int i = 0; i < array.length(); i++) {
                User u = new User();
                JSONObject obj = (JSONObject) array.get(i);
                u.setId(obj.getString("uid"));
                u.setName(obj.optString("name"));
                users.add(u);
            }
            return users;
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<User>();
        }
    }

    public int getFriendsUpdateTimestamp() {
        int ts = ConfigHelper.getInstance(null).loadIntKey(ConfigHelper.CONFIG_KEY_FRIENDS_UPDATE_TIMESTAMP, 0);
        return ts;
    }


}

package cn.com.nd.momo.activity;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.model.FriendDB;
import cn.com.nd.momo.util.Utils;
import cn.com.nd.momo.view.CustomImageView;

public class SelectorActivity extends Activity implements OnClickListener {
    private final static String TAG = "SelectorActivity";

    public final static int USER_PICKED = 2000;

    public static final String ACTION_SINGLE_PICK = "ACTION_SINGLE_PICK";

    public static final String ACTION_MULIT_PICK = "ACTION_MULIT_PICK";

    public static final String ACTION_IM_PICK = "ACTION_IM_PICK";

    public static final String ACTION_USER_PICK = "ACTION_USER_PICK";

    public static final String ACTION_STATUSES_PICK = "ACTION_STATUSES_PICK";

    public static final int REQUEST_USER_PICK = 788;

    public static final String EXTRA_RECEIVERS = "receivers";

    private ListView mGridView;


    private FriendGridAdapter mFriendGridAdapter;
    private class User {
        public long uid;
        public String name;
        public boolean isSelected;
    }
    /**
     * 分享用户选择
     */
    private boolean mIsStatusesUserSelect = false;

    /**
     * <br>
     * Description:准备开始选择,清空用户数组. <br>
     * Author:hexy <br>
     * Date:2011-10-6下午04:57:58
     */
    public static void prepare() {

    }


    private final int MSG_GET_UID_FINISH = 1;


    private ProgressDialog mProgressUID = null;

    /**
     * <br>
     * Description:点击确认后调用的函数. <br>
     * Author:hexy <br>
     * Date:2011-10-6下午04:59:33
     */
    private void onConfirm() {
        try {
            JSONArray json = new JSONArray();
            for (User user : mItems) {
                if (!user.isSelected) {
                    continue;
                }
                JSONObject u = new JSONObject();
                u.put("id", String.valueOf(user.uid));
                u.put("name", user.name);
                json.put(u);
            }

            String s = json.toString();
            Intent data = new Intent();
            data.putExtra(EXTRA_RECEIVERS, s);
            if (json.length() > 0) {
                setResult(RESULT_OK, data);
            }
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FriendDB db = FriendDB.instance();
        int ts = db.getFriendsUpdateTimestamp();
        int now = Utils.getNow();
        //超过1个小时为更新
        if (now - ts > 60*60) {
            new AsyncTask<Void, Integer, ArrayList<cn.com.nd.momo.api.types.User>>() {
                @Override
                protected ArrayList<cn.com.nd.momo.api.types.User> doInBackground(Void... urls) {
                    try {
                        return MoMoHttpApi.getFriends();
                    } catch (MoMoException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
                @Override
                protected void onPostExecute(ArrayList<cn.com.nd.momo.api.types.User> users) {
                    if (users != null) {
                        FriendDB.instance().setFriends(users);

                        mItems.clear();
                        for (cn.com.nd.momo.api.types.User u : users) {
                            User uu = new User();
                            uu.uid = Long.parseLong(u.getId());
                            uu.name = u.getName();
                            mItems.add(uu);
                        }
                        mFriendGridAdapter.notifyDataSetChanged();
                    }
                }
            }.execute();
        } else {
            ArrayList<cn.com.nd.momo.api.types.User> users = db.getFriends();
            if (users != null) {
                for (cn.com.nd.momo.api.types.User u : users) {
                    User uu = new User();
                    uu.uid = Long.parseLong(u.getId());
                    uu.name = u.getName();
                    mItems.add(uu);
                }
            }
        }


        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dynamic_search);
        findViewById(R.id.title_bar).setVisibility(View.VISIBLE);

        mGridView = (ListView)findViewById(R.id.gridview);
        mFriendGridAdapter = new FriendGridAdapter();
        mGridView.setOnItemClickListener(mOnItemClickListener);
        mGridView.setAdapter(mFriendGridAdapter);

        findViewById(R.id.btn_confirm).setOnClickListener(this);
    }

    ProgressDialog mProgress = null;

    @Override
    protected void onResume() {
        super.onResume();
    }

    // 退出程序监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown" + event.getRepeatCount());
        return super.onKeyDown(keyCode, event);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            int i = arg2 - mGridView.getHeaderViewsCount();
            User u = mItems.get(i);
            u.isSelected = !u.isSelected;
            mFriendGridAdapter.notifyDataSetChanged();
        }
    };

    private ArrayList<User> mItems = new ArrayList<User>();

    public class FriendGridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (position == mItems.size())
                return 0;
            User info = mItems.get(position);
            return info.uid;
        }

        class ViewHold {
            CustomImageView avatar;

            TextView name;

            CheckBox checkbox;
        };

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Log.i(TAG, "getView" + position);

            ViewHold hold;
            // find view
            if (convertView == null) {
                convertView = View.inflate(SelectorActivity.this, R.layout.user_selector_list_item,
                        null);
                hold = new ViewHold();
                hold.avatar = (CustomImageView)convertView
                        .findViewById(R.id.img_contact_item_presence);
                hold.name = (TextView)convertView.findViewById(R.id.txt_contact_item_name);
                hold.checkbox = (CheckBox)convertView.findViewById(R.id.chk_contact_item_select);
                hold.checkbox.setClickable(false);
                android.widget.LinearLayout.LayoutParams layoutParam = new android.widget.LinearLayout.LayoutParams(
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParam.setMargins(0, 0, 40, 0);
                hold.checkbox.setLayoutParams(layoutParam);
                convertView.setTag(hold);
            } else {
                hold = (ViewHold)convertView.getTag();
            }
            final User u = (User)getItem(position);
            hold.name.setText(u.name);
            boolean selected = u.isSelected;
            hold.checkbox.setChecked(selected);
            return convertView;
        }
    }


    public static String addUser(Contact info) {
        // 已选中的 不重复加入
        for (Contact dc : mUserArrayMap) {
            if (!TextUtils.isEmpty(info.getPrimePhoneNumber()) 
            		&& dc.getPrimePhoneNumber().equals(info.getPrimePhoneNumber()) 
            		|| info.getUid() > 0 && info.getUid() == dc.getUid()) {
            	//Log.i(TAG, "addUser: " + dc.getPrimePhoneNumber());
                return "@" + info.getFormatName();
            }
        }

        mUserArrayMap.add(info);

        //Log.i(TAG, "addUser2: " + info.getPrimePhoneNumber());
        return "@" + info.getFormatName();
    }



    public static String addUser(long id, String name) {
        // 已选中的 不重复加入
        Contact c = new Contact();
        c.setUid(id);
        c.setFormatName(name);
        return addUser(c);
    }

    public static String encodeName(String name, long id) {
        return "@" + name;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                onConfirm();
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressUID != null && mProgressUID.isShowing()) {
            mProgressUID.dismiss();
        }

        System.gc();
    }

    private static ArrayList<Contact> mUserArrayMap = new ArrayList<Contact>();

    public static int getAtSize() {
        return mUserArrayMap.size();
    }

    public static String encode(String src) {
        for (Contact finfo : mUserArrayMap) {
            src = src.replace("@" + finfo.getFormatName(), "[" + finfo.getUid() + "]");
        }
        for (Contact finfo : mUserArrayMap) {
            src = src.replace("[" + finfo.getUid() + "]", "@" + finfo.getFormatName() + "("
                    + finfo.getUid() + ")");
        }
        Log.i(TAG, "encode : " + src);
        return src;
    }







}

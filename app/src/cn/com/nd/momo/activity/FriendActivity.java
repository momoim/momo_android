package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.model.ContactDB;
import cn.com.nd.momo.model.FriendDB;
import cn.com.nd.momo.R;

/**
 * Created by houxh on 14-8-12.
 */
public class FriendActivity extends TabActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener {
    private final  static String TAG = "im";

    ArrayList<User> potentialFriends = new ArrayList<User>();
    ArrayList<User> friends = new ArrayList<User>();

    private ListView lv;
    private BaseAdapter adapter;

    class FriendAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return potentialFriends.size() + friends.size();
        }

        @Override
        public Object getItem(int position) {
            if (position < potentialFriends.size()) {
                return potentialFriends.get(position);
            } else {
                position = position - potentialFriends.size();
                return friends.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null) {
                view = getLayoutInflater().inflate(R.layout.friend_item, null);
                Button btn = (Button)view.findViewById(R.id.button);
                btn.setOnClickListener(FriendActivity.this);
            } else {
                view = convertView;
            }
            TextView tv = (TextView) view.findViewById(R.id.name);
            Button btn = (Button)view.findViewById(R.id.button);

            User u = null;
            if (position < potentialFriends.size()) {
                u = potentialFriends.get(position);
                btn.setEnabled(true);
                btn.setText(R.string.btn_add_friend);
            } else {
                u = friends.get(position - potentialFriends.size());
                btn.setEnabled(false);
                btn.setText(R.string.btn_is_friend);
            }
            tv.setText(u.getName());
            btn.setTag(u);
            return view;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.friends);

        FriendDB db = FriendDB.instance();
        int now = cn.com.nd.momo.util.Utils.getNow();
        int ts = db.getFriendsUpdateTimestamp();

        //todo 在后台定时刷新好友列表
        new AsyncTask<Void, Integer, ArrayList<User>>() {
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
                    friends = users;
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute();


        ContactDB.getInstance().loadContacts();
        final ArrayList<String> mobiles = ContactDB.getInstance().loadAllMobile();
        new AsyncTask<Void, Integer, ArrayList<User>>() {
            @Override
            protected ArrayList<cn.com.nd.momo.api.types.User> doInBackground(Void... urls) {
                try {
                    return MoMoHttpApi.getPotentialFriends(mobiles);
                } catch (MoMoException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            @Override
            protected void onPostExecute(ArrayList<cn.com.nd.momo.api.types.User> users) {
                if (users != null) {
                    potentialFriends = users;
                    adapter.notifyDataSetChanged();
                }
            }
        }.execute();

        adapter = new FriendAdapter();
        lv = (ListView)findViewById(R.id.list);

        lv.setAdapter(adapter);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {


    }

    public void onClick(View v) {
        final User u = (User)v.getTag();
        Log.i("friend", "tag:" + v.getTag());

        if (potentialFriends.contains(u)) {

            final ProgressDialog progressDlg = ProgressDialog.show(this,
                    "",
                    getString(R.string.msg_add_friend));
            progressDlg.setCancelable(false);

            new AsyncTask<Void, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... urls) {
                    try {
                        MoMoHttpApi.addFriend(Long.valueOf(u.getId()));
                        return true;
                    } catch (MoMoException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean r) {
                    progressDlg.dismiss();
                    if (r) {
                        potentialFriends.remove(u);
                        friends.add(u);
                        adapter.notifyDataSetChanged();
                        Utils.displayToast("添加成功", 0);
                    } else {
                        Utils.displayToast("添加失败", 0);
                    }
                }
            }.execute();
        }
    }
}

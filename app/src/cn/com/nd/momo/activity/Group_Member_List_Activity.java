
package cn.com.nd.momo.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.adapters.GroupMemberListAdapter;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.parsers.json.UserParser;
import cn.com.nd.momo.api.statuses.StatusesManager;
import cn.com.nd.momo.api.types.Group;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.types.GroupMember;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.util.Log;

/**
 * 群组成员列表页面
 * 
 * @author 曾广贤 (muroqiu@qq.com)
 */
public class Group_Member_List_Activity extends LinearLayout {

    private Context mContext;

    private GroupInfo mGroupInfo;

    private LayoutInflater mInflater;

    private ListView mListContact = null;

    private GroupMemberListAdapter mGroupMemberListAdapter = null;

    private Group<GroupMember> groupMemberList = null;

    private final int MSG_GET_GROUP_MEMBER_OK = 0;

    private final int MSG_GET_GROUP_MEMBER_FAIL = 1;

    public Group_Member_List_Activity(Context context, GroupInfo groupInfo) {
        super(context);
        mContext = context;
        mGroupInfo = groupInfo;

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.group_member_list, this);

        initView();
    }

    /**
     * 初始化页面
     */
    private void initView() {
        mListContact = (ListView)findViewById(R.id.list_group_member);
        mGroupMemberListAdapter = new GroupMemberListAdapter(mContext);
        mListContact.setAdapter(mGroupMemberListAdapter);
        mListContact.setOnItemClickListener(new OnContactListItemClick());
        mListContact.requestFocus();
        getGroupMemberList(mGroupInfo.getGroupID());
        mListContact.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    case SCROLL_STATE_FLING:
                        mGroupMemberListAdapter.setScrolling(true);
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        mGroupMemberListAdapter.setScrolling(true);
                        break;
                    case SCROLL_STATE_IDLE:
                        mGroupMemberListAdapter.setScrolling(false);
                        mGroupMemberListAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
                // TODO Auto-generated method stub

            }
        });
    }

    /**
     * 获取群成员列表
     * 
     * @param groupID
     */
    private void getGroupMemberList(final int groupID) {
        // 获取群组联系人，刷新页面
        new Thread(new Runnable() {

            @Override
            public void run() {
                Message msg = new Message();
                try {
                    groupMemberList = StatusesManager.getGroupMemberList(groupID);
                    msg.what = MSG_GET_GROUP_MEMBER_OK;
                } catch (MoMoException e) {
                    msg.what = MSG_GET_GROUP_MEMBER_FAIL;
                    e.printStackTrace();
                }

                mHandler.sendMessage(msg);

            }
        }).start();
    }

    private class OnContactListItemClick implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            //// TODO: 15/12/31

            /*
            GroupMember groupMember = (GroupMember)mGroupMemberListAdapter.getItem(arg2);
            if (groupMember != null && groupMember.getId() > 0) {
                Intent intent = new Intent();
                User user = new User();
                user.setId(groupMember.getId().toString());
                user.setName(groupMember.getName());
                intent.setAction(ContactFragmentActivity.ACTION_IM);
                UserParser up = new UserParser();
                String jsonString = "";
                try {
                    JSONObject jsonObject = up.toJSONObject(user);
                    jsonString = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                intent.putExtra(ContactFragmentActivity.EXTRA_USER, jsonString);
                intent.setClass(mContext, ContactFragmentActivity.class);
                mContext.startActivity(intent);
            }*/
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_GROUP_MEMBER_OK:
                    Log.d("MSG_GET_IMG_FINISH received");
                    if (mGroupMemberListAdapter != null) {
                        mGroupMemberListAdapter.SetDataArray(groupMemberList);
                        // when finish loading image from system, refresh list
                        mGroupMemberListAdapter.notifyDataSetChanged();
                    }
                    break;
                case MSG_GET_GROUP_MEMBER_FAIL:
                    Log.d("MSG_GET_IMG_FINISH fail");
                    break;
            }
        }
    };
}

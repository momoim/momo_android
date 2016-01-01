
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.RequestUrl;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.parsers.json.RobotParser;
import cn.com.nd.momo.api.types.Robot;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.RobotManager;
import cn.com.nd.momo.view.CustomImageView;

/**
 * 机器人列表展示
 * 
 * @author jiaolei
 */
public class RobotListActivity extends Activity {
    private String TAG = "RobotListActivity";

    private static final int MSG_GET_ROBOT_COMPLETE = 1;

    private static final int MSG_GET_ROBOT_ERROR = 2;

    private ListView mRobotList = null;

    private List<Robot> mArrayRobot = new ArrayList<Robot>();

    private LayoutInflater mInflater = null;

    private ProgressDialog mProgressDlg = null;

    private RobotAdapter mRobotAdapter = new RobotAdapter();

    private String mError = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.robot_list_activity);

        mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

        mRobotList = (ListView)findViewById(R.id.robot_list);
        mRobotList.setAdapter(mRobotAdapter);
        mRobotList.setOnItemClickListener(new OnRobotItemClick());
        mRobotList.setOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub
                switch (scrollState) {
                    case SCROLL_STATE_FLING:
                        mRobotAdapter.setScrolling(true);
                        break;
                    case SCROLL_STATE_TOUCH_SCROLL:
                        mRobotAdapter.setScrolling(true);
                        break;
                    case SCROLL_STATE_IDLE:
                        mRobotAdapter.setScrolling(false);
                        mRobotAdapter.notifyDataSetChanged();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.getActiveNetWorkName(getApplicationContext()) == null) {
            cn.com.nd.momo.util.Utils.DialogNetWork(this);
        } else {
            getRobotListFromServer();
        }

    }

    private class OnRobotItemClick implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Robot robot = (Robot)mRobotAdapter.getItem(arg2);
            if (robot != null) {
                String url = RequestUrl.ROBOT_3G_URL + robot.getId() + "?token="
                        + GlobalUserInfo.getOAuthKey();
                GlobalUserInfo.openMoMoUrl(RobotListActivity.this, url, true);
            }
        }
    }

    private void getRobotListFromServer() {
        mProgressDlg = ProgressDialog.show(this, "", "获取机器人列表...");
        mProgressDlg.setCancelable(true);
        mError = "";
        new Thread() {
            @Override
            public void run() {
                try {
                    ArrayList<Robot> robotList = MoMoHttpApi.getRobotList();
                    if (robotList != null && !robotList.isEmpty()) {
                        mArrayRobot.clear();
                        mArrayRobot = robotList;

                        RobotManager appManager = RobotManager.getInstance(getApplicationContext());
                        ArrayList<Long> robotIdList = appManager.getRobotIdListFromDababase();
                        ArrayList<Long> subscribedIdList = new ArrayList<Long>();
                        ArrayList<Robot> subscribedList = new ArrayList<Robot>();
                        for (Robot robot : mArrayRobot) {

                            if (robot.isSubscribed()) {
                                if (!robotIdList.contains(robot.getId())) {
                                    JSONObject json = null;
                                    try {
                                        json = new RobotParser().toJSONObject(robot);
                                        appManager.updateRobot2Database(robot.getId(), 0,
                                                appManager.ROBOT_SUBSCRIBE, json.toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                subscribedIdList.add(robot.getId());
                                subscribedList.add(robot);
                            }
                        }
                        for (long robotId : robotIdList) {
                            if (!subscribedIdList.contains(robotId)) {
                                appManager.cancelSubscribeRobot2Database(robotId);
                            }
                        }
                        appManager.setRobotList(subscribedList);
                        mHandler.sendEmptyMessage(MSG_GET_ROBOT_COMPLETE);
                    }
                } catch (MoMoException ex) {
                    Log.e(TAG, ex.getMessage());
                    mHandler.sendEmptyMessage(MSG_GET_ROBOT_ERROR);
                }
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mProgressDlg != null && mProgressDlg.isShowing()) {
                mProgressDlg.dismiss();
            }
            switch (msg.what) {
                case MSG_GET_ROBOT_COMPLETE:
                    mRobotAdapter.setData(mArrayRobot);
                    mRobotAdapter.notifyDataSetChanged();
                    break;
                case MSG_GET_ROBOT_ERROR:
                    String error = "获取机器人列表失败";
                    if (mError != null && mError.length() > 0) {
                        error = mError;
                    }
                    Utils.displayToast(error, 0);
                    break;
            }
        }
    };

    class RobotAdapter extends BaseAdapter {

        private List<Robot> robotList = new ArrayList<Robot>();

        public ConcurrentHashMap<Long, Bitmap> mMapCacheAppAvatar = new ConcurrentHashMap<Long, Bitmap>();

        private boolean mIsScrolling = false;

        public void setData(List<Robot> dataList) {
            robotList.clear();
            if (dataList != null) {
                robotList = new ArrayList<Robot>(Arrays.asList(new Robot[dataList.size()]));
                Collections.copy(robotList, dataList);
            }
        }

        public void setScrolling(boolean bScroll) {
            mIsScrolling = bScroll;
        }

        @Override
        public int getCount() {
            return robotList.size();
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();
            if (count == 0 || position < 0 || position > count - 1) {
                return null;
            }
            return robotList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            ViewHolder holder = null;
            if (convertView == null) {
                view = mInflater.inflate(R.layout.robot_list_item, null);
                holder = new ViewHolder();
                holder.avatar = (CustomImageView)view.findViewById(R.id.image_robot_avatar);
                holder.name = (TextView)view.findViewById(R.id.txt_robot_name);
                holder.subscription = (TextView)view.findViewById(R.id.txt_robot_subscription);
                view.setTag(holder);
            } else {
                view = convertView;
                holder = (ViewHolder)view.getTag();
            }
            Robot robot = (Robot)getItem(position);
            if (robot.getAvatar() != null && robot.getAvatar().length() > 0) {
                holder.avatar.setCustomImageByRobotId(robot.getId(), robot.getAvatar(),
                        mMapCacheAppAvatar, mIsScrolling);
            }
            holder.name.setText(robot.getName());
            if (robot.isSubscribed()) {
                holder.subscription.setVisibility(View.VISIBLE);
            } else {
                holder.subscription.setVisibility(View.GONE);
            }
            return view;
        }

    }

    static class ViewHolder {
        CustomImageView avatar;

        TextView name;

        TextView subscription;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressDlg != null && mProgressDlg.isShowing()) {
            mProgressDlg.dismiss();
        }

        if (mRobotAdapter != null) {
            mRobotAdapter.mMapCacheAppAvatar.clear();
        }
    }

}

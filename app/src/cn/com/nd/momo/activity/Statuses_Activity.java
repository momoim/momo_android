
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpStatus;
import org.json.JSONArray;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.adapters.GroupAdapter;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.statuses.StatusesManager;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.api.AbsSdk.SdkResult;
import cn.com.nd.momo.model.DraftMgr;
import cn.com.nd.momo.adapters.DynamicAdapter;
import cn.com.nd.momo.model.DynamicDB;
import cn.com.nd.momo.model.DynamicDB.DraftInfo;
import cn.com.nd.momo.model.DynamicInfo;
import cn.com.nd.momo.model.DynamicItemInfo;
import cn.com.nd.momo.model.DynamicMgr;
import cn.com.nd.momo.util.NotifyProgress;
import cn.com.nd.momo.util.IMUtil;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.view.PullToRefreshListView;
import cn.com.nd.momo.view.PullToRefreshListView.OnRefreshListener;

import com.flurry.android.FlurryAgent;

/**
 * 动态列表
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public class Statuses_Activity extends TabActivity implements OnClickListener,
        OnRefreshListener/* , OnBottomListener */{

    public static final String ACTION_VIEW_PERSON_FEED = "DynamicActivity.View.Person";

    private PullToRefreshListView mlistView;

    // private ListView mGroupListView;

    private View mFooterView;

    private TextView mFootText;

    private DynamicAdapter mlistViewAdapter;

    private DynamicBroadcastReceiver mDynamicBroadcastReceiver = new DynamicBroadcastReceiver(
            this);

    private ImageButton btnDynamicComment;

    private ImageView btnAboutMe;

    private LinearLayout btnGroupSelect;

    private CheckBox btnTitleIcon;

    private TextView txtTitle;

    public static DynamicDB mDynamicDB;

    public static DynamicItemInfo mDynamicItemInfo = new DynamicItemInfo();

    private PopupWindow popupWindow;

    private ListView listViewGroup;

    private ArrayList<GroupInfo> groupList = new ArrayList<GroupInfo>();

    private GroupInfo mCurrentGroup = null;

    // private RadioGroup

    /**
     * 虚拟的全部分享组ID
     */
    private static final int ID_GROUPALL = -1;

    private RelativeLayout barTitle = null;

    GroupAdapter adapter = null;

    public static ConcurrentHashMap<Long, Bitmap> mapCache = new ConcurrentHashMap<Long, Bitmap>();

    /**
     * main--------------------------------------------------------------------
     * --------
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.i("onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.statuses_main);

        GlobalUserInfo.statuses_image_mode = ConfigHelper.getInstance(Statuses_Activity.this)
                .loadIntKey(ConfigHelper.CONFIG_KEY_IMAGE_VIEW_MODE,
                        GlobalUserInfo.STATUSES_IMAGE_MODE_SMALL);

        // init db
        mDynamicDB = DynamicDB.initInstance(Statuses_Activity.this);

        barTitle = (RelativeLayout)findViewById(R.id.bar_statuses_title);
        btnDynamicComment = (ImageButton)findViewById(R.id.btn_dynamic_comment);
        btnDynamicComment.setOnClickListener(this);
        btnAboutMe = (ImageView)findViewById(R.id.btn_dynamic_top);
        btnAboutMe.setOnClickListener(this);

        btnGroupSelect = (LinearLayout)findViewById(R.id.btn_group_select);
        btnGroupSelect.setOnClickListener(this);

        mCurrentGroup = initGroupAll();

        txtTitle = (TextView)findViewById(R.id.txt_title);
        txtTitle.setText(mCurrentGroup.getGroupName());

        btnTitleIcon = (CheckBox)findViewById(R.id.icon_title);

        // init listview
        mlistView = (PullToRefreshListView)findViewById(R.id.list_dynimic);
        mlistView.setAbleLoadNextPage(true);
        mlistView.setonLoadNextPageListener(new PullToRefreshListView.OnLoadNextPageListener() {

            @Override
            public void onLoadNextPage() {
                if (inBottom) {
                    // setFootText("无更多分享");
                    mlistView.setAbleLoadNextPage(false);
                    return;
                }
                mlistView.setAbleLoadNextPage(true);
                if (mlistViewAdapter != null && mlistViewAdapter.getCount() > 10)
                    loadDynamicOld();

            }

        });

        mlistView.setOnItemClickListener(mOnItemClickListener);
        mlistView.setonRefreshListener(this);
        // mlistView.setOnBottomListener(this);
        mlistView.setOnItemLongClickListener(mOnLongClickListener);

        mFooterView = View.inflate(this, R.layout.dynamic_list_header, null);
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDynamicOld();
            }
        });

        mFootText = ((TextView)mFooterView.findViewById(R.id.dynamic_title));

        mlistView.addFooterView(mFooterView, null, false);

        // set adapter
        mlistViewAdapter = new DynamicAdapter(this, true);
        mlistView.setAdapter(mlistViewAdapter);

        Log.i("onCreate done");

        mDynamicBroadcastReceiver
                .registerAction(getString(R.string.action_message_destroy));
        mDynamicBroadcastReceiver
                .registerAction(NotifyProgress.ACTION_COMMENT_SUCCEED);
        mDynamicBroadcastReceiver.registerAction(NotifyProgress.ACTION_SUCCEED);
        mDynamicBroadcastReceiver.registerAction(NotifyProgress.ACTION_FAIL);
        mDynamicBroadcastReceiver.registerAction(NotifyProgress.ACTION_PROCESS);
    }

    // DrawImagesView btnImages;

    private String getFilter() {
        String filter = "";
        if (mCurrentGroup == null || mCurrentGroup.getGroupID() == ID_GROUPALL) {
            return "";
        } else {
            filter = "?group_id=" + mCurrentGroup.getGroupID();
        }
        return filter;

    }

    private void loadDynamicOld() {
        if (mlistViewAdapter == null)
            return;
        if (mlistViewAdapter.getCount() == 0) {
            loadData(0, "down", getFilter(), 0);
        } else {
            loadData(mlistViewAdapter.getLastTime(), "down", getFilter(), 0);
        }
    }

    private void loadDynamicNew() {
        loadDynamicNew(0);
    }

    private void loadDynamicNew(long draftID) {
        loadData(mlistViewAdapter.getFirstTime(), "up", getFilter(), draftID);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mDynamicBroadcastReceiver);
        mapCache.clear();
        System.gc();
        super.onDestroy();
    }

    private void setAllVisible(boolean visible) {
        btnDynamicComment.setEnabled(visible);
        btnAboutMe.setEnabled(visible);
    }

    @Override
    protected void onPause() {
        Log.i("onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        Log.i("onStart");
        super.onStart();
        FlurryAgent.logEvent("Statuses List");
    }

    @Override
    protected void onResume() {
        super.onResume();

        TextView txtHeader = (TextView)this.findViewById(R.id.conversation_history_list_header);
        txtHeader.setTextColor(0xff246a7e);
        int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 45, getResources()
                .getDisplayMetrics());
        txtHeader.setHeight(height);
        txtHeader.setGravity(Gravity.CENTER);
        txtHeader.setBackgroundResource(R.drawable.dynamic_list_new_bg);

        if (IMUtil.isEmptyString(GlobalUserInfo.getAvatar())) { // 提示设置头像
            txtHeader.setText(R.string.notify_set_avatar);
            txtHeader.setOnClickListener(this);

        } else {
            txtHeader.setVisibility(View.GONE);
        }
        setAllVisible(GlobalUserInfo.hasLogined());

        if (!GlobalUserInfo.hasLogined()) {
            mlistViewAdapter.removeAll();
            mlistViewAdapter.notifyDataSetChanged();
            return;
        }

        // 当界面无东西时， 优先显示缓存
        if (mlistViewAdapter.getCount() == 0) {
            Log.i("loading cache on resume");
            loadDynamicOld();
        }

        initData();

        // 无网络 直接返回
        String net = Utils.getActiveNetWorkName(Statuses_Activity.this);
        if (net == null) {
            mlistView
                    .onRefreshComplete(getString(R.string.msg_group_sync_no_connection_found));
            // Utils.DialogNetWork(DynamicActivity.this);
            Toast.makeText(this,
                    getString(R.string.msg_group_sync_no_connection_found), 0)
                    .show();
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        super.onPrepareOptionsMenu(menu);
        menu.add(0, 1, 1, getResources().getString(R.string.main_activity_menu_draft));
        menu.add(0, 2, 2, "选人分享");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch (item.getItemId()) {
            case 1:
                // ToDo Zgx 20120731 分享发送
                i = new Intent(Statuses_Activity.this, Statuses_Draft_Activity.class);
                startActivity(i);
                break;
            case 2:
                Intent iShare = new Intent(this, Statuses_Send_Activity.class);
                iShare.setAction(Statuses_Send_Activity.ACTION_BROADCAST);
                iShare.putExtra(Statuses_Send_Activity.ACTIVITY_TYPE,
                        Statuses_Send_Activity.TYPE_FOR_MO_SHARE);
                if (mCurrentGroup.getGroupID() > 0) {
                    iShare.putExtra(Statuses_Send_Activity.PARAM_GROUPID,
                            mCurrentGroup.getGroupID());
                    iShare.putExtra(Statuses_Send_Activity.PARAM_GROUPNAME,
                            mCurrentGroup.getGroupName());
                }
                startActivity(iShare);
                break;
            case 3:
                i = new Intent(this, OptionActivity.class);
                startActivity(i);
                break;
            case 4:
            case 5:
            case 6:
                break;
        }

        return true;
    }

    private boolean inBottom = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent paramIntent) {
        switch (requestCode) {
            case REQUEST_SELETE_GROUP:
                if (resultCode == RESULT_OK) {
                    // mCurrentGroup = DynamicPoster.mLastGroupinfo;
                    // mlistViewAdapter.removeAll();
                    // loadDynamicNew();
                    // inBottom = false;
                    break;
                }
        }

        switch (resultCode) {
            case DynamicMgr.MSG_POST_BROADCAST:
            case DynamicMgr.MSG_POST_COMMENT:
            case DynamicMgr.MSG_POST_PRISE:
            case DynamicMgr.MSG_POST_REPLY:
                Log.i("onActivityResult");
                loadDynamicNew();
                break;
            case DynamicMgr.MSG_POST_DELETE_DYNAMIC:
                // 个人主页删除动态后 更新此界面
                mlistViewAdapter.deleteItem(mDynamicItemInfo.id);
                mlistViewAdapter.notifyDataSetChanged();
                break;
            // case DynamicMgr.MSG_POST_BROADCAST_PROCESS:
            // // 发送广播后 加入发送中的动态
            // long draftID;
            // draftID = paramIntent.getLongExtra(DraftMgr.DRAFT_ID, 0);
            // DraftInfo draftInfo = DraftMgr.instance().getDraft(draftID);
            // draftInfo.sending = true;
            // addDraftInfoToList(draftInfo);
            //
            // Log.i( "draft process add to list" + draftID);
            // mlistViewAdapter.notifyDataSetChanged();
            // break;
            case DynamicMgr.MSG_POST_BROADCAST_FAIL:
                // 提示
                // draftID = paramIntent.getLongExtra(DraftMgr.DRAFT_ID, 0);
                // DraftInfo draftInfoFail =
                // DraftMgr.instance().getDraft(draftID);
                // Toast.makeText(DynamicActivity.this, draftInfoFail.content,
                // 0).show();
                // draftInfoFail.sending = false;
                // //mlistViewAdapter.getDynamicItemInfo(draftInfoFail.typeid,
                // draftInfoFail.objid).sendStatus = DynamicItemInfo.SEND_FAIL;
                // mlistViewAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }

    private void addDraftInfoToList(DraftInfo draftInfo) {
        DynamicItemInfo dinfo = getDynamicInfoFromDraft(draftInfo);
        if (dinfo != null) {
            mlistViewAdapter.addItem(0, dinfo);
        }
    }

    private DynamicItemInfo getDynamicInfoFromDraft(DraftInfo draftInfo) {
        if (!GlobalUserInfo.hasLogined())
            return null;
        DynamicItemInfo dinfo = new DynamicItemInfo();
        dinfo.realname = GlobalUserInfo.getName();
        Log.i("addDraftInfoToList:" + dinfo.realname);
        dinfo.text = draftInfo.content;
        dinfo.sendStatus = DynamicItemInfo.SEND_PROCESS;
        dinfo.draftID = draftInfo.id;
        dinfo.createAt = draftInfo.crateData;
        dinfo.modifiedAt = mlistViewAdapter.getFirstTime();
        dinfo.uid = Long.valueOf(GlobalUserInfo.getUID());
        dinfo.id = String.valueOf(draftInfo.id);
        dinfo.commentCount = draftInfo.percent;
        dinfo.date = DynamicInfo.getTime(dinfo.createAt * 1000);
        // json 解析pic
        if (draftInfo.images != null && draftInfo.images.length() > 0) {
            try {
                JSONArray imagesArray = new JSONArray(draftInfo.images);
                for (int j = 0; j < imagesArray.length(); ++j) {
                    dinfo.images.add(imagesArray.get(j).toString());
                }
            } catch (Exception e) {
            }
        }
        return dinfo;
    }

    // 界面按钮监听
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.conversation_history_list_header:
                DynamicItemInfo.viewContactFragmentActivity(
                        Long.parseLong(GlobalUserInfo.getUID()), GlobalUserInfo.getName(), this);
                // }
                break;
            // 跳转到广播界面
            case R.id.btn_dynamic_comment:
                Intent intent = new Intent(Statuses_Activity.this,
                        Statuses_Send_Activity.class);
                intent.setAction(Statuses_Send_Activity.ACTION_BROADCAST);
                if (mCurrentGroup.getGroupID() > 0) {
                    intent.putExtra(Statuses_Send_Activity.PARAM_GROUPID,
                            mCurrentGroup.getGroupID());
                    intent.putExtra(Statuses_Send_Activity.PARAM_GROUPNAME,
                            mCurrentGroup.getGroupName());
                }
                startActivityForResult(intent, DynamicMgr.MSG_POST_BROADCAST);
                break;
            // 移动到顶部
            case R.id.btn_dynamic_top:

                //todo
//                Intent i = new Intent(this, MentionListActivity.class);
//
//                ConfigHelper.getInstance(this).saveKey("mome_viewed", "1");
//                ConfigHelper.getInstance(this).commit();
//
//                startActivity(i);
                break;
            // 显示群组选择
            case R.id.btn_group_select:
                showGroup();
                break;

            default:
                break;
        }
    }

    private final int REQUEST_SELETE_GROUP = 100;

    /**
     * 弹出菜单点击监听
     */
    OnItemClickListener groupListClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            GroupInfo selectedGroup = groupList.get(position - mlistView.getHeaderViewsCount());
            if (mCurrentGroup.getGroupID() != selectedGroup.getGroupID()) {
                mCurrentGroup = selectedGroup;

                // 选中其他群组，则重新刷新分享列表数据
                mlistViewAdapter.removeAll();
                mlistViewAdapter.notifyDataSetChanged();
                mlistView.forceRefresh(false);
            }

            if (mCurrentGroup != null) {
                String groupName = mCurrentGroup.getGroupName();
                if (groupName.length() > 8) {
                    groupName = groupName.substring(0, 8);
                }
                txtTitle.setText(groupName);
                Log.i(mCurrentGroup.getGroupID() + mCurrentGroup.getGroupName());
            }
            popupWindow.dismiss();
        }
    };

    /**
     * 弹出菜单消失监听
     */
    OnDismissListener popupdismissListener = new OnDismissListener() {

        @Override
        public void onDismiss() {
            btnTitleIcon.setChecked(false);
            btnTitleIcon.setButtonDrawable(R.drawable.arrow_down);
        }
    };

    /**
     * 图片预览模式选择监听
     */
    OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            // 保存配置
            switch (checkedId) {
                case R.id.rbtn_small_map:
                    GlobalUserInfo.statuses_image_mode = GlobalUserInfo.STATUSES_IMAGE_MODE_SMALL;
                    break;
                case R.id.rbtn_big_map:
                    GlobalUserInfo.statuses_image_mode = GlobalUserInfo.STATUSES_IMAGE_MODE_BIG;
                    break;
                case R.id.rbtn_no_map:
                    GlobalUserInfo.statuses_image_mode = GlobalUserInfo.STATUSES_IMAGE_MODE_NO;
                    break;
                default:
                    break;
            }
            ConfigHelper.getInstance(Statuses_Activity.this).saveIntKey(
                    ConfigHelper.CONFIG_KEY_IMAGE_VIEW_MODE, GlobalUserInfo.statuses_image_mode);

            // 去除弹出菜单
            popupWindow.dismiss();

            // 更新图片预览
            mlistViewAdapter.removeAll();
            mlistViewAdapter.notifyDataSetChanged();
            mlistView.forceRefresh(false);
        }
    };

    private void showGroup() {
        if (btnTitleIcon.isChecked()) {
            popupWindow.dismiss();
        }

        // mCurrentGroup = null;
        btnTitleIcon.setChecked(true);
        btnTitleIcon.setButtonDrawable(R.drawable.arrow_up);
        if (popupWindow == null) {
            LayoutInflater lay = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = lay.inflate(R.layout.popup_menu, null);
            // v.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corners_view));

            // 初始化listview，加载数据。
            listViewGroup = (ListView)v.findViewById(R.id.popup_list);

            LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
            RadioGroup mLayoutHeader = (RadioGroup)mInflater.inflate(R.layout.popup_menu_header,
                    null);
            GlobalUserInfo.statuses_image_mode = ConfigHelper.getInstance(Statuses_Activity.this)
                    .loadIntKey(ConfigHelper.CONFIG_KEY_IMAGE_VIEW_MODE,
                            GlobalUserInfo.STATUSES_IMAGE_MODE_SMALL);
            switch (GlobalUserInfo.statuses_image_mode) {
                case GlobalUserInfo.STATUSES_IMAGE_MODE_SMALL:
                    mLayoutHeader.check(R.id.rbtn_small_map);
                    break;
                case GlobalUserInfo.STATUSES_IMAGE_MODE_BIG:
                    mLayoutHeader.check(R.id.rbtn_big_map);
                    break;
                case GlobalUserInfo.STATUSES_IMAGE_MODE_NO:
                    mLayoutHeader.check(R.id.rbtn_no_map);
                    break;
                default:
                    mLayoutHeader.check(R.id.rbtn_small_map);
                    break;
            }
            mLayoutHeader.setOnCheckedChangeListener(checkedChangeListener);
            mLayoutHeader.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();

                }
            });

            listViewGroup.addHeaderView(mLayoutHeader);

            adapter = new GroupAdapter(this);
            listViewGroup.setAdapter(adapter);
            listViewGroup.setItemsCanFocus(false);
            listViewGroup.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listViewGroup.setOnItemClickListener(groupListClickListener);

            popupWindow = new PopupWindow(v, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT,
                    true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(true);
            popupWindow.setOnDismissListener(popupdismissListener);
        }

        groupList.clear();
        ArrayList<GroupInfo> groupDatas = DynamicMgr.getInstance().getGroups();
        if (groupDatas != null && groupDatas.size() > 0) {
            groupList.addAll(groupDatas);
        }
        groupList.add(0, initGroupAll());

        adapter.SetDataArray(groupList);
        adapter.notifyDataSetChanged();
        popupWindow.update();
        popupWindow.showAsDropDown(barTitle);
    }

    /**
     * 读取数据
     */

    private String mLoadingDirect;

    private boolean mIsLoading = false;

    private void setFootText(String str) {
        mFootText.setText(str);
    }

    private void startLoading() {
        mIsLoading = true;
        // btnDynamicTop.setStart(true);
    }

    private void stopLoading() {
        Log.i("stopLoading");
        mIsLoading = false;
        // btnDynamicTop.setStart(false);
        // if (mlistView.getFirstVisiblePosition() == 0) {
        // Log.i("stopLoading top");
        // mlistView.setSelection(1);
        // }
    }

    // 读取动态数据
    private void loadData(final long inLasttime, final String iDirect,
            final String filter, final long draftID) {
        final int PAGESIZE_UP = 50;
        final int PAGESIZE_DOWN = 20;
        if (mIsLoading)
            return;

        if (!GlobalUserInfo.hasLogined()) {
            mlistViewAdapter.removeAll();
            mlistViewAdapter.notifyDataSetChanged();
            return;
        }

        if (iDirect.equals("up")) {
        } else {
            setFootText("读取中");
        }

        Log.i("currentTimeMillis: " + System.currentTimeMillis());
        Log.i("inLasttime: " + inLasttime + "  iDirect: " + iDirect);
        mIsLoading = true;

        new Thread() {
            @Override
            public void run() {
                mLoadingDirect = iDirect;
                long lasttime = inLasttime;
                SdkResult result = new SdkResult();
                // 读取旧的就读20条,读取新的, 就读到最新
                int pagesize = mLoadingDirect.equals("down") ? PAGESIZE_DOWN : PAGESIZE_UP;

                final Vector<DynamicItemInfo> resultInfoArray = new Vector<DynamicItemInfo>();
                // 读本地
                if (mLoadingDirect.equals("down") && filter.length() == 0) {
                    ArrayList<DynamicInfo> cacheDynamicInfo = mDynamicDB
                            .queryDynamic(lasttime, "" + PAGESIZE_DOWN);

                    // 当本地有数据时 读取并且显示,
                    if (cacheDynamicInfo != null && cacheDynamicInfo.size() > 0) {
                        for (int i = 0; i < cacheDynamicInfo.size(); i++) {
                            DynamicItemInfo dinfo = new DynamicItemInfo(
                                    cacheDynamicInfo.get(i));

                            // 如果此动态已经被顶到前面 则不显示!
                            if (mlistViewAdapter.isExist(dinfo.id))
                                continue;

                            resultInfoArray.add(dinfo);
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // 使得发送中的总在最前
                                if (DraftMgr.instance().getDraft().size() > 0) {
                                    for (DraftInfo draftInfo : DraftMgr
                                            .instance().getDraft()) {
                                        if (draftInfo.sending) {
                                            addDraftInfoToList(draftInfo);
                                        }
                                    }
                                }
                                mlistViewAdapter.addItems(resultInfoArray);
                                mlistViewAdapter.notifyDataSetChanged();
                            }
                        });

                        // 本地读取结束 返回
                        result.ret = DynamicMgr.HTTP_OK;
                        sendMessage(DynamicMgr.MSG_GET_BROADCAST, result);
                        return;
                    }

                    // 第一次登录-无缓存的情况 则请求最新的数据
                    if (Long.MAX_VALUE == lasttime
                            && (cacheDynamicInfo == null || cacheDynamicInfo
                                    .size() == 0)) {
                        mLoadingDirect = "up";
                        lasttime = 0;
                        pagesize = PAGESIZE_UP;
                    }
                }

                // 读网络
                String timeDirect = mLoadingDirect.equals("down") ? "downtime"
                        : "uptime";

                try {
                    ArrayList<DynamicItemInfo> resultData = StatusesManager.getStatuses(pagesize,
                            timeDirect, inLasttime, filter);
                    final Vector<DynamicItemInfo> resultInfoArray_Deleted = new Vector<DynamicItemInfo>();
                    for (DynamicItemInfo dynamicItemInfo : resultData) {
                        if (!dynamicItemInfo.isDeleted) {
                            // 正常动态
                            resultInfoArray.add(dynamicItemInfo);
                        } else {
                            // 服务端已删除的动态
                            resultInfoArray_Deleted.add(dynamicItemInfo);
                        }
                    }
                    result.ret = HttpStatus.SC_OK;

                    // 显示
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {

                            // 优先去除刚发送成功的分享草稿
                            if (draftID > 0) {
                                mlistViewAdapter.deleteItem(String.valueOf(draftID));
                                mlistViewAdapter.notifyDataSetChanged();
                            }

                            if (mLoadingDirect.equals("up")) {
                                mlistViewAdapter.addItems(0,
                                        resultInfoArray);

                                // 使得发送中的总在最前
                                if (DraftMgr.instance().getDraft().size() > 0) {
                                    for (DraftInfo draftInfo : DraftMgr
                                            .instance().getDraft()) {
                                        if (draftInfo.sending) {
                                            addDraftInfoToList(draftInfo);
                                        }
                                    }
                                }
                                mlistView.onRefreshComplete();

                            } else {
                                mlistViewAdapter.addItems(resultInfoArray);
                                if (resultInfoArray.size() != PAGESIZE_DOWN) {
                                    // 已经没有旧数据
                                    inBottom = true;
                                }
                            }
                            stopLoading();
                            mlistViewAdapter.notifyDataSetChanged();
                        }
                    });

                    // delete items
                    if (resultInfoArray_Deleted.size() > 0) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mlistViewAdapter.deleteItems(resultInfoArray_Deleted);
                                stopLoading();
                                mlistViewAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                } catch (MoMoException e) {
                    result.ret = e.getCode();
                    result.response = e.getSimpleMsg();
                } finally {
                    mIsLoading = false;
                    sendMessage(DynamicMgr.MSG_GET_BROADCAST, result);
                    if (result.ret == HttpStatus.SC_OK) {
                        if (filter.length() == 0) {
                            for (DynamicItemInfo item : resultInfoArray)
                                mDynamicDB.insertDynamic(item, false);
                        }

                        // 群组等动态，不缓存
                        // if (getFilter().length() != 0) {
                        // for (DynamicItemInfo item : resultInfoArray)
                        // mDynamicDB.insertDynamic(item, true);
                        // }
                    }
                }
            }
        }.start();
    }

    private void sendMessage(int what, SdkResult result) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            cn.com.nd.momo.util.Utils.hideWaitDialog();
            // ��json����
            switch (msg.what) {
                case DynamicMgr.MSG_DOWNLOAD_AVATAR:
                    mlistViewAdapter.notifyDataSetChanged();
                    return;
                case DynamicMgr.MSG_POST_LOGIN:
                    if (msg.arg1 == 200) {
                        loadDynamicNew();
                        btnDynamicComment.setEnabled(true);
                    } else {

                        String error = getString(R.string.msg_login_unknow_error)
                                + getString(R.string.dynamic_errorcode) + "("
                                + msg.arg1 + ")";
                        mlistView.onRefreshComplete(error);
                    }
                    return;
                case DynamicMgr.MSG_GET_GROUPS:
                    // setGroupAdapter();
                    return;
            }

            // json
            SdkResult result = (SdkResult)(msg.obj);
            int ret = result.ret;

            if (ret == DynamicMgr.HTTP_OK) {
                switch (msg.what) {
                    case DynamicMgr.MSG_GET_BROADCAST:
                        stopLoading();
                        setFootText(getString(R.string.dynamic_more));

                        mlistViewAdapter.notifyDataSetChanged();

                        if (mLoadingDirect.equals("up")) {
                            mlistView
                                    .onRefreshComplete(getString(R.string.dynamic_refresh_last_time)
                                            + DynamicInfo.getTimeFormat(
                                                    "yyyy-MM-dd HH:mm",
                                                    System.currentTimeMillis()));
                            mlistView.setSelection(0);
                        }

                        break;
                    case DynamicMgr.MSG_POST_DELETE_DYNAMIC:
                    case DynamicMgr.MSG_POST_HIDE_DYNAMIC:
                        mlistViewAdapter.deleteItem(mDynamicItemInfo.id);
                        mlistViewAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            } else {
                if (msg.what == DynamicMgr.MSG_GET_BROADCAST) {
                    stopLoading();
                }

                if (ret == 0) {
                    // 无网络 直接返回
                    String net = Utils
                            .getActiveNetWorkName(Statuses_Activity.this);
                    if (net == null) {
                        mlistView
                                .onRefreshComplete(getString(R.string.msg_group_sync_no_connection_found));
                        return;
                    }
                }

                switch (msg.what) {
                    case DynamicMgr.MSG_GET_BROADCAST:
                    default:
                        mlistView.onRefreshComplete(result.ret + " " + result.response);
                        break;
                }
            }
            stopLoading();
        }
    };

    /**
     * 删除分享
     */
    private void postDeleteStatuses() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dynamic_isdelete_comment))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(getString(R.string.txt_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                cn.com.nd.momo.util.Utils.showWaitDialog(
                                        getString(R.string.txt_wait),
                                        Statuses_Activity.this);
                                new Thread() {
                                    public void run() {
                                        SdkResult result = new SdkResult();
                                        try {
                                            StatusesManager.delStatuses(mDynamicItemInfo.id);

                                            DynamicDB.instance().deleteDynamic(mDynamicItemInfo.id);
                                            result.ret = HttpStatus.SC_OK;
                                        } catch (MoMoException e) {
                                            result.ret = e.getCode();
                                        }

                                        sendMessage(DynamicMgr.MSG_POST_DELETE_DYNAMIC, result);
                                    }
                                }.start();
                            }
                        })
                .setNegativeButton(getString(R.string.txt_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }

    /**
     * 隐藏分享
     */
    private void postHide() {
        new Thread() {
            public void run() {
                SdkResult result = new SdkResult();
                try {
                    StatusesManager.hide(mDynamicItemInfo.id, mDynamicItemInfo.hided);

                    // mDynamicItemInfo.hided = mDynamicItemInfo.hided==1?0:1;
                    DynamicDB.instance().deleteDynamic(mDynamicItemInfo.id);
                    result.ret = HttpStatus.SC_OK;
                } catch (MoMoException e) {
                    result.ret = e.getCode();
                }

                sendMessage(DynamicMgr.MSG_POST_HIDE_DYNAMIC, result);
            }
        }.start();
    }

    /**
     * 赞
     */
    private void postFav() {
        new Thread() {
            public void run() {
                SdkResult result = new SdkResult();
                try {
                    StatusesManager.fav(mDynamicItemInfo.id, mDynamicItemInfo.storaged);

                    mDynamicItemInfo.storaged = mDynamicItemInfo.storaged == 1 ? 0 : 1;
                    DynamicDB.instance().insertDynamic(mDynamicItemInfo, false);
                    result.ret = HttpStatus.SC_OK;
                } catch (MoMoException e) {
                    result.ret = e.getCode();
                }

                sendMessage(DynamicMgr.MSG_POST_FAV_DYNAMIC, result);
            }
        }.start();
    }

    private OnItemLongClickListener mOnLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> paramAdapterView,
                View paramView, int paramInt, long paramLong) {
            mDynamicItemInfo = (DynamicItemInfo)mlistViewAdapter
                    .getItem(paramInt - mlistView.getHeaderViewsCount());

            if (mDynamicItemInfo == null) {
                return false;
            }

            boolean isMine = GlobalUserInfo.getUID().equals(String.valueOf(mDynamicItemInfo.uid));

            AlertDialog alertDialog = new AlertDialog.Builder(Statuses_Activity.this).create();
            alertDialog.setTitle("请选择操作");
            if (isMine) {
                alertDialog.setButton("删除", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postDeleteStatuses();
                    }

                });
            } else {
                alertDialog.setButton("转发", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(Statuses_Activity.this, Statuses_Send_Activity.class);
                        i.setAction(Statuses_Send_Activity.ACTION_RETWEET);
                        if (mCurrentGroup.getGroupID() > 0) {
                            i.putExtra(Statuses_Send_Activity.PARAM_GROUPID,
                                    mCurrentGroup.getGroupID());
                            i.putExtra(Statuses_Send_Activity.PARAM_GROUPNAME,
                                    mCurrentGroup.getGroupName());
                        }
                        i.putExtra(GlobalUserInfo.PARAM_STATUSES_ID, mDynamicItemInfo.id);

                        Statuses_Activity.this.startActivity(i);
                    }

                });
                alertDialog.setButton2("隐藏", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        postHide();
                    }

                });
            }

            alertDialog.show();
            return false;
        }
    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            int itemIndex = arg2 - mlistView.getHeaderViewsCount();
            if (itemIndex < 0) {
                return;
            }
            mDynamicItemInfo = (DynamicItemInfo)mlistViewAdapter.getItem(itemIndex);

            // 发送中的不能点击
            if (mDynamicItemInfo.sendStatus == DynamicItemInfo.SEND_PROCESS)
                return;

            Intent i = null;
            if (mDynamicItemInfo.sendStatus == DynamicItemInfo.SEND_FAIL) {
                // 发送失败，进入发送页面
                mlistViewAdapter.deleteItem(mDynamicItemInfo.id);
                mlistViewAdapter.notifyDataSetChanged();

                i = new Intent(Statuses_Activity.this, Statuses_Draft_Activity.class);
            } else {
                i = new Intent(Statuses_Activity.this,
                        Statuses_Comment_Activity.class);
                i.putExtra(GlobalUserInfo.PARAM_STATUSES_ID, mDynamicItemInfo.id);
            }
            // 可在个人主页进行评论 回复 赞 ==操作
            Statuses_Activity.this.startActivityForResult(i, 0);
            // View btnsbar = arg1.findViewById(R.id.quick_btn_bar);
            // btnsbar.setVisibility(btnsbar.getVisibility()==View.GONE?btnsbar.VISIBLE:btnsbar.GONE);
            // Log.i( "onitemclick" + arg2 + "--" + arg2 + "--" + arg3);
        }
    };

    /**
     * receiver----------------------------------------------------------------
     * -------
     */
    public class DynamicBroadcastReceiver extends BroadcastReceiver {

        private Context context;

        private DynamicBroadcastReceiver mReceiver;

        public DynamicBroadcastReceiver(Context c) {
            Log.v("DynamicBroadcastReceiver");
            context = c;
            // to instance it
            mReceiver = this;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            do {

                // when log out reset some thing
                if (intent.getAction().equals(
                        getString(R.string.action_message_destroy))) {
                    mlistViewAdapter = new DynamicAdapter(Statuses_Activity.this,
                            true);
                    mlistView.setAdapter(mlistViewAdapter);
                    mlistViewAdapter.notifyDataSetChanged();
                    // findViewById(R.id.btn_group_select).setEnabled(false);
                    break;
                }
                long draftID = intent.getLongExtra(DraftMgr.DRAFT_ID, 0);
                if (intent.getAction().equals(NotifyProgress.ACTION_SUCCEED)) {
                    Log.i("draft process delete" + draftID);
                    loadDynamicNew(draftID);
                    break;
                }
                if (intent.getAction().equals(NotifyProgress.ACTION_FAIL)) {
                    Log.i("draft process fail" + draftID);
                    String error = intent.getStringExtra("error");
                    // 提示
                    DraftInfo draftInfo = DraftMgr.instance().getDraft(draftID);
                    Toast.makeText(
                            Statuses_Activity.this,
                            error + " " + draftInfo.content
                                    + getString(R.string.dynamic_sendfail), 0)
                            .show();
                    draftInfo.sending = false;
                    DynamicItemInfo dfnfo = mlistViewAdapter
                            .getDynamicItemInfo(String.valueOf(draftID));
                    if (dfnfo != null) {
                        dfnfo.sendStatus = DynamicItemInfo.SEND_FAIL;
                    } else {
                        addDraftInfoToList(draftInfo);
                        dfnfo = mlistViewAdapter.getDynamicItemInfo(String
                                .valueOf(draftID));
                        dfnfo.sendStatus = DynamicItemInfo.SEND_FAIL;
                    }
                    mlistViewAdapter.notifyDataSetChanged();
                    break;
                }
                if (intent.getAction().equals(NotifyProgress.ACTION_PROCESS)) {
                    DraftInfo draftInfo = DraftMgr.instance().getDraft(draftID);
                    int percent = intent.getIntExtra(DraftMgr.DRAFT_PROCESS, 0);
                    Log.i("draft process add percent" + percent);
                    if (draftInfo != null) {
                        draftInfo.sending = true;
                        draftInfo.percent = percent;
                        addDraftInfoToList(draftInfo);
                        Log.i("draft process add" + draftID);
                        mlistViewAdapter.notifyDataSetChanged();
                    }

                    break;
                }
            } while (false);

        }

        public void registerAction(String action) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(action);
            context.registerReceiver(mReceiver, filter);
        }
    }

    private void initData() {
        // mCurrentGroup = DynamicPoster.mLastGroupinfo;
        // if (mCurrentGroup != null) {
        // btnGroupSelect.setText(mCurrentGroup.gname);
        // } else {
        // btnGroupSelect.setText(getString(R.string.dynamic_public));
        // }

        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = DynamicMgr.MSG_GET_GROUPS;

                // if (!DynamicMgr.getInstance().isLoaded()) {
                DynamicMgr.getInstance().refreshGroup();
                mHandler.sendMessage(msg);
                // }

                super.run();
            }
        }.start();
    }

    /**
     * 全部分享（虚拟群）
     * 
     * @return
     */
    private GroupInfo initGroupAll() {
        GroupInfo groupAll = new GroupInfo();
        groupAll.setGroupID(ID_GROUPALL);
        groupAll.setGroupName(getString(R.string.statuses_all));
        return groupAll;
    }

    // @Override
    // public void onBottom() {
    // if (inBottom) {
    // setFootText("无动态");
    // return;
    // }
    // if (mlistViewAdapter != null && mlistViewAdapter.getCount() > 10)
    // loadDynamicOld();
    // }

    @Override
    public void onRefresh(boolean needToast) {
        loadDynamicNew();
    }
}

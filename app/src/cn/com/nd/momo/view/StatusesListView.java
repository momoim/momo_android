
package cn.com.nd.momo.view;

import java.util.ArrayList;
import java.util.Vector;

import org.apache.http.HttpStatus;
import org.json.JSONArray;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.Statuses_Comment_Activity;
import cn.com.nd.momo.activity.Statuses_Send_Activity;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.statuses.StatusesManager;
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
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.view.PullToRefreshListView.OnRefreshListener;

/**
 * 动态列表视图
 * 
 * @author 曾广贤 (muroqiu@qq.com)
 */
public class StatusesListView extends PullToRefreshListView implements
        OnRefreshListener {

    /**
     * 普通分享
     */
    public static final int STATUSES_TYPE_NORMAL = 0;

    /**
     * 个人分享
     */
    public static final int STATUSES_TYPE_USER = 1;

    /**
     * 群组分享
     */
    public static final int STATUSES_TYPE_GROUP = 2;

    private Context mContext;

    private DynamicAdapter mlistViewAdapter;

    private static DynamicItemInfo mDynamicItemInfo = new DynamicItemInfo();

    private boolean mIsLoading = false;

    private String mLoadingDirect;

    private View mFooterView;

    private TextView mFootText;

    private boolean inBottom = false;

    private static DynamicDB mDynamicDB;

    private int mStatusesType = STATUSES_TYPE_NORMAL;

    private String mFilterID = "";

    public StatusesListView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mDynamicDB = DynamicDB.initInstance(mContext);

        setAbleLoadNextPage(true);
        setonLoadNextPageListener(new PullToRefreshListView.OnLoadNextPageListener() {

            @Override
            public void onLoadNextPage() {
                if (inBottom) {
                    // setFootText("无更多分享");
                    setAbleLoadNextPage(false);
                    return;
                }
                setAbleLoadNextPage(true);
                if (mlistViewAdapter != null && mlistViewAdapter.getCount() > 10)
                    loadDynamicOld();
            }

        });

        setOnItemClickListener(mOnItemClickListener);
        setonRefreshListener(this);
        setOnItemLongClickListener(mOnLongClickListener);

        mFooterView = View.inflate(mContext, R.layout.dynamic_list_header, null);
        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadDynamicOld();
            }
        });
        mFootText = ((TextView)mFooterView.findViewById(R.id.dynamic_title));
        addFooterView(mFooterView, null, false);
    }

    /**
     * 加载数据
     */
    public void initData() {
        if (mlistViewAdapter == null) {
            // set adapter
            mlistViewAdapter = new DynamicAdapter(mContext, true);
            setAdapter(mlistViewAdapter);
        }
        if (mlistViewAdapter.getCount() == 0) {
            loadDynamicOld();
        }
    }

    /**
     * 设置分享类型信息
     * 
     * @param statuses_type
     * @param filterID
     */
    public void setStatusesTypeInfo(int statuses_type, String filterID) {
        mStatusesType = statuses_type;
        mFilterID = filterID;
    }

    /**
     * 获取分享类型过滤条件
     * 
     * @return
     */
    private String getFilter() {
        String filter = "";

        switch (mStatusesType) {
            case STATUSES_TYPE_GROUP:
                filter = "?group_id=" + mFilterID;
                break;
            case STATUSES_TYPE_USER:
                filter = "?user_id=" + mFilterID;
                break;
            default:
                break;
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
                    if (cacheDynamicInfo != null && cacheDynamicInfo.size() > 0)
                    {
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

                                onRefreshComplete();

                            } else {
                                mlistViewAdapter.addItems(resultInfoArray);
                                if (resultInfoArray.size() != PAGESIZE_DOWN) {
                                    // 已经没有旧数据
                                    inBottom = true;
                                }
                            }
                            mIsLoading = false;
                            mlistViewAdapter.notifyDataSetChanged();
                        }
                    });

                    // delete items
                    if (resultInfoArray_Deleted.size() > 0) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mlistViewAdapter.deleteItems(resultInfoArray_Deleted);
                                mIsLoading = false;
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
                        //
                        // // 群组等动态，不缓存
                        // // if (getFilter().length() != 0) {
                        // // for (DynamicItemInfo item : resultInfoArray)
                        // // mDynamicDB.insertDynamic(item, true);
                        // // }
                    }
                }
            }
        }.start();
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

    private OnItemLongClickListener mOnLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> paramAdapterView,
                View paramView, int paramInt, long paramLong) {
            mDynamicItemInfo = (DynamicItemInfo)mlistViewAdapter
                    .getItem(paramInt - getHeaderViewsCount());

            if (mDynamicItemInfo == null) {
                return false;
            }
            boolean isMine = GlobalUserInfo.getUID().equals(String.valueOf(mDynamicItemInfo.uid));

            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
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
                        Intent i = new Intent(mContext, Statuses_Send_Activity.class);
                        i.setAction(Statuses_Send_Activity.ACTION_RETWEET);
                        i.putExtra(GlobalUserInfo.PARAM_STATUSES_ID, mDynamicItemInfo.id);

                        ((Activity)mContext).startActivityForResult(i, 0);
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
            int itemIndex = arg2 - getHeaderViewsCount();
            if (itemIndex < 0) {
                return;
            }
            mDynamicItemInfo = (DynamicItemInfo)mlistViewAdapter.getItem(itemIndex);

            // 发送中的不能点击
            if (mDynamicItemInfo.sendStatus == DynamicItemInfo.SEND_PROCESS)
                return;

            Intent i = null;
            i = new Intent(mContext, Statuses_Comment_Activity.class);
            i.putExtra(GlobalUserInfo.PARAM_STATUSES_ID, mDynamicItemInfo.id);

            // 可在个人主页进行评论 回复 赞 ==操作
            ((Activity)mContext).startActivityForResult(i, 0);
        }
    };

    private void setFootText(String str) {
        mFootText.setText(str);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            cn.com.nd.momo.util.Utils.hideWaitDialog();

            // json
            SdkResult result = (SdkResult)(msg.obj);
            int ret = result.ret;

            if (ret == DynamicMgr.HTTP_OK) {
                switch (msg.what) {
                    case DynamicMgr.MSG_GET_BROADCAST:
                        mIsLoading = false;
                        setFootText(mContext.getString(R.string.dynamic_more));

                        mlistViewAdapter.notifyDataSetChanged();
                        
                        if (mLoadingDirect.equals("up")) {
                            onRefreshComplete(mContext
                                    .getString(R.string.dynamic_refresh_last_time)
                                    + DynamicInfo.getTimeFormat(
                                            "yyyy-MM-dd HH:mm",
                                            System.currentTimeMillis()));
                            StatusesListView.this.setSelection(0);
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
                    mIsLoading = false;
                }

                if (ret == 0) {
                    // 无网络 直接返回
                    String net = Utils
                            .getActiveNetWorkName(mContext);
                    if (net == null) {
                        onRefreshComplete(mContext
                                .getString(R.string.msg_group_sync_no_connection_found));
                        return;
                    }
                }

                switch (msg.what) {
                    case DynamicMgr.MSG_GET_BROADCAST:
                    default:
                        onRefreshComplete(result.ret + " " + result.response);
                        break;
                }
            }
            mIsLoading = false;
        }
    };

    /**
     * 隐藏分享
     */
    private void postHide() {
        new Thread() {
            public void run() {
                SdkResult result = new SdkResult();
                try {
                    StatusesManager.hide(mDynamicItemInfo.id, mDynamicItemInfo.hided);

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
     * 删除分享
     */
    private void postDeleteStatuses() {
        new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.dynamic_isdelete_comment))
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(mContext.getString(R.string.txt_ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                cn.com.nd.momo.util.Utils.showWaitDialog(
                                        mContext.getString(R.string.txt_wait),
                                        mContext);
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
                .setNegativeButton(mContext.getString(R.string.txt_cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.dismiss();
                            }
                        }).show();
    }
    
    
    private void sendMessage(int what, SdkResult result) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    @Override
    public void onRefresh(boolean needToast) {
        loadDynamicNew();
    }

}

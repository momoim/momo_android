
package cn.com.nd.momo.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.model.DraftMgr;
import cn.com.nd.momo.model.DynamicDB.DraftInfo;
import cn.com.nd.momo.model.DynamicInfo;
import cn.com.nd.momo.util.NotifyProgress;

/**
 * 草稿箱
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public class Statuses_Draft_Activity extends Activity implements OnClickListener {
    private ListView mlistView;

    private DynamicListdapter mlistViewAdapter;

    private ImageButton btnClear;

    private TextView btnTitle;

    private DynamicBroadcastReceiver mDynamicBroadcastReceiver = new DynamicBroadcastReceiver(this);

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
                long draftID = intent.getLongExtra(DraftMgr.DRAFT_ID, 0);
                if (intent.getAction().equals(NotifyProgress.ACTION_SUCCEED)) {
                    // 点击刷新
                    DraftMgr.instance().deleteDraft(draftID);
                    mlistViewAdapter.delItem(draftID);
                    Log.i("draft process delete" + draftID);
                    mlistViewAdapter.notifyDataSetChanged();
                    break;
                }
                if (intent.getAction().equals(NotifyProgress.ACTION_FAIL)) {
                    DraftInfo draftinfo = (DraftInfo)mlistViewAdapter.getItemByid(draftID);
                    if (draftinfo != null) {
                        draftinfo.sending = false;
                    }
                    mlistViewAdapter.notifyDataSetChanged();
                    break;
                }
                if (intent.getAction().equals(NotifyProgress.ACTION_PROCESS)) {

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

    /**
     * main--------------------------------------------------------------------
     * --------
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.statuses_draft);

        findViewById(R.id.btn_dynamic_all).setVisibility(View.INVISIBLE);

        btnClear = (ImageButton)findViewById(R.id.btn_dynamic_comment);
        btnClear.setImageResource(R.drawable.dynamic_clear);
        // TODO
        // btnClear.setText("清空草稿箱");
        btnClear.setOnClickListener(this);
        findViewById(R.id.btn_dynamic_top).setVisibility(View.GONE);

        btnTitle = (TextView)findViewById(R.id.draft_count);
        btnTitle.setVisibility(View.VISIBLE);

        findViewById(R.id.list_dynimic).setVisibility(View.GONE);
        mlistView = (ListView)findViewById(R.id.list_resend);
        mlistView.setVisibility(View.VISIBLE);

        // mlistView.setOnItemLongClickListener(mOnLongClickListener);

        // set adapter
        mlistViewAdapter = new DynamicListdapter();
        mlistView.setAdapter(mlistViewAdapter);
        mlistView.setOnItemClickListener(mOnItemClickListener);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            DraftInfo info = (DraftInfo)mlistViewAdapter.getItem(arg2
                    - mlistView.getHeaderViewsCount());

            mDraftInfo = info;
            DraftMgr.instance().deleteDraft(info.id);
            mlistViewAdapter.notifyDataSetChanged();
            // resend(info);
            Intent i = new Intent(Statuses_Draft_Activity.this, Statuses_Send_Activity.class);
            i.setAction(Statuses_Send_Activity.ACTION_DRAFT);
            startActivity(i);

            finish();
        }
    };

    public static DraftInfo mDraftInfo;

    // private void resend(DraftInfo info){
    // DynamicSearchActivity.prepare();
    // DynamicPoster mDyanmicPoster = new
    // DynamicPoster(DynamicSendActivity.this);
    // info.sending = true;
    // mDyanmicPoster.mParamContent = info.content;
    // mDyanmicPoster.mParamRetweetId = info.objid;
    // GroupInfo ginfo = new GroupInfo();
    // ginfo.gid = info.groupid;
    // mDyanmicPoster.mParamGroupInfo = ginfo;
    // mDyanmicPoster.mDraftid = info.id;
    //
    // if (info.images != null
    // && info.images.length() > 0) {
    // try {
    // JSONArray imagesArray = new JSONArray(
    // info.images);
    // for (int j = 0; j < imagesArray.length(); ++j) {
    // mDyanmicPoster.addPhoto(new PhotoUpload( new
    // StartForResults.PickData(imagesArray.get(j).toString())));
    // }
    // } catch (Exception e) {
    // }
    // }
    //
    // Intent brodcastIntent = new Intent(NotifyProgress.ACTION_PROCESS);
    //
    // brodcastIntent.putExtra(DraftMgr.DRAFT_ID, info.id);
    // this.sendBroadcast(brodcastIntent);
    //
    // mDyanmicPoster.postBroadcast();
    // mlistViewAdapter.notifyDataSetChanged();
    // }
    private OnItemLongClickListener mOnLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> paramAdapterView,
                View paramView, int paramInt, long paramLong) {
            return false;
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.i("onPause");
        super.onPause();
    }

    @Override
    protected void onStart() {
        mDynamicBroadcastReceiver.registerAction(NotifyProgress.ACTION_SUCCEED);
        mDynamicBroadcastReceiver.registerAction(NotifyProgress.ACTION_FAIL);
        mDynamicBroadcastReceiver.registerAction(NotifyProgress.ACTION_PROCESS);
        super.onStart();
    }

    @Override
    protected void onStop() {
        this.unregisterReceiver(mDynamicBroadcastReceiver);
        super.onStop();
    }

    @Override
    protected void onResume() {
        mlistViewAdapter.initData();
        mlistViewAdapter.notifyDataSetChanged();
        super.onResume();
    }

    // 界面按钮监听
    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_dynamic_comment:
                /**
                 * 退出程序
                 */
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dynamic_clear_draft))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(getString(R.string.txt_ok),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mlistViewAdapter.clear();
                                        mlistViewAdapter.notifyDataSetChanged();
                                    }
                                })
                        .setNegativeButton(getString(R.string.txt_cancel),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }).show();
                break;
            default:
                break;
        }
    }

    // private Handler mHandler = new Handler() {
    // public void handleMessage(Message msg) {
    // // 非json处理
    // switch (msg.what) {
    // case DynamicMgr.MSG_DOWNLOAD_AVATAR:
    // mlistViewAdapter.notifyDataSetChanged();
    // break;
    // }
    // }
    // };
    public class DynamicListdapter extends BaseAdapter {
        private ArrayList<DraftInfo> mItems = new ArrayList<DraftInfo>();

        public void initData() {
            mItems = DraftMgr.instance().getDraft();
            btnTitle.setText("草稿箱(" + getCount() + ")");
        }

        // private void addItem(DraftInfo item) {
        // Message msg = new Message();
        // msg.what = 0;
        // msg.obj = item;
        // mAddHandler.sendMessage(msg);
        // }
        //
        // private void addItem(int at, DraftInfo item, boolean modiDB) {
        // Message msg = new Message();
        // msg.what = 1;
        // msg.arg1 = at;
        // msg.obj = item;
        // mAddHandler.sendMessage(msg);
        // }
        // private Handler mAddHandler = new Handler() {
        // public void handleMessage(Message msg) {
        // DraftInfo item = (DraftInfo) msg.obj;
        // mItems.add(item);
        // DynamicListdapter.this.notifyDataSetChanged();
        // }
        // };

        public int isExist(long draftID) {
            for (int i = 0; i < mItems.size(); ++i) {
                if (mItems.get(i).id == draftID) {
                    return i;
                }
            }
            return -1;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        public DynamicListdapter() {
        }

        @Override
        public Object getItem(int position) {
            Log.i("getItem" + position);
            return mItems.get(position);
        }

        public Object getItemByid(long draftID) {
            int index = isExist(draftID);
            if (index != -1) {
                return mItems.get(index);
            }
            return null;

        }

        private void delItem(long id) {
            int index = isExist(id);
            if (index != -1) {
                mItems.remove(index);
            }
            btnTitle.setText("草稿箱(" + getCount() + ")");
        }

        private void clear() {
            mItems.clear();
            DraftMgr.instance().deleteAll();
            btnTitle.setText("草稿箱(" + getCount() + ")");
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id;
        }

        class ViewHold {
            // ImageView avatar;
            // TextView gname;
            TextView name;

            TextView time;

            TextView content;
            // TextView commentCount;
            // TextView commentLast;
        };

        // private FriendInfo getFriend(long id) {
        // return DynamicMgr.getInstance().getItem(id);
        // }

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            Log.i("getView" + position);

            ViewHold hold;
            // find view
            if (convertView == null) {
                convertView = View.inflate(Statuses_Draft_Activity.this,
                        R.layout.dynamic_resend_list_item, null);

                hold = new ViewHold();
                hold.content = (TextView)convertView
                        .findViewById(R.id.dynamic_resend_item_content);
                hold.name = (TextView)convertView
                        .findViewById(R.id.dynamic_resend_item_name);
                hold.time = (TextView)convertView
                        .findViewById(R.id.dynamic_resend_item_time);

                convertView.setTag(hold);
            } else {
                hold = (ViewHold)convertView.getTag();
            }

            do {
                // normal
                final DraftInfo itemInfo = (DraftInfo)getItem(position);
                // GroupInfo gInfo =
                // DynamicMgr.getInstance().getGroupItem(itemInfo.groupid);

                // hold.gname.setText(gInfo!=null?gInfo.gname:"");
                // hold.gname.setVisibility(gInfo!=null?View.VISIBLE:View.GONE);

                Log.i("itemInfo.content" + itemInfo.content);
                // hold.content.setVisibility(View.GONE);
                // hold.name.setTextSize(14f);
                // hold.name.setTextColor(Color.BLACK);
                // hold.name.setText(GlobalUserInfo.getName());
                hold.content.setText(Html.fromHtml(itemInfo.content));
                hold.name.setText(itemInfo.sending ? getString(R.string.dynamic_sending)
                        : getString(R.string.dynamic_sendfail));
                int imageFlagId = 0;
                if (itemInfo.images.length() > 0) {
                    imageFlagId = R.drawable.dynamic_list_item_image;
                }
                // if (itemInfo.images.size()>1) {
                // imageFlagId = R.drawable.dynamic_list_item_images;
                // }
                // hold.name.setCompoundDrawablesWithIntrinsicBounds(0, 0,
                // imageFlagId, 0);
                hold.time.setText(DynamicInfo.getTime(itemInfo.crateData * 1000));
                hold.time.setCompoundDrawablesWithIntrinsicBounds(imageFlagId, 0, 0, 0);
                convertView.setBackgroundColor(itemInfo.sending ? getResources().getColor(
                        R.color.blue_block) : getResources().getColor(R.color.transparent));
                // ((View)hold.avatar.getParent()).setVisibility(View.GONE);

            } while (false);

            return convertView;
        }
    }

}

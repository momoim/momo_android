
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.Calendar;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.statuses.StatusesManager;
import cn.com.nd.momo.api.types.Attachment;
import cn.com.nd.momo.api.util.BitmapToolkit.BitmapMemoryMgr;
import cn.com.nd.momo.api.util.DateFormater;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.AbsSdk.SdkResult;
import cn.com.nd.momo.model.DynamicDB;
import cn.com.nd.momo.model.DynamicDB.CommentInfo;
import cn.com.nd.momo.model.DynamicInfo;
import cn.com.nd.momo.model.DynamicItemInfo;
import cn.com.nd.momo.model.DynamicMgr;
import cn.com.nd.momo.api.DynamicPoster;
import cn.com.nd.momo.api.DynamicSdk;
import cn.com.nd.momo.util.NotifyProgress;
import cn.com.nd.momo.view.DynamicListItemUI;
import cn.com.nd.momo.util.TalkHistoryAdapter;
import cn.com.nd.momo.util.TextViewUtil;
import cn.com.nd.momo.view.EditTextEx;
import cn.com.nd.momo.view.SmileySelector;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.util.Utils;
import cn.com.nd.momo.view.CustomImageView;
import cn.com.nd.momo.view.PullToRefreshListView;

/**
 * 评论列表
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public class Statuses_Comment_Activity extends Activity implements
        OnClickListener {
    private final static String TAG = "DynamicHomePageActivity";

    private DynamicItemInfo mDynamicInfo;

    private View mCommentArea;

    private EditTextEx mTxtCommentEdit;

    private SmileySelector mSmileySelector;

    private View mSend;

    public static CommentInfo mCommentInfo = null;

    public BitmapMemoryMgr mBitmapMemoryMgr = new BitmapMemoryMgr();

    private CommentListAdapter mlistViewAdapter;

    private PullToRefreshListView mlistView;

    private View mFooterView;

    private DynamicListItemUI.ViewHold mHold;

    private String mCurrentReplyName = "";

    private DynamicListItemUI mDynamicListItemUI = new DynamicListItemUI();

    private DynamicBroadcastReceiver mDynamicBroadcastReceiver = new DynamicBroadcastReceiver(
            this);

    private FrameLayout mLayoutHeader = null;

    private ImageButton btnPraise = null;

    private ImageButton btnForward = null;    
    
    private boolean hasMeasured = false;
    
    public class CommentListAdapter extends BaseAdapter {
        private ArrayList<CommentItemInfo> mItems = new ArrayList<CommentItemInfo>();

        private void addItems(ArrayList<CommentItemInfo> items) {
            Message msg = new Message();
            msg.obj = items;
            msg.what = 0;
            mAddHandler.sendMessage(msg);
        }

        private void addItemsAtFront(ArrayList<CommentItemInfo> items) {
            Message msg = new Message();
            msg.obj = items;
            msg.what = 1;
            mAddHandler.sendMessage(msg);
        }

        private Handler mAddHandler = new Handler() {
            @Override
            public synchronized void handleMessage(Message msg) {
                @SuppressWarnings("unchecked")
                ArrayList<CommentItemInfo> items = (ArrayList<CommentItemInfo>)msg.obj;

                switch (msg.what) {
                    case 0:
                        mItems.addAll(items);
                        // mlistView.setSelection(mItems.size()
                        // - mlistView.getHeaderViewsCount());
                        break;
                    case 1:
                        if (items.size() < 20) {
                            isNone = true;
                            Log.i(TAG, "is null");
                        }

                        // ((TextView)mHeaderView.findViewById(R.id.dynamic_title)).setText(getString(R.string.dynamic_oldpage));

                        if (mlistView.getAdapter() == null) {
                            mlistView.setAdapter(mlistViewAdapter);
                            // findViewById(R.id.loading_comments).setVisibility(
                            // View.GONE);
                        }

                        mItems.addAll(0, items);
                        // mlistView.setSelection(items.size()
                        // - mlistView.getHeaderViewsCount());
                        break;
                }

                CommentListAdapter.this.notifyDataSetChanged();
            }
        };

        @Override
        public int getCount() {
            return mItems.size();
        }

        public long getLastID() {
            if (mItems.size() == 0)
                return 0;
            return mItems.get(mItems.size() - 1).createTime;
        }

        public long getFirstID() {
            if (mItems.size() == 0)
                return 0;
            return mItems.get(0).createTime;
        }

        @Override
        public Object getItem(int position) {
            if (position > mItems.size() - 1) {
                return null;
            }
            if (position < 0)
                position = 0;
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public synchronized void deleteItem(String id) {
            CommentItemInfo item;
            for (int i = 0; i < mItems.size(); i++) {
                item = mItems.get(i);
                if (item.id.equalsIgnoreCase(id)) {
                    mItems.remove(i);
                }
            }
        }

        class CommentListViewHold {
            CustomImageView avatar;

            ImageView iconSource;

            TextView image;

            TextView name;

            TextView time;

            TextView content;

            TextView likeCount;

            View lastComment;
        };

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            Log.i(TAG, "getView" + position);

            CommentListViewHold hold;
            // find view
            if (convertView == null) {
                convertView = View.inflate(Statuses_Comment_Activity.this,
                        R.layout.statuses_comment_list, null);

                hold = new CommentListViewHold();
                hold.avatar = (CustomImageView)convertView
                        .findViewById(R.id.dynamic_list_item_avatar);
                hold.content = (TextView)convertView
                        .findViewById(R.id.dynamic_list_item_content);

                mDynamicListItemUI.linkiFy(hold.content);

                hold.name = (TextView)convertView
                        .findViewById(R.id.dynamic_list_item_name);
                hold.time = (TextView)convertView
                        .findViewById(R.id.dynamic_list_item_time);

                convertView.setTag(hold);
            } else {
                hold = (CommentListViewHold)convertView.getTag();
            }

            Log.i(TAG,
                    "HeaderViewsCount:" + position + "__"
                            + mlistView.getHeaderViewsCount());
            final CommentItemInfo itemInfo = (CommentItemInfo)getItem(position);

            OnClickListener onReplyClick = new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // 不能回复自己
                    if (String.valueOf(itemInfo.uid).equals(
                            GlobalUserInfo.getUID()))
                        return;

                    mCommentInfo = itemInfo;
                    
                    if (mCommentArea.getVisibility() == View.GONE) {
                        switichTitleVisible();
                    }

                    mTxtCommentEdit.setText("");
                    addAt(mCommentInfo.uid, mCommentInfo.realName);
                    mTxtCommentEdit.requestFocus();
                }
            };

            hold.content.setOnClickListener(onReplyClick);
            convertView.setOnClickListener(onReplyClick);

            hold.avatar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    //todo
//                    Context context = Statuses_Comment_Activity.this;
//
//                    Intent intent = new Intent();
//                    User user = new User();
//                    user.setId(String.valueOf(itemInfo.uid));
//                    user.setName(itemInfo.realName);
//                    intent.setAction(ContactFragmentActivity.ACTION_IM);
//                    UserParser up = new UserParser();
//                    String jsonString = "";
//                    try {
//                        JSONObject jsonObject = up.toJSONObject(user);
//                        jsonString = jsonObject.toString();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    intent.putExtra(ContactFragmentActivity.EXTRA_USER, jsonString);
//                    intent.setClass(context, ContactFragmentActivity.class);
//                    context.startActivity(intent);

                }
            });
            hold.name.setText(itemInfo.realName + ": ");
            hold.time.setText(itemInfo.date);

            hold.content.setText(TalkHistoryAdapter.addSmileySpans(
                    getApplicationContext(), Html.fromHtml(itemInfo.content + " ")));

            mDynamicListItemUI.linkiFy(hold.content);

            hold.avatar.setCustomImage(itemInfo.uid, itemInfo.avatar,
                    Statuses_Activity.mapCache);

            return convertView;
        }
    }

    private void getDynamicContent() {
        new Thread() {
            @Override
            public void run() {
                String sid = GlobalUserInfo.getSessionID();
                DynamicSdk sdk = new DynamicSdk();
                try {
                    SdkResult result = sdk.getDynamicContentOpt(sid,
                            mDynamicInfo.id);
                	DynamicItemInfo item = (DynamicItemInfo) result.object;
                	if(item.isLongText) {
                		try {
                			String longText = MoMoHttpApi.getFeedLongText(mDynamicInfo.id);
                			if(!TextUtils.isEmpty(longText)) {
                				item.text = longText;
                				JSONObject json = new JSONObject(item.json);
                				if(json.has("text")) {
                					json.put("text", longText);
                				}
                			}
                		} catch(Exception e) {
                			//
                		}
                	}
                    	
                    sendMessage(DynamicMgr.MSG_GET_SINGLE_BROADCAST, result);
                } catch (Exception e) {
                    Log.e(TAG, "getDynamicContent " + e.toString());
                }
            }
        }.start();
    }

    private void setDes() {
        Log.i(TAG, "setDes" + mDynamicInfo.avatarBmp);
        // 通用初始化部分
        mDynamicListItemUI.init(mHold, mDynamicInfo, this, false,
                mHandler, null);
        // initBar(mHold.btnsBar, mDynamicInfo);
        mHold.commentLast.setVisibility(View.GONE);
        mDynamicListItemUI.linkiFy(mHold.content);
        // mDynamicListItemUI.linkiFy(mHold.likeCount);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_name:
                break;
            case R.id.friend_at:
                startAddAtActivity();
                break;
            case R.id.message_send_btn:
                DynamicPoster dp = new DynamicPoster(this,
                        System.currentTimeMillis());
                String replyID = mCommentInfo == null ? null : mCommentInfo.id;
                dp.mDynamicPostInfo.setParamReply(mDynamicInfo.id,
                        replyID);
                dp.postReply(mTxtCommentEdit.getText().toString());

                // add to main
                CommentItemInfo cinfo = new CommentItemInfo();
                cinfo.sendStatus = CommentItemInfo.SEND_PROCESS;
                cinfo.uid = Long.valueOf(GlobalUserInfo.getUID());
                cinfo.date = DynamicInfo.getTime(System.currentTimeMillis());
                cinfo.content = mTxtCommentEdit.getText().toString();
                // cinfo.feedId = String.valueOf(dp.mDynamicPostInfo.mDraftid);
                cinfo.realName = GlobalUserInfo.getName();
                cinfo.id = String.valueOf(dp.mDynamicPostInfo.getDraftID());

                mlistViewAdapter.mItems.add(cinfo);
                mlistViewAdapter.notifyDataSetChanged();

                mCommentInfo = null;
                mTxtCommentEdit.setText("");
                cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), mTxtCommentEdit);
                mlistView.setSelection(mlistView.getCount() - 1);
                break;
            case R.id.btn_praise:
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        SdkResult result = new SdkResult();
                        try {
                            StatusesManager.praise(mDynamicInfo.id);
                            result.ret = HttpStatus.SC_OK;
                        } catch (MoMoException e) {
                            result.ret = e.getCode();
                            result.response = e.getSimpleMsg();
                        }
                        sendMessage(DynamicMgr.MSG_POST_PRISE, result);
                    }
                }).run();
                break;
            case R.id.btn_forward:
                Intent i = new Intent(Statuses_Comment_Activity.this, Statuses_Send_Activity.class);
                i.setAction(Statuses_Send_Activity.ACTION_RETWEET);
                if (mDynamicInfo.gid > 0) {
                    i.putExtra(Statuses_Send_Activity.PARAM_GROUPID,
                            mDynamicInfo.gid);
                    i.putExtra(Statuses_Send_Activity.PARAM_GROUPNAME,
                            mDynamicInfo.gname);
                }
                i.putExtra(GlobalUserInfo.PARAM_STATUSES_ID, mDynamicInfo.id);
                
                startActivityForResult(i, DynamicMgr.MSG_POST_COMMENT);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult" + requestCode);
        if (resultCode == Activity.RESULT_OK) {
            String name = data.getStringExtra("name");
            switch (requestCode) {
                case REQUEST_PICK:
                    mTxtCommentEdit.append(name);
                    break;
                case SmileySelector.SMILEY_PICK_WITH_DATA:
                    onSelect(data.getExtras().getString(SmileySelector.INTENT_SMILEY_KEY));
                    break;
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static final int HTTP_OK = 200;

    // private ImageView mArrow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.statuses_comment);

        // set dynamic info
        // mDynamicInfo = Statuses_Activity.mDynamicItemInfo;
        mDynamicInfo = new DynamicItemInfo();
        Intent intent = getIntent();
        if (intent != null) {
            mDynamicInfo.id = intent.getStringExtra(GlobalUserInfo.PARAM_STATUSES_ID);
        }

        getDynamicContent();
        
        btnPraise = (ImageButton)findViewById(R.id.btn_praise);

        btnForward = (ImageButton)findViewById(R.id.btn_forward);
        btnForward.setOnClickListener(this);

        // init list view
        LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        mLayoutHeader = (FrameLayout)mInflater.inflate(R.layout.statuses_comment_header, null);
        
        mHold = mDynamicListItemUI.createHold(mLayoutHeader, this);
        mHold.images.setVisibility(View.VISIBLE);
        ((View)mHold.images.getParent()).setVisibility(View.GONE);

        // get content

        mlistView = (PullToRefreshListView)findViewById(R.id.list_comment);
        mlistView.addHeaderView(mLayoutHeader);
        mFooterView = View.inflate(this, R.layout.dynamic_list_footer, null);
        mlistView.addFooterView(mFooterView, null, false);
        // mlistView.setonRefreshListener(this);

        mFooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View paramView) {
                // load new
                loadDataByPage("pre");
            }
        });

        mlistViewAdapter = new CommentListAdapter();

        mHold.avatar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switichTitleVisible();
            }
        });

        // 评论输入框
        mCommentArea = findViewById(R.id.sending_area);
        initCommentArea();

        mDynamicBroadcastReceiver
                .registerAction(NotifyProgress.ACTION_COMMENT_FAIL);
        mDynamicBroadcastReceiver
                .registerAction(NotifyProgress.ACTION_COMMENT_SUCCEED);
    }

    private void switichTitleVisible() {
        // setTitleVisible(mHold.btnsBar.getVisibility() == View.VISIBLE);
    }

    private void setTitleVisible(boolean isVisible) {
        Log.i(TAG, "setTitleVisible" + " " + isVisible);
        if (mDynamicInfo.images.size() > 0)
            ((View)mHold.images.getParent())
                    .setVisibility(isVisible ? View.VISIBLE : View.GONE);
    }

    private void initCommentArea() {
        // mCommentArea.findViewById(R.id.friend_at).setOnClickListener(this);
        mSend = mCommentArea.findViewById(R.id.message_send_btn);
        mSend.setOnClickListener(this);
        mTxtCommentEdit = (EditTextEx)(mCommentArea
                .findViewById(R.id.message_content));
        mTxtCommentEdit.setOnClickListener(this);
        mSend.setEnabled(false);
        mTxtCommentEdit.addTextChangedListener(new TextWatcher() {
            // 文字改变事件监听
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) { // btnKeyboard.setText(String.valueOf((140 -
                                 // s.length())));
                Log.i(TAG, s + "-" + start + "-" + before + "-" + count);
                // 当无图片 且 无文字时, 不让发布 ,
                String mReplyContent = s.toString().replace(" ", "");
                Log.i(TAG, mReplyContent + ":" + mCurrentReplyName);
                boolean notInput = mReplyContent
                        .equals("@" + mCurrentReplyName);

                if (s.length() == 0 || notInput) {
                    mSend.setEnabled(false);
                } else if (!mSend.isEnabled()) {
                    mSend.setEnabled(true);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                    int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        ((View)mTxtCommentEdit.getParent()).requestFocus();

        mSmileySelector = (SmileySelector)this
                .findViewById(R.id.smiley_selector);

        final TextView friendAT = new TextView(this);
        friendAT.setText("@好友");
        friendAT.setTextSize(16);
        friendAT.setGravity(Gravity.CENTER);
        friendAT.setTextColor(Color.BLACK);
        mSmileySelector.setFooterView(friendAT);
        friendAT.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startAddAtActivity();

                if (friendAT.getTag() != null) {
                    AlertDialog dialog = (AlertDialog)(friendAT.getTag());
                    if (dialog.isShowing()) {
                        dialog.dismiss();
                    }
                }
            }
        });
    }

    private void addAt(long id, String name) {
        mCurrentReplyName = mCommentInfo.realName;
        mTxtCommentEdit.append(SelectorActivity.addUser(id, name) + " ");
    }

    private final static int REQUEST_PICK = 100;

    private void startAddAtActivity() {
        Intent i = new Intent(this, SelectorActivity.class);
        // i.setAction(DynamicSearchActivity.ACTION_SINGLE_PICK);
        startActivityForResult(i, REQUEST_PICK);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(mDynamicBroadcastReceiver);
        mBitmapMemoryMgr.releaseAllMemory();
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * receiver----------------------------------------------------------------
     * -------
     */
    public class DynamicBroadcastReceiver extends BroadcastReceiver {

        private Context context;

        private DynamicBroadcastReceiver mReceiver;

        public DynamicBroadcastReceiver(Context c) {
            Log.v(TAG, "DynamicBroadcastReceiver");
            context = c;
            // to instance it
            mReceiver = this;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            do {
                long commentID = intent.getLongExtra(
                        CommentItemInfo.COMMENT_ID, 0);
                String commentContent = intent
                        .getStringExtra(CommentItemInfo.COMMENT_CONTENT);

                if (intent.getAction().equals(
                        NotifyProgress.ACTION_COMMENT_SUCCEED)) {
                    // 点击刷新
                    mlistViewAdapter.deleteItem(String.valueOf(commentID));
                    mlistViewAdapter.notifyDataSetChanged();
                    loadDataByPage("pre");
                    Statuses_Comment_Activity.this
                            .setResult(DynamicMgr.MSG_POST_COMMENT);
                    mDynamicInfo.commentCount++;
                    setDes();
                    break;
                }
                if (intent.getAction().equals(
                        NotifyProgress.ACTION_COMMENT_FAIL)) {
                    mlistViewAdapter.deleteItem(String.valueOf(commentID));
                    mlistViewAdapter.notifyDataSetChanged();
                    // 提示
                    Toast.makeText(
                            Statuses_Comment_Activity.this,
                            commentContent
                                    + getString(R.string.dynamic_send_fail), 0)
                            .show();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown");
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Utils.hideWaitDialog();

            SdkResult result = (SdkResult)msg.obj;
            if (result.ret == HTTP_OK) {
                switch (msg.what) {
                    case DynamicMgr.MSG_GETCOMMENT_CACHE:
                        break;
                    case DynamicMgr.MSG_GET_SINGLE_BROADCAST:
                        mDynamicInfo = (DynamicItemInfo)(((SdkResult)(msg.obj)).object);
                        if (mDynamicInfo.allowPraise == 1) {
                            btnPraise.setImageResource(R.drawable.share_topbar_praise);
                            btnPraise.setEnabled(true);
                            btnPraise.setOnClickListener(Statuses_Comment_Activity.this);
                        } else {
                            btnPraise.setImageResource(R.drawable.share_praise_disable);
                            btnPraise.setEnabled(false);
                        }
                        setTitleVisible(true);
                        setDes();

                        loadDataByPage("next");
                        
                        if (mDynamicInfo.images.size() > 0) {
                            // 只显示第1张图片，且详情里面显示大图
                            ArrayList<String> imagesBig = new ArrayList<String>();
                            for (String imageUrl : mDynamicInfo.images) {
                                String url = imageUrl.replace("_80", "_780");
                                url = url.replace("_160", "_780");
                                url = url.replace("_130", "_780");
                                imagesBig.add(url);
                            }

                            final Attachment attachment;
                            if (mDynamicInfo.attachmentList.size() > 0) {
                                attachment = mDynamicInfo.attachmentList.get(0);
                            } else {
                                attachment = null;
                            }

                            if (attachment != null && attachment.getWidth() > 0 && attachment.getHeight() > 0) {
                                ViewTreeObserver obsView = mHold.images.getViewTreeObserver();
                                obsView.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                                    
                                    @Override
                                    public boolean onPreDraw() {

                                        if (!hasMeasured) {
                                            int width = mHold.images.getMeasuredWidth();
                                            if (width > attachment.getWidth()) {
                                                width = attachment.getWidth();
                                            }
                                            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                                                    width, width * attachment.getHeight() / attachment.getWidth());
                                            mHold.images.getChildAt(0).setLayoutParams(lParams);
                                            
                                            hasMeasured = true;
                                        }
                                        return true;
                                    }
                                });
                                                                      
                            } else {
                                LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                                        LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                                mHold.images.getChildAt(0).setLayoutParams(lParams);
                            }

                            WholeImageActivity.Thumbnail.loadImages(
                                    Statuses_Comment_Activity.this,
                                    mHold.images, imagesBig, mDynamicInfo.imageBmps,
                                    0);

                        } else {
                            ((View)mHold.images.getParent())
                                    .setVisibility(View.GONE);
                        }
                                                
                        break;
                    case DynamicMgr.MSG_GETCOMMENT:
                        loadStatus(false);
                        // 显示评论
                        setDes();
                        break;
                    case DynamicMgr.MSG_POST_PRISE:
                        mDynamicInfo.likeList = "我"
                                + (mDynamicInfo.likeList.length() == 0 ? "觉得这挺赞的"
                                        : "和") + mDynamicInfo.likeList;
                        mDynamicInfo.liked = 1;
                        setDes();

                        btnPraise.setEnabled(false);
                        btnPraise.setImageResource(R.drawable.share_praise_disable);
                        setResult(DynamicMgr.MSG_POST_PRISE);
                        break;
                    // case DynamicMgr.MSG_POST_FAV:
                    // break;
                    case DynamicMgr.MSG_POST_DELETE_COMMENT:
                        break;
                    case DynamicMgr.MSG_POST_DELETE_DYNAMIC:
                        setResult(DynamicMgr.MSG_POST_DELETE_DYNAMIC);
                        finish();
                        break;
                }
            } else {
                switch (msg.what) {
                    case DynamicMgr.MSG_GETCOMMENT:
                        loadStatus(false);
                        break;
                }

                try {
                    Toast.makeText(Statuses_Comment_Activity.this,
                            DynamicMgr.getErrorCode(result), 0).show();
                } catch (JSONException e) {
                    Log.i(TAG, e.toString());
                }

            }
        }
    };

    private void loadStatus(boolean start) {
        if (start) {
            ((TextView)mFooterView.findViewById(R.id.dynamic_title))
                    .setText(getString(R.string.dynamic_loading));
            mFooterView.findViewById(R.id.dynamic_loading)
                    .setVisibility(View.VISIBLE);
        } else {
            ((TextView)mFooterView.findViewById(R.id.dynamic_title))
                    .setText(getString(R.string.dynamic_refresh));
            mFooterView.findViewById(R.id.dynamic_loading)
                    .setVisibility(View.INVISIBLE);

            if (isNone) {
                // mlistView.setNothingToRefreshLabel("无更多评论", "共" +
                // mlistViewAdapter.getCount()
                // + "条评论");
                mlistView.onRefreshComplete("");
            } else {
                mlistView.onRefreshComplete("最后更新时间"
                        + DateFormater.GetFullTime(Calendar.getInstance()
                                .getTimeInMillis()));
            }

            if (isNone) {
                // TODO zgx 20120914
                // findViewById(R.id.loading_flag).setVisibility(View.GONE);
            }
        }
        mFooterView.setEnabled(!start);
    }

    private void loadDataByPage(final String loadFlag) {
        if (mIsLoading)
            return;
        loadStatus(true);

        new Thread() {
            @Override
            public void run() {
                mIsLoading = true;

                String startID = "0";
                startID = String
                        .valueOf(loadFlag.equals("pre") ? mlistViewAdapter
                                .getLastID() : mlistViewAdapter.getFirstID());

                SdkResult result = new SdkResult();
                try {
                    ArrayList<CommentItemInfo> items = StatusesManager.getComments(mDynamicInfo.id,
                            loadFlag, startID, 50);

                    if (loadFlag.equals("pre")) {
                        mlistViewAdapter.addItems(items);
                    } else {
                        mlistViewAdapter.addItemsAtFront(items);
                    }
                    result.ret = HttpStatus.SC_OK;
                } catch (MoMoException e) {
                    result.ret = e.getCode();
                }

                sendMessage(DynamicMgr.MSG_GETCOMMENT, result);
                mIsLoading = false;
            }
        }.start();

    }

    private boolean mIsLoading = false;

    private void sendMessage(int what, SdkResult result) {
        Message msg = new Message();
        msg.what = what;
        msg.obj = result;
        mHandler.sendMessage(msg);
    }

    public static class CommentItemInfo extends DynamicDB.CommentInfo {
        public static String COMMENT_ID = "commentID";

        public static String COMMENT_CONTENT = "commentContent";

        public final static int SEND_DONE = 0;

        public final static int SEND_PROCESS = 1;

        public final static int SEND_FAIL = 2;

        public int sendStatus = SEND_DONE;

        public Bitmap avatarBmp;

        public Boolean isdownloading = false;

        public void log(String tag) {
            Log.i(tag, this.toString());
        }

        public CommentItemInfo(CommentInfo info) {
            feedId = info.feedId;
            uid = info.uid;
            id = info.id;
            avatar = info.avatar;
            content = info.content;
            date = info.date;
            realName = info.realName;
        }

        public CommentItemInfo() {
        }
    }

    boolean isNone = false;;

    // @Override
    // public void onRefresh(boolean needToast) {
    // loadDataByPage("next");
    //
    // }

    private void onSelect(String smiley) {
        String current = mTxtCommentEdit.getText().toString();
        int nowSelectStart = mTxtCommentEdit.getSelectionStart();
        int nowSelectEnd = mTxtCommentEdit.getSelectionEnd();
        String pre = current.substring(0,
                mTxtCommentEdit.getSelectionEnd());
        String last = current.length() > 1 ? current.substring(
                mTxtCommentEdit.getSelectionEnd(), current.length())
                : "";
        String result = pre + smiley + last;

        SpannableStringBuilder emojiStr = TextViewUtil.addEmojiSmileySpans(
                Statuses_Comment_Activity.this, result,
                ImageSpan.ALIGN_BASELINE);
        mTxtCommentEdit.setText(emojiStr);

        if (((nowSelectStart == nowSelectEnd) && (nowSelectEnd == current
                .length())) || current.equals("")) {
            mTxtCommentEdit.setSelection(result.length());
        } else {
            if (nowSelectEnd != nowSelectStart) {
                mTxtCommentEdit.setSelection(nowSelectStart,
                        nowSelectEnd);
            } else {
                mTxtCommentEdit.setSelection(nowSelectStart
                        + smiley.length());
            }
        }
        Log.i(TAG, "onSmiley select"
                + mTxtCommentEdit.getText().toString());
    }
    
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hasMeasured = false;
    }
}

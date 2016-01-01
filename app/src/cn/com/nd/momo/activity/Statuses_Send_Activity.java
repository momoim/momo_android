
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.Statuses_Comment_Activity.CommentItemInfo;
import cn.com.nd.momo.adapters.GroupAdapter;
import cn.com.nd.momo.api.parsers.json.GroupParser;
import cn.com.nd.momo.api.parsers.json.UserParser;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.model.DynamicDB.DraftInfo;
import cn.com.nd.momo.model.DynamicDB;
import cn.com.nd.momo.model.DynamicMgr;
import cn.com.nd.momo.api.DynamicPoster;
import cn.com.nd.momo.api.DynamicPoster.PhotoUpload;
import cn.com.nd.momo.util.TextViewUtil;
import cn.com.nd.momo.view.EditTextEx;
import cn.com.nd.momo.view.SmileySelector;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.util.StartForResults;
import cn.com.nd.momo.util.StartForResults.PickData;

/**
 * 分享发布页面
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public class Statuses_Send_Activity extends Activity implements OnClickListener {

    public static final String ACTION_BROADCAST = "action_broadcast";

    public static final String ACTION_DRAFT = "action_draft";

    public static final String ACTION_COMMENT = "action_comment";

    public static final String ACTION_RETWEET = "action_retweet";

    public static final String ACTION_REPLY = "action_reply";

    public static final String ACTION_FEED = "action_feed";

    public static final String NEED_LOCATE = "need_locate";
    
    public static final String PARAM_GROUPID = "GROUPID";
    public static final String PARAM_GROUPNAME = "GROUPNAME";
    // define usage of this activity (in bundle)
    public static final String ACTIVITY_TYPE = "type";

    public static final int TYPE_FOR_MO_SHARE = 1;

    public static final int TYPE_FOR_MO_SMS = 2;

    private int mActivityType = 0;

    private ImageButton btnBroadcast;

    private ImageButton btnImage;

    private TextView btnKeyboard;

    private SmileySelector mSmileySelector;

    private ImageButton btnLocation;

    private EditTextEx txtBroadcast;

    private TextView txtTitle;

    private DynamicPoster mDynamicPoster;

    private TextView mUploadedProcess;

    /**
     * 用户UID, NAME缓存
     */
    private HashMap<String, String> userMap = new HashMap<String, String>();

    private LinearLayout btnGroupSelect;

    private CheckBox btnTitleIcon;

    private PopupWindow popupWindow;

    private ListView listViewGroup;

    private ArrayList<GroupInfo> groupList = new ArrayList<GroupInfo>();

    private GroupInfo mCurrentGroup = null;

    private static final int ID_GROUPALL = -1;

    private RelativeLayout barTitle = null;
    
    /**
     * fail 超时提示 maybe done resend done oncreate send disapteer done 保存 重复
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.statuses_send);
        
        DynamicDB.initInstance(this);
        
        // find create type for this activity
        mActivityType = this.getIntent().getIntExtra(ACTIVITY_TYPE, 0);
        
        if (mActivityType == TYPE_FOR_MO_SHARE) {
            SelectorActivity.prepare();
            Intent intentSearch = new Intent(Statuses_Send_Activity.this, SelectorActivity.class);
            intentSearch.setAction(SelectorActivity.ACTION_STATUSES_PICK);
            startActivityForResult(intentSearch, SelectorActivity.USER_PICKED);
        }

        mUploadedProcess = (TextView)findViewById(R.id.upload_attach);
        barTitle = (RelativeLayout)findViewById(R.id.bar_statuses_title);
        btnImage = (ImageButton)findViewById(R.id.btn_sync_sina);
        // 地理位置按钮
        btnLocation = (ImageButton)findViewById(R.id.btn_location);
        // mUploadedProcess.setOnClickListener(this);

        // 发布按钮
        btnBroadcast = (ImageButton)findViewById(R.id.btn_comment);
        btnBroadcast.setOnClickListener(this);
        btnBroadcast.setEnabled(false);
        btnBroadcast.setVisibility(View.VISIBLE);

        btnGroupSelect = (LinearLayout)findViewById(R.id.btn_group_select);
        btnGroupSelect.setOnClickListener(this);
        // 标题
        mCurrentGroup = initGroupAll();

        // 处理其他页面传过来的GROUP信息
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle data = intent.getExtras();
            if (data.containsKey(PARAM_GROUPID) && data.containsKey(PARAM_GROUPNAME)) {
                mCurrentGroup.setGroupID(data.getInt(PARAM_GROUPID));
                mCurrentGroup.setGroupName(data.getString(PARAM_GROUPNAME));
            }
        }
        
        txtTitle = (TextView)findViewById(R.id.txt_title);
        String groupName = mCurrentGroup.getGroupName();
        if (groupName.length() > 8) {
            groupName = groupName.substring(0, 8);
        }
        txtTitle.setText(groupName);

        btnTitleIcon = (CheckBox)findViewById(R.id.icon_title);

        // 文本编辑区域
        txtBroadcast = (EditTextEx)findViewById(R.id.dynamic_comment_text);
        txtBroadcast.requestFocus();

        mDynamicPoster = new DynamicPoster(Statuses_Send_Activity.this);
        txtBroadcast.addTextChangedListener(new TextWatcher() {
            // 文字改变事件监听
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                    int count) {
                // ((View)(findViewById(R.id.btn_ablum_camera).getParent())).setVisibility(((InputMethodManager)
                // getSystemService(INPUT_METHOD_SERVICE)).isActive()?View.GONE:View.VISIBLE);

                // btnKeyboard.setText(String.valueOf((140 - s.length())));
                Log.i(s + "-" + start + "-" + before + "-" + count);
                // 当无图片 且 无文字时, 不让发布 ,
                if ((s.length() == 0 && mDynamicPoster.getPhotoUploadArray().size() == 0)) {
                    btnBroadcast.setEnabled(false);
                }
                else if (!btnBroadcast.isEnabled()) {
                    btnBroadcast.setEnabled(true);
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

        mSmileySelector = (SmileySelector)this
                .findViewById(R.id.smiley_selector);

        adjustByAction();
        
        //get params from share
        initInfoFromShareIntent();
    }
        
    private boolean isShareSystem() {
    	boolean result = false;
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        
        if (Intent.ACTION_SEND.equals(action) && type != null) {
        	mActivityType = TYPE_FOR_MO_SHARE;
            if ("text/plain".equals(type)) {
            	result = true;
            } else if (type.startsWith("image/")) {
            	result = true;
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
            	result = true;
            }
        }
        return result;
    }
    
    private void initInfoFromShareIntent() {
    	// Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
        	mActivityType = TYPE_FOR_MO_SHARE;
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
        }
    }
    
    private void handleSendText(Intent intent) {
    	if(intent.hasExtra(Intent.EXTRA_TEXT)) {
    		this.txtBroadcast.setText(intent.getStringExtra(Intent.EXTRA_TEXT));
    	}
    }
    
    private void handleSendImage(Intent intent) {
    	if(intent.hasExtra(Intent.EXTRA_STREAM)) {
    		Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
    		addShareUriImage(uri);
    	}
    }
    
    private void handleSendMultipleImages(Intent intent) {
    	if(intent.hasExtra(Intent.EXTRA_STREAM)) {
    		ArrayList<Uri> uriList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    		for(Uri uri : uriList) {
    			this.addShareUriImage(uri);
    		}
    	}
    }
    
    private void addShareUriImage(Uri uri) {
    	String file = getFilePathFromUri(uri);
    	if(TextUtils.isEmpty(file)) {
    		return;
    	}
    	StartForResults.PickData pickData = new StartForResults.PickData();
    	pickData.localPath = file;
    	Log.i("addShareUriImage: " + file);
    	//pickData.thumbBmp = BitmapToolkit.loadLocalBitmapExactScaled(file, 80);
    	mDynamicPoster.addPhoto(new PhotoUpload(pickData));
    	setImageCount();
    	btnBroadcast.setEnabled(true);
    }
    
    private String getFilePathFromUri(Uri uri) {
    	String filePath = "";
    	String scheme = uri.getScheme(); 

    	if(scheme.equals("content")){
    		String[] projection = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,};
    	    Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
    	    cursor.moveToFirst(); // <--no more NPE
    	    int columnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
    	    filePath = cursor.getString(columnIndex);
    	    cursor.close();
    	} else if(scheme.equals("file")){
    	    filePath = uri.getPath();
    	    Log.d("Loading file " + filePath);
    	} else {
    	    Log.d("Failed to load URI " + uri.toString());
    	}
    	return filePath;
    }

    private void setImageCount() {
        int count = mDynamicPoster.getLocalUrls().size();
        String imageCount = count == 0 ? "" : String.valueOf(count);
        mUploadedProcess.setText(imageCount);

        if (txtBroadcast.length() == 0 && mDynamicPoster.getPhotoUploadArray().size() == 0) {
            btnBroadcast.setEnabled(false);
        }
    }

    // 退出程序监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i("onKeyDown" + event.getRepeatCount());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mDynamicPoster.mDynamicPostInfo.mParamContent = txtBroadcast.getText().toString();
            if (mCurrentGroup.getGroupID() > 0) {
                mDynamicPoster.mDynamicPostInfo.mParamGroupInfo = mCurrentGroup;
            }
            if (event.getRepeatCount() == 0
                    && (mDynamicPoster.mDynamicPostInfo.mParamContent.length() > 0 || mDynamicPoster
                            .getPhotoUploadArray().size() > 0)
                    && (getIntent().getAction().equals(ACTION_BROADCAST)
                            || getIntent().getAction().equals(ACTION_RETWEET) || getIntent()
                            .getAction().equals(ACTION_DRAFT))) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.dynamic_is_save_draft))
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setPositiveButton(getString(R.string.txt_save),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        mDynamicPoster.addDraft();
                                        Statuses_Send_Activity.this.finish();
                                    }
                                })
                        .setNegativeButton(getString(R.string.txt_nosave),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        Statuses_Send_Activity.this.finish();
                                    }
                                }).show();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 弹出菜单点击监听
     */
    OnItemClickListener groupListClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            GroupInfo selectedGroup = groupList.get(position);
            if (mCurrentGroup.getGroupID() != selectedGroup.getGroupID()) {
                mCurrentGroup = selectedGroup;

                // 选中其他群组，则重新刷新分享列表数据
                // mlistViewAdapter.removeAll();
                // mlistViewAdapter.notifyDataSetChanged();
                // loadDynamicNew();
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
            txtBroadcast.requestFocus();
        }
    };

    private void showGroup() {
        if (btnTitleIcon.isChecked()) {
            popupWindow.dismiss();
        }

        cn.com.nd.momo.util.Utils.hideKeyboard(getApplicationContext(), txtBroadcast);
        
        // mCurrentGroup = null;
        btnTitleIcon.setChecked(true);
        btnTitleIcon.setButtonDrawable(R.drawable.arrow_up);
        GroupAdapter adapter = new GroupAdapter(this);
        if (popupWindow == null) {
            LayoutInflater lay = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = lay.inflate(R.layout.popup_menu, null);
            // v.setBackgroundDrawable(getResources().getDrawable(R.drawable.rounded_corners_view));

            // 初始化listview，加载数据。
            listViewGroup = (ListView)v.findViewById(R.id.popup_list);
            listViewGroup.setAdapter(adapter);
            listViewGroup.setItemsCanFocus(false);
            listViewGroup.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            listViewGroup.setOnItemClickListener(groupListClickListener);

            popupWindow = new PopupWindow(v, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, true);
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
     * 全部分享（虚拟群）
     * 
     * @return
     */
    private GroupInfo initGroupAll() {
        GroupInfo groupAll = new GroupInfo();
        groupAll.setGroupID(ID_GROUPALL);
        groupAll.setGroupName(getString(R.string.statuses_all_send));
        return groupAll;
    }

    private void setSinaState() {
        boolean syncSina = ConfigHelper.getInstance(this).loadBooleanKey(
                ConfigHelper.CONFIG_KEY_SYNC_SINA, false);
        btnImage.setImageResource(syncSina ? R.drawable.btn_dynamic_sina
                : R.drawable.btn_dynamic_sina_no_sync);
    }

    // set geography location
    private void setLocationState() {
        boolean geoLocation = ConfigHelper.getInstance(this).loadBooleanKey(
                ConfigHelper.CONFIG_KEY_GEOGRAPHY_LOCATION, false);
        btnLocation.setImageResource(geoLocation ? R.drawable.btn_dynamic_location
                : R.drawable.btn_dynamic_location_no_sync);
    }

    // private String mRealname;
    private void adjustByAction() {
        Intent intent = getIntent();
        String statusesID_Retweet = "";
        if (intent != null) {
            statusesID_Retweet = intent.getStringExtra(GlobalUserInfo.PARAM_STATUSES_ID);
        }
        
        SelectorActivity.prepare();

        do {
            setSinaState();
            findViewById(R.id.btn_friend).setOnClickListener(this);

            // 发分享
            if (getIntent().getAction().equals(ACTION_BROADCAST) || isShareSystem()) {
                findViewById(R.id.btn_ablum_camera).setOnClickListener(this);
                findViewById(R.id.btn_ablum_pic).setOnClickListener(this);
                findViewById(R.id.btn_sync_sina).setOnClickListener(this);
                findViewById(R.id.btn_location).setOnClickListener(this);
                break;
            }

            // 转发分享
            if (getIntent().getAction().equals(ACTION_RETWEET)) {
                mDynamicPoster.mDynamicPostInfo.setParamRetweet(statusesID_Retweet);

                findViewById(R.id.btn_ablum_camera).setVisibility(View.GONE);
                findViewById(R.id.upload_attach).setVisibility(View.GONE);
                findViewById(R.id.btn_ablum_pic).setVisibility(View.GONE);
                findViewById(R.id.btn_sync_sina).setOnClickListener(this);
                findViewById(R.id.btn_location).setOnClickListener(this);
                break;
            }

            // 草稿箱重发
            if (getIntent().getAction().equals(ACTION_DRAFT)) {
                findViewById(R.id.btn_ablum_camera).setOnClickListener(this);
                findViewById(R.id.btn_ablum_pic).setOnClickListener(this);
                findViewById(R.id.btn_sync_sina).setOnClickListener(this);
                findViewById(R.id.btn_location).setOnClickListener(this);
                DraftInfo info = Statuses_Draft_Activity.mDraftInfo;
                mDynamicPoster.mDynamicPostInfo.mParamContent = info.content;
                mDynamicPoster.mDynamicPostInfo.mParamRetweetId = info.objid;

                GroupInfo ginfo = DynamicMgr.getInstance().getGroupItem(info.groupid);
                mDynamicPoster.mDynamicPostInfo.mParamGroupInfo = ginfo;
                mDynamicPoster.mDynamicPostInfo.setDraftID(info.id);

                if (info.images != null
                        && info.images.length() > 0) {
                    try {
                        JSONArray imagesArray = new JSONArray(
                                info.images);
                        for (int j = 0; j < imagesArray.length(); ++j) {
                            mDynamicPoster.addPhoto(new PhotoUpload(new StartForResults.PickData(
                                    imagesArray.get(j).toString())));
                        }
                    } catch (Exception e) {
                    }
                }

                txtBroadcast.append(html2normal(info.content));
                setImageCount();
                // set default group name
                if (ginfo != null) {
                    mCurrentGroup = ginfo;
                    txtTitle.setText(mCurrentGroup.getGroupName());
                }
            }
        } while (false);
    }

    private void addAt(long id, String name) {
        txtBroadcast.append(SelectorActivity.addUser(id, name) + " ");
        // txtBroadcast.setSelection(txtBroadcast.getText().length()-1);
    }

    private String html2normal(String src) {
        String patternStr = ConfigHelper.PATTERN_USER;

        Pattern pattern = Pattern.compile(patternStr);
        Matcher matcher = pattern.matcher(src);
        while (matcher.find()) {
            String matchStr = matcher.group(1);
            // if (Long.getLong(matchStr) == null) {
            // continue;
            // }
            long uid = 0;
            try {
                Long.parseLong(matchStr);
                uid = Long.valueOf(matcher.group(1));
            } catch (Exception e) {
                Log.i(e.toString());
            }

            String name = matcher.group(2);
            // name include @ so , wo should start at 1
            SelectorActivity.addUser(uid, name);
            src = matcher.replaceFirst("@" + name);
            matcher = pattern.matcher(src);
        }
        return src;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        // 摄像头
            case R.id.btn_ablum_camera:
                StartForResults.getPicFromCamera(Statuses_Send_Activity.this);
                break;
            // 相册
            case R.id.btn_ablum_pic:
                if (mDynamicPoster.getLocalUrls().size() == 0) {
                    StartForResults.getPicFromAlbum(Statuses_Send_Activity.this);
                } else {
                    Intent i = new Intent(Statuses_Send_Activity.this,
                            Statuses_Images_Activity.class);
                    i.putExtra(Statuses_Images_Activity.EXTRAS_IMAGES,
                            mDynamicPoster.getLocalUrls());
                    startActivityForResult(i, 115);
                }
                break;
            case R.id.btn_comment:
                // 发布
                send();
                break;
            case R.id.btn_friend:
                Intent intentSearch = new Intent(Statuses_Send_Activity.this,
                        SelectorActivity.class);
                intentSearch.setAction(SelectorActivity.ACTION_STATUSES_PICK);
                startActivityForResult(intentSearch, SelectorActivity.USER_PICKED);
                break;
            case R.id.upload_attach:
                // DynamicActivity.mDynamicItemInfo.images =
                // mDynamicPoster.getLocalUrls();
                Intent i = new Intent(Statuses_Send_Activity.this,
                        Statuses_Images_Activity.class);
                i.putExtra(Statuses_Images_Activity.EXTRAS_IMAGES, mDynamicPoster.getLocalUrls());
                startActivityForResult(i, 115);
                break;
            case R.id.btn_sync_sina:
                boolean syncSina = ConfigHelper.getInstance(this).loadBooleanKey(
                        ConfigHelper.CONFIG_KEY_SYNC_SINA, false);
                ConfigHelper.getInstance(this).saveBooleanKey(ConfigHelper.CONFIG_KEY_SYNC_SINA,
                        !syncSina);
                ConfigHelper.getInstance(this).commit();
                setSinaState();
                break;
            case R.id.btn_location:
                boolean geoLocation = ConfigHelper.getInstance(this).loadBooleanKey(
                        ConfigHelper.CONFIG_KEY_GEOGRAPHY_LOCATION, false);
                ConfigHelper.getInstance(this).saveBooleanKey(
                        ConfigHelper.CONFIG_KEY_GEOGRAPHY_LOCATION, !geoLocation);
                ConfigHelper.getInstance(this).commit();
                setLocationState();
                // 显示群组选择
            case R.id.btn_group_select:
                showGroup();
                break;
            default:
                break;
        }
    }

    /**
     * 组织分享/评论 内容，包含at
     * 
     * @param value
     * @return
     */
    private String makeContent(String value) {
        String result = value;
        Iterator<String> iter = userMap.keySet().iterator();
        while (iter.hasNext()) {
            String name = iter.next();
            String uid = userMap.get(name);
            result = result.replace(name, name + "(" + uid + ")");
        }

        return result;
    }

    private void send() {
        do {
            mDynamicPoster.mDynamicPostInfo.mParamContent = makeContent(txtBroadcast.getText()
                    .toString());

            boolean syncSina = ConfigHelper.getInstance(this).loadBooleanKey(
                    ConfigHelper.CONFIG_KEY_SYNC_SINA, false);
            mDynamicPoster.mDynamicPostInfo.mParamIsSyncSina = syncSina;

            if (mCurrentGroup.getGroupID() != ID_GROUPALL) {
                mDynamicPoster.mDynamicPostInfo.mParamGroupInfo = mCurrentGroup;
            }
            if (getIntent().getAction().equals(ACTION_BROADCAST)
            		|| isShareSystem()
                    || getIntent().getAction().equals(ACTION_RETWEET)
                    || getIntent().getAction().equals(ACTION_FEED)
                    || getIntent().getAction().equals(ACTION_DRAFT)) {
                mDynamicPoster.addDraft();
                mDynamicPoster.postBroadcast();

                /*
                 * Intent brodcastIntent = new
                 * Intent(NotifyProgress.ACTION_PROCESS);
                 * brodcastIntent.putExtra(DraftMgr.DRAFT_ID,
                 * mDynamicPoster.mDraftid);
                 * setResult(DynamicMgr.MSG_POST_BROADCAST_PROCESS
                 * ,brodcastIntent);
                 */
                Statuses_Send_Activity.this.finish();
                break;
            }

            Intent i = new Intent();
            i.putExtra(CommentItemInfo.COMMENT_CONTENT,
                    mDynamicPoster.mDynamicPostInfo.mParamContent);
            long draftid = System.currentTimeMillis();
            i.putExtra(CommentItemInfo.COMMENT_ID, String.valueOf(draftid));

            // public Result getLocationResult(){
            // Result locationResult = null;
            // boolean geoLocation =
            // ConfigHelper.getInstance(this).loadBooleanKey(ConfigHelper.CONFIG_KEY_GEOGRAPHY_LOCATION,
            // false);
            // if (geoLocation) {
            // final LocManager locManager = new LocManager(this);
            // locationResult = locManager.getLocationPoint();
            // }
            //
            // return locationResult;
            // }

            boolean geoLocation = ConfigHelper.getInstance(this).loadBooleanKey(
                    ConfigHelper.CONFIG_KEY_GEOGRAPHY_LOCATION, false);
            i.putExtra(NEED_LOCATE, geoLocation);

        } while (false);
        btnBroadcast.setEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent paramIntent) {
        super.onActivityResult(requestCode, resultCode, paramIntent);

        Log.i("onActivityResult begin");
        if (resultCode != Activity.RESULT_OK) {
            if (mActivityType == TYPE_FOR_MO_SHARE) {
                finish();
            }
            return;
        }
        switch (requestCode) {
        // 图片上传
            case 115:
                ArrayList<String> result = paramIntent
                        .getStringArrayListExtra(Statuses_Images_Activity.EXTRAS_UNSELECT_IMAGES);
                if (result != null) {
                    mDynamicPoster.removeAll();
                    for (String url : result) {
                        mDynamicPoster.addPhoto(new PhotoUpload(new PickData(url)));
                    }
                }

                setImageCount();
                break;
            case StartForResults.CAMERA_PICKED_WITH_DATA:
            case StartForResults.PHOTO_PICKED_WITH_DATA:
                // 取得图片数据
                PickData pickedPhotoData = StartForResults.onRresult(this, requestCode, resultCode,
                        paramIntent);
                if (pickedPhotoData == null) {
                    Toast.makeText(this, "图片格式错误", 0).show();
                    break;
                }

                mDynamicPoster.addPhoto(new PhotoUpload(pickedPhotoData));
                setImageCount();
                // mUploadedProcess.setVisibility(View.VISIBLE);
                btnBroadcast.setEnabled(true);

                // 生成缩略图并且上传
                // if (mData.thumbBmp != null) {
                // addThumbView(mData.thumbBmp, R.id.upload_image);
                // startUpload();
                // }
                Log.i("onActivityResult end");
                break;
            case SelectorActivity.USER_PICKED:
                String extras = paramIntent.getStringExtra(SelectorActivity.EXTRA_RECEIVERS);
                ArrayList<User> userList = new ArrayList<User>();
                try {
                    userList.addAll(new GroupParser(new UserParser())
                            .parse(new JSONArray(extras)));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                for (User user : userList) {
                    String str = "@" + user.getName();
                    userMap.put(str, user.getId());
                    str = str + " ";
                    txtBroadcast.getText().insert(txtBroadcast.getSelectionStart(), str);
                }
                // String str = paramIntent.getStringExtra("name");
                // txtBroadcast.getText().insert(txtBroadcast.getSelectionStart(),
                // str);
                break;
            case SmileySelector.SMILEY_PICK_WITH_DATA:
                onSelect(paramIntent.getExtras().getString(SmileySelector.INTENT_SMILEY_KEY));
                break;
            default:
                break;
        }
    }

    private void onSelect(String smiley) {
        String current = txtBroadcast.getText().toString();
        int nowSelectStart = txtBroadcast.getSelectionStart();
        int nowSelectEnd = txtBroadcast.getSelectionEnd();
        String pre = current.substring(0, txtBroadcast.getSelectionEnd());
        String last = current.length() > 1 ? current.substring(
                txtBroadcast.getSelectionEnd(), current.length()) : "";
        String result = pre + smiley + last;
        SpannableStringBuilder emojiStr = TextViewUtil.addEmojiSmileySpans(
                Statuses_Send_Activity.this, result,
                ImageSpan.ALIGN_BASELINE);
        txtBroadcast.setText(emojiStr);
        if (((nowSelectStart == nowSelectEnd) && (nowSelectEnd == current
                .length())) || current.equals("")) {
            txtBroadcast.setSelection(result.length());
        } else {
            if (nowSelectEnd != nowSelectStart) {
                txtBroadcast.setSelection(nowSelectStart, nowSelectEnd);
            } else {
                txtBroadcast.setSelection(nowSelectStart
                        + smiley.length());
            }
        }
        Log.i("onSmiley select" + txtBroadcast.getText().toString());
    }
}


package cn.com.nd.momo.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.parsers.json.GroupParser;
import cn.com.nd.momo.api.parsers.json.UserParser;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.api.types.UserList;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.view.AlphabeticBar;


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

    private View mLayoutHeader;

    private EditText mEditText;

    private TextView mTxtTitle;


    private SelectedUser mSelectedUser;

    private ArrayList<User> mUserList = new ArrayList<User>();

    private Collection<User> mCacheUserList = null;

    /**
     * 直接跳转到聊天界面，防止出现闪动（不用ActivityResult）
     */
    private boolean mImSelect = false;

    /**
     * 选择结果是JSONArray， 可以用GroupParser(new UserParser())得到用户
     */
    private boolean mUserListSelect = false;

    private AlphabeticBar mAlphabeticBar = null;

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
        mUserArrayMap.clear();
    }

    /**
     * <br>
     * Description:用户选中的数组. <br>
     * Author:hexy <br>
     * Date:2011-10-6下午04:58:35
     * 
     * @return
     */
    public static ArrayList<Contact> getEditingUserArray() {
        return mUserArrayMap;
    }

    private final int MSG_GET_UID_FINISH = 1;

    private Handler mUIDHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MSG_GET_UID_FINISH:
                    if (mGetUidCanceled) {
                        return;
                    }

                    if (mProgressUID != null && mProgressUID.isShowing()) {
                        mProgressUID.dismiss();
                    }

                    // has message we show it
                    if (msg.obj != null && (msg.obj instanceof String)) {
                        Toast
                                .makeText(getApplicationContext(), (String)msg.obj,
                                        Toast.LENGTH_SHORT).show();
                    }

                    if (mImSelect) {
                        Intent iResult = new Intent();
                        UserList ul = new UserList();
                        if (mUserArrayMap.size() == 0 || mCacheUserList == null
                                || mCacheUserList.size() == 0) {
                            SelectorActivity.this.finish();
                            return;
                        }
                        for (User user : mCacheUserList) {
                            if (Long.valueOf(user.getId()) > 0) {
                                ul.add(user);
                            }
                        }

                        if (ul.size() == 0) {
                            Utils.displayToast(getString(R.string.txt_unfound_mo_user), 0);
                            SelectorActivity.this.finish();
                            return;
                        }
                        mUserArrayMap.clear();
                        // start IM activity
                        if (ul.size() > 1) {

                        } else if (ul.size() == 1) {

                        }
                        SelectorActivity.this.startActivity(iResult);
                        SelectorActivity.this.finish();

                    } else if (mUserListSelect) {
                        UserList ul = new UserList();
                        if (mUserArrayMap.size() == 0 || mCacheUserList == null
                                || mCacheUserList.size() == 0) {
                            SelectorActivity.this.finish();
                            return;
                        }
                        for (User user : mCacheUserList) {
                            if (Long.valueOf(user.getId()) > 0) {
                                ul.add(user);
                            }
                        }
                        if (ul.size() == 0) {
                            Utils.displayToast(getString(R.string.txt_unfound_mo_user), 0);
                            SelectorActivity.this.finish();
                            return;
                        }
                        mUserArrayMap.clear();
                        Intent data = new Intent();
                        try {
                            @SuppressWarnings({
                                    "unchecked", "rawtypes"
                            })
                            String receivers = new GroupParser(new UserParser()).toJSONArray(
                                    ul.getList()).toString();
                            data.putExtra(EXTRA_RECEIVERS, receivers);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SelectorActivity.this.setResult(RESULT_OK, data);
                        finish();
                    } else if (mIsStatusesUserSelect) {
                        UserList ul = new UserList();
                        if (mUserArrayMap.size() == 0 || mCacheUserList == null
                                || mCacheUserList.size() == 0) {
                            SelectorActivity.this.finish();
                            return;
                        }
                        for (User user : mCacheUserList) {
                            if (Long.valueOf(user.getId()) > 0) {
                                ul.add(user);
                            }
                        }
                        if (ul.size() == 0) {
                            Utils.displayToast(getString(R.string.txt_unfound_mo_user), 0);
                            SelectorActivity.this.finish();
                            return;
                        }
                        mUserArrayMap.clear();
                        Intent data = new Intent();
                        try {
                            @SuppressWarnings({
                                    "unchecked", "rawtypes"
                            })
                            String receivers = new GroupParser(new UserParser()).toJSONArray(
                                    ul.getList()).toString();
                            data.putExtra(EXTRA_RECEIVERS, receivers);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        SelectorActivity.this.setResult(RESULT_OK, data);
                        finish();
                    } else {
                        // start
                        Intent i = new Intent();

                        mSelectedUser.removeWrongUser();

                        i.putExtra("name", SelectorActivity.getNames(mSelectedUser.getUsers()));
                        setResult(RESULT_OK, i);
                        finish();
                    }

                    break;
            }
        }
    };

    private ProgressDialog mProgressUID = null;

    private boolean mGetUidCanceled = false;

    /**
     * <br>
     * Description:点击确认后调用的函数. <br>
     * Author:hexy <br>
     * Date:2011-10-6下午04:59:33
     */
    private void onConfirm() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dynamic_search);
        findViewById(R.id.title_bar).setVisibility(View.VISIBLE);
        mTxtTitle = (TextView)findViewById(R.id.txt_contact_select);
        // if single select
        Intent i = this.getIntent();

        if (i != null && i.getAction() != null) {
            String action = i.getAction();

            if (action.equalsIgnoreCase(ACTION_IM_PICK)) {
                mUserArrayMap.clear();
                mImSelect = true;
            } else if (action.equalsIgnoreCase(ACTION_USER_PICK)) {
                mUserArrayMap.clear();
                mUserListSelect = true;
            } else if (action.equalsIgnoreCase(ACTION_STATUSES_PICK)) {
                mUserArrayMap.clear();
                mIsStatusesUserSelect = true;
            } else {
                finish();
                return;
            }
        }

        mGridView = (ListView)findViewById(R.id.gridview);
        LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        mLayoutHeader = mInflater.inflate(R.layout.contact_search_bar, null);
        mEditText = (EditText)mLayoutHeader.findViewById(R.id.txt_search);
        mGridView.addHeaderView(mLayoutHeader);

        cn.com.nd.momo.util.Utils.hideKeyboard(this, mEditText);
        mSelectedUser = new SelectedUser();
        mEditText.addTextChangedListener(new TextWatcher() {
            // 文字改变事件监听
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                SelectorActivity.this.SearchContact(s.toString());
            }
        });
        mGridView.setOnItemClickListener(mOnItemClickListener);
        findViewById(R.id.btn_confirm).setOnClickListener(this);
        findViewById(R.id.btn_add_mobile).setOnClickListener(this);

        mAlphabeticBar = (AlphabeticBar)findViewById(R.id.alphabetic_bar);
        mAlphabeticBar.setOnTouchingLetterChangedListener(new LetterListViewListener());

        initListIndexer();
    }

    private class LetterListViewListener implements
            cn.com.nd.momo.view.AlphabeticBar.OnTouchingLetterChangedListener {

        @Override
        public void onTouchingLetterChanged(final String s) {

        }

        @Override
        public void onChangeIsTouch(boolean isTouch) {
            mIsScrolling = false;
            mTxtIndexerView.setVisibility(View.GONE);
        }

    }

    private TextView mTxtIndexerView;

    private void initListIndexer() {
        mTxtIndexerView = (TextView)findViewById(R.id.txt_letter_index);
        mTxtIndexerView.setVisibility(View.GONE);
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
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // onConfirm();
            if (mEditText.getText().length() > 0) {
                mEditText.setText("");
                return true;
            }
        } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            mGridView.setSelection(0);
            mEditText.requestFocus();
            cn.com.nd.momo.util.Utils.showKeyboard(getApplicationContext(), mEditText);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

        }
    };

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

    public void removeUser(Contact info) {
        // 已选中的 不重复加入
        int index = -1;
        for (int i = 0; i < mUserArrayMap.size(); i++) {
            Contact dc = mUserArrayMap.get(i);
            if (dc.getPrimePhoneNumber().equals(info.getPrimePhoneNumber())) {
                index = i;
                break;
            }
        }

        if (index != -1) {
            mUserArrayMap.remove(index);
        }
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

    private void SearchContact(String orig) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                onConfirm();
                break;
            case R.id.btn_add_mobile:
                addMobileToConversation();
                break;
        }
    }

    public void addMobileToConversation() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View contentview = inflater.inflate(R.layout.user_card_edit_item, null);
        builder.setView(contentview);
        final View phoneView = contentview.findViewById(R.id.item_setting);
        phoneView.setVisibility(View.VISIBLE);
        final EditText phoneEditView = (EditText)phoneView.findViewById(R.id.edit_text_value);
        phoneEditView.setKeyListener(new DigitsKeyListener());
        builder.setTitle("输入手机号发送MO短信");
        builder.setPositiveButton(
                getResources().getString(R.string.txt_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                            int whichButton) {
                        String mobile = phoneEditView.getText().toString();

                    }
                });
        builder.setNegativeButton(
                getResources().getString(R.string.txt_cancel),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Field field = dialog.getClass().getSuperclass().getDeclaredField(
                                    "mShowing");
                            field.setAccessible(true);
                            field.set(dialog, true);
                            cn.com.nd.momo.util.Utils.hideKeyboard(SelectorActivity.this,
                                    phoneEditView);
                            SelectorActivity.this
                                    .getWindow()
                                    .setSoftInputMode(
                                            android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                            dialog.dismiss();
                        } catch (Exception e) {

                        }
                    }
                });
        builder.create().show();
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

    /**
     * items
     */
    private boolean mIsScrolling = false;

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int paramInt) {

        }

        @Override
        public void onScroll(AbsListView view, int paramInt1, int paramInt2,
                int paramInt3) {
            if ((view.getLastVisiblePosition() + 1) == view.getCount()) {
                mIsScrolling = false;
                updateView();
            }
        }
    };

    private void updateView() {
        int visiblePosition = mGridView.getFirstVisiblePosition();
        Log.d(TAG, "visiblePosition:" + visiblePosition + " count:" + mGridView.getCount());
        try {
            for (int i = visiblePosition; i < mGridView.getCount(); i++) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error:" + e.getMessage());
        }
    }




    public class SelectedUser {
        private ArrayList<Contact> mUsers;

        public SelectedUser() {
            mUsers = new ArrayList<Contact>();
        };

        public void add(Contact item) {
            mUsers.add(item);
        }

        public void delete(int index) {
            if (index >= 0 && index < mUsers.size())
                mUsers.remove(index);
        }

        public int isSelected(Contact item) {
            for (int i = 0; i < mUsers.size(); i++) {
                Contact contact = mUsers.get(i);
                if (contact.getPrimePhoneNumber().equals(item.getPrimePhoneNumber())) {
                    return i;
                }
            }
            return -1;
        }

        public void clear() {
            mUsers.clear();
        }

        public ArrayList<Contact> getUsers() {
            return mUsers;
        }

        public void removeWrongUser() {
            for (int i = mUsers.size() - 1; i >= 0; i--) {
                Contact cinfo = mUsers.get(i);
                boolean inUserMap = false;
                for (Contact cRightInfo : mUserArrayMap) {
                    if (cinfo.getContactId() == cRightInfo.getContactId()) {
                        inUserMap = true;
                    }
                }

                if (!inUserMap) {
                    mUsers.remove(cinfo);
                }
            }
        }

        public String getNames() {
            return SelectorActivity.getNames(mUsers);
        }

    }

    public static String getNames(ArrayList<Contact> contacts) {
        String names = "";
        for (Contact item : contacts) {
            names += encodeName(item.getFormatName(), item.getUid()) + " ";
        }

        return names;
    }

}


package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.parsers.json.ChatContentParser;
import cn.com.nd.momo.api.sync.ContactSyncManager;
import cn.com.nd.momo.api.sync.LocalContactsManager;
import cn.com.nd.momo.api.sync.MoMoContactsManager;
import cn.com.nd.momo.api.sync.SyncManager;
import cn.com.nd.momo.api.types.ChatContent;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

/**
 * 更换手机号流程
 * 
 * @author jiaolei
 */
public class ChangeMobileActivity extends Activity implements OnClickListener {
    private final String TAG = "ChangeMobileActivity";

    public static final String EXTRA_NAME = "name";

    public static final String EXTRA_MOBILE_EXCHANGE = "mobile_exchange";

    private static final int MSG_GET_EXCHANGE_CONTACT_PHONE_COMPLETE = 11;

    private static final int MSG_EXCHANGE_CONTACT_PHONE_SUCCESS = 12;

    private static final int MSG_EXCHANGE_CONTACT_PHONE_FAILED = 13;

    private LayoutInflater mInflater;

    private ChatContent.MobileModify mMobileModify = null;

    private LinearLayout mLayoutChangeList = null;

    private ProgressDialog mProgress = null;

    private Map<Long, Contact> mContactMap = new HashMap<Long, Contact>();

    private Map<Long, Boolean> mCheckedMap = new HashMap<Long, Boolean>();

    private String mOldMobile = "";

    private String mNewFullMobile = "";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mProgress != null && mProgress.isShowing()) {
                mProgress.dismiss();
            }
            switch (msg.what) {
                case MSG_GET_EXCHANGE_CONTACT_PHONE_COMPLETE:
                    initContactPhoneView();
                    break;
                case MSG_EXCHANGE_CONTACT_PHONE_SUCCESS:
                    Utils.displayToast("联系人电话号码替换完成", 0);
                    finish();
                    break;
                case MSG_EXCHANGE_CONTACT_PHONE_FAILED:
                    Utils.displayToast("联系人电话号码替换失败，请稍后再试...", 0);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.change_mobile_activity);
        Intent intent = getIntent();
        String jsonString = intent.getStringExtra(EXTRA_MOBILE_EXCHANGE);
        if (jsonString == null || jsonString.length() < 1) {
            cn.com.nd.momo.api.util.Utils.displayToast("没有任何号码变更数据", 0);
            finish();
            return;
        }
        Log.d(TAG, "jsonString:" + jsonString);
        if (SyncContactApi.getInstance(getApplicationContext()).isSyncInProgress()) {
            cn.com.nd.momo.api.util.Utils.displayToast("正在同步联系人，请稍后...", 0);
            finish();
            return;
        }

        try {
            JSONObject json = new JSONObject(jsonString);
            mMobileModify = (ChatContent.MobileModify)(new ChatContentParser().parse(json));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mMobileModify == null) {
            cn.com.nd.momo.api.util.Utils.displayToast("数据不合法", 0);
            finish();
            return;
        }
        mLayoutChangeList = (LinearLayout)findViewById(R.id.layout_contact_list);

        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TextView newMobileView = (TextView)findViewById(R.id.txt_new_mobile);
        mNewFullMobile = mMobileModify.getZoneCode() + mMobileModify.getMobile();
        newMobileView.setText(mNewFullMobile);

        findViewById(R.id.btn_exchange).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        mOldMobile = mMobileModify.getMobileOld();
        getExchangeContactMobile(mOldMobile);
    }

    private void getExchangeContactMobile(final String mobile) {
        Log.i(TAG, "old mobile:" + mobile);
        if (SyncContactApi.getInstance(getApplicationContext()).isSyncInProgress()) {
            cn.com.nd.momo.api.util.Utils.displayToast("正在同步联系人，请稍后...", 0);
            return;
        }
        mProgress = ProgressDialog.show(this, "", "获取可替换的联系人中...");
        new Thread() {
            @Override
            public void run() {
                MoMoContactsManager mmcm = MoMoContactsManager.getInstance();
                mContactMap = mmcm.getContactListByMobile(mobile);
                mHandler.sendEmptyMessage(MSG_GET_EXCHANGE_CONTACT_PHONE_COMPLETE);
            }
        }.start();

    }

    private void initContactPhoneView() {
        mCheckedMap.clear();
        mLayoutChangeList.removeAllViews();
        if (mContactMap.size() > 0) {
            for (long contactId : mContactMap.keySet()) {
                Contact contact = mContactMap.get(contactId);
                if (contact.getPhoneList().size() > 0) {
                    if (!mCheckedMap.containsKey(contactId)) {
                        mCheckedMap.put(contactId, false);
                    }
                    dynamicAddContactItem(contactId, contact.getFormatName(), contact
                            .getPhoneList()
                            .get(0));
                }
            }
        } else {
            Utils.displayToast("未找到可替换的联系人电话号码", 0);
            View itemView = mInflater.inflate(R.layout.change_mobile_contact_item, null);
            TextView nameView = (TextView)itemView.findViewById(R.id.txt_contact_name);
            TextView mobileView = (TextView)itemView.findViewById(R.id.txt_mobile);
            CheckBox contactCheck = (CheckBox)itemView.findViewById(R.id.ck_contact);
            nameView.setVisibility(View.GONE);
            contactCheck.setVisibility(View.GONE);
            mobileView.setText("未找到可替换的联系人电话号码");
            mLayoutChangeList.addView(itemView);
        }

    }

    private View dynamicAddContactItem(long contactId, String name, String mobile) {
        View itemView = mInflater.inflate(R.layout.change_mobile_contact_item, null);
        TextView nameView = (TextView)itemView.findViewById(R.id.txt_contact_name);
        TextView mobileView = (TextView)itemView.findViewById(R.id.txt_mobile);
        CheckBox contactCheck = (CheckBox)itemView.findViewById(R.id.ck_contact);
        contactCheck.setTag(contactId);
        contactCheck.setOnCheckedChangeListener(chectedChangeListener);
        nameView.setText(name);
        mobileView.setText(mobile);
        mLayoutChangeList.addView(itemView);
        return itemView;
    }

    private OnCheckedChangeListener chectedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            long contactId = Long.valueOf(buttonView.getTag().toString());
            Log.i(TAG, "contactId " + contactId + " isChecked:" + isChecked);
            if (mCheckedMap.containsKey(contactId)) {
                mCheckedMap.put(contactId, isChecked);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mProgress != null && mProgress.isShowing()) {
            mProgress.dismiss();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_exchange:
                List<Long> contactIdList = new ArrayList<Long>();
                String contactIdString = "";
                for (long contactId : mCheckedMap.keySet()) {
                    if (mCheckedMap.get(contactId) && !contactIdList.contains(contactId)) {
                        contactIdList.add(contactId);
                        contactIdString += contactId + ",";
                    }
                }
                if (contactIdList.size() < 1) {
                    Utils.displayToast(getString(R.string.exchange_no_mobile), 0);
                } else {
                    Log.i(TAG, "contactIdString:" + contactIdString);
                    final String syncMode = ConfigHelper.getInstance(this).loadKey(
                            ConfigHelper.CONFIG_KEY_SYNC_MODE);
                    List<Contact> updateContactList = new ArrayList<Contact>();
                    MoMoContactsManager mm = MoMoContactsManager.getInstance();

                    for (Long contactId : contactIdList) {
                        boolean find = false;
                        Contact contact = mm.getContactById(contactId);
                        if (contact != null && contact.getPhoneList() != null) {
                            List<String> phoneList = new ArrayList<String>();
                            for (String phone : contact.getPhoneList()) {
                                if (phone != null && phone.endsWith(mOldMobile)) {
                                    phoneList.add(mNewFullMobile);
                                    find = true;
                                } else {
                                    phoneList.add(phone);
                                }
                            }
                            contact.setPhoneList(phoneList);
                        } else {
                            Log.d(TAG, "unfund contactId(" + contactId + ") contact ");
                        }
                        if (find) {
                            updateContactList.add(contact);
                        }
                    }
                    if (updateContactList.size() == 0) {
                        finish();
                        return;
                    }
                    exchange(syncMode, updateContactList);
                }
                break;
            case R.id.btn_cancel:
                finish();
                return;
            default:
                break;
        }
    }

    private void exchange(final String syncMode, final List<Contact> updateContactList) {
        if (SyncContactApi.getInstance(getApplicationContext()).isSyncInProgress()) {
            cn.com.nd.momo.api.util.Utils.displayToast("正在同步联系人，请稍后...", 0);
            return;
        }
        if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(syncMode)) {
            if (null == Utils.getActiveNetWorkName(ChangeMobileActivity.this)) {
                cn.com.nd.momo.api.util.Utils.displayToast("请检查网络连接", 0);
                return;
            }
        }

        mProgress = ProgressDialog.show(this, "", "获取可替换的联系人中...");
        new Thread() {
            @Override
            public void run() {
                if (ConfigHelper.SYNC_MODE_TWO_WAY.equals(syncMode)) {
                    ContactSyncManager csm = ContactSyncManager.getInstance();
                    boolean success = false;
                    for (Contact contact : updateContactList) {
                        int code = csm.updateContactsToServer(contact);
                        if (code == HttpStatus.SC_OK) {
                            success = true;
                        }
                    }
                    if (success) {
                        mHandler.sendEmptyMessage(MSG_EXCHANGE_CONTACT_PHONE_SUCCESS);
                    } else {
                        mHandler.sendEmptyMessage(MSG_EXCHANGE_CONTACT_PHONE_FAILED);
                    }
                } else {
                    SyncManager.getInstance().setSyncInProgress(true);
                    boolean result = MoMoContactsManager.getInstance().updateContacts(
                            updateContactList);
                    Log.d(TAG, "update momo result:" + result);
                    if (result) {
                        Log.d(TAG, "正要更新手机：" + updateContactList.size());
                        LocalContactsManager.getInstance().batchUpdateContact(updateContactList);
                    }
                    SyncManager.getInstance().setSyncInProgress(false);
                    if (result) {
                        mHandler.sendEmptyMessage(MSG_EXCHANGE_CONTACT_PHONE_SUCCESS);
                    } else {
                        mHandler.sendEmptyMessage(MSG_EXCHANGE_CONTACT_PHONE_FAILED);
                    }
                }
            }
        }.start();
    }

}

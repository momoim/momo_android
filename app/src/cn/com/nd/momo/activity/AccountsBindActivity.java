
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.sync.SyncManager;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.types.MyAccount;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

/**
 * 导入同步帐号联系人Activity
 * 
 * @author jiaolei
 */
public class AccountsBindActivity extends Activity implements OnClickListener {
    private String TAG = "AccountsBindActivity";

    public static String NEED_LEAD_TO_SYNC = "need_lead_to_sync";

    public static String FIRST_SYNC_AFTER_LOGIN = "first_sync_after_login";

    private static final int MSG_GET_ADD_CONTACT_COMPLETE = 1;

    private static final int MSG_GET_ACCOUNTS_LIST_COMPLETE = 2;

    private ListView mAccountsListView = null;

    private List<MyAccount> mArrayAccount = new ArrayList<MyAccount>();

    private LayoutInflater mInflater = null;

    private ProgressDialog m_progressDlg = null;

    private AccountsAdapter mAccountsAdapter = new AccountsAdapter();

    private List<Contact> mContactList = new ArrayList<Contact>();

    private boolean mNeedLeadToSync = false;

    private boolean mFirstSyncAfterLogin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.accounts_bind);
        Intent intent = getIntent();
        if (intent != null) {
            mNeedLeadToSync = intent.getBooleanExtra(NEED_LEAD_TO_SYNC, true);
            mFirstSyncAfterLogin = intent.getBooleanExtra(FIRST_SYNC_AFTER_LOGIN, false);
        }
        Log.d(TAG, "mNeedLeadToSync:" + mNeedLeadToSync);
        if (!Utils.isBindedAccountExist(Utils.getCurrentAccount())) {
            Utils.resetAccount(this);
        }

        mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

        mAccountsListView = (ListView)findViewById(R.id.account_list);
        Log.d(TAG, "get account list begin");
        getAccounts();
        // mArrayAccount = Utils.getAccounts();
        mAccountsAdapter.setData(mArrayAccount);
        mAccountsListView.setAdapter(mAccountsAdapter);
        mAccountsListView.setOnItemClickListener(new OnAccountListItemClick());

        // button ok
        Button btnOK = (Button)findViewById(R.id.btn_accounts_bind_ok);
        btnOK.setOnClickListener(this);
        Button btnCancel = (Button)findViewById(R.id.btn_accounts_bind_cancel);
        btnCancel.setVisibility(View.VISIBLE);
        btnCancel.setClickable(true);
        btnCancel.setOnClickListener(this);

    }

    private class OnAccountListItemClick implements OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            mAccountsAdapter.setChecked(arg2);
        }
    }

    private void getAccounts() {
        m_progressDlg = ProgressDialog.show(this, "", "加载帐号列表...");
        m_progressDlg.setCancelable(true);
        new Thread() {
            @Override
            public void run() {
                mArrayAccount = Utils.getAccounts();
                Account currentAccount = Utils.getCurrentAccount();
                if (currentAccount == null) {
                    currentAccount = Utils.resetAccount(AccountsBindActivity.this);
                }
                Iterator<MyAccount> ite = mArrayAccount.iterator();
                while (ite.hasNext()) {
                    MyAccount account = ite.next();
                    if (currentAccount.name.equals(account.name)
                            && currentAccount.type.equals(account.type)) {
                        ite.remove();
                        continue;
                    } else {
                        for (MyAccount myAccount : MyAccount.KNOWN_SUPPORT_VENDOR_LIST) {
                            if (myAccount.isEqual(account)) {
                                ite.remove();
                                break;
                            }
                        }
                    }
                }
                MyAccount simAccount = new MyAccount("sim卡", "sim");
                simAccount.setCount(SyncContactApi.getInstance(getApplicationContext())
                        .getSimContactsCount());
                mArrayAccount.add(simAccount);
                int count = Utils.getContactCountByAccount(currentAccount);
                MyAccount syncAccount = new MyAccount(currentAccount.name, currentAccount.type);
                syncAccount.setCount(count);
                mArrayAccount.add(0, syncAccount);
                Log.d(TAG, "get account list end");
                Message msg = new Message();
                msg.what = MSG_GET_ACCOUNTS_LIST_COMPLETE;
                mHandler.sendMessage(msg);
                if (m_progressDlg.isShowing()) {
                    m_progressDlg.dismiss();
                }
            }
        }.start();
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_GET_ACCOUNTS_LIST_COMPLETE:
                    mAccountsAdapter.setData(mArrayAccount);
                    mAccountsAdapter.notifyDataSetChanged();
                    break;
                case MSG_GET_ADD_CONTACT_COMPLETE:
                    Log.d(TAG, "add contact list end");
                    if (m_progressDlg.isShowing()) {
                        m_progressDlg.dismiss();
                    }
                    if (mNeedLeadToSync) {
                        ConfigHelper ch = ConfigHelper.getInstance(AccountsBindActivity.this);
                        ch.saveBooleanKey(ConfigHelper.CONFIG_KEY_IMPORT_ACCOUNTS, true);
                        ch.commit();
                        Intent i = new Intent(AccountsBindActivity.this, MainActivity.class);
                        i.putExtra(MainActivity.DELETE_MOMO_DB, true);
                        startActivity(i);
                    } else {
                        setResult(RESULT_OK);
                    }
                    finish();
                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_accounts_bind_ok:
                if (SyncManager.getInstance().isSyncInProgress()) {
                    cn.com.nd.momo.api.util.Utils.displayToast("正在同步中，请稍后...", 0);
                    return;
                }
                ConfigHelper.getInstance(getApplicationContext()).saveKey(
                        ConfigHelper.CONFIG_KEY_SYNC_MODE, ConfigHelper.SYNC_MODE_TWO_WAY);
                ConfigHelper.getInstance(getApplicationContext()).commit();
                m_progressDlg = ProgressDialog.show(this, "", "正在导入选中帐号联系人...");
                m_progressDlg.setCancelable(false);
                new Thread() {
                    @Override
                    public void run() {
                        List<MyAccount> selectedAccounts = mAccountsAdapter.getSelectAccountList();
                        SyncContactApi syncApi = SyncContactApi
                                .getInstance(getApplicationContext());
                        mContactList.clear();
                        for (MyAccount account : selectedAccounts) {
                            if (account.type.equals("sim")) {
                                mContactList.addAll(syncApi.getSimContactList());
                            } else {
                                mContactList.addAll(syncApi.getLocalContactListByAccount(account));
                            }
                        }
                        Log.d(TAG, "before crc, add contact size:" + mContactList.size());
                        if (mContactList.size() < 1) {
                            Log.d(TAG, "import account contacts cmplete");
                            Message msg = new Message();
                            msg.what = MSG_GET_ADD_CONTACT_COMPLETE;
                            mHandler.sendMessage(msg);
                        }
                        Account account = Utils.getCurrentAccount();
                        List<Contact> syncAccountContacts = syncApi
                                .getLocalContactListByAccount(account);
                        Log.d(TAG, "syc account contact size:" + syncAccountContacts.size());
                        List<Long> contactCrcList = new ArrayList<Long>();
                        for (Contact contact : syncAccountContacts) {
                            contactCrcList.add(contact.generateProperCRC());
                        }
                        // crc去重,不比较头像，是否收藏，分组
                        Iterator<Contact> ite = mContactList.iterator();
                        while (ite.hasNext()) {
                            Contact addContact = ite.next();
                            long crc = addContact.generateProperCRC();
                            if (contactCrcList.contains(crc)) {
                                ite.remove();
                            }
                        }
                        Log.d(TAG, "after crc, add contact size:" + mContactList.size());

                        if (Utils.isBindedAccountExist(account)) {
                            final int eachNum = 100;
                            int count = mContactList.size();
                            int times = count / eachNum + 1;
                            if(MyAccount.MOBILE_ACCOUNT.isEqual(account)) {
                                account = Utils.getVendorAccount();
                            }
                            for (int i = 0; i < times; i++) {
                                List<Contact> list;
                                int begin = i * eachNum;
                                if (i == times - 1) {
                                    list = mContactList.subList(begin, count);
                                } else {
                                    int end = (i + 1) * eachNum;
                                    list = mContactList.subList(begin, end);
                                }
                                
                                Log.d(TAG, "account name:" + account.name + " account type:"
                                        + account.type);
                                Log.d(TAG, "list size:" + list.size());

                                syncApi.addContactListToAccount(list, account);
                            }
                        }
                        Log.d(TAG, "import account contacts cmplete");
                        Message msg = new Message();
                        msg.what = MSG_GET_ADD_CONTACT_COMPLETE;
                        mHandler.sendMessage(msg);
                    }
                }.start();
                break;
            case R.id.btn_accounts_bind_cancel:
                if (mNeedLeadToSync || mFirstSyncAfterLogin) {
                    showNotSyncConfirmDialog();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
                break;
            default:
                break;
        }
    }

    class AccountsAdapter extends BaseAdapter {
        private List<Boolean> mCheckedList = new ArrayList<Boolean>();

        private List<MyAccount> mAccountList = new ArrayList<MyAccount>();

        private HashMap<Integer, View> mMap = new HashMap<Integer, View>();

        public void setData(List<MyAccount> myAccountList) {
            if (myAccountList != null) {
                mAccountList.clear();
                mAccountList = new ArrayList<MyAccount>(Arrays.asList(new MyAccount[myAccountList
                        .size()]));
                Collections.copy(mAccountList, myAccountList);
                mCheckedList.clear();
                for (int i = 0; i < mAccountList.size(); i++) {
                    mCheckedList.add(true);
                }
            }
        }

        public List<MyAccount> getSelectAccountList() {
            List<MyAccount> selectedAccounts = new ArrayList<MyAccount>();
            for (int i = 0; i < mCheckedList.size(); i++) {
                if (mCheckedList.get(i)) {
                    MyAccount account = (MyAccount)getItem(i);
                    Account currentAccount = Utils.getCurrentAccount();
                    if(currentAccount == null) {
                        if (account != null && !account.isEqual(MyAccount.MOBILE_ACCOUNT)) {
                            Log.d(TAG, "import account name:" + account.name);
                            selectedAccounts.add(account);
                        }
                    } else {
                        if (account != null && !account.isEqual(currentAccount)) {
                            Log.d(TAG, "import account name:" + account.name);
                            selectedAccounts.add(account);
                        } 
                    }
                }
            }
            return selectedAccounts;
        }

        public void setChecked(int position) {
            MyAccount myAccount = mAccountList.get(position);
            Account currentAccount = Utils.getCurrentAccount();
            if(currentAccount == null) {
                if (!myAccount.isEqual(MyAccount.MOBILE_ACCOUNT)) {
                    boolean isChecked = mCheckedList.get(position);
                    mCheckedList.set(position, !isChecked);
                    notifyDataSetChanged();
                }
            } else {
                if (!myAccount.isEqual(currentAccount)) {
                    boolean isChecked = mCheckedList.get(position);
                    mCheckedList.set(position, !isChecked);
                    notifyDataSetChanged();
                }
            }
        }

        @Override
        public int getCount() {
            return mAccountList.size();
        }

        @Override
        public Object getItem(int position) {
            int count = getCount();
            if (count == 0 || position < 0 || position > count - 1) {
                return null;
            }
            return mAccountList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            ViewHolder holder = null;
            if (mMap.get(position) == null) {
                view = mInflater.inflate(R.layout.account_list_item, null);
                holder = new ViewHolder();
                holder.selected = (CheckBox)view.findViewById(R.id.account_select);
                holder.name = (TextView)view.findViewById(R.id.account_name);
                mMap.put(position, view);
                view.setTag(holder);
            } else {
                view = mMap.get(position);
                holder = (ViewHolder)view.getTag();
            }
            holder.selected.setClickable(false);
            holder.selected.setFocusable(false);
            holder.name.setClickable(false);
            holder.selected.setChecked(mCheckedList.get(position));
            MyAccount account = mAccountList.get(position);
            Account currentAccount = Utils.getCurrentAccount();
            if(currentAccount == null) {
                if (account.isEqual(MyAccount.MOBILE_ACCOUNT)) {
                    holder.name.setText("同步帐号:" + getString(R.string.txt_phone) + "("
                            + account.getCount() + ")");
                    holder.selected.setVisibility(View.INVISIBLE);
                } else {
                    holder.name.setText(account.name + "(" + account.getCount() + ")");
                    holder.selected.setVisibility(View.VISIBLE);
                }
            } else {
                if (account.isEqual(currentAccount)) {
                    String syncAccountName = currentAccount.name;
                    if (MyAccount.MOBILE_ACCOUNT.isEqual(currentAccount)) {
                        syncAccountName = getString(R.string.txt_phone);
                    }
                    holder.name.setText("同步帐号:" + syncAccountName + "(" + account.getCount() + ")");
                    holder.selected.setVisibility(View.INVISIBLE);
                } else {
                    holder.name.setText(account.name + "(" + account.getCount() + ")");
                    holder.selected.setVisibility(View.VISIBLE);
                }
            }

            return view;
        }

    }

    static class ViewHolder {
        CheckBox selected;

        TextView name;
    }

    @Override
    public void onBackPressed() {
        if (mNeedLeadToSync || mFirstSyncAfterLogin) {
            showNotSyncConfirmDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showNotSyncConfirmDialog() {
        // show message box to ensure quit
        Builder b = new AlertDialog.Builder(this);
        b.setTitle("同步退出");
        b.setMessage("您将退出联系人同步流程，确定不进行同步吗？");
        b.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ConfigHelper.getInstance(getApplicationContext()).saveKey(
                        ConfigHelper.CONFIG_KEY_SYNC_MODE, ConfigHelper.SYNC_MODE_LOCAL_ONLY);
                ConfigHelper.getInstance(getApplicationContext()).commit();

                // finish this activity
                Intent i = new Intent(AccountsBindActivity.this, MainActivity.class);
                i.putExtra(MainActivity.DELETE_MOMO_DB, true);
                startActivity(i);
                AccountsBindActivity.this.finish();
                return;
            }
        });
        b.setNegativeButton(getString(R.string.txt_cancel),
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        b.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_progressDlg != null && m_progressDlg.isShowing()) {
            m_progressDlg.dismiss();
        }
    }

}

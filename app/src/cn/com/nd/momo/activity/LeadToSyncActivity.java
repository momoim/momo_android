
package cn.com.nd.momo.activity;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.MyAccount;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;

/**
 * 引导同步activity
 * 
 * @author jiaolei
 */
public class LeadToSyncActivity extends Activity implements OnClickListener {
    private String TAG = "LeadToSyncActivity";

    public static int REQ_CODE_REG_INFO = 1;

    private ViewGroup mBtnSyncBegin;

    private ViewGroup mBtnNoSync;

    private List<MyAccount> arrayAcc;

    private String[] strAccItem;

    private ViewGroup mChooseAccount;

    private TextView mTxtAccountName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.lead_to_sync);
        Log.d(TAG, "onCreate");

        mBtnNoSync = (ViewGroup)findViewById(R.id.btn_sync_cancel);
        mBtnNoSync.setOnClickListener(this);

        mBtnSyncBegin = (ViewGroup)findViewById(R.id.btn_lead_sync_begin);
        mBtnSyncBegin.setOnClickListener(this);

        mChooseAccount = (ViewGroup)findViewById(R.id.btn_choose_account);
        mChooseAccount.setOnClickListener(this);
        mTxtAccountName = (TextView)findViewById(R.id.txt_choose_account);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // ----------------------------------------------------
        // arrayAcc = Utils.getAccounts();
        // Log.d(TAG, "account size:" + arrayAcc.size());
        //
        // // when no account found, show a dialog and exit application
        // if (arrayAcc == null || arrayAcc.size() < 1) {
        // Log.e(TAG, "MomoAccount.getAccounts: can't get any account");
        // Intent i = new Intent(this, MainActivity.class);
        // startActivity(i);
        // finish();
        // return;
        // }
        //
        // // init current account
        // if (mCurrAccount == null) {
        // mCurrAccount = arrayAcc.get(0);
        // }
        // mTxtAccountName.setText(getAccountName(mCurrAccount));

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sync_cancel:
                localSync();
                break;
            case R.id.btn_lead_sync_begin:
                // save config
                beginSync();
                break;

            case R.id.btn_choose_account:
                showAccountChoose();
                break;
        }
    }

    private void beginSync() {
        Log.e(TAG, "btn_lead_sync_begin");
        // saveUserChoice();

        Intent i = new Intent(getApplicationContext(), AccountsBindActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        this.finish();
    }

    private void localSync() {
        // saveUserChoice();
        // do local sync only
        ConfigHelper.getInstance(getApplicationContext()).saveKey(
                ConfigHelper.CONFIG_KEY_SYNC_MODE, ConfigHelper.SYNC_MODE_LOCAL_ONLY);
        ConfigHelper.getInstance(getApplicationContext()).commit();

        // finish this activity
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(MainActivity.DELETE_MOMO_DB, true);
        startActivity(i);
        this.finish();

    }

    /**
     * 确认不同步
     */
    @SuppressWarnings("unused")
    private void confirmNotSync() {
        Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.msg_lead_sync_sync_cancel_title);
        b.setMessage(getString(R.string.msg_lead_sync_cancel_confirm));
        b.setPositiveButton(getString(R.string.txt_sync), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                beginSync();
                return;
            }
        });
        b.setNegativeButton(getString(R.string.txt_no_sync), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                localSync();
                return;
            }
        });
        b.show();
    }

    private int mAccountChoice = 0;

    private int mAccountCurrChoice = 0;

    private void showAccountChoose() {
        strAccItem = new String[arrayAcc.size()];
        for (int i = 0; i < arrayAcc.size(); i++) {
            strAccItem[i] = getAccountName(arrayAcc.get(i));
        }

        Builder b = new AlertDialog.Builder(this);
        mAccountCurrChoice = mAccountChoice;
        b.setTitle("选择同步的帐号");
        b.setSingleChoiceItems(strAccItem, mAccountChoice, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                mAccountCurrChoice = paramInt;
            }
        });
        b.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAccountChoice = mAccountCurrChoice;
                mTxtAccountName.setText(getAccountName(arrayAcc.get(mAccountChoice)));
                // mCurrAccount = arrayAcc.get(mAccountChoice);
            }
        });
        b.setNegativeButton(getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        b.show();
    }

    private String getAccountName(MyAccount acc) {
        if (acc.name.equals(MyAccount.ACCOUNT_MOBILE_NAME)) {
            return getString(R.string.txt_phone) + "(" + acc.getCount() + ")";
        } else {
            return acc.name + "(" + acc.getCount() + ")";
        }
    }

    // private void saveUserChoice() {
    // Utils.saveCurrentAccount(mCurrAccount);
    // }

    @Override
    public void onBackPressed() {
        // show message box to ensure quit
        Builder b = new AlertDialog.Builder(this);
        b.setTitle("确定退出");
        b.setMessage("退出momo？下次启动momo再设置同步方式");
        b.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                LeadToSyncActivity.this.finish();
                return;
            }
        });
        b.setNegativeButton(getString(R.string.txt_cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        b.show();
    }

}

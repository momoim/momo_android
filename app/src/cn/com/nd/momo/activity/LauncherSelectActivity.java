
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.manager.AvatarManager;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.LoginSearchSMSThread;

/**
 * 启动扫描到momo短信，选择是否登录短信对应用户帐号
 * 
 * @author jiaolei
 */
public class LauncherSelectActivity extends Activity implements OnClickListener {

    final String TAG = "LauncherSelectActivity";

    TextView mPhone;

    TextView mName;

    Button mBtnOK;

    Button mBtnCancel;

    Bundle mBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome_select);

        mBundle = getIntent().getExtras();
        if (mBundle == null) {
            Log.e(TAG, "get null bundle");
            finish();
        }

        mPhone = (TextView)findViewById(R.id.txt_phone_number);
        mPhone.setText(mBundle.getString(LoginSearchSMSThread.MOBILE));
        mName = (TextView)findViewById(R.id.txt_user_name);
        mName.setText(mBundle.getString(LoginSearchSMSThread.NAME));
        mBtnOK = (Button)findViewById(R.id.btn_wel_ok);
        mBtnOK.setOnClickListener(this);
        mBtnCancel = (Button)findViewById(R.id.btn_wel_cancel);
        mBtnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent i = null;
        switch (v.getId()) {
            case R.id.btn_wel_ok:
                String strUID = mBundle.getString(LoginSearchSMSThread.UID);
                String strUserName = mBundle.getString(LoginSearchSMSThread.NAME);
                String strZoneCode = mBundle.getString(LoginSearchSMSThread.ZONE_CODE);
                String strPhone = mBundle.getString(LoginSearchSMSThread.MOBILE);
                String strAvatar = mBundle.getString(LoginSearchSMSThread.AVATAR);
                String strOToken = mBundle.getString(LoginSearchSMSThread.OTOKEN);
                String strOSecret = mBundle.getString(LoginSearchSMSThread.OSECRET);
                String strQName = mBundle.getString(LoginSearchSMSThread.QNAME);
                String strStatus = mBundle.getString(LoginSearchSMSThread.STATUS);

                GlobalUserInfo.setOAuthToken(strUID, strOToken, strOSecret, strUserName, strAvatar,
                        strQName, strStatus, strZoneCode, strPhone);
                // download avatar
                Bitmap bmp = AvatarManager.getAvaterBitmap(Long.valueOf(strUID),
                        strAvatar);
                GlobalUserInfo.setMyAvatar(bmp);
                ConfigHelper.getInstance(this).saveKey(ConfigHelper.CONFIG_KEY_LOGIN_STATUS,
                        String.valueOf(GlobalUserInfo.LOGIN_STATUS_LOGINED));
                ConfigHelper.getInstance(this).removeKey(ConfigHelper.CONFIG_KEY_SYNC_MODE);
                ConfigHelper.getInstance(this).commit();
                // setResult(RESULT_OK);
                GlobalUserInfo.checkLoginStatus(getApplicationContext());

                // tell service to start consume
                sendBroadcast(new Intent(getString(R.string.action_message_login)));

                // clear momo database
                SyncContactApi.getInstance(getApplicationContext()).deleteMoMoDatabaseContacts();
                // start lead to sync activity
                i = new Intent(getApplicationContext(), LoginPreActivity.class);
                i.putExtra(LoginPreActivity.INTENT_EXTRA_AUTO_LOGIN_BY_SMS, true);
                startActivity(i);
                finish();
                break;
            case R.id.btn_wel_cancel:
                setResult(RESULT_OK);
                i = new Intent(getApplicationContext(), LoginPreActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent i = new Intent(getApplicationContext(), LoginPreActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

}

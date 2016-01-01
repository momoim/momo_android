
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;

import com.flurry.android.FlurryAgent;

/**
 * 完善用户信息activity
 * 
 * @author jiaolei
 */
public class RegInfoActivity extends Activity implements OnClickListener {
    private static String TAG = "RegInfoActivity";

    // flurry logs
    private static final String FLURRY_LOGIN_SUCCESS = "体验者注册完善信息成功";

    EditText mTxtName;

    EditText mTxtPwd;

    Button mBtnRegNow;

    final int REG_PERSONAL_INFO_SUCCESS = 1;

    final int MSG_REG_QUIT = 2;

    final int MSG_REG_SHOW_ERROR = 3;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case REG_PERSONAL_INFO_SUCCESS:
                    sendBroadcast(new Intent(getString(R.string.action_message_login)));
                    // start lead to sync activity
                    Intent i = new Intent(RegInfoActivity.this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                    break;

                case MSG_REG_QUIT:
                    finish();
                    break;

                case MSG_REG_SHOW_ERROR:
                    String str = (String)msg.obj;
                    if (str != null) {
                        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reg_info_activity);

        mTxtName = (EditText)findViewById(R.id.reg_name_input);
        String tryOnceName = GlobalUserInfo.getName();
        mTxtName.setText(tryOnceName);
        mTxtPwd = (EditText)findViewById(R.id.reg_pwd_input);
        mBtnRegNow = (Button)findViewById(R.id.btn_regist_now);
        mBtnRegNow.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTxtName.getText().toString().length() == 0 && GlobalUserInfo.getName() != null) {
            mTxtName.setText(GlobalUserInfo.getName());
        }
    }

    private ProgressDialog mPDlg = null;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_regist_now:
                // check correct first
                if (!checkEditText()) {
                    return;
                }

                if (null == Utils.getActiveNetWorkName(this)) {
                    cn.com.nd.momo.api.util.Utils.displayToast(getString(R.string.error_net_work),
                            Toast.LENGTH_SHORT);
                    return;
                }

                mPDlg = new ProgressDialog(this);
                mPDlg.setMessage("正在发送个人信息...");
                mPDlg.setCancelable(false);
                mPDlg.show();
                final String name = mTxtName.getText().toString();
                final String password = mTxtPwd.getText().toString();

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int user_status = 0;
                            user_status = MoMoHttpApi.updateUserInfo(name, password);
                            // user_status > 0 方表示完善成功
                            if (user_status > 0) {
                                GlobalUserInfo.setUserStatus(user_status);
                                ConfigHelper configHelper = ConfigHelper
                                        .getInstance(RegInfoActivity.this);
                                configHelper.saveKey(ConfigHelper.CONFIG_USER_STATUS, String
                                        .valueOf(user_status));
                                GlobalUserInfo.setName(name);
                                configHelper.commit();
                                mHandler.sendEmptyMessage(REG_PERSONAL_INFO_SUCCESS);
                            } else {
                                Message msg = new Message();
                                msg.what = MSG_REG_SHOW_ERROR;
                                msg.obj = "完善信息失败";
                                mHandler.sendMessage(msg);
                            }
                        } catch (MoMoException e) {
                            Message msg = new Message();
                            msg.what = MSG_REG_SHOW_ERROR;
                            msg.obj = e.getSimpleMsg();
                            mHandler.sendMessage(msg);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Message msg = new Message();
                            msg.what = MSG_REG_SHOW_ERROR;
                            msg.obj = "完善信息失败";
                            mHandler.sendMessage(msg);
                        }

                        // close progress dialog
                        if (mPDlg != null && mPDlg.isShowing()) {
                            mPDlg.dismiss();
                        }
                    }
                };
                t.start();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        // show message box to ensure quit
        Builder b = new AlertDialog.Builder(this);
        b.setTitle("确定退出");
        b.setMessage("退出momo？下次启动momo再设置用户名");
        b.setPositiveButton(getString(R.string.txt_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.sendEmptyMessage(MSG_REG_QUIT);
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

    private boolean checkEditText() {
        String strName = mTxtName.getText().toString();
        String strPwd = mTxtPwd.getText().toString();

        if ("".equals(strName)) {
            mTxtName.setError("请输入名字");
            return false;
        }

        if (strPwd.length() == 0) {
            mTxtPwd.setError("请输入密码");
            return false;
        }

        return true;
    }
}


package cn.com.nd.momo.activity;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;

import cn.com.nd.momo.R;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.RequestUrl;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.view.MarqueeTextView;

/**
 * 设置页面activity
 * 
 * @author jiaolei
 */
public class OptionActivity extends TabActivity implements OnClickListener {

    static private final String TAG = "OptionActivity";
    public static String TEMP_FILE_NAME = "camera-t.jpg";
    public static int CAMERA_PICKED_WITH_DATA = 1;

    public static int PHOTO_PICKED_WITH_DATA = 2;

    public static int CROP_PHOTO_WITH_DATA = 3;


    public static int SIZE_FOR_MY_AVATAR = 120;

    public static int SIZE_FOR_MY_AVATAR_BIG = 480;



    private final int MSG_LOGOUT_END = 1;

    private TextView m_txtLoginUser;

    // name
    private ViewGroup m_btnMyHomePage;

    // change mobile
    private ViewGroup mBtnChangeMobile;

    // change password
    private ViewGroup mBtnChangePassword;


    // about
    private ViewGroup m_btnAbout;

    // help
    private ViewGroup m_btnHelp;

    // feed back
    private ViewGroup m_btnFeedBack;

    // quit
    private Button m_btnQuit;


    // for camera temp file
    private File mTempCameraFile = null;

    // avatar
    private Bitmap mMyAvatar = null;

    private Bitmap mMyAvatarBig = null;



    private ProgressDialog dlgLogout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.option_activity);

        m_txtLoginUser = (TextView)findViewById(R.id.txt_option_login_user_info);
        // name
        m_btnMyHomePage = (ViewGroup)findViewById(R.id.btn_opt_my_home_page);
        m_btnMyHomePage.setOnClickListener(this);



        // about
        m_btnAbout = (ViewGroup)findViewById(R.id.btn_opt_about);
        m_btnAbout.setOnClickListener(this);


        // quit
        m_btnQuit = (Button)findViewById(R.id.btn_quit);
        m_btnQuit.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // change login status string and button enable due to the login status
        if (GlobalUserInfo.getName() != null && GlobalUserInfo.getName().length() != 0) {
            m_txtLoginUser.setText(GlobalUserInfo.getName() + "：设置个人头像 ");
        } else {
            m_txtLoginUser.setText("设置个人头像");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private Handler hLogout = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_LOGOUT_END:
                    if (dlgLogout != null && dlgLogout.isShowing()) {
                        dlgLogout.dismiss();
                    }

                    // after logout lead to login activity
                    Intent iLogin = new Intent(OptionActivity.this, LoginActivity.class);
                    startActivity(iLogin);
                    finish();

                    break;
            }
        }
    };

    @Override
    public void onClick(View v) {
        Intent i;
        switch (v.getId()) {

            case R.id.btn_opt_my_home_page:
                setAvatar();
                break;

            case R.id.btn_opt_about:
                i = new Intent(this, AboutActivity.class);
                startActivity(i);
                break;

            case R.id.btn_quit:
                dlgLogout = ProgressDialog.show(this, "退出登录", "正在退出登录，请稍等...");
                dlgLogout.setCancelable(false);
                Thread tQuit = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        GlobalUserInfo.logout(getApplicationContext());
                        hLogout.sendEmptyMessage(MSG_LOGOUT_END);
                    }
                };
                tQuit.start();
                break;
        }
    }

    private void setAvatar() {

        String[] photoType = new String[] {
                getResources().getString(
                        R.string.btn_my_home_page_photo_from_camera),
                getResources().getString(
                        R.string.btn_my_home_page_photo_from_album)
        };
        new AlertDialog.Builder(this)
                .setTitle(
                        getResources().getString(
                                R.string.txt_my_home_page_photo_title))
                .setItems(photoType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int whichButton) {
                        try {
                            if (whichButton == 0) {
                                // set camera intent
                                Intent localIntent = new Intent(
                                        android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                                // temp file
                                File dir = Environment
                                        .getExternalStorageDirectory();
                                File localTemp = new File(
                                        dir,
                                        TEMP_FILE_NAME);
                                Uri localUri = Uri.fromFile(localTemp);
                                localIntent.putExtra(
                                        MediaStore.EXTRA_OUTPUT, localUri);
                                localIntent
                                        .putExtra(
                                                MediaStore.EXTRA_VIDEO_QUALITY,
                                                100);
                                OptionActivity.this.startActivityForResult(
                                                localIntent,
                                                CAMERA_PICKED_WITH_DATA);
                            } else if (whichButton == 1) {
                                Intent intent = new Intent(
                                        Intent.ACTION_PICK);
                                intent.setDataAndType(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        MediaStore.Images.Media.CONTENT_TYPE);
                                OptionActivity.this.startActivityForResult(
                                                intent,
                                                PHOTO_PICKED_WITH_DATA);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                }).create().show();
    }


    @Override
    protected void onStop() {
        Log.i("momo", "option stop");
        super.onStop();
        if (dlgLogout != null && dlgLogout.isShowing()) {
            dlgLogout.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("momo", "option destory");
        super.onDestroy();
        if (dlgLogout != null && dlgLogout.isShowing()) {
            dlgLogout.dismiss();
        }
    }


    private void uploadAvatar() {
// upload image
        if ((mMyAvatar != null) && (mMyAvatarBig != null)) {
            // show waiting dialog
            final ProgressDialog progressDlg = ProgressDialog.show(this, getResources()
                    .getText(R.string.txt_my_home_page_photo_title), getResources().getText(
                    R.string.msg_my_home_page_photo_waiting_upload));

            new AsyncTask<Void, Integer, Boolean>() {
                @Override
                protected Boolean doInBackground(Void... urls) {
                    try {
                        String avatarUrl = MoMoHttpApi.uploadMyAvatar(mMyAvatar, mMyAvatarBig);
                        GlobalUserInfo.setMyAvatar(mMyAvatar);
                        GlobalUserInfo.setAvatar(avatarUrl);
                        return true;
                    } catch (MoMoException e) {
                        return false;
                    }
                }

                @Override
                protected void onPostExecute(Boolean r) {
                    mMyAvatarBig.recycle();
                    mMyAvatarBig = null;

                    // destroy progress dialog
                    if (progressDlg.isShowing()) {
                        progressDlg.dismiss();
                    }

                    if (r) {
                        Utils.displayToast("更新成功", 0);
                    } else {
                        Utils.displayToast("更新失败", 0);
                    }
                }
            }.execute();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent paramIntent) {
        if (resultCode != RESULT_OK) {
            return;
        }

        Uri photoUri = null;
        if (requestCode == CAMERA_PICKED_WITH_DATA) {
            try {
                // get temp file from camera
                File sdDir = Environment.getExternalStorageDirectory();
                String tempPicName = TEMP_FILE_NAME;
                mTempCameraFile = new File(sdDir, tempPicName);
                mMyAvatarBig = BitmapToolkit.loadLocalBitmapRoughScaled(mTempCameraFile
                        .getAbsolutePath(), 1200);
                if (mMyAvatarBig == null) {
                    if (paramIntent.getData() == null) {
                        return;
                    }
                    photoUri = paramIntent.getData();
                    Cursor cursor = this.getContentResolver().query(photoUri, null, null, null,
                            null);
                    cursor.moveToFirst();
                    int index = cursor.getColumnIndex("_data");
                    if (index < 0) {
                        return;
                    }
                    String picPath = cursor.getString(index);
                    mMyAvatarBig = BitmapToolkit.loadLocalBitmapRoughScaled(picPath, 600);
                    if (mMyAvatarBig == null) {
                        return;
                    }
                }

                // compress to right size
                mMyAvatarBig = BitmapToolkit.compress(mMyAvatarBig, SIZE_FOR_MY_AVATAR_BIG);
                mMyAvatar = BitmapToolkit.compress(mMyAvatarBig, SIZE_FOR_MY_AVATAR);

                uploadAvatar();
            } catch (Exception localException) {
                Log.e(TAG, localException.getMessage());
                if (mTempCameraFile != null) {
                    mTempCameraFile.delete();
                    mTempCameraFile = null;
                }
                return;
            }

        } else if (requestCode == PHOTO_PICKED_WITH_DATA) {
            photoUri = paramIntent.getData();
            if (photoUri != null) {
                // 获取文件的绝对路径
                String path = "";
                String[] projection = {
                        BaseColumns._ID, MediaStore.MediaColumns.DATA,
                        MediaStore.MediaColumns.SIZE
                };
                Cursor cursor = MediaStore.Images.Media.query(this.getContentResolver(), photoUri,
                        projection);
                if (cursor != null && cursor.moveToNext()) {
                    path = cursor.getString(cursor
                            .getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                }
                // compress to right size
                mMyAvatarBig = BitmapToolkit.loadLocalBitmapExactScaled(path,
                        SIZE_FOR_MY_AVATAR_BIG);
                mMyAvatar = BitmapToolkit.loadLocalBitmapExactScaled(path,
                        SIZE_FOR_MY_AVATAR);
                if (mMyAvatarBig == null || mMyAvatar == null) {
                    return;
                }

                uploadAvatar();

            }
        }  else {
        }
    }


}

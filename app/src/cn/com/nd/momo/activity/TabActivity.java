
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.view.KeyEvent;
import cn.com.nd.momo.api.util.Log;

public class TabActivity extends Activity {
    private final String TAG = "TabActivity";

    // 退出程序监听
    private int backCount = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown" + event.getRepeatCount());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (1 == backCount) {
                // Process.killProcess(Process.myPid());
                // Log.i(TAG, "backcount=1");
                // if (!GlobalUserInfo.openMQAfterExit()) { // 程序退出后关闭服务
                // Intent intent = new Intent(this, MQService.class);
                // this.stopService(intent);
                // }
                return false;
            }
            if (0 == backCount) {

                // check if net sync is doing
                // if (GlobalUserInfo.isNetSyncDoing()) {
                // Toast.makeText(this, getString(R.string.msg_exit_wait_sync),
                // 0).show();
                // } else {
                // Toast.makeText(this, getString(R.string.msg_exit), 0).show();
                // backCount = 1;
                // Timer timer = new Timer();
                // timer.schedule(new TimerTask() {
                // @Override
                // public void run() {
                // backCount = 0;
                // }
                // }, 2000);
                // }

                return false;
            }
        }
        // backCount = 0;
        return super.onKeyDown(keyCode, event);
    }

}

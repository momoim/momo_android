
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.Log;

import cn.com.nd.momo.manager.GlobalUserInfo;


/**
 * 启动初始activity
 * 
 * @author jiaolei
 */
public class LauncherActivity extends Activity {
    private String TAG = "LauncherActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.welcome);
        startApp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //startApp();
    }

    private void startApp() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Intent i = null;
                GlobalUserInfo.checkLoginStatus(getApplicationContext());
                if (!GlobalUserInfo.hasLogined()) {
                    Log.d(TAG, "login called");
                    i = new Intent(getApplicationContext(), LoginActivity.class);
                } else {
                    i = new Intent(getApplicationContext(), MainActivity.class);
                }
                startActivity(i);
                Log.d(TAG, "launcher end");
                finish();
            }
        };

        View welcomeContainer = this.findViewById(R.id.welcome_container);
        welcomeContainer.post(r);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.welcome_container));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup)view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup)view).getChildAt(i));
            }
            ((ViewGroup)view).removeAllViews();
        }
    }

}

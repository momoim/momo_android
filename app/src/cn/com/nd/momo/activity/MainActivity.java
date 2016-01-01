
package cn.com.nd.momo.activity;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.UpgradeInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.manager.GlobalUserInfo;




public class MainActivity extends ActivityGroup {
    private static final String TAG = "MainActivity";


    public static String TAB_DYNAMIC = "dynamic";
    public static String TAG_OPTION = "option";
    public static int TAB_TOGGLED = 2;

    private boolean mHasInited = false;


    private TabHost mTabHost;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        Log.d(TAG, "intent action:" + intent.getAction());
        if (intent.getAction() != null
                && intent.getAction().equals(getString(R.string.action_message_income))) {
            if (GlobalUserInfo.getUserStatus() < GlobalUserInfo.STATUS_VERIFY_USER) {
                Intent i = new Intent(this, RegInfoActivity.class);
                startActivity(i);
                finish();
                return;
            }
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);

        int tabIndex = 0;
        initActivity(tabIndex);

        sendBroadcast(new Intent(getString(R.string.action_message_launch)));
        Log.d(TAG, "On Create end");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            initActivity(0);
        } else {
            finish();
        }
    }

    private void initActivity(int tabIndex) {
        this.setVisible(true);
        Log.d(TAG, "initActivity called");

        if (mHasInited) {
            return;
        }

        mTabHost = (TabHost) findViewById(R.id.tabhost);
        mTabHost.setup(this.getLocalActivityManager());


        // add statuses
        TabSpec mTab = mTabHost.newTabSpec(TAB_DYNAMIC);
        mTab.setIndicator(inflaterTab(getResources().getDrawable(R.drawable.ic_dynamic),
                getResources().getString(R.string.tab_txt_dynamic)));
        mTab.setContent(new Intent(this, Statuses_Activity.class));
        mTabHost.addTab(mTab);

        // add option
        mTab = mTabHost.newTabSpec(TAG_OPTION);
        mTab.setIndicator(inflaterTab(getResources().getDrawable(R.drawable.ic_option),
                getResources().getString(R.string.tab_txt_option)));
        mTab.setContent(new Intent(this, OptionActivity.class));
        mTabHost.addTab(mTab);

        View view = mTabHost.getTabWidget().getChildTabViewAt(1);
        view.setOnTouchListener(new TabHost.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final EditText searchBar = (EditText) mTabHost.getCurrentView().findViewById(
                        R.id.txt_search);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    ListView contactListView = (ListView) mTabHost.getCurrentView()
                            .findViewById(
                                    R.id.listContacts);

                    if (searchBar != null && contactListView != null) {
                        contactListView.setSelection(0);
                        View view = contactListView.getFocusedChild();
                        if (view != null) {
                            contactListView.clearChildFocus(view);
                        }
                        contactListView.clearFocus();
                        searchBar.clearFocus();
                        cn.com.nd.momo.util.Utils.showKeyboard(getApplicationContext(), searchBar);
                        searchBar.requestFocus();
                        return true;
                    } else {
                        Log.i(TAG, "seach edittext not find");
                    }
                }

                return false;
            }
        });

        mTabHost.setCurrentTab(tabIndex);

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                int tabNow = mTabHost.getCurrentTab();
                ConfigHelper.getInstance(getApplication()).saveIntKey(ConfigHelper.CONFIG_KEY_LAST_TAB, tabNow);
            }
        });

        Log.w(TAG, "begin bind service task");

        mHasInited = true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent called");
        super.onNewIntent(intent);
    }

    private View inflaterTab(Drawable icon, String strText) {
        View view = LayoutInflater.from(this).inflate(R.layout.tab_item_layout, null);
        ViewGroup layout = (ViewGroup) view.findViewById(R.id.tab_bar_layout);
        layout.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.tab_selector));
        ImageView imageView = (ImageView) view.findViewById(R.id.tab_imageView);
        imageView.setImageDrawable(icon);
        TextView textView = (TextView) view.findViewById(R.id.tab_textView);
        textView.setText(strText);

        return view;
    }

    public void setToggleIcon(int nToggleState) {

        if (mTabHost.getCurrentTab() == 0) {
            ViewGroup layout = (ViewGroup) mTabHost.getCurrentTabView().findViewById(
                    R.id.tab_bar_layout);
            if (layout != null) {
                if (nToggleState == MainActivity.TAB_TOGGLED) {
                    layout.setBackgroundResource(R.drawable.tab_selector_toggled);
                } else {
                    layout.setBackgroundResource(R.drawable.tab_selector);
                }
            }

        } else if (mTabHost.getCurrentTab() == 2) {
            ViewGroup layout = (ViewGroup) mTabHost.getCurrentTabView().findViewById(
                    R.id.tab_bar_layout);
            if (layout != null) {
                if (nToggleState == MainActivity.TAB_TOGGLED) {
                    layout.setBackgroundResource(R.drawable.tab_selector_toggled);
                } else {
                    layout.setBackgroundResource(R.drawable.tab_selector);
                }
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy");
        super.onDestroy();


        System.gc();
    }
}

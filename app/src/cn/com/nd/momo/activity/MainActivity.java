package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.ActivityGroup;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.types.OAuthInfo;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.util.Timer;
import cn.com.nd.momo.util.Utils;

import static android.os.SystemClock.uptimeMillis;


public class MainActivity extends ActivityGroup {
    private static final String TAG = "MainActivity";

    public static String TAB_DYNAMIC = "dynamic";
    public static String TAG_OPTION = "option";
    public static String TAG_FRIEND = "friend";


    private boolean mHasInited = false;

    private Timer refreshTokenTimer;
    private int refreshErrorCount;

    private TabHost mTabHost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Intent intent = getIntent();
        Log.d(TAG, "intent action:" + intent.getAction());
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_activity);

        int tabIndex = 0;
        initActivity(tabIndex);

        sendBroadcast(new Intent(getString(R.string.action_message_launch)));

        int now = Utils.getNow();
        if (now >= GlobalUserInfo.getExpireTS() - 60) {
            refreshToken();
        } else {
            int t = GlobalUserInfo.getExpireTS() - 60 - now;
            refreshTokenDelay(t);
        }
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

        mTab = mTabHost.newTabSpec(TAG_FRIEND);
        mTab.setIndicator(inflaterTab(getResources().getDrawable(R.drawable.ic_contact),
                getResources().getString(R.string.tab_txt_friend)));
        mTab.setContent(new Intent(this, FriendActivity.class));
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

    private void refreshToken() {
        new AsyncTask<Void, Integer, OAuthInfo>() {
            @Override
            protected OAuthInfo doInBackground(Void... urls) {
                try {
                    OAuthInfo authInfo = MoMoHttpApi.refreshAccessToken(GlobalUserInfo.getRefreshToken());
                    return authInfo;
                } catch (MoMoException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(OAuthInfo result) {
                int now = Utils.getNow();
                if (result != null) {
                    Log.i(TAG, "token refreshed");
                    refreshErrorCount = 0;
                    GlobalUserInfo.setOAuthToken(result.mAccessToken, result.mRefreshToken, result.mExpireTS);
                    int ts = result.mExpireTS - 60 - now;
                    if (ts <= 0) {
                        android.util.Log.w(TAG, "expire timestamp:" + result.mExpireTS);
                        return;
                    }
                    refreshTokenDelay(ts);
                } else {
                    Log.i(TAG, "refresh token error");
                    refreshErrorCount++;
                    refreshTokenDelay(60 * refreshErrorCount);
                }
            }
        }.execute();
    }

    private void refreshTokenDelay(int t) {
        if (refreshTokenTimer != null) {
            refreshTokenTimer.suspend();
        }

        refreshTokenTimer = new Timer() {
            @Override
            protected void fire() {
                refreshToken();
            }
        };
        refreshTokenTimer.setTimer(uptimeMillis() + t*1000);
        refreshTokenTimer.resume();
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
    }
}

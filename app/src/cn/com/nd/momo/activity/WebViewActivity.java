
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.Log;

public class WebViewActivity extends Activity {
    /** Called when the activity is first created. */
    String TAG = "WebViewActivity";

    private WebView wv;

    private View titleLayout;

    private TextView titleView;

    private ProgressDialog pd;

    private boolean webviewCanBack = true;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {// 定义一个Handler，用于处理下载线程与UI间通讯
            if (!Thread.currentThread().isInterrupted()) {
                titleView = (TextView)findViewById(R.id.title_view);
                switch (msg.what) {
                    case 0:
                        titleView.setText("载入中...");
                        if (pd != null && !pd.isShowing()) {
                            pd.show();// 显示进度对话框
                        }
                        break;
                    case 1:
                        titleView.setText(wv.getTitle());
                        pd.dismiss();// 隐藏进度对话框，不可使用dismiss()、cancel(),否则再次调用show()时，显示的对话框小圆圈不会动。
                        break;
                }
            }
            super.handleMessage(msg);
        }
    };

    public static String EXTARS_WEBVIEW_URL = "extars_webview_url";

    public static String EXTARS_WEBVIEW_NEED_TITLE = "extars_webview_need_title";

    public static String EXTARS_WEBVIEW_CAN_CALL_BACK = "extars_webview_need_title";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.webview);

        webviewCanBack = getIntent().getBooleanExtra(EXTARS_WEBVIEW_CAN_CALL_BACK, true);
        titleLayout = findViewById(R.id.layout_title);
        titleView = (TextView)findViewById(R.id.title_view);

        init();// 执行初始化函数
        String url = getIntent().getExtras().getString(EXTARS_WEBVIEW_URL);
        boolean needTitle = getIntent().getExtras().getBoolean(EXTARS_WEBVIEW_NEED_TITLE, true);
        if (!needTitle) {
            titleLayout.setVisibility(View.GONE);
        } else {
            titleLayout.setVisibility(View.VISIBLE);
        }
        Log.i(TAG, "url : " + url);
        loadurl(wv, url);
    }

    public void init() {// 初始化
        wv = (WebView)findViewById(R.id.webview);
        wv.getSettings().setJavaScriptEnabled(true);// 可用JS
        wv.setScrollBarStyle(0);// 滚动条风格，为0就是不给滚动条留空间，滚动条覆盖在网页上
        wv.getSettings().setSupportZoom(true);
        wv.getSettings().setBuiltInZoomControls(true);
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                Log.i(TAG, "url click");
                handler.sendEmptyMessage(0);
                return super.shouldOverrideUrlLoading(view, url);
            }// 重写点击动作,用webview载入
        });
        wv.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {
                // 载入进度改变而触发
                Log.i(TAG, "progress " + progress);
                super.onProgressChanged(view, progress);
                if (progress == 100) {
                    handler.sendEmptyMessage(1);// 如果全部载入,隐藏进度对话框
                }
            }
        });

        pd = new ProgressDialog(WebViewActivity.this);
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setMessage("请稍候...");
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {// 捕捉返回键
        if (webviewCanBack && (keyCode == KeyEvent.KEYCODE_BACK) && wv.canGoBack()) {
            wv.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void loadurl(final WebView view, final String url) {
        new Thread() {
            @Override
            public void run() {
                handler.sendEmptyMessage(0);
                try {
                    view.loadUrl(url);// 载入网页
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pd != null && pd.isShowing()) {
            pd.dismiss();
        }
    }

}

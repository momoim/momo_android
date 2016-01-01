
package cn.com.nd.momo.im.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.ConfigHelper;

public class SmsSettingActivity extends Activity implements OnClickListener {
    // 拦截所有短信选项
    private ViewGroup mOptInterceptAll;

    CheckBox mChkInterceptAll;

    // 配置文件里已经设置的值——是否拦截所有短信
    private boolean mIsInterceptAll;

    CheckBox mChkInterceptMomo;

    // 拦截mo短信选项
    private ViewGroup mOptInterceptMomo;

    // 配置文件里已经设置的值——是否拦截MO短信
    private boolean mIsInterceptMomo;

    private ConfigHelper mConfigHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.sms_setting);

        mOptInterceptAll = (ViewGroup)findViewById(R.id.sms_opt_all_intercep);
        mOptInterceptAll.setOnClickListener(this);

        mChkInterceptAll = (CheckBox)findViewById(R.id.sms_chk_option_all);

        mOptInterceptMomo = (ViewGroup)findViewById(R.id.sms_opt_momo_intercep);
        mOptInterceptMomo.setOnClickListener(this);
        mChkInterceptMomo = (CheckBox)findViewById(R.id.sms_chk_option_momo);

        mConfigHelper = ConfigHelper.getInstance(this);
        mIsInterceptAll = mConfigHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_ALL,
                true);
        mChkInterceptAll.setChecked(mIsInterceptAll);
        mIsInterceptMomo = mConfigHelper.loadBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_MOMO,
                true);
        mChkInterceptMomo.setChecked(mIsInterceptMomo);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sms_opt_all_intercep:
                mIsInterceptAll = !mIsInterceptAll;
                mChkInterceptAll.setChecked(mIsInterceptAll);
                mConfigHelper
                        .saveBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_ALL, mIsInterceptAll);
                if (mIsInterceptAll && !mIsInterceptMomo) {
                    mIsInterceptMomo = true;
                    mChkInterceptMomo.setChecked(mIsInterceptMomo);
                    mConfigHelper
                            .saveBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_MOMO,
                                    mIsInterceptMomo);
                }
                mConfigHelper.commit();
                break;
            case R.id.sms_opt_momo_intercep:
                mIsInterceptMomo = !mIsInterceptMomo;
                mChkInterceptMomo.setChecked(mIsInterceptMomo);
                mConfigHelper
                        .saveBooleanKey(ConfigHelper.CONFIG_KEY_INTERCEPT_MOMO, mIsInterceptMomo);
                mConfigHelper.commit();
                break;

            default:
                break;
        }

    }

}

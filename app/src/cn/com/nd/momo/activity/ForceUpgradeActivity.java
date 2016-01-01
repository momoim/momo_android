
package cn.com.nd.momo.activity;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.UpgradeInfo;
import cn.com.nd.momo.manager.UpgradeMgr;

public class ForceUpgradeActivity extends Activity implements OnClickListener {
    private Handler mHandler = new Handler();

    private TextView txtUpgradeInfo;

    private TextView btnUpgrade;

    private UpgradeInfo mUpgradeInfo;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.force_upgrade);

        txtUpgradeInfo = (TextView)findViewById(R.id.txt_upgrade_info);
        btnUpgrade = (TextView)findViewById(R.id.btn_upgrade_client);

        btnUpgrade.setOnClickListener(this);
        btnUpgrade.setEnabled(false);

        // show wait
        cn.com.nd.momo.util.Utils
                .showWaitDialog(getString(R.string.txt_option_version_check), this);

        new Thread() {
            @Override
            public void run() {
                // check new version
                int versionCode = 0;
                try {
                    versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                } catch (NameNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return;
                }
                mUpgradeInfo = UpgradeMgr.GetInstance().postUpgrade(String.valueOf(versionCode));

                // back to ui thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // hide wait
                        cn.com.nd.momo.util.Utils.hideWaitDialog();

                        // if exit new verstion
                        if (mUpgradeInfo != null) {
                            final String message = getString(R.string.txt_option_upgrade_size)
                                    + ": "
                                    + cn.com.nd.momo.util.Utils.formatSize2String(Long
                                            .valueOf(mUpgradeInfo.fileSize))
                                    + "\n"
                                    + getString(R.string.txt_option_upgrade_version)
                                    + ": "
                                    + mUpgradeInfo.currentVersion
                                    + "\n"
                                    + getString(R.string.txt_option_upgrade_remark)
                                    + ": "
                                    + mUpgradeInfo.remark
                                    + "\n"
                                    + getString(R.string.txt_option_upgrade_date)
                                    + ": "
                                    + cn.com.nd.momo.util.Utils.formatDateToNormalRead(String
                                            .valueOf(Long
                                                    .valueOf(mUpgradeInfo.publishDate) * 1000));

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    txtUpgradeInfo.setText(message);
                                    btnUpgrade.setEnabled(true);
                                }
                            });
                        }
                    }
                });
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        UpgradeMgr.down(ForceUpgradeActivity.this, mUpgradeInfo.downloadUrl);
    }
}

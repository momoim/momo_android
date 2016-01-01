/**
 * 
 */

package cn.com.nd.momo.view;

import android.app.Dialog;
import android.content.Context;
import cn.com.nd.momo.R;

/**
 * @date Nov 24, 2011
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class LoadingDialog {
    private Dialog mDialog;

    public LoadingDialog(Context context) {
        mDialog = new Dialog(context, R.style.popu_contact_avatar_dialog);
        mDialog.setContentView(R.layout.contact_info_avatar);
    }

    public void show() {
        if (mDialog != null) {
            mDialog.show();
        }
    }

    public void dismiss() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }
}


package cn.com.nd.momo.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import cn.com.nd.momo.R;

/**
 * @date Nov 24, 2011
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class ConfirmDialog {
    public ConfirmDialog(Context context, String msg, final ConfirmListener l) {
        new AlertDialog.Builder(context)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.txt_confirm)
                .setMessage(msg)
                .setPositiveButton(R.string.txt_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                l.onConfirmYes();
                            }
                        })
                .setNegativeButton(R.string.txt_cancel,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                l.onConfirmNo();
                            }
                        }).show();
    }

    public static interface ConfirmListener {
        void onConfirmYes();

        void onConfirmNo();
    }
}

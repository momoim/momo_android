
package cn.com.nd.momo.view;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;

public class TouchCloseDialog extends Dialog {

    public TouchCloseDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        // TODO Auto-generated constructor stub
    }

    public TouchCloseDialog(Context context, int theme) {
        super(context, theme);
        // TODO Auto-generated constructor stub
    }

    public TouchCloseDialog(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_OUTSIDE
                || event.getAction() == MotionEvent.ACTION_UP) {
            dismiss();
        }
        return super.onTouchEvent(event);
    }

}

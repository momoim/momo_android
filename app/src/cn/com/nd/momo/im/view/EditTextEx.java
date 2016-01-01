
package cn.com.nd.momo.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * TextView扩展，方便控制显示录音按钮
 * 
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class EditTextEx extends EditText {

    public EditTextEx(Context context) {
        super(context);
        init(context);
    }

    public EditTextEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditTextEx(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            if (this.mOnEditTextListener != null) {
                this.mOnEditTextListener.onIMEClose();
            }
            return false;
        }
        return super.dispatchKeyEvent(event);
    }

    public void setmOnEditTextListener(OnEditTextListener mOnEditTextListener) {
        this.mOnEditTextListener = mOnEditTextListener;
    }

    public OnEditTextListener getmOnEditTextListener() {
        return mOnEditTextListener;
    }

    private OnEditTextListener mOnEditTextListener;

    public interface OnEditTextListener {
        void onIMEClose();
    }
}

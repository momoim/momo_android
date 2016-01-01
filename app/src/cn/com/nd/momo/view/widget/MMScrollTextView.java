
package cn.com.nd.momo.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 扩展TextView以实现多条TextView能同时滚动的效果，及字幕的走马灯效果
 * 
 * @author Wuwl
 */
public class MMScrollTextView extends TextView {

    public MMScrollTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction,
            Rect previouslyFocusedRect) {
        if (focused) {
            super.onFocusChanged(focused, direction, previouslyFocusedRect);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        if (hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    private void init() {
        setSingleLine();
        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontallyScrolling(true);
        // scroll forever.
        setMarqueeRepeatLimit(-1);
    }
}

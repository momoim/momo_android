
package cn.com.nd.momo.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import cn.com.nd.momo.R;

/**
 * 联系人显示列表右侧字母列表视图
 * 
 * @author jiaolei
 */
public class AlphabeticBar extends View {

    OnTouchingLetterChangedListener onTouchingLetterChangedListener;

    String[] b = {
            "#", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"
            , "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };

    int choose = -1;

    Paint paint = new Paint();

    boolean showBkg = false;

    public AlphabeticBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public AlphabeticBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AlphabeticBar(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (showBkg) {
            canvas.drawColor(getResources()
                    .getColor(R.color.list_indexer_moving));
        }

        Configuration cf = getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == Configuration.ORIENTATION_PORTRAIT) {
            paint.setTextSize(getResources()
                    .getDimension(R.dimen.contact_letter_font_size_portrait));
        } else {
            paint.setTextSize(getResources().getDimension(
                    R.dimen.contact_letter_font_size_landscape));
        }
        paint.setColor(Color.parseColor("#7e7e7f"));
        paint.setAntiAlias(true);

        float height = getHeight();
        int width = getWidth();
        float singleHeight = height / b.length - 0.2f;
        for (int i = 0; i < b.length; i++) {

            // if(i == choose){
            // paint.setColor(Color.parseColor("#3399ff"));
            // paint.setFakeBoldText(true);
            // }

            float xPos = width / 2 - paint.measureText(b[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            canvas.drawText(b[i], xPos, yPos, paint);
        }
        paint.reset();

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int)(y / getHeight() * b.length);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                showBkg = true;
                if (oldChoose != c) {
                    if (c > -1 && c < b.length) {
                        if (listener != null)
                            listener.onTouchingLetterChanged(b[c]);
                        choose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                showBkg = true;
                if (oldChoose != c) {
                    if (c > -1 && c < b.length) {
                        if (listener != null)
                            listener.onTouchingLetterChanged(b[c]);
                        choose = c;
                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                showBkg = false;
                choose = -1;
                invalidate();
                if (listener != null)
                    listener.onChangeIsTouch(false);
                break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    public void setOnTouchingLetterChangedListener(
            OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {
        public void onTouchingLetterChanged(String s);

        public void onChangeIsTouch(boolean isTouch);
    }

}

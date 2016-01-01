
package cn.com.nd.momo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageButton;
import cn.com.nd.momo.R;

public final class DrawView extends ImageButton {
    private Bitmap mBmp = null;

    private float mDistance = 0.0F;

    private boolean mIsStart = false;

    private long mLastTime;

    private Paint mTxtPaint;

    public DrawView(Context paramContext) {
        super(paramContext);
        long time = System.currentTimeMillis();
        mLastTime = time;
        init();
    }

    public DrawView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        long time = System.currentTimeMillis();
        mLastTime = time;
        init();
    }

    public void setStart(boolean paramBoolean) {
        mIsStart = paramBoolean;
        invalidate();
    }

    protected void init() {
        mTxtPaint = new Paint();

        // init bitmap
        mBmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_title_refresh);
        // float scale = 0.5f;
        // int w = (int)(mBmp.getWidth()*scale);
        // int h = (int)(mBmp.getHeight()*scale);
        // mBmp = Bitmap.createScaledBitmap(mBmp, w, h, false);

        if (getLayoutParams() != null) {
            getLayoutParams().height = mBmp.getHeight();
            getLayoutParams().width = mBmp.getWidth();
        }
    }

    @Override
    protected void onDraw(Canvas paramCanvas) {

        // playing
        if (mIsStart) {
            // get distance
            long currentTime = System.currentTimeMillis();
            long lastTime = mLastTime;
            mDistance += (currentTime - lastTime);

            rotateView(paramCanvas);
            mLastTime = currentTime;
            invalidate();
        } else {
            super.onDraw(paramCanvas);
        }
    }

    private void rotateView(Canvas paramCanvas) {
        // int degree = ((int)(mDistance/3/45))*45;
        float degree = mDistance / 3f;
        paramCanvas.rotate(-degree, this.getWidth() / 2, this.getHeight() / 2);
        paramCanvas.drawBitmap(mBmp, this.getWidth() / 2 - mBmp.getWidth() / 2, this.getHeight()
                / 2 - mBmp.getHeight() / 2, mTxtPaint);
    }

    public boolean isStarted() {
        return mIsStart;
    }
}

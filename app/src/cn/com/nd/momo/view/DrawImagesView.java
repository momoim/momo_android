
package cn.com.nd.momo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

public final class DrawImagesView extends ImageButton {
    // private Bitmap mBmp = null;
    private float mDistance = 0.0F;

    private boolean mIsStart = false;

    private long mLastTime;

    private Paint mTxtPaint;

    public DrawImagesView(Context paramContext) {
        super(paramContext);
        long time = System.currentTimeMillis();
        mLastTime = time;
        init();
    }

    public DrawImagesView(Context paramContext, AttributeSet paramAttributeSet) {
        super(paramContext, paramAttributeSet);
        long time = System.currentTimeMillis();
        mLastTime = time;
        init();
    }

    private int[] mIds;

    private int mIdIndex = 0;

    public void setStart(boolean paramBoolean, int[] ids) {
        mIsStart = paramBoolean;
        invalidate();
        mIds = ids;
    }

    public void stop() {
        mIsStart = false;
    }

    protected void init() {
        mTxtPaint = new Paint();

        // init bitmap
        // mBmp = BitmapFactory.decodeResource(getResources(),
        // R.drawable.ic_title_refresh);
        // float scale = 0.5f;
        // int w = (int)(mBmp.getWidth()*scale);
        // int h = (int)(mBmp.getHeight()*scale);
        // mBmp = Bitmap.createScaledBitmap(mBmp, w, h, false);

        if (getLayoutParams() != null) {
            // getLayoutParams().height = mBmp.getHeight();
            // getLayoutParams().width = mBmp.getWidth();
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
        }
        else {
            super.onDraw(paramCanvas);
        }
    }

    private void rotateView(Canvas paramCanvas) {
        // int degree = ((int)(mDistance/3/45))*45;
        // float degree = mDistance/3f;
        // paramCanvas.rotate(-degree, this.getWidth()/2,this.getHeight()/2);
        if (mIdIndex >= mIds.length * 10) {
            mIdIndex = 0;
        }
        int index = mIdIndex / 10;
        BitmapDrawable bd = (BitmapDrawable)(this.getContext().getResources()
                .getDrawable(mIds[index]));
        Bitmap mBmp = bd.getBitmap();
        mBmp = Bitmap.createScaledBitmap(mBmp, this.getWidth(), this.getHeight(), true);
        paramCanvas.drawBitmap(mBmp, 0, 0, mTxtPaint);
        mIdIndex += 3;
    }

    public boolean isStarted() {
        return mIsStart;
    }
}

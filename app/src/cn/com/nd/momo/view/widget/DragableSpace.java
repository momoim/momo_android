
package cn.com.nd.momo.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.Scroller;
import cn.com.nd.momo.api.util.Log;

public class DragableSpace extends ViewGroup {
    private Scroller mScroller;

    private VelocityTracker mVelocityTracker;

    private OnScreenChangedListener onScreenChangedListener;

    private int mScrollX = 0;

    private int mPreScreen = 0;

    private int mCurrentScreen = 0;

    private float mLastMotionX;

    private float mLastMotionY;

    private MOVE_TYPE mMoveType = MOVE_TYPE.NOTMOVE;

    private boolean isScrollFinished = true;

    private static final String LOG_TAG = "DragableSpace";

    private static final int SNAP_VELOCITY = 1000;

    private final static int TOUCH_STATE_REST = 0;

    private final static int TOUCH_STATE_SCROLLING = 1;

    private int mTouchState = TOUCH_STATE_REST;

    private int mTouchSlop = 0;

    public DragableSpace(Context context) {
        super(context);
        mScroller = new Scroller(context);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        this.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.FILL_PARENT));
    }

    public DragableSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        this.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.FILL_PARENT));

        // TypedArray
        // a=getContext().obtainStyledAttributes(attrs,R.style.DragableSpace);
        // mCurrentScreen = a.getInteger(R.style.DragableSpace_default_screen,
        // 0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            if (mMoveType == MOVE_TYPE.VMOVE) {
                return false;
            } else if (mTouchState != TOUCH_STATE_REST) {
                return true;
            }
        }

        final float x = ev.getX();
        final float y = ev.getY();

        switch (action) {
            case MotionEvent.ACTION_MOVE:
                // Log.i(LOG_TAG, "event : move on intercept");

                final int xDiff = (int)Math.abs(x - mLastMotionX);

                boolean xMoved = xDiff > mTouchSlop;

                if (xMoved) {
                    // Scroll if the user moved far enough along the X axis
                    mTouchState = TOUCH_STATE_SCROLLING;
                }
                break;

            case MotionEvent.ACTION_DOWN:
                // Remember location of down touch
                mLastMotionX = x;
                mLastMotionY = y;

                mTouchState = mScroller.isFinished() ? TOUCH_STATE_REST : TOUCH_STATE_SCROLLING;
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // Release the drag
                mTouchState = TOUCH_STATE_REST;
                break;
        }

        return mTouchState != TOUCH_STATE_REST;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();
        if (getChildCount() <= 1 || action == MotionEvent.ACTION_MOVE
                && mMoveType == MOVE_TYPE.VMOVE) {
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
            final int deltaY = (int)(mLastMotionY - y);
            mLastMotionY = y;
            mLastMotionX = x;
            View child = this.getChildAt(mCurrentScreen);
            // Log.i(LOG_TAG, "onVScroll: " + deltaY);
            if (onVScrollListener != null) {
                onVScrollListener.onVScroll(child, deltaY);
            } else {
                if (child instanceof ScrollView) {
                    ((ScrollView)child).scrollBy(0, deltaY);
                }
            }
            return false;
        }

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                Log.i(LOG_TAG, "event : down");

                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionX = x;
                mLastMotionY = y;
                break;
            case MotionEvent.ACTION_MOVE:
                // Log.i(LOG_TAG, "event : move");
                // if (mTouchState == TOUCH_STATE_SCROLLING) {
                // Scroll to follow the motion event
                final int deltaY = (int)(mLastMotionY - y);
                mLastMotionY = y;
                final int deltaX = (int)(mLastMotionX - x);
                mLastMotionX = x;

                if (mMoveType == MOVE_TYPE.NOTMOVE) {
                    Log.i(LOG_TAG, "event : move horable: x: " + deltaX + " y: " + deltaY);
                    if (Math.abs(deltaY) > Math.abs(deltaX)) {
                        Log.i(LOG_TAG, "event : move horable v");
                        mMoveType = MOVE_TYPE.VMOVE;
                        return false;
                    } else {
                        Log.i(LOG_TAG, "event : move horable h");
                        mMoveType = MOVE_TYPE.HMOVE;
                    }
                }

                // Log.i(LOG_TAG, "event : move, deltaX " + deltaX +
                // ", mScrollX " + mScrollX);

                if (deltaX < 0) {
                    if (mScrollX > 0) {
                        scrollBy(Math.max(-mScrollX, deltaX), 0);
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1)
                            .getRight()
                            - mScrollX - getWidth();
                    if (availableToScroll > 0) {
                        scrollBy(Math.min(availableToScroll, deltaX), 0);
                    }
                }
                // }
                break;
            case MotionEvent.ACTION_UP:
                Log.i(LOG_TAG, "event : up");
                // if (mTouchState == TOUCH_STATE_SCROLLING) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000);
                int velocityX = (int)velocityTracker.getXVelocity();

                // if (mMoveType == MOVE_TYPE.HMOVE) {
                if (velocityX > SNAP_VELOCITY && mCurrentScreen > 0) {
                    // Fling hard enough to move left
                    snapToScreen(mCurrentScreen - 1);
                } else if (velocityX < -SNAP_VELOCITY
                        && mCurrentScreen < getChildCount() - 1) {
                    // Fling hard enough to move right
                    snapToScreen(mCurrentScreen + 1);
                } else {
                    snapToDestination();
                }
                // }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                // }
                mTouchState = TOUCH_STATE_REST;
                // reset move type
                mMoveType = MOVE_TYPE.NOTMOVE;
                break;
            case MotionEvent.ACTION_CANCEL:
                Log.i(LOG_TAG, "event : cancel");
                mTouchState = TOUCH_STATE_REST;
        }
        mScrollX = this.getScrollX();

        return true;
    }

    private void snapToDestination() {
        final int screenWidth = getWidth();
        final int whichScreen = (mScrollX + (screenWidth / 2)) / screenWidth;
        Log.i(LOG_TAG, "from des");
        snapToScreen(whichScreen);
    }

    public void snapToScreen(int whichScreen) {
        Log.i(LOG_TAG, "snap To Screen " + whichScreen);
        mPreScreen = mCurrentScreen;
        mCurrentScreen = whichScreen;
        final int newX = whichScreen * getWidth();
        final int delta = newX - mScrollX;
        mScroller.startScroll(mScrollX, 0, delta, 0, Math.abs(delta) * 2);
        invalidate();
    }

    public void setToScreen(int whichScreen) {
        Log.i(LOG_TAG, "set To Screen " + whichScreen);
        mCurrentScreen = whichScreen;
        final int newX = whichScreen * getWidth();
        mScroller.startScroll(newX, 0, 0, 0, 10);
        invalidate();
    }

    public void setOnScreenChangedListener(OnScreenChangedListener onScreenChangedListener) {
        this.onScreenChangedListener = onScreenChangedListener;
    }

    public boolean isScrollFinished() {
        return isScrollFinished;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childLeft = 0;

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child
                        .getMeasuredHeight());
                childLeft += childWidth;
            }
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("error mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("error mode.");
        }

        // The children are given the same width and height as the workspace
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }
        Log.i(LOG_TAG, "moving to screen " + mCurrentScreen);
        scrollTo(mCurrentScreen * width, 0);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            isScrollFinished = false;
            mScrollX = mScroller.getCurrX();
            scrollTo(mScrollX, 0);
            postInvalidate();
        } else {
            if (onScreenChangedListener != null) {
                if (mCurrentScreen != mPreScreen) {
                    onScreenChangedListener.onScreenChanged(mCurrentScreen, mPreScreen);
                }
                isScrollFinished = true;
            }
        }
    }

    public interface OnScreenChangedListener {
        void onScreenChanged(int curScreen, int preScreen);
    }

    public enum MOVE_TYPE {
        NOTMOVE, VMOVE, HMOVE
    }

    public interface OnVScrollListener {
        void onVScroll(View which, int deltaY);
    }

    private OnVScrollListener onVScrollListener;

    public OnVScrollListener getOnVScrollListener() {
        return onVScrollListener;
    }

    public void setOnVScrollListener(OnVScrollListener onVScrollListener) {
        this.onVScrollListener = onVScrollListener;
    }
}


package cn.com.nd.momo.activity.guide;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ViewFlipper;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.Log;

public class EnterGuideActivity extends Activity implements OnGestureListener {
    /** Called when the activity is first created. */

    GestureDetector mGestureDetector;

    // private static final int SCROLL_MIN_DISTANCE = 160;
    // private static final int SCROLL_MIN_VELOCITY = 10;
    private ViewFlipper vf;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enterguide);

        vf = (ViewFlipper)findViewById(R.id.vf);
        vf.addView(addImageById(R.drawable.openup), new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        vf.addView(addImageById(R.drawable.aboutme), new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT));
        mGestureDetector = new GestureDetector(EnterGuideActivity.this);
    }

    // 开发时对手势的测试
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        boolean retValue = mGestureDetector.onTouchEvent(event);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_UP) {
            // Helper method for lifted finger
        } else if (action == MotionEvent.ACTION_CANCEL) {
        }
        return retValue;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // TODO Auto-generated method stub

        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // TODO Auto-generated method stub
        Log.i("Fling", "Fling Happened!");
        if (e1.getX() - e2.getX() > 120) {
            this.vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_in));
            this.vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_left_out));

            this.vf.showNext();
            return true;
        } else if (e1.getX() - e2.getX() < -120) {
            this.vf.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_in));
            this.vf.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.push_right_out));
            this.vf.showPrevious();
            return true;
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        Log.i("MotionEvent", "onScroll.........");
        // 参数解释：
        // e1：第1个ACTION_DOWN MotionEvent
        // e2：最后一个ACTION_MOVE MotionEvent
        // velocityX：X轴上的移动速度，像素/秒
        // velocityY：Y轴上的移动速度，像素/秒

        // 触发条件 ：
        // X轴的坐标位移大于SCROLL_MIN_DISTANCE，且移动速度大于SCROLL_MIN_VELOCITY个像素/秒

        // if (e1.getX() - e2.getX() > SCROLL_MIN_DISTANCE
        // && Math.abs(distanceX) > SCROLL_MIN_VELOCITY) {
        // // Fling left
        // iv.setBackgroundResource(R.drawable.guardpic2);
        // } else if (e2.getX() - e1.getX() > SCROLL_MIN_DISTANCE
        // && Math.abs(distanceX) > SCROLL_MIN_VELOCITY) {
        // // Fling right
        // iv.setBackgroundResource(R.drawable.detailpageguideinfo);
        // }
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        EnterGuideActivity.this.finish();
        return false;
    }

    public View addImageById(int id) {
        ImageView iv = new ImageView(this);
        iv.setImageResource(id);
        return iv;
    }
}

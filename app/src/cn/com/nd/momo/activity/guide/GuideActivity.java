
package cn.com.nd.momo.activity.guide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.ImageView;
import cn.com.nd.momo.R;

public class GuideActivity extends Activity implements OnGestureListener {

    private ImageView iv;

    GestureDetector mGestureDetector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.Transparent);
        setContentView(R.layout.guide);

        iv = (ImageView)findViewById(R.id.guideiv);
        Intent intent = getIntent();
        String flag = (String)intent.getExtras().get("flag");
        if (flag.equals("contacts")) {
            iv.setBackgroundResource(R.drawable.contactperson);
        } else if (flag.equals("dynamic")) {
            iv.setBackgroundResource(R.drawable.sendoutdynamic);
        } else if (flag.equals("mention")) {
            iv.setBackgroundResource(R.drawable.aboutme2);
        } else if (flag.equals("im")) {
            iv.setBackgroundResource(R.drawable.chateachother);
        }
        mGestureDetector = new GestureDetector(this);

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
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        // TODO Auto-generated method stub
        GuideActivity.this.finish();
        return false;
    }
}

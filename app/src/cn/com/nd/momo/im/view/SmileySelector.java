
package cn.com.nd.momo.im.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import cn.com.nd.momo.im.activity.SmileTabActivity;

/**
 * 表情选择器
 * 
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class SmileySelector extends ImageButton {

    public final static int SMILEY_PICK_WITH_DATA = 916;

    public final static String INTENT_SMILEY_KEY = "smiley";

    private Context mContext;

    public SmileySelector(Context context) {
        super(context);
        init(context);
    }

    public SmileySelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SmileySelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        // createSmileyGrid();

        this.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Activity)mContext).startActivityForResult(new
                        Intent(mContext,
                                SmileTabActivity.class), SMILEY_PICK_WITH_DATA);
            }
        });
    }

    private View mFooterView;

    public void setFooterView(View view) {
        mFooterView = view;
    }

}

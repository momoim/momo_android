
package cn.com.nd.momo.im.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import cn.com.nd.momo.im.view.SmileyAdapter.OnSmileyListener;

/**
 * 表情选择GridView
 * 
 * @author Tsung Wu <tsung.bz@gmail.com>
 */
public class SmileyGrid extends GridView implements OnItemClickListener {

    public SmileyGrid(Context context) {
        super(context);
        init(context);
    }

    public SmileyGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SmileyGrid(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private Context mContext;

    private void init(Context context) {
        this.mContext = context;

        this.setNumColumns(5);
        // this.setBackgroundResource(R.drawable.conversation_smiley_grid_round);

        final SmileyAdapter adapter = new SmileyAdapter(mContext);
        this.setAdapter(adapter);

        this.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        if (getOnSmileyListener() != null) {
            // getOnSmileyListener().onSelect(
            // "[" +
            // ((SmileyAdapter)SmileyGrid.this.getAdapter()).getItemValue(arg2)
            // + "]");
            getOnSmileyListener().onSelect(
                    ((SmileyAdapter)SmileyGrid.this.getAdapter()).getItemValue(arg2));
        }
    }

    private OnSmileyListener mOnSmileyListener;

    public void setOnSmileyListener(OnSmileyListener mSmileyListener) {
        this.mOnSmileyListener = mSmileyListener;
    }

    public OnSmileyListener getOnSmileyListener() {
        return mOnSmileyListener;
    }
}

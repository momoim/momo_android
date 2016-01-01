/**
 * @author wuyq
 * 标签选择器
 */

package cn.com.nd.momo.view.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.Log;

public class LabelSelector extends Button {
    private static final String TAG = "LabelSelector";

    private Context mContext;

    public LabelSelector(Context context) {
        super(context);
        init(context);
    }

    public LabelSelector(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public LabelSelector(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;

        this.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(mContext).setItems(mLabels,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "onLabelClick" + String.valueOf(which));
                                int tmp = -1;
                                if (LabelSelector.this.getClickListener() != null) {
                                    tmp = LabelSelector.this.getClickListener().before();
                                }
                                final int pos = tmp;
                                LabelSelector.this.setDirty(true);
                                LabelSelector.this.setSelectedIndex(which);
                                final boolean last = LabelSelector.this.getSelectedIndex() == LabelSelector.this
                                        .getLabels().length - 1;

                                if (LabelSelector.this.isCustomizable() && last) {
                                    // TODO let user set custom string, some
                                    // bug, so put it in MultipleAdapter
                                    // final EditText label = new
                                    // EditText(mContext);
                                    // label.setKeyListener(TextKeyListener.getInstance(false,
                                    // Capitalize.WORDS));
                                    // label.requestFocus();
                                    // new AlertDialog.Builder(mContext)
                                    // .setView(label)
                                    // .setTitle(R.string.customLabelPickerTitle)
                                    // .setPositiveButton(android.R.string.ok,
                                    // new DialogInterface.OnClickListener() {
                                    // public void onClick(DialogInterface
                                    // dialog, int which) {
                                    // String newLabel =
                                    // label.getText().toString();
                                    // Log.i(TAG, "going to select custom" +
                                    // newLabel);
                                    // select(pos, last, newLabel);
                                    // }
                                    // })
                                    // .setNegativeButton(android.R.string.cancel,
                                    // null)
                                    // .show();
                                    select(pos, last);
                                } else {
                                    Log.i(TAG, String.valueOf(pos) + "going to select normal");
                                    select(pos, last);
                                }
                            }
                        }).setTitle(R.string.selectLabel).show();
            }
        });

        setLabel();
    }

    private void select(int pos, boolean last, String custom) {
        if (LabelSelector.this.getClickListener() != null) {
            if (pos != -1) {
                LabelSelector.this.getClickListener().after(pos);
            }
            LabelSelector.this.getClickListener().select(this, LabelSelector.this.getValue(), last,
                    custom);
        }
    }

    private void select(int pos, boolean last) {
        select(pos, last, "自定义的啦");
    }

    private void setLabel() {
        if (mLabels != null && mLabels.length > this.getSelectedIndex()) {
            if (this.getSelectedIndex() < 0) {
                this.setSelectedIndex(this.getDefaultIndex());
            }
            this.setText(mLabels[this.getSelectedIndex()]);
        }
    }

    public void setCustomizable(boolean customizable) {
        this.customizable = customizable;
    }

    public boolean isCustomizable() {
        return customizable;
    }

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
        setLabel();
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setLabels(String mLabels[]) {
        this.mLabels = mLabels;
        if (this.mLabels != null && this.mLabels.length > 0) {
            setLabel();
        }
    }

    public String[] getLabels() {
        return mLabels;
    }

    public int getValue() {
        if (this.getValueConverter() != null) {
            return this.getValueConverter().work(this.getSelectedIndex());
        } else {
            return this.getSelectedIndex();
        }
    }

    public void setValue(int value) {
        this.setDirty(true);
        if (this.getValueConverter() != null) {
            this.setSelectedIndex(this.getValueConverter().revert(value));
        } else {
            this.setSelectedIndex(value);
        }
    }

    public void setValueConverter(ValueConverter mValueConverter) {
        this.mValueConverter = mValueConverter;
    }

    public ValueConverter getValueConverter() {
        return mValueConverter;
    }

    public void setDirty(boolean mDirty) {
        this.mDirty = mDirty;
    }

    public boolean isDirty() {
        return mDirty;
    }

    public void setClickListener(ClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public ClickListener getClickListener() {
        return mClickListener;
    }

    public void setDefaultIndex(int defaultIndex) {
        this.defaultIndex = defaultIndex;
    }

    public int getDefaultIndex() {
        return defaultIndex;
    }

    private boolean customizable = false;

    private int selectedIndex = -1;

    private int defaultIndex = 0;

    private String mLabels[] = {
            "工作", "家庭", "朋友"
    };

    private ValueConverter mValueConverter;

    private boolean mDirty = false;

    private ClickListener mClickListener;

    public abstract static interface ValueConverter {
        int work(int original);

        int revert(int result);
    }

    public abstract static interface ClickListener {
        int before();

        void after(int pos);

        void select(LabelSelector ls, int value, boolean last, String custom);
    }
}


package cn.com.nd.momo.dynamic;

import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class AbsAdapter extends BaseAdapter {
    @SuppressWarnings("rawtypes")
    protected Vector mItems;

    protected final Context mContext;

    protected final LayoutInflater mInflater;

    // for post down bitmap notify
    protected Handler mHandler = new Handler();

    protected boolean mIsScrolling = false;

    public void setScrollingStatus(boolean isScrolling) {
        mIsScrolling = isScrolling;
    }

    @SuppressWarnings("rawtypes")
    public AbsAdapter(Context paramContext) {
        if (paramContext == null)
            throw new IllegalArgumentException("Context must not be null");

        this.mContext = paramContext;
        this.mInflater = LayoutInflater.from(paramContext);

        mItems = new Vector();
    }

    @SuppressWarnings("rawtypes")
    public Vector getContents() {
        return this.mItems;
    }

    @Override
    public int getCount() {
        int i = 0;
        if ((this.mItems != null) && (!this.mItems.isEmpty()))
            i = this.mItems.size();
        return i;
    }

    @Override
    public Object getItem(int paramInt) {
        Object localObject = null;
        if ((this.mItems != null) && (!this.mItems.isEmpty()))
            localObject = this.mItems.get(paramInt);
        return localObject;
    }

    @Override
    public long getItemId(int paramInt) {
        return paramInt;
    }

    @SuppressWarnings({
            "unchecked"
    })
    public void addItem(Object obj) {
        mItems.add(obj);
    }

    @SuppressWarnings({
            "unchecked"
    })
    public void addItems(Vector<?> obj) {
        mItems.addAll(obj);
    }

    @SuppressWarnings({
            "unchecked"
    })
    public void addItem(int index, Object obj) {
        mItems.add(index, obj);
    }

    @SuppressWarnings({
            "unchecked"
    })
    public void addItems(int index, Vector<?> obj) {
        mItems.addAll(index, obj);
    }

    @Override
    public abstract View getView(int paramInt, View paramView,
            ViewGroup paramViewGroup);

    public void removeAll() {
        if ((this.mItems != null) && (!this.mItems.isEmpty())) {
            this.mItems.removeAllElements();
            notifyDataSetChanged();
        }
    }
}


package cn.com.nd.momo.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.util.Log;

/**
 * 群组选择列表适配器
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public class GroupAdapter extends BaseAdapter {

    private List<GroupInfo> mArrayData = new ArrayList<GroupInfo>();

    private Context mContext;

    public GroupAdapter(Context c) {
        mContext = c;
    }

    public void SetDataArray(List<GroupInfo> arrayData) {
        if (arrayData == null) {
            Log.e("parameter arrayData is null");
            return;
        }

        mArrayData.clear();
        mArrayData = new ArrayList<GroupInfo>(Arrays.asList(new GroupInfo[arrayData.size()]));
        Collections.copy(mArrayData, arrayData);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mArrayData.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < mArrayData.size()) {
            return mArrayData.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return mArrayData.get(position).getGroupID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView txtName = null;

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.popup_menu_item, null);
        }

        View v = convertView;

        txtName = (TextView)v.findViewById(R.id.txt_groupName);

        txtName.setText(mArrayData.get(position).getGroupName());

        return v;
    }

}

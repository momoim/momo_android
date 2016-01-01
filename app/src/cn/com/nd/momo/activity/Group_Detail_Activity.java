
package cn.com.nd.momo.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.view.StatusesListView;

/**
 * 群组详情页面
 * 
 * @author 曾广贤 (muroqiu@qq.com)
 */
public class Group_Detail_Activity extends LinearLayout {

    private Context mContext;

    private GroupInfo mGroupInfo;

    private LayoutInflater mInflater;

    private StatusesListView mStatusesListView = null;

    public Group_Detail_Activity(Context context, GroupInfo groupInfo) {
        super(context);
        mContext = context;

        mGroupInfo = groupInfo;

        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.group_detail, this);

        initView();
    }

    /**
     * 初始化页面
     */
    private void initView() {
        mStatusesListView = (StatusesListView)findViewById(R.id.list_dynimic);
        LinearLayout mLayoutHeader = (LinearLayout)mInflater.inflate(R.layout.group_detail_header,
                null);
        ((TextView)mLayoutHeader.findViewById(R.id.txt_group_creator)).setText(mContext
                .getString(R.string.group_creator)
                + mGroupInfo.getCreatorName());
        ((TextView)mLayoutHeader.findViewById(R.id.txt_group_member_count)).setText(mContext
                .getString(R.string.group_member_count)
                + mGroupInfo.getMemberCount());
        ((TextView)mLayoutHeader.findViewById(R.id.txt_group_info)).setText(mGroupInfo.getNotice());
        mStatusesListView.addHeaderView(mLayoutHeader);

        mStatusesListView.setStatusesTypeInfo(StatusesListView.STATUSES_TYPE_GROUP,
                "" + mGroupInfo.getGroupID());
        mStatusesListView.initData();
    }
    
    public void refreshData() {
        mStatusesListView.onRefresh(false);
    } 
}

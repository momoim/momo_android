
package cn.com.nd.momo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.User;
import cn.com.nd.momo.model.DynamicItemInfo;
import cn.com.nd.momo.view.StatusesListView;

/**
 * 好友动态
 * 
 * @author 曾广贤 (muroqiu@qq.com)
 */
public class UserStatusesView extends LinearLayout {

    private LayoutInflater mInflater;

    public static DynamicItemInfo mDynamicItemInfo = new DynamicItemInfo();

    private User mUser = null;

    private StatusesListView mStatusesListView;

    public UserStatusesView(Context context, AttributeSet attrs, User user, boolean isRoot) {
        super(context, attrs);

        mUser = user;
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater.inflate(R.layout.user_statuses_view, this);

        mStatusesListView = (StatusesListView)findViewById(R.id.list_dynimic);
        mStatusesListView.setStatusesTypeInfo(StatusesListView.STATUSES_TYPE_USER,
                "" + mUser.getId());

        mStatusesListView.initData();
    }
    
    public void refreshData() {
        mStatusesListView.onRefresh(false);
    }
}

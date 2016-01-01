
package cn.com.nd.momo.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.parsers.json.GroupInfoParser;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.model.DynamicMgr;
import cn.com.nd.momo.view.widget.DragableSpace;
import cn.com.nd.momo.view.widget.DragableSpace.OnScreenChangedListener;

/**
 * 群组相关页面选择器
 * 
 * @author 曾广贤 (muroqiu@qq.com)
 */
public class Group_Fragment_Activity extends Activity implements OnClickListener {

    public static final String PARAM = "PARAM";

    private GroupInfo groupInfo = null;

    private LinearLayout layoutLeft = null;

    private LinearLayout layoutRight = null;

    private TextView txtItemName = null;

    private DragableSpace dragableSpace = null;

    private Group_Detail_Activity groupDetailView;

    /** Called when the activity is first created. */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.group_fragment);

        Intent intent = getIntent();
        if (intent != null) {
            String param = intent.getStringExtra(PARAM);
            try {
                groupInfo = new GroupInfoParser().parse(new JSONObject(param));
                Log.i(groupInfo.getGroupName());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        initView();
    }

    /**
     * 初始化页面
     */
    private void initView() {
        TextView txtGroupName = (TextView)findViewById(R.id.txt_group_name);
        txtGroupName.setText(groupInfo.getGroupName());

        dragableSpace = (DragableSpace)findViewById(R.id.contact_pager);

        Group_Member_List_Activity groupMemberView = new Group_Member_List_Activity(this, groupInfo);
        dragableSpace.addView(groupMemberView);
        groupDetailView = new Group_Detail_Activity(this, groupInfo);
        dragableSpace.addView(groupDetailView);
        dragableSpace.setOnScreenChangedListener(new OnScreenChangedListener() {

            @Override
            public void onScreenChanged(int curScreen, int preScreen) {
                initSubTitle(curScreen);
            }
        });

        layoutLeft = (LinearLayout)findViewById(R.id.layout_left);
        layoutLeft.setOnClickListener(this);
        layoutRight = (LinearLayout)findViewById(R.id.layout_right);
        layoutRight.setOnClickListener(this);
        txtItemName = (TextView)findViewById(R.id.txt_contact_info_center);

        initSubTitle(0);
    }

    private void initSubTitle(int pos) {
        if (pos == 0) {
            txtItemName.setText(R.string.group_member_list);
            layoutLeft.setVisibility(View.INVISIBLE);
            layoutRight.setVisibility(View.VISIBLE);
        } else {
            txtItemName.setText(R.string.group_detail);
            layoutLeft.setVisibility(View.VISIBLE);
            layoutRight.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_left:
                dragableSpace.snapToScreen(0);
                break;
            case R.id.layout_right:
                dragableSpace.snapToScreen(1);
                break;
        }
        ;

    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent paramIntent) {
        switch (resultCode) {
            case DynamicMgr.MSG_POST_BROADCAST:
            case DynamicMgr.MSG_POST_COMMENT:
            case DynamicMgr.MSG_POST_PRISE:
            case DynamicMgr.MSG_POST_REPLY:
                Log.i("onActivityResult");
                groupDetailView.refreshData();
                break;
            case DynamicMgr.MSG_POST_DELETE_DYNAMIC:
                // 个人主页删除动态后 更新此界面
//                mlistViewAdapter.deleteItem(mDynamicItemInfo.id);
//                mlistViewAdapter.notifyDataSetChanged();
                break;
            default:
                break;
        }
    }    
}

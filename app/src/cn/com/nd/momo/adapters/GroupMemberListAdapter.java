
package cn.com.nd.momo.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.types.GroupMember;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.view.CustomImageView;

public class GroupMemberListAdapter extends BaseAdapter {

    private Context mContext;

    private List<GroupMember> mArrayData = new ArrayList<GroupMember>();

    // private MoMoContactsManager mContactManager;
    // private long mGroupID = 0;
    private boolean mIsScrolling = false;

    public static final int TYPE_FOR_CONTACT = 1;

    public static final int TYPE_FOR_GROUP_MEMBER = 2;

    public static final String ALPHABETIC_DEFAULT = "#";

    private int mAppCount = 0;

    public int getAppCount() {
        return mAppCount;
    }

    // for indexer
    // private boolean mShowHeader = true;
    private ArrayList<Integer> mPositions = new ArrayList<Integer>();

    private final String[] mIndexer = new String[] {
            "#", "A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q",
            "R", "S", "T",
            "U", "V", "W",
            "X", "Y", "Z"
    };

    // check box status for selecting item
    private boolean mShowSelectionCheckBox = false;

    private ArrayList<Long> mSelectedItem = new ArrayList<Long>();

    public ConcurrentHashMap<Long, Bitmap> mMapCacheAvatar = new ConcurrentHashMap<Long, Bitmap>();

    /**
     * constructor for adapter set nAdapterType = TYPE_FOR_CONTACT for contact
     * list set nAdapterType = TYPE_FOR_GROUP_MEMBER for group member list
     * 
     * @param activity
     * @param nAdapterType
     */
    public GroupMemberListAdapter(Activity activity) {
        this.mContext = activity;
    }

    public GroupMemberListAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * set array data for list view, if the adapter is for group member, you
     * should call setDataArrayForGroupMember
     * 
     * @param arrayData
     */
    public void SetDataArray(List<GroupMember> arrayData) {
        setDataArray(arrayData, false);
    }

    public void SetSearchDataArray(List<GroupMember> arrayData) {
        setDataArray(arrayData, true);
    }

    private void setDataArray(List<GroupMember> arrayData, boolean isSearch) {
        mAppCount = 0;
        // mMapCacheAvatar.clear();
        if (null == arrayData)
            mArrayData = new ArrayList<GroupMember>();
        else {
            mArrayData.clear();
            mArrayData = new ArrayList<GroupMember>(
                    Arrays.asList(new GroupMember[arrayData.size()]));
            Collections.copy(mArrayData, arrayData);
        }
        // if (!isSearch) {
        // List<GroupMember> arrayApp = new ArrayList<GroupMember>();
        // ArrayList<Robot> robotList =
        // RobotManager.getInstance(Utils.getContext())
        // .getRobotList();
        //
        // addAppHeader(arrayApp);
        // mArrayData.addAll(0, arrayApp);
        // }

        mSelectedItem.clear();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView nameView;

        public CustomImageView image;

        public CheckBox checkSelect;

        public ImageView imgFriend;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public int getPositionByIndexerLetter(String indexer) {
        int nIndex = 0;
        if (mPositions.size() < 1)
            return -1;
        for (nIndex = 0; nIndex < mIndexer.length; nIndex++) {
            if (mIndexer[nIndex].equals(indexer)) {
                break;
            }
        }
        return mPositions.get(nIndex);
    }

    public void showSelectionCheckBox(boolean bShow) {
        mShowSelectionCheckBox = bShow;
        mSelectedItem.clear();
        notifyDataSetChanged();
    }

    public void setChecked(long lPhoneCid) {
        if ((!mShowSelectionCheckBox) || lPhoneCid <= 0) {
            return;
        }
        if (mSelectedItem.contains(lPhoneCid)) {
            mSelectedItem.remove(lPhoneCid);
        } else {
            mSelectedItem.add(lPhoneCid);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Long> getSelectedPhoneCid() {
        return mSelectedItem;
    }

    public void setSelectedPhoneCid(ArrayList<Long> arrItems) {
        mSelectedItem = arrItems;
        notifyDataSetChanged();
    }

    public void setScrolling(boolean bScroll) {
        mIsScrolling = bScroll;
    }

    @Override
    public int getCount() {
        if (mArrayData == null)
            return 0;
        else
            return mArrayData.size();
    }

    @Override
    public Object getItem(int position) {
        if (mArrayData == null || mArrayData.size() <= position) {
            return null;
        } else {
            return mArrayData.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        GroupMember groupMember = (GroupMember)getItem(position);
        if (null == groupMember)
            return GlobalUserInfo.MY_CONTACT_ID;
        else
            return groupMember.getId();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup arg1) {
        ViewHolder cache;
        if (convertView == null) {
            cache = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.contacts_list_item, null);
            // name
            cache.nameView = (TextView)convertView.findViewById(R.id.txt_contact_item_name);

            // image
            cache.image = (CustomImageView)convertView.findViewById(R.id.img_contact_item_presence);

            // check box for selection
            cache.checkSelect = (CheckBox)convertView.findViewById(R.id.chk_contact_item_select);

            cache.imgFriend = (ImageView)convertView.findViewById(R.id.img_contact_is_friend);

            convertView.setTag(cache);
        } else {
            cache = (ViewHolder)convertView.getTag();
            cache.image.cancelLoadAvatar();
        }

        // group info
        if (mArrayData.size() <= position)
            return convertView;
        GroupMember groupMember = mArrayData.get(position);
        if (null == groupMember)
            return convertView;

        // show name
        String name = groupMember.getName();
        cache.nameView.setText(name);

        cache.image.setCustomImage(groupMember.getId(), groupMember.getAvatar(), mMapCacheAvatar,
                mIsScrolling);

        cache.checkSelect.setVisibility(View.GONE);

        cache.imgFriend.setVisibility(View.GONE);

        return convertView;
    }
}

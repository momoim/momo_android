
package cn.com.nd.momo.adapters;

import java.util.ArrayList;
import java.util.Vector;

import android.R.color;
import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.WholeImageActivity;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.statuses.StatusesManager;
import cn.com.nd.momo.api.types.Attachment;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.view.DynamicListItemUI;
import cn.com.nd.momo.view.DynamicListItemUI.ViewHold;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.model.DynamicDB;
import cn.com.nd.momo.model.DynamicInfo;
import cn.com.nd.momo.model.DynamicItemInfo;

public class DynamicAdapter extends AbsAdapter {
    private final static String TAG = "DynamicAdapter";
    
    private boolean mIsRemoveRepeat;

    private Vector<DynamicItemInfo> mCurrentDisplayItems = new Vector<DynamicItemInfo>();

    private Context mContext = null;
        
    public DynamicAdapter(Context context, boolean IsRemoveRepeat) {
        super(context);
        mContext = context;
        mIsRemoveRepeat = IsRemoveRepeat;
    }
    
    public DynamicAdapter(Activity context) {
        super(context);
        mContext = context;
        mIsRemoveRepeat = false;
    }

    @SuppressWarnings("unchecked")
    public Vector<DynamicItemInfo> getAllItems() {
        return mItems;
    }

    public Vector<DynamicItemInfo> getItemsByGroupID(long id) {
        mCurrentDisplayItems.removeAllElements();
        for (DynamicItemInfo info : getAllItems()) {
            mCurrentDisplayItems.add(info);
        }
        return mCurrentDisplayItems;
    }

    /**
     * <br>
     * Description:获取群列表 <br>
     * Author:hexy <br>
     * Date:2011-4-26下午04:06:43
     * 
     * @param gname
     * @return
     */
    public Vector<DynamicItemInfo> getItemsByGname(String gname) {
        mCurrentDisplayItems.removeAllElements();
        for (DynamicItemInfo info : getAllItems()) {
            if (info.gname.equals(gname))
                mCurrentDisplayItems.add(info);
        }
        return mCurrentDisplayItems;
    }

    /**
     * <br>
     * Description:添加 item, 必须在ui主线程里面弄 <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:28:22
     * 
     * @param item
     */
    public void addItem(DynamicItemInfo item) {
        if (mIsRemoveRepeat) {
            removeRepeating(item);
        }
        super.addItem(item);
    }

    /**
     * <br>
     * Description:添加 item , 必须在ui主线程里面弄 <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:28:51
     * 
     * @param at
     * @param item
     */
    public void addItem(int at, DynamicItemInfo item) {
        if (mIsRemoveRepeat) {
            removeRepeating(item);
        }

        super.addItem(at, item);
    }

    /**
     * <br>
     * Description:根据 typeid 跟 objid 删除item <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:29:03
     * 
     * @param typeid
     * @param objid
     */
    public void deleteItem(String id) {
        DynamicItemInfo info;
        for (int i = 0; i < mItems.size(); i++) {
            info = (DynamicItemInfo)mItems.get(i);
            if (info.id.equalsIgnoreCase(id)) {
                mItems.remove(i);
                break;
            }
        }
    };

    public void deleteItems(Vector<DynamicItemInfo> items) {
        for (DynamicItemInfo item : items) {
            deleteItem(item.id);
        }
    };

    public void removeRepeating(DynamicItemInfo item) {
        if (mIsRemoveRepeat) {
            int index = getDynamicItemIndex(item.id);
            if (index != -1) {
                mItems.remove(index);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addItems(Vector<?> obj) {
        if (mIsRemoveRepeat) {
            for (DynamicItemInfo info : (Vector<DynamicItemInfo>)obj) {
                removeRepeating(info);
            }
        }

        super.addItems(obj);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addItems(int index, Vector<?> obj) {
        if (mIsRemoveRepeat) {
            for (DynamicItemInfo info : (Vector<DynamicItemInfo>)obj) {
                removeRepeating(info);
            }
        }

        super.addItems(0, obj);
    }

    /**
     * <br>
     * Description:get item index <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:31:41
     * 
     * @param typeid
     * @param objid
     * @return -1 for not found
     */
    public int getDynamicItemIndex(String id) {
        int index = -1;
        DynamicItemInfo info;
        for (int i = 0; i < mItems.size(); ++i) {
            info = (DynamicItemInfo)mItems.get(i);
            if (info.id.equalsIgnoreCase(id))
                index = i;
        }
        return index;
    }

    public DynamicItemInfo getDynamicItemInfo(String id) {
        int index = getDynamicItemIndex(id);

        return index == -1 ? null : (DynamicItemInfo)getItem(index);
    }

    /**
     * <br>
     * Description:is item has exits <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:33:20
     * 
     * @param typeid
     * @param objid
     * @return
     */
    public boolean isExist(String id) {
        return getDynamicItemInfo(id) != null;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    /**
     * <br>
     * Description:需要用到 first lasttime 来翻页 <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:35:11
     * 
     * @return lasttime
     */
    public long getFirstTime() {
        long time = 0;

        if (getCount() > 0) {
            DynamicItemInfo info = (DynamicItemInfo)(mItems.get(0));
            time = info.modifiedAt;
        }
        return time;
    }

    /**
     * <br>
     * Description:需要用到 last lasttime 来翻页 <br>
     * Author:hexy <br>
     * Date:2011-4-7上午11:35:11
     * 
     * @return lasttime
     */
    public long getLastTime() {
        long time = 0;

        if (getCount() > 0) {
            DynamicItemInfo info = (DynamicItemInfo)(mItems.get(mItems.size() - 1));
            time = info.modifiedAt;
        }
        return time;
    }

    @Override
    public Object getItem(int position) {
        // Log.i(TAG, "getItem" + position);
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return ((DynamicItemInfo)mItems.get(position)).uid;
    }


    private int mCurrentClickItem;

    @Override
    public View getView(final int position, View convertView, ViewGroup paramViewGroup) {
        Log.i(TAG, "getView step 1");
        do {
            DynamicListItemUI dynamicUI = new DynamicListItemUI();
            final DynamicItemInfo itemInfo = (DynamicItemInfo)getItem(position);

            // create hold
            final ViewHold hold;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.dynamic_list_item, null);
                hold = dynamicUI.createHold(convertView, mContext);
            }
            else {
                hold = (ViewHold)convertView.getTag();
            }

            // 通用初始化部分
            dynamicUI.init(hold, itemInfo, mContext, mIsScrolling, mHandler, this);

            boolean isShowComment = (itemInfo.commentCount > 0) || (itemInfo.likeCount > 0);
            convertView.findViewById(R.id.dynamic_list_item_comment_info).setVisibility(
                    isShowComment ? View.VISIBLE : View.GONE);

            // 底部快捷bar
            itemInfo.isQuickBtnsExpanded = itemInfo.isQuickBtnsExpanded
                    && mCurrentClickItem == position;

            // 背景
            int backgroundColor = mContext.getResources().getColor(color.transparent);
            if (itemInfo.sendStatus == DynamicItemInfo.SEND_PROCESS
                    || (itemInfo.isQuickBtnsExpanded && mCurrentClickItem == position)) {
                backgroundColor = mContext.getResources().getColor(R.color.blue_block);
            }
            if (itemInfo.sendStatus == DynamicItemInfo.SEND_FAIL) {
                backgroundColor = mContext.getResources().getColor(R.color.orangle_block);
            }

            hold.wrap.setBackgroundColor(backgroundColor);

            // 发送状态
            switch (itemInfo.sendStatus) {
                case DynamicItemInfo.SEND_DONE:
                    hold.commentCount.setText(mContext.getString(R.string.dynamic_comment)
                            + String.valueOf(itemInfo.commentCount));
                     hold.likeCount.setText(Html.fromHtml(itemInfo.likeList));
                    break;
                case DynamicItemInfo.SEND_FAIL:
                    hold.commentCount.setText(mContext.getString(R.string.dynamic_sendfail));
                     hold.likeCount.setText("");
                    break;
                case DynamicItemInfo.SEND_PROCESS:
                    hold.commentCount.setText("进度:" + itemInfo.commentCount);
                     hold.likeCount.setText("");
                    break;
                default:
                    break;
            }

            hold.avatar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    //todo
//                    Intent intent = new Intent();
//                    User user = new User();
//                    user.setId(String.valueOf(itemInfo.uid));
//                    user.setName(itemInfo.realname);
//                    intent.setAction(ContactFragmentActivity.ACTION_IM);
//                    UserParser up = new UserParser();
//                    String jsonString = "";
//                    try {
//                        JSONObject jsonObject = up.toJSONObject(user);
//                        jsonString = jsonObject.toString();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    intent.putExtra(ContactFragmentActivity.EXTRA_USER, jsonString);
//                    intent.setClass(mContext, ContactFragmentActivity.class);
//                    mContext.startActivity(intent);

                }
            });
            if (itemInfo.allowPraise == 1) {
                hold.praise.setImageResource(R.drawable.share_praise);
                final View viewCach = convertView;
                hold.praise.setEnabled(true);
                hold.praise.setOnClickListener(new OnClickListener() {
                    
                    @Override
                    public void onClick(View v) { 
                        new Thread(new Runnable() {
                            
                            @Override
                            public void run() {
                                try {
                                    StatusesManager.praise(itemInfo.id);                                    
                                    
                                    hold.praise.setEnabled(false);
                                    hold.praise.setImageResource(R.drawable.share_praise_disable);
                                    itemInfo.allowPraise = 0;
                                    
                                    itemInfo.likeList = "我"
                                            + (itemInfo.likeList.length() == 0 ? "觉得这挺赞的"
                                                    : "和") + itemInfo.likeList;
                                    itemInfo.likeCount ++;
                                    
                                    DynamicListItemUI.setTextFromHtml(hold.likeCount, itemInfo.likeList + " ");
                                    viewCach.findViewById(R.id.dynamic_list_item_comment_info).setVisibility(View.VISIBLE);
                                    
                                    // 有存储的需替换，无存储(群分享、个人分享)的不做处理
                                    DynamicInfo info = DynamicDB.instance().queryDynamic(itemInfo.id);

                                    boolean isCached = info != null && info.id != null && info.id.length() != 0;
                                    
                                    if (isCached) {
                                        DynamicDB.instance().insertDynamic(itemInfo, false);
                                    }
                                } catch (MoMoException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).run();                        
                    }
                });
            } else {
                hold.praise.setImageResource(R.drawable.share_praise_disable);
                hold.praise.setEnabled(false);
            }
            Log.i(TAG, "getView step 2");

            // 读取动态图片
            if (GlobalUserInfo.statuses_image_mode == GlobalUserInfo.STATUSES_IMAGE_MODE_NO) {
                // 无图预览模式
                hold.images.setVisibility(View.GONE);
                hold.imagesMore.setVisibility(View.GONE);
            }
            else {
                boolean isLoadImages = itemInfo.sendStatus == DynamicItemInfo.SEND_DONE
                        && itemInfo.images.size() > 0;

                hold.images.setVisibility(isLoadImages ? View.VISIBLE : View.GONE);
                if (GlobalUserInfo.statuses_image_mode == GlobalUserInfo.STATUSES_IMAGE_MODE_SMALL) {
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams((int)mContext
                            .getResources().getDimensionPixelSize(
                                    R.dimen.statuses_small_image_width), (int)mContext
                            .getResources().getDimensionPixelSize(
                                    R.dimen.statuses_small_image_height));
                    hold.images.getChildAt(0).setLayoutParams(lParams);                    
                    ((ImageView)hold.images.getChildAt(0))
                            .setScaleType(ImageView.ScaleType.CENTER_CROP);
                    
                    hold.images.getChildAt(1).setVisibility(
                            itemInfo.images.size() > 1 ? View.VISIBLE : View.GONE);
                    hold.images.getChildAt(2).setVisibility(
                            itemInfo.images.size() > 2 ? View.VISIBLE : View.GONE);
                    hold.images.getChildAt(3).setVisibility(
                            itemInfo.images.size() == 4 ? View.VISIBLE : View.GONE);
                } else {
                    
                    final Attachment attachment;
                    if (itemInfo.attachmentList.size() > 0) {
                        attachment = itemInfo.attachmentList.get(0);
                    } else {
                        attachment = null;
                    }

                    if (attachment != null && attachment.getWidth() > 0 && attachment.getHeight() > 0) {
                        ViewTreeObserver obsView = hold.images.getViewTreeObserver();
                        obsView.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                            
                            private boolean hasMeasured = false;

                            @Override
                            public boolean onPreDraw() {

                                if (!hasMeasured) {
                                    int width = hold.images.getMeasuredWidth();
                                    if (width > attachment.getWidth()) {
                                        width = attachment.getWidth();
                                    }
                                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                                            width, width * attachment.getHeight() / attachment.getWidth());
                                    hold.images.getChildAt(0).setLayoutParams(lParams);
                                    
                                    hasMeasured = true;
                                }
                                return true;
                            }
                        });
                    } else {
                        LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                                LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
                        hold.images.getChildAt(0).setLayoutParams(lParams);
                    }

                    ((ImageView)hold.images.getChildAt(0))
                        .setScaleType(ImageView.ScaleType.FIT_XY);
                    
                    hold.images.getChildAt(1).setVisibility(View.GONE);
                    hold.images.getChildAt(2).setVisibility(View.GONE);
                    hold.images.getChildAt(3).setVisibility(View.GONE);                    
                }

                ImageView v;
                if (mIsScrolling) {
                    for (int i = 0; i < hold.images.getChildCount() - 1; i++) {
                        v = (ImageView)hold.images.getChildAt(i);
                        v.setImageResource(R.drawable.dynamic_thumb_image);
                    }
                    break;
                }
    
                // 读取 图片预览模式配置
                
                if (isLoadImages) {
                    boolean isStartThread =
                            !mIsScrolling && itemInfo.imageBmps.size() == 0;
                    if (GlobalUserInfo.statuses_image_mode == GlobalUserInfo.STATUSES_IMAGE_MODE_SMALL) {
                        WholeImageActivity.Thumbnail.loadImages(mContext, hold.images, itemInfo.images,
                                itemInfo.imageBmps,
                                60, isStartThread);
                    } else if (GlobalUserInfo.statuses_image_mode == GlobalUserInfo.STATUSES_IMAGE_MODE_BIG) {
                        ArrayList<String> imagesBig = new ArrayList<String>();
                        for (String imageUrl : itemInfo.images) {
                            String url = imageUrl.replace("_80", "_780");
                            url = url.replace("_160", "_780");
                            url = url.replace("_130", "_780");
                            imagesBig.add(url);
                        }
                        
                        WholeImageActivity.Thumbnail.loadImages(mContext, hold.images, imagesBig,
                                itemInfo.imageBmps,
                                0, isStartThread);                        
                    }
                }
    
                boolean isHaveMoreImage = isLoadImages && itemInfo.images.size() > 4;
                if (GlobalUserInfo.statuses_image_mode == GlobalUserInfo.STATUSES_IMAGE_MODE_BIG) {
                    isHaveMoreImage = false;
                }
                hold.imagesMore.setVisibility(isHaveMoreImage ? View.VISIBLE : View.GONE);
                if (isHaveMoreImage) {
                    hold.imagesMore.setText("+" + String.valueOf((itemInfo.images.size() - 3)));
                    hold.imagesMore.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            WholeImageActivity.Thumbnail.ViewNetImages(itemInfo.getBigImages(), 3);
                        }
                    });
                }
            }
        } while (false);
        Log.i(TAG, "getView step 3");
        return convertView;
    }
}

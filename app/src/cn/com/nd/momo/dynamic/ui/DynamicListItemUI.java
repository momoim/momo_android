
package cn.com.nd.momo.dynamic.ui;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.Statuses_Activity;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.dynamic.DynamicItemInfo;
import cn.com.nd.momo.dynamic.DynamicMgr;
import cn.com.nd.momo.im.buss.TalkHistoryAdapter;
import cn.com.nd.momo.view.CustomImageView;

public class DynamicListItemUI {
    public static class ViewHold {
        public CustomImageView avatar;

        // public ImageView iconSina;
        public ImageView iconSource;

        public TextView gname;

        public TextView name;
        
        public ImageButton praise;

        public TextView time;

        public TextView content;

        public ViewGroup images;

        public TextView commentCount;

        public TextView likeCount;

        public TextView commentLast;

        public TextView imagesMore;

        public ViewGroup btnsBar;

        public ViewGroup wrap;

        public TextView viewOpt;
        
        public RelativeLayout panelGroup = null;
    };

    public static class ViewInfo {
        public long uid;

        public String avatar = "";

        public String gname = "";

        public String realname = "";

        public String date = "";

        public String content = "";

        public String commentCount = "";

        public String likeList = "";

        public String commentLast = "";

        public String client = "";

        public int isSyncSina;

        public ArrayList<String> images = new ArrayList<String>();

        public Bitmap avatarBmp = null;

        public boolean isdownloading = false;

        public ViewInfo(DynamicItemInfo info) {
            super();
            this.uid = info.uid;
            this.avatar = info.avatar;
            this.gname = info.gname;
            this.realname = info.realname;
            this.date = info.date;
            this.content = info.text;
            this.commentCount = info.commentCount == -1 ? "" : ("评论："
                    + String.valueOf(info.commentCount) + "");
            this.likeList = info.likeList;
            this.commentLast = info.commentLast;
            this.client = info.sourceName;
            this.images = info.images;
            this.avatarBmp = info.avatarBmp;
            this.isdownloading = info.isdownloading;
            isSyncSina = info.synced;
        }

        // public long lasttime;
        // public String objid;
        // public int typeid;
        // public long uid;
        // public long dateline = 0;
        // public String imagesJson = "";
        // public String likeList = "";
        // public int liked;
        // public int faved;
        // public String client="";
    }

    // public void getSpan(TextView tv, String txt,final Context context){
    // Spannable span = Spannable.Factory.getInstance().newSpannable("txt");
    // span.setSpan(new ClickableSpan() {
    // @Override
    // public void onClick(View v) {
    // Log.d("main", "link clicked");
    // // Toast.makeText(context, "link clicked", Toast.LENGTH_SHORT).show();
    // } }, 5, 9, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    //
    // tv.setText(span);
    // }
    /**
     * <A href="momouser://user/([^"]*?)/([^<]*?)\">([^<]*?)</A> http://[^\s]*
     * <A href="momouser://user/sdf/324">sadf</A>http://www.zhongguosou.com/
     * computer_question_tools/test_regex.aspx
     */
    public void linkiFy(TextView tv) {
        if (tv == null) {
            return;
        }

        Pattern pattern = Pattern.compile(ConfigHelper.PATTERN_USER + "|"
                + ConfigHelper.PATTERN_URL);
        Linkify.addLinks(tv, pattern, "");
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        // Linkify.addLinks(tv, Linkify.WEB_URLS);
    }

    public ViewHold createHold(View convertView, Context context) {
        ViewHold hold = new ViewHold();
        hold.avatar = (CustomImageView)convertView
                .findViewById(R.id.dynamic_list_item_avatar);
        hold.content = (TextView)convertView
                .findViewById(R.id.dynamic_list_item_content);
        hold.commentCount = (TextView)convertView.findViewById(R.id.dynamic_list_item_commentcount);
         hold.likeCount =
         (TextView)convertView.findViewById(R.id.dynamic_list_item_likecount);
        hold.name = (TextView)convertView
                .findViewById(R.id.dynamic_list_item_name);
        hold.praise = (ImageButton)convertView.findViewById(R.id.btn_praise);
        hold.time = (TextView)convertView
                .findViewById(R.id.dynamic_list_item_time);
        hold.gname = (TextView)convertView
                .findViewById(R.id.dynamic_list_item_group_info);
        hold.panelGroup = (RelativeLayout)convertView
                .findViewById(R.id.dynamic_list_group_panel);
        hold.commentLast = (TextView)convertView
                .findViewById(R.id.dynamic_list_item_comment);
        hold.imagesMore = (TextView)convertView
                .findViewById(R.id.dynamic_image_more);

        // hold.iconSina = (ImageView)convertView.findViewById(R.id.icon_sina);
        // hold.iconSource =
        // (ImageView)convertView.findViewById(R.id.icon_source);

        hold.images = (ViewGroup)convertView.findViewById(R.id.dynamic_list_item_images);
        // hold.btnsBar =
        // (ViewGroup)convertView.findViewById(R.id.quick_btn_bar);
        hold.wrap = (ViewGroup)convertView.findViewById(R.id.dynamic_list_item_wrap);
        convertView.setTag(hold);
        return hold;
    }

    /**
     * <br>
     * Description: <br>
     * Author:hexy <br>
     * Date:2011-5-12下午07:39:35
     * 
     * @param id
     * @return
     */
    //
    // private FriendInfo getFriendInfo(long uid) {
    // FriendInfo friendInfo;
    // do {
    // // get avatar from head pool
    // friendInfo = DynamicMgr.getInstance().getItem(uid);
    //
    // if (friendInfo == null) {
    // FriendInfo friendInfoAdd = new FriendInfo();
    // friendInfoAdd.id = uid;
    // DynamicMgr.getInstance().addItem(friendInfoAdd);
    // friendInfo = friendInfoAdd;
    // Log.e(TAG, "getView friendInfo" + uid);
    // }
    // } while (false);
    // return friendInfo;
    // }

    private void setText(TextView tv, String value) {
        if (value == null)
            return;
        tv.setVisibility(value.length() > 0 ? View.VISIBLE : View.GONE);
        tv.setText(value);
    }

    public static void setTextFromHtml(TextView tv, String value) {
        if (value == null)
            return;
        tv.setVisibility(value.length() > 0 ? View.VISIBLE : View.GONE);
        tv.setText(Html.fromHtml(value));
    }

    public static void setImageBitmapSafe(ImageView imageview, Bitmap bmp) {
        if (bmp != null && bmp.isRecycled()) {
            bmp = null;
        }

        imageview.setImageBitmap(bmp);
    }

    public void init(final ViewHold hold, final DynamicItemInfo itemInfo, final Context mContext,
            boolean isScorlling, final Handler handler, final BaseAdapter adapter) {
        do {
            // 姓名
            setText(hold.name, itemInfo.realname);
            // 事件
            setText(hold.time, itemInfo.date);
            // 群组
            setText(hold.gname, itemInfo.gname);

            if (itemInfo.gname != null && !itemInfo.gname.equals("")) {
                hold.panelGroup.setVisibility(View.VISIBLE);
            } else {
                hold.panelGroup.setVisibility(View.GONE);
            }
                
            // 最后一条评论
            if (itemInfo.commentLast != null && itemInfo.commentLast.length() > 0) {
                CharSequence commentLast = TalkHistoryAdapter.addSmileySpans(mContext,
                        Html.fromHtml(itemInfo.commentLast));
                hold.commentLast.setText(commentLast);
                hold.commentLast.setVisibility(View.VISIBLE);
            } else {
                hold.commentLast.setVisibility(View.GONE);               
            }

            // 内容
            // setTextFromHtml(hold.content, content.toString());
            CharSequence content = TalkHistoryAdapter.addSmileySpans(mContext,
                    Html.fromHtml(itemInfo.text + " "));
            hold.content.setText(content);

            String strCommentCount = itemInfo.commentCount == -1 ? "" : ("评论："
                    + String.valueOf(itemInfo.commentCount) + "");

            // 评论
            setText(hold.commentCount, strCommentCount);
            setTextFromHtml(hold.likeCount, itemInfo.likeList + " ");

            // set icon, platform from flag
            Log.i("platform from flag" + itemInfo.sourceName);
            // hold.iconSource.setVisibility(itemInfo.sourceName.length() > 0 ?
            // View.VISIBLE
            // : View.GONE);

            // 新浪微博
            // int sina =
            // itemInfo.isSyncSina==1?R.drawable.dynamic_sina_small:drawable.dynamic_sina_small_disable;
            // hold.iconSina.setImageResource(sina);
            // hold.iconSina.setVisibility(itemInfo.isSyncSina == -1
            // ?View.GONE:View.VISIBLE);

            // 动态有无包含图片标志
//            int imageFlagId = 0;
//            if (itemInfo.images.size() > 0) {
//                imageFlagId = R.drawable.dynamic_list_item_image;
//            }
//            if (itemInfo.images.size() > 1) {
//                imageFlagId = R.drawable.dynamic_list_item_images;
//            }
//            hold.name.setCompoundDrawablesWithIntrinsicBounds(0, 0, imageFlagId, 0);

            // if scrolling
            if (isScorlling) {
                hold.avatar.setImageResource(R.drawable.ic_contact_picture_frame);
                break;
            }

            // 设置头像
            hold.avatar.setCustomImage(itemInfo.uid, itemInfo.avatar,
                    Statuses_Activity.mapCache);
            
//            if (itemInfo.avatarBmp != null) {
//                setImageBitmapSafe(hold.avatar, itemInfo.avatarBmp);
//
//                break;
//            } else {
//                hold.avatar.setImageResource(R.drawable.ic_contact_picture_frame);
//            }

            // if downloading avatar break!
//            if (itemInfo.isdownloading)
//                break;

//            if (isScorlling)
//                break;
//
//            Thread thread = new Thread() {
//                public void run() {
//                    itemInfo.isdownloading = true;
//                    try {
//                        // final String sid = GlobalUserInfo.getSessionID();
//                        String imageurl;
//                        imageurl = itemInfo.avatar;// DynamicMgr.getAvatar(itemInfo.uid);//
//                                                   // +"&sid="+sid;
//
//                        Log.i("loading avatar" + imageurl);
//                        // 实现头像缓存
//                        itemInfo.avatarBmp = DynamicMgr.getInstance().getAvaterBitmapWithFrame(
//                                itemInfo.uid, itemInfo.avatar);
//                        if (itemInfo.avatarBmp == null) {
//                            BitmapDrawable db = (BitmapDrawable)mActivity.getResources()
//                                    .getDrawable(R.drawable.ic_contact_picture_frame);
//                            itemInfo.avatarBmp = db.getBitmap();
//                        }
//                        handler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                if (adapter != null) {
//                                    adapter.notifyDataSetChanged();
//                                } else {
//                                    setImageBitmapSafe(hold.avatar, itemInfo.avatarBmp);
//                                }
//
//                            }
//                        });
//                    } catch (Exception e) {
//                    }
//                    finally {
//                        itemInfo.isdownloading = false;
//                    }
//                }
//            };
//            thread.start();
        } while (false);
    }
}

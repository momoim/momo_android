
package cn.com.nd.momo.activity;

import java.util.ArrayList;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.adapters.AbsAdapter;
import cn.com.nd.momo.util.StartForResults;
import cn.com.nd.momo.util.StartForResults.PickData;

/**
 * 图片查看页面
 * 
 * @author 曾广贤 (muroqiu@sina.com)
 */
public class Statuses_Images_Activity extends Activity implements OnClickListener {
    private final static String TAG = "DynamicImageManagerActivity";

    public final static String EXTRAS_IMAGES = "extras_images";

    public final static String EXTRAS_UNSELECT_IMAGES = "extras_unselected_images";

    private ListView mListView;

    private FriendGridAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.statuses_images_select);

        // if single select
        Intent i = this.getIntent();
        ArrayList<String> arraylist = i.getExtras().getStringArrayList(EXTRAS_IMAGES);
        Vector<ImageInfo> vector = new Vector<ImageInfo>();

        for (String url : arraylist) {
            ImageInfo imageInfo = new ImageInfo(url);
            vector.add(imageInfo);
        }

        mListView = (ListView)findViewById(R.id.gridview);
        findViewById(R.id.text_name).setVisibility(View.GONE);

        ((Button)findViewById(R.id.btnAdd)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                StartForResults.getPicFromAlbum(Statuses_Images_Activity.this);
            }
        });
        ((Button)findViewById(R.id.btnOk)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirm();
            }
        }); 
        // mListView.setOnItemClickListener(mOnItemClickListener);

        mAdapter = new FriendGridAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(mOnScrollListener);

        mAdapter.addItems(vector);
        mAdapter.notifyDataSetChanged();
    }
//
//    private void initPhotoControl() {
//        ViewGroup bottomBar = (ViewGroup)findViewById(R.id.bottom_bar);
//        bottomBar.removeAllViews();
//        Button btnAddPhoto = new Button(this);
//        btnAddPhoto.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//        btnAddPhoto.setText("添加照片");
//        bottomBar.addView(btnAddPhoto);
//        btnAddPhoto.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                StartForResults.getPicFromAlbum(Statuses_Images_Activity.this);
//            }
//        });
//        
//        Button btnOK = new Button(this);
//        btnOK.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
//                ViewGroup.LayoutParams.WRAP_CONTENT));
//        btnOK.setText("完成");
//        bottomBar.addView(btnOK);
//        btnOK.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onConfirm();
//            }
//        });        
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            PickData picData = StartForResults.onRresult(this, requestCode, resultCode, data);

            if (picData != null) {

                for (int i = 0; i < mAdapter.getCount(); i++) {
                    ImageInfo info = (ImageInfo)mAdapter.getItem(i);
                    if (info.url.equalsIgnoreCase(picData.localPath)) {
                        Toast.makeText(this, "已选过此图片", 0).show();
                        return;
                    }
                }

                ImageInfo info = new ImageInfo(picData.localPath);
                mAdapter.addItem(info);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    // 退出程序监听
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown" + event.getRepeatCount());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onConfirm();
        }
        return super.onKeyDown(keyCode, event);
    }

    public void onConfirm() {
        ArrayList<String> result = getSelectedUrl();

        Intent i = new Intent();
        i.putExtra(EXTRAS_UNSELECT_IMAGES, result);
        setResult(Activity.RESULT_OK, i);

        finish();
    }

    // private OnItemClickListener mOnItemClickListener = new
    // OnItemClickListener() {
    // @Override
    // public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long
    // arg3) {
    // ImageInfo dinfo = (ImageInfo) mAdapter.getItem(arg2);
    // dinfo.selected = !dinfo.selected;
    // mAdapter.notifyDataSetChanged();
    // }
    // };

    public ArrayList<String> getSelectedUrl() {
        ArrayList<String> unSelectedUrls = new ArrayList<String>();
        for (int i = 0; i < mAdapter.getCount(); i++) {
            ImageInfo info = (ImageInfo)(mAdapter.getItem(i));
            if (info.selected) {
                unSelectedUrls.add(info.url);
            }
        }
        return unSelectedUrls;
    }

    public class ImageInfo {
        public ImageInfo(String url) {
            this.url = url;
        }

        String url;

        Bitmap thumb;

        boolean selected = true;

        boolean downloading = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_confirm:
                onConfirm();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * items
     */
    private boolean mIsScrolling = false;

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView paramAbsListView,
                int paramInt) {
            mIsScrolling = (paramInt == SCROLL_STATE_FLING);
            if (!mIsScrolling) {
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onScroll(AbsListView paramAbsListView, int paramInt1,
                int paramInt2, int paramInt3) {

        }
    };

    private Handler mhandler = new Handler();

    public class FriendGridAdapter extends AbsAdapter {
        public FriendGridAdapter(Activity paramContext) {
            super(paramContext);
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void deleteItem(int position) {
            mItems.remove(position);
        }

        class ViewHold {
            ImageView avatar;

            TextView name;

            Button button;
        };

        @Override
        public View getView(final int position, View convertView,
                ViewGroup parent) {
            Log.i(TAG, "getView" + position);

            ViewHold hold;
            // find view
            if (convertView == null) {
                convertView = View.inflate(Statuses_Images_Activity.this,
                        R.layout.dynamic_image_list_item, null);

                hold = new ViewHold();
                hold.avatar = (ImageView)convertView
                        .findViewById(R.id.img_contact_item_presence);
                hold.name = (TextView)convertView
                        .findViewById(R.id.txt_contact_item_name);
                hold.button = (Button)convertView
                        .findViewById(R.id.chk_contact_item_select);
                hold.button.setClickable(false);
                // LayoutParams layoutParam= new
                // LayoutParams(LayoutParams.WRAP_CONTENT,
                // LayoutParams.WRAP_CONTENT);
                // layoutParam.setMargins(0, 0, 20, 0);
                // hold.checkbox.setLayoutParams(layoutParam);
                convertView.setTag(hold);
            } else {
                hold = (ViewHold)convertView.getTag();
            }
            // hold.checkbox.setVisibility(mIsMulitSelected?View.VISIBLE:View.INVISIBLE);

            final ImageInfo itemInfo = (ImageInfo)getItem(position);

            if (itemInfo == null)
                return convertView;
            hold.name.setText("");
            hold.avatar.setImageBitmap(itemInfo.thumb);
            // boolean selected = itemInfo.selected;
            // hold.checkbox.setChecked(selected);
            hold.button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    itemInfo.selected = false;
                    mAdapter.deleteItem(position);
                    mAdapter.notifyDataSetChanged();
                }
            });

            hold.avatar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    WholeImageActivity.Thumbnail.mContext = Statuses_Images_Activity.this;
                    WholeImageActivity.Thumbnail.ViewLocalImages(getSelectedUrl(), position);
                }
            });

            boolean avatarDowloaded = itemInfo.thumb != null;
            if (avatarDowloaded)
                return convertView;

            // load avatar
            if (!itemInfo.downloading && !mIsScrolling) {
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        itemInfo.downloading = true;
                        try {
                            String imageurl;
                            imageurl = itemInfo.url;
                            Log.i(TAG, "load avatar" + imageurl);
                            // 实现头像缓存
                            if (itemInfo.thumb == null) {
                                itemInfo.thumb = BitmapToolkit.loadLocalBitmapExactScaled(
                                        itemInfo.url, 60);
                                mhandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }

                            itemInfo.downloading = false;
                        } catch (Exception e) {
                        }
                    }
                };
                thread.start();
            }
            return convertView;
        }
    }
    //
    // @Override
    // public boolean onPrepareOptionsMenu(Menu menu) {
    // menu.clear();
    // super.onPrepareOptionsMenu(menu);
    // if (!mSingleSelect) {
    // menu.add(0, 1, 1,
    // getResources().getString(R.string.txt_mulit_select)).setIcon(R.drawable.ic_menu_draft);
    // }
    //
    // return true;
    // }
    //
    // private boolean mIsMulitSelected = false;
    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    // Intent i;
    // switch (item.getItemId()) {
    // case 1:
    // mIsMulitSelected = !mIsMulitSelected;
    // mAdapter.notifyDataSetChanged();
    // break;
    // case 2:
    // i = new Intent(this, OptionActivity.class);
    // startActivity(i);
    // break;
    // }
    //
    // return true;
    // }
}

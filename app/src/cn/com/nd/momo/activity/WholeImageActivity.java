
package cn.com.nd.momo.activity;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import cn.com.nd.momo.R;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.BitmapToolkit.BitmapMemoryMgr;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.dynamic.ui.DynamicListItemUI;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.util.StartForResults;

public class WholeImageActivity extends Activity implements
        AdapterView.OnItemSelectedListener, ViewSwitcher.ViewFactory {
    private static final String TAG = "WholeImageActivity";

    public static String ACION_VIEW_LOACL = "action_view_local";

    public static String ACION_VIEW_NET = "action_view_net";

    public static String EXTRAL_IMAGES = "images";

    public static String EXTRAL_INDEX = "index";

    private ArrayList<ImageInfo> mImages = new ArrayList<ImageInfo>();

    private ImageSwitcher mSwitcher;

    private TextView mTextIndex;

    private GestureDetector mGestureDetector = new GestureDetector(
            new MyGestureDetector());

    public String mAciton;

    private int mPosition;

    public BitmapMemoryMgr mBitmapMemoryMgr = new BitmapMemoryMgr();

    /**
     * <br>
     * Title:显示缩略图 <br>
     * Description:TODO 类功能描述 <br>
     * Author:hexy <br>
     * Date:2011-5-13下午03:40:28
     */
    public static class Thumbnail {
        public static Context mContext;

        private static Handler mHandler = new Handler();

        /**
         * <br>
         * Description:跳转到看大图界面 <br>
         * Author:hexy <br>
         * Date:2011-5-13下午04:25:47
         * 
         * @param images
         * @param index
         */
        public static void ViewNetImages(ArrayList<String> images,
                int index) {
            ArrayList<String> imagesBig = new ArrayList<String>();
            for (String imageUrl : images) {
                String url = imageUrl.replace("_80", "_780");
                url = url.replace("_160", "_780");
                url = url.replace("_130", "_780");
                imagesBig.add(url);
            }

            Intent i = new Intent(mContext, WholeImageActivity.class);
            i.setAction(WholeImageActivity.ACION_VIEW_NET);
            i.putExtra(WholeImageActivity.EXTRAL_IMAGES, imagesBig);
            i.putExtra(WholeImageActivity.EXTRAL_INDEX, index);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }

        public static void ViewLocalImages(ArrayList<String> images,
                int index) {
            ArrayList<String> imagesBig = new ArrayList<String>();
            for (String imageUrl : images) {
                String url = imageUrl.replace("_80", "_780");
                url = url.replace("_160", "_780");
                url = url.replace("_130", "_780");
                imagesBig.add(url);
            }

            Intent i = new Intent(mContext, WholeImageActivity.class);
            i.setAction(WholeImageActivity.ACION_VIEW_LOACL);
            i.putExtra(WholeImageActivity.EXTRAL_IMAGES, imagesBig);
            i.putExtra(WholeImageActivity.EXTRAL_INDEX, index);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);
        }

        /**
         * <br>
         * Description:读取缩略图 <br>
         * Author:hexy <br>
         * Date:2011-5-13下午04:26:05
         * 
         * @param context
         * @param imageGroupView
         * @param imageUrls
         * @param imageBmps 优化用, 如果程序里缓存了 imageBmps, 可以直接显示,无需发请求
         * @param thumbSize
         * @return
         */
        public static boolean loadImages(Context context,
                final ViewGroup imageGroupView,
                final ArrayList<String> imageUrls,
                final ArrayList<Bitmap> imageBmps, final int thumbSize) {
            return loadImages(context, imageGroupView, imageUrls, imageBmps,
                    thumbSize, true);
        }

        public static boolean loadImages(Context context,
                final ViewGroup imageGroupView, final ArrayList<String> imageUrls,
                final ArrayList<Bitmap> imageBmps, final int thumbSize, boolean isStartThread) {
            // adapter 缓存优化用
            if (imageBmps == null || imageUrls == null || imageUrls.size() == 0)
                return false;

            mContext = context;

            // 获取图片的个数, 图片的实际个数取决于, imageview的个数
            int imageCount = imageUrls.size();
            int layoutCount = imageGroupView.getChildCount();

            // 使得所有容器都可看见, 空容器看不见
            for (int i = 0; i < layoutCount; i++) {
                // boolean visible = i < imageCount;
                // imageGroupView.getChildAt(i).setVisibility(visible?View.VISIBLE:View.GONE);

                // if (!visible)
                // continue;

                // 为可见按钮设置事件监听
                final View view = imageGroupView.getChildAt(i);
                view.setId(i);

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewNetImages(imageUrls, view.getId());
                    }
                });
            }

            // 图片个数一定得小于或等于容器个数
            if (imageCount > layoutCount) {
                imageCount = layoutCount;
            }

            // memory cache
            if (imageBmps.size() == 0) {
                for (int i = 0; i < imageCount; i++) {
                    ImageView imageView = (ImageView)imageGroupView.getChildAt(i);
                    imageView.setImageResource(R.drawable.dynamic_thumb_image);
                }
            } else {
                for (int i = 0; (i < imageBmps.size()) && (i < imageCount); i++) {
                    ImageView imageView = (ImageView)imageGroupView.getChildAt(i);
                    DynamicListItemUI.setImageBitmapSafe(imageView, imageBmps.get(i));
                }
                return false;
            }

            if (!isStartThread)
                return false;

            final int fImageCount = imageCount;
            new Thread() {
                @Override
                public void run() {
                    final ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>();
                    for (int i = 0; i < fImageCount; i++) {
                        String str = imageUrls.get(i);
                        Bitmap bitmapOrigin = null;
                        if (str.indexOf("?momolink=0") >= 0) {
                            BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_PHOTO, str,
                                    "", "");
                            bt.setSid(GlobalUserInfo.getSessionID());
                            if (thumbSize < 1) {
                                bitmapOrigin = bt.loadBitmapNetOrLocal();
                            } else {
                                bitmapOrigin = bt.loadBitmapNetOrLocalScale(ConfigHelper.SZIE_AVATAR);
                            }
                        }
                        else {
                            BitmapDrawable bitmapDrable = (BitmapDrawable)(mContext.getResources()
                                    .getDrawable(R.drawable.dynamic_thumb_image_out));
                            bitmapOrigin = bitmapDrable.getBitmap();
                        }

                        if (bitmapOrigin == null) {
                            BitmapDrawable bd = (BitmapDrawable)(mContext.getResources()
                                    .getDrawable(R.drawable.dynamic_thumb_image));
                            bitmapOrigin = bd.getBitmap();
                        }

                        if (bitmapOrigin.isRecycled()) {
                            continue;
                        }

                        final Bitmap bmp;
                        if (thumbSize < 1) {
                            bmp = bitmapOrigin;
                        } else {
                            bmp = BitmapToolkit.compress(bitmapOrigin, thumbSize);
                        }
                        // if (bitmapOrigin!=null&&!bitmapOrigin.isRecycled()) {
                        // bitmapOrigin.recycle();
                        // bitmapOrigin = null;
                        // }

                        final int index = i;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ImageView imageView = (ImageView)imageGroupView.getChildAt(index);
                                if (!bmp.isRecycled())
                                    DynamicListItemUI.setImageBitmapSafe(imageView, bmp);
                            }
                        });

                        bitmaps.add(bmp);
                    }
                    imageBmps.clear();
                    imageBmps.addAll(bitmaps);
                }
            }.start();

            return true;
        }

    }

    public class ImageInfo {
        public String url;

        public String localpath = "";

        public Bitmap bitmap;

        public ImageInfo(String iUrl) {
            url = iUrl;
        }
    }

    public class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            Log.i(TAG, "onFling");
            WholeImageActivity.this.onItemSelected(null, null, mPosition
                    + (velocityX > 0 ? -1 : 1), 0);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (mImages.get(mPosition) == null)
                return false;
            if (mImages.get(mPosition).localpath.length() > 0)
                StartForResults.viewPicFromAlbum(WholeImageActivity.this,
                        mImages.get(mPosition).localpath);
            return super.onSingleTapConfirmed(e);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        mBitmapMemoryMgr.releaseAllMemory();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dynamic_whole_image);
        mAciton = getIntent().getAction();

        mSwitcher = (ImageSwitcher)findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
        mTextIndex = (TextView)findViewById(R.id.txt_index);
        initData();
        if (mImages != null && mImages.size() > 0) {
            for (ImageInfo image : mImages)
                loadImage(image);
        }
    }

    private void initData() {
        if (getIntent().getExtras() != null
                && getIntent().getExtras().containsKey(EXTRAL_IMAGES)) {
            ArrayList<String> urls = getIntent().getExtras()
                    .getStringArrayList(EXTRAL_IMAGES);
            for (String url : urls) {
                mImages.add(new ImageInfo(url));
            }

            mPosition = getIntent().getExtras().getInt(EXTRAL_INDEX);
        }
    }

    private Handler mHandler = new Handler();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        do {
            result = mGestureDetector.onTouchEvent(event);
        } while (false);

        return result;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View v, int position,
            long id) {
        do {
            if (position < 0 || position >= mImages.size())
                break;

            mPosition = position;
            mTextIndex.setText((mPosition + 1) + "/" + mImages.size()
                    + "   单击缩放");

            mSwitcher.setImageDrawable(new BitmapDrawable(
                    mImages.get(position).bitmap));
        } while (false);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public View makeView() {
        ImageView i = new ImageView(this);
        i.setBackgroundColor(0xFF000000);
        i.setScaleType(ImageView.ScaleType.FIT_CENTER);
        i.setLayoutParams(new ImageSwitcher.LayoutParams(
                android.view.ViewGroup.LayoutParams.FILL_PARENT,
                android.view.ViewGroup.LayoutParams.FILL_PARENT));
        return i;
    }

    private void loadImage(final ImageInfo inInfo) {
        // 没有图片id的图片不缓存 待处理
        findViewById(R.id.txt_loadimg).setVisibility(View.VISIBLE);

        new Thread() {
            @Override
            public void run() {
                ImageInfo imageUrls = inInfo;

                do {
                    if (inInfo.bitmap != null) {
                        imageUrls.bitmap = inInfo.bitmap;
                        break;
                    }

                    if (mAciton.equalsIgnoreCase(ACION_VIEW_NET)) {
                        String url = inInfo.url;
                        BitmapToolkit bt = new BitmapToolkit(
                                BitmapToolkit.DIR_MOMO_PHOTO, url, "",
                                BitmapToolkit.getSuffix(url) + ".cache");
                        bt.setSid(GlobalUserInfo.getSessionID());
                        imageUrls.bitmap = bt.loadBitmapNetOrLocal();

                        imageUrls.localpath = bt.getAbsolutePath();
                        fileScan(getApplicationContext(), imageUrls.localpath);
                        break;
                    }

                    imageUrls.bitmap = cn.com.nd.momo.util.Utils.getLocalBitmap(imageUrls.url, 780);
                    imageUrls.localpath = imageUrls.url;
                } while (false);
                mBitmapMemoryMgr.addBitmap(imageUrls.bitmap);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.txt_loadimg).setVisibility(View.GONE);

                        int index = mImages.indexOf(inInfo);
                        if (index == mPosition) {
                            WholeImageActivity.this.onItemSelected(null, null,
                                    index, 0);
                        }

                        return;
                    }
                });

                super.run();
            }
        }.start();
    }

    // private String getImageID(String url){
    // int start = url.lastIndexOf("/");
    // int end = url.lastIndexOf("_");
    // if (start>=url.length()||end>=url.length()||start>end||start<0||end<0){
    // Log.e(TAG, start+":"+end+" getImageID "+url);
    // return null;
    // }
    // Log.i(TAG, start+":"+end+" getImageID "+url);
    // return (url.subSequence(start+1,end)).toString();
    //
    // }
    static public void fileScan(Context context, String fName) {
        Log.i(TAG, "fileScan : " + fName);
        Uri data = Uri.parse("file://" + fName);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, data));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBitmapMemoryMgr.releaseAllMemory();
    }

    public static Bitmap getLocalBitmap(String path, int size) {
        BitmapFactory.Options options = new BitmapFactory.Options();

        options.outHeight = size;

        options.inJustDecodeBounds = true;

        // 获取这个图片的宽和高
        Bitmap bm = BitmapFactory.decodeFile(path, options); // 此时返回bm为空

        options.inJustDecodeBounds = false;
        int be = options.outHeight / (size / 10);
        if (be % 10 != 0)
            be += 10;

        be = be / 10;
        if (be <= 0)
            be = 1;

        options.inSampleSize = be;
        bm = BitmapFactory.decodeFile(path, options);

        return bm;
    }
}

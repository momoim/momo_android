
package cn.com.nd.momo.util;

import java.io.File;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.Log;

public final class StartForResults {
    /**
     * 调用系统相册 或是 摄像头 获取图片数据 用法： // 摄像头
     * StartForResults.getPicFromCamera(DynamicCommentActivity.this); // 相册
     * StartForResults.getPicFromAlbum(DynamicCommentActivity.this);
     * 
     * @Override protected void onActivityResult(int requestCode, int
     *           resultCode, Intent paramIntent) { //获取图片数据 switch (requestCode)
     *           { // 图片上传 case StartForResults.CAMERA_PICKED_WITH_DATA: case
     *           StartForResults.PHOTO_PICKED_WITH_DATA: PickData mData =
     *           StartForResults.onRresult(this, requestCode,resultCode,
     *           paramIntent); } }
     */
    public final static int PHOTO_PICKED_WITH_DATA = 111;

    public final static int CAMERA_PICKED_WITH_DATA = 222;

    public final static int CROP_PHOTO_FROM_URI = 10;

    public final static String TAG = "StartForResults";

    // get photo from album
    public static void getPicFromAlbum(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.CONTENT_TYPE);
        try {
            activity.startActivityForResult(intent, PHOTO_PICKED_WITH_DATA);// 启动相册
        } catch (ActivityNotFoundException e) {
        }
    }

    // get photo from album
    public static void cropPicWithUrl(Uri photoUri, Activity activity, int w,
            int h) {
        Log.e("uri", photoUri.toString());
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", w / 10);
        intent.putExtra("aspectY", h / 10);// x:y=1:2
        intent.putExtra("outputX", w);
        intent.putExtra("outputY", h);
        intent.putExtra("return-data", true);

        activity.startActivityForResult(intent, CROP_PHOTO_FROM_URI);

        // final Bundle extras = paramIntent.getExtras();
        // if (extras != null) {
        // Bitmap photo = extras.getParcelable("data");
        // }
    }

    // view photo from album
    public static void viewPicFromAlbum(Activity activity, String loadpath) {

        Intent intent = new Intent("android.intent.action.VIEW");

        intent.addCategory("android.intent.category.DEFAULT");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = Uri.fromFile(new File(loadpath));

        intent.setDataAndType(uri, "image/*");

        activity.startActivity(intent);
    }

    // get photo from camera
    public static void getPicFromCamera(Activity activity) {
        // set camera intent
        Intent localIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        // temp file
        File dir = Environment.getExternalStorageDirectory();
        File localTemp = new File(dir, "camera-t.jpg");

        Uri localUri = Uri.fromFile(localTemp);
        localIntent.putExtra(MediaStore.EXTRA_OUTPUT, localUri);
        localIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 50);

        activity.startActivityForResult(localIntent, CAMERA_PICKED_WITH_DATA);
    }

    public static class PickData {
        public Bitmap thumbBmp = null;

        public String localPath;

        public String title;

        // type:1 = GIF，2 = JPG，3 = PNG
        public int type;

        public long size;

        public int width;

        public int height;

        public PickData() {
        }

        public PickData(String lp) {
            localPath = lp;
            type = getSuffixInt(localPath);
            title = new File(localPath).getName();
        }

        public Bitmap getThumbBitmap(int size) {
            if (thumbBmp == null || thumbBmp.isRecycled()) {
                thumbBmp = BitmapToolkit.loadLocalBitmapExactScaled(localPath, size);
            }
            return thumbBmp;
        }
    }

    public static PickData onRresult(Activity activity, int requestCode,
            int resultCode, Intent paramIntent) {
        /***/
        Log.i(TAG, "requestCode" + requestCode + "|resultCode" + resultCode
                + "|" + paramIntent);

        PickData pickData = null;

        if (resultCode != Activity.RESULT_OK)
            return pickData;

        pickData = new PickData();

        Uri photoUri = null;
        if (paramIntent != null)
            photoUri = paramIntent.getData();
        switch (requestCode) {
        // pick photo from album
            case StartForResults.PHOTO_PICKED_WITH_DATA:
                // get path
                pickData.localPath = getLocalPath(photoUri, activity);
                File photoFile = new File(pickData.localPath);
                if (IMUtil.isEmptyString(pickData.localPath) || !photoFile.exists()) {
                    photoUri = paramIntent.getData();
                    Cursor cursor = activity.getContentResolver().query(photoUri, null, null, null,
                            null);
                    cursor.moveToFirst();
                    int index = cursor.getColumnIndex("_data");
                    if (index < 0) {
                        return null;
                    }
                    pickData.localPath = cursor.getString(index);
                }
                if (pickData.localPath.length() == 0)
                    return null;

                // get title
                pickData.title = new File(pickData.localPath).getName();

                // get type
                pickData.type = getSuffixInt(pickData.localPath);

                if (pickData.type == -1)
                    return null;
                break;
            // pick photo from camera
            case StartForResults.CAMERA_PICKED_WITH_DATA:
                try {
                    // 获取 拍照临时文件
                    File sdDir = Environment.getExternalStorageDirectory();
                    String tempPicName = "camera-t.jpg";
                    File tempPic = new File(sdDir, tempPicName);
                    if (!tempPic.exists()) {
                        photoUri = paramIntent.getData();
                        Cursor cursor = activity.getContentResolver().query(photoUri, null, null,
                                null,
                                null);
                        cursor.moveToFirst();
                        int index = cursor.getColumnIndex("_data");
                        if (index < 0) {
                            return null;
                        }
                        pickData.localPath = cursor.getString(index);
                        File uriFile = new File(pickData.localPath);
                        if (!uriFile.exists()) {
                            return null;
                        }
                        // get title
                        pickData.title = uriFile.getName();

                        // set type jpg default
                        pickData.type = 2;
                        return pickData;
                    }

                    BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_CAMERA, ""
                            + System.currentTimeMillis(), "", ".jpg");
                    bt.mkdirsIfNotExist();
                    File moveTo = new File(bt.getAbsolutePath());
                    tempPic.renameTo(moveTo);

                    pickData.localPath = moveTo.getAbsolutePath();

                    // get title
                    pickData.title = moveTo.getName();

                    // set type jpg default
                    pickData.type = 2;
                } catch (Exception localException) {
                    Log.e(TAG,
                            "localException " + localException == null ? "" : localException
                                    .getMessage());
                }
                break;
        }

        return pickData;
    }

    public static String getSuffix(String url) {
        int index = url.lastIndexOf(".");

        if (index > 0 && index < url.length() - 1) {
            String typeStr = url.substring(index + 1);
            return typeStr.toLowerCase();
        }

        return "";
    }

    public static int getSuffixInt(String url) {
        // type:1 = GIF，2 = JPG，3 = PNG
        String typeStr = getSuffix(url);
        /**
         * 默认当作jpeg
         */
        int type = 2;
        if (typeStr.equalsIgnoreCase("gif")) {
            type = 1;
        }

        if (typeStr.equalsIgnoreCase("jpg") || typeStr.equalsIgnoreCase("jpeg")) {
            type = 2;
        }

        if (typeStr.equalsIgnoreCase("png")) {
            type = 3;
        }
        return type;
    }

    private static String getLocalPath(Uri uri, Activity activity) {
        String path = "";
        // 获取文件的绝对路径
        String[] projection = {
                BaseColumns._ID,
                MediaColumns.DATA, MediaColumns.SIZE
        };
        Cursor cursor = MediaStore.Images.Media.query(activity
                .getContentResolver(), uri, projection);
        if (cursor != null && cursor.moveToNext()) {
            path = cursor.getString(cursor
                    .getColumnIndexOrThrow(MediaColumns.DATA));
        }
        if (IMUtil.isEmptyString(path)) {
            Log.w(TAG, "get local path fail, trying get path from uri.");
            path = uri.getPath();
            if (IMUtil.isEmptyString(path)) {
                Log.e(TAG, "get local path fail, trying get path from uri. fail");
            }
        }
        return path;
    }
}

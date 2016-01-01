
package cn.com.nd.momo.api;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpStatus;
import org.json.JSONArray;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import cn.com.nd.momo.activity.SelectorActivity;
import cn.com.nd.momo.activity.Statuses_Activity;
import cn.com.nd.momo.activity.Statuses_Comment_Activity.CommentItemInfo;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.statuses.StatusesManager;
import cn.com.nd.momo.api.types.Attachment;
import cn.com.nd.momo.api.types.GroupInfo;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;
import cn.com.nd.momo.api.util.gps.LocManager;
import cn.com.nd.momo.api.util.gps.LocManager.Result;
import cn.com.nd.momo.api.AbsSdk.SdkResult;
import cn.com.nd.momo.model.DraftMgr;
import cn.com.nd.momo.model.DynamicDB.DraftInfo;
import cn.com.nd.momo.model.DynamicMgr;
import cn.com.nd.momo.util.NotifyProgress;
import cn.com.nd.momo.util.StartForResults;

public class DynamicPoster {
    private static final int HTTP_OK = 200;

    private static final String TAG = "DynamicPoster";

    public DynamicPoster(Activity ac, long draftid) {
        mDynamicPostInfo = new DynamicPostInfo();
        mDynamicPostInfo.mDraftid = draftid;

        mActivity = ac;
    }

    public DynamicPoster(Activity ac) {
        mDynamicPostInfo = new DynamicPostInfo();
        mActivity = ac;
    }

    private static Activity mActivity;

    public DynamicPostInfo mDynamicPostInfo;

    public class DynamicPostInfo {
        // 用户id
        // public long mParamUid;
        // 评论id
        public String mParamCommentid;

        // 类型id
        // public long mParamTypeid;
        // resend
        // public String mParamObjid;
        public String mParamID;

        // resend // 转发id
        public String mParamRetweetId = null;

        // resend // 内容
        public String mParamContent = "";

        // sync sina
        public boolean mParamIsSyncSina;

        // resend // 群组信息
        public GroupInfo mParamGroupInfo;

        // 图片
        public ArrayList<Attachment> attachmentList = new ArrayList<Attachment>();

        // draft id
        private long mDraftid;

        public long getDraftID() {
            return mDraftid;
        }

        public void setDraftID(long draft) {
            mDraftid = draft;
        }

        public void setParamComment(String id) {
            setParam(id, null, null);
        }

        public void setParamReply(String id, String cid) {
            setParam(id, cid, null);
        }

        public void setParamRetweet(String retweetid) {
            setParam("", null, retweetid);
        }

        private void setParam(String id, String cid, String retweetid) {
            // mParamUid = uid;
            mParamID = id;
            mParamCommentid = cid;
            mParamRetweetId = retweetid;
        }
    }

    public Result getLocationResult() {
        Result locationResult = null;
        boolean geoLocation = ConfigHelper.getInstance(mActivity).loadBooleanKey(
                ConfigHelper.CONFIG_KEY_GEOGRAPHY_LOCATION, false);
        if (geoLocation) {
            final LocManager locManager = new LocManager(mActivity);
            locationResult = locManager.getLocationPoint();
        }

        return locationResult;
    }

    /**
     * <br>
     * Title:上传图片类 <br>
     * Description: <br>
     * Author:hexy <br>
     * Date:2011-4-11下午07:25:12
     */
    public static class PhotoUpload {
        public StartForResults.PickData mLocalPhotoInfo = null;

        public String mSucceedUrl = "";

        public String mPhotoID = "";

        public boolean mIsUploading = false;

        private byte[] mBytes;

        // poster 的handler 仅为了更新主界面进度
        Handler mPhotoHandler;

        public PhotoUpload(StartForResults.PickData data) {
            mLocalPhotoInfo = data;
        }

        /**
         * <br>
         * Description:准备上传， 以md5判断服务端时候有图片， 没有则断点续传 <br>
         * Author:hexy <br>
         * Date:2011-4-11下午08:12:17
         */
        private SdkResult uploadPhoto() {
            SdkResult result = new SdkResult();
            String localPath = mLocalPhotoInfo.localPath;

            try {
            	Attachment attachment;
            	boolean fullSize = ConfigHelper.getInstance(Utils.getContext()).loadBooleanKey(ConfigHelper.CONFIG_KEY_FULL_SIZE, false);
            	if(fullSize) {
            		attachment = MoMoHttpApi.upLoadPhoto(localPath);
            	} else {
            		mBytes = BitmapToolkit.loadLocalBitmapExactScaledBytes(localPath,
            				ConfigHelper.PHOTO_UPLOAD_COMPRESS);
            		
            		if (mBytes == null) {
            			return result;
            		}
            		attachment = MoMoHttpApi.upLoadPhoto(mBytes);
            	}
            	mSucceedUrl = attachment.getUrl();
            	mPhotoID = attachment.getID();
            	result.ret = HttpStatus.SC_OK;
            } catch (MoMoException e) {
            }
            return result;
        }
    }

    // PhotoUploadInfo
    private ArrayList<PhotoUpload> mPhotoToUpload = new ArrayList<PhotoUpload>();

    public ArrayList<PhotoUpload> getPhotoUploadArray() {
        Log.i(TAG, "LOCAL ERROR getPhotoUploadArray()");
        return mPhotoToUpload;
    }

    public ArrayList<String> getLocalUrls() {
        ArrayList<String> arraylist = new ArrayList<String>();
        for (PhotoUpload p : getPhotoUploadArray()) {
            arraylist.add(p.mLocalPhotoInfo.localPath);
        }
        return arraylist;
    }

    private long getTotalPhotoSize() {
        long size = 0;
        for (PhotoUpload p : getPhotoUploadArray()) {
            // 由于要上传准备 才能获取到size， 所以先用伪数据
            size += (p.mLocalPhotoInfo.size == 0 ? 102400 : p.mLocalPhotoInfo.size);
        }
        return size;
    }

    private long getUploadedPhotoSize() {
        long size = 0;
        for (PhotoUpload p : getPhotoUploadArray()) {
            if (p.mSucceedUrl != null && p.mSucceedUrl.length() > 0) {
                size += p.mLocalPhotoInfo.size;
            }
        }
        return size;
    }

    /**
     * <br>
     * Description:添加照片上传 <br>
     * Author:hexy <br>
     * Date:2011-4-12上午11:03:30
     * 
     * @param pUploadInfo
     */
    public void addPhoto(PhotoUpload pUploadInfo) {
        Log.i(TAG, "LOCAL ERROR addPhoto");
        // 防止重复
        if (getPhotoIndex(pUploadInfo.mLocalPhotoInfo.localPath) == -1) {
            pUploadInfo.mPhotoHandler = mHandler;
            getPhotoUploadArray().add(pUploadInfo);
        }
    }

    private int getPhotoIndex(String url) {
        for (int i = 0; i < getPhotoUploadArray().size(); i++) {
            if (getPhotoUploadArray().get(i).mLocalPhotoInfo.localPath.equals(url)) {
                return i;
            }
        }
        return -1;
    }

    public void removePhoto(String url) {
        int index = getPhotoIndex(url);
        if (index != -1) {
            getPhotoUploadArray().remove(index);
        }
    }

    public void removeAll() {
        getPhotoUploadArray().clear();

    }

    /**
     * <br>
     * Description:add to draft <br>
     * Author:hexy <br>
     * Date:2011-4-11下午07:28:34
     * 
     * @return
     */
    public long addDraft() {
        DraftInfo draftInfo = new DraftInfo();
        //todo
        //draftInfo.content = mDynamicPostInfo.mParamContent.length() == 0 ? "分享照片"
        //: SelectorActivity.encode(mDynamicPostInfo.mParamContent);

        draftInfo.content = mDynamicPostInfo.mParamContent.length() == 0 ? "分享照片"
                : mDynamicPostInfo.mParamContent;


        // save images
        JSONArray jsonImages = new JSONArray();
        for (PhotoUpload image : getPhotoUploadArray())
            jsonImages.put(image.mLocalPhotoInfo.localPath);

        if (jsonImages.length() > 0)
            draftInfo.images = jsonImages.toString();

        // save group
        if (mDynamicPostInfo.mParamGroupInfo != null) {
            draftInfo.groupid = mDynamicPostInfo.mParamGroupInfo.getGroupID();
        }
        if (mDynamicPostInfo.mParamRetweetId != null) {
            draftInfo.objid = mDynamicPostInfo.mParamRetweetId;
            for (String image : Statuses_Activity.mDynamicItemInfo.images)
                jsonImages.put(image);
            if (Statuses_Activity.mDynamicItemInfo.images.size() > 0)
                draftInfo.images = jsonImages.toString();
        }
        draftInfo.crateData = System.currentTimeMillis() / 1000;
        long id = DraftMgr.instance().insertDraft(draftInfo);
        mDynamicPostInfo.mDraftid = id;
        return id;
    }

    /**
     * <br>
     * Description:发送广播 <br>
     * Author:hexy <br>
     * Date:2011-4-19下午03:44:09
     */
    public void postBroadcast() {
        new Thread() {
            @Override
            public void run() {
                SdkResult result = new SdkResult();
                //// TODO: 15/12/31
//                if (SelectorActivity.getAtSize() > 0) {
//                    ConfigHelper.getInstance(mActivity).saveKey("mome_viewed", "0");
//                    ConfigHelper.getInstance(mActivity).commit();
//                }
                do {
                    // 更新主界面
                    sendBroadcastProcess(0);

                    // 上传图片列表
                    boolean uploadSucceed = true;

                    mDynamicPostInfo.attachmentList.clear();
                    for (PhotoUpload info : getPhotoUploadArray()) {
                        result = info.uploadPhoto();
                        Attachment attachment = new Attachment();
                        attachment.setID(info.mPhotoID);
                        attachment.setUrl(info.mSucceedUrl);
                        mDynamicPostInfo.attachmentList.add(attachment);
                        Log.i(TAG, "step 3:" + result.Log());
                        if (result.ret != HTTP_OK) {
                            uploadSucceed = false;
                            break;
                        }
                    }
                    if (!uploadSucceed) {
                        break;
                    }

                    //todo
//                    mDynamicPostInfo.mParamContent = mDynamicPostInfo.mParamContent.length() == 0 ? "分享照片"
//                            : SelectorActivity.encode(mDynamicPostInfo.mParamContent);

                    mDynamicPostInfo.mParamContent = mDynamicPostInfo.mParamContent.length() == 0 ? "分享照片"
                            : mDynamicPostInfo.mParamContent;
                    try {
                        String statusesID = StatusesManager.postStatuses(mDynamicPostInfo);

                        // 发送成功 删除草稿
                        DraftMgr.instance().deleteDraft(mDynamicPostInfo.mDraftid);

                        result.ret = HttpStatus.SC_OK;
                    } catch (MoMoException e) {
                        result.ret = e.getCode();
                        result.response = e.getMessage();
                    }
                } while (false);

                Message msg = new Message();
                msg.what = DynamicMgr.MSG_POST_BROADCAST;
                msg.obj = result;
                Log.i(TAG, "step end:" + result.Log());
                mHandler.sendMessage(msg);
            }
        }.start();
    }

    // 请求方式 POST
    // 请求资源 /praise?sid={sid}
    // sid 必填，登录用户的sessionID
    // 请求数据
    // {
    // "appdescribe":"动态类型，默认为record_add",
    // "objid":"动态对象id",
    // "uid":"动态所属用户uid"
    // }
    public void postComment(final String content, final String commentID) {
        new Thread() {
            @Override
            public void run() {
                if (SelectorActivity.getAtSize() > 0) {
                    ConfigHelper.getInstance(mActivity).saveKey("mome_viewed", "0");
                    ConfigHelper.getInstance(mActivity).commit();
                }

                SdkResult result = new SdkResult();
                try {
                    String strComment = content.length() == 0 ? "评论"
                            : SelectorActivity.encode(content);
                    Log.i(TAG, "postReply: " + strComment);
                    StatusesManager.postComment(mDynamicPostInfo.mParamID, strComment, commentID);
                    result.ret = HttpStatus.SC_OK;

                } catch (MoMoException e) {
                    result.response = e.getSimpleMsg();
                    result.ret = e.getCode();
                }

                Message msg = new Message();
                msg.what = DynamicMgr.MSG_POST_COMMENT;
                msg.obj = result;

                mHandler.sendMessage(msg);


            }
        }.start();
    }

    public void postReply(final String content) {
        Log.i(TAG, "postReply" + mDynamicPostInfo.mParamCommentid);
        postComment(content, mDynamicPostInfo.mParamCommentid);
    }

    private void sendBroadcastProcess(final long size) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                long total = getTotalPhotoSize();
                long current = getUploadedPhotoSize() + size;
                int percent = total == 0 ? 0 : (int)((current * 100) / total);
                Log.i(TAG, "sendBroadcastProcess " + percent);

                Intent brodcastIntent = new Intent(NotifyProgress.ACTION_PROCESS);

                // Log.i(TAG, "MSG_POST_BROADCAST DRAFT ID = "+mDraftid);
                brodcastIntent.putExtra(DraftMgr.DRAFT_ID, mDynamicPostInfo.mDraftid);
                brodcastIntent.putExtra(DraftMgr.DRAFT_PROCESS, percent);
                mActivity.sendBroadcast(brodcastIntent);
            }
        });
    }

    private void sendBroadcastFail(final String msg) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                Intent brodcastIntent = new Intent(NotifyProgress.ACTION_FAIL);
                Log.i(TAG, "MSG_POST_BROADCAST DRAFT ID = " + mDynamicPostInfo.mDraftid);
                brodcastIntent.putExtra(DraftMgr.DRAFT_ID, mDynamicPostInfo.mDraftid);
                brodcastIntent.putExtra("error", msg);
                mActivity.sendBroadcast(brodcastIntent);
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            SdkResult result = (SdkResult)msg.obj;
            // JSONObject jRet = null;
            // try {
            // if (result.response != null && result.response.length() > 0) {
            // jRet = new JSONObject(result.response);
            // }
            // } catch (Exception e) {
            // }

            try {
                Log.i(TAG, "handleMessage:" + result.ret + "---" + result.response);

                if (result.ret == HTTP_OK) {
                    switch (msg.what) {
                    // // 图片进度
                    // case DynamicMgr.MSG_UPLOAD_PROCESS:
                    // long offsetRet = jRet.optLong("offset");
                    // sendBroadcastProcess(offsetRet);
                    // break;
                    // 发送广播成功时 更新主界面
                        case DynamicMgr.MSG_POST_BROADCAST:
                            Intent brodcastIntent = new Intent(NotifyProgress.ACTION_SUCCEED);
                            Log.i(TAG, "MSG_POST_BROADCAST DRAFT ID = " + mDynamicPostInfo.mDraftid);
                            brodcastIntent.putExtra(DraftMgr.DRAFT_ID, mDynamicPostInfo.mDraftid);
                            mActivity.sendBroadcast(brodcastIntent);
                            break;
                        // 发送评论成功时 更新主界面
                        case DynamicMgr.MSG_POST_COMMENT:
                        case DynamicMgr.MSG_POST_REPLY:
                            Intent commentIntent = new Intent(NotifyProgress.ACTION_COMMENT_SUCCEED);

                            Log.i(TAG, "MSG_POST_COMMENT DRAFT ID = " + mDynamicPostInfo.mDraftid);
                            commentIntent.putExtra(CommentItemInfo.COMMENT_ID,
                                    mDynamicPostInfo.mDraftid);
                            commentIntent.putExtra(CommentItemInfo.COMMENT_CONTENT,
                                    mDynamicPostInfo.mParamContent);
                            mActivity.sendBroadcast(commentIntent);
                            break;
                        default:
                            break;
                    }

                } else {
                    // final String errorMsg = DynamicMgr.getErrorCode(result);
                    final String errorMsg = result.response;
                    switch (msg.what) {
                    // 发送评论失败时 更新主界面
                        case DynamicMgr.MSG_POST_COMMENT:
                        case DynamicMgr.MSG_POST_REPLY:
                            Intent commentIntent = new Intent(NotifyProgress.ACTION_COMMENT_FAIL);
                            commentIntent.putExtra(CommentItemInfo.COMMENT_ID,
                                    mDynamicPostInfo.mDraftid);
                            commentIntent.putExtra(CommentItemInfo.COMMENT_CONTENT,
                                    mDynamicPostInfo.mParamContent);
                            mActivity.sendBroadcast(commentIntent);
                            break;
                        case DynamicMgr.MSG_POST_BROADCAST:
                            // 更新主界面
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    sendBroadcastFail(errorMsg);
                                }
                            }, 500);
                            break;
                        default:
                            Toast.makeText(mActivity, errorMsg, 0).show();
                            break;
                    }
                }
            } catch (Exception e) {
            }
        }

    };

}

// Returns the contents of the file in a byte array.
// private static byte[] getBytesFromFile(File file) throws IOException {
// InputStream is = new FileInputStream(file);
//
// // Get the size of the file
// long length = file.length();
//
// // You cannot create an array using a long type.
// // It needs to be an int type.
// // Before converting to an int type, check
// // to ensure that file is not larger than Integer.MAX_VALUE.
// if (length > Integer.MAX_VALUE) {
// // File is too large
// }
//
// // Create the byte array to hold the data
// byte[] bytes = new byte[(int) length];
//
// // Read in the bytes
// int offset = 0;
// int numRead = 0;
// while (offset < bytes.length
// && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
// offset += numRead;
// }
//
// // Ensure all the bytes have been read in
// if (offset < bytes.length) {
// throw new IOException("Could not completely read file "
// + file.getName());
// }
//
// // Close the input stream and return bytes
// is.close();
// return bytes;
// }
// }

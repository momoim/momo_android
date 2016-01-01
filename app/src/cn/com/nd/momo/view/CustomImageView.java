
package cn.com.nd.momo.view;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ImageView;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.GlobalContactList;
import cn.com.nd.momo.api.sync.MoMoContactsManager;
import cn.com.nd.momo.api.types.Contact;
import cn.com.nd.momo.api.util.BitmapToolkit;
import cn.com.nd.momo.api.util.ConfigHelper;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.im.buss.IMUtil;
import cn.com.nd.momo.manager.AvatarManager;
import cn.com.nd.momo.manager.CardManager;
import cn.com.nd.momo.manager.GlobalUserInfo;
import cn.com.nd.momo.manager.CardManager.IgnoreMobilePrefix;

public class CustomImageView extends ImageView {
    protected static final String TAG = "CustomImageView";

    private Handler handle = new Handler();

    private long mCurrentUid = 0;

    private String zoneCode = GlobalUserInfo.DEFAULT_ZONE_CODE;

    private IgnoreMobilePrefix ignoreObject = null;

    public CustomImageView(Context context) {
        super(context);
        init();
        zoneCode = GlobalUserInfo.getCurrentZoneCode(context);
        ignoreObject = CardManager.getInstance().getIgnoreObject(context);
    }

    public CustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        zoneCode = GlobalUserInfo.getCurrentZoneCode(context);
        ignoreObject = CardManager.getInstance().getIgnoreObject(context);
    }

    public CustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
        zoneCode = GlobalUserInfo.getCurrentZoneCode(context);
        ignoreObject = CardManager.getInstance().getIgnoreObject(context);
    }

    public void setImageBitmapSafe(Bitmap bmp) {
        if (bmp != null && bmp.isRecycled()) {
            bmp = null;
        }

        this.setImageBitmap(bmp);
    }

    /**
     * 设置默认头像背景，src如果bitmap被设置为null后会变成一片空白， 而如果不设置为null，则可能头像乱窜
     * TODO:这个控件并不是专门为ListView写的，所以关于ListView头像乱窜的问题应该在外面解决，而不应该由
     * 这个控件的构造函数来做。请保持此控件的干净。
     * 
     * @author Tsung Wu <tsung.bz@gmail.com>
     */
    private void init() {
        this.setBackgroundResource(R.drawable.ic_contact_picture);
    }

    /**
     * 通过uid获取用户头像图片并填充ImageView
     * 
     * @param uid 用户头像的uid
     */
    public void setBimtmapUid(final long uid) {
        // hexy 修改 , 已经废弃的接口
        // String url = DynamicMgr.getAvatar(uid);
        // setBitmapUri(url);
    }

    /**
     * 通过url获取图片填充ImageView
     * 
     * @param url 图片url
     */
    public void setBitmapUri(final String url) {
        new Thread() {
            @Override
            public void run() {
                BitmapToolkit bt = new BitmapToolkit(
                        BitmapToolkit.DIR_MOMO_PHOTO, url, "", "");
                bt.setSid(GlobalUserInfo.getSessionID());
                Bitmap tempBmp = bt.loadBitmapNetOrLocal();
                if (tempBmp != null) {
                    tempBmp = BitmapToolkit.compress(tempBmp, 80);
                    tempBmp = BitmapToolkit.cornerBitmap(tempBmp, 8);
                }
                final Bitmap bitmap = tempBmp;
                if (bitmap == null) {
                    Log.e(TAG, "could not get the bitmap:" + url);
                    return;
                }
                CustomImageView.this.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomImageView.this.setImageBitmapSafe(bitmap);
                    }
                });
            }
        }.start();

    }

    /**
     * 通过uid，或者uri设置用户头像
     * 
     * @param uid 用户的uid
     * @param url 头像的url,可为null
     *            （当ur为null的时候，可能在取某些头像的时候效率会低——取的是不是好友的头像，或者头像没有同步下来）
     */
    public void setCustomImage(final long uid, final String avatarUrl) {
        new Thread() {
            @Override
            public void run() {
                final Bitmap bp = AvatarManager.getAvaterBitmap(uid,
                        avatarUrl);
                if (bp == null) {
                    Log.e(TAG, "could not get the bitmap uid:" + uid
                            + "; avatarUrl:" + avatarUrl);
                    return;
                }
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.i(TAG,
                                "get the bitmap, uid:" + uid + "; avatarUrl:"
                                        + avatarUrl + "; Bitmap:"
                                        + bp.hashCode());
                        CustomImageView.this.setImageBitmapSafe(bp);
                    }
                });
            }
        }.start();

    }

    /**
     * 通过uri设置用户头像
     * 
     * @param url 头像的url
     */
    public void setCustomOriginalImage(final long uid, final String avatarUrl) {
        new Thread() {
            @Override
            public void run() {
                final Bitmap bp = AvatarManager.getAvaterBitmapOrigin(uid, avatarUrl);
                if (bp == null) {
                    Log.e(TAG, "could not get the bitmap " + " avatarUrl:" + avatarUrl);
                    return;
                }
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomImageView.this.setImageBitmapSafe(bp);
                    }
                });
            }
        }.start();

    }

    public void setCustomOriginalBigImage(final String avatarUrl) {
        new Thread() {
            @Override
            public void run() {
                final Bitmap bp = AvatarManager.getBigAvaterBitmapOrigin(avatarUrl);
                if (bp == null) {
                    Log.e(TAG, "could not get the bitmap " + " avatarUrl:" + avatarUrl);
                    return;
                }
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomImageView.this.setImageBitmapSafe(bp);
                    }
                });
            }
        }.start();

    }

    /**
     * 通过uid，或者uri设置用户头像
     * 
     * @param uid 用户的uid
     * @param url 头像的url,可为null
     *            （当ur为null的时候，可能在取某些头像的时候效率会低——取的是不是好友的头像，或者头像没有同步下来）
     */
    public void setCustomImage(final long uid, final String avatarUrl,
            final ConcurrentHashMap<Long, Bitmap> cache, boolean scrolling) {
        mCurrentUid = uid;
        Bitmap temply = cache.get(uid);
        CustomImageView.this.setImageBitmapSafe(temply);
        if (temply != null) {
            Log.i(TAG, "set the bitmap from cache, " + "uid:" + uid + "; "
                    + "avatarUrl:" + avatarUrl);
            if (BitmapToolkit.available() && avatarUrl != null && avatarUrl.length() > 5) {
                String url = avatarUrl.replace("_48.jpg", "_130.jpg");
                BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_PHOTO, url, "",
                        ".small.avatar");
                File aFile = new File(bt.getAbsolutePath());
                if (aFile != null && aFile.exists()) {
                    return;
                } else {
                    CustomImageView.this.setImageBitmapSafe(null);
//                    temply.recycle();
//                    temply = null;
                    cache.remove(uid);
                }
            } else {
                return;
            }
        }

        // 优化滚动显示
        if (scrolling) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                Bitmap bp = null;

                boolean avatarExist = false;
                Bitmap bmpOld = cache.get(uid);
                if (bmpOld != null) {
                    if (BitmapToolkit.available() && avatarUrl != null && avatarUrl.length() > 5) {
                        String url = avatarUrl.replace("_48.jpg", "_130.jpg");
                        BitmapToolkit bt = new BitmapToolkit(BitmapToolkit.DIR_MOMO_PHOTO, url, "",
                                ".small.avatar");
                        File aFile = new File(bt.getAbsolutePath());
                        if (aFile != null && aFile.exists()) {
                            avatarExist = true;
                        }
                    }
                    if (avatarExist) {
                        bp = bmpOld;
                    } else {
                        cache.remove(uid);
//                        bmpOld.recycle();
//                        bmpOld = null;
                    }
                }
                if (!avatarExist) {
                    bp = AvatarManager.getAvaterBitmap(uid,
                            avatarUrl);
                    if (bp == null) {
                        Log.e(TAG, "could not get the bitmap uid:" + uid
                                + " avatarUrl:" + avatarUrl);
                    } else {
                        Log.i(TAG, "get the bitmap from inteface, " + "uid:"
                                + uid + "; " + "avatarUrl:" + avatarUrl);
                        CustomImageView.this.setTag(false);
                    }
                }
                final Bitmap ava = bp;
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap temply = ava;
                        if (temply == null) {
                            temply = ((BitmapDrawable)CustomImageView.this
                                    .getResources().getDrawable(
                                            R.drawable.ic_contact_picture))
                                    .getBitmap();
                        }
                        if (!cache.containsKey(uid)) {
                            cache.put(uid, temply);
                        }
                        /**
                         * 可能这个view已经被复用了，不能在这时候设置使用，会导致混乱
                         * 
                         * @author Tsung Wu <tsung.bz@gmail.com>
                         */
                        if (mCurrentUid == uid) {
                            CustomImageView.this.setImageBitmapSafe(temply);
                        }
                        if (onAvatarListener != null) {
                            onAvatarListener.onAvatarDownload();
                        }
                    }
                });
            }
        }.start();

    }

    private String mCurrentMobile = "";

    /**
     * 通过电话号码设置用户头像
     * 
     * @param uid 用户的uid
     * @param url 头像的url,可为null
     *            （当ur为null的时候，可能在取某些头像的时候效率会低——取的是不是好友的头像，或者头像没有同步下来）
     */
    public void setCustomImageByMobile(final String mobile,
            final ConcurrentHashMap<String, Bitmap> cache, boolean scrolling) {
        mCurrentMobile = mobile;
        if (IMUtil.isEmptyString(mobile)) {
            return;
        }
        Bitmap temply = cache.get(mobile);
        CustomImageView.this.setImageBitmapSafe(temply);
        if (temply != null) {
            return;
        }

        // 优化滚动显示
        if (scrolling) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                final Bitmap bp;
                if (cache.get(mobile) != null) {
                    bp = cache.get(mobile);
                } else {
                    Contact contact = GlobalContactList.getInstance().getContactByMobile(mobile);
                    if (contact != null && contact.getAvatar() != null) {
                        byte[] bytes = contact.getAvatar().getMomoAvatarImage();
                        if (bytes != null) {
                            bp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        } else {
                            bp = null;
                        }
                    } else {
                        bp = null;
                    }
                    if (bp == null) {
                        //
                    } else {
                        CustomImageView.this.setTag(false);
                    }
                }
                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap temply = bp;
                        if (temply == null) {
                            temply = ((BitmapDrawable)CustomImageView.this
                                    .getResources().getDrawable(
                                            R.drawable.ic_contact_picture))
                                    .getBitmap();
                        } else {
                            temply = BitmapToolkit.compress(temply, ConfigHelper.SZIE_AVATAR);
                            temply = BitmapToolkit.cornerBitmap(temply, 8);
                        }
                        if (!cache.containsKey(mobile)) {
                            cache.put(mobile, temply);
                        }
                        /**
                         * 可能这个view已经被复用了，不能在这时候设置使用，会导致混乱
                         * 
                         * @author Tsung Wu <tsung.bz@gmail.com>
                         */
                        if (mCurrentMobile == mobile) {
                            CustomImageView.this.setImageBitmapSafe(temply);
                        }
                        if (onAvatarListener != null) {
                            onAvatarListener.onAvatarDownload();
                        }
                    }
                });
            }
        }.start();

    }

    private long mCurrentContactId = 0;

    public void setCustomImageByContactId(final long contactId,
            final ConcurrentHashMap<Long, Bitmap> cache, boolean scrolling) {
        mCurrentContactId = contactId;
        mCurrentRobotId = 0;
        if (contactId < 0 && contactId != -1) {
            return;
        }
        Bitmap temply = cache.get(contactId);
        CustomImageView.this.setImageBitmapSafe(temply);
        if (temply != null) {
            mCurrentContactId = 0;
            return;
        }
        // 优化滚动显示
        if (scrolling) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                Bitmap bp = null;

                if (contactId == -1) {
                    String avatarUrl = GlobalUserInfo.getAvatar();
                    long userId = 0;
                    try {
                        userId = Long.valueOf(GlobalUserInfo.getUID());
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    bp = AvatarManager.getAvaterBitmap(userId, avatarUrl);
                } else {
                    Contact contact = MoMoContactsManager.getInstance().getContactById(contactId);

                    if (contact != null && contact.getPhoneList() != null) {
                        Map<String, String> cardMap = CardManager.getInstance().getCardCache();
                        for (String phone : contact.getPhoneList()) {
                            phone = ignoreObject.ignore(zoneCode, phone);
                            if (phone == null || phone.length() < 1) {
                                continue;
                            } else if (cardMap.containsKey(phone)
                                    && cardMap.get(phone).length() > 0) {
                                bp = AvatarManager.getAvaterBitmap(0, cardMap.get(phone));
                                break;
                            }
                        }
                    }
                }
                if (bp == null) {
                    //
                } else {
                    CustomImageView.this.setTag(false);
                }

                final Bitmap tbp = bp;

                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap temply = tbp;
                        if (temply == null) {
                            temply = ((BitmapDrawable)CustomImageView.this
                                    .getResources().getDrawable(
                                            R.drawable.ic_contact_picture))
                                    .getBitmap();
                        } else {
                            temply = BitmapToolkit.compress(temply, ConfigHelper.SZIE_AVATAR);
                            temply = BitmapToolkit.cornerBitmap(temply, 8);
                        }

                        if (!cache.containsKey(contactId)) {
                            cache.put(contactId, temply);
                        }

                        if (mCurrentContactId == contactId) {
                            mCurrentContactId = 0;
                            CustomImageView.this.setImageBitmapSafe(temply);
                        }
                    }
                });
            }
        }.start();

    }

    private long mCurrentRobotId = 0;

    private boolean loadAvatar = false;

    public void cancelLoadAvatar() {
        loadAvatar = true;
    }

    public void setCustomImageByRobotId(final long roleId, final String avatarUrl,
            final ConcurrentHashMap<Long, Bitmap> cache, boolean scrolling) {
        mCurrentRobotId = roleId;
        mCurrentContactId = 0;
        loadAvatar = false;
        if (roleId < 0 || avatarUrl == null || avatarUrl.length() < 1) {
            return;
        }
        Bitmap temply = cache.get(roleId);
        CustomImageView.this.setImageBitmapSafe(temply);
        if (temply != null) {
            return;
        }
        // 优化滚动显示
        if (scrolling) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                final Bitmap bp;
                bp = AvatarManager.getAvaterBitmap(roleId, avatarUrl);
                if (bp == null) {
                    //
                } else {
                    CustomImageView.this.setTag(false);
                }

                handle.post(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap temply = bp;
                        if (temply == null) {
                            temply = ((BitmapDrawable)CustomImageView.this
                                    .getResources().getDrawable(
                                            R.drawable.ic_contact_picture))
                                    .getBitmap();
                        } else {
                            temply = BitmapToolkit.compress(temply, ConfigHelper.SZIE_AVATAR);
                            temply = BitmapToolkit.cornerBitmap(temply, 8);
                        }
                        if (!cache.containsKey(roleId)) {
                            cache.put(roleId, temply);
                        }

                        if (mCurrentRobotId == roleId && !loadAvatar) {
                            CustomImageView.this.setImageBitmapSafe(temply);
                        }
                    }
                });
            }
        }.start();

    }

    public void setCustomImage(final long uid, final String avatarUrl,
            final ConcurrentHashMap<Long, Bitmap> cache) {
        setCustomImage(uid, avatarUrl, cache, false);
    }

    public void setOnAvatarListener(OnAvatarListener onAvatarListener) {
        this.onAvatarListener = onAvatarListener;
    }

    public OnAvatarListener getOnAvatarListener() {
        return onAvatarListener;
    }

    public interface OnAvatarListener {
        void onAvatarDownload();
    }

    private OnAvatarListener onAvatarListener;
}


package cn.com.nd.momo.model;

import java.util.ArrayList;
import java.util.LinkedList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.text.TextUtils;
import cn.com.nd.momo.api.util.Log;

/**
 * Helper class to interact with the database that stores the Flickr contacts.
 */
public class DynamicDB extends SQLiteOpenHelper implements BaseColumns {
    private static final String DATABASE_NAME = "dynamic.db";

    private static final int DATABASE_VERSION = 31;

    private static final String TAG = "DynamicDB";

    public static final String T_DYNAMIC = "dynamic";

    // public static final String T_COMMENT = "comment";
    public static final String T_DRAFT = "table_draft";

    public static final String T_MENTION = "T_Mention";

    public static final String T_AGG_MENTION = "T_Mention_Agg";
    
    private static final String I_DYNAMIC_MODIFYTIME = "idx_uptime";
    
    private static final String I_MESSAGE_ID = "message_id_idx";

    private static final String I_READ = "read_idx";

    private static final String I_FEED_SORT = " feed_sort_idx ";

    private final static String C_UID = "uid";

    private final static String C_LASTTIME = "utime";

    private final static String C_REAL_NAME = "real_name";

    private final static String C_AVATAR = "avatar";

    private final static String C_CONTENT = "content";

    private final static String C_JSON = "json";

    private final static String C_GROUP_NAME = "gname";

    private final static String C_GROUP_TYPE = "gtype";

    private final static String C_QID = "qid";

    private final static String C_COMMENT_COUNT = "comment_count";

    private final static String C_COMMENT_LAST = "comment_last";

    private final static String C_LIKE_COUNT = "like_count";

    private final static String C_LIKE_LIST = "like_list";

    private final static String C_LIKED = "liked";

    private final static String C_FAVED = "faved";

    private final static String C_HIDED = "hided";

    private final static String C_CLIENT = "client";

    private final static String C_DATELINE = "date_line";

    // private final static String C_DATE = "date";

    private final static String C_ID = "id";

    private final static String C_RETWEET_OBJID = "retweet_objid";

    private final static String C_RETWEET_TYPEDID = "retweet_typeid";

    private final static String C_GROUP_ID = "group_id";

    private final static String C_CREATE_DATE = "create_date";

    private final static String C_LAST_COMMENT_TIME = "last_comment_time";

    private final static String C_HAS_READ = "has_read";

    private final static String C_FEED_ID = "feed_id";

    // private final static String C_FEED_UID = "feed_uid";

    private final static String C_COMMENT_ID = "comment_id";

    private final static String C_COMMENT = "comment";

    private final static String C_SRC_COMMENT = "src_comment";

    private final static String C_IS_READ = "is_read";

    private final static String C_IS_REPLY = "is_reply";

    private final static String C_KIND = "kind";

    // 由于动态列表和"关于我"列表共用一张动态表，为了不干扰动态列表的时间线
    // 增加ignore字段，ignore等于1的数据将不予显示
    private final static String C_IGNORE = "ignore";

    public static class CommentInfo {
        public String id;

        public long createTime;

        public String feedId;

        public long uid;

        public String realName;

        public String content;

        public String date;

        public String avatar;

        public String client = "";
    }

    public static class DraftInfo {
        public long id;

        public int typeid;

        public String objid = "";

        public long groupid;

        public String content = "";

        public long crateData;

        public String images = "";

        public boolean sending = false;

        public int percent;
    }

    private static final String CREATE_TABLES_DYNAMIC = "CREATE TABLE " + T_DYNAMIC + "("
            + C_ID + " TEXT Primary Key NOT NULL,"
            + C_UID + " INT(11),"
            + C_LASTTIME + " INT(11),"
            + C_REAL_NAME + " VARCHAR(100),"
            + C_AVATAR + " VARCHAR(100),"
            + C_CONTENT + " TEXT,"
            + C_COMMENT_LAST + " TEXT,"
            + C_JSON + " TEXT,"
            + C_GROUP_NAME + " VARCHAR(100),"
            + C_GROUP_ID + " INT(11),"
            + C_GROUP_TYPE + " INT(5),"
            + C_QID + " TEXT,"
            + C_COMMENT_COUNT + " INT(11),"
            + C_LIKE_COUNT + " INT(11),"
            + C_CLIENT + " VARCHAR(50),"
            + C_LIKE_LIST + " VARCHAR(100),"
            + C_LIKED + " INT(1),"
            + C_FAVED + " INT(1),"
            + C_HIDED + " INT(1),"
            + C_DATELINE + " BIGINT, "
            + C_IGNORE + " TINYINT NOT NULL DEFAULT 0" + ");";

    // private static final String CREATE_TABLES_COMMENT = "CREATE TABLE " +
    // T_COMMENT + "("
    // + C_ID + " INT(11) PRIMARY KEY NOT NULL,"
    // + C_FEED_ID + " INT(11) NOT NULL,"
    // + C_CLIENT + " VARCHAR(50),"
    // + C_UID + " INT(11),"
    // + C_CONTENT + " TEXT,"
    // + C_DATE + " TEXT,"
    // + C_REALNAME + " TEXT,"
    // + C_AVATAR + " TEXT" + ");";

    private static final String CREATE_TABLES_DRAFT = "CREATE TABLE " + T_DRAFT + "("
            + C_ID + " INTEGER PRIMARY KEY  AUTOINCREMENT  NOT NULL,"
            + C_CONTENT + " VARCHAR(1024),"
            + C_RETWEET_OBJID + " VARCHAR(32),"
            + C_RETWEET_TYPEDID + " VARCHAR(32),"
            + C_GROUP_ID + " INTEGER,"
            + C_CREATE_DATE + " INTEGER,"
            + C_JSON + " TEXT" + ");";

    private static final String CREATE_TABLE_AGG_MENTION = "CREATE TABLE " + T_AGG_MENTION + " ( "
            + C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + C_FEED_ID + " TEXT NOT NULL, "
            + C_LAST_COMMENT_TIME + " INTEGER NOT NULL DEFAULT 0, "
            + C_HAS_READ + "  INTEGER NOT NULL DEFAULT 0); ";

    private static final String CREATE_IDX_FEED = "CREATE INDEX " + I_FEED_SORT
            + " ON " + T_AGG_MENTION
            + " ( " + C_HAS_READ + " , " + C_LAST_COMMENT_TIME + " );";

    private static final String CREATE_TABLE_MENTION = "CREATE TABLE " + T_MENTION + " ( "
            + C_ID + "  VARCHAR(32) PRIMARY KEY, " // 关于我的唯一标识
            + C_UID + " INTEGER NOT NULL, " // 回复者的id
            + C_GROUP_NAME + " VARCHAR(100),"
            + C_GROUP_ID + " INT(11),"
            + C_AVATAR + " VARCHAR(100),"
            + C_REAL_NAME + " VARCHAR(16) NOT NULL, " // 回复者的名字
            + C_COMMENT_ID + " VARCHAR(16), " // 回复标识id，用于回复这条回复
            + C_COMMENT + " VARCHAR(1024), " // 评论内容
            + C_SRC_COMMENT + "  VARCHAR(1024), " // 原评论内容
            + C_DATELINE + " INTEGER NOT NULL, " // 回复的时间
            + C_IS_READ + " INTEGER NOT NULL DEFAULT 0, " // 是否已读, true,1-读过了
                                                          // ,false, 0-未读
            + C_IS_REPLY + " INTEGER NOT NULL DEFAULT 0, " // 是否已回复,
                                                           // true-回复过了，false-未回复
            + C_KIND + " tinyint NOT NULL DEFAULT 0, " // 关于我的类型
            + C_FEED_ID + " TEXT NOT NULL)"; // 源动态Id

    private static final String CREATE_IDX_MESSAGE_ID = "CREATE INDEX  " + I_MESSAGE_ID
            + " ON " + T_MENTION
            + "(" + C_FEED_ID + ");";

    private static final String CREATE_IDX_DYNAMIC_MODIFYTIME = "CREATE INDEX  " + I_DYNAMIC_MODIFYTIME
            + " ON " + T_DYNAMIC
            + "(" + C_LASTTIME + ");";
    
    private static final String CREATE_IDX_READ = "CREATE INDEX " + I_READ
            + " ON " + T_MENTION
            + " ( " + C_IS_READ + " , " + C_DATELINE + " );";

    private SQLiteDatabase mDB = null;

    private static Context mContext = null;

    private static DynamicDB _mInstance = null;

    public static DynamicDB initInstance(Context context) {
        if (_mInstance == null) {
            mContext = context.getApplicationContext();
            _mInstance = new DynamicDB(mContext);
            _mInstance.openDB();
        }
        return _mInstance;
    }

    public static DynamicDB instance() {
        if (_mInstance == null) {
            _mInstance = new DynamicDB(mContext);
            _mInstance.openDB();
        }
        return _mInstance;
    }

    private DynamicDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createDB(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        dropDB(db);
        onCreate(db);
    }

    public void openDB() {
        if (mDB != null)
            return;
        try {
            mDB = super.getWritableDatabase();
            Log.i(TAG, "openDB" + mDB.toString());
            // test();

        } catch (Exception e) {
            Log.e(TAG, "openDB " + e.toString());
        }
    }

    public void closeDB() {
        Log.i(TAG, "closeDB");
        super.close();
        mDB = null;
    }

    // private void test(){
    // testDraft();
    // }
    public void dropDB(SQLiteDatabase db) {
        Log.i(TAG, "dropDB");
        if (db == null) {
            return;
        }
        // db.execSQL("drop table if exists " + T_COMMENT + ";");
        db.execSQL("drop table if exists " + T_DYNAMIC + ";");
        db.execSQL("drop table if exists " + T_DRAFT + ";");
        db.execSQL("drop table if exists " + T_MENTION + ";");
        db.execSQL("drop table if exists " + T_AGG_MENTION + ";");
    }

    public void createDB(SQLiteDatabase db) {
        try {
            Log.i(TAG, "createDB");
            // db.execSQL(CREATE_TABLES_COMMENT);
            db.execSQL(CREATE_TABLES_DYNAMIC);
            db.execSQL(CREATE_IDX_DYNAMIC_MODIFYTIME);            
            db.execSQL(CREATE_TABLES_DRAFT);
            db.execSQL(CREATE_TABLE_AGG_MENTION);
            db.execSQL(CREATE_TABLE_MENTION);

            db.execSQL(CREATE_IDX_MESSAGE_ID);
            db.execSQL(CREATE_IDX_READ);
            db.execSQL(CREATE_IDX_FEED);
        } catch (SQLException e) {
            Log.e(TAG, "createDB:" + e.toString());
        }
    }

    /**
     * 添加一条动态信息
     * 
     * @param info 动态信息
     * @param isIgnore 获取动态时间线的时候是否忽略此条动态(共用动态表)
     * @return insert id
     */
    public long insertDynamic(DynamicInfo info, boolean isIgnore) {
        final ContentValues values = new ContentValues();

        values.put(C_AVATAR, info.avatar);
        values.put(C_CONTENT, info.text);
        values.put(C_DATELINE, info.createAt);
        values.put(C_LASTTIME, info.modifiedAt);
        values.put(C_GROUP_NAME, info.gname);
        values.put(C_GROUP_TYPE, info.gtype);
        values.put(C_GROUP_ID, info.gid);
        values.put(C_JSON, info.json);
        values.put(C_REAL_NAME, info.realname);
        values.put(C_COMMENT_COUNT, info.commentCount);
        values.put(C_LIKE_COUNT, info.likeCount);
        values.put(C_CLIENT, info.sourceName);
        values.put(C_LIKE_LIST, info.likeList);
        values.put(C_LIKED, info.liked);
        values.put(C_FAVED, info.storaged);
        values.put(C_HIDED, info.hided);
        values.put(C_ID, info.id);
        values.put(C_UID, info.uid);
        values.put(C_COMMENT_LAST, info.commentLast);
        values.put(C_IGNORE, isIgnore ? 1 : 0);

        long result = 0;
        try {
            result = mDB.replace(T_DYNAMIC, null, values);
            Log.i(TAG, "insertDynamic " + result);
        } catch (Exception e) {
            Log.e(TAG, "insertDynamic" + e.toString());
            e.toString();
        }
        return result;
    }

    // delete dynamic
    public long deleteDynamic(DynamicInfo info) {
        return deleteDynamic(info.id);
    }

    // delete dynamic
    public long deleteDynamic(String id) {
        long result = 0;
        try {
            result = mDB.delete(T_DYNAMIC, "(" + C_ID + "=\"" + id + "\")", null);
            Log.i(TAG, "delDynamic " + result);
        } catch (Exception e) {
            Log.e(TAG, "delDynamic" + e.toString());
            e.toString();
        }
        return result;
    }

    // insert dynamic
    // public long insertComment(CommentInfo info) {
    // final ContentValues values = new ContentValues();
    // values.put(C_CLIENT, info.client);
    // values.put(C_CONTENT, info.content);
    // values.put(C_AVATAR, info.avatar);
    // values.put(C_DATE, info.date);
    // values.put(C_REALNAME, info.realName);
    // values.put(C_FEED_ID, info.feedId);
    // values.put(C_UID, info.uid);
    // values.put(C_ID, info.id);
    //
    // // values.put(C_IM, info.im);
    // // values.put(C_ISPRIVATE, info.isPrivate);
    // // values.put(C_UIN, info.uin);
    //
    // long result = 0;
    // try {
    // result = mDB.replace(T_COMMENT, null, values);
    // Log.i(TAG, "insertComment" + result);
    // } catch (Exception e) {
    // Log.e(TAG, "insertComment " + e.toString());
    // e.toString();
    // }
    // return result;
    // }

    // insert draft
    public long insertDraft(DraftInfo info) {
        final ContentValues values = new ContentValues();

        values.put(C_GROUP_ID, info.groupid);
        values.put(C_RETWEET_TYPEDID, info.typeid);
        values.put(C_RETWEET_OBJID, info.objid);

        values.put(C_CONTENT, info.content);
        values.put(C_CREATE_DATE, info.crateData);
        values.put(C_JSON, info.images);

        long result = 0;
        try {
            result = mDB.replace(T_DRAFT, null, values);
            Log.i(TAG, "insertDraft" + result);
        } catch (Exception e) {
            Log.e(TAG, "insertDraft " + e.toString());
            e.toString();
        }
        return result;
    }

    // insert dynamic
    public long deleteDraft(long id) {
        long result = 0;
        try {
            result = mDB.delete(T_DRAFT, "(" + C_ID + "=" + id + ")", null);
            Log.i(TAG, "deleteDraft " + result);
        } catch (Exception e) {
            Log.e(TAG, "deleteDraft " + e.toString());
            e.toString();
        }
        return result;
    }

    public long deleteAllDraft() {
        long result = 0;
        try {
            mDB.execSQL("drop table if exists " + T_DRAFT + ";");
            mDB.execSQL(CREATE_TABLES_DRAFT);

        } catch (Exception e) {
            Log.e(TAG, "deleteDraft " + e.toString());
            e.toString();
        }
        return result;
    }

    public ArrayList<DraftInfo> queryDraft() {
        try {
            Cursor result = null;
            result = mDB.query(T_DRAFT, new String[] {}, null, new String[] {}, "", "",
                    C_CREATE_DATE + " DESC");
            ArrayList<DraftInfo> resultList = new ArrayList<DraftInfo>();
            for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                DraftInfo draftInfo = new DraftInfo();
                draftInfo.id = result.getLong(result.getColumnIndexOrThrow(C_ID));
                draftInfo.objid = result.getString(result.getColumnIndexOrThrow(C_RETWEET_OBJID));
                draftInfo.content = result.getString(result.getColumnIndexOrThrow(C_CONTENT));
                draftInfo.crateData = result.getLong(result.getColumnIndexOrThrow(C_CREATE_DATE));
                draftInfo.typeid = result.getInt(result.getColumnIndexOrThrow(C_RETWEET_TYPEDID));
                draftInfo.groupid = result.getInt(result.getColumnIndexOrThrow(C_GROUP_ID));
                draftInfo.images = result.getString(result.getColumnIndexOrThrow(C_JSON));
                resultList.add(draftInfo);
            }
            result.close();
            return resultList;
        } catch (Exception e) {
            Log.e(TAG, "queryDraft：" + e.toString());
            return null;
        }
    }

    public int getDraftCount() {
        try {
            int count = 0;
            Cursor result = null;
            result = mDB.query(T_DRAFT, new String[] {
                    "count(0)"
            }, null, new String[] {}, "", "", C_ID);
            count = result.getCount();
            result.close();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
            return -1;
        }
    }

    public boolean testDraft() {
        boolean result = false;

        DraftInfo draftInfo = new DraftInfo();
        for (int i = 0; i < 50; i++) {
            deleteDraft(i);
        }
        for (int i = 0; i < 50; i++) {
            insertDraft(draftInfo);
        }

        getDraftCount();
        ArrayList<DraftInfo> draftInfoArray = queryDraft();
        Log.i(TAG, "TEST DRAFT COUNT" + getDraftCount());
        for (DraftInfo dinfo : draftInfoArray) {
            Log.i(TAG, "TEST DRAFT" + dinfo.id);
        }

        return result;
    }

    // 获取
    public ArrayList<DynamicInfo> queryDynamic(long lasttime, String limit) {
        try {
            Cursor result = null;
            // ("where "+C_UTIME+ "<" + utime)
            lasttime = lasttime == -1 ? Long.MAX_VALUE : lasttime;
            String strSelection = C_IGNORE + "=0";
            if (lasttime > 0) {
                strSelection += " and " + C_LASTTIME + "<" + lasttime;
            }
            result = mDB.query(T_DYNAMIC, null,
                    strSelection,
                    null,
                    null,
                    null,
                    C_LASTTIME + " DESC",
                    limit);
            
            ArrayList<DynamicInfo> resultList = new ArrayList<DynamicInfo>();
            for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext()) {
                String json = result.getString(result.getColumnIndexOrThrow(C_JSON));
                DynamicInfo dynamicInfo = new DynamicInfo(json);

                setDynamic(dynamicInfo, result);

                resultList.add(dynamicInfo);
            }
            result.close();
            return resultList;
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
            return null;
        }
    }

    /**
     * 获取单条动态
     * 
     * @param objId 和typeId标识一条动态
     * @param typeId 和objId标识一条动态
     * @return 单条动态
     */
    public DynamicInfo queryDynamic(String id) {
        Cursor result = null;
        DynamicInfo dynamicInfo = null;
        try {
            String selection = C_ID
                    + "=?";
            String[] selectionArgs = new String[] {
                    id
            };

            // query
            result = mDB.query(T_DYNAMIC, null, selection, selectionArgs, null, null, null);
            if (!result.moveToFirst()) {
                return null;
            }

            String json = result.getString(result.getColumnIndexOrThrow(C_JSON));
            dynamicInfo = new DynamicInfo(json);

            setDynamic(dynamicInfo, result);
        } catch (Exception e) {
            Log.e(TAG, "" + e.toString());
        } finally {
            if (result != null && !result.isClosed())
                result.close();
        }
        return dynamicInfo;
    }

    private void setDynamic(DynamicInfo dynamicInfo, Cursor result) {
        dynamicInfo.avatar = result.getString(result.getColumnIndexOrThrow(C_AVATAR));
        dynamicInfo.id = result.getString(result.getColumnIndexOrThrow(C_ID));
        dynamicInfo.uid = result.getLong(result.getColumnIndexOrThrow(C_UID));
        dynamicInfo.modifiedAt = result.getLong(result.getColumnIndexOrThrow(C_LASTTIME));

        dynamicInfo.realname = result.getString(result.getColumnIndexOrThrow(C_REAL_NAME));

        dynamicInfo.text = result.getString(result.getColumnIndexOrThrow(C_CONTENT));
        dynamicInfo.gname = result.getString(result.getColumnIndexOrThrow(C_GROUP_NAME));
        dynamicInfo.gid = result.getInt(result.getColumnIndexOrThrow(C_GROUP_ID));
        dynamicInfo.gtype = result.getInt(result.getColumnIndexOrThrow(C_GROUP_TYPE));
        dynamicInfo.createAt = result.getLong(result.getColumnIndexOrThrow(C_DATELINE));

        dynamicInfo.commentLast = result.getString(result.getColumnIndexOrThrow(C_COMMENT_LAST));
        dynamicInfo.commentCount = result.getInt(result.getColumnIndexOrThrow(C_COMMENT_COUNT));

        dynamicInfo.likeCount = result.getInt(result.getColumnIndexOrThrow(C_LIKE_COUNT));
        dynamicInfo.sourceName = result.getString(result.getColumnIndexOrThrow(C_CLIENT));
        dynamicInfo.likeList = result.getString(result.getColumnIndexOrThrow(C_LIKE_LIST));
        dynamicInfo.liked = result.getInt(result.getColumnIndexOrThrow(C_LIKED));
        dynamicInfo.storaged = result.getInt(result.getColumnIndexOrThrow(C_FAVED));
        dynamicInfo.hided = result.getInt(result.getColumnIndexOrThrow(C_HIDED));
    }

    public LinkedList<MentionInfo> getAllUnreadMention() {
        return getAllUnreadMention("");
    }

    /**
     * 获取未读的关于我的，将exlucdeId这条排除在外
     * 
     * @param excludeId C_ID等于excludeId的，不获取
     * @return 未读的关于我的队列
     */
    public LinkedList<MentionInfo> getAllUnreadMention(String excludeId) {
        Cursor cursor;
        if (TextUtils.isEmpty(excludeId)) {
            cursor = mDB.query(T_MENTION, null, C_IS_READ + " =0", null, null, null, C_DATELINE
                    + " DESC");
        } else {
            cursor = mDB.query(T_MENTION, null, C_IS_READ + " =0 AND " + C_ID + " <> '" + excludeId
                    + "'", null, null, null, C_DATELINE + " DESC");
        }
        LinkedList<MentionInfo> lstMentionInfo = new LinkedList<MentionInfo>();
        if (cursor == null)
            return lstMentionInfo;

        while (cursor.moveToNext()) {
            MentionInfo info = new MentionInfo();
            info.setId(cursor.getString(cursor.getColumnIndex(C_ID)));
            info.setCommentUserId(cursor.getLong(cursor.getColumnIndex(C_UID)));
            info.setCommentUserName(cursor.getString(cursor.getColumnIndex(C_REAL_NAME)));
            info.setCommentUserAvatar(cursor.getString(cursor.getColumnIndex(C_AVATAR)));
            info.setCommentId(cursor.getString(cursor.getColumnIndex(C_COMMENT_ID)));
            info.setComment(cursor.getString(cursor.getColumnIndex(C_COMMENT)));
            info.setSrcComment(cursor.getString(cursor.getColumnIndex(C_SRC_COMMENT)));
            info.setDateline(cursor.getLong(cursor.getColumnIndex(C_DATELINE)));
            info.setRead(cursor.getInt(cursor.getColumnIndex(C_IS_READ)) == 1);
            info.setReply(cursor.getInt(cursor.getColumnIndex(C_IS_REPLY)) == 1);
            info.setKind(cursor.getInt(cursor.getColumnIndex(C_KIND)));
            info.setFeedId(cursor.getString(cursor.getColumnIndex(C_FEED_ID)));
            lstMentionInfo.add(info);
        }

        cursor.close();

        return lstMentionInfo;
    }

    /**
     * 获取 时间戳lastCommentTime之前的10条关于我的动态
     * 
     * @param lastCommentTime 获取这个时间戳之前的关于我的动态
     * @return "关于我的"相对于动态的聚合
     */
    public ArrayList<AggregateMentionInfo> getAggMentionInfo(long lastCommentTime) {
        String sqlFeed = "SELECT a.feed_id, b.client, b.avatar, b.real_name, b.uid, a.last_comment_time, b.content"
                + " from agg_mention a"
                + " left join dynamic b on (a.feed_id=b.id)"
                + " where last_comment_time<? order by has_read, last_comment_time DESC"
                + " limit 20";

        String sqlMention = "select * from mention"
                + " where feed_id=? order by date_line desc limit 1";

        String sqlMentionCount = "select sum(1-is_read) as unread_count from mention"
                + " where feed_id=?";

        ArrayList<AggregateMentionInfo> lstAggMenInfo = new ArrayList<AggregateMentionInfo>();
        Cursor feedCursor = null;
        Cursor mentionCursor = null;
        Cursor mentionCountCursor = null;
        try {
            feedCursor = mDB.rawQuery(sqlFeed, new String[] {
                    String.valueOf(lastCommentTime)
            });
            // 取数据
            while (feedCursor.moveToNext()) {
                AggregateMentionInfo aggMentionInfo = new AggregateMentionInfo();
                ArrayList<MentionInfo> lstMentionInfo = new ArrayList<MentionInfo>();
                aggMentionInfo.setLstInfo(lstMentionInfo);

                aggMentionInfo.setRealName(feedCursor.getString(feedCursor
                        .getColumnIndex(C_REAL_NAME)));
                aggMentionInfo
                        .setFeedUrl(feedCursor.getString(feedCursor.getColumnIndex(C_AVATAR)));
                aggMentionInfo
                        .setFeedId(feedCursor.getString(feedCursor.getColumnIndex(C_FEED_ID)));
                aggMentionInfo.setFeedUid(feedCursor.getLong(feedCursor.getColumnIndex(C_UID)));
                aggMentionInfo.setDateline(feedCursor.getLong(feedCursor
                        .getColumnIndex(C_LAST_COMMENT_TIME)));
                aggMentionInfo
                        .setContent(feedCursor.getString(feedCursor.getColumnIndex(C_CONTENT)));
                aggMentionInfo.setClient(feedCursor.getString(feedCursor.getColumnIndex(C_CLIENT)));

                // 获取一条动态中关于我的未读数量
                mentionCountCursor = mDB.rawQuery(sqlMentionCount,
                        new String[] {
                            aggMentionInfo.getFeedId()
                        });
                if (!mentionCountCursor.moveToFirst()) {
                    Log.e(TAG, "get aggMention's mention count faliure");
                } else {
                    aggMentionInfo.setCount(mentionCountCursor.getInt(
                            mentionCountCursor.getColumnIndex("unread_count")));
                }

                MentionInfo info = new MentionInfo();

                // 获取最新一条关于我
                mentionCursor = mDB.rawQuery(sqlMention,
                        new String[] {
                            aggMentionInfo.getFeedId()
                        });
                if (!mentionCursor.moveToFirst()) {
                    Log.e(TAG, "get aggMention's mention faliure");
                } else {
                    info.setCommentUserId(mentionCursor.getLong(mentionCursor.getColumnIndex(C_UID)));
                    info.setCommentUserName(mentionCursor.getString(mentionCursor
                            .getColumnIndex(C_REAL_NAME)));
                    info.setCommentId(mentionCursor.getString(mentionCursor
                            .getColumnIndex(C_COMMENT_ID)));
                    info.setComment(mentionCursor.getString(mentionCursor.getColumnIndex(C_COMMENT)));
                    info.setSrcComment(mentionCursor.getString(mentionCursor
                            .getColumnIndex(C_SRC_COMMENT)));
                    info.setDateline(mentionCursor.getLong(mentionCursor.getColumnIndex(C_DATELINE)));
                    info.setRead(mentionCursor.getInt(mentionCursor.getColumnIndex(C_IS_READ)) == 1);
                    info.setReply(mentionCursor.getInt(mentionCursor.getColumnIndex(C_IS_REPLY)) == 1);
                    info.setKind(mentionCursor.getInt(mentionCursor.getColumnIndex(C_KIND)));
                }

                info.setFeedId(aggMentionInfo.getFeedId());
                lstMentionInfo.add(info);

                lstAggMenInfo.add(aggMentionInfo);
                if (mentionCursor != null && !mentionCursor.isClosed()) {
                    mentionCursor.close();
                }
                if (mentionCountCursor != null && !mentionCountCursor.isClosed()) {
                    mentionCountCursor.close();
                }
            }
        } catch (SQLException sqlExcp) {
            Log.e(TAG, "GET aggMention's sql failure, Message:" + sqlExcp.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "GET aggMention failure, Message:" + e.getMessage());
        } finally {
            if (feedCursor != null && !feedCursor.isClosed()) {
                feedCursor.close();
            }
            if (mentionCursor != null && !mentionCursor.isClosed()) {
                mentionCursor.close();
            }
            if (mentionCountCursor != null && !mentionCountCursor.isClosed()) {
                mentionCountCursor.close();
            }
        }
        return lstAggMenInfo;
    }

    /**
     * 获取关于我的
     * 
     * @param lastCommentTime 获取的动态的最新的时间线
     * @return 关于我的
     */
    public ArrayList<MentionInfo> getMention(long lastCommentTime) {
        ArrayList<MentionInfo> lstInfo = new ArrayList<MentionInfo>();
        String sqlMention;
        if (lastCommentTime == 0) {
            sqlMention = "select * from " + T_MENTION
                    + " ORDER BY " + C_IS_READ + ", " + C_DATELINE + " DESC LIMIT 20";
        } else {
            sqlMention = "select * from " + T_MENTION
                    + " WHERE " + C_LAST_COMMENT_TIME + "<" + String.valueOf(lastCommentTime)
                    + " ORDER BY " + C_IS_READ + ", " + C_DATELINE + " DESC LIMIT 20";
        }

        Cursor mentionCursor = null;
        try {
            mentionCursor = mDB.rawQuery(sqlMention, null);
            while (mentionCursor.moveToNext()) {
                MentionInfo info = new MentionInfo();
                info.setId(mentionCursor.getString(mentionCursor.getColumnIndex(C_ID)));
                info.setFeedId(mentionCursor.getString(mentionCursor.getColumnIndex(C_FEED_ID)));
                info.setCommentUserId(mentionCursor.getLong(mentionCursor.getColumnIndex(C_UID)));
                info.setCommentUserName(mentionCursor.getString(mentionCursor
                        .getColumnIndex(C_REAL_NAME)));
                info.setCommentUserAvatar(mentionCursor.getString(mentionCursor
                        .getColumnIndex(C_AVATAR)));
                info.setCommentId(mentionCursor.getString(mentionCursor
                        .getColumnIndex(C_COMMENT_ID)));
                info.setComment(mentionCursor.getString(mentionCursor.getColumnIndex(C_COMMENT)));
                info.setSrcComment(mentionCursor.getString(mentionCursor
                        .getColumnIndex(C_SRC_COMMENT)));
                info.setDateline(mentionCursor.getLong(mentionCursor.getColumnIndex(C_DATELINE)));
                info.setRead(mentionCursor.getInt(mentionCursor.getColumnIndex(C_IS_READ)) == 1);
                info.setReply(mentionCursor.getInt(mentionCursor.getColumnIndex(C_IS_REPLY)) == 1);
                info.setKind(mentionCursor.getInt(mentionCursor.getColumnIndex(C_KIND)));

                info.setGroupId(mentionCursor.getLong(mentionCursor.getColumnIndex(C_GROUP_ID)));
                info.setGroupName(mentionCursor.getString(mentionCursor.getColumnIndex(C_GROUP_NAME)));
                
                lstInfo.add(info);
            }

        } catch (SQLException sqlExcp) {
            Log.d(TAG, sqlExcp.getMessage());
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        } finally {
            if (mentionCursor != null && !mentionCursor.isClosed()) {
                mentionCursor.close();
            }
        }

        return lstInfo;
    }

    public boolean dropOrInsertMention(MentionInfo info) {
        Cursor cursor = mDB.query(T_MENTION, null, C_ID + "='" + info.getId() + "'", null, null,
                null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            cursor = null;
            return false;
        }
        addMention(info);
        cursor.close();
        cursor = null;
        return true;
    }

    /**
     * 添加新的关于我到mention表，并且查找agg_mention表中是否存在 和这条mention相关的feed，有则修改未读状态和时间，无则增加
     * 
     * @param info 关于我的数据结构
     * @return
     */
    public long addMention(MentionInfo info) {

        // add to mention table
        ContentValues values = new ContentValues();
        values.put(C_ID, info.getId());
        values.put(C_UID, info.getCommentUserId());
        values.put(C_REAL_NAME, info.getCommentUserName());
        values.put(C_COMMENT_ID, info.getCommentId());
        values.put(C_AVATAR, info.getCommentUserAvatar());
        values.put(C_COMMENT, info.getComment());
        values.put(C_SRC_COMMENT, info.getSrcComment());
        values.put(C_DATELINE, info.getDateline());
        values.put(C_IS_READ, info.isRead());
        values.put(C_IS_REPLY, info.isReply());
        values.put(C_KIND, info.getKind());
        values.put(C_FEED_ID, info.getFeedId());
        values.put(C_GROUP_ID, info.getGroupId());
        values.put(C_GROUP_NAME, info.getGroupName());
        
        long id = mDB.insert(T_MENTION, null, values);

        // reply to agg_mention table
        values.clear();
        Cursor cursor = mDB.query(T_AGG_MENTION,
                new String[] {
                        C_ID, C_LAST_COMMENT_TIME, C_HAS_READ
                },
                C_FEED_ID + "=?",
                new String[] {
                    String.valueOf(info.getFeedId())
                },
                null,
                null,
                null);
        values.put(C_FEED_ID, info.getFeedId());
        if (cursor.moveToFirst()) {
            long rowId = cursor.getLong(cursor.getColumnIndex(C_ID));
            long dateline = cursor.getLong(cursor.getColumnIndex(C_LAST_COMMENT_TIME));
            int isRead = cursor.getInt(cursor.getColumnIndex(C_HAS_READ));
            // 如果新的关于我的评论时间比数据库当前评论时间晚或者状态是未读，就更新agg_mention数据库
            if (info.getDateline() > dateline || !info.isRead()) {
                values.put(C_HAS_READ, info.isRead() ? isRead : 0); // 如果要更新的关于我的是已读的，就用原来的状态去更新(保持不变)
                values.put(C_LAST_COMMENT_TIME,
                        info.getDateline() > dateline ? info.getDateline() : dateline);
                mDB.update(T_AGG_MENTION, values, C_ID + "=?", new String[] {
                        String.valueOf(rowId)
                });
            }
        } else {
            values.put(C_HAS_READ, info.isRead() ? 1 : 0);
            values.put(C_LAST_COMMENT_TIME, info.getDateline());
            mDB.insert(T_AGG_MENTION, null, values);
        }
        cursor.close();
        cursor = null;
        return id;
    }

    /**
     * 设置一条动态内的关于我的状态为已读
     * 
     * @param feedId
     */
    public int setFeedReadState(String feedId) {
        ContentValues values = new ContentValues();
        values.put(C_IS_READ, 1);
        String whereClause = C_FEED_ID + "='" + feedId + "' AND " + C_IS_READ + "= 0";
        int fetchNum = mDB.update(T_MENTION, values, whereClause, null);

        values.clear();
        values.put(C_HAS_READ, 1);
        whereClause = C_FEED_ID + "='" + feedId + "'";
        mDB.update(T_AGG_MENTION, values, whereClause, null);
        return fetchNum;
    }

    /**
     * 设置单条关于我的状态为已读
     * 
     * @param mentionId
     */
    public void setReadState(String mentionId, String feedId) {
        ContentValues values = new ContentValues();
        values.put(C_IS_READ, 1);
        String whereClause = C_ID + "='" + mentionId + "'";
        mDB.update(T_MENTION, values, whereClause, null);

        // agg_mention table
        Cursor cursor = mDB.query(T_MENTION, new String[] {
                "count(*) AS unread_count"
        },
                C_IS_READ + "=0", null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int unReadCount = cursor.getInt(0);
            Log.i(TAG, "un read count:" + unReadCount);
            if (unReadCount == 0) { // 已经没有未读的mention
                values.clear();
                values.put(C_HAS_READ, 1);
                mDB.update(T_AGG_MENTION, values, C_FEED_ID + "='" + feedId + "'", null);
            }
        } else {
            Log.e(TAG, "set single mention read state failure");
        }
    }

    public void setReadState(String mentionId) {
        ContentValues values = new ContentValues();
        values.put(C_IS_READ, 1);
        String whereClause = C_ID + "='" + mentionId + "'";
        mDB.update(T_MENTION, values, whereClause, null);
    }

    public void setAllReadState() {
        ContentValues values = new ContentValues();
        // feed表
        values.put(C_IS_READ, 1);
        mDB.update(T_MENTION, values, null, null);
        // agg 表
        values.clear();
        values.put(C_HAS_READ, 1);
        mDB.update(T_AGG_MENTION, values, null, null);
    }

    public int getUnreadCount() {
        String sqlClause = "SELECT COUNT(*) AS unread_count FROM " + T_MENTION
                + " WHERE " + C_IS_READ + " = 0";
        Cursor cursor = mDB.rawQuery(sqlClause, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndex("unread_count"));
        }
        cursor.close();
        return count;
    }

    // 获取数据库中关于我的数量(总的)
    public int getMentionCount() {
        String sqlClause = "SELECT COUNT(*) AS count FROM " + T_MENTION;
        Cursor cursor = mDB.rawQuery(sqlClause, null);
        int count = 0;
        if (cursor.moveToNext()) {
            count = cursor.getInt(cursor.getColumnIndex("count"));
        }
        cursor.close();
        return count;
    }

    /**
     * 删除关于我的所有数据
     */
    public void delMention() {
        mDB.delete(T_MENTION, null, null);
        mDB.delete(T_AGG_MENTION, null, null);
    }

    public void reset() {
        // dropDB(mDB);
        // createDB(mDB);
        delData();
    }

    private void delData() {
        // mDB.delete(T_COMMENT, null, null);
        mDB.delete(T_DYNAMIC, null, null);
        mDB.delete(T_DRAFT, null, null);
        mDB.delete(T_MENTION, null, null);
        mDB.delete(T_AGG_MENTION, null, null);
    }
}

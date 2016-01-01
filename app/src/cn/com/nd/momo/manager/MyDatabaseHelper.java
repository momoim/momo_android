
package cn.com.nd.momo.manager;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.com.nd.momo.api.util.Log;

public class MyDatabaseHelper {

    private static final String DATABASE_NAME = "momo.db3";

    private static final String TAG = "MyDatabaseHelper";

    private static final int DATABASE_VERSION = 13;

    private static final int DATABASE_VERSION_ADD_RINGTONE_VERSION = 9;

    private static final int DATABASE_VERSION_ADD_CARD_TABLE_VERSION = 10;

    private static final int DATABASE_VERSION_FORCE_RELOGIN_VERSION = 11;

    private static final int DATABASE_VERSION_ADD_ROBOT_VERSION = 12;

    private static SQLiteDatabase db;

    private static Context context;

    private static SQLiteDatabase readDb;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createDatabase(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase sqlitedatabase, int oldVersion, int newVersion) {
            Log.d(TAG, "update database");
            if (oldVersion < DATABASE_VERSION_ADD_RINGTONE_VERSION) {
                context.deleteDatabase(DATABASE_NAME);
            } else if (oldVersion == DATABASE_VERSION_ADD_RINGTONE_VERSION)
                addColumnRingtong(sqlitedatabase);
            if (oldVersion <= DATABASE_VERSION_ADD_CARD_TABLE_VERSION) {
                try {
                    // 名片
                    if (!tabbleIsExist(sqlitedatabase, SQLCreator.TABLE_CARD)) {
                        sqlitedatabase.execSQL(SQLCreator.CARD);
                    }
                    if (!tabbleIsExist(sqlitedatabase, SQLCreator.TABLE_USER_CARD)) {
                        sqlitedatabase.execSQL(SQLCreator.CARD_USER);
                        sqlitedatabase.execSQL(SQLCreator.CARD_USER_MOBILE_IDX);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (oldVersion <= DATABASE_VERSION_FORCE_RELOGIN_VERSION) {
                GlobalUserInfo.forceLogout(context);
                if (tabbleIsExist(sqlitedatabase, SQLCreator.TABLE_CARD)) {
                    addColumnCardValidity(sqlitedatabase);
                }
            }

            if (oldVersion <= DATABASE_VERSION_ADD_ROBOT_VERSION) {
                try {
                    // 应用机器人
                    if (!tabbleIsExist(sqlitedatabase, SQLCreator.TABLE_ROBOT)) {
                        sqlitedatabase.execSQL(SQLCreator.ROBOT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        private void createDatabase(SQLiteDatabase db) {
            // // 通话记录
            // db.execSQL(SQLCreator.CALL_HISTORY);
            // // 同步记录
            // db.execSQL(SQLCreator.SYNC_HISTORY);

            // // 分组
            // db.execSQL(SQLCreator.CATEGORY);
            //
            // // 组成员
            // db.execSQL(SQLCreator.CATEGORY_MEMBER);

            // 联系人
            db.execSQL(SQLCreator.CONTACT);
            db.execSQL(SQLCreator.CONTACT_ID_IDX);
            db.execSQL(SQLCreator.CONTACT_PHONE_CID_IDX);
            db.execSQL(SQLCreator.CONTACT_UID_IDX);
            // db.execSQL(SQLCreator.CONTACT_NAME_PHONETIC_IDX);
            // db.execSQL(SQLCreator.CONTACT_NAME_PHONETIC_KEY_IDX);
            // db.execSQL(SQLCreator.CONTACT_NAME_ABBR_KEY_IDX);
            // db.execSQL(SQLCreator.CONTACT_CREATE_STATE_IDX);
            // db.execSQL(SQLCreator.CONTACT_UPDATE_STATE_IDX);
            // db.execSQL(SQLCreator.CONTACT_DELETE_STATE_IDX);
            // db.execSQL(SQLCreator.CONTACT_DELETE_STATE_NAME_PHONETIC_IDX);
            // db.execSQL(SQLCreator.CONTACT_TRIPLE_NAME_IDX);
            // db.execSQL(SQLCreator.CONTACT_TWIN_IDX1);
            // db.execSQL(SQLCreator.CONTACT_TWIN_IDX2);

            // 联系人数据
            db.execSQL(SQLCreator.DATA);
            db.execSQL(SQLCreator.DATA_CONTACT_ID_IDX);
            db.execSQL(SQLCreator.DATA_CONTACT_ID_PROPERTY_IDX);
            // db.execSQL(SQLCreator.DATA_POOM_RELATED_IDX);
            db.execSQL(SQLCreator.DATA_PROPERTY_IDX);

            // // 群
            // db.execSQL(SQLCreator.GROUP_CONTACT);
            // db.execSQL(SQLCreator.GROUP_CONTACT_AVATAR_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_GROUP_ID_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_NAME_PHONETIC_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_NAME_PHONETIC_KEY_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_NAME_ABBR_KEY_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_CREATE_STATE_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_UPDATE_STATE_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_DELETE_STATE_IDX);
            // db.execSQL(SQLCreator.GROUP_CONTACT_DELETE_STATE_NAME_PHONETIC_IDX);
            //
            // // 群数据
            // db.execSQL(SQLCreator.GROUP_DATA);
            // db.execSQL(SQLCreator.GROUP_DATA_CONTACT_ID_IDX);
            // db.execSQL(SQLCreator.GROUP_DATA_PROPERTY_IDX);
            //
            // // 群头像
            // db.execSQL(SQLCreator.GROUP_IMAGE);
            //
            // // 群消息
            // db.execSQL(SQLCreator.GROUP_INFO);

            // 头像
            db.execSQL(SQLCreator.IMAGE);
            db.execSQL(SQLCreator.IMAGE_CONTACT_ID_IDX);
            // db.execSQL(SQLCreator.IMAGE_UPDATE_STATE_IDX);
            // ol db.execSQL(SQLCreator.IMAGE_DELETE_STATE_IDX);

            // 设置
            db.execSQL(SQLCreator.PROFILE);
            db.execSQL(SQLCreator.PROFILE_KEY_IDX);
            // addColumnRingtong();
            // //活动
            // db.execSQL(SQLCreator.ACTIVITY);
            // db.execSQL(SQLCreator.ACTIVITY_MEMBER);
            // db.execSQL(SQLCreator.ACTIVITY_MEMBER_CREATE_ACTIVITY_ID_IDX);
            // db.execSQL(SQLCreator.ACTIVITY_MEMBER_CREATE_USER_ID_IDX);

            // 名片
            db.execSQL(SQLCreator.CARD);
            db.execSQL(SQLCreator.CARD_USER);
            db.execSQL(SQLCreator.CARD_USER_MOBILE_IDX);

            // 应用机器人
            db.execSQL(SQLCreator.ROBOT);
        }
    }

    /**
     * 判断某张表是否存在
     * 
     * @param tabName 表名
     * @return
     */
    private static boolean tabbleIsExist(SQLiteDatabase sqlitedatabase, String tableName) {
        boolean result = false;
        if (tableName == null || tableName.trim().length() < 1 || sqlitedatabase == null) {
            return false;
        }
        Cursor cursor = null;
        try {
            String sql = "select * from " + tableName.trim();
            cursor = sqlitedatabase.rawQuery(sql, null);
            if (cursor.moveToNext()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static void addColumnRingtong(SQLiteDatabase sqlitedatabase) {
        final String CONTACT_TABLE = "contact";
        final String COLUMN_RINGTONE = "custom_ringtone";
        if (null != sqlitedatabase) {
            Cursor c = null;
            try {
                c = sqlitedatabase.rawQuery("select custom_ringtone from contact limit 1", null);
            } catch (Exception e) {
                sqlitedatabase.execSQL("ALTER TABLE " + CONTACT_TABLE + " ADD " + COLUMN_RINGTONE
                        + " String");
            } finally {
                if (null != c)
                    c.close();
            }

        }

    }

    /**
     * 添加名片表有效期字段
     * 
     * @param sqlitedatabase
     */
    private static void addColumnCardValidity(SQLiteDatabase sqlitedatabase) {
        if (null != sqlitedatabase) {
            Cursor c = null;
            try {
                c = sqlitedatabase.rawQuery("select validity from card limit 1", null);
            } catch (Exception e) {
                e.printStackTrace();
                sqlitedatabase.execSQL("ALTER TABLE card ADD validity INTEGER DEFAULT 0");
            } finally {
                if (null != c)
                    c.close();
            }
        }
    }

    private MyDatabaseHelper() {
    }

    /**
     * 获取一个数据库连接
     * 
     * @return
     */
    public static SQLiteDatabase getInstance() {
        Data data = MyThreadLocal.get();
        if (data == null) {
            data = new Data();
        }
        if (!data.isUseNewConnected) { // 如果无需创建一个新连接，就使用现有的全局数据库连接
            if (MyDatabaseHelper.db == null || !MyDatabaseHelper.db.isOpen()) {
                DatabaseHelper mOpenHelper = new DatabaseHelper(context);
                MyDatabaseHelper.db = mOpenHelper.getWritableDatabase();
            }
            return MyDatabaseHelper.db;
        } else {
            if (data.db == null || !data.db.isOpen()) {
                DatabaseHelper openHelper = new DatabaseHelper(context);
                data.db = openHelper.getWritableDatabase();
                MyThreadLocal.set(data);
            }
            return data.db;
        }
    }

    public static SQLiteDatabase getReadableDatabase() {
        if (readDb == null || !readDb.isOpen()) {
            DatabaseHelper openHelper = new DatabaseHelper(context);
            readDb = openHelper.getReadableDatabase();
        }
        return readDb;
    }

    public static void initDatabase(Context context) {
        MyDatabaseHelper.context = context;
        if (MyDatabaseHelper.db == null || !MyDatabaseHelper.db.isOpen()) {
            DatabaseHelper OpenHelper = new DatabaseHelper(context);
            MyDatabaseHelper.db = OpenHelper.getWritableDatabase();
        }
    }

    public static void set(boolean isUseNewConnected) {
        if (MyThreadLocal.get() == null) {
            Data data = new Data();
            data.isUseNewConnected = isUseNewConnected;
            MyThreadLocal.set(data);
        }

        Data data = MyThreadLocal.get();
        data.isUseNewConnected = isUseNewConnected;
        MyThreadLocal.set(data);
    }

    public static void closeThreadDb() {
        Data data = MyThreadLocal.get();
        if (data != null && data.db != null && data.db.isOpen())
            data.db.close();
    }

    public static void close() {
        if (MyDatabaseHelper.db != null && MyDatabaseHelper.db.isOpen()) {
            MyDatabaseHelper.db.close();
        }
    }

    private static class MyThreadLocal {

        private static ThreadLocal<Data> tLocal = new ThreadLocal<Data>();

        public static void set(Data i) {
            tLocal.set(i);
        }

        public static Data get() {
            return tLocal.get();
        }
    }

    private static class Data {
        public boolean isUseNewConnected = false;

        public SQLiteDatabase db = null;
    }

}

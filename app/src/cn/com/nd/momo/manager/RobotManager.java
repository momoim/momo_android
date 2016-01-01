
package cn.com.nd.momo.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import cn.com.nd.momo.api.MoMoHttpApi;
import cn.com.nd.momo.api.SyncContactApi;
import cn.com.nd.momo.api.exception.MoMoException;
import cn.com.nd.momo.api.parsers.json.RobotParser;
import cn.com.nd.momo.api.sync.SyncManager;
import cn.com.nd.momo.api.types.Robot;
import cn.com.nd.momo.api.util.Log;
import cn.com.nd.momo.api.util.Utils;

public class RobotManager {

    private static final String TAG = "AppManager";

    private static RobotManager sInstance;

    private final String TABLE_ROBOT = "robot";

    private final String TABLE_KEY_ROBOT_ID = "robot_id";

    private final String TABLE_KEY_APP_ID = "app_id";

    private final String TABLE_KEY_SUBSCRIBE = "subscribe";

    private final String TABLE_KEY_ROBOT_INFO = "robot_info";

    public final int ROBOT_SUBSCRIBE = 1;

    public final int ROBOT_NOT_SUBSCRIBE = 0;

    private SQLiteDatabase mDB = null;

    private ArrayList<Robot> mRobotList = new ArrayList<Robot>();

    private Context mContext;

    private Handler mHandler;

    private HashMap<Long, Robot> mRobotCache = new HashMap<Long, Robot>();

    private RobotManager(Context context) {
        mContext = context;
    }

    public static synchronized RobotManager getInstance(Context context) {
        if (null == sInstance) {
            sInstance = new RobotManager(context);
        }
        return sInstance;
    }

    public Handler getmHandler() {
        return mHandler;
    }

    public void setmHandler(Handler mHandler) {
        this.mHandler = mHandler;
    }

    public ArrayList<Robot> getRobotList() {
        Log.d(TAG, "getRobotList");
        if (mRobotList == null || mRobotList.size() < 1) {
            mRobotList = getSubscribeRobotListFromDababase();
        }
        return mRobotList;
    }

    public void setRobotList(ArrayList<Robot> robotList) {
        mRobotList.clear();
        if (robotList != null) {
            mRobotList = new ArrayList<Robot>(Arrays.asList(new Robot[robotList.size()]));
            Collections.copy(mRobotList, robotList);
            this.mRobotCache.clear();
            for (Robot robot : robotList) {
                this.mRobotCache.put(robot.getId(), robot);
            }
        }
    }

    public HashMap<Long, Robot> getRobotListCache() {
        return mRobotCache;
    }

    public ArrayList<Robot> getRobotListFromServer() {
        ArrayList<Robot> robotList = null;
        try {
            robotList = MoMoHttpApi.getSubscriptRobotList();
            if (robotList != null && !robotList.isEmpty()) {
                for (Robot robot : robotList) {
                    JSONObject json = null;
                    try {
                        json = new RobotParser().toJSONObject(robot);
                        updateRobot2Database(robot.getId(), 0,
                                ROBOT_SUBSCRIBE, json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MoMoException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return robotList;
    }

    private Robot parseRobotFromJsonString(String response) {
        Robot robot = null;

        RobotParser psr = new RobotParser();
        JSONObject json = null;
        try {
            json = new JSONObject(response);
            robot = psr.parse(json);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return robot;
    }

    public boolean updateRobot2Database(long robotId, long appId, int subscribe, String info) {
        if (robotId < 1 || info == null || info.length() < 1) {
            return false;
        }
        try {
            mDB = MyDatabaseHelper.getInstance();
            ContentValues values = new ContentValues();
            values.put(TABLE_KEY_ROBOT_ID, robotId);
            values.put(TABLE_KEY_APP_ID, appId);
            values.put(TABLE_KEY_SUBSCRIBE, subscribe);
            values.put(TABLE_KEY_ROBOT_INFO, info);
            mDB.replace(TABLE_ROBOT, null, values);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public boolean cancelSubscribeRobot2Database(long robotId) {
        if (robotId < 1) {
            return false;
        }
        try {
            mDB = MyDatabaseHelper.getInstance();
            ContentValues values = new ContentValues();
            values.put(TABLE_KEY_SUBSCRIBE, ROBOT_NOT_SUBSCRIBE);
            mDB.update(TABLE_ROBOT, values, TABLE_KEY_ROBOT_ID + " = ? ", new String[] {
                    String.valueOf(robotId)
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public boolean deleteRobot(long robotId) {
        if (robotId < 1)
            return false;
        try {
            mDB = MyDatabaseHelper.getInstance();
            mDB.delete(TABLE_ROBOT, "robot_id=?",
                    new String[] {
                        String.valueOf(robotId)
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public ArrayList<Robot> getSubscribeRobotListFromDababase() {
        ArrayList<Robot> robotList = new ArrayList<Robot>();
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select * from '" + TABLE_ROBOT + "' where subscribe = ?;";
            cursor = mDB.rawQuery(sql, new String[] {
                    String.valueOf(ROBOT_SUBSCRIBE)
            });
            while (cursor.moveToNext()) {
                String robotInfo = cursor.getString(cursor.getColumnIndex(TABLE_KEY_ROBOT_INFO));
                Log.i(TAG, "robot json:" + robotInfo);
                Robot robot = parseRobotFromJsonString(robotInfo);
                if (robot != null) {
                    robotList.add(robot);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return robotList;
    }

    public ArrayList<Long> getRobotIdListFromDababase() {
        ArrayList<Long> robotIdList = new ArrayList<Long>();
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select * from '" + TABLE_ROBOT + "';";
            cursor = mDB.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                long robotId = cursor.getLong(cursor.getColumnIndex(TABLE_KEY_ROBOT_ID));
                Log.i(TAG, "robot id:" + robotId);
                if (!robotIdList.contains(robotId)) {
                    robotIdList.add(robotId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return robotIdList;
    }

    public boolean checkIsCacheRobot(long robotId) {
        boolean isRobot = false;
        if (robotId < 1) {
            return isRobot;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select * from '" + TABLE_ROBOT + "' where robot_id = ?";
            String[] selectionArgs = {
                    String.valueOf(robotId)
            };
            cursor = mDB.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                isRobot = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return isRobot;
    }

    public Robot getRobotFromDatabase(long robotId) {
        Robot robot = null;
        if (robotId < 1) {
            return robot;
        }
        Cursor cursor = null;
        try {
            mDB = MyDatabaseHelper.getInstance();
            String sql = "select * from '" + TABLE_ROBOT + "' where robot_id = ?";
            String[] selectionArgs = {
                    String.valueOf(robotId)
            };
            cursor = mDB.rawQuery(sql, selectionArgs);
            if (cursor.moveToNext()) {
                String robotInfo = cursor.getString(cursor.getColumnIndex(TABLE_KEY_ROBOT_INFO));
                Log.i(TAG, "robot json:" + robotInfo);
                robot = parseRobotFromJsonString(robotInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        return robot;
    }

    /**
     * 更新缓存
     */
    public void reloadRobotList() {
        if (null == Utils.getActiveNetWorkName(mContext)) {
            // 没有网络
            Log.i(TAG, "network disable");
            return;
        }
        new Thread() {
            @Override
            public void run() {
                synchronized (this) {
                    ArrayList<Robot> robotList = getRobotListFromServer();
                    ArrayList<Robot> oldRobotList = getSubscribeRobotListFromDababase();
                    if (robotList != null) {
                        for (Robot oldRobot : oldRobotList) {
                            long oldRobotId = oldRobot.getId();
                            boolean find = false;
                            for (Robot robot : robotList) {
                                if (robot.getId() == oldRobotId) {
                                    find = true;
                                    break;
                                }
                                if (!find) {
                                    cancelSubscribeRobot2Database(oldRobotId);
                                }
                            }
                        }
                        setRobotList(robotList);
                        if (null != mHandler && !SyncManager.getInstance().isSyncInProgress()) {
                            Message msg = new Message();
                            msg.what = SyncContactApi.MSG_SYNC_PROCESS_FINISHED;
                            mHandler.sendMessage(msg);
                        }
                    }
                }
            }
        }.start();
    }

    public void cancelSubscribe(long robotId) {
        if (robotId < 1) {
            return;
        }
        if (mRobotList != null) {
            for (Robot robot : mRobotList) {
                if (robot.getId() == robotId) {
                    mRobotList.remove(robot);
                    break;
                }
            }
        }
        cancelSubscribeRobot2Database(robotId);

    }

    public void subscribe(String info) {
        if (info == null || info.length() < 1) {
            return;
        }
        Robot robot = parseRobotFromJsonString(info);
        if (robot == null || robot.getId() < 1) {
            return;
        }
        boolean find = false;
        if (mRobotList != null) {
            for (Robot r : mRobotList) {
                if (r.getId() == robot.getId()) {
                    find = true;
                    break;
                }
            }
        } else {
            mRobotList = new ArrayList<Robot>();
        }
        if (!find) {
            mRobotList.add(robot);
        }
        updateRobot2Database(robot.getId(), 0, ROBOT_SUBSCRIBE, info);
    }
}

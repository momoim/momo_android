
package cn.com.nd.momo.dynamic;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotifyProgress {
    public static String ACTION_PROCESS = "cn.com.nd.action_dynamic_process";

    public static String ACTION_SUCCEED = "cn.com.nd.action_dynamic_succeed";

    public static String ACTION_FAIL = "cn.com.nd.action_dynamic_fail";

    public static String ACTION_COMMENT_SUCCEED = "cn.com.nd.action_comment_succeed";

    public static String ACTION_COMMENT_FAIL = "cn.com.nd.action_comment_fail";

    public static String TAG = "NotifyProgress";

    private static NotificationManager mManager = null;

    private static NotificationManager getManager(Context context) {
        if (mManager == null) {
            mManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    // 成功
    public static void succeed(Context context, int id, RemoteViews view, String tickerText,
            Intent intent) {
        intent.setAction(ACTION_SUCCEED);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.tickerText = tickerText;
        notification.icon = android.R.drawable.stat_notify_error;
        notification.contentView = view;
        notification.contentIntent = pendingIntent;

        getManager(context).notify(id, notification);
    }

    // 失败
    public static void fail(Context context, int id, RemoteViews view, String tickerText,
            Intent intent) {
        intent.setAction(ACTION_FAIL);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.tickerText = tickerText;
        notification.icon = android.R.drawable.stat_notify_error;
        notification.contentView = view;
        notification.contentIntent = pendingIntent;

        getManager(context).notify(id, notification);
    }

    // 进度 progress 0-100
    public static void process(Context context, int id, RemoteViews view, Intent intent) {
        intent.setAction(ACTION_PROCESS);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification();
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        notification.icon = android.R.drawable.stat_sys_upload;
        notification.contentView = view;
        notification.contentIntent = pendingIntent;

        getManager(context).notify(id, notification);
    }
}

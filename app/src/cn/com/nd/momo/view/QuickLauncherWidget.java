
package cn.com.nd.momo.view;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import cn.com.nd.momo.R;
import cn.com.nd.momo.activity.MainActivity;
import cn.com.nd.momo.api.util.Log;

public class QuickLauncherWidget extends AppWidgetProvider {
    /** Called when the activity is first created. */

    public static String ACTION_MENTION = "SHOW_MENTION_IN_MAIN";

    @Override
    public void onDeleted(Context arg0, int[] arg1) {
        super.onDeleted(arg0, arg1);
        Log.i("ADCG", "onDeleted");
    }

    @Override
    public void onDisabled(Context arg0) {
        super.onDisabled(arg0);
        Log.i("ADCG", "onDisabled");
    }

    @Override
    public void onEnabled(Context arg0) {
        super.onEnabled(arg0);
        Log.i("ADCG", "onEnabled");
        updateWidget(arg0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("ADCG", "onReceive");
        super.onReceive(context, intent);
        updateWidget(context);
        // RemoteViews remoteview = new
        // RemoteViews(context.getPackageName(),R.layout.main);
        // if(intent.getAction().equals("android.appwidget.action.message")){
        // remoteview.setTextViewText(R.id.ImageButton01, "llllll");
        // Toast.makeText(context, "test", Toast.LENGTH_LONG).show();
        // updateWidget(context);
        // }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        updateWidget(context);
        // RemoteViews remoteviews = new RemoteViews(context.getPackageName(),
        // R.layout.main);
        // Intent intent = new Intent(context,Widget.class);
        // PendingIntent pi = PendingIntent.getActivity(context, 0, intent,
        // PendingIntent.FLAG_CANCEL_CURRENT);
        // remoteviews.setOnClickPendingIntent(R.id.ImageButton01, pi);

    }

    private void updateWidget(Context context) {
        Log.i("ADCG", "onReceive");

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_quick_launcher);
        Intent inten = new Intent(context, MainActivity.class);
        PendingIntent pin = PendingIntent.getActivity(context.getApplicationContext(), 0, inten, 0);

        rv.setOnClickPendingIntent(R.id.ImageButton01, pin);
        rv.setOnClickPendingIntent(R.id.ImageButton02, pin);
        rv.setOnClickPendingIntent(R.id.ImageButton03, pin);
        rv.setOnClickPendingIntent(R.id.ImageButton04, pin);

        ComponentName component = new ComponentName(context, QuickLauncherWidget.class);
        AppWidgetManager appwidgetmanger = AppWidgetManager.getInstance(context);
        appwidgetmanger.updateAppWidget(component, rv);
    }
}

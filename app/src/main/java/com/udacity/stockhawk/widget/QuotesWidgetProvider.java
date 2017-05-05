package com.udacity.stockhawk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.HistoryActivity;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Stocks widget provider
 */

public class QuotesWidgetProvider extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, getClass()));
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_stocks_list);
            }
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            onUpdate(context, appWidgetManager, appWidgetId);
        }
    }

    private void onUpdate(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        Intent intent = new Intent(context, QuotesWidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_quotes);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        remoteViews.setOnClickPendingIntent(R.id.widget_stocks_title, pendingIntent);

        remoteViews.setRemoteAdapter(R.id.widget_stocks_list, intent);
        remoteViews.setEmptyView(R.id.widget_stocks_list, R.id.empty_widget_list);

        Intent clickIntentTemplate = new Intent(context, HistoryActivity.class);

        PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(clickIntentTemplate)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.widget_stocks_list, clickPendingIntentTemplate);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
}

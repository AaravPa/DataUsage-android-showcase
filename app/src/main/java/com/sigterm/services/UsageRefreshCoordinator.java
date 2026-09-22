package com.sigterm.services;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.RemoteViews;

import com.sigterm.R;
import com.sigterm.activities.DataUsageActivity;
import com.sigterm.domainservices.DataUsageService;
import com.sigterm.domainservices.NotificationService;
import com.sigterm.domainservices.UsageCounterService;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.ProgressbarColors;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.PreferenceUtils;
import com.sigterm.widget.DataUsageCellularUsageProvider;
import com.sigterm.widget.DataUsageWiFiUsageProvider;

import java.util.Locale;

/** Coordinates a single refresh without owning a long-lived Android component. */
public final class UsageRefreshCoordinator {
    private static final String LOG_TAG = "UsageRefreshCoordinator";
    private final Context context;
    private final DatabaseHelper helper;
    private final UsageCounterService counterService;

    public UsageRefreshCoordinator(Context context, DatabaseHelper helper) {
        this.context = context.getApplicationContext();
        this.helper = helper;
        this.counterService = new UsageCounterService(helper);
    }

    public void refresh() {
        new DataUsageService(helper).logUsage(context);
        updateWidgets();
        UsageCounter cellular = counterService.getDefaultCellularBillingCounter();
        UsageCounter wifi = counterService.getDefaultWiFiBillingCounter();
        if (PreferenceUtils.GetPreference(PrefKeys.ENABLE_ALERTS_PREF, false, context)) {
            if (cellular != null) NotificationService.processNotifications(helper, context, cellular, true);
            if (wifi != null) NotificationService.processNotifications(helper, context, wifi, false);
        }
        context.sendBroadcast(new Intent(UsageRefreshWorker.USAGE_UPDATED).setPackage(context.getPackageName()));
    }

    private void updateWidgets() {
        updateWidgetType(new ComponentName(context, DataUsageCellularUsageProvider.class), true);
        updateWidgetType(new ComponentName(context, DataUsageWiFiUsageProvider.class), false);
    }

    private void updateWidgetType(ComponentName provider, boolean cellular) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] widgetIds = manager.getAppWidgetIds(provider);
        UsageCounter counter = cellular ? counterService.getDefaultCellularBillingCounter()
                : counterService.getDefaultWiFiBillingCounter();
        if (counter == null) return;
        String color = counterService.getProgressBarColor(context, counter);
        int layout = ProgressbarColors.YELLOW.equals(color) ? R.layout.widget_usagebar_yellow
                : ProgressbarColors.RED.equals(color) ? R.layout.widget_usagebar_red
                : R.layout.widget_usagebar;
        for (int widgetId : widgetIds) {
            RemoteViews views = new RemoteViews(context.getPackageName(), layout);
            setWidgetView(views, counter, cellular);
            Intent intent = new Intent(context, DataUsageActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_usagebar_layout, pendingIntent);
            manager.updateAppWidget(widgetId, views);
        }
        Log.i(LOG_TAG, "Updated " + widgetIds.length + (cellular ? " cellular" : " Wi-Fi") + " widgets");
    }

    private void setWidgetView(RemoteViews views, UsageCounter counter, boolean cellular) {
        Resources resources = context.getResources();
        views.setTextViewText(R.id.tvPlan, resources.getString(
                cellular ? R.string.Cellular : R.string.WiFi) + " " + resources.getString(R.string.Data));
        if (counter.getQuota() == 0) {
            views.setTextViewText(R.id.tvPlanQuota, resources.getString(R.string.unlimited_plan));
            views.setProgressBar(R.id.widgetPbUsage, 100, 0, false);
        } else {
            views.setTextViewText(R.id.tvPlanQuota, String.format(Locale.getDefault(), resources.getString(R.string.OF) + " %s "
                    + resources.getString(R.string.USED), DataunitUtils.formatData(counter.getQuotaInBytes(), context)));
            float percent = cellular ? counter.getPercentCellularQuotaUsed(helper)
                    : counter.getPercentWiFiQuotaUsed(helper);
            views.setProgressBar(R.id.widgetPbUsage, 100, Math.max(0, Math.min(100, (int) percent)), false);
            views.setTextViewText(R.id.tvPlanPercent, String.format(Locale.getDefault(), " %d%%  %s", (int) percent,
                    resources.getString(R.string.USED)));
        }
        long usage = cellular ? counter.getCellularBytesTotal(helper) : counter.getWifiBytesTotal(helper);
        views.setTextViewText(R.id.tvPlanUsage, DataunitUtils.formatData(usage, context));
    }
}

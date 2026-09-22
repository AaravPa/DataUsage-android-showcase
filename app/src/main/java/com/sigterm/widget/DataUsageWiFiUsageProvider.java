package com.sigterm.widget;



import com.sigterm.services.RefreshScheduler;


import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

public class DataUsageWiFiUsageProvider extends AppWidgetProvider {
	private final String LOG_TAG = getClass().getSimpleName();
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

		RefreshScheduler.refreshNow(context);
		Log.i(LOG_TAG, "Scheduled Wi-Fi widget refresh");

	}


}

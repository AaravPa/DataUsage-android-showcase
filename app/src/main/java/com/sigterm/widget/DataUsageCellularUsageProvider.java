package com.sigterm.widget;


import com.sigterm.R;
import com.sigterm.activities.DataUsageActivity;
import com.sigterm.services.RefreshScheduler;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.util.Log;

public class DataUsageCellularUsageProvider  extends AppWidgetProvider  {
	private final String LOG_TAG = getClass().getSimpleName();
 
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {

		Log.i(LOG_TAG, "DataUsageCellularUsageProvider onUpdate method called");
		RefreshScheduler.refreshNow(context);
		Log.i(LOG_TAG, "Scheduled cellular widget refresh");
		
	}

}

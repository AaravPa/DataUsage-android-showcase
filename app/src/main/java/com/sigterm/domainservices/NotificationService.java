package com.sigterm.domainservices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.Manifest;
import android.os.Build;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.sigterm.R;
import com.sigterm.activities.DataUsageActivity;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.enums.AlertThresholds;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.util.PreferenceUtils;

public class NotificationService {
	private static final int DU_NOTIFICATION_CELL_1 = 1;
	private static final int DU_NOTIFICATION_CELL_2 = 2;
	private static final int DU_NOTIFICATION_CELL_3 = 3;
	private static final int DU_NOTIFICATION_WIFI_1 = 4;
	private static final int DU_NOTIFICATION_WIFI_2 = 5;
	private static final int DU_NOTIFICATION_WIFI_3 = 6;
	
	static final int RESET_NOTIFICATION_CELL = 7;
	static final int RESET_NOTIFICATION_WIFI = 8;
	
	private static final String LOG_TAG = "NotificationService";
	private static final String CHANNEL_ID = "usage-alerts";
	
	//DEBUG Codes start at 100
	public static final int DEBUG1_WIFI = 101;
	public static final int DEBUG2_WIFI = 102;
	public static final int DEBUG1_CELL = 103;
	public static final int DEBUG2_CELL = 104;
	public static final int DEBUG3_CELL = 105;
	public static final int DEBUG3_WIFI = 106;
	
	
	public static void processNotifications(DatabaseHelper helper, Context context, UsageCounter counter, boolean isCellularCounter) {
		float percentUsed = (isCellularCounter) ? counter.getPercentCellularQuotaUsed(helper) : counter.getPercentWiFiQuotaUsed(helper);
		Log.i(LOG_TAG, "processNotifications Plan % Usage is : " + percentUsed);
		if (percentUsed == 0 || counter.getQuotaInBytes() == 0) return; // no need to process notifications if percent used is zero or unlimited quota
		
		String alert1 = PreferenceUtils.GetPreference(PrefKeys.ALERT1_PREF, AlertThresholds.ALERT1_DEFAULT, context);
		String alert2 = PreferenceUtils.GetPreference(PrefKeys.ALERT2_PREF, AlertThresholds.ALERT2_DEFAULT, context);
		String alert3 = PreferenceUtils.GetPreference(PrefKeys.ALERT3_PREF, AlertThresholds.ALERT3_DEFAULT, context);
		
		int alert1Threshold = parseThreshold(alert1, AlertThresholds.ALERT1_DEFAULT);
		int alert2Threshold = parseThreshold(alert2, AlertThresholds.ALERT2_DEFAULT);
		int alert3Threshold = parseThreshold(alert3, AlertThresholds.ALERT3_DEFAULT);
		PreferenceUtils.GetPreference(PrefKeys.WIFI_ALERT1_EXECUTED, false, context);
		
		boolean alert3Executed = (isCellularCounter) ? PreferenceUtils.GetPreference(PrefKeys.CELLULAR_ALERT3_EXECUTED, false, context) :
			PreferenceUtils.GetPreference(PrefKeys.WIFI_ALERT3_EXECUTED, false, context);
		boolean alert2Executed = (isCellularCounter) ? PreferenceUtils.GetPreference(PrefKeys.CELLULAR_ALERT2_EXECUTED, false, context) :
			PreferenceUtils.GetPreference(PrefKeys.WIFI_ALERT2_EXECUTED, false, context);
		boolean alert1Executed = (isCellularCounter) ? PreferenceUtils.GetPreference(PrefKeys.CELLULAR_ALERT1_EXECUTED, false, context) :
			PreferenceUtils.GetPreference(PrefKeys.WIFI_ALERT1_EXECUTED, false, context);
		String dataAlert = context.getResources().getString(R.string.DataAlert);
		String dataUsageAlert = context.getResources().getString(R.string.DataUsageAlert);
		String usageExceeded = context.getResources().getString(R.string.UsageExceeded);
		String type = (isCellularCounter) ? context.getResources().getString(R.string.Cellular) : context.getResources().getString(R.string.WiFi);
		if(percentUsed > alert3Threshold && !alert3Executed){
			String msg = String.format(type+" "+usageExceeded +" %d%s" , (int)percentUsed,"%");
			
			if (isCellularCounter) {
				sendStatusBarNotification(context,DU_NOTIFICATION_CELL_3,dataAlert,dataUsageAlert,msg);
				PreferenceUtils.SetBooleanPreference(PrefKeys.CELLULAR_ALERT3_EXECUTED, true, context);
			}
			else {
				sendStatusBarNotification(context,DU_NOTIFICATION_WIFI_3,dataAlert,dataUsageAlert,msg);
				PreferenceUtils.SetBooleanPreference(PrefKeys.WIFI_ALERT3_EXECUTED, true, context);
			}
		}
		else if(percentUsed > alert2Threshold && !alert2Executed){
			String msg = String.format(type+" "+usageExceeded +" %d%s" , (int)percentUsed,"%");
			
			if (isCellularCounter) {
				sendStatusBarNotification(context,DU_NOTIFICATION_CELL_2,dataAlert,dataUsageAlert,msg);
				PreferenceUtils.SetBooleanPreference(PrefKeys.CELLULAR_ALERT2_EXECUTED, true, context);
			}
			else {
				sendStatusBarNotification(context,DU_NOTIFICATION_WIFI_2,dataAlert,dataUsageAlert,msg);
				PreferenceUtils.SetBooleanPreference(PrefKeys.WIFI_ALERT2_EXECUTED, true, context);
			}

		}
		else if(percentUsed > alert1Threshold && !alert1Executed){
			String msg = String.format(type+" "+usageExceeded +" %d%s" , (int)percentUsed,"%");
			
			if (isCellularCounter) {
				sendStatusBarNotification(context,DU_NOTIFICATION_CELL_1,dataAlert,dataUsageAlert,msg);
				PreferenceUtils.SetBooleanPreference(PrefKeys.CELLULAR_ALERT1_EXECUTED, true, context);
			}
			else {
				sendStatusBarNotification(context,DU_NOTIFICATION_WIFI_1,dataAlert,dataUsageAlert,msg);
				PreferenceUtils.SetBooleanPreference(PrefKeys.WIFI_ALERT1_EXECUTED, true, context);
			}
		}
	}
	
	public static void sendStatusBarNotification(Context context, int notification_id, CharSequence tickerText, CharSequence contentTitle,CharSequence contentText){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
				&& context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
				!= android.content.pm.PackageManager.PERMISSION_GRANTED) {
			return;
		}

		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mNotificationManager.createNotificationChannel(new android.app.NotificationChannel(
					CHANNEL_ID, "Usage alerts", NotificationManager.IMPORTANCE_DEFAULT));
		}

		Intent notificationIntent = new Intent(context, DataUsageActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(context, notification_id, notificationIntent,
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
		NotificationCompat.Builder notification = new NotificationCompat.Builder(context, CHANNEL_ID)
				.setSmallIcon(R.drawable.icon)
				.setTicker(tickerText)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(contentTitle)
				.setContentText(contentText)
				.setContentIntent(contentIntent)
				.setAutoCancel(true)
				.setDefaults(NotificationCompat.DEFAULT_SOUND | NotificationCompat.DEFAULT_VIBRATE)
				.setVibrate(new long[] {0, 100, 200, 300});
		mNotificationManager.notify(notification_id, notification.build());
	}

	private static int parseThreshold(String value, String defaultValue) {
		try {
			int threshold = Integer.parseInt(value == null ? defaultValue : value.trim());
			return Math.max(0, Math.min(100, threshold));
		} catch (NumberFormatException ignored) {
			try {
				return Math.max(0, Math.min(100, Integer.parseInt(defaultValue)));
			} catch (NumberFormatException impossible) {
				return 0;
			}
		}
	}
}

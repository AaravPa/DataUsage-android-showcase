package com.sigterm.util;

import java.util.Date;

import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.QuotaUnit;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import androidx.preference.PreferenceManager;
import android.util.Log;

public class PreferenceUtils {

	public static String GetPrefTextFromStringValue(String value, String[] values,String[] texts){
		int index = ArrayUtils.GetIndexOf(value, values);
		Log.i("PreferenceUtils", "Value: " + value);
		Log.i("PreferenceUtils", "Text: " + texts[index]);
		return texts[index];
	}
	
	public static String GetPrefTextFromIntValue(int value, int[] values,String[] texts){
		int index = ArrayUtils.GetIndexOf(value, values);
		Log.i("PreferenceUtils","Value: " + String.valueOf(value));
		Log.i("PreferenceUtils", "Text: " + texts[index]);
		return texts[index];
	}

	public static String GetPreference(String key, String defaultValue, Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	return sp.getString(key,defaultValue);
	}

	public static Date GetPreference(String key, Date defaultValue, Context context){
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	long longDate = sp.getLong(key,defaultValue.getTime());
    	return new Date(longDate);
	}

	public static boolean GetPreference(String key, boolean defaultValue, Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	return sp.getBoolean(key,defaultValue);
	}
	
	public static void SetBooleanPreference(String key, boolean value, Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor editor = sp.edit();
    	editor.putBoolean(key, value);
		editor.apply();
	}
	public static void SetIntPreference(String key, int value, Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor editor = sp.edit();
    	editor.putInt(key, value);
		editor.apply();
	}
	

	
	public static float GetFloatPreference(String key, float defaultValue, Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	return sp.getFloat(key,defaultValue);
	}
	
	public static int GetIntPreference(String key, int defaultValue, Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	return sp.getInt(key,defaultValue);
	}
	
	public static long GetWiFiQuotaInBytes(Context context){
		float quota = PreferenceUtils.GetFloatPreference(PrefKeys.WIFI_QUOTA, 0, context);
		String quotaUnit = PreferenceUtils.GetPreference(PrefKeys.WIFI_UNIT, QuotaUnit.MB, context);
		if (!Float.isFinite(quota) || quota <= 0 || quotaUnit == null) return 0;
		long quotaInBytes = 0;
		if(quotaUnit.equals(QuotaUnit.KB)){
			quotaInBytes = (long) (1024*quota);
		}
		else if(quotaUnit.equals(QuotaUnit.MB)){
			quotaInBytes = (long) (1024*1024*quota);
		}
		else if(quotaUnit.equals(QuotaUnit.GB)){
			quotaInBytes = (long) (1024*1024*1024*quota);
		}
    	return quotaInBytes;
		
	}
	
	public static long GetCellularQuotaInBytes(Context context){
		float quota = PreferenceUtils.GetFloatPreference(PrefKeys.CELLULAR_QUOTA, 0, context);
		String quotaUnit = PreferenceUtils.GetPreference(PrefKeys.CELLULAR_UNIT, QuotaUnit.MB, context);
		if (!Float.isFinite(quota) || quota <= 0 || quotaUnit == null) return 0;
		long quotaInBytes = 0;
		if(quotaUnit.equals(QuotaUnit.KB)){
			quotaInBytes = (long) (1024*quota);
		}
		else if(quotaUnit.equals(QuotaUnit.MB)){
			quotaInBytes = (long) (1024*1024*quota);
		}
		else if(quotaUnit.equals(QuotaUnit.GB)){
			quotaInBytes = (long) (1024*1024*1024*quota);
		}
    	return quotaInBytes;
		
	}

	public static void SetDatePreference(Context context, String key, Date value) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	Editor editor = sp.edit();
    	editor.putLong(key, value.getTime());
		editor.apply();
	}

	public static Date GetDatePreference(Context context, String key) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
    	long longDate = sp.getLong(key,0L);
    	return new Date(longDate);
	}

}

package com.sigterm.util;

import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.QuotaUnit;

import android.content.Context;
import java.util.Locale;


public class DataunitUtils {
	private final static String LOG_TAG = "DataunitUtils";

	public static String formatData(long bytes, Context context) {
		String formattedData = "0.00 KB";
		float data = 0;
		String dataUnitPref = PreferenceUtils.GetPreference(PrefKeys.DATAUNIT_DISPLAY_PREF, QuotaUnit.AUTO, context);
		//Log.i(LOG_TAG, "Data Unit Pref:" + dataUnitPref);
		if (dataUnitPref.equals(QuotaUnit.MB)) {
			data = (float) bytes / (1024 * 1024);
			//Log.i(LOG_TAG, "Data in MB:" + data);
			formattedData = String.format(Locale.US, "%.02f MB", data);
			//Log.i(LOG_TAG, "Data in MB formatted:" + formattedData);
		} else if (dataUnitPref.equals(QuotaUnit.GB)) {
			data = (float) bytes / (1024 * 1024 * 1024);
			formattedData = String.format(Locale.US, "%.02f GB", data);
		} else if (dataUnitPref.equals(QuotaUnit.KB)) {
			data = (float) bytes / 1024;
			formattedData = String.format(Locale.US, "%.02f KB", data);
		} else if (dataUnitPref.equals(QuotaUnit.AUTO)) {
			// if greater than 1024 MB then show GB
			if (bytes > (1024 * 1024 * 1024)) {
				data = (float) bytes / (1024 * 1024 * 1024);
				formattedData = String.format(Locale.US, "%.02f GB", data);
				//Log.i("DataunitUtils", formattedData);
			}
			// if greater than 1024 KB then MB
			else if (bytes > (1024 * 1024)) {
				data = (float) bytes / (1024 * 1024);
				//Log.i(LOG_TAG, "Data in MB (auto):" + data);
				formattedData = String.format(Locale.US, "%.02f MB", data);
				//Log.i(LOG_TAG, "Data in MB formatted:" + formattedData);
			}
			// if greater than 1024 Bytes then KB
			else if (bytes > 1024) {
				data = (float) bytes / 1024;
				formattedData = String.format(Locale.US, "%.02f KB", data);
				//Log.i("DataunitUtils", formattedData);
			}
		}

		return formattedData;
	}

	public static String formatDataInMB(long bytes) {
		String formattedData = "0.00 KB";
		float data = 0;
		data = (float) bytes / (1024 * 1024);
		//Log.i(LOG_TAG, "Data in MB:" + data);
		formattedData = String.format(Locale.US, "%.02f MB", data);
		//Log.i(LOG_TAG, "Data in MB formatted:" + formattedData);
		return formattedData;
	}
	
	public static float getMBFromBytes(long bytes) {
		
		float data = 0;
		data = (float) bytes / (1024 * 1024);
		//Log.i(LOG_TAG, "Data in MB:" + data);
		return data;
	}
	
	public static long getBytesFromFormattedData(float data, String unit) {
		long dataInBytes = 0;
		if (unit.equals(QuotaUnit.MB)) {
			dataInBytes = (long) (data * 1024 * 1024);
		} else if (unit.equals(QuotaUnit.GB)) {
			dataInBytes = (long) (data * 1024 * 1024 * 1024);
		} else if (unit.equals(QuotaUnit.KB)) {
			dataInBytes = (long) (data * 1024);
		}
		//Log.i(LOG_TAG, "dataInBytes:" + dataInBytes);
		return dataInBytes;
	}
}

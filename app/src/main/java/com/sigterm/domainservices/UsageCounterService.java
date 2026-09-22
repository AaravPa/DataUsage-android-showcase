package com.sigterm.domainservices;

import java.security.Guard;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventObject;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Debug;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.sigterm.R;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.UsageLog;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.ProgressBarPrefs;
import com.sigterm.entities.enums.ProgressbarColors;
import com.sigterm.entities.enums.RepeatInterval;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;
import com.sigterm.util.PreferenceUtils;

public class UsageCounterService {

	private final String LOG_TAG = getClass().getSimpleName();
	private DatabaseHelper _helper;

	public UsageCounterService(DatabaseHelper helper) {
		this._helper = helper;
	}

	public void createCounter(UsageCounter usageCounter) {
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			dao.create(usageCounter);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception createCounter " + e.getStackTrace().toString());
		}

	}

	/*
	 * gets number of completed days in billing period including hour and minute
	 * fraction
	 */
	public float getElapsedDays(UsageCounter usageCounter) {
		if (usageCounter == null) return 0;
		float daysElapsed = 0;
		Date today = DateUtils.getToday();
		if (usageCounter.getStartDateTime() != null && usageCounter.getStartDateTime().before(today)) {
			Date startDate = usageCounter.getStartDateTime();
			daysElapsed = DateUtils.daysBetween(DateUtils.getCalendarFromDate(startDate),
					DateUtils.getCalendarFromDate(today));

			int endHour = DateUtils.getCalendarFromDate(today).get(Calendar.HOUR_OF_DAY);
			int endMinute = DateUtils.getCalendarFromDate(today).get(Calendar.MINUTE);
			float hoursDiff = endHour - 0;
			float minuteDiff = endMinute - 0;
			float fraction = (hoursDiff / 24) + (minuteDiff / (60 * 24));
			// note: removed entire day and added fraction instead
			daysElapsed -= 1;
			daysElapsed += fraction;
			Log.i(LOG_TAG, "Days Elapsed With fraction :" + daysElapsed);
		}
		return Math.max(0, daysElapsed);
	}

	/*
	 * gets number of remaining days in billing period including hour and minute
	 * fraction
	 */
	public float getRemainingDays(UsageCounter usageCounter,Context context) {
		float totalDays,daysRemaining,daysElapsed  = 0;
		daysElapsed = getElapsedDays(usageCounter);
		totalDays = getBillingPeriodDays(usageCounter);
		daysRemaining = totalDays - daysElapsed;
		if(daysRemaining < 0){
			this.checkForEndedDefaultCounters(context);
			totalDays = getBillingPeriodDays(usageCounter);
			daysRemaining = totalDays - daysElapsed;
			daysRemaining = totalDays - daysElapsed;
		}
			
		return daysRemaining;
	}

	public long getBillingPeriodDays(UsageCounter usageCounter) {
		long days = 0;

		if (usageCounter.getEndDateTime() != null && usageCounter.getStartDateTime() != null) {
			days = DateUtils.daysBetween(DateUtils.getCalendarFromDate(usageCounter.getStartDateTime()),
					DateUtils.getCalendarFromDate(usageCounter.getEndDateTime()));
		}
		Log.i(LOG_TAG, "Billing Period Days:" + days);
		return days;
	}

	public long getIdealUsageInBytes(UsageCounter usageCounter) {
		if (usageCounter == null) return 0;
		float daysElapsed = getElapsedDays(usageCounter);
		float quota = usageCounter.getQuotaInBytes();
		long billingPeriodDays = getBillingPeriodDays(usageCounter);
		if (billingPeriodDays <= 0) return 0;
		return (long) ((quota / billingPeriodDays) * daysElapsed);
	}

	public long getTodayWiFiUsage() {
		long wifiToday = 0;
		UsageLogService usageLogService = new UsageLogService(_helper);
		List<UsageLog> logList = usageLogService.getLogByDate(DateUtils.getToday());
		// if (logList.size() > 0) {
		// UsageLog log = logList.get(0);
		// wifiToday = log.getWifiBytesTotal();
		// }
		if (logList.size() > 0) {
			for (UsageLog log : logList)
				wifiToday += log.getWifiBytesTotal();
		}
		return wifiToday;
	}

	public long getRemainingCellularQuota(UsageCounter usageCounter) {
		long quota = usageCounter.getQuotaInBytes();
		long usage = usageCounter.getCellularBytesTotal(_helper);
		return quota - usage;
	}

	public long getRemainingWiFiQuota(UsageCounter usageCounter) {
		long quota = usageCounter.getQuotaInBytes();
		long usage = usageCounter.getWifiBytesTotal(_helper);
		return quota - usage;
	}

	public long getRemainingDailyWiFiQuota(UsageCounter usageCounter,Context context) {
		long remainingQuota = getRemainingWiFiQuota(usageCounter);
		float remainingDays = getRemainingDays(usageCounter, context);
		long remainingDaily = (remainingDays > 1) ? (long) (remainingQuota / remainingDays) : remainingQuota;
		return remainingDaily;
	}

	public long getRemainingDailyCellularQuota(UsageCounter usageCounter,Context context) {
		long remainingQuota = getRemainingCellularQuota(usageCounter);
		float remainingDays = getRemainingDays(usageCounter,context);
		long remainingDaily = (remainingDays > 1) ? (long) (remainingQuota / remainingDays) : remainingQuota;
		return remainingDaily;
	}

	// public long getTodayCellularUsage() {
	// long cellularToday = 0;
	// UsageLogService usageLogService = new UsageLogService(_helper);
	// List<UsageLog> logList =
	// usageLogService.getLogByDate(DateUtils.getToday());
	// if (logList.size() > 0) {
	// UsageLog log = logList.get(0);
	// cellularToday = log.getCellularBytesTotal();
	// }
	// return cellularToday;
	// }
	public long getTodayCellularUsage() {
		long cellularToday = 0;
		UsageLogService usageLogService = new UsageLogService(_helper);
		List<UsageLog> logList = usageLogService.getLogByDate(DateUtils.getToday());
		if (logList.size() > 0) {
			for (UsageLog log : logList)
				cellularToday += log.getCellularBytesTotal();
		}
		return cellularToday;
	}

	public long getIdealMinusActualWiFiUsage(UsageCounter usageCounter) {
		long idealUsage = getIdealUsageInBytes(usageCounter);
		long actualUsage = usageCounter.getWifiBytesTotal(_helper);
		return idealUsage - actualUsage;
	}

	public long getIdealMinusActualCellularUsage(UsageCounter usageCounter) {
		long idealUsage = getIdealUsageInBytes(usageCounter);
		long actualUsage = usageCounter.getCellularBytesTotal(_helper);
		return idealUsage - actualUsage;
	}

	public long getAvgDailyCellularUsage(UsageCounter counter) {
		long currentUsage = counter.getCellularBytesTotal(_helper);
		float elapsedDays = getElapsedDays(counter);
		if (elapsedDays <= 0) return 0;
		long avgUsage = (long) (currentUsage / elapsedDays);
		return avgUsage;
	}

	public long getAvgDailyWiFiUsage(UsageCounter counter) {
		long currentUsage = counter.getWifiBytesTotal(_helper);
		float elapsedDays = getElapsedDays(counter);
		if (elapsedDays <= 0) return 0;
		long avgUsage = (long) (currentUsage / elapsedDays);
		return avgUsage;
	}

	public long getPredictedCellularUsage(UsageCounter counter) {

		long avgUsage = getAvgDailyCellularUsage(counter);
		long billingPeriodDays = getBillingPeriodDays(counter);
		long predictedUsage = avgUsage * billingPeriodDays;
		return predictedUsage;
	}

	public long getPredictedWiFiUsage(UsageCounter counter) {
		long avgUsage = getAvgDailyWiFiUsage(counter);
		long billingPeriodDays = getBillingPeriodDays(counter);
		long predictedUsage = avgUsage * billingPeriodDays;
		return predictedUsage;
	}

	public int updateCounter(UsageCounter usageCounter) {
		int result = 0;
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			result = dao.update(usageCounter);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());

		}
		return result;
	}

	public int updateCounter(String sql, String... args) {
		int result = 0;
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			result = dao.updateRaw(sql, args);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());

		}
		return result;
	}

	public void createOrUpdateCounter(UsageCounter usageCounter) {
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			dao.createOrUpdate(usageCounter);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());
		}
	}

	public void createIfNotExistCounter(UsageCounter usageCounter) {
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			dao.createIfNotExists(usageCounter);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());
		}
	}

	public List<UsageCounter> getAllCounters() {
		List<UsageCounter> counters = null;
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			counters = dao.queryBuilder().where().isNotNull(UsageCounter.STARTDATETIME_COLUMN_NAME).and()
					.isNotNull(UsageCounter.ENDDATETIME_COLUMN_NAME).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());
		}
		return counters;
	}

	public UsageCounter getDefaultCellularBillingCounter() {
		UsageCounter counter = null;
		List<UsageCounter> counters = null;
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			counters = dao.queryBuilder().where().eq(UsageCounter.ISDEFAULT_COLUMN_NAME, true).and()
					.eq(UsageCounter.NETWORKDATATYPE_COLUMN_NAME, NetworkDataType.CELLULAR).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getDefaultCellularBillingCounter " + e.getMessage().toString() );
		}
		if (counters != null && counters.size() > 0)
			counter = counters.get(0);
		return counter;
	}

	public UsageCounter getDefaultWiFiBillingCounter() {
		UsageCounter counter = null;
		List<UsageCounter> counters = null;
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			counters = dao.queryBuilder().where().eq(UsageCounter.ISDEFAULT_COLUMN_NAME, true).and()
					.eq(UsageCounter.NETWORKDATATYPE_COLUMN_NAME, NetworkDataType.WIFI).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getDefaultWiFiBillingCounter " + e.getStackTrace().toString());
		}
		if (counters != null && counters.size() > 0)
			counter = counters.get(0);
		return counter;
	}

	// public void listAllCounters()
	// {
	// Dao<UsageCounter, Integer> dao;
	// try {
	// dao = _helper.getUsageCounterDao();
	// List<UsageCounter> list = dao.queryForAll();
	// for(UsageCounter counter : list){
	// String info =
	// String.format("CounterId:%i,CounterName:%s,Active:%b,StartDate:%t,EndDate:%t,isDefault:%b,NetworkDataType:%s "
	// ,counter.getCounterId(),counter.getCounterName(),counter.isActive(),counter.getStartDateTime(),
	// counter.getEndDateTime(),counter.isDefault(),counter.getNetworkDataType());
	//
	// Log.i("RESET",info);
	// }
	// } catch (SQLException e) {
	// 
	// e.printStackTrace();
	// }
	//
	// }

	public void checkForEndedDefaultCounters(Context context) {
		String tag = "RESET";
		Date today = DateUtils.getToday();
		Log.i(tag, " checkForEndedCounters was called..");
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			// check if there are active counters that has ended and they are
			// recurring.
			List<UsageCounter> endedCounters = dao.queryBuilder().where().eq(UsageCounter.ACTIVE_COLUMN_NAME, true)
					// .and().eq(UsageCounter.ARCHIVED_COLUMN_NAME, false)
					.and().eq(UsageCounter.RECURRING_COLUMN_NAME, true).and()
					.eq(UsageCounter.ISDEFAULT_COLUMN_NAME, true)
					// .and().lt(UsageCounter.ENDDATETIME_COLUMN_NAME, today)
					.query();
			if (endedCounters != null && endedCounters.size() > 0) {
				for (UsageCounter ec : endedCounters) {
					Date endDate = ec.getEndDateTime();
					if (endDate != null && endDate.before(today)) {
						Log.i(tag, "ec Counter ID:" + ec.getCounterId());
						Log.i(tag, "ec Counter Name:" + ec.getCounterName());
						Log.i(tag, "ec isActive:" + ec.isActive());
						// Log.i(tag, "ec isArchived:"+ec.isArchived());
						Log.i(tag, "ec EndDate:" + ec.getEndDateTime());
						Log.i(tag, "ec Today:" + today);
						Log.i(tag, "Called createHistoryCounterAndUpdateEndedCounter with above counter..");

						Calendar startDateCal = DateUtils.getCalendarFromDate(ec.getEndDateTime());
						startDateCal.add(Calendar.MINUTE, 1);
						Date startDate = startDateCal.getTime();
						int repeatFreq = ec.getRepeatFrequency();
						String repeatInterval = ec.getRepeatInterval();
						Date endDateTime = getNewEndDate(repeatFreq, repeatInterval, ec.getEndDateTime());

						Log.i("RESET", " default counter setting start date:" + startDate);
						Log.i("RESET", " default counter setting end date:" + endDateTime);

						Calendar todayBeginCal = DateUtils.getTodayCal();
						todayBeginCal.set(Calendar.HOUR_OF_DAY, 0);
						todayBeginCal.set(Calendar.MINUTE, 0);
						todayBeginCal.set(Calendar.SECOND, 0);
						Date todayBeginDate = todayBeginCal.getTime();
						String dataAlert = context.getResources().getString(R.string.DataAlert);
						String dataUsageAlert = context.getResources().getString(R.string.DataUsageAlert);
						String msg = context.getResources().getString(R.string.DataResetMsg);
						
						updateBillingPeriod(ec.getCounterId(), startDate, endDateTime);
						
						if (ec.isCellularOnlyCounter()) {
							Log.i("RESET", "reset cellular data usage..");
							Date lastCellResetDate = PreferenceUtils.GetDatePreference(context, PrefKeys.LAST_CELL_RESET_DATE);
							if(lastCellResetDate.before(todayBeginDate)){
								archiveCounter(ec);
								PreferenceUtils.SetDatePreference(context,PrefKeys.LAST_CELL_RESET_DATE,DateUtils.getToday());
								this.resetCellularNotificationKeys(context);
								String type = context.getResources().getString(R.string.Cellular);
								NotificationService.sendStatusBarNotification(context,
										NotificationService.RESET_NOTIFICATION_CELL, dataAlert, dataUsageAlert, type + " "
												+ msg);
							}
						} else if (ec.isWiFiOnlyCounter()) {
							Log.i("RESET", "reset wifi data usage..");
							Date lastWiFiResetDate = PreferenceUtils.GetDatePreference(context, PrefKeys.LAST_WIFI_RESET_DATE);
							if(lastWiFiResetDate.before(todayBeginDate)){
								archiveCounter(ec);
								PreferenceUtils.SetDatePreference(context,PrefKeys.LAST_WIFI_RESET_DATE,DateUtils.getToday());
								this.resetWiFiNotificationKeys(context);
								String type = context.getResources().getString(R.string.WiFi);
								NotificationService.sendStatusBarNotification(context,
										NotificationService.RESET_NOTIFICATION_WIFI, dataAlert, dataUsageAlert, type + " "
												+ msg);
							}
						}


//						if (ec.isCellularOnlyCounter()) {
//							// NotificationService.sendStatusBarNotification(
//							// context,
//							// NotificationService.DEBUG2_CELL,
//							// "DEBUG2",
//							// "CELL-AFTER-Counter:" + ec.getCounterName(),
//							// "R:" + result + " ED:" + ec.getEndDateTime() +
//							// " TD:" + today + "AC:"
//							// + ec.isActive()
//							// //+ "AR?:" + ec.isArchived()
//							// );
//							// NotificationService.sendStatusBarNotification(context,
//							// NotificationService.DEBUG3_CELL, "DEBUG3",
//							// "CELL-NEW-Counter:" + archivedCounter
//							// .getCounterName(),
//							// " SD:" + archivedCounter .getStartDateTime() +
//							// " ED:" + archivedCounter .getEndDateTime()
//							// //+ "AC:" + archivedCounter .isActive() + "AR?:"
//							// + archivedCounter .isArchived()
//							// );
//
//						} else if (ec.isWiFiOnlyCounter()) {
////							NotificationService.sendStatusBarNotification(context, NotificationService.DEBUG2_WIFI,
////									"DEBUG2", "WIFI-AFTER-Counter:" + ec.getCounterName(),
////									"R:" + result + " ED:" + ec.getEndDateTime() + " TD:" + today
//							// + "AC:" + ec.isActive() + "AR?:" +
//							// ec.isArchived()
////									);
//							// NotificationService.sendStatusBarNotification(context,
//							// NotificationService.DEBUG3_WIFI, "DEBUG3",
//							// "WIFI-NEW-Counter:" + archivedCounter
//							// .getCounterName(),
//							// " SD:" + archivedCounter .getStartDateTime() +
//							// " ED:" + archivedCounter .getEndDateTime()
//							// //+ "AC:" + archivedCounter .isActive() + "AR?:"
//							// + archivedCounter .isArchived()
//							// );
//
//						}

						// return result;
					}
				}
			}
		} catch (SQLException e) {
			Log.e(tag, "Error Updating usage for active counters " + e.getStackTrace().toString());
		}

	}

	private void updateBillingPeriod(int counterId, Date startDate, Date endDateTime) {
		UsageCounter counter = this.getCounterById(counterId);
		if (counter != null) {
			counter.setStartDateTime(startDate);
			counter.setEndDateTime(endDateTime);
			updateCounter(counter);
		}
	}

//	public void updateUsageForActiveCounters(long cellularBytesRecdToAdd, long cellularBytesSentToAdd,
//			long cellularBytesTotalToAdd, long wifiBytesRecdToAdd, long wifiBytesSentToAdd, long wifiBytesTotalToAdd) {
//
//		Date today = DateUtils.getToday();
//		// get list of active and non archived counters
//
//		Dao<UsageCounter, Integer> dao;
//		try {
//			dao = _helper.getUsageCounterDao();
//
//			List<UsageCounter> counters = dao.queryBuilder().where().eq(UsageCounter.ACTIVE_COLUMN_NAME, true).and()
//			// .eq(UsageCounter.ARCHIVED_COLUMN_NAME, false)
//					.query();
//			//
//			// iterate through counters and add new usage if they are active and
//			// not archived.
//
//			for (UsageCounter counter : counters) {
//
//				// if (counter.getStartDateTime() != null &&
//				// counter.getEndDateTime() != null) {
//				//
//				// Log.i(LOG_TAG, "Today " + today + ", Start DateTime" +
//				// counter.getStartDateTime()
//				// + " End Date Time: " + counter.getEndDateTime());
//				//
//				// if (!today.before(counter.getStartDateTime()) &&
//				// !today.after(counter.getEndDateTime())) {
//				long cellularBytesRecd = counter.getCellularBytesRecd() + cellularBytesRecdToAdd;
//				long cellularBytesSent = counter.getCellularBytesSent() + cellularBytesSentToAdd;
//				long cellularBytesTotal = counter.getCellularBytesTotal() + cellularBytesTotalToAdd;
//				counter.setCellularBytesRecd(cellularBytesRecd);
//				counter.setCellularBytesSent(cellularBytesSent);
//				counter.setCellularBytesTotal(cellularBytesTotal);
//
//				long wifiBytesRecd = counter.getWifiBytesRecd() + wifiBytesRecdToAdd;
//				long wifiBytesSent = counter.getWifiBytesSent() + wifiBytesSentToAdd;
//				long wifiBytesTotal = counter.getWifiBytesTotal() + wifiBytesTotalToAdd;
//				Log.i(LOG_TAG, "wifiBytesTotal Old:" + counter.getWifiBytesTotal());
//				Log.i(LOG_TAG, "wifiBytesTotal New:" + wifiBytesTotal);
//				counter.setWifiBytesRecd(wifiBytesRecd);
//				counter.setWifiBytesSent(wifiBytesSent);
//				counter.setWifiBytesTotal(wifiBytesTotal);
//
//				dao.update(counter);
//				Log.i(LOG_TAG, "Logged Usage for Counter: " + counter.getCounterName() + " Current WiFi Usage: "
//						+ counter.getWifiBytesTotal());
//				// } else {
//				// Log.i(LOG_TAG, "Update not made to counter " +
//				// counter.getCounterName()
//				// + " as today is outside billing cycle");
//				// }
//				// }
//			}
//
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//		}
//
//	}

	/*
	 * Date is the start date to which repeat interval will be added based on
	 * repeat Frequency
	 */
	public Date getNewEndDate(int repeatFreq, String repeatInterval, Date date) {
		if (date == null || repeatFreq <= 0 || repeatInterval == null) return date;
		Calendar endDate = DateUtils.getCalendarFromDate(date);
		if (repeatFreq > 0) {
			if (repeatInterval.equalsIgnoreCase(RepeatInterval.DAYS)) {
				Log.i(LOG_TAG, "adding days:" + repeatFreq);
				endDate.add(Calendar.DAY_OF_MONTH, repeatFreq);
			} else if (repeatInterval.equalsIgnoreCase(RepeatInterval.WEEKS)) {
				Log.i(LOG_TAG, "adding days:" + repeatFreq * 7);
				endDate.add(Calendar.DAY_OF_MONTH, (repeatFreq * 7));
			} else if (repeatInterval.equalsIgnoreCase(RepeatInterval.MONTHS)) {
				Log.i(LOG_TAG, "adding months:" + repeatFreq);
				endDate.add(Calendar.MONTH, repeatFreq);
			}
			// NOTE: reduced one minute so that billing period ends at 11.59 PM
			endDate.add(Calendar.MINUTE, -1);
		}

		return endDate.getTime();
	}

	public void adjustCellularCounter(UsageCounter counterToReset, long bytesSent, long bytesRecd) {
		String tag = "adjustCounter";
		long cellTotalDiff = (bytesRecd +bytesSent) - counterToReset.getCellularBytesTotal(_helper);
		long cellRecdDiff = bytesRecd - counterToReset.getCellularBytesRecd(_helper);
		long cellSentDiff = bytesSent - counterToReset.getCellularBytesSent(_helper);
		Log.i(tag,"cellTotalDiff:"+cellTotalDiff);
		Log.i(tag,"cellRecdDiff:"+cellRecdDiff);
		Log.i(tag,"cellSentDiff:"+cellSentDiff);
		UsageLogService usageLogService = new UsageLogService(_helper);
		UsageLog usageLog = usageLogService.getLastLog();
		if(usageLog != null){
			long interfaceCellularBytesRecd = usageLog.getInterfaceCellularBytesRecd();
			long interfaceCellularBytesSent = usageLog.getInterfaceCellularBytesSent();
			long interfaceCellularBytesTotal = usageLog.getInterfaceCellularBytesTotal();
			long interfaceWifiBytesRecd = usageLog.getInterfaceWifiBytesRecd();
			long interfaceWifiBytesSent = usageLog.getInterfaceWifiBytesSent();
			long interfaceWifiBytesTotal = usageLog.getInterfaceWifiBytesTotal();
			Log.i(tag,"interfaceCellularBytesRecd:"+interfaceCellularBytesRecd);
			Log.i(tag,"interfaceCellularBytesSent:"+interfaceCellularBytesSent);
			Log.i(tag,"interfaceCellularBytesTotal:"+interfaceCellularBytesTotal);
			Log.i(tag,"interfaceWifiBytesRecd:"+interfaceWifiBytesRecd);
			Log.i(tag,"interfaceWifiBytesSent:"+interfaceWifiBytesSent);
			Log.i(tag,"interfaceWifiBytesTotal:"+interfaceWifiBytesTotal);
			UsageLog log = new UsageLog(DateUtils.getToday(),interfaceCellularBytesSent,interfaceCellularBytesRecd,
					interfaceCellularBytesTotal,interfaceWifiBytesSent,interfaceWifiBytesRecd,interfaceWifiBytesTotal,cellSentDiff,cellRecdDiff,cellTotalDiff,0,0,0);
		
			UsageLogService logService = new UsageLogService(_helper);
			logService.create(log);
			Log.i(tag,"cellular adjust log created ");
		}
//		counterToReset.setCellularBytesRecd(bytesRecd);
//		counterToReset.setCellularBytesSent(bytesSent);
//		counterToReset.setCellularBytesTotal(bytesSent + bytesRecd);
//		updateCounter(counterToReset);

		Log.i(tag, "Cellular Counter Adjusted: " + counterToReset.getCounterName());

	}

	public void resetCellularNotificationKeys(Context context) {
		PreferenceUtils.SetBooleanPreference(PrefKeys.CELLULAR_ALERT1_EXECUTED, false, context);
		PreferenceUtils.SetBooleanPreference(PrefKeys.CELLULAR_ALERT2_EXECUTED, false, context);
		PreferenceUtils.SetBooleanPreference(PrefKeys.CELLULAR_ALERT3_EXECUTED, false, context);
	}

	public void resetWiFiNotificationKeys(Context context) {
		PreferenceUtils.SetBooleanPreference(PrefKeys.WIFI_ALERT1_EXECUTED, false, context);
		PreferenceUtils.SetBooleanPreference(PrefKeys.WIFI_ALERT2_EXECUTED, false, context);
		PreferenceUtils.SetBooleanPreference(PrefKeys.WIFI_ALERT3_EXECUTED, false, context);
	}

	public void adjustWiFiCounter(UsageCounter counterToReset, long bytesSent, long bytesRecd) {
		String tag = "adjustCounter";
		long wifiTotalDiff = (bytesRecd +bytesSent) - counterToReset.getWifiBytesTotal(_helper);
		long wifiRecdDiff = bytesRecd - counterToReset.getWifiBytesRecd(_helper);
		long wifiSentDiff = bytesSent - counterToReset.getWifiBytesSent(_helper);
		Log.i(tag,"wifiTotalDiff:"+wifiTotalDiff);
		Log.i(tag,"wifiRecdDiff:"+wifiRecdDiff);
		Log.i(tag,"wifiSentDiff:"+wifiSentDiff);
		UsageLogService usageLogService = new UsageLogService(_helper);
		UsageLog usageLog = usageLogService.getLastLog();
		if(usageLog != null){
			long interfaceCellularBytesRecd = usageLog.getInterfaceCellularBytesRecd();
			long interfaceCellularBytesSent = usageLog.getInterfaceCellularBytesSent();
			long interfaceCellularBytesTotal = usageLog.getInterfaceCellularBytesTotal();
			long interfaceWifiBytesRecd = usageLog.getInterfaceWifiBytesRecd();
			long interfaceWifiBytesSent = usageLog.getInterfaceWifiBytesSent();
			long interfaceWifiBytesTotal = usageLog.getInterfaceWifiBytesTotal();
			Log.i(tag,"interfaceCellularBytesRecd:"+interfaceCellularBytesRecd);
			Log.i(tag,"interfaceCellularBytesSent:"+interfaceCellularBytesSent);
			Log.i(tag,"interfaceCellularBytesTotal:"+interfaceCellularBytesTotal);
			Log.i(tag,"interfaceWifiBytesRecd:"+interfaceWifiBytesRecd);
			Log.i(tag,"interfaceWifiBytesSent:"+interfaceWifiBytesSent);
			Log.i(tag,"interfaceWifiBytesTotal:"+interfaceWifiBytesTotal);
			UsageLog log = new UsageLog(DateUtils.getToday(),interfaceCellularBytesSent,interfaceCellularBytesRecd,
					interfaceCellularBytesTotal,interfaceWifiBytesSent,interfaceWifiBytesRecd,interfaceWifiBytesTotal,0L,0L,0L,wifiSentDiff,wifiRecdDiff,wifiTotalDiff);
			UsageLogService logService = new UsageLogService(_helper);
			logService.create(log);
			Log.i(tag,"wifi adjust log created ");
		}		
//		counterToReset.setWifiBytesRecd(bytesRecd);
//		counterToReset.setWifiBytesSent(bytesSent);
//		counterToReset.setWifiBytesTotal(bytesSent + bytesRecd);
//		updateCounter(counterToReset);
		Log.i(tag, "WiFi Counter Adjusted: " + counterToReset.getCounterName());

	}

	public void resetCellularUsage(Context context, UsageCounter counterToReset) {
		// UsageCounter archivedCounter = counterToReset.copy();
		archiveCounter(counterToReset);
//		counterToReset.setCellularBytesRecd(0);
//		counterToReset.setCellularBytesSent(0);
//		counterToReset.setCellularBytesTotal(0);
//		resetCellularNotificationKeys(context);
//		updateCounter(counterToReset);
		adjustCellularCounter(counterToReset, 0, 0);
		this.resetCellularNotificationKeys(context);
		Log.i(LOG_TAG, "Cellular Counter Reset: " + counterToReset.getCounterName());

	}

	private void archiveCounter(UsageCounter ec) {

		Date endDate = ec.getEndDateTime();
		if (DateUtils.getToday().before(endDate))
			endDate = DateUtils.getToday();
		ArchivedUsageCounter archivedCounter = new ArchivedUsageCounter(ec.getCounterName(), ec.getStartDateTime(),
				endDate, ec.isRecurring(), ec.getRepeatInterval(), ec.getRepeatFrequency(), ec.getDataDirectionType(),
				ec.getNetworkDataType(), ec.getCellularBytesSent(_helper), ec.getCellularBytesRecd(_helper),
				ec.getCellularBytesTotal(_helper), ec.getWifiBytesSent(_helper), ec.getWifiBytesRecd(_helper), ec.getWifiBytesTotal(_helper),
				ec.isEnableAlerts(), ec.getQuota(), ec.getQuotaUnit(), ec.getQuotaInBytes());

		ArchivedCounterService archivedCounterService = new ArchivedCounterService(_helper);
		archivedCounterService.createCounter(archivedCounter);

		// archivedCounter.setCounterName("Archived - " +
		// archivedCounter.getCounterName());
		// archivedCounter.setArchived(true);
		// archivedCounter.setActive(false);
		// archivedCounter.setEndDateTime(DateUtils.getToday());
		// createCounter(archivedCounter);
		Log.i(LOG_TAG, "Created Archived Counter: " + archivedCounter.getCounterName());
	}

	public void resetWiFiUsage(Context context, UsageCounter counterToReset) {
		// UsageCounter archivedCounter = counterToReset.copy();

		archiveCounter(counterToReset);
//		counterToReset.setWifiBytesRecd(0);
//		counterToReset.setWifiBytesSent(0);
//		counterToReset.setWifiBytesTotal(0);
//		updateCounter(counterToReset);
		adjustWiFiCounter(counterToReset, 0, 0);
		resetWiFiNotificationKeys(context);
		Log.i(LOG_TAG, "WiFi Counter Reset: " + counterToReset.getCounterName());
	}

	private int getDayofWeek(String weekStartsOn) {
		int dayofWeek = 1;
		if (weekStartsOn.equals("SUNDAY"))
			dayofWeek = 1;
		else if (weekStartsOn.equals("MONDAY"))
			dayofWeek = 2;
		else if (weekStartsOn.equals("TUESDAY"))
			dayofWeek = 3;
		else if (weekStartsOn.equals("WEDNESDAY"))
			dayofWeek = 4;
		else if (weekStartsOn.equals("THURSDAY"))
			dayofWeek = 5;
		else if (weekStartsOn.equals("FRIDAY"))
			dayofWeek = 6;
		else if (weekStartsOn.equals("SATURDAY"))
			dayofWeek = 7;
		return dayofWeek;
	}

	public long getThisWeekCellularUsage(Context context) {
		String tag = "WeeklyUsage";
		String weekStartsOn = PreferenceUtils.GetPreference(PrefKeys.WEEK_STATS_ON, "SUNDAY", context);
		int dayofWeek = getDayofWeek(weekStartsOn);
		long cellularThisWeek = 0;
		UsageLogService usageLogService = new UsageLogService(_helper);
		cellularThisWeek = usageLogService.getCellularUsageForDateRange(DateUtils.getDateForLastOcrruranceofDay(dayofWeek),
				DateUtils.getToday());
		Log.i(tag,"cellular new method week usage"+cellularThisWeek);
		return cellularThisWeek;
	}

	public long getThisWeekWiFiUsage(Context context) {
		String tag = "WeeklyUsage";
		String weekStartsOn = PreferenceUtils.GetPreference(PrefKeys.WEEK_STATS_ON, "SUNDAY", context);
		int dayofWeek = getDayofWeek(weekStartsOn);
		UsageLogService usageLogService = new UsageLogService(_helper);
		long wifiThisWeek = usageLogService.getWiFiUsageForDateRange(DateUtils.getDateForLastOcrruranceofDay(dayofWeek),
				DateUtils.getToday());
		return wifiThisWeek;
	}

	public float getIdealUsagePercentage(UsageCounter counter) {
		long idealUsage = this.getIdealUsageInBytes(counter);
		long quota = counter.getQuotaInBytes();

		float percentage = (quota > 0) ? (float) ((float) idealUsage / quota * 100) : 0.0f;
		return percentage;
	}

	public String getProgressBarColor(Context context, UsageCounter counter) {

		String yellowPref = PreferenceUtils.GetPreference(PrefKeys.YELLOW_PREF, ProgressBarPrefs.IDEAL, context);
		String redPref = PreferenceUtils.GetPreference(PrefKeys.RED_PREF, ProgressBarPrefs.P95, context);
		float currentUsage = (counter.getNetworkDataType().equals(NetworkDataType.CELLULAR)) ? counter
				.getPercentCellularQuotaUsed(_helper) : counter.getPercentWiFiQuotaUsed(_helper);

		float idealUsage = getIdealUsagePercentage(counter);
		boolean showGreen = true;
		boolean showYellow = false;
		boolean showRed = false;
		if (yellowPref.equals(ProgressBarPrefs.IDEAL)) {
			if (currentUsage > idealUsage) {
				showYellow = true;
				showRed = false;
				showGreen = false;
			}
		} else if (yellowPref.equals(ProgressBarPrefs.P80)) {
			if (currentUsage > 80) {
				showYellow = true;
				showRed = false;
				showGreen = false;
			}
		} else if (yellowPref.equals(ProgressBarPrefs.P90)) {
			if (currentUsage > 90) {
				showYellow = true;
				showRed = false;
				showGreen = false;
			}
		}

		if (redPref.equals(ProgressBarPrefs.P95)) {
			if (currentUsage > 95) {
				showYellow = false;
				showRed = true;
				showGreen = false;
			}
		} else if (redPref.equals(ProgressBarPrefs.P100)) {
			if (currentUsage > 100) {
				showYellow = false;
				showRed = true;
				showGreen = false;
			}
		}
		String color = ProgressbarColors.GREEN;
		if (showGreen)
			color = ProgressbarColors.GREEN;
		else if (showYellow)
			color = ProgressbarColors.YELLOW;
		else if (showRed)
			color = ProgressbarColors.RED;

		return color;
	}

	public UsageCounter getCounterById(int counterId) {
		UsageCounter counter = null;
		List<UsageCounter> counters = null;
		try {
			Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
			counters = dao.queryBuilder().where().eq(UsageCounter.COUNTERID_COLUMN_NAME, counterId).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getCounterById" + e.getStackTrace().toString());
		}
		if (counters != null && counters.size() > 0)
			counter = counters.get(0);
		return counter;
	}

	// public List<UsageCounter> getAllCellularCounters() {
	//
	// List<UsageCounter> counters = null;
	// try {
	// Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
	// counters =
	// dao.queryBuilder().where().isNotNull(UsageCounter.STARTDATETIME_COLUMN_NAME).and()
	// .isNotNull(UsageCounter.ENDDATETIME_COLUMN_NAME).and()
	// .eq(UsageCounter.NETWORKDATATYPE_COLUMN_NAME,
	// NetworkDataType.CELLULAR).query();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// Log.e(LOG_TAG, "Sql Exception updateCounter " +
	// e.getStackTrace().toString());
	// }
	// // Log.i(LOG_TAG,"Cellular Counters count:"+counters.size());
	// return counters;
	//
	// }
	//
	// public List<UsageCounter> getAllWiFiCounters() {
	// List<UsageCounter> counters = null;
	// try {
	// Dao<UsageCounter, Integer> dao = _helper.getUsageCounterDao();
	// counters =
	// dao.queryBuilder().where().isNotNull(UsageCounter.STARTDATETIME_COLUMN_NAME).and()
	// .isNotNull(UsageCounter.ENDDATETIME_COLUMN_NAME).and()
	// .eq(UsageCounter.NETWORKDATATYPE_COLUMN_NAME,
	// NetworkDataType.WIFI).query();
	// } catch (SQLException e) {
	// e.printStackTrace();
	// Log.e(LOG_TAG, "Sql Exception updateCounter " +
	// e.getStackTrace().toString());
	// }
	// return counters;
	// }
}

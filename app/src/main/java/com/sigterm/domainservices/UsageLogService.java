package com.sigterm.domainservices;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.UsageLog;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.util.DateUtils;

public class UsageLogService {

	private final String LOG_TAG = getClass().getSimpleName();
	private DatabaseHelper _helper;

	public UsageLogService(DatabaseHelper helper) {
		this._helper = helper;
	}

	public void update(UsageLog usageLog) {
		try {
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			dao.update(usageLog);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateUsageLog " + e.getStackTrace().toString());
		}

	}
	
	public void deleteLogsPriorToDate(Date newDate){
		try {
			Calendar calendar = Calendar.getInstance();
//			Date today = calendar.getTime();
			Log.i("delete","date to delete upto: "+newDate);
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			
			List<UsageLog> usageLog = dao.queryBuilder().where()
					.le(UsageLog.LOGDATE_COLUMN_NAME, newDate).query();
			Log.i("delete","recoreds found for yesterday"+usageLog.size());
			DeleteBuilder<UsageLog, Integer> deleteBuilder =
					dao.deleteBuilder();
			deleteBuilder.where()
					.le(UsageLog.LOGDATE_COLUMN_NAME, newDate);
			int count = deleteBuilder.delete();
			Log.i("delete","deleted recoreds :"+ count);
			
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateUsageLog " + e.getStackTrace().toString());
		}		
	}
	
	public void create(UsageLog usageLog) {
		try {
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			dao.create(usageLog);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateUsageLog " + e.getStackTrace().toString());
		}

	}

	public List<UsageLog> getLogByDate(Date startDate, Date endDate) {
		List<UsageLog> usageLog = null;
		try {
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			Log.i(LOG_TAG,
					"Querying for record between " + startDate.toString() + " and end date : " + endDate.toString());
			usageLog = dao.queryBuilder().where().between(UsageLog.LOGDATE_COLUMN_NAME, startDate, endDate).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getLogByDate " + e.getStackTrace().toString());
		}
		return usageLog == null ? Collections.emptyList() : usageLog;
	}

	public List<UsageLog> getLogByDate(Date date) {
		List<UsageLog> usageLog = null;
		try {
			Log.i(LOG_TAG, "Date Received:" + date.toString());
			Calendar start = Calendar.getInstance();
			start.setTime(date);
			start.set(Calendar.HOUR_OF_DAY, 0);
			start.set(Calendar.MINUTE, 0);
			start.set(Calendar.SECOND, 0);
			start.set(Calendar.MILLISECOND, 0);
			Calendar end = (Calendar) start.clone();
			end.set(Calendar.HOUR_OF_DAY, 23);
			end.set(Calendar.MINUTE, 59);
			end.set(Calendar.SECOND, 59);
			end.set(Calendar.MILLISECOND, 999);
			Date dateStart = start.getTime();
			Date dateEnd = end.getTime();

			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			Log.i(LOG_TAG,
					"Querying for record between " + dateStart.toString() + " and end date : " + dateEnd.toString());
			usageLog = dao.queryBuilder().where().between(UsageLog.LOGDATE_COLUMN_NAME, dateStart, dateEnd).query();

		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getLogByDate " + e.getStackTrace().toString());
		}
		return usageLog == null ? Collections.emptyList() : usageLog;
	}

	public List<UsageLog> getAllUsageLogs() {
		List<UsageLog> list = null;
		try {
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			list = dao.queryForAll();

		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getting all  data usage logs" + e.getStackTrace().toString());
		}
		return list == null ? Collections.emptyList() : list;
	}

	public UsageLog getLastLog() {
		List<UsageLog> logs = null;
		try {
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();
			logs = dao.queryBuilder().orderBy(UsageLog.LOGID_COLUMN_NAME, false).limit(1L).query();

		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getting last  data usage log" + e.getStackTrace().toString());
		}
		UsageLog log = null;
		if (logs != null && !logs.isEmpty())
			log = logs.get(0);
		return log;
	}

	public List<UsageLog> getLogByCounter(UsageCounter counter) {
		Log.i(LOG_TAG,
				"getLogByCounter StartDate: " + counter.getStartDateTime() + " EndDate: " + counter.getEndDateTime());
		return getLogByDate(counter.getStartDateTime(), counter.getEndDateTime());
	}

	public UsageLog getCumulativeLogByDate(Date date) {
		//List<UsageLog> logs = getLogByDate(date);
	 
			long cellsent = getTotalBytes(UsageLog.CELLULARBYTESSENT_COLUMN_NAME,date);
			long cellrecd =getTotalBytes(UsageLog.CELLULARBYTESRECD_COLUMN_NAME,date);
			long celltotal = getTotalBytes(UsageLog.CELLULARBYTESTOTAL_COLUMN_NAME,date);
			long wifisent = getTotalBytes(UsageLog.WIFIBYTESSENT_COLUMN_NAME,date);
			long wifirecd =getTotalBytes(UsageLog.WIFIBYTESRECD_COLUMN_NAME,date);
			long wifitotal = getTotalBytes(UsageLog.WIFIBYTESTOTAL_COLUMN_NAME,date);
			UsageLog cumLog =  new UsageLog(date, 0, 0, 0, 0, 0, 0, cellsent, cellrecd, celltotal, wifisent, wifirecd, wifitotal);
		 
			
//		if (logs != null && logs.size() > 0) {
//			for (UsageLog log : logs) {
//				cumLog.setCellularBytesRecd(cumLog.getCellularBytesRecd() + log.getCellularBytesRecd());
//				cumLog.setCellularBytesSent(cumLog.getCellularBytesSent() + log.getCellularBytesSent());
//				cumLog.setCellularBytesTotal(cumLog.getCellularBytesTotal() + log.getCellularBytesTotal());
//				cumLog.setWifiBytesRecd(cumLog.getWifiBytesRecd() + log.getWifiBytesRecd());
//				cumLog.setWifiBytesSent(cumLog.getWifiBytesSent() + log.getWifiBytesSent());
//				cumLog.setWifiBytesTotal(cumLog.getWifiBytesTotal() + log.getWifiBytesTotal());
//			}
//		}
		return cumLog;
	}

	public long getCellularBytesSent(Date startDateTime, Date endDateTime) {
		return getTotalBytes(UsageLog.CELLULARBYTESSENT_COLUMN_NAME,startDateTime,endDateTime);
	}
	public long getCellularBytesReceived(Date startDateTime, Date endDateTime) {
		return getTotalBytes(UsageLog.CELLULARBYTESRECD_COLUMN_NAME,startDateTime,endDateTime);	}

	public long getCellularBytesTotal(Date startDateTime, Date endDateTime) {
		return getTotalBytes(UsageLog.CELLULARBYTESTOTAL_COLUMN_NAME,startDateTime,endDateTime);
	}
	public long getWiFiBytesSent(Date startDateTime, Date endDateTime) {
		return getTotalBytes(UsageLog.WIFIBYTESSENT_COLUMN_NAME,startDateTime,endDateTime);
	}
	public long getWiFiBytesReceived(Date startDateTime, Date endDateTime) {
		return getTotalBytes(UsageLog.WIFIBYTESRECD_COLUMN_NAME,startDateTime,endDateTime);
	}

	public long getWiFiBytesTotal(Date startDateTime, Date endDateTime) 
	{
		return getTotalBytes(UsageLog.WIFIBYTESTOTAL_COLUMN_NAME,startDateTime,endDateTime);
	}
	
	private long getTotalBytes(String columnName,Date date){
		String tag = "getTotalBytes";
		long result = 0;
		Log.i(tag,"Date:"+date);
		
		try {
			
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();	
			String formattedDate = DateUtils.getDateInYYYY_MM_DD_Format(date);
			String query = "select sum("+columnName+") from UsageLogs where DATE(logDate) = DATE('"+ formattedDate+"') ";
			Log.i(tag,"query:"+query);
			GenericRawResults<Object[]> rawResults = dao.queryRaw(query,new DataType[] { DataType.LONG});
			List<Object[]> results = rawResults.getResults();
			Object[] resultArray = results.get(0);
			
			if(resultArray !=  null && resultArray.length > 0){
				
				if(resultArray[0] != null){
					result = (Long) (resultArray[0]);	
					Log.i(tag,"Column:"+columnName+" - Result: "+result);
				}
				else{
					Log.i(tag,"Column:"+columnName+" - Result null");
				}
			}
			Log.i(tag,"Result:"+result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private long getTotalBytes(String columnName,Date startDateTime, Date endDateTime){
		String tag = "getTotalBytes";
		long result = 0;
		if(startDateTime == null) startDateTime = DateUtils.getStartDateOfCurrentMonth();
		if(endDateTime == null) endDateTime = DateUtils.getToday();
		Log.i(tag,"StartDate:"+startDateTime);
		Log.i(tag,"EndDate:"+endDateTime);
		try {
			
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();	
			String formattedDate = DateUtils.getDateInYYYY_MM_DD_Format(startDateTime);
			String formattedEndDate = DateUtils.getDateInYYYY_MM_DD_Format(endDateTime);
			String query = "select sum("+columnName+") from UsageLogs where DATE(logDate) BETWEEN DATE('"+ formattedDate+"') AND DATE('"+ formattedEndDate +"') ";
			Log.i(tag,"query:"+query);
			GenericRawResults<Object[]> rawResults = dao.queryRaw(query,new DataType[] { DataType.LONG});
			List<Object[]> results = rawResults.getResults();
			Object[] resultArray = results.get(0);
			
			if(resultArray !=  null && resultArray.length > 0){
				
				if(resultArray[0] != null){
					result = (Long) (resultArray[0]);	
					Log.i(tag,"Column:"+columnName+" - Result: "+result);
				}
				else{
					Log.i(tag,"Column:"+columnName+" - Result null");
				}
			}
			Log.i(tag,"Result:"+result);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	public ArrayList<UsageLog> getCumulativeLogByDateRange(Calendar startDateCal, Calendar endDateCal) {
		ArrayList<UsageLog> usageLogs = new ArrayList<UsageLog>();
		Long cellsent = null;
		Long cellrecd = null;
		Long celltotal = null;
		Long wifisent = null;
		Long wifirecd = null;
		Long wifitotal = null;
		String tag = "getCumulativeLogByDateRange";
		long result = 0;
//		if(startDateTime == null) startDateTime = DateUtils.getStartDateOfCurrentMonth();
//		if(endDateTime == null) endDateTime = DateUtils.getToday();
//		Log.i(tag,"StartDate:"+startDateTime);
//		Log.i(tag,"EndDate:"+endDateTime);
		try {
			
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();	
			String formattedDate = DateUtils.getDateInYYYY_MM_DD_Format(startDateCal.getTime());
			String formattedEndDate = DateUtils.getDateInYYYY_MM_DD_Format(endDateCal.getTime());
			String query = "select sum("+UsageLog.CELLULARBYTESSENT_COLUMN_NAME+") as cellSent, " +
					"sum("+UsageLog.CELLULARBYTESSENT_COLUMN_NAME+") as cellSent, " +
					"sum("+UsageLog.CELLULARBYTESRECD_COLUMN_NAME+") as cellrecd, " +
					"sum("+UsageLog.CELLULARBYTESTOTAL_COLUMN_NAME+") as celltotal, " +
					"sum("+UsageLog.WIFIBYTESSENT_COLUMN_NAME+") as wifisent, " +
					"sum("+UsageLog.WIFIBYTESRECD_COLUMN_NAME+") as wifirecd, " +
					"sum("+UsageLog.WIFIBYTESTOTAL_COLUMN_NAME+") as wifitotal," +
					"logDate  " +
					"from UsageLogs where DATE(logDate) BETWEEN DATE('"+ formattedDate+"') AND DATE('"+ formattedEndDate +"') " +
					"GROUP BY DATE(logDate)";
			Log.i(tag,"query:"+query);
			GenericRawResults<Object[]> rawResults = dao.queryRaw(query,new DataType[] { DataType.LONG});
			List<Object[]> results = rawResults.getResults();
			Log.i(tag,"rows returned:"+results.size());
			for(Object[] resultArray : results)
			{
				if(resultArray !=  null && resultArray.length > 0){
					
//					Log.i(tag,"cellsent:"+resultArray[1]);
//					Log.i(tag,"cellrecd:"+resultArray[2]);
//					Log.i(tag,"celltotal:"+resultArray[3]);
//					Log.i(tag,"wifisent:"+resultArray[4]);
//					Log.i(tag,"wifirecd:"+resultArray[5]);
//					Log.i(tag,"wifitotal:"+resultArray[6]);
//					Log.i(tag,"date:"+resultArray[7]);
					
					String sCellsent = resultArray[1].toString();
					String sCellrecd = resultArray[2].toString();
					String sCelltotal = resultArray[3].toString();
					String sWifisent = resultArray[4].toString();
					String sWifirecd = resultArray[5].toString();
					String sWifitotal = resultArray[6].toString();
					String sDate = resultArray[7].toString();
					
					cellsent = Long.parseLong(sCellsent);
					cellrecd = Long.parseLong(sCellrecd);
					celltotal = Long.parseLong(sCelltotal);
					wifisent = Long.parseLong(sWifisent);
					wifirecd = Long.parseLong(sWifirecd);
					wifitotal = Long.parseLong(sWifitotal);
					Log.i(tag,"date"+sDate);
					Date date = null;
					try{
					 date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS",Locale.getDefault()).parse(sDate);
					}
					catch(Exception ex){
						Log.e(tag,ex.getStackTrace().toString());
					}
					
					UsageLog cumLog =  new UsageLog(date, 0, 0, 0, 0, 0, 0, cellsent, cellrecd, celltotal, wifisent, wifirecd, wifitotal);
					usageLogs.add(cumLog);
				}
				Log.i(tag,"Result:"+result);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return usageLogs;
		
	}

	public long getCellularUsageForDateRange(Date startDate, Date endDate) {
		return getUsageForDateRange(UsageLog.CELLULARBYTESTOTAL_COLUMN_NAME,startDate,endDate);
	}
	public long getWiFiUsageForDateRange(Date startDate, Date endDate) {
		return getUsageForDateRange(UsageLog.WIFIBYTESTOTAL_COLUMN_NAME,startDate,endDate);
	}
	private long getUsageForDateRange(String columnName, Date startDate, Date endDate)
	{
		Long celltotal = null;
		String tag = "WeeklyUsage";
		
		try {
			Dao<UsageLog, Integer> dao = _helper.getUsageLogDao();	
			String formattedDate = DateUtils.getDateInYYYY_MM_DD_Format(startDate);
			String formattedEndDate = DateUtils.getDateInYYYY_MM_DD_Format(endDate);
			String query = "select sum("+columnName+") as celltotal " +
					"from UsageLogs where DATE(logDate) BETWEEN DATE('"+ formattedDate+"') AND DATE('"+ formattedEndDate +"') ";
			Log.i(tag,"query:"+query);
			GenericRawResults<Object[]> rawResults = dao.queryRaw(query,new DataType[] { DataType.LONG});
			List<Object[]> results = rawResults.getResults();
			if(results != null && results.size() > 0 )
			{
				Log.i(tag,"rows returned:"+results.size());
				Object[] resultArray = results.get(0);
				if(resultArray !=  null && resultArray.length > 0){
					String sCelltotal = resultArray[0].toString();
					celltotal = Long.parseLong(sCelltotal);
					Log.i(tag,columnName+" total for week"+celltotal);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return celltotal;
	}
}

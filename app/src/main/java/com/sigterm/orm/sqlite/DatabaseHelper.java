package com.sigterm.orm.sqlite;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.sigterm.domainservices.NetworkServices;
import com.sigterm.entities.Alert;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.UsageLog;
import com.sigterm.entities.enums.DataDirectionType;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.entities.enums.QuotaUnit;
import com.sigterm.entities.enums.RepeatInterval;


/**
 * Database helper class used to manage the creation and upgrading of your database. This class also usually provides
 * the DAOs used by the other classes.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {

	// name of the database file for your application -- change to something appropriate for your app
	private static final String DATABASE_NAME = "datausage.db";
	// any time you make changes to your database objects, you may have to increase the database version
	private static final int DATABASE_VERSION = 2;
	private final String LOG_TAG = getClass().getSimpleName();
	// the DAO object we use to access the Alerts table
	private Dao<Alert, Integer> alertDao = null;	
	// the DAO object we use to access the UsageCounters table
	private Dao<UsageCounter, Integer> usageCounterDao = null;
	// the DAO object we use to access the UsageLog table
	private Dao<UsageLog, Integer> usageLogDao = null;
	private Dao<ArchivedUsageCounter,Integer> archivedUsageCounterDao = null;
	
	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * This is called when the database is first created. Usually you should call createTable statements here to create
	 * the tables that will store your data.
	 */
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		try {
			
			TableUtils.createTable(connectionSource, Alert.class);
			TableUtils.createTable(connectionSource, UsageCounter.class);
			TableUtils.createTable(connectionSource, UsageLog.class);
			TableUtils.createTable(connectionSource, ArchivedUsageCounter.class);
			db.execSQL("CREATE INDEX IF NOT EXISTS idx_usage_logs_date ON UsageLogs(logDate)");
			
			Log.i(LOG_TAG, "Created Tables ");
			Date today  = Calendar.getInstance().getTime();
			UsageLog newUsageLog = new UsageLog(today,NetworkServices.getCellularBytesSent(),NetworkServices.getCellularBytesReceived(),
					NetworkServices.getCellularBytesTotal(),NetworkServices.getWiFiBytesSent(),NetworkServices.getWiFiBytesReceived(),NetworkServices.getWiFiBytesTotal(),
					0,0,0,0,0,0);
			Dao<UsageLog, Integer> dao = getUsageLogDao();
			dao.create(newUsageLog);
			Log.i(LOG_TAG, "Created First Log Entry to initialize log table.. entry is for date - " + today.toString());
			
			// also create a sample billing cycle for testing
//			Calendar cal = Calendar.getInstance();
//			cal.set(Calendar.DAY_OF_MONTH,1);
//			cal.set(Calendar.HOUR, 0);
//			cal.set(Calendar.MINUTE,0);
//			cal.set(Calendar.SECOND,0);
//			Date billingStartDate = cal.getTime();
//			cal.add(Calendar.MONTH, 1);
//			Date billingEndDate = cal.getTime();
//			
//			Log.i(LOG_TAG,"Billing date is : " + billingStartDate.toString());
			Date billingStartDate = null;
			Date billingEndDate = null;
			Dao<UsageCounter,Integer> usageCounterDao = getUsageCounterDao();
			UsageCounter cellularCounter = new UsageCounter(UsageCounter.DEFAULT_CELLULAR_BILLING_COUNTER_NAME,billingStartDate,billingEndDate,true,RepeatInterval.MONTHS,1,
					DataDirectionType.BOTH,NetworkDataType.CELLULAR,true,0,QuotaUnit.MB,0L,true,true);
			usageCounterDao.create(cellularCounter);
//			Log.i(LOG_TAG, "Created Sample Cellular Billing cycle.. With Start Date - " + billingStartDate.toString());
//			Log.i(LOG_TAG, "And With End Date - " + billingEndDate.toString());
			
			UsageCounter wifiCounter = new UsageCounter(UsageCounter.DEFAULT_WIFI_BILLING_COUNTER_NAME,billingStartDate,billingEndDate,true,RepeatInterval.MONTHS,1,
					DataDirectionType.BOTH,NetworkDataType.WIFI,true,0,QuotaUnit.MB,0L,true,true);
			 
			usageCounterDao.create(wifiCounter);
			
//			Log.i(LOG_TAG, "Created Sample WiFi Billing cycle.. With Start Date - " + billingStartDate.toString());
//			Log.i(LOG_TAG, "And With End Date - " + billingEndDate.toString());
			
		} catch (SQLException e) {
			Log.e(LOG_TAG, "Can't create database"   + e.getStackTrace().toString());
			throw new RuntimeException(e);
		}
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		if (oldVersion < 2) {
			db.execSQL("CREATE INDEX IF NOT EXISTS idx_usage_logs_date ON UsageLogs(logDate)");
		}
	}
	/**
	 * Returns the Database Access Object (DAO) for our UsageCounter class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UsageCounter, Integer> getUsageCounterDao() throws SQLException {
		if (usageCounterDao == null) {
			Log.i(LOG_TAG,"UsageCounter is null will create a new dao");
			usageCounterDao = getDao(UsageCounter.class);
			
		}
		return usageCounterDao;
	}
 
	/**
	 * Returns the Database Access Object (DAO) for our UsageCounter class. It will create it or just give the cached
	 * value.
	 */
	public Dao<ArchivedUsageCounter, Integer> getArchivedUsageCounterDao() throws SQLException {
		if (archivedUsageCounterDao == null) {
			Log.i(LOG_TAG,"archivedUsageCounter is null will create a new dao");
			archivedUsageCounterDao = getDao(ArchivedUsageCounter.class);
			
		}
		return archivedUsageCounterDao;
	}
 	
	/**
	 * Returns the Database Access Object (DAO) for our Alert class. It will create it or just give the cached
	 * value.
	 */
	public Dao<Alert, Integer> getAlertDao() throws SQLException {
		if (alertDao == null) {
			Log.i(LOG_TAG,"AlertDao is null will create a new dao");
			alertDao = getDao(Alert.class);
			
		}
		return alertDao;
	}
	
	/**
	 * Returns the Database Access Object (DAO) for our UsageLog class. It will create it or just give the cached
	 * value.
	 */
	public Dao<UsageLog, Integer> getUsageLogDao() throws SQLException {
		if (usageLogDao == null) {
			Log.i(LOG_TAG,"usagelogDao is null will create a new dao");
			usageLogDao = getDao(UsageLog.class);
			
		}
		return usageLogDao;
	}

	/**
	 * Close the database connections and clear any cached DAOs.
	 */
	@Override
	public void close() {
		super.close();
		alertDao = null;
		usageCounterDao = null;
		usageLogDao = null;
		archivedUsageCounterDao = null;
		Log.i(LOG_TAG,"usagelogDao is closed and set to null");
	}
}

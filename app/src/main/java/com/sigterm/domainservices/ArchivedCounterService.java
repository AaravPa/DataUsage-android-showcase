package com.sigterm.domainservices;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.util.DateUtils;

public class ArchivedCounterService {
	private final String LOG_TAG = getClass().getSimpleName();
	private DatabaseHelper _helper;

	public ArchivedCounterService (DatabaseHelper helper) {
		this._helper = helper;
	}
	public ArchivedUsageCounter getArchivedCounterFromDefaultCounter(UsageCounter c){
		
		Date startDate = null;
		Date endDate = null;
		if (c.getStartDateTime() == null) startDate = DateUtils.getStartDateOfCurrentMonth();
		else startDate = c.getStartDateTime();
		
		endDate = DateUtils.getToday();
		ArchivedUsageCounter cellularArchivedCounter = new ArchivedUsageCounter(c.getCounterName(), startDate, endDate,
				c.isRecurring(), c.getRepeatInterval(), c.getRepeatFrequency(), c.getDataDirectionType(),
				c.getNetworkDataType(), c.getCellularBytesSent(_helper), c.getCellularBytesRecd(_helper), c.getCellularBytesTotal(_helper),
				c.getWifiBytesSent(_helper), c.getWifiBytesRecd(_helper), c.getWifiBytesTotal(_helper), c.isEnableAlerts(), c.getQuota(),
				c.getQuotaUnit(), c.getQuotaInBytes());

		return cellularArchivedCounter;
	}

	public void createCounter(ArchivedUsageCounter usageCounter) {
		try {
			Dao<ArchivedUsageCounter, Integer> dao = _helper.getArchivedUsageCounterDao();
			dao.create(usageCounter);
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception createCounter " + e.getStackTrace().toString());
		}

	}
	public List<ArchivedUsageCounter> getArchivedCellularCounters() {

		List<ArchivedUsageCounter> counters = null;
		try {
			Dao<ArchivedUsageCounter, Integer> dao = _helper.getArchivedUsageCounterDao();
			counters = dao.queryBuilder().where()
					.eq(UsageCounter.NETWORKDATATYPE_COLUMN_NAME, NetworkDataType.CELLULAR).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());
		}
		return counters;

	}

	public List<ArchivedUsageCounter> getArchivedWiFiCounters() {
		List<ArchivedUsageCounter> counters = null;
		try {
			Dao<ArchivedUsageCounter, Integer> dao = _helper.getArchivedUsageCounterDao();
			counters = dao.queryBuilder().where()
					.eq(UsageCounter.NETWORKDATATYPE_COLUMN_NAME, NetworkDataType.WIFI).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());
		}
		return counters;
	}

	public List<ArchivedUsageCounter> getArchivedCounters() {
		List<ArchivedUsageCounter> counters = null;
		try {
			Dao<ArchivedUsageCounter, Integer> dao = _helper.getArchivedUsageCounterDao();
			counters = dao.queryForAll();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception updateCounter " + e.getStackTrace().toString());
		}
		return counters;
	}

	public ArchivedUsageCounter getCounterById(int counterId) {
		ArchivedUsageCounter counter = null;
		List<ArchivedUsageCounter> counters = null;
		try {
			Dao<ArchivedUsageCounter, Integer> dao = _helper.getArchivedUsageCounterDao();
			counters = dao.queryBuilder().where().eq(ArchivedUsageCounter.COUNTERID_COLUMN_NAME, counterId).query();
		} catch (SQLException e) {
			e.printStackTrace();
			Log.e(LOG_TAG, "Sql Exception getCounterById" + e.getStackTrace().toString());
		}
		if (counters != null && counters.size() > 0)
			counter = counters.get(0);
		return counter;
	}


}

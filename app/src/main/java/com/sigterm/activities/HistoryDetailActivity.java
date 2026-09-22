package com.sigterm.activities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.sigterm.domainservices.ArchivedCounterService;
import com.sigterm.domainservices.UsageCounterService;
import com.sigterm.domainservices.UsageLogService;
//import com.sigterm.entities.ArchivedUsageCounter;
//import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.UsageLog;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;
import com.sigterm.util.BackgroundExecutor;
import com.sigterm.views.HistoryDetailListAdapter;
import com.sigterm.views.PlanDetailHistoryChart;
import com.sigterm.views.PlanHistoryChart;
import com.sigterm.R;

public class HistoryDetailActivity extends OrmLiteBaseListActivity<DatabaseHelper> {
	private final String LOG_TAG = getClass().getSimpleName();
	// private UsageCounterService _usageCounterService;
	// private ArchivedCounterService _archivedCounterService;
	private DatabaseHelper _helper;
	private UsageLogService _usageLogService;
	// private int _counterId = 0;
	private View _chartView;
	private LinearLayout _chartLayout;
	private String _networkType;
	private String _startDate;
	private String _endDate;
	private String _totalUsage;
	private String _sent;
	private String _received;
	private final BackgroundExecutor _backgroundExecutor = new BackgroundExecutor();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Debug.startMethodTracing("HistoryDetailActivity");
		super.onCreate(savedInstanceState);
		_helper = getHelper();
		// _usageCounterService = new UsageCounterService(_helper);
		// _archivedCounterService = new ArchivedCounterService(_helper);
		_usageLogService = new UsageLogService(_helper);

		// _counterId = (int) this.getIntent().getIntExtra("CounterId", 0);
		// _isCurrentCellular = (boolean)
		// this.getIntent().getBooleanExtra("IsCurrentCellular", false);
		// _isCurrentWiFi = (boolean)
		// this.getIntent().getBooleanExtra("IsCurrentWiFi", false);
		_networkType = this.getIntent().getStringExtra("networkType");
		_startDate = this.getIntent().getStringExtra("startDate");
		_endDate = this.getIntent().getStringExtra("endDate");
		_totalUsage = this.getIntent().getStringExtra("totalUsage");
		_sent = this.getIntent().getStringExtra("sent");
		_received = this.getIntent().getStringExtra("recd");

		setContentView(R.layout.historydetail);
		//Debug.stopMethodTracing();
	}

//	private void setView() {
//		_chartLayout = (LinearLayout) findViewById(R.id.chartDetail);
//		Resources res = getResources();
//		TextView tvDates = (TextView) this.findViewById(R.id.tvDates);
//		TextView tvType = (TextView) this.findViewById(R.id.tvType);
//		TextView tvDataTotal = (TextView) this.findViewById(R.id.tvDataTotal);
//		TextView tvDataSent = (TextView) this.findViewById(R.id.tvDataSent);
//		TextView tvDataRecd = (TextView) this.findViewById(R.id.tvDataRecd);
//		// if (_counterId > 0) {
//		// ArchivedUsageCounter counter = null;
//		// if (_isCurrentCellular)
//		// counter =
//		// _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultCellularBillingCounter());
//		// else if(_isCurrentWiFi)
//		// counter =
//		// _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultWiFiBillingCounter());
//		// else
//		// counter = _archivedCounterService .getCounterById(_counterId);
//
//		// String networkType = counter.getNetworkDataType();
//		boolean isCellular = (_networkType.equals(NetworkDataType.CELLULAR));
//		// tvDates.setText(DateUtils.getDateInMediumFormat(counter.getStartDateTime())
//		// + " to "
//		// + DateUtils.getDateInMediumFormat(counter.getEndDateTime()));
//		tvDates.setText(_startDate + " to " + _endDate);
//		tvDataTotal.setText(_totalUsage);
//		tvDataSent.setText(_sent);
//		tvDataRecd.setText(_received);
//
//		if (isCellular) {
//			// tvDataTotal.setText(DataunitUtils.formatData(counter.getCellularBytesTotal(),
//			// getBaseContext()));
//			// tvDataSent.setText(DataunitUtils.formatData(counter.getCellularBytesSent(),
//			// getBaseContext()));
//			// tvDataRecd.setText(DataunitUtils.formatData(counter.getCellularBytesRecd(),
//			// getBaseContext()));
//			String lbl = res.getString(R.string.Cellular) + " " + res.getString(R.string.Usage) + " "
//					+ res.getString(R.string.From) + " ";
//			tvType.setText(lbl);
//		} else {
//			// tvDataTotal.setText(DataunitUtils.formatData(counter.getWifiBytesTotal(),
//			// getBaseContext()));
//			// tvDataSent.setText(DataunitUtils.formatData(counter.getWifiBytesSent(),
//			// getBaseContext()));
//			// tvDataRecd.setText(DataunitUtils.formatData(counter.getWifiBytesRecd(),
//			// getBaseContext()));
//			// tvDataTotal.setText(DataunitUtils.formatData(_wifiTotal,
//			// getBaseContext()));
//			// tvDataSent.setText(DataunitUtils.formatData(_wifiSent,
//			// getBaseContext()));
//			// tvDataRecd.setText(DataunitUtils.formatData(_wifiReceived,
//			// getBaseContext()));
//			String lbl = res.getString(R.string.WiFi) + " " + res.getString(R.string.Usage) + " "
//					+ res.getString(R.string.From) + " ";
//			tvType.setText(lbl);
//		}
//
//		// this will get all logs for this counter by start and end date.
//
//		// List<UsageLog> logList =
//		// _usageLogService.getLogByCounter(counter);
//		// get usageLog array for days based on usage log
//		Date startDate = DateUtils.getDateFromMediumFormatString(_startDate);
//		Date endDate = DateUtils.getDateFromMediumFormatString(_endDate);
//
//		List<UsageLog> usageLogs = getDailyUsageLog(startDate, endDate);
//		Log.i(LOG_TAG, "StartDate:" + startDate);
//		Log.i(LOG_TAG, "EndDate:" + endDate);
//		Log.i(LOG_TAG, "log Count:" + usageLogs.size());
//		UsageLog[] logs = usageLogs.toArray(new UsageLog[usageLogs.size()]);
//		// logList.toArray(usageLogs);
//		HistoryDetailListAdapter adapter = new HistoryDetailListAdapter(getBaseContext(), logs, isCellular);
//		setListAdapter(adapter);
//		if (usageLogs != null && usageLogs.size() > 0) {
//			Log.i(LOG_TAG, "networkType:" + _networkType);
//			setChartView(usageLogs, _networkType);
//		}
//		// } else {
//		// TextView tv = new TextView(getBaseContext());
//		// tv.setText(getResources().getString(R.string.detailsNotAvailable));
//		// UsageLog[] log = null;
//		// HistoryDetailListAdapter adapter = new
//		// HistoryDetailListAdapter(getBaseContext(), log, false);
//		// setListAdapter(adapter);
//		// setContentView(tv);
//		// }
//	}
	private class SetViewTask {
		List<UsageLog> usageLogs = null;
		UsageLog[] logs  = null;
		void executeTask() {
			_backgroundExecutor.execute(() -> { doInBackground(); return null; }, ignored -> onPostExecute());
		}

		private Void doInBackground() {
	 		Date startDate = DateUtils.getDateFromMediumFormatString(_startDate);
	 		Date endDate = DateUtils.getDateFromMediumFormatString(_endDate);

	 		usageLogs = getDailyUsageLog(startDate, endDate);
	 		Log.i(LOG_TAG, "StartDate:" + startDate);
	 		Log.i(LOG_TAG, "EndDate:" + endDate);
	 		Log.i(LOG_TAG, "log Count:" + usageLogs.size());
	 		logs = usageLogs.toArray(new UsageLog[usageLogs.size()]);	

			
			return null;
			
		}
		
	     private void onPostExecute() {
	    	 _chartLayout = (LinearLayout) HistoryDetailActivity.this.findViewById(R.id.chartDetail);
	 		Resources res = getResources();
	 		TextView tvDates = (TextView) HistoryDetailActivity.this.findViewById(R.id.tvDates);
	 		TextView tvType = (TextView) HistoryDetailActivity.this.findViewById(R.id.tvType);
	 		TextView tvDataTotal = (TextView) HistoryDetailActivity.this.findViewById(R.id.tvDataTotal);
	 		TextView tvDataSent = (TextView) HistoryDetailActivity.this.findViewById(R.id.tvDataSent);
	 		TextView tvDataRecd = (TextView) HistoryDetailActivity.this.findViewById(R.id.tvDataRecd);
	 		
	 		boolean isCellular = (_networkType.equals(NetworkDataType.CELLULAR));
	 		
		 		tvDates.setText(res.getString(R.string.date_range, _startDate, _endDate));
	 		tvDataTotal.setText(_totalUsage);
	 		tvDataSent.setText(_sent);
	 		tvDataRecd.setText(_received);

	 		if (isCellular) {
	 			String lbl = res.getString(R.string.Cellular) + " " + res.getString(R.string.Usage) + " "
	 					+ res.getString(R.string.From) + " ";
	 			tvType.setText(lbl);
	 		} else {
	 			String lbl = res.getString(R.string.WiFi) + " " + res.getString(R.string.Usage) + " "
	 					+ res.getString(R.string.From) + " ";
	 			tvType.setText(lbl);
	 		}

	 		HistoryDetailListAdapter adapter = new HistoryDetailListAdapter(getBaseContext(), logs, isCellular);
	 		setListAdapter(adapter);
	 		if (usageLogs != null && usageLogs.size() > 0) {
	 			Log.i(LOG_TAG, "networkType:" + _networkType);
	 			setChartView(usageLogs, _networkType);
	 		}
	     }
	}
	
	private ArrayList<UsageLog> getDailyUsageLog(Date startDate, Date endDate) {
		ArrayList<UsageLog> logs = new ArrayList<UsageLog>();
		// Date startDate = counter.getStartDateTime();
		// Date endDate = counter.getEndDateTime();
		Log.i(LOG_TAG, "StartDate:" + startDate);
		Log.i(LOG_TAG, "EndDate:" + endDate);
		Calendar startDateCal = DateUtils.getCalendarFromDate(startDate);
		Calendar endDateCal = DateUtils.getCalendarFromDate(endDate);
		endDateCal.add(Calendar.DATE, 1);
		if (startDateCal.equals(endDateCal)) {
			UsageLog log = _usageLogService.getCumulativeLogByDate(startDateCal.getTime());
			logs.add(log);
		} else {
//			while (startDateCal.before(endDateCal) && !startDateCal.after(DateUtils.getTodayCal())) {
//				Log.i(LOG_TAG, "Looking up history details for date:" + startDateCal.getTime());
//				UsageLog log = _usageLogService.getCumulativeLogByDate(startDateCal.getTime());
//				logs.add(log);
//				startDateCal.add(Calendar.DATE, 1);
//			}
			logs = _usageLogService.getCumulativeLogByDateRange(startDateCal,endDateCal);
		}
		return logs;
	}

	@Override
	protected void onResume() {
	 	new SetViewTask().executeTask();//setView();
		super.onResume();
	}

	private void setChartView(List<UsageLog> logList, String networkDataType) {
		if (_chartView != null)
			_chartLayout.removeAllViewsInLayout();
		_chartView = new PlanDetailHistoryChart().execute(this, logList, networkDataType);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		_chartLayout.addView(_chartView, params);

	}

	@Override
	protected void onDestroy() {
		_backgroundExecutor.shutdown();
		super.onDestroy();
	}

}

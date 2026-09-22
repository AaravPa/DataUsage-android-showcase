package com.sigterm.activities;

import java.util.List;

import android.content.Intent;

import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;


import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.j256.ormlite.android.apptools.OrmLiteBaseListActivity;
import com.sigterm.R;
import com.sigterm.domainservices.ArchivedCounterService;
import com.sigterm.domainservices.UsageCounterService;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.views.HistoryListAdapter;
import com.sigterm.views.PlanHistoryChart;
import com.sigterm.views.SegmentedRadioGroup;
import com.sigterm.util.BackgroundExecutor;

public class HistoryActivity extends OrmLiteBaseListActivity<DatabaseHelper> {

	private final String LOG_TAG = getClass().getSimpleName();
	private ArchivedCounterService _archivedCounterService;
	private UsageCounterService _usageCounterService;
	private DatabaseHelper _helper;
	private View _chartView;
	private LinearLayout _chartLayout;
	private TextView _tvNoItems;
	private final BackgroundExecutor _backgroundExecutor = new BackgroundExecutor();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Debug.startMethodTracing("HistoryActivity");
		super.onCreate(savedInstanceState);
		_helper = getHelper();
		_archivedCounterService = new ArchivedCounterService(_helper);
		_usageCounterService = new UsageCounterService(_helper);
//		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
//	    requestWindowFeature(Window.FEATURE_PROGRESS);
//
//	       setProgressBarIndeterminateVisibility(true);
//	        setProgressBarVisibility(true);
		setContentView(R.layout.history);
		//Debug.stopMethodTracing();
	}
	
	private void setView() {
		
		_chartLayout = (LinearLayout) findViewById(R.id.chart);

		_tvNoItems = (TextView) this.findViewById(R.id.tvNoItems);
		_tvNoItems.setVisibility(View.GONE);

		SegmentedRadioGroup sgGroup = (SegmentedRadioGroup) findViewById(R.id.segment_text);
		int selected = sgGroup.getCheckedRadioButtonId();
		if (selected == R.id.rbBoth)
			new SetBothViewTask().executeTask();//setBothView();
		else if (selected == R.id.rbWiFi)
			new SetWiFiViewTask().executeTask();//setWiFiView();
		else
			new SetCellularViewTask().executeTask();//setCellularView();

		sgGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup arg0, int resId) {
				if (resId == R.id.rbCellular) {
					new SetCellularViewTask().executeTask();//setCellularView();
				} else if (resId == R.id.rbWiFi) {
					new SetWiFiViewTask().executeTask();//setWiFiView();
				} else if (resId == R.id.rbBoth) {
					new SetBothViewTask().executeTask();//setBothView();
				}
			}
		});
		
	}


	
//	private void setBothView() {
//		List<ArchivedUsageCounter> counterList = _archivedCounterService.getArchivedCounters();
//		ArchivedUsageCounter cellularArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultCellularBillingCounter());
//		counterList.add(cellularArchivedCounter);
//		ArchivedUsageCounter wifiArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultWiFiBillingCounter());
//		counterList.add(wifiArchivedCounter);
//		
//		Log.i(LOG_TAG, "Total Archived Counters:" + counterList.size());
//		if (counterList != null && counterList.size() > 0) {
//			List<ArchivedUsageCounter> cellularCounterList = _archivedCounterService.getArchivedCellularCounters();
//			cellularCounterList.add(cellularArchivedCounter);
//			List<ArchivedUsageCounter> wifiCounterList = _archivedCounterService.getArchivedWiFiCounters();
//			wifiCounterList.add(wifiArchivedCounter);
//			Log.i(LOG_TAG, "Total Archived cellular Counters:" + cellularCounterList.size());
//			setListView(counterList);
//			setChartView(cellularCounterList, wifiCounterList, NetworkDataType.BOTH);
//		} else {
//			_tvNoItems.setVisibility(View.VISIBLE);
//		}
//	}
	
	private abstract class ScreenTask {
		void executeTask() {
			_backgroundExecutor.execute(() -> { doInBackground(); return null; }, ignored -> onPostExecute(null));
		}
		protected abstract Void doInBackground();
		protected abstract void onPostExecute(Void ignored);
	}

	private class SetBothViewTask extends ScreenTask {
		// TODO reuse cellular counters (defer)
		List<ArchivedUsageCounter> counterList = null;
		ArchivedUsageCounter cellularArchivedCounter = null;
		ArchivedUsageCounter wifiArchivedCounter = null;
		List<ArchivedUsageCounter> cellularCounterList = null;
		List<ArchivedUsageCounter> wifiCounterList = null;
		 
		@Override 
		protected Void doInBackground() {
			counterList = _archivedCounterService.getArchivedCounters();
			cellularArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultCellularBillingCounter());
			counterList.add(cellularArchivedCounter);
			wifiArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultWiFiBillingCounter());
			counterList.add(wifiArchivedCounter);
			 
			Log.i(LOG_TAG, "Total Archived Counters:" + counterList.size());
			if (counterList != null && counterList.size() > 0) {
				cellularCounterList = _archivedCounterService.getArchivedCellularCounters();
				cellularCounterList.add(cellularArchivedCounter);
				wifiCounterList = _archivedCounterService.getArchivedWiFiCounters();
				wifiCounterList.add(wifiArchivedCounter);
				Log.i(LOG_TAG, "Total Archived cellular Counters:" + cellularCounterList.size());
			} 
			
			return null;
			
		}
		
	     protected void onProgressUpdate(Void... arg0) {
	         
	     }
	     
	     @Override 
	     protected void onPostExecute(Void arg0) {
	    	 if (counterList != null && counterList.size() > 0) {
					Log.i(LOG_TAG, "Total Archived cellular Counters:" + cellularCounterList.size());
					setListView(counterList);
					setChartView(cellularCounterList, wifiCounterList, NetworkDataType.BOTH);
				} else {
					_tvNoItems.setVisibility(View.VISIBLE);
				}
	    	 loadAd(); 
	     }
	    
	}
//	private void setWiFiView() {
//
//		List<ArchivedUsageCounter> counterList = _archivedCounterService.getArchivedWiFiCounters();
//		ArchivedUsageCounter wifiArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultWiFiBillingCounter());
//		counterList.add(wifiArchivedCounter);
//
//		if (counterList != null && counterList.size() > 0) {
//			setListView(counterList);
//			setChartView(null, counterList, NetworkDataType.WIFI);
//		} else {
//			_tvNoItems.setVisibility(View.VISIBLE);
//		}
//	}
	private class SetWiFiViewTask extends ScreenTask {
		List<ArchivedUsageCounter> counterList = null;
		ArchivedUsageCounter wifiArchivedCounter = null;
		 
		@Override 
		protected Void doInBackground() {
			counterList = _archivedCounterService.getArchivedWiFiCounters();
			wifiArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultWiFiBillingCounter());
			counterList.add(wifiArchivedCounter);
			 
			return null;
			
		}
		
	     protected void onProgressUpdate(Void... arg0) {
	         
	     }
	     
	     @Override 
	     protected void onPostExecute(Void arg0) {
		    	 if (counterList != null && counterList.size() > 0) {
		 			setListView(counterList);
		 			setChartView(null, counterList, NetworkDataType.WIFI);
		 		} else {
		 			_tvNoItems.setVisibility(View.VISIBLE);
		 		}
	    		 loadAd(); 
	     }
	}
//	private void setCellularView() {
//		List<ArchivedUsageCounter> counterList = _archivedCounterService.getArchivedCellularCounters();
//		ArchivedUsageCounter cellularArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultCellularBillingCounter());
//		counterList.add(cellularArchivedCounter);
//		if (counterList != null && counterList.size() > 0) {
//			setListView(counterList);
//			setChartView(counterList, null, NetworkDataType.CELLULAR);
//		} else {
//			_tvNoItems.setVisibility(View.VISIBLE);
//		}
//	}
	
	private class SetCellularViewTask extends ScreenTask {
		List<ArchivedUsageCounter> counterList = null;
		ArchivedUsageCounter cellularArchivedCounter = null;
		 
		@Override 
		protected Void doInBackground() {
			counterList = _archivedCounterService.getArchivedCellularCounters();
			cellularArchivedCounter = _archivedCounterService.getArchivedCounterFromDefaultCounter(_usageCounterService.getDefaultCellularBillingCounter());
			counterList.add(cellularArchivedCounter);
			 
			return null;
			
		}
		
	     protected void onProgressUpdate(Void... arg0) {
	         
	     }
	     
	     @Override 
	     protected void onPostExecute(Void arg0) {
				if (counterList != null && counterList.size() > 0) {
					setListView(counterList);
					setChartView(counterList, null, NetworkDataType.CELLULAR);
				} else {
					_tvNoItems.setVisibility(View.VISIBLE);
				}
				loadAd(); 
	     }
	}
	
	private void loadAd() {
		AdView adView = (AdView) findViewById(R.id.adView);
		if (adView != null) adView.loadAd(new AdRequest.Builder().build());
	}
	@Override
	protected void onResume() {
		setView();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		_backgroundExecutor.shutdown();
		super.onDestroy();
	}

	private void setChartView(List<ArchivedUsageCounter> cellularCounterList,
			List<ArchivedUsageCounter> wifiCounterList, String networkDataType) {
		if (_chartView != null)
			_chartLayout.removeAllViewsInLayout();
		_chartView = new PlanHistoryChart().execute(this, cellularCounterList, wifiCounterList, networkDataType);
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		_chartLayout.addView(_chartView, params);

	}

	private void setListView(List<ArchivedUsageCounter> counterList) {
		if (counterList == null)
			return;
		ArchivedUsageCounter[] usageCounters = new ArchivedUsageCounter[counterList.size()];
		counterList.toArray(usageCounters);
		HistoryListAdapter adapter = new HistoryListAdapter(getBaseContext(), usageCounters);
		setListAdapter(adapter);
		ListView lv = getListView();
		final Intent detailActivity = new Intent().setClass(this, HistoryDetailActivity.class);
		lv.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int arg2, long arg3) {
				TextView tvNetworkType = (TextView) view.findViewById(R.id.tvNetworkType);
				TextView tvStartDate = (TextView) view.findViewById(R.id.tvStartDate);
				TextView tvEndDate = (TextView) view.findViewById(R.id.tvEndDate);
				TextView tvTotalUsage = (TextView) view.findViewById(R.id.tvTotalUsage);
				TextView tvSent = (TextView) view.findViewById(R.id.tvSent);
				TextView tvRecd = (TextView) view.findViewById(R.id.tvReceived);
			
				String networkType = (String) tvNetworkType.getText();
				String startDate = (String)tvStartDate.getText();
				String endDate = (String)tvEndDate.getText();
				String totalUsage = (String)tvTotalUsage.getText();
				String sent =  (String)tvSent.getText();
				String recd =  (String)tvRecd.getText();
				
				detailActivity.putExtra("networkType", networkType);
				detailActivity.putExtra("startDate", startDate);
				detailActivity.putExtra("endDate", endDate);
				detailActivity.putExtra("totalUsage", totalUsage);
				detailActivity.putExtra("sent", sent);
				detailActivity.putExtra("recd", recd);
				startActivity(detailActivity);
			}
		});
		lv.setTextFilterEnabled(true);
	}

}

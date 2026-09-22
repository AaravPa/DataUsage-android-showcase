package com.sigterm.activities;


import java.util.Calendar;
import java.util.Locale;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
 

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.j256.ormlite.android.apptools.OrmLiteBaseActivity;
import com.sigterm.R;
import com.sigterm.domainservices.UsageCounterService;

import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.ProgressbarColors;
import com.sigterm.entities.enums.QuotaUnit;
import com.sigterm.entities.enums.RepeatInterval;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.services.UsageRefreshWorker;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;
import com.sigterm.util.BackgroundExecutor;
import com.sigterm.util.PreferenceUtils;
import com.sigterm.views.DataUsageProgressBar;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

public class PlanUsageActivity extends OrmLiteBaseActivity<DatabaseHelper> {

	private final String LOG_TAG = getClass().getSimpleName();
	DataUsageReceiver _receiver;
	private UsageCounterService _usageCounterSerivce;
	private DatabaseHelper _helper;
	private String _networkDataType;
	private boolean _isCellularView;
	private final BackgroundExecutor _backgroundExecutor = new BackgroundExecutor();
	 
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		//Debug.startMethodTracing("PlanUsageActivity");
				
		super.onCreate(savedInstanceState);
		setContentView(R.layout.planusage);
		_helper = getHelper();
		_usageCounterSerivce = new UsageCounterService(_helper);		
		_networkDataType = this.getIntent().getStringExtra(NetworkDataType.class.getSimpleName());
		_isCellularView = _networkDataType.equals(NetworkDataType.CELLULAR);
		//updatePrefs();
		Log.i("PlanUsageCall","called from oncreate");
		
		//Debug.stopMethodTracing();
	}
	@Override
	protected void onStart() {
		
		super.onStart();
		Log.i("PlanUsageCall","called from onStart");
		//new SetViewTask().execute();
		Log.i("reset","onStart called");
	}
	
	private void loadAd() {
		AdView adView = (AdView) findViewById(R.id.adView);
		if (adView != null) adView.loadAd(new AdRequest.Builder().build());
	}

	 private class SetViewTask {
		 boolean limited,expires = false;
		 long recd,sent,todayUsage,weekUsage,planUsage,remainingQuota = 0;
		 float remainingDays,idealUsagePercentage,percentUsed=0.0f;
		 long idealUsageVal,idealMinusActual,predictedUsage,remainingDaily = 0;		 
		 String desText, overageLikely, startDateTime, endDateTime, nextBillingStartDate;
		 String quotaInBytes, color;
		
//		 ProgressDialog progressDialog = new ProgressDialog(PlanUsageActivity.this);
//		    //declare other objects as per your need
//		    @Override
//		    protected void onPreExecute()
//		    {
//		       // progressDialog.show();
//		    };   
		void executeTask() {
			_backgroundExecutor.execute(() -> { doInBackground(); return null; }, ignored -> onPostExecute());
		}

		private Void doInBackground() {
			 	//Debug.startMethodTracing("PlanUsageBackground");
				Resources res = getResources();
				
		 		UsageCounter counter = (_isCellularView) ? _usageCounterSerivce.getDefaultCellularBillingCounter() : _usageCounterSerivce.getDefaultWiFiBillingCounter();
		 		Log.i(LOG_TAG,"Set View in BG: IsCellularView: "+_isCellularView);
		 		String na = res.getString(R.string.na);
		 		 limited = (counter.getQuota() > 0);
		 		 expires = (counter.getStartDateTime() != null && counter.getEndDateTime() != null);

		 		//this values will always be available
		 		 recd = (_isCellularView) ? counter.getCellularBytesRecd(_helper) : counter.getWifiBytesRecd(_helper);
		 		 sent =  (_isCellularView) ? counter.getCellularBytesSent(_helper) : counter.getWifiBytesSent(_helper);
		 		 todayUsage = (_isCellularView) ? _usageCounterSerivce.getTodayCellularUsage() : _usageCounterSerivce.getTodayWiFiUsage();
		 		 weekUsage = (_isCellularView) ? _usageCounterSerivce.getThisWeekCellularUsage(getBaseContext()) : _usageCounterSerivce.getThisWeekWiFiUsage(getBaseContext());

		 		
		 		//show this stuff based on quota 
		 		if (limited) {
		 			 planUsage = (_isCellularView) ? counter.getCellularBytesTotal(_helper) : counter.getWifiBytesTotal(_helper);
		 			 percentUsed =  ((_isCellularView) ? counter.getPercentCellularQuotaUsed(_helper): counter.getPercentWiFiQuotaUsed(_helper));
		 			 remainingQuota = (_isCellularView) ? _usageCounterSerivce.getRemainingCellularQuota(counter) : _usageCounterSerivce.getRemainingWiFiQuota(counter);
		 			 quotaInBytes = DataunitUtils.formatData(counter.getQuotaInBytes(), getBaseContext());
					 color = _usageCounterSerivce.getProgressBarColor(getBaseContext(), counter);
		 						
		 		} else {
		 			 planUsage = (_isCellularView) ? counter.getCellularBytesTotal(_helper) : counter.getWifiBytesTotal(_helper);
		 			}
		 		if(expires)
		 		{
		 			startDateTime = DateUtils.getDateInMediumFormat(counter.getStartDateTime());
		 			endDateTime = DateUtils.getDateInMediumFormat(counter.getEndDateTime());
		 			remainingDays =  _usageCounterSerivce.getRemainingDays(counter,getApplicationContext());
		 			nextBillingStartDate = DateUtils.getDateInMediumFormat(counter.getNextBillingStartDate());
		 		}
		 		//
		 		//show this stuff only when we have quota that expires
		 		if(expires && limited){
		 			 idealUsagePercentage = _usageCounterSerivce.getIdealUsagePercentage(counter);
		 			idealUsageVal = _usageCounterSerivce.getIdealUsageInBytes(counter);
		 			 idealMinusActual = (_isCellularView) ? _usageCounterSerivce.getIdealMinusActualCellularUsage(counter) : _usageCounterSerivce.getIdealMinusActualWiFiUsage(counter);
		 			 desText = (idealMinusActual > 0) ? res.getString(R.string.lessthanideal) :res.getString(R.string.morethanideal);
		 			 predictedUsage = (_isCellularView) ? _usageCounterSerivce.getPredictedCellularUsage(counter) : _usageCounterSerivce.getPredictedWiFiUsage(counter);
		 			 overageLikely = (idealMinusActual < 0) ? res.getString(R.string.yes) : res.getString(R.string.no);    
		 			 remainingDaily = (_isCellularView) ? _usageCounterSerivce.getRemainingDailyCellularQuota(counter,getApplicationContext()) : _usageCounterSerivce.getRemainingDailyWiFiQuota(counter,getApplicationContext());
		 		
		 		}
		 		
		 		//Debug.stopMethodTracing();
				return null;	
				
			}
	     private void onPostExecute() {
	    	 Log.i(LOG_TAG,"Now setting view..");
	    	 //if(progressDialog.isShowing())	 progressDialog.dismiss();
	    	 	Resources res = getResources();
	    	 	String na = res.getString(R.string.na);
		 		TextView tvPlanUsage = (TextView) PlanUsageActivity.this.findViewById(R.id.tvPlanUsage);
		 		TextView tvPlanQuota = (TextView) PlanUsageActivity.this.findViewById(R.id.tvPlanQuota);
		 		TextView tvDaysRemVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvDaysRemVal);
		 		TextView tvQuotaRemVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvQuotaRemVal);
		 		TextView tvDailyQuotaRemVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvDailyQuotaRemVal);
		 		TextView tvIdealUsageVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvIdealUsageVal);
		 		TextView tvProjectedUsageVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvProjectedUsageVal);
		 		TextView tvUsageDiffVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvUsageDiffVal);
		 		
		 		TextView tvOverageLikelyVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvOverageLikelyVal);
		 		TextView tvUsageTodayVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvUsageTodayVal);
		 		TextView tvUsageThisWeekVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvUsageThisWeekVal);
		 		TextView tvBillingPeriodVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvBillingPeriodVal);
		 		TextView tvNextBillDateVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvNextBillDateVal);
		 		TextView todaydateVal = (TextView) PlanUsageActivity.this.findViewById(R.id.todaydateVal);
		 		TextView tvReceivedVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvReceivedVal);
		 		TextView tvSentVal = (TextView) PlanUsageActivity.this.findViewById(R.id.tvSentVal);
		 		DataUsageProgressBar pbUsage = (DataUsageProgressBar) PlanUsageActivity.this.findViewById(R.id.pbUsage);

		 		tvReceivedVal.setText(DataunitUtils.formatData(recd,getBaseContext()));

		 		tvSentVal.setText(DataunitUtils.formatData(sent,getBaseContext()));
		 		todaydateVal.setText(String.format("%s",DateUtils.getDateInMediumFormat(DateUtils.getToday())));

		 		tvUsageTodayVal.setText(DataunitUtils.formatData(todayUsage,getBaseContext()));
		 		tvUsageThisWeekVal.setText(DataunitUtils.formatData(weekUsage,getBaseContext()));
		 		if (limited) {
		 			tvPlanQuota.setText(String.format("OF %s USED",quotaInBytes));
		 			tvPlanUsage.setText(DataunitUtils.formatData(planUsage, getBaseContext()));
		 			
		 			Rect bounds = pbUsage.getProgressDrawable().getBounds();
		 			if (color.equals(ProgressbarColors.GREEN)){
			 				pbUsage.setProgressDrawable(ContextCompat.getDrawable(PlanUsageActivity.this, R.drawable.greenprogress));
		 			}
		 			else if (color.equals(ProgressbarColors.YELLOW)){
			 				pbUsage.setProgressDrawable(ContextCompat.getDrawable(PlanUsageActivity.this, R.drawable.yellowprogress));
		 			}
		 			else if (color.equals(ProgressbarColors.RED)){
			 				pbUsage.setProgressDrawable(ContextCompat.getDrawable(PlanUsageActivity.this, R.drawable.redprogress));
		 			}
		 			pbUsage.getProgressDrawable().setBounds(bounds);
		 			pbUsage.setProgress(1); 
		 			pbUsage.setMax(100);
		 			pbUsage.setProgress((int)percentUsed);
					pbUsage.setTextColor(ContextCompat.getColor(PlanUsageActivity.this, R.color.text_primary));
					pbUsage.setTextSize(14);
		 			pbUsage.setText(String.format(Locale.getDefault(), "%.02f%s", percentUsed, "%"));
		 			
		 			tvQuotaRemVal.setText(DataunitUtils.formatData(remainingQuota,getBaseContext()));
		 						
		 		} else {
		 			pbUsage.setMax(100);
		 			tvPlanQuota.setText(String.format(Locale.getDefault(), "%s", res.getString(R.string.unlimited_plan)));
					pbUsage.setTextColor(ContextCompat.getColor(PlanUsageActivity.this, R.color.text_primary));
					pbUsage.setTextSize(14);
		 			pbUsage.setText("0.0%");
		 			pbUsage.setProgress(0);
		 			tvPlanUsage.setText(DataunitUtils.formatData(planUsage, getBaseContext()));
		 			tvQuotaRemVal.setText(na);

		 			}
		 		
		 		//show this stuff when we have a plan that expires
		 		if (expires) {
		 			tvBillingPeriodVal.setText(String.format(Locale.getDefault(), "%s - %s",startDateTime, endDateTime));
		 			tvDaysRemVal.setText(String.format(Locale.getDefault(), "%.01f ", remainingDays));
		 			tvNextBillDateVal.setText(String.format(Locale.getDefault(), "%s",nextBillingStartDate));
		 		} else {
		 			tvBillingPeriodVal.setText(String.format(Locale.getDefault(), "%s",  res.getString(R.string.no_expiry)));
		 			tvDaysRemVal.setText(na);
		 			tvNextBillDateVal.setText(na);
		 		}
		 		
		 		//show this stuff only when we have quota that expires
		 		if(expires && limited){
		 			pbUsage.setDottedLine(idealUsagePercentage);
		 			tvIdealUsageVal.setText(DataunitUtils.formatData(idealUsageVal,getBaseContext()));
		 			String desText = (idealMinusActual > 0) ? res.getString(R.string.lessthanideal) :res.getString(R.string.morethanideal);
		 			tvUsageDiffVal.setText(String.format("%s %s",DataunitUtils.formatData(Math.abs(idealMinusActual),getBaseContext()),desText));
		 			tvProjectedUsageVal.setText(DataunitUtils.formatData(predictedUsage,getBaseContext()));
		 			String overageLikely = (idealMinusActual < 0) ? res.getString(R.string.yes) : res.getString(R.string.no);    
		 			tvOverageLikelyVal.setText(overageLikely);
		 			tvDailyQuotaRemVal.setText(DataunitUtils.formatData(remainingDaily, getBaseContext()));
		 		}
		 		else{
		 			tvIdealUsageVal.setText(na);
		 			tvUsageDiffVal.setText(na);
		 			tvProjectedUsageVal.setText(na);
		 			tvOverageLikelyVal.setText(na);
		 			tvDailyQuotaRemVal.setText(na);			    
		 		}
		 		//TODO load plan usage details here?
		 		Log.i(LOG_TAG,"set view completeted executing in background.");
		 		loadAd();
		 		
	     }
		
	 }
	 
	

	@Override
	public void onResume() {
		IntentFilter filter;
		filter = new IntentFilter(UsageRefreshWorker.USAGE_UPDATED);
		_receiver = new DataUsageReceiver();
		ContextCompat.registerReceiver(this, _receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
		//updatePrefs();
		Log.i("PlanUsageCall","called from onResume");
		new SetViewTask().executeTask();
		super.onResume();
		Log.i(LOG_TAG,"PlanUsageActivity onResume Called");
	}

	
	@Override
	public void onPause() {
		if (_receiver != null) {
			unregisterReceiver(_receiver);
			_receiver = null;
		}
		super.onPause();
		Log.i(LOG_TAG,"PlanUsageActivity onPause Called");
	}

	@Override
	protected void onDestroy() {
		_backgroundExecutor.shutdown();
		super.onDestroy();
	}

	public class DataUsageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.i(LOG_TAG,"DataUsageReceiver > BroadcastReceiver onReceive Called");
			new SetViewTask().executeTask();
		}
	}

}

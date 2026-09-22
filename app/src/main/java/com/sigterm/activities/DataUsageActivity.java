package com.sigterm.activities;

import com.j256.ormlite.android.apptools.OrmLiteBaseTabActivity;
import com.sigterm.R;
import com.sigterm.domainservices.UsageCounterService;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.enums.LaunchScreenPrefs;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.QuotaUnit;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.services.RefreshScheduler;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.PreferenceUtils;
import com.sigterm.util.RatingHelper;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Debug;
import androidx.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.os.Build;
import com.google.android.gms.ads.MobileAds;
import androidx.core.content.ContextCompat;

public class DataUsageActivity extends OrmLiteBaseTabActivity<DatabaseHelper> {

	private final String LOG_TAG = getClass().getSimpleName();
	private final int MENU_ADJUST_RESET = 0;
	private final int MENU_HELP = 1;
	Intent _adjustResetIntent;
	private UsageCounterService _usageCounterService;
	private DatabaseHelper _helper;
	private UsageCounter _currentCounter;
	public final static String USAGE_ADJUSTED = "USAGE_ADJUSTED";

	// private DataUsageService _dataUsageService;

	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		//Debug.startMethodTracing("DataUsageActivity");
		super.onCreate(savedInstanceState);
		//TODO rating 26% time remove?
		RatingHelper.app_launched(this);
		MobileAds.initialize(this, initializationStatus -> { });
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
				&& checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
				!= android.content.pm.PackageManager.PERMISSION_GRANTED) {
			requestPermissions(new String[] {android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
		}
		setContentView(R.layout.main);
		initDataUsageService();
		setupTabs();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		// _dataUsageService = new DataUsageService(getHelper());
		_helper = getHelper();
		_usageCounterService = new UsageCounterService(_helper);
		_currentCounter = _usageCounterService.getDefaultCellularBillingCounter();
		//Debug.stopMethodTracing();
	}

	private void setupTabs() {
		Resources res = getResources();
		// Resource object to get Drawables
		final TabHost tabHost = getTabHost();
		// The activity TabHost
		TabHost.TabSpec spec;
		// Resusable TabSpec for each tab
		Intent intent;
		// Reusable Intent for each tab
		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, PlanUsageActivity.class);
		intent.putExtra(NetworkDataType.class.getSimpleName(), NetworkDataType.CELLULAR);
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("cellular")
				.setIndicator(createTabIndicator(res.getString(R.string.Cellular), R.drawable.ic_tab_cellular))
				.setContent(intent);
		tabHost.addTab(spec);
		// Do the same for the other tabs
		intent = new Intent().setClass(this, PlanUsageActivity.class);
		intent.putExtra(NetworkDataType.class.getSimpleName(), NetworkDataType.WIFI);
		spec = tabHost.newTabSpec("wifi")
				.setIndicator(createTabIndicator(res.getString(R.string.WiFi), R.drawable.ic_tab_wifi)).setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, HistoryActivity.class);
		spec = tabHost.newTabSpec("history")
				.setIndicator(createTabIndicator(res.getString(R.string.History), R.drawable.ic_tab_history))
				.setContent(intent);
		tabHost.addTab(spec);

		intent = new Intent().setClass(this, SettingsActivity.class);
		spec = tabHost.newTabSpec("settings")
				.setIndicator(createTabIndicator(res.getString(R.string.Settings), R.drawable.ic_tab_settings))
				.setContent(intent);
		tabHost.addTab(spec);

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		String launchScreenPrefValue = sp.getString(PrefKeys.LAUNCH_SCREEN_PREF, LaunchScreenPrefs.CELLULAR);
		if (launchScreenPrefValue.equals(LaunchScreenPrefs.WIFI))
			tabHost.setCurrentTab(1);
		else
			tabHost.setCurrentTab(0);
		tabHost.setOnTabChangedListener(tabId -> updateTabIndicators(tabHost, tabId));
		updateTabIndicators(tabHost, tabHost.getCurrentTabTag());

		// if this is first execution of program display a message and go to
		// Settings tab
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		int setupDone = PreferenceUtils.GetIntPreference(PrefKeys.INITIAL_SETUP_DONE,0, getApplicationContext());
		if (setupDone == 0) {
			builder.setMessage(res.getString(R.string.InitialSetupMsg));
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					PreferenceUtils.SetIntPreference(PrefKeys.INITIAL_SETUP_DONE,1, getApplicationContext());
					tabHost.setCurrentTab(3);
				}
			});
			AlertDialog alertDialog = builder.create();
			alertDialog.show();
		}
	}

	private void updateTabIndicators(TabHost tabHost, String selectedTag) {
		String[] tags = {"cellular", "wifi", "history", "settings"};
		for (int i = 0; i < tags.length; i++) {
			View child = tabHost.getTabWidget().getChildAt(i);
			if (child != null) setSelectedRecursively(child, tags[i].equals(selectedTag));
		}
	}

	private void setSelectedRecursively(View view, boolean selected) {
		view.setSelected(selected);
		if (view.getId() == R.id.tabIndicator) {
			view.setBackgroundResource(selected ? R.drawable.bg_nav_item_selected : R.drawable.bg_nav_item_unselected);
		}
		if (view.getId() == R.id.tabLabel && view instanceof TextView) {
			((TextView) view).setTextColor(ContextCompat.getColor(this,
					selected ? R.color.brand_primary : R.color.text_muted));
		}
		if (view.getId() == R.id.tabIcon && view instanceof ImageView) {
			((ImageView) view).setColorFilter(ContextCompat.getColor(this,
					selected ? R.color.brand_primary : R.color.text_muted),
					android.graphics.PorterDuff.Mode.SRC_IN);
		}
		if (view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			for (int i = 0; i < group.getChildCount(); i++) {
				setSelectedRecursively(group.getChildAt(i), selected);
			}
		}
	}

	private View createTabIndicator(String label, int iconResId) {
		View indicator = LayoutInflater.from(this).inflate(R.layout.tab_indicator, null, false);
		ImageView icon = indicator.findViewById(R.id.tabIcon);
		TextView text = indicator.findViewById(R.id.tabLabel);
		icon.setImageResource(iconResId);
		text.setText(label);
		return indicator;
	}

	@Override
	protected void onPause() {
		super.onPause();
	}// End of onPause

	@Override
	protected void onResume() {
		super.onResume();
	}// End of onResume

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ADJUST_RESET, 0, getResources().getString(R.string.menu_title_adjustreset)).setIcon(
				R.drawable.ic_menu_settings);
		menu.add(0, MENU_HELP, 1, getResources().getString(R.string.Help)).setIcon(android.R.drawable.ic_menu_help);
		return true;
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean handled = false;
		// Intent intent = new Intent(this, DataUsageActivity.class);

		switch (item.getItemId()) {
			case MENU_ADJUST_RESET:
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				View view = LayoutInflater.from(this)
						.inflate(R.layout.adjustreset, null);
			builder.setView(view);
			final CheckBox cbReset = (CheckBox) view.findViewById(R.id.cbReset);
			final EditText tbSent = (EditText) view.findViewById(R.id.tbSent);
			final EditText tbRecd = (EditText) view.findViewById(R.id.tbRecd);
			final Spinner spinnerRecd = (Spinner) view.findViewById(R.id.spinnerRecd);
			final Spinner spinnerSent = (Spinner) view.findViewById(R.id.spinnerSent);

			final RadioButton rbCellular = (RadioButton) view.findViewById(R.id.rdCellular);
			final RadioButton rbWiFi = (RadioButton) view.findViewById(R.id.rdWiFi);

			setView(tbRecd, tbSent, spinnerRecd, spinnerSent, rbCellular, rbWiFi, cbReset);
			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					if (cbReset.isChecked()) {
						if (rbCellular.isChecked()) {
							_usageCounterService.resetCellularUsage(getApplicationContext(),_currentCounter);
						} else if (rbWiFi.isChecked()) {
							_usageCounterService.resetWiFiUsage(getApplicationContext(),_currentCounter);
						}
					} else {
						long bytesSent = 0;
						long bytesRecd = 0;
						String[] units = getResources().getStringArray(R.array.entries_dataunits);
						if (tbSent.getText().length() > 0) {
							int selectedUnit = (spinnerSent.getSelectedItemPosition() > 0) ? spinnerSent
									.getSelectedItemPosition() : 0;
							String unit = units[selectedUnit];
							float data;
							try { data = Float.parseFloat(tbSent.getText().toString()); }
							catch (NumberFormatException error) { return; }
							if (!Float.isFinite(data) || data < 0) return;
							bytesSent = DataunitUtils.getBytesFromFormattedData(data, unit);
						}
						if (tbRecd.getText().length() > 0) {
							int selectedUnit = (spinnerRecd.getSelectedItemPosition() > 0) ? spinnerRecd
									.getSelectedItemPosition() : 0;
							String unit = units[selectedUnit];
							float data;
							try { data = Float.parseFloat(tbRecd.getText().toString()); }
							catch (NumberFormatException error) { return; }
							if (!Float.isFinite(data) || data < 0) return;
							bytesRecd = DataunitUtils.getBytesFromFormattedData(data, unit);
						}

						if (rbCellular.isChecked()) {
							_usageCounterService.adjustCellularCounter(_currentCounter, bytesSent, bytesRecd);
							_usageCounterService.resetCellularNotificationKeys(getApplicationContext());
						} else if (rbWiFi.isChecked()) {
							_usageCounterService.adjustWiFiCounter(_currentCounter, bytesSent, bytesRecd);
							_usageCounterService.resetWiFiNotificationKeys(getApplicationContext());
						}
					}
					refreshPlanUsageView();
					

				}
			});
			builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int arg1) {

				}
			});
			AlertDialog alertDialog = builder.create();

			alertDialog.show();
			handled = true;

			break;

		case MENU_HELP:
			Intent intentHelp = new Intent(this, HelpActivity.class);
			startActivity(intentHelp);
			handled = true;
			break;
		}
		return handled;
	}

	// Send an Intent with an action named "custom-event-name". The Intent sent
	// should
	// be received by the ReceiverActivity.
	private void refreshPlanUsageView() {
		Log.d("sender", "Broadcasting message");
		Intent intent = new Intent(com.sigterm.services.UsageRefreshWorker.USAGE_UPDATED).setPackage(getPackageName());
		sendBroadcast(intent);
	}

	private void setView(final EditText tbRecd, final EditText tbSent, final Spinner spinnerRecd,
			final Spinner spinnerSent, final RadioButton rbCellular, final RadioButton rbWiFi, final CheckBox cbReset) {

		// set defaults
		setCellularData(tbRecd, tbSent, spinnerRecd, spinnerSent);
		// set cellular and wifi radio button events
		rbCellular.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					setCellularData(tbRecd, tbSent, spinnerRecd, spinnerSent);

			}
		});
		rbWiFi.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					setWiFiData(tbRecd, tbSent, spinnerRecd, spinnerSent);
			}
		});

		// reset logic

		cbReset.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					tbRecd.setText("0.0");
					tbSent.setText("0.0");
				} else {
					if (rbCellular.isChecked())
						setCellularData(tbRecd, tbSent, spinnerRecd, spinnerSent);
					else if (rbWiFi.isChecked())
						setWiFiData(tbRecd, tbSent, spinnerRecd, spinnerSent);
				}
			}
		});
	}

	private void setWiFiData(final EditText tbRecd, final EditText tbSent, final Spinner spinnerRecd,
			final Spinner spinnerSent) {
		_currentCounter = _usageCounterService.getDefaultWiFiBillingCounter();

		String dataRecd = DataunitUtils.formatData(_currentCounter.getWifiBytesRecd(_helper), getBaseContext());
		if (dataRecd != null && dataRecd.length() > 0) {
			String[] dataPartsRecd = dataRecd.split(" ");
			if (dataPartsRecd.length == 2) {
				tbRecd.setText(dataPartsRecd[0].toString());
				String unitRecd = dataPartsRecd[1].trim();
				if (unitRecd.equals(QuotaUnit.KB))
					spinnerRecd.setSelection(0);
				else if (unitRecd.equals(QuotaUnit.MB))
					spinnerRecd.setSelection(1);
				else if (unitRecd.equals(QuotaUnit.GB))
					spinnerRecd.setSelection(2);
			}
		} else {
			tbRecd.setText("0");
			spinnerRecd.setSelection(1);
		}
		String dataSent = DataunitUtils.formatData(_currentCounter.getWifiBytesSent(_helper), getBaseContext());
		if (dataSent != null && dataSent.length() > 0) {
			String[] dataPartsSent = dataSent.split(" ");
			if (dataPartsSent.length == 2) {
				if (dataPartsSent[0] != null)
					tbSent.setText(dataPartsSent[0].toString());
				else
					tbSent.setText("0.0");
				String unitSent = dataPartsSent[1].trim();
				if (dataPartsSent[1] != null) {
					if (unitSent.equals(QuotaUnit.KB))
						spinnerSent.setSelection(0);
					else if (unitSent.equals(QuotaUnit.MB))
						spinnerSent.setSelection(1);
					else if (unitSent.equals(QuotaUnit.GB))
						spinnerSent.setSelection(2);
				} else {
					spinnerRecd.setSelection(1);
				}
			}
		} else {
			tbSent.setText("0");
			spinnerSent.setSelection(1);
		}
	}

	private void setCellularData(final EditText tbRecd, final EditText tbSent, final Spinner spinnerRecd,
			final Spinner spinnerSent) {
		_currentCounter = _usageCounterService.getDefaultCellularBillingCounter();

		String dataRecd = DataunitUtils.formatData(_currentCounter.getCellularBytesRecd(_helper), getBaseContext());

		if (dataRecd != null && dataRecd.length() > 0) {
			String[] dataPartsRecd = dataRecd.split(" ");
			if (dataPartsRecd.length == 2) {
				if (tbRecd == null)
					Log.i(LOG_TAG, "tbRecd is null");
				if (dataPartsRecd[0] != null)
					tbRecd.setText(dataPartsRecd[0].toString());
				else
					tbRecd.setText("0.0");

				String unitRecd = dataPartsRecd[1].trim();
				if (dataPartsRecd[1] != null) {
					if (unitRecd.equals(QuotaUnit.KB))
						spinnerRecd.setSelection(0);
					else if (unitRecd.equals(QuotaUnit.MB))
						spinnerRecd.setSelection(1);
					else if (unitRecd.equals(QuotaUnit.GB))
						spinnerRecd.setSelection(2);
				} else {
					spinnerRecd.setSelection(1);
				}
			}
		} else {
			tbRecd.setText("0");
			spinnerRecd.setSelection(1);
		}
		String dataSent = DataunitUtils.formatData(_currentCounter.getCellularBytesSent(_helper), getBaseContext());
		if (dataSent != null && dataSent.length() > 0) {
			String[] dataPartsSent = dataSent.split(" ");
			if (dataPartsSent.length == 2) {
				tbSent.setText(dataPartsSent[0].toString());
				String unitSent = dataPartsSent[1].trim();
				if (unitSent.equals(QuotaUnit.KB))
					spinnerSent.setSelection(0);
				else if (unitSent.equals(QuotaUnit.MB))
					spinnerSent.setSelection(1);
				else if (unitSent.equals(QuotaUnit.GB))
					spinnerSent.setSelection(2);
			}
		} else {
			tbSent.setText("0");
			spinnerSent.setSelection(1);
		}

	}

	public void initDataUsageService() {
		RefreshScheduler.refreshNow(this);
	}

}

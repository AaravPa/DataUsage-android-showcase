package com.sigterm.activities;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.sigterm.R;
import com.sigterm.domainservices.UsageCounterService;
import com.sigterm.entities.UsageCounter;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.QuotaUnit;
import com.sigterm.entities.enums.RepeatInterval;
import com.sigterm.orm.sqlite.DatabaseHelper;
import com.sigterm.preference.QuotaPreference;
import com.sigterm.services.RefreshScheduler;
import com.sigterm.util.DateUtils;
import com.sigterm.util.PreferenceUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

public class SettingsActivity extends FragmentActivity {
	private DatabaseHelper helper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		helper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		setContentView(R.layout.settings_container);
		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.settings_container, new SettingsFragment()).commit();
		}
	}

	DatabaseHelper getHelper() { return helper; }

	@Override
	protected void onDestroy() {
		OpenHelperManager.releaseHelper();
		helper = null;
		super.onDestroy();
	}

	public static class SettingsFragment extends PreferenceFragmentCompat
			implements SharedPreferences.OnSharedPreferenceChangeListener {
		private UsageCounterService counterService;

		@Override
		public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
			setPreferencesFromResource(R.xml.settings, rootKey);
			SettingsActivity activity = (SettingsActivity) requireActivity();
			counterService = new UsageCounterService(activity.getHelper());
			refreshPreferences();
		}

		@Override
		public void onResume() {
			super.onResume();
			getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
			refreshPreferences();
		}

		@Override
		public void onPause() {
			getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
			super.onPause();
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
			if (PrefKeys.UPDATE_FREQ_PREF.equals(key)) RefreshScheduler.schedulePeriodic(requireContext());
			refreshPreferences();
		}

		private void refreshPreferences() {
			if (getPreferenceManager().getSharedPreferences() == null || counterService == null) return;
			SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
			setListSummary(PrefKeys.DATAUNIT_DISPLAY_PREF);
			setListSummary(PrefKeys.LAUNCH_SCREEN_PREF);
			setListSummary(PrefKeys.WEEK_STATS_ON);
			setListSummary(PrefKeys.YELLOW_PREF);
			setListSummary(PrefKeys.RED_PREF);
			setListSummary(PrefKeys.UPDATE_FREQ_PREF);
			setEditSummary(PrefKeys.ALERT1_PREF, "first alert on %s%% of Used", preferences.getString(PrefKeys.ALERT1_PREF, "50"));
			setEditSummary(PrefKeys.ALERT2_PREF, "second alert on %s%% of Used", preferences.getString(PrefKeys.ALERT2_PREF, "75"));
			setEditSummary(PrefKeys.ALERT3_PREF, "third alert on %s%% of Used", preferences.getString(PrefKeys.ALERT3_PREF, "90"));
			setQuotaSummary(PrefKeys.CELLULAR_QUOTA_PREF, PrefKeys.CELLULAR_UNLIMITED, PrefKeys.CELLULAR_QUOTA,
					PrefKeys.CELLULAR_UNIT, PrefKeys.CELLULAR_NOEXPIRY, PrefKeys.CELLULAR_REPEATFREQUENCY,
					PrefKeys.CELLULAR_REPEATINTERVAL);
			setQuotaSummary(PrefKeys.WIFI_QUOTA_PREF, PrefKeys.WIFI_UNLIMITED, PrefKeys.WIFI_QUOTA,
					PrefKeys.WIFI_UNIT, PrefKeys.WIFI_NOEXPIRY, PrefKeys.WIFI_REPEATFREQUENCY,
					PrefKeys.WIFI_REPEATINTERVAL);
			updateDefaultCounter(true);
			updateDefaultCounter(false);
		}

		private void setListSummary(String key) {
			ListPreference preference = findPreference(key);
			if (preference != null && preference.getEntry() != null) preference.setSummary(preference.getEntry());
		}

		private void setEditSummary(String key, String format, String value) {
			EditTextPreference preference = findPreference(key);
			if (preference != null) preference.setSummary(String.format(format, value));
		}

		private void setQuotaSummary(String preferenceKey, String unlimitedKey, String quotaKey, String unitKey,
				String noExpiryKey, String repeatFrequencyKey, String repeatIntervalKey) {
			SharedPreferences preferences = getPreferenceManager().getSharedPreferences();
			QuotaPreference preference = findPreference(preferenceKey);
			if (preference == null) return;
			String summary = preferences.getBoolean(unlimitedKey, true)
					? getString(R.string.unlimited_plan)
					: preferences.getFloat(quotaKey, 0) + " " + preferences.getString(unitKey, QuotaUnit.MB);
			if (preferences.getBoolean(noExpiryKey, true)) {
				summary += " " + getString(R.string.no_expiry);
			} else {
				summary += " " + getString(R.string.Per) + " "
						+ preferences.getInt(repeatFrequencyKey, 0) + " "
						+ preferences.getString(repeatIntervalKey, RepeatInterval.MONTHS);
			}
			preference.setSummary(summary);
		}

		private void updateDefaultCounter(boolean cellular) {
			android.content.Context context = requireContext();
			UsageCounter counter = cellular ? counterService.getDefaultCellularBillingCounter()
					: counterService.getDefaultWiFiBillingCounter();
			String unlimitedKey = cellular ? PrefKeys.CELLULAR_UNLIMITED : PrefKeys.WIFI_UNLIMITED;
			String quotaKey = cellular ? PrefKeys.CELLULAR_QUOTA : PrefKeys.WIFI_QUOTA;
			String unitKey = cellular ? PrefKeys.CELLULAR_UNIT : PrefKeys.WIFI_UNIT;
			String noExpiryKey = cellular ? PrefKeys.CELLULAR_NOEXPIRY : PrefKeys.WIFI_NOEXPIRY;
			String startKey = cellular ? PrefKeys.CELLULAR_STARTDATE : PrefKeys.WIFI_STARTDATE;
			String frequencyKey = cellular ? PrefKeys.CELLULAR_REPEATFREQUENCY : PrefKeys.WIFI_REPEATFREQUENCY;
			String intervalKey = cellular ? PrefKeys.CELLULAR_REPEATINTERVAL : PrefKeys.WIFI_REPEATINTERVAL;
			SharedPreferences preferences = getPreferenceManager().getSharedPreferences();

			boolean changed = false;
			if (preferences.getBoolean(unlimitedKey, true)) {
				if (counter.getQuotaInBytes() != 0) { counter.setQuota(0); counter.setQuotaInBytes(0); changed = true; }
			} else {
				float quota = preferences.getFloat(quotaKey, 0);
				String unit = preferences.getString(unitKey, QuotaUnit.MB);
				long bytes = cellular ? PreferenceUtils.GetCellularQuotaInBytes(context) : PreferenceUtils.GetWiFiQuotaInBytes(context);
				if (counter.getQuotaInBytes() != bytes || !Objects.equals(counter.getQuotaUnit(), unit)) {
					counter.setQuota(quota);
					counter.setQuotaUnit(unit);
					counter.setQuotaInBytes(bytes);
					changed = true;
				}
			}

			boolean noExpiry = preferences.getBoolean(noExpiryKey, true);
			if (noExpiry) {
				if (counter.getStartDateTime() != null || counter.getEndDateTime() != null) {
					counter.setStartDateTime(null); counter.setEndDateTime(null); changed = true;
				}
			} else {
				Date start = new Date(preferences.getLong(startKey, DateUtils.getToday().getTime()));
				int frequency = preferences.getInt(frequencyKey, 1);
				String interval = preferences.getString(intervalKey, RepeatInterval.MONTHS);
				if (counter.getStartDateTime() == null || counter.getEndDateTime() == null
						|| !counter.getStartDateTime().equals(start) || counter.getRepeatFrequency() != frequency
						|| !Objects.equals(counter.getRepeatInterval(), interval)) {
					counter.setStartDateTime(start);
					counter.setRepeatFrequency(frequency);
					counter.setRepeatInterval(interval);
					counter.setEndDateTime(counterService.getNewEndDate(frequency, interval, start));
					changed = true;
				}
			}

			if (changed) {
				counterService.updateCounter(counter);
				if (cellular) counterService.resetCellularNotificationKeys(context);
				else counterService.resetWiFiNotificationKeys(context);
			}
		}
	}
}

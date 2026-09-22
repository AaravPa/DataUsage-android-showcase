package com.sigterm.preference;

import java.util.Calendar;
import java.util.Date;

import com.sigterm.R;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.RepeatInterval;
import com.sigterm.util.DateUtils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import androidx.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Spinner;

public class QuotaPreference extends Preference {
	private boolean unlimited = true;
	private float quota = 0;
	private int repeatFrequency = 0;
	private String unit;
	private String repeatInterval;
	private boolean noExpiry = true;
	private View view;
	private Date startDate;
	private final String LOG_TAG = getClass().getSimpleName();
	Button btnPickDate;

	// private OnQuotaChangedListener dialogQuotaChangedListener = null;
	//
	// /**
	// * Interface describing a color change listener.
	// */
	// public interface OnQuotaChangedListener {
	// void quotaChanged(float quota, String unit, float repeatFrequency,
	// String repeatInterval);
	// }

	public QuotaPreference(Context context) {
		super(context, null);
	}

	public QuotaPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public QuotaPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onClick() {
		super.onClick();
		Builder builder = new AlertDialog.Builder(getContext());
		prepareDialog(builder);
		builder.setPositiveButton(android.R.string.ok, (dialog, which) -> saveDialog());
		builder.setNegativeButton(android.R.string.cancel, null);
		builder.show();
	}

	private void saveDialog() {
			CheckBox cbNoExpiry = (CheckBox) view.findViewById(R.id.cbNoExpiry);
			noExpiry = cbNoExpiry.isChecked();
			CheckBox cbUnlimited = (CheckBox) view.findViewById(R.id.cbUnlimitedData);
			unlimited = cbUnlimited.isChecked();
			if (!noExpiry) {
				Date candidateStartDate = DateUtils.getDateFromMediumFormatString(btnPickDate.getText().toString());
				if (!validateStartDate(candidateStartDate)) return;
				EditText frequencyField = (EditText) view.findViewById(R.id.editTextRepeatFrequency);
				try {
					repeatFrequency = Integer.parseInt(frequencyField.getText().toString().trim());
				} catch (NumberFormatException error) {
					return;
				}
				if (repeatFrequency <= 0) return;
			}
			if (!unlimited) {
				EditText quotaField = (EditText) view.findViewById(R.id.editTextQuota);
				try {
					quota = Float.parseFloat(quotaField.getText().toString().trim());
				} catch (NumberFormatException error) {
					return;
				}
				if (!Float.isFinite(quota) || quota < 0) return;
			}

			if (!noExpiry) {
				startDate = DateUtils.getDateFromMediumFormatString(btnPickDate.getText().toString());
				Spinner spinnerRepeatInterval = (Spinner) view.findViewById(R.id.spinnerRepeatInterval);
				repeatInterval = spinnerRepeatInterval.getSelectedItem().toString();
			}
			if (!unlimited) {
				Spinner spinnerUnit = (Spinner) view.findViewById(R.id.spinnerUnit);
				unit = spinnerUnit.getSelectedItem().toString();
			}
			persistPrefs();
	}

	private void persistPrefs() {
		if (startDate == null || unit == null || (!noExpiry && repeatInterval == null)) return;
		SharedPreferences.Editor editor = getSharedPreferences().edit();
		// Log.i(LOG_TAG, "Setting Pref key is :" + this.getKey());
		if (this.getKey().equals(PrefKeys.CELLULAR_QUOTA_PREF)) {
			editor.putBoolean(PrefKeys.CELLULAR_UNLIMITED, unlimited);
			// Log.i(LOG_TAG, "Setting Pref cellular_unlimited  is : " +
			// String.valueOf(unlimited));
			editor.putFloat(PrefKeys.CELLULAR_QUOTA, quota);
			// Log.i(LOG_TAG, "Setting Pref cellular quota is : " +
			// String.valueOf(quota));
			editor.putString(PrefKeys.CELLULAR_UNIT, unit);
			editor.putInt(PrefKeys.CELLULAR_REPEATFREQUENCY, repeatFrequency);
			editor.putString(PrefKeys.CELLULAR_REPEATINTERVAL, repeatInterval);
			editor.putBoolean(PrefKeys.CELLULAR_NOEXPIRY, noExpiry);
			// Log.i(LOG_TAG, "Setting Pref cellular noExpiry is : " +
			// String.valueOf(noExpiry));
			editor.putLong(PrefKeys.CELLULAR_STARTDATE, startDate.getTime());
			editor.apply();
		} else if (this.getKey().equals(PrefKeys.WIFI_QUOTA_PREF)) {
			editor.putBoolean(PrefKeys.WIFI_UNLIMITED, unlimited);
			// Log.i(LOG_TAG, "Setting Pref wifi_unlimited  is : " +
			// String.valueOf(unlimited));
			editor.putFloat(PrefKeys.WIFI_QUOTA, quota);
			editor.putString(PrefKeys.WIFI_UNIT, unit);
			editor.putInt(PrefKeys.WIFI_REPEATFREQUENCY, repeatFrequency);
			editor.putString(PrefKeys.WIFI_REPEATINTERVAL, repeatInterval);
			editor.putBoolean(PrefKeys.WIFI_NOEXPIRY, noExpiry);
			// Log.i(LOG_TAG, "Setting Pref Wifi noExpiry is : " +
			// String.valueOf(noExpiry));
			editor.putLong(PrefKeys.WIFI_STARTDATE, startDate.getTime());
			editor.apply();
		}

	}

	/**
	 * {@inheritDoc}
	 */
	private void prepareDialog(final Builder builder) {

		// dialogQuotaChangedListener = new OnQuotaChangedListener() {
		// /**
		// * {@inheritDoc}
		// */
		// public void quotaChanged(float quota, String unit,
		// float repeatFrequency, String repeatInterval) {
		// QuotaPreference.this.quota = quota;
		// QuotaPreference.this.unit = unit;
		// QuotaPreference.this.repeatInterval = repeatInterval;
		// QuotaPreference.this.repeatFrequency = repeatFrequency;
		// }
		// };
		String unitDefaultValue = this.getContext().getResources().getStringArray(R.array.entries_dataunits)[0];
		String repeatIntervalDefaultValue = this.getContext().getResources()
				.getStringArray(R.array.entries_repeatIntervals)[2];
		Log.i(LOG_TAG, "Getting Pref key is :" + this.getKey());
		if (this.getKey().equals(PrefKeys.CELLULAR_QUOTA_PREF)) {

			unlimited = getSharedPreferences().getBoolean(PrefKeys.CELLULAR_UNLIMITED, true);
			noExpiry = getSharedPreferences().getBoolean(PrefKeys.CELLULAR_NOEXPIRY, true);
			quota = getSharedPreferences().getFloat(PrefKeys.CELLULAR_QUOTA, 0);
			// Log.i(LOG_TAG, "Getting Pref cellular quota is : " +
			// String.valueOf(quota));
			unit = getSharedPreferences().getString(PrefKeys.CELLULAR_UNIT, unitDefaultValue);
			repeatFrequency = getSharedPreferences().getInt(PrefKeys.CELLULAR_REPEATFREQUENCY, 0);

			repeatInterval = getSharedPreferences().getString(PrefKeys.CELLULAR_REPEATINTERVAL,
					repeatIntervalDefaultValue);
			long startDateLong = getSharedPreferences().getLong(PrefKeys.CELLULAR_STARTDATE, 0);
			if (startDateLong == 0)
				startDate = Calendar.getInstance().getTime();
			else
				startDate = new Date(startDateLong);

		} else if (this.getKey().equals(PrefKeys.WIFI_QUOTA_PREF)) {
			unlimited = getSharedPreferences().getBoolean(PrefKeys.WIFI_UNLIMITED, true);
			noExpiry = getSharedPreferences().getBoolean(PrefKeys.WIFI_NOEXPIRY, true);
			quota = getSharedPreferences().getFloat(PrefKeys.WIFI_QUOTA, 0);
			unit = getSharedPreferences().getString(PrefKeys.WIFI_UNIT, unitDefaultValue);
			repeatFrequency = getSharedPreferences().getInt(PrefKeys.WIFI_REPEATFREQUENCY, 0);
			repeatInterval = getSharedPreferences().getString(PrefKeys.WIFI_REPEATINTERVAL, repeatIntervalDefaultValue);
			long startDateLong = getSharedPreferences().getLong(PrefKeys.WIFI_STARTDATE, 0);
			if (startDateLong == 0)
				startDate = Calendar.getInstance().getTime();
			else
				startDate = new Date(startDateLong);
		}

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.quota, new FrameLayout(getContext()), false);

		final EditText editTextQuota = (EditText) view.findViewById(R.id.editTextQuota);
		editTextQuota.setText(String.valueOf(quota));

		final Spinner spinnerUnit = (Spinner) view.findViewById(R.id.spinnerUnit);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> unitArrayAdapter = (ArrayAdapter<String>) spinnerUnit.getAdapter();
		int unitSpinnerPosition = unitArrayAdapter.getPosition(unit);
		spinnerUnit.setSelection(unitSpinnerPosition);

		final EditText editTextRepeatFrequency = (EditText) view.findViewById(R.id.editTextRepeatFrequency);
		editTextRepeatFrequency.setText(String.valueOf(repeatFrequency));
		editTextRepeatFrequency.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() > 0) {
					try {
						repeatFrequency = Integer.parseInt(s.toString());
						btnPickDate.setEnabled((repeatFrequency > 0));
					} catch (NumberFormatException nfe) {
						// its not int so set date picker button to disabled.
						btnPickDate.setEnabled(false);
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// do nothing

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// do nothing

			}
		});
		final Spinner spinnerRepeatInterval = (Spinner) view.findViewById(R.id.spinnerRepeatInterval);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> repeatIntervalArrayAdapter = (ArrayAdapter<String>) spinnerRepeatInterval.getAdapter();
		int repeatIntervalSpinnerPosition = repeatIntervalArrayAdapter.getPosition(repeatInterval);
		spinnerRepeatInterval.setSelection(repeatIntervalSpinnerPosition);
		// add a click listener to the button

		btnPickDate = (Button) view.findViewById(R.id.pickDate);

		btnPickDate.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				DatePickerDialog dpDialog = new DatePickerDialog(getContext(), mDateSetListener, DateUtils
						.getYear(startDate), DateUtils.getMonth(startDate), DateUtils.getDay(startDate));
				dpDialog.show();
			}
		});
		if (startDate == null)
			startDate = DateUtils.getToday();
		Log.i(LOG_TAG, "Start Date  is:" + startDate.toString());
		Log.i(LOG_TAG, "Unlimited:" + unlimited);
		Log.i(LOG_TAG, "noExpiry:" + noExpiry);
		Log.i(LOG_TAG, "quota:" + quota);
		Log.i(LOG_TAG, "repeatFrequency:" + repeatFrequency);
		btnPickDate.setText(DateUtils.getDateInMediumFormat(startDate));

		CheckBox cbUnlimitedPlan = (CheckBox) view.findViewById(R.id.cbUnlimitedData);
		cbUnlimitedPlan.setChecked(unlimited);
		editTextQuota.setEnabled(!cbUnlimitedPlan.isChecked());
		spinnerUnit.setEnabled(!cbUnlimitedPlan.isChecked());

		cbUnlimitedPlan.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				editTextQuota.setEnabled(!isChecked);
				spinnerUnit.setEnabled(!isChecked);
			}
		});

		CheckBox cbNoExpiry = (CheckBox) view.findViewById(R.id.cbNoExpiry);
		cbNoExpiry.setChecked(noExpiry);
		editTextRepeatFrequency.setEnabled(!cbNoExpiry.isChecked());
		spinnerRepeatInterval.setEnabled(!cbNoExpiry.isChecked());
		btnPickDate.setEnabled(!cbNoExpiry.isChecked());
		cbNoExpiry.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				editTextRepeatFrequency.setEnabled(!isChecked);
				spinnerRepeatInterval.setEnabled(!isChecked);
				btnPickDate.setEnabled(!isChecked);
			}
		});

		builder.setView(view);

	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			Date newDate = DateUtils.getDate(year, monthOfYear, dayOfMonth);
			Log.i(LOG_TAG, "new date is:" + newDate.toString());
			validateStartDate(newDate);
		}
	};

	private boolean validateStartDate(Date newDate) {
		if (newDate == null) return false;
		boolean dateOk = true;
		Date today = DateUtils.getToday();
		Date minBillingDate = getMinBillingDateStartDate();
		Log.i(LOG_TAG, "Min BIling date:" + minBillingDate);
		Log.i(LOG_TAG, "new date:" + newDate);
		Calendar minAdjustedBillingDateCal = DateUtils.getCalendarFromDate(minBillingDate);
		minAdjustedBillingDateCal.add(Calendar.MINUTE, -1);
		if (newDate.after(today) || newDate.before(minAdjustedBillingDateCal.getTime()))
			dateOk = false;
		Log.i(LOG_TAG, "is date ok? :" + dateOk);
		if (!dateOk) {

			AlertDialog invalidDateDialog = new AlertDialog.Builder(getContext()).create();
			invalidDateDialog.setTitle(getContext().getResources().getString(R.string.invalid_date_title));
			invalidDateDialog.setMessage(getContext().getResources().getString(R.string.invalid_date_msg) + " "
					+ DateUtils.getDateInMediumFormat(minBillingDate));
			invalidDateDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			invalidDateDialog.show();
			btnPickDate.setText(DateUtils.getDateInMediumFormat(minBillingDate));
		} else {
			btnPickDate.setText(DateUtils.getDateInMediumFormat(newDate));
		}
		return dateOk;
	}

	private Date getMinBillingDateStartDate() {

		Calendar cal = Calendar.getInstance();
		Spinner spinnerRepeatInterval = (Spinner) view.findViewById(R.id.spinnerRepeatInterval);
		repeatInterval = spinnerRepeatInterval.getSelectedItem().toString();

		if (repeatInterval == null) {
			String repeatIntervalDefaultValue = this.getContext().getResources()
					.getStringArray(R.array.entries_repeatIntervals)[2];
			repeatInterval = repeatIntervalDefaultValue;
		}
		Log.i(LOG_TAG, "repeatInterval is:" + repeatInterval);
		// add one day for today.
		if (repeatInterval.equals(RepeatInterval.DAYS)) {
			cal.add(Calendar.DAY_OF_MONTH, -(repeatFrequency - 1));
			Log.i(LOG_TAG, "subtracting days " + repeatFrequency);
		} else if (repeatInterval.equals(RepeatInterval.WEEKS)) {
			int daysToSubtract = ((repeatFrequency) * 7) - 1;
			cal.add(Calendar.DAY_OF_MONTH, -(daysToSubtract));
			Log.i(LOG_TAG, "subtracting days " + daysToSubtract);
		} else if (repeatInterval.equals(RepeatInterval.MONTHS)) {
			cal.add(Calendar.MONTH, -(repeatFrequency));
			cal.add(Calendar.DAY_OF_MONTH, 1);
			Log.i(LOG_TAG, "subtracting months " + repeatFrequency);
		}

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);

		Log.i(LOG_TAG, "Min Billing Start Date is :" + cal.getTime().toString());
		return cal.getTime();
	}
}

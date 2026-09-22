package com.sigterm.views;

import android.content.Context;
import android.view.View;
import androidx.core.content.ContextCompat;

import com.sigterm.R;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PlanHistoryChart {
	public View execute(Context context, List<ArchivedUsageCounter> cellularCounters,
			List<ArchivedUsageCounter> wifiCounters, String networkDataType) {
		List<UsageBarChartView.Series> series = new ArrayList<>();
		List<String> labels = new ArrayList<>();
		if (NetworkDataType.BOTH.equals(networkDataType)) {
			series.add(new UsageBarChartView.Series(context.getString(R.string.Cellular),
					toCellularValues(cellularCounters), ContextCompat.getColor(context, R.color.chart_cellular)));
				series.add(new UsageBarChartView.Series(context.getString(R.string.WiFi),
					toWifiValues(wifiCounters), ContextCompat.getColor(context, R.color.chart_wifi)));
			int count = Math.max(cellularCounters.size(), wifiCounters.size());
			for (int i = 0; i < count; i++) {
				String cell = i < cellularCounters.size() && cellularCounters.get(i).getEndDateTime() != null
						? DateUtils.getDateInShortFormat(cellularCounters.get(i).getEndDateTime()) : "";
				String wifi = i < wifiCounters.size() && wifiCounters.get(i).getEndDateTime() != null
						? DateUtils.getDateInShortFormat(wifiCounters.get(i).getEndDateTime()) : "";
				labels.add(cell.isEmpty() ? wifi : wifi.isEmpty() ? cell : cell + " & " + wifi);
			}
		} else if (NetworkDataType.CELLULAR.equals(networkDataType)) {
			series.add(new UsageBarChartView.Series(context.getString(R.string.Cellular),
					toCellularValues(cellularCounters), ContextCompat.getColor(context, R.color.chart_cellular)));
			addCounterLabels(labels, cellularCounters);
		} else {
			series.add(new UsageBarChartView.Series(context.getString(R.string.WiFi),
					toWifiValues(wifiCounters), ContextCompat.getColor(context, R.color.chart_wifi)));
			addCounterLabels(labels, wifiCounters);
		}
		return new UsageBarChartView(context, context.getString(R.string.billingplanchart),
				context.getString(R.string.billingperiods), context.getString(R.string.MB), series, labels);
	}

	private static float[] toCellularValues(List<ArchivedUsageCounter> counters) {
		float[] values = new float[counters.size()];
		for (int i = 0; i < values.length; i++) values[i] = (float) DataunitUtils.getMBFromBytes(counters.get(i).getCellularBytesTotal());
		return values;
	}

	private static float[] toWifiValues(List<ArchivedUsageCounter> counters) {
		float[] values = new float[counters.size()];
		for (int i = 0; i < values.length; i++) values[i] = (float) DataunitUtils.getMBFromBytes(counters.get(i).getWifiBytesTotal());
		return values;
	}

	private static void addCounterLabels(List<String> labels, List<ArchivedUsageCounter> counters) {
		for (ArchivedUsageCounter counter : counters) {
			labels.add(counter.getEndDateTime() == null ? "" : DateUtils.getDateInShortFormat(counter.getEndDateTime()));
		}
	}
}

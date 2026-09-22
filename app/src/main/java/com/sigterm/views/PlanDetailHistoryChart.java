package com.sigterm.views;

import android.content.Context;
import android.view.View;
import androidx.core.content.ContextCompat;

import com.sigterm.R;
import com.sigterm.entities.UsageLog;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class PlanDetailHistoryChart {
	public View execute(Context context, List<UsageLog> logs, String networkDataType) {
		boolean cellular = NetworkDataType.CELLULAR.equals(networkDataType);
		float[] values = new float[logs == null ? 0 : logs.size()];
		List<String> labels = new ArrayList<>();
		if (logs != null) {
			for (int i = 0; i < logs.size(); i++) {
				UsageLog log = logs.get(i);
				long bytes = cellular ? log.getCellularBytesTotal() : log.getWifiBytesTotal();
				values[i] = (float) DataunitUtils.getMBFromBytes(bytes);
				labels.add(log.getLogDate() == null ? "" : DateUtils.getDateInShortFormat(log.getLogDate()));
			}
		}
		List<UsageBarChartView.Series> series = new ArrayList<>();
		series.add(new UsageBarChartView.Series(context.getString(R.string.DailyLogTitle), values,
				ContextCompat.getColor(context, cellular ? R.color.chart_cellular : R.color.chart_wifi)));
		return new UsageBarChartView(context, context.getString(R.string.DailyLogTitle),
				context.getString(R.string.billingperiods), context.getString(R.string.MB), series, labels);
	}
}

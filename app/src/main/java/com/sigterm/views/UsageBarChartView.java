package com.sigterm.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.util.AttributeSet;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Lightweight chart view for the usage histories. */
public final class UsageBarChartView extends View {
	public static final class Series {
		public final String title;
		public final float[] values;
		public final int color;

		public Series(String title, float[] values, int color) {
			this.title = title;
			this.values = values;
			this.color = color;
		}
	}

	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final RectF barRect = new RectF();
	private final String title;
	private final String xAxisTitle;
	private final String yAxisTitle;
	private final List<Series> series;
	private final String[] labels;

	public UsageBarChartView(Context context, String title, String xAxisTitle, String yAxisTitle,
			List<Series> series, List<String> labels) {
		super(context);
		this.title = title;
		this.xAxisTitle = xAxisTitle;
		this.yAxisTitle = yAxisTitle;
		this.series = Collections.unmodifiableList(new ArrayList<>(series));
		this.labels = labels.toArray(new String[0]);
		setBackgroundColor(ContextCompat.getColor(context, com.sigterm.R.color.surface));
	}

	public UsageBarChartView(Context context) {
		this(context, "", "", "", Collections.emptyList(), Collections.emptyList());
	}

	public UsageBarChartView(Context context, AttributeSet attrs) {
		this(context);
	}

	public UsageBarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		float density = getResources().getDisplayMetrics().density;
		float left = 48 * density;
		float right = 12 * density;
		float top = 34 * density;
		float bottom = getHeight() - 40 * density;
		float width = Math.max(1, getWidth() - left - right);
		float height = Math.max(1, bottom - top);
		int groupCount = labels.length;
		for (Series item : series) groupCount = Math.max(groupCount, item.values.length);

		float max = 1;
		for (Series item : series) for (float value : item.values) max = Math.max(max, value);

		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth(density);
		paint.setColor(ContextCompat.getColor(getContext(), com.sigterm.R.color.divider));
		canvas.drawLine(left, top, left, bottom, paint);
		canvas.drawLine(left, bottom, left + width, bottom, paint);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextAlign(Paint.Align.CENTER);
		paint.setTextSize(14 * density);
		paint.setColor(ContextCompat.getColor(getContext(), com.sigterm.R.color.text_secondary));
		canvas.drawText(title, left + width / 2, 18 * density, paint);
		paint.setTextSize(10 * density);
		canvas.drawText(xAxisTitle, left + width / 2, getHeight() - 8 * density, paint);
		paint.setTextAlign(Paint.Align.RIGHT);
		canvas.drawText(yAxisTitle, left - 5 * density, top - 5 * density, paint);
		canvas.drawText(String.valueOf(Math.round(max)), left - 5 * density, top + 4 * density, paint);

		if (groupCount == 0) return;
		float groupWidth = width / groupCount;
		float barWidth = Math.max(2 * density, groupWidth / Math.max(2, series.size() + 1));
		for (int group = 0; group < groupCount; group++) {
			float start = left + group * groupWidth + (groupWidth - barWidth * series.size()) / 2;
			for (int index = 0; index < series.size(); index++) {
				Series item = series.get(index);
				float value = group < item.values.length ? Math.max(0, item.values[group]) : 0;
				float barHeight = height * value / max;
				paint.setColor(item.color);
				barRect.set(start + index * barWidth, bottom - barHeight,
						start + (index + 1) * barWidth - density, bottom);
				canvas.drawRect(barRect, paint);
			}
			paint.setColor(ContextCompat.getColor(getContext(), com.sigterm.R.color.text_secondary));
			paint.setTextAlign(Paint.Align.CENTER);
			paint.setTextSize(9 * density);
			if (group < labels.length) canvas.drawText(labels[group], left + group * groupWidth + groupWidth / 2,
					bottom + 14 * density, paint);
		}

		float legendX = left;
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTextSize(10 * density);
		for (Series item : series) {
			paint.setColor(item.color);
			canvas.drawRect(legendX, top - 19 * density, legendX + 8 * density, top - 11 * density, paint);
			paint.setColor(ContextCompat.getColor(getContext(), com.sigterm.R.color.text_secondary));
			canvas.drawText(item.title, legendX + 12 * density, top - 11 * density, paint);
			legendX += 12 * density + paint.measureText(item.title) + 14 * density;
		}
	}
}

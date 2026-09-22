package com.sigterm.views;

import com.sigterm.R;
import com.sigterm.entities.ArchivedUsageCounter;
import com.sigterm.entities.enums.NetworkDataType;

import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class HistoryListAdapter extends ArrayAdapter<ArchivedUsageCounter> {
	private final Context context;
	private final ArchivedUsageCounter[] values;

	public HistoryListAdapter(Context context, ArchivedUsageCounter[] values) {
		super(context, R.layout.billinghistorylistitem, values);
		this.context = context;
		this.values = values;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ArchivedUsageCounter counter = values[position];
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View rowView = convertView != null ? convertView : inflater.inflate(
					R.layout.billinghistorylistitem, parent, false);

			TextView tvDates = (TextView) rowView.findViewById(R.id.tvDates);
			ImageView imageView = (ImageView) rowView
					.findViewById(R.id.imgView);
			TextView tvTotalUsage = (TextView) rowView
					.findViewById(R.id.tvTotalUsage);
			// TextView tvDetail = (TextView)
			// rowView.findViewById(R.id.tvDetail);
			TextView tvNetworkType = (TextView) rowView
					.findViewById(R.id.tvNetworkType);
			TextView tvStartDate = (TextView) rowView
					.findViewById(R.id.tvStartDate);
			TextView tvEndDate = (TextView) rowView
					.findViewById(R.id.tvEndDate);
			TextView tvSent = (TextView) rowView
					.findViewById(R.id.tvSent);
			TextView tvReceived = (TextView) rowView
					.findViewById(R.id.tvReceived);

			String dateText = DateUtils.getDateInShortFormat(counter
					.getStartDateTime())
					+ " - "
					+ DateUtils.getDateInShortFormat(counter.getEndDateTime());
			tvStartDate.setText(DateUtils.getDateInMediumFormat(counter.getStartDateTime()));
			tvEndDate.setText(DateUtils.getDateInMediumFormat(counter.getEndDateTime()));
			tvDates.setText(dateText);
			// Change the icon for Windows and iPhone
			tvNetworkType.setText(counter.getNetworkDataType());
			if (counter.getNetworkDataType().equals(NetworkDataType.CELLULAR)) {
				imageView.setImageResource(R.drawable.ic_tab_cellular);
				tvTotalUsage.setText(DataunitUtils.formatData(
						counter.getCellularBytesTotal(), rowView.getContext()));
				tvSent.setText(DataunitUtils.formatData(
						counter.getCellularBytesSent(), rowView.getContext()));
				tvReceived.setText(DataunitUtils.formatData(
						counter.getCellularBytesRecd(), rowView.getContext()));
			} else {
				imageView.setImageResource(R.drawable.ic_tab_wifi);
				tvTotalUsage.setText(DataunitUtils.formatData(
						counter.getWifiBytesTotal(), rowView.getContext()));
				tvSent.setText(DataunitUtils.formatData(
						counter.getWifiBytesSent(), rowView.getContext()));
				tvReceived.setText(DataunitUtils.formatData(
						counter.getWifiBytesRecd(), rowView.getContext()));
			}
			// tvDetail.setText(">");

//			if (tvCounterId != null)
//				tvCounterId.setText(String.valueOf(counter.getCounterId()));
//			else
//				Log.i("HistoryListAdapter", "Counter id text view null");
		return rowView;
	}

}

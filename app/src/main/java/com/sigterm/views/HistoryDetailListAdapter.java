package com.sigterm.views;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sigterm.R;
import com.sigterm.entities.UsageLog;
import com.sigterm.entities.enums.NetworkDataType;
import com.sigterm.util.DataunitUtils;
import com.sigterm.util.DateUtils;

public class HistoryDetailListAdapter extends ArrayAdapter<UsageLog> {
	private final Context context;
	private final UsageLog[] values;
	private final boolean isCellular;
	
	public HistoryDetailListAdapter(Context context, UsageLog[] values,boolean isCellular) {
		super(context,  R.layout.billinghistorydetaillistitem, values);
		this.context = context;
		this.values = values;
		this.isCellular = isCellular;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		UsageLog log = values[position];
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = convertView != null ? convertView : inflater.inflate(
				R.layout.billinghistorydetaillistitem, parent, false);
		TextView tvDates = (TextView) rowView.findViewById(R.id.tvDates);
		TextView tvTotalUsage = (TextView) rowView.findViewById(R.id.tvTotalUsage);
		
		String dateText = DateUtils.getDateInShortFormat(log.getLogDate());
		tvDates.setText(dateText);
		// Change the icon for Windows and iPhone
		
		if (isCellular) {
			Log.i("HistoryDetailListAdapter",DataunitUtils.formatData(log.getCellularBytesTotal(),rowView.getContext()));
			tvTotalUsage.setText(DataunitUtils.formatData(log.getCellularBytesTotal(),rowView.getContext()));
		} else {
			Log.i("HistoryDetailListAdapter",DataunitUtils.formatData(log.getWifiBytesTotal(),rowView.getContext()));
			tvTotalUsage.setText(DataunitUtils.formatData(log.getWifiBytesTotal(),rowView.getContext()));
		}
	 
		
		return rowView;
	}
}

package com.sigterm.activities;

import com.sigterm.R;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;

public class FAQActivity extends Activity {
	/**
	 * strings for group elements
	 */
	static final String arrGroupelements[] = { 
		"What phones and Android OS version is supported? ", 
		"Can I install this application on SD Card?", 
		"What type of data is counted by Data Usage?", 
		"How do I use this app to keep my data usage in control?",
		"How do I read usage progress bar?",
		"What is data diplay unit settings?",
		"How can I customize launch screen?"
		};

	/**
	 * strings for child elements
	 */
	static final String arrChildelements[][] = { 
		{ "Most Android phones that correctly implement networking interfaces are supported. App supports Android 7.0 (API 24) and later."},
		{ "No. Google recommends not installing applications on SD cards that requires running background services."},
		{ "Data Usage includes 4G/3G/3G+/2G/Edge/GPRS under Cellular data and WiFi hotspot data is shown under WiFi. Generally speaking, tethering data should be counted in cellular - however it is not guranteed as system may count it in a block."},
		{ "In order to get most out of the app, we recommend monitoring your actual usage and average usage (indicated by dotted line on progress bar). We also recommend that you allow turning progress bar to turn yellow as soon as average usage is exceeded." }, 
		{ "Progress bar has three possible colors - green, yellow and red. You can customize when you'd like to see usage progress bar highlighted in yellow color. The options are on crossing 80% or 90% of quota or on crossing average usage for particular period in time. For example, if you are allowed to use 300 MB in 30 days, average usage is 100 MB for first 10 days. You can choose to show red color on progress bar when 95% or 100% of data is consumed." },
		{"You can customize unit of data this application will use to show data globally - options are Auto, KB, MB and GB."},
		{"You can choose to launch application with either cellular or WiFi usage."}
	};

	DisplayMetrics metrics;
	int width;
	ExpandableListView expList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.faq);

		expList = findViewById(R.id.faq_list);
		metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		width = metrics.widthPixels;
		// this code for adjusting the group indicator into right side of the
		// view
		expList.setIndicatorBounds(width - GetDipsFromPixel(50), width - GetDipsFromPixel(10));
		expList.setAdapter(new FAQExpListAdapter(this));

		expList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				Log.e("onGroupExpand", "OK");
			}
		});

		expList.setOnGroupCollapseListener(new OnGroupCollapseListener() {
			@Override
			public void onGroupCollapse(int groupPosition) {
				Log.e("onGroupCollapse", "OK");
			}
		});

		expList.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
				Log.e("OnChildClickListener", "OK");
				return false;
			}

		});
	}

	public int GetDipsFromPixel(float pixels) {
		// Get the screen's density scale
		final float scale = getResources().getDisplayMetrics().density;
		// Convert the dps to pixels, based on density scale
		return (int) (pixels * scale + 0.5f);
	}

	public class FAQExpListAdapter extends BaseExpandableListAdapter {
		private Context myContext;

		public FAQExpListAdapter(Context context) {
			myContext = context;
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return 0;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.faq_child_row, parent, false);
			}

			TextView tvPlayerName = (TextView) convertView.findViewById(R.id.tvPlayerName);

			tvPlayerName.setText(arrChildelements[groupPosition][childPosition]);

			return convertView;
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return arrChildelements[groupPosition].length;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}

		@Override
		public int getGroupCount() {
			return arrGroupelements.length;
		}

		@Override
		public long getGroupId(int groupPosition) {
			return 0;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.faq_group_row, parent, false);
			}

			TextView tvGroupName = (TextView) convertView.findViewById(R.id.tvGroupName);
			tvGroupName.setText(arrGroupelements[groupPosition]);

			return convertView;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}
}

package com.sigterm.views;

import com.sigterm.R;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;

public class SegmentedRadioGroup  extends RadioGroup{
	public SegmentedRadioGroup(Context context) {
		super(context);
	}

	public SegmentedRadioGroup(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		changeButtonsImages();
	}

	private void changeButtonsImages(){
		int count = super.getChildCount();
		for (int i = 0; i < count; i++) {
			super.getChildAt(i).setBackgroundResource(R.drawable.segment_modern);
		}
	}
}

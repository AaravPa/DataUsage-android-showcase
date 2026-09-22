package com.sigterm.views;

import com.sigterm.entities.enums.ProgressbarColors;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

public class DataUsageProgressBar extends ProgressBar {

	private String text = "";
	private int textColor = Color.BLACK;
	private float textSize = 15;
	private float dottedLine = 0;
	private float solidLine = 0;
	private String LOG_TAG = getClass().getSimpleName();
	private Paint textPaint = new Paint();
	private Rect bounds = new Rect();
	private DashPathEffect dashPathEffect =new DashPathEffect(new float[] { 7, 5 }, 0);
	private Paint solidLinePaint = new Paint();
	private Paint dottedlinePaint = new Paint();
	
	public DataUsageProgressBar(Context context) {
		super(context);
	}

	public DataUsageProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DataUsageProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// create an instance of class Paint, set color and font size
		//Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		//Log.i(LOG_TAG,"Color is set to:"+textColor)
		textPaint.setColor(textColor);
		textPaint.setTextSize(textSize);
		// In order to show text in a middle, we need to know its size
		
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		// Now we store font size in bounds variable and can calculate it's
		// position
		int x = getWidth() / 2 - bounds.centerX();
		int y = getHeight() / 2 - bounds.centerY();
		// drawing text with appropriate color and size in the center
		canvas.drawText(text, x, y, textPaint);
		Log.i(LOG_TAG, "Dotted Line is" + dottedLine);
		if (dottedLine > 0 && dottedLine <= 100) {
			
			dottedlinePaint.setARGB(255, 255, 255, 255);
			dottedlinePaint.setStyle(Style.STROKE);
			dottedlinePaint.setPathEffect(dashPathEffect);
			dottedlinePaint.setStrokeWidth(1.5f);
			float startX = getWidth() * dottedLine / 100;
			float stopX = startX;
			float startY = 0.0f;
			float stopY = startY + getHeight();
			Log.i(LOG_TAG, "Width: " + getWidth() + " height: " + getHeight());
			Log.i(LOG_TAG, "start x is " + startX + " end x is " + stopX);
			Log.i(LOG_TAG, "start y is " + startY + " end y is " + stopY);

			canvas.drawLine(startX, startY, stopX, stopY, dottedlinePaint);
			//drawTriangle(stopX, stopY, canvas);
		}
		if (solidLine > 0 && solidLine <= 100) {
			
			solidLinePaint.setARGB(255, 255, 255,255);
			solidLinePaint.setStyle(Style.STROKE);
			float startX = getWidth() * solidLine / 100;
			float stopX = startX;
			float startY = 0.0f;
			float stopY = startY + getHeight();
			canvas.drawLine(startX, startY, stopX, stopY, solidLinePaint);
		}
	}

//	private void drawTriangle(float stopX, float stopY, Canvas canvas) {
//
//		Paint triangle = new Paint();
//		triangle.setColor(Color.WHITE);
//		triangle.setStyle(Style.FILL);
//
//		int size = 30;
//		int tStartX = (int) stopX;
//		int tStartY = (int) stopY + size;
//		Point point1 = new Point(tStartX, tStartY);
//		Point point2 = new Point(tStartX + size, tStartY + size);
//		Point point3 = new Point(tStartX - size, tStartY + size);
//		Log.i(LOG_TAG, "Point 1 X:" + point1.x + " Y:" + point1.y);
//		Log.i(LOG_TAG, "Point 2 X:" + point2.x + " Y:" + point2.y);
//		Log.i(LOG_TAG, "Point 3 X:" + point3.x + " Y:" + point3.y);
//		// canvas.drawLine(point1.x, point1.y, point2.x, point2.y, triangle);
//		// canvas.drawLine(point2.x, point2.y, point3.x, point3.y, triangle);
//		// canvas.drawLine(point3.x, point3.y, point1.x, point1.y, triangle);
//		Path path = new Path();
//		path.moveTo(point1.x, point1.y);
//		path.lineTo(point2.x, point2.y);
//		path.moveTo(point2.x, point2.y);
//		path.lineTo(point3.x, point3.y);
//		path.moveTo(point3.x, point3.y);
//		path.lineTo(point1.x, point1.y);
//		path.close();
//		canvas.drawPath(path, triangle);
//	}

	public String getText() {
		return text;
	}

	public synchronized void setText(String text) {
		if (text != null) {
			this.text = text;
		} else {
			this.text = "";
		}
		postInvalidate();
	}

	public int getTextColor() {
		return textColor;
	}

	public synchronized void setTextColor(int textColor) {
		this.textColor = textColor;
		postInvalidate();
	}

	public float getTextSize() {
		return textSize;
	}

	public synchronized void setTextSize(float textSize) {
		this.textSize = textSize;
		postInvalidate();
	}

	public float getDottedLine() {
		return dottedLine;
	}

	public synchronized void setDottedLine(float dottedLine) {
		this.dottedLine = dottedLine;
		postInvalidate();
	}

	public float getSolidLine() {
		return solidLine;
	}

	public synchronized void setSolidLine(float solidLine) {
		this.solidLine = solidLine;
		postInvalidate();
	}

//	public void setProgressResource(Resources res, String color) {
//		Rect bounds = this.getProgressDrawable().getBounds();
//		if (color.equals(ProgressbarColors.GREEN))
//			this.setProgressDrawable(res.getDrawable(R.drawable.greenprogress));
//		else if (color.equals(ProgressbarColors.YELLOW))
//			this.setProgressDrawable(res.getDrawable(R.drawable.yellowprogress));
//		else if (color.equals(ProgressbarColors.RED))
//			this.setProgressDrawable(res.getDrawable(R.drawable.redprogress));
//		this.getProgressDrawable().setBounds(bounds);
//		
//	}

 
}

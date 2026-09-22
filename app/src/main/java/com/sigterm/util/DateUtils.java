package com.sigterm.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.util.Log;

public class DateUtils {
	private static final String LOG_TAG = "DateUtils";

	public static Calendar getCalendarFromDate(Date date){
		Calendar cal = Calendar.getInstance(); 
		cal.setTime(date); 
		return cal;

	}
	public static Date getDate(int year, int month, int day) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date getDate(int year, int month, int day,int hour, int minute, int second,int millisecond) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DAY_OF_MONTH, day);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, millisecond);
		return cal.getTime();
	}

	public static int getYear(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.YEAR);
	}

	public static int getMonth(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.MONTH);
	}

	public static int getDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public static Date getToday() {
		Calendar cal = Calendar.getInstance();
		return cal.getTime();
	}
	public static Calendar getTodayCal() {
		Calendar cal = Calendar.getInstance();
		return cal;
	}
	public static Date getDateFromMediumFormatString(String sDate) {
		if (sDate == null || sDate.trim().isEmpty()) return null;
		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
		format.setLenient(false);
		try {
			return format.parse(sDate.trim());
		} catch (java.text.ParseException e) {
			Log.e(LOG_TAG, "Unable to parse date: " + sDate, e);
		}
		return null;
	}
	 
	public static String getDateInMediumFormat(Date date) {
		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
		return format.format(date);
	}
	public static String getDateInShortFormat(Date date) {
		DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT);
		return format.format(date);
	}
	public static String getDateInYYYY_MM_DD_Format(Date date) {
		return (String) android.text.format.DateFormat.format("yyyy-MM-dd", date); 
	}
	public static long daysBetween(Calendar startDate, Calendar endDate) {
		Calendar date = (Calendar) startDate.clone();
		long daysBetween = 0;
		while (date.before(endDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysBetween++;
		}
		return daysBetween;
	}
	public static Date getDateForLastOcrruranceofDay(int dayOfWeek){
		Calendar cal = Calendar.getInstance();
		while(cal.get(Calendar.DAY_OF_WEEK) != dayOfWeek){
			cal.add(Calendar.DATE, -1);
		}
		return cal.getTime();
	}
	public static Date getStartDateOfCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);
		return cal.getTime();
		
	}
	
//	public static float daysBetweenIncludingHoursAndMinutes(Calendar startDate, Calendar endDate) {
//		Log.i(LOG_TAG,"StartDate:"+startDate.getTime().toString());
//		Log.i(LOG_TAG,"EndDate:"+endDate.getTime().toString());
//		Calendar date = (Calendar) startDate.clone();
//		float daysBetween = 0;
//		while (date.before(endDate)) {
//			date.add(Calendar.DAY_OF_MONTH, 1);
//			daysBetween++;
//		}
//		
//		Log.i(LOG_TAG,"Days between full:"+daysBetween);
//		int startHour = startDate.get(Calendar.HOUR);
//		int startMinute = startDate.get(Calendar.MINUTE);
//		int endHour = endDate.get(Calendar.HOUR);
//		int endMinute = endDate.get(Calendar.MINUTE);
//		float hoursDiff = endHour - startHour;
//		float minuteDiff = endMinute - startMinute;
//		float fraction = (hoursDiff/24)+(minuteDiff/(60*24));
//		//remove full day for today and instead add fraction
//		daysBetween = daysBetween -1;
//		daysBetween += fraction;
//		Log.i(LOG_TAG,"Days between fraction:"+daysBetween);
//		return daysBetween;
//	}
}

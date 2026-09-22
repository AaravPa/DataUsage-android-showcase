package com.sigterm.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="Alerts")
public class Alert  {
	public final static String ALERTID_COLUMN_NAME = "alertId";
	public final static String ALERTNAME_COLUMN_NAME = "alertName";
	public final static String THRESHOLD_COLUMN_NAME = "threshold";
	public final static String ALERTFIRED_COLUMN_NAME = "alertFired";
	 
	
	@DatabaseField(generatedId=true)
	private int alertId;
	@DatabaseField(canBeNull=false)
	private String alertName;
	@DatabaseField(canBeNull=false)
	private int threshold;
	@DatabaseField(canBeNull=false)
	private boolean alertFired;
    @DatabaseField(canBeNull = false, foreign = true)
    private UsageCounter usageCounter;
	

	public int getAlertId() {
		return alertId;
	}
	 
	public void setAlertId(int alertId) {
		this.alertId = alertId;
	}
	 
	public String getAlertName() {
		return alertName;
	}
	 
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	 
	public int getThreshold() {
		return threshold;
	}
	 
	public void setThreshold(int threshold) {
		this.threshold = threshold;
	}

	 
	public boolean isAlertFired() {
		return alertFired;
	}

	 
	public void setAlertFired(boolean alertFired) {
		this.alertFired = alertFired;
	}

	public UsageCounter getUsageCounter() {
		return usageCounter;
	}

	public void setUsageCounter(UsageCounter usageCounter) {
		this.usageCounter = usageCounter;
	}
	
	
}

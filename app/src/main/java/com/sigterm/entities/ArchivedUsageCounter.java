package com.sigterm.entities;

import java.util.Date;

import android.util.Log;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import com.sigterm.entities.enums.DataDirectionType;
import com.sigterm.entities.enums.NetworkDataType;



@DatabaseTable(tableName="ArchivedUsageCounters")
public class ArchivedUsageCounter   {
	
	public final static String COUNTERID_COLUMN_NAME = "counterId";
	public final static String COUNTERNAME_COLUMN_NAME = "counterName";
	public final static String STARTDATETIME_COLUMN_NAME = "startDateTime";
	public final static String ENDDATETIME_COLUMN_NAME = "endDateTime";
	public final static String RECURRING_COLUMN_NAME = "recurring";
	public final static String REPEATFREQUENCY_COLUMN_NAME = "repeatInterval";
	public final static String DATADIRECTIONTYPE_COLUMN_NAME = "dataDirectionType";
	public final static String NETWORKDATATYPE_COLUMN_NAME = "networkDataType";	
	public final static String CELLULARBYTESSENT_COLUMN_NAME = "cellularBytesSent";
	public final static String CELLULARBYTESRECD_COLUMN_NAME = "cellularBytesRecd";
	public final static String CELLULARBYTESTOTAL_COLUMN_NAME = "cellularBytesTotal";
	public final static String WIFIBYTESSENT_COLUMN_NAME = "wifiBytesSent";
	public final static String WIFIBYTESRECD_COLUMN_NAME = "wifiBytesRecd";
	public final static String WIFIBYTESTOTAL_COLUMN_NAME = "wifiBytesTotal";
	public final static String ENABLEALERTS_COLUMN_NAME = "enableAlerts";
	public final static String QUOTA_COLUMN_NAME = "quota";
	public final static String QUOTAUNIT_COLUMN_NAME = "quotaUnit";
	public final static String QUOTAINBYTES_COLUMN_NAME = "quotaInBytes";
//	public final static String ARCHIVED_COLUMN_NAME = "archived";
//	public final static String ACTIVE_COLUMN_NAME = "active";
	
//	public final static String DEFAULT_CELLULAR_BILLING_COUNTER_NAME = "Cellular Billing Usage";
//	public final static String DEFAULT_WIFI_BILLING_COUNTER_NAME = "WiFi Billing Usage";
//	public final static String ISDEFAULT_COLUMN_NAME = "isDefault";
	
	@DatabaseField(generatedId=true)
	private int counterId;
	@DatabaseField(canBeNull=false)
	private String counterName;
	@DatabaseField(canBeNull=true)
	private Date startDateTime;
	@DatabaseField(canBeNull=true)
	private Date endDateTime;
	@DatabaseField(canBeNull=false)
	private boolean recurring;
	@DatabaseField(canBeNull=false)
	private String repeatInterval;
	@DatabaseField(canBeNull=false)
	private int repeatFrequency;
	@DatabaseField(canBeNull=false)
	private String dataDirectionType;
	@DatabaseField(canBeNull=false)
	private String networkDataType;
	@DatabaseField(canBeNull=false)
	private long cellularBytesSent;
	@DatabaseField(canBeNull=false)
	private long cellularBytesRecd;
	@DatabaseField(canBeNull=false)
	private long cellularBytesTotal;
	@DatabaseField(canBeNull=false)
	private long wifiBytesSent;
	@DatabaseField(canBeNull=false)
	private long wifiBytesRecd;
	@DatabaseField(canBeNull=false)
	private long wifiBytesTotal;
	@DatabaseField(canBeNull=false)
	private boolean enableAlerts;
	@DatabaseField(canBeNull=false)
	private float quota;
	@DatabaseField(canBeNull=false)
	private String quotaUnit;
	@DatabaseField(canBeNull=false)
	private long quotaInBytes;
	
//	@ForeignCollectionField(eager = false)
//	private ForeignCollection<Alert> alerts;
	
//	@DatabaseField(canBeNull=false) 
//	private boolean archived;
//	@DatabaseField(canBeNull=false) 
//	private boolean active;
	
	@DatabaseField(persisted=false)
	private float percentCellularQuotaUsed;
	
	@DatabaseField(persisted=false)
	private float percentWiFiQuotaUsed;
	
//	@DatabaseField(canBeNull=false) 
//	private boolean isDefault;
	
	public ArchivedUsageCounter(){

	}
		
	public ArchivedUsageCounter(String counterName, Date startDateTime,Date endDateTime,boolean recurring,String repeatInterval, int repeatFrequency,
			String dataDirectionType,String networkDataType,long cellularBytesSent,long cellularBytesRecd,
			long cellularBytesTotal,long wifiBytesSent,long wifiBytesRecd,long wifiBytesTotal, boolean enableAlerts,
			float quota, String quotaUnit,long quotaInBytes){
		this.counterName = counterName;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.recurring = recurring;
		this.repeatFrequency = repeatFrequency;
		this.repeatInterval = repeatInterval;
		this.dataDirectionType = dataDirectionType;
		this.networkDataType = networkDataType;
		this.cellularBytesSent = cellularBytesSent;
		this.cellularBytesRecd = cellularBytesRecd;
		this.cellularBytesTotal = cellularBytesTotal;
		this.wifiBytesSent = wifiBytesSent;
		this.wifiBytesRecd = wifiBytesRecd;
		this.wifiBytesTotal = wifiBytesTotal;
		this.enableAlerts = enableAlerts;
		this.quota = quota;
		this.quotaUnit = quotaUnit;
		this.quotaInBytes = quotaInBytes;
//		this.archived = archived;
//		this.active = active;
//		this.isDefault = isDefault;
	}
		 

	public float getPercentCellularQuotaUsed() {
		float percent = 0;
		if(getQuotaInBytes() > 0)
			percent = (float) getCellularBytesTotal() / getQuotaInBytes() * 100;
		return percent; 
	}

	public float getPercentWiFiQuotaUsed() {
		float percent = 0;
		if(getQuotaInBytes() > 0)
			percent = (float) getWifiBytesTotal() / getQuotaInBytes() * 100;
		return percent;
	}

	public int getCounterId() {
		return counterId;
	}

	 
	public void setCounterId(int counterId) {
		this.counterId = counterId;
	}

	 
	public String getCounterName() {
		return counterName;
	}

	 
	public void setCounterName(String counterName) {
		this.counterName = counterName;
	}

	 
	public Date getStartDateTime() {
		return startDateTime;
	}

	 
	public void setStartDateTime(Date startDateTime) {
		this.startDateTime = startDateTime;
	}

	 
	public boolean isRecurring() {
		return recurring;
	}

	 
	public void setRecurring(boolean recurring) {
		this.recurring = recurring;
	}

	 
	public String getRepeatInterval() {
		return repeatInterval;
	}

	 
	public void setRepeatInterval(String repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	 
	public String getDataDirectionType() {
		return dataDirectionType;
	}

	 
	public void setDataDirectionType(String dataDirectionType) {
		this.dataDirectionType = dataDirectionType;
	}

	 
	public String getNetworkDataType() {
		return networkDataType;
	}

	 
	public void setNetworkDataType(String networkDataType) {
		this.networkDataType = networkDataType;
	}

 
	public long getCellularBytesSent() {
		return cellularBytesSent;
	}

	 
	public void setCellularBytesSent(long cellularBytesSent) {
		this.cellularBytesSent = cellularBytesSent;
	}

	 
	public long getCellularBytesRecd() {
		return cellularBytesRecd;
	}

 
	public void setCellularBytesRecd(long cellularBytesRecd) {
		this.cellularBytesRecd = cellularBytesRecd;
	}

	 
	public long getCellularBytesTotal() {
		return cellularBytesTotal;
	}

	 
	public void setCellularBytesTotal(long cellularBytesTotal) {
		this.cellularBytesTotal = cellularBytesTotal;
	}

	 
	public long getWifiBytesSent() {
		return wifiBytesSent;
	}

	 
	public void setWifiBytesSent(long wifiBytesSent) {
		this.wifiBytesSent = wifiBytesSent;
	}

	 
	public long getWifiBytesRecd() {
		return wifiBytesRecd;
	}


	public void setWifiBytesRecd(long wifiBytesRecd) {
		this.wifiBytesRecd = wifiBytesRecd;
	}


	public long getWifiBytesTotal() {
		return wifiBytesTotal;
	}


	public void setWifiBytesTotal(long wifiBytesTotal) {
		this.wifiBytesTotal = wifiBytesTotal;
	}


	public boolean isEnableAlerts() {
		return enableAlerts;
	}


	public void setEnableAlerts(boolean enableAlerts) {
		this.enableAlerts = enableAlerts;
	}


	public float getQuota() {
		return quota;
	}

	public void setQuota(float quota) {
		this.quota = quota;
	}


	public String getQuotaUnit() {
		return quotaUnit;
	}


	public void setQuotaUnit(String quotaUnit) {
		this.quotaUnit = quotaUnit;
	}


	public long getQuotaInBytes() {
		return quotaInBytes;
	}


	public void setQuotaInBytes(long quotaInBytes) {
		this.quotaInBytes = quotaInBytes;
	}


//	public ForeignCollection<Alert> getAlerts() {
//		return alerts;
//	}
//
//
//	public void setAlerts(ForeignCollection<Alert> alerts) {
//		this.alerts = alerts;
//	}

	public Date getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(Date endDateTime) {
		this.endDateTime = endDateTime;
	}

//	public boolean isArchived() {
//		return archived;
//	}
//
//	public void setArchived(boolean archived) {
//		this.archived = archived;
//	}
//
//	public boolean isActive() {
//		return active;
//	}
//	
//	public void setActive(boolean active) {
//		this.active = active;
//	}
//
//	public boolean isDefault() {
//		return isDefault;
//	}
//	
//	public void setDefault(boolean isDefault) {
//		this.isDefault= isDefault;
//	}
	
	public int getRepeatFrequency() {
		return repeatFrequency;
	}

	public void setRepeatFrequency(int repeatFrequency) {
		this.repeatFrequency = repeatFrequency;
	}
	
	public boolean isCellularOnlyCounter(){
		return getNetworkDataType().equals(NetworkDataType.CELLULAR);
	}
	
	public boolean isWiFiOnlyCounter(){
		return getNetworkDataType().equals(NetworkDataType.WIFI);
	}
	
	 
	
}


package com.sigterm.entities;

import java.util.Date;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.field.DatabaseField;

@DatabaseTable(tableName="UsageLogs")
public class UsageLog    {
	
	public final static String LOGID_COLUMN_NAME = "logId";
	public final static String LOGDATE_COLUMN_NAME = "logDate";
	public final static String INTERFACECELLULARBYTESSENT_COLUMN_NAME = "interfaceCellularBytesSent";
	public final static String INTERFACECELLULARBYTESRECD_COLUMN_NAME = "interfaceCellularBytesRecd";
	public final static String INTERFACECELLULARBYTESTOTAL_COLUMN_NAME = "interfaceCellularBytesTotal";
	public final static String INTERFACEWIFIBYTESSENT_COLUMN_NAME = "interfaceWifiBytesSent";
	public final static String INTERFACEWIFIBYTESRECD_COLUMN_NAME = "interfaceWifiBytesRecd";
	public final static String INTERFACEWIFIBYTESTOTAL_COLUMN_NAME = "interfaceWifiBytesTotal";	
	public final static String CELLULARBYTESSENT_COLUMN_NAME = "cellularBytesSent";
	public final static String CELLULARBYTESRECD_COLUMN_NAME = "cellularBytesRecd";
	public final static String CELLULARBYTESTOTAL_COLUMN_NAME = "cellularBytesTotal";
	public final static String WIFIBYTESSENT_COLUMN_NAME = "wifiBytesSent";
	public final static String WIFIBYTESRECD_COLUMN_NAME = "wifiBytesRecd";
	public final static String WIFIBYTESTOTAL_COLUMN_NAME = "wifiBytesTotal";
	
	
	@DatabaseField(generatedId=true)
	private int logId;
	@DatabaseField(canBeNull=false)
	private Date logDate;

	@DatabaseField(canBeNull=false)
	private long interfaceCellularBytesSent;
	@DatabaseField(canBeNull=false)
	private long interfaceCellularBytesRecd;
	@DatabaseField(canBeNull=false)
	private long interfaceCellularBytesTotal;
	@DatabaseField(canBeNull=false)
	private long interfaceWifiBytesSent;
	@DatabaseField(canBeNull=false)
	private long interfaceWifiBytesRecd;
	@DatabaseField(canBeNull=false)
	private long interfaceWifiBytesTotal;
	
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
	
	public UsageLog(){
		
	}
	
	public UsageLog( Date logDate, long interfaceCellularBytesSent,
			long interfaceCellularBytesRecd, long interfaceCellularBytesTotal,
			long interfaceWifiBytesSent, long interfaceWifiBytesRecd,
			long interfaceWifiBytesTotal, long cellularBytesSent,
			long cellularBytesRecd, long cellularBytesTotal,
			long wifiBytesSent, long wifiBytesRecd, long wifiBytesTotal) {
		super();
		 
		this.logDate = logDate;
		this.interfaceCellularBytesSent = interfaceCellularBytesSent;
		this.interfaceCellularBytesRecd = interfaceCellularBytesRecd;
		this.interfaceCellularBytesTotal = interfaceCellularBytesTotal;
		this.interfaceWifiBytesSent = interfaceWifiBytesSent;
		this.interfaceWifiBytesRecd = interfaceWifiBytesRecd;
		this.interfaceWifiBytesTotal = interfaceWifiBytesTotal;
		this.cellularBytesSent = cellularBytesSent;
		this.cellularBytesRecd = cellularBytesRecd;
		this.cellularBytesTotal = cellularBytesTotal;
		this.wifiBytesSent = wifiBytesSent;
		this.wifiBytesRecd = wifiBytesRecd;
		this.wifiBytesTotal = wifiBytesTotal;
	}

	public int getLogId() {
		return logId;
	}

	public void setLogId(int logId) {
		this.logId = logId;
	}

	public Date getLogDate() {
		return logDate;
	}

	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}

	public long getInterfaceCellularBytesSent() {
		return interfaceCellularBytesSent;
	}

	public void setInterfaceCellularBytesSent(long interfaceCellularBytesSent) {
		this.interfaceCellularBytesSent = interfaceCellularBytesSent;
	}

	public long getInterfaceCellularBytesRecd() {
		return interfaceCellularBytesRecd;
	}

	public void setInterfaceCellularBytesRecd(long interfaceCellularBytesRecd) {
		this.interfaceCellularBytesRecd = interfaceCellularBytesRecd;
	}

	public long getInterfaceCellularBytesTotal() {
		return interfaceCellularBytesTotal;
	}

	public void setInterfaceCellularBytesTotal(long interfaceCellularBytesTotal) {
		this.interfaceCellularBytesTotal = interfaceCellularBytesTotal;
	}

	public long getInterfaceWifiBytesSent() {
		return interfaceWifiBytesSent;
	}

	public void setInterfaceWifiBytesSent(long interfaceWifiBytesSent) {
		this.interfaceWifiBytesSent = interfaceWifiBytesSent;
	}

	public long getInterfaceWifiBytesRecd() {
		return interfaceWifiBytesRecd;
	}

	public void setInterfaceWifiBytesRecd(long interfaceWifiBytesRecd) {
		this.interfaceWifiBytesRecd = interfaceWifiBytesRecd;
	}

	public long getInterfaceWifiBytesTotal() {
		return interfaceWifiBytesTotal;
	}

	public void setInterfaceWifiBytesTotal(long interfaceWifiBytesTotal) {
		this.interfaceWifiBytesTotal = interfaceWifiBytesTotal;
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


	 
}

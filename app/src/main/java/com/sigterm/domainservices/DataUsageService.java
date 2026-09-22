package com.sigterm.domainservices;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import android.content.Context;
import android.util.Log;

import com.sigterm.entities.*;
import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.domainservices.NetworkServices;
import com.sigterm.orm.sqlite.DatabaseHelper;

public class DataUsageService {
	
	private final String LOG_TAG = getClass().getSimpleName();
	private  DatabaseHelper _helper;
	private UsageCounterService _usageCounterSerivce;
	private UsageLogService _usageLogService;
	
	public DataUsageService(DatabaseHelper helper){
		this._helper = helper;
		this._usageCounterSerivce = new UsageCounterService(_helper);
		this._usageLogService = new UsageLogService(_helper);
	}

	private void deleteLogsOlderThanYear(){
		Calendar calendar = Calendar.getInstance();
		Date today = calendar.getTime();
		//todo for testing replace with YEAR
		calendar.add(Calendar.YEAR, -1);
		Date newDate = calendar.getTime();
		_usageLogService.deleteLogsPriorToDate(newDate);
	}
	
	public void logUsage(Context context){

		//first if today's log exist, add it to log there.
		Date today  = Calendar.getInstance().getTime();
//		List<UsageLog> usageLogList = _usageLogService.getLogByDate(today);
//		if(usageLogList.size() > 0){
//			UsageLog usageLog = usageLogList.get(0);
//			
//			long interfaceCellularBytesRecd = NetworkServices.getCellularBytesReceived();
//			long interfaceCellularBytesSent = NetworkServices.getCellularBytesSent();
//			long interfaceCellularBytesTotal = NetworkServices.getCellularBytesTotal();
//			
//			long cellularBytesSentToAdd =  interfaceCellularBytesSent - usageLog.getInterfaceCellularBytesSent();
//			long cellularBytesRecdToAdd =  interfaceCellularBytesRecd - usageLog.getInterfaceCellularBytesRecd();
//			long cellularBytesTotalToAdd = interfaceCellularBytesTotal - usageLog.getInterfaceCellularBytesTotal();
//
//			long cellularBytesSent =  cellularBytesSentToAdd + usageLog.getCellularBytesSent();
//			long cellularBytesRecd =  cellularBytesRecdToAdd + usageLog.getCellularBytesRecd();
//			long cellularBytesTotal = cellularBytesTotalToAdd + usageLog.getCellularBytesTotal();
//
//			long interfaceWifiBytesRecd = NetworkServices.getWiFiBytesReceived();
//			long interfaceWifiBytesSent = NetworkServices.getWiFiBytesSent();
//			long interfaceWifiBytesTotal = NetworkServices.getWiFiBytesTotal();
//			
//			long wifiBytesSentToAdd = interfaceWifiBytesSent - usageLog.getInterfaceWifiBytesSent();
//			long wifiBytesRecdToAdd = interfaceWifiBytesRecd - usageLog.getInterfaceWifiBytesRecd();
//			long wifiBytesTotalToAdd = interfaceWifiBytesTotal - usageLog.getInterfaceWifiBytesTotal();
//			
//			if(cellularBytesSentToAdd < 0) cellularBytesSentToAdd = 0;
//			if(cellularBytesRecdToAdd < 0) cellularBytesRecdToAdd = 0;
//			if(cellularBytesTotalToAdd < 0) cellularBytesTotalToAdd = 0;
//			
//			if(wifiBytesSentToAdd < 0) wifiBytesSentToAdd = 0;
//			if(wifiBytesRecdToAdd < 0) wifiBytesRecdToAdd = 0;
//			if(wifiBytesTotalToAdd < 0) wifiBytesTotalToAdd = 0;
//			
//			long wifiBytesSent = wifiBytesSentToAdd + usageLog.getWifiBytesSent();
//			long wifiBytesRecd = wifiBytesRecdToAdd + usageLog.getWifiBytesRecd();
//			long wifiBytesTotal = wifiBytesTotalToAdd + usageLog.getWifiBytesTotal();
//			
//			Log.i(LOG_TAG,"wifiBytesTotal Old:"+usageLog.getWifiBytesTotal()); 
//			Log.i(LOG_TAG,"wifiBytesTotal New: "+wifiBytesTotal);
//			usageLog.setCellularBytesSent(cellularBytesSent);
//			usageLog.setCellularBytesRecd(cellularBytesRecd);
//			usageLog.setCellularBytesTotal(cellularBytesTotal);
//
//			usageLog.setWifiBytesRecd(wifiBytesRecd);
//			usageLog.setWifiBytesSent(wifiBytesSent);
//			usageLog.setWifiBytesTotal(wifiBytesTotal);
//
//			usageLog.setInterfaceCellularBytesRecd(interfaceCellularBytesRecd);
//			usageLog.setInterfaceCellularBytesSent(interfaceCellularBytesSent);
//			usageLog.setInterfaceCellularBytesTotal(interfaceCellularBytesTotal);
//			
//			usageLog.setInterfaceWifiBytesRecd(interfaceWifiBytesRecd);
//			usageLog.setInterfaceWifiBytesSent(interfaceWifiBytesSent);
//			usageLog.setInterfaceWifiBytesTotal(interfaceWifiBytesTotal);
//			
//			_usageLogService.update(usageLog);
//			//now update all counters
//			_usageCounterSerivce.updateUsageForActiveCounters(context,cellularBytesRecdToAdd, cellularBytesSentToAdd, cellularBytesTotalToAdd, wifiBytesRecdToAdd, wifiBytesSentToAdd, wifiBytesTotalToAdd);
//			Log.i(LOG_TAG,"Updated usage record for today: " + today.toString());
//		}
//		//if today's log doesn't exist, get yesterday's usage log and create a log for today.
//		else{
//			Calendar cal = Calendar.getInstance();
//			cal.add(Calendar.DATE,-1);
//			Date yesterday = cal.getTime();
//			List<UsageLog> usageLogYesteray = _usageLogService.getLogByDate(yesterday);
//			if(usageLogYesteray.size() > 0){
//				UsageLog usageLog = usageLogYesteray.get(0);
//				long interfaceCellularBytesRecd = NetworkServices.getCellularBytesReceived();
//				long interfaceCellularBytesSent = NetworkServices.getCellularBytesSent();
//				long interfaceCellularBytesTotal = NetworkServices.getCellularBytesTotal();
//				
//				long cellularBytesSentToAdd =  interfaceCellularBytesSent - usageLog.getInterfaceCellularBytesSent();
//				long cellularBytesRecdToAdd =  interfaceCellularBytesRecd - usageLog.getInterfaceCellularBytesRecd();
//				long cellularBytesTotalToAdd = interfaceCellularBytesTotal - usageLog.getInterfaceCellularBytesTotal();
//
//				long interfaceWifiBytesRecd = NetworkServices.getWiFiBytesReceived();
//				long interfaceWifiBytesSent = NetworkServices.getWiFiBytesSent();
//				long interfaceWifiBytesTotal = NetworkServices.getWiFiBytesTotal();
//				
//				long wifiBytesSentToAdd = interfaceWifiBytesSent - usageLog.getInterfaceWifiBytesSent();
//				long wifiBytesRecdToAdd = interfaceWifiBytesRecd - usageLog.getInterfaceWifiBytesRecd();
//				long wifiBytesTotalToAdd = interfaceWifiBytesTotal - usageLog.getInterfaceWifiBytesTotal();
//				
//				if(cellularBytesSentToAdd < 0) cellularBytesSentToAdd = 0;
//				if(cellularBytesRecdToAdd < 0) cellularBytesRecdToAdd = 0;
//				if(cellularBytesTotalToAdd < 0) cellularBytesTotalToAdd = 0;
//				
//				if(wifiBytesSentToAdd < 0) wifiBytesSentToAdd = 0;
//				if(wifiBytesRecdToAdd < 0) wifiBytesRecdToAdd = 0;
//				if(wifiBytesTotalToAdd < 0) wifiBytesTotalToAdd = 0;
//				
//				UsageLog newUsageLog = new UsageLog(today,interfaceCellularBytesSent,interfaceCellularBytesRecd,
//						interfaceCellularBytesTotal,interfaceWifiBytesSent,interfaceWifiBytesRecd,interfaceWifiBytesTotal,
//						cellularBytesSentToAdd,cellularBytesRecdToAdd,cellularBytesTotalToAdd,wifiBytesSentToAdd,wifiBytesRecdToAdd,wifiBytesTotalToAdd);
//				_usageLogService.create(newUsageLog);
//				_usageCounterSerivce.updateUsageForActiveCounters(context,cellularBytesRecdToAdd, cellularBytesSentToAdd, cellularBytesTotalToAdd, wifiBytesRecdToAdd, wifiBytesSentToAdd, wifiBytesTotalToAdd);
//				Log.i(LOG_TAG,"Creating new usage record for today: " + today.toString());
//			}
//			//if yesterday's log doesn't exist then get last log entry and go from there
//			else{
				UsageLog usageLog = _usageLogService.getLastLog();
				if(usageLog != null){
					 
					long interfaceCellularBytesRecd = NetworkServices.getCellularBytesReceived();
					long interfaceCellularBytesSent = NetworkServices.getCellularBytesSent();
					long interfaceCellularBytesTotal = NetworkServices.getCellularBytesTotal();
					
					long cellularBytesSentToAdd =  interfaceCellularBytesSent - usageLog.getInterfaceCellularBytesSent();
					long cellularBytesRecdToAdd =  interfaceCellularBytesRecd - usageLog.getInterfaceCellularBytesRecd();
					long cellularBytesTotalToAdd = interfaceCellularBytesTotal - usageLog.getInterfaceCellularBytesTotal();

					
					long interfaceWifiBytesRecd = NetworkServices.getWiFiBytesReceived();
					long interfaceWifiBytesSent = NetworkServices.getWiFiBytesSent();
					long interfaceWifiBytesTotal = NetworkServices.getWiFiBytesTotal();
					
					long wifiBytesSentToAdd = interfaceWifiBytesSent - usageLog.getInterfaceWifiBytesSent();
					long wifiBytesRecdToAdd = interfaceWifiBytesRecd - usageLog.getInterfaceWifiBytesRecd();
					long wifiBytesTotalToAdd = interfaceWifiBytesTotal - usageLog.getInterfaceWifiBytesTotal();
					
					if(cellularBytesSentToAdd < 0) cellularBytesSentToAdd = 0;
					if(cellularBytesRecdToAdd < 0) cellularBytesRecdToAdd = 0;
					if(cellularBytesTotalToAdd < 0) cellularBytesTotalToAdd = 0;
					
					if(wifiBytesSentToAdd < 0) wifiBytesSentToAdd = 0;
					if(wifiBytesRecdToAdd < 0) wifiBytesRecdToAdd = 0;
					if(wifiBytesTotalToAdd < 0) wifiBytesTotalToAdd = 0;
					
					
					UsageLog newUsageLog = new UsageLog(today,interfaceCellularBytesSent,interfaceCellularBytesRecd,
							interfaceCellularBytesTotal,interfaceWifiBytesSent,interfaceWifiBytesRecd,interfaceWifiBytesTotal,
							cellularBytesSentToAdd,cellularBytesRecdToAdd,cellularBytesTotalToAdd,wifiBytesSentToAdd,wifiBytesRecdToAdd,wifiBytesTotalToAdd);
					_usageLogService.create(newUsageLog);
					
					//_usageCounterSerivce.listAllCounters();
					Log.i("RESET","Data Usage Service checking for ended counters..");
					_usageCounterSerivce.checkForEndedDefaultCounters(context);
					deleteLogsOlderThanYear();
					//Log.i("RESET","Data Usage Service updating active counters..");
					//_usageCounterSerivce.updateUsageForActiveCounters(cellularBytesRecdToAdd, cellularBytesSentToAdd, cellularBytesTotalToAdd, wifiBytesRecdToAdd, wifiBytesSentToAdd, wifiBytesTotalToAdd);
				//	Log.i(LOG_TAG,"Creating new usage record for today: " + today.toString());
				}
//			}
//		
//				
//		}

	}
}

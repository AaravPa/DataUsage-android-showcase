package com.sigterm.domainservices;

import android.net.TrafficStats;

/** Read-only network accounting backed by Android's TrafficStats counters. */
public final class NetworkServices {
	private NetworkServices() { }

	public static boolean IsCellularSupported() {
		return TrafficStats.getMobileRxBytes() != TrafficStats.UNSUPPORTED
				&& TrafficStats.getMobileTxBytes() != TrafficStats.UNSUPPORTED;
	}

	public static boolean IsWiFiSupported() {
		return TrafficStats.getTotalRxBytes() != TrafficStats.UNSUPPORTED
				&& TrafficStats.getTotalTxBytes() != TrafficStats.UNSUPPORTED;
	}

	public static long getCellularBytesSent() {
		return supportedValue(TrafficStats.getMobileTxBytes());
	}

	public static long getCellularBytesReceived() {
		return supportedValue(TrafficStats.getMobileRxBytes());
	}

	public static long getCellularBytesTotal() {
		return getCellularBytesSent() + getCellularBytesReceived();
	}

	public static long getWiFiBytesSent() {
		return subtractMobile(TrafficStats.getTotalTxBytes(), getCellularBytesSent());
	}

	public static long getWiFiBytesReceived() {
		return subtractMobile(TrafficStats.getTotalRxBytes(), getCellularBytesReceived());
	}

	public static long getWiFiBytesTotal() {
		return getWiFiBytesSent() + getWiFiBytesReceived();
	}

	public static long getBytesReceivedByAppID(int uid) {
		return supportedValue(TrafficStats.getUidRxBytes(uid));
	}

	public static long getBytesSentByAppID(int uid) {
		return supportedValue(TrafficStats.getUidTxBytes(uid));
	}

	public static long getBytesTotalByAppID(int uid) {
		return getBytesReceivedByAppID(uid) + getBytesSentByAppID(uid);
	}

	private static long supportedValue(long value) {
		return value == TrafficStats.UNSUPPORTED || value < 0 ? 0 : value;
	}

	private static long subtractMobile(long total, long mobile) {
		if (total == TrafficStats.UNSUPPORTED) return 0;
		return Math.max(0, total - mobile);
	}
}

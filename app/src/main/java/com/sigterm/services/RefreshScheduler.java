package com.sigterm.services;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.sigterm.entities.enums.PrefKeys;
import com.sigterm.entities.enums.UpdateFreqPrefs;
import com.sigterm.util.PreferenceUtils;

import java.util.concurrent.TimeUnit;

/** Owns durable, process-independent scheduling for usage refreshes. */
public final class RefreshScheduler {
    private static final String PERIODIC_WORK_NAME = "usage-refresh-periodic";
    private static final String IMMEDIATE_WORK_NAME = "usage-refresh-immediate";
    private static final long MIN_INTERVAL_MINUTES = 15L;

    private RefreshScheduler() { }

    public static void refreshNow(Context context) {
        Context appContext = context.getApplicationContext();
        schedulePeriodic(appContext);
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(UsageRefreshWorker.class).build();
        WorkManager.getInstance(appContext).enqueueUniqueWork(
                IMMEDIATE_WORK_NAME, ExistingWorkPolicy.REPLACE, request);
    }

    public static void schedulePeriodic(Context context) {
        Context appContext = context.getApplicationContext();
        long intervalMinutes = getIntervalMinutes(appContext);
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                UsageRefreshWorker.class, intervalMinutes, TimeUnit.MINUTES).build();
        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }

    private static long getIntervalMinutes(Context context) {
        String preference = PreferenceUtils.GetPreference(
                PrefKeys.UPDATE_FREQ_PREF, UpdateFreqPrefs.M15, context);
        long requested = UpdateFreqPrefs.M30.equals(preference) ? 30L
                : UpdateFreqPrefs.M60.equals(preference) ? 60L
                : UpdateFreqPrefs.M15.equals(preference) ? 15L
                : MIN_INTERVAL_MINUTES;
        return Math.max(MIN_INTERVAL_MINUTES, requested);
    }
}

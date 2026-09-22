package com.sigterm.services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.sigterm.domainservices.DataUsageService;
import com.sigterm.orm.sqlite.DatabaseHelper;

/** Performs one usage sample and then lets WorkManager release the process. */
public final class UsageRefreshWorker extends Worker {
    public static final String USAGE_UPDATED = "com.sigterm.action.USAGE_UPDATED";
    private static final String LOG_TAG = "UsageRefreshWorker";
    private static final Object REFRESH_LOCK = new Object();

    public UsageRefreshWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        synchronized (REFRESH_LOCK) {
            DatabaseHelper helper = new DatabaseHelper(getApplicationContext());
            try {
                new UsageRefreshCoordinator(getApplicationContext(), helper).refresh();
                return Result.success();
            } catch (RuntimeException error) {
                Log.e(LOG_TAG, "Usage refresh failed; WorkManager will retry", error);
                return Result.retry();
            } finally {
                helper.close();
            }
        }
    }
}

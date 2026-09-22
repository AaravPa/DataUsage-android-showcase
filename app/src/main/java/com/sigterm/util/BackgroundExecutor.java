package com.sigterm.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/** Runs repository work off the main thread and delivers results on it. */
public final class BackgroundExecutor {
	public interface Work<T> { T run() throws Exception; }
	public interface Callback<T> {
		void onComplete(T value);
		default void onError(Throwable error) { }
	}

	private final ExecutorService executor = Executors.newCachedThreadPool();
	private final Handler mainHandler = new Handler(Looper.getMainLooper());
	private volatile boolean shutdown;

	public <T> void execute(Work<T> work, Callback<T> callback) {
		if (shutdown) return;
		try {
			executor.execute(() -> {
			try {
				T result = work.run();
				mainHandler.post(() -> {
					if (!shutdown) callback.onComplete(result);
				});
			} catch (Throwable error) {
				android.util.Log.e("BackgroundExecutor", "Background refresh failed", error);
				mainHandler.post(() -> {
					if (!shutdown) callback.onError(error);
				});
			}
			});
		} catch (RejectedExecutionException ignored) {
			// The owner was destroyed while a refresh was being scheduled.
		}
	}

	public void shutdown() {
		shutdown = true;
		executor.shutdownNow();
	}
}

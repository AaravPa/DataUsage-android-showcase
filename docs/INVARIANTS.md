# Invariants

Each rule below must remain true across implementation changes. The cited paths are the primary
enforcement or dependency points.

1. **Two defaults exist.** A fresh database contains one default cellular and one default Wi-Fi
   `UsageCounter`. Preserve names, `isDefault`, network type, and schema mapping. (`DatabaseHelper`,
   `UsageCounter`, `UsageCounterService`.)
2. **The first log is a baseline.** Database creation inserts current interface counters with zero
   deltas; later sampling compares against the latest row. (`DatabaseHelper.onCreate`,
   `DataUsageService.logUsage`.)
3. **Totals come from logs.** `UsageCounter` byte getters delegate to date-range sums in
   `UsageLogService`; do not add a separate mutable aggregate without a coordinated migration.
4. **No negative usage is recorded.** Cellular/Wi-Fi sent, received, and total deltas below zero
   are clamped to zero while interface baselines still advance. (`DataUsageService`.)
5. **Units and sentinels are stable.** KB/MB/GB use powers of 1024; quota zero means unlimited;
   null start/end dates mean no expiry. (`DataunitUtils`, `QuotaPreference`, `PrefKeys`,
   `UsageCounterService`.)
6. **Rollover preserves history.** Ended recurring periods are archived before active dates advance;
   alert flags are reset for the matching network. (`UsageCounterService`.)
7. **Adjust/reset uses the accounting path.** It writes compensating log data and/or archives a
   period; it does not silently mutate totals outside `UsageLogService`. (`UsageCounterService`.)
8. **Alerts are one-shot per network/level.** Highest crossed un-fired level wins; plan changes,
   reset, and rollover clear that network's flags. (`NotificationService`, `UsageCounterService`.)
9. **Refreshes are serialized and ordered.** Unique WorkManager names plus the worker lock prevent
   overlapping samples; each refresh samples, updates widgets and alerts, then broadcasts the
   package-scoped update action. (`RefreshScheduler`, `UsageRefreshWorker`, `UsageRefreshCoordinator`.)
10. **Background refresh is bounded.** Periodic scheduling never requests less than WorkManager's
    15-minute minimum, and no endless foreground service or `dataSync` permission is required.
11. **Traffic accounting is read-only.** `NetworkServices` may read `TrafficStats` and normalize
    unsupported values, but must never silently toggle connectivity or add privileged control APIs.
12. **SQLite compatibility is explicit.** The file remains `datausage.db`; schema version 2 adds
    only an index migration, and ORMLite entity/table/field mappings plus R8 keep rules stay
    aligned. (`DatabaseHelper`, `entities/`, `app/proguard-rules.pro`.)
13. **Usage history is not backed up.** Explicit full-backup and data-extraction rules exclude
    `database/datausage.db`; do not loosen this without a privacy review. (`AndroidManifest.xml`,
    `res/xml/backup_rules.xml`, `res/xml/data_extraction_rules.xml`.)
14. **Platform boundaries are explicit.** Manifest components have intentional `exported` values;
    dynamic broadcasts are package-scoped/not-exported; notification `PendingIntent`s are immutable;
    no secrets or obsolete external-storage/network-control permissions are introduced.

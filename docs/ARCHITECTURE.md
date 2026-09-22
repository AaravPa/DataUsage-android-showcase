# Architecture

## System shape

The repository is one Java Android module (`:app`) with no backend. The current build uses AGP
9.4.0, Gradle 9.7.1, Java 17, compile/target API 37, min API 24, AndroidX Core 1.19.0,
Activity 1.13.0, Fragment 1.9.0, Preference 1.2.1, WorkManager 2.11.2, Google Mobile Ads 25.5.0,
and ORMLite 6.1.
Charts are native custom `Canvas` views; the old AChartEngine and bundled support/ads/ORM JARs are
gone. SQLite is accessed through ORMLite over the app-private `datausage.db` file.

## Components and ownership

| Component | Owns | Key paths |
| --- | --- | --- |
| Main shell | Embedded child-activity navigation with a modern bottom navigation surface, launch tab, first-run prompt, adjust/reset menu, and service start. | `activities/DataUsageActivity.java`, `res/layout/main.xml`, `res/layout/tab_indicator.xml` |
| Plan screens | Cellular/Wi-Fi totals, quota/projection state, progress rendering, update refresh. | `activities/PlanUsageActivity.java`, `views/DataUsageProgressBar.java`, `res/layout/planusage.xml` |
| Settings | AndroidX Preference UI and synchronization into the two default counters. | `activities/SettingsActivity.java`, `preference/QuotaPreference.java`, `res/xml/settings.xml` |
| Sampling coordinator | Durable WorkManager scheduling, one refresh transaction, widgets, alerts, broadcast. | `services/RefreshScheduler.java`, `services/UsageRefreshWorker.java`, `services/UsageRefreshCoordinator.java`, `domainservices/DataUsageService.java` |
| Traffic adapter | Read-only device/app TrafficStats counters and unsupported-counter normalization. | `domainservices/NetworkServices.java` |
| Persistence | Schema creation, seed rows, cached DAOs, ORMLite mapping. | `orm/sqlite/DatabaseHelper.java`, `entities/*.java` |
| Accounting | Date-range sums, recurring rollover, archive, reset/adjust, projections and colors. | `domainservices/UsageLogService.java`, `UsageCounterService.java`, `ArchivedCounterService.java` |
| Notifications | Threshold alert channels and one-shot preference flags. | `domainservices/NotificationService.java`, `entities/enums/AlertThresholds.java`, `PrefKeys.java` |
| History | Archived/current period lists, daily detail, native charts. | `activities/HistoryActivity.java`, `HistoryDetailActivity.java`, `views/PlanHistoryChart.java`, `PlanDetailHistoryChart.java`, `UsageBarChartView.java` |
| Widgets | Cellular/Wi-Fi widget update entry points and remote views. | `widget/*.java`, `services/UsageRefreshCoordinator.java`, `res/layout/widget_usagebar*.xml` |
| Support screens | Local help/FAQ and rating flow. Per-app usage is not shipped. | `activities/HelpActivity.java`, `FAQActivity.java`, `util/RatingHelper.java` |

## Data and control flow

1. `DataUsageActivity` schedules an immediate refresh and creates the cellular/Wi-Fi/history/settings
   tabs. It chooses the initial tab from `PrefKeys.LAUNCH_SCREEN_PREF`.
2. `DatabaseHelper.onCreate()` creates the alert, counter, log, and archive tables, inserts a
   zero-delta TrafficStats baseline, and creates default cellular/Wi-Fi counters.
3. `RefreshScheduler` enqueues unique immediate and periodic `UsageRefreshWorker` work. Legacy
   1-minute and 5-minute preference values are safely treated as 15 minutes.
4. `UsageRefreshWorker` serializes refreshes in-process. `UsageRefreshCoordinator` calls
   `DataUsageService.logUsage()`, refreshes widgets, checks counter rollover, processes alerts, and
   sends a package-scoped update broadcast. Plan screens reload asynchronously through
   `util/BackgroundExecutor`.
5. `UsageCounter` is plan metadata. `UsageLogService` sums log deltas by period; `UsageCounterService`
   derives remaining, ideal, daily, projected, weekly, and color values.
6. Settings preferences are the user-facing configuration source. The settings fragment updates
   default counter metadata and clears that network's alert flags when its plan changes.

## Boundaries and entry points

- Activities own lifecycle/navigation; domain services own calculations and database operations;
  `DatabaseHelper` owns schema/DAO creation; `NetworkServices` only reads platform counters.
- ORMLite reflection rules live in `app/proguard-rules.pro`; entity field/table compatibility is
  part of the persistence boundary.
- `RefreshScheduler` is called from `DataUsageActivity`, settings changes, and both widget
  providers. WorkManager owns persistence across process death; there is no refresh service or
  foreground-service permission.
- Launcher: `.activities.DataUsageActivity` in `app/src/main/AndroidManifest.xml`.
- Refresh: `services/RefreshScheduler.java`, `UsageRefreshWorker.java`, and
  `UsageRefreshCoordinator.java`.
- Widget entry points: `widget/DataUsageCellularUsageProvider.java` and `DataUsageWiFiUsageProvider.java`.

## Known compatibility boundary

The shell still uses ORMLite's `OrmLiteBaseTabActivity`, Android's `TabHost`, and embedded child
activities because replacing that contract requires converting the existing screen activities to
fragments. The legacy contract is wrapped in a modern light theme, card-based layouts, and a
custom bottom navigation indicator; do not expand the TabHost dependency or add new code against
it. All other migrated platform paths should use AndroidX/current APIs. Usage history is excluded
from cloud/device-transfer backups by explicit manifest backup rules; settings remain eligible.

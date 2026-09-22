# Data Usage agent guide

## Product

Data Usage is a self-contained Android app that samples device-wide cellular and Wi-Fi
`TrafficStats`, stores append-only usage samples in SQLite, and presents quota progress, billing
history, charts, alerts, widgets, help, and settings. It has no backend. The shipped package is
`com.sigterm`; the current minimum Android version is API 24.

## Repository map

| Path | Purpose |
| --- | --- |
| `build.gradle`, `settings.gradle`, `gradle/`, `gradlew*` | One-module Gradle/Android build. |
| `app/build.gradle`, `gradle.properties` | Android plugin, SDK, Java, dependencies, R8 settings. |
| `app/src/main/AndroidManifest.xml` | Components, permissions, exported boundaries, AdMob metadata. |
| `app/src/main/java/com/sigterm/activities/` | Main tab shell and user-facing screens. |
| `app/src/main/java/com/sigterm/domainservices/` | Sampling, accounting, persistence queries, alerts. |
| `app/src/main/java/com/sigterm/entities/`, `orm/sqlite/` | ORMLite models, schema, DAOs. |
| `app/src/main/java/com/sigterm/services/`, `widget/` | WorkManager refresh orchestration and widgets. |
| `app/src/main/java/com/sigterm/views/`, `preference/`, `util/` | Custom views, settings dialog, shared helpers. |
| `app/src/main/res/` | Layouts, preference XML, strings, drawables, widget metadata. |
| `app/src/test/` | JVM tests for pure utility behavior. |
| `docs/` | Durable repository knowledge; start with `docs/CONTEXT_INDEX.md`. |

## Canonical environment and commands

Use Android Studio's JDK 17 (or another JDK 17 installation), Android SDK platform 37, and Build
Tools 36.0.0. The checked-in wrapper uses Gradle 9.7.1 and the project uses Android Gradle Plugin
9.4.0. Run from the repository root:

```sh
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleRelease
```

For this workstation, prefix commands with `JAVA_HOME='/Applications/Android Studio.app/Contents/jbr/Contents/Home'`.
Use `docs/TESTING.md` for emulator/manual validation and known limitations.

## Critical rules and invariants

- `UsageLog` interval deltas are the source of plan totals; `UsageCounter` stores plan metadata.
- The first database log is a TrafficStats baseline; negative deltas are clamped to zero.
- Two default counters (cellular and Wi-Fi) must exist; quota `0` means unlimited and null dates
  mean no expiry. Preserve preference keys and ORMLite field/table mappings.
- `UsageRefreshWorker`/`UsageRefreshCoordinator` own refresh ordering: sample, rollover/alerts,
  widgets, then the package-scoped update broadcast. Periodic work is clamped to WorkManager's
  15-minute minimum; never reintroduce an endless foreground timer.
- `NetworkServices` is read-only. Automatic network enable/disable was removed because current
  Android does not provide a safe supported app API for that legacy behavior.
- Notification channels, explicit component export flags, immutable `PendingIntent`s, and the
  API 33 notification permission must remain correct.

## Sensitive paths and safe changes

Treat `DatabaseHelper.java`, entity classes, `UsageLogService.java`, `UsageCounterService.java`,
`DataUsageService.java`, `UsageRefreshWorker.java`, `UsageRefreshCoordinator.java`,
`SettingsActivity.java`, `QuotaPreference.java`,
`PrefKeys.java`, `app/build.gradle`, the manifest, and preference/layout resources as sensitive.
Prefer focused changes, preserve persisted names/semantics, and validate both cellular/Wi-Fi plus
unlimited/expiring plans. Do not add secrets or restore obsolete local JARs/API calls.

## Documentation protocol

Before starting a task:

1. Read `AGENTS.md`.
2. Read `docs/CONTEXT_INDEX.md`.
3. Select the minimum 1–3 documents relevant to the task.
4. Read only those documents plus relevant source code; do not recursively load all of `docs/`.

After completing a task:

1. Run the appropriate validation.
2. If architecture, product behavior, invariants, or durable decisions changed, update only the
   affected documentation.
3. Do not turn `docs/ACTIVE_WORK.md` into a task history.
4. If implementation changes make documentation inaccurate, correct it in the same change.

The deeper references are `docs/ARCHITECTURE.md`, `PRODUCT_BEHAVIOR.md`, `INVARIANTS.md`,
`TESTING.md`, `DECISIONS.md`, and `ACTIVE_WORK.md`; use the index to choose, not to load them all.

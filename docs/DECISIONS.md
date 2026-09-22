# Decisions

Decision: Target the current Android toolchain.
Why: The original API 17/Gradle 2.2/AGP 1.0 build could not run on a current JDK or SDK.
Alternatives rejected: Pinning obsolete runtimes or retaining abandoned local compatibility JARs.
Affected code: `build.gradle`, `settings.gradle`, `app/build.gradle`, `gradle/wrapper/`, manifest.
Status: active
Superseded by: —

Decision: Keep Java and the single `:app` module during modernization.
Why: The existing activity/ORMLite architecture can be brought forward without a wholesale rewrite.
Alternatives rejected: An unscoped Kotlin/Compose or multi-module rewrite.
Affected code: `app/src/main/java/`, `app/build.gradle`.
Status: active
Superseded by: —

Decision: Use Maven ORMLite 6.1 and preserve the SQLite mapping.
Why: 6.1 is the latest ORMLite release and the app depends on its Android helper/reflective entities.
Alternatives rejected: Replacing persistence during a toolchain migration or adding duplicate core JARs.
Affected code: `app/build.gradle`, `DatabaseHelper.java`, `entities/`, `app/proguard-rules.pro`.
Status: active
Superseded by: —

Decision: Replace AChartEngine with a native custom bar chart.
Why: The bundled chart library was abandoned and incompatible with current packaging.
Alternatives rejected: Adding another chart dependency for two simple bar-chart screens.
Affected code: `views/UsageBarChartView.java`, `PlanHistoryChart.java`, `PlanDetailHistoryChart.java`.
Status: active
Superseded by: —

Decision: Make traffic access read-only and remove automatic connectivity toggles.
Why: The old reflection-based network-control path is unsupported and unsafe on current Android.
Alternatives rejected: Hidden privileged APIs or a false promise that app-level toggles still work.
Affected code: `NetworkServices.java`, `DataUsageService.java`, settings XML, manifest.
Status: active
Superseded by: —

Decision: Run refresh as unique WorkManager work with a 15-minute minimum.
Why: Android background execution rules make an endless `dataSync` foreground timer unsafe and
bounded; WorkManager survives process death and retries failed samples.
Alternatives rejected: Endless foreground service, activity-only polling, or pretending to preserve
1-minute background cadence that current Android cannot guarantee.
Affected code: `services/RefreshScheduler.java`, `UsageRefreshWorker.java`,
`UsageRefreshCoordinator.java`, manifest, widget callers, settings XML.
Status: active
Superseded by: —

Decision: Preserve the existing embedded tab shell temporarily.
Why: It preserves the four-screen UX while the child activities still own their ORMLite helpers;
converting them all to fragments is a separate structural migration.
Alternatives rejected: Replacing tabs with unrelated launch buttons during this modernization.
Affected code: `DataUsageActivity.java`, `res/layout/main.xml`, ORMLite base activities.
Status: active
Superseded by: —

Decision: Remove the incomplete per-app usage scaffold.
Why: Its table creation and sampling were disabled and its query/counter code returned incorrect
or empty results; keeping dead paths increased audit surface without user-visible functionality.
Alternatives rejected: Re-enabling it without a correct per-network Android usage source.
Affected code: removed app-usage activities, entities, services, adapter, and layouts.
Status: active
Superseded by: —

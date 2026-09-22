# Testing and validation

## Toolchain

Use JDK 17, Android SDK API 37, Build Tools 36.0.0, wrapper Gradle 9.7.1, and AGP 9.4.0. On the
development workstation:

```sh
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew assembleDebug
```

## Fast and full checks

```sh
./gradlew assembleDebug          # compile/package normal APK
./gradlew testDebugUnitTest     # JVM utility tests
./gradlew lintDebug              # Android lint; must have zero errors
./gradlew assembleRelease       # R8/shrinker and release packaging
./gradlew clean check            # clean all available verification tasks
```

Current focused unit coverage is `app/src/test/java/com/sigterm/util/DataunitUtilsTest.java`.
There are no instrumentation tests or repository-owned backend/service tests; GitHub Actions runs
the debug build, unit tests, and lint in `.github/workflows/android.yml`.
`lintDebug` currently exits cleanly with no errors; remaining warnings are limited to legacy
indentation, asset-density duplication, and two structural layout hints.

## Subsystem coverage

| Subsystem | Automated check | Required manual check |
| --- | --- | --- |
| Build/resources/manifest | `assembleDebug`, `lintDebug` | Install and launch on an API 34+ emulator/device. |
| Unit arithmetic | `testDebugUnitTest` | Verify display/adjustment values in the UI. |
| Sampling/accounting | Compile only | Let WorkManager create multiple logs; check cellular/Wi-Fi totals and relaunch behavior. |
| Quotas/rollover/reset | Compile only | Exercise unlimited, limited, no-expiry, recurring, adjust, and reset paths. |
| Alerts | Compile only | Grant notifications, cross configured thresholds, verify one-shot behavior and reset. |
| History/charts | Compile only | Create multiple periods, open all filters and daily detail. |
| Widgets | Compile only | Add both widgets, wait for refresh, verify values/colors and tap targets. |
| Help/FAQ/ads | Compile only | Open local screens; intent-based actions and live ads require installed services/network. |

## Emulator smoke test

With an installed AVD and `adb`:

```sh
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell pm grant com.sigterm android.permission.POST_NOTIFICATIONS  # emulator test only
adb shell am force-stop com.sigterm
adb shell am start -W -n com.sigterm/.activities.DataUsageActivity
adb shell dumpsys activity activities | grep com.sigterm
adb logcat -d | grep -E 'FATAL EXCEPTION|AndroidRuntime|com.sigterm'
```

The current workstation smoke-tested the clean APK on the API 34 arm64 emulator: install and
launcher start succeeded, a WorkManager system job was scheduled and completed, the fresh database
contained the expected tables and three non-negative usage rows, and no app fatal exception was
logged. The first-run notification permission UI can make `am start -W` time out; that is not an
app crash. Non-exported child activities cannot be launched directly through `adb shell`; exercise
those through the in-app tabs.

## Limitations

- Tests do not cover Android lifecycle, SQLite migrations, TrafficStats hardware behavior, widgets,
  or foreground-service restrictions.
- Wi-Fi totals are derived from total minus mobile TrafficStats; physical devices and emulators can
  expose different counter support. Unsupported counters normalize to zero.
- Release output is unsigned unless signing credentials are supplied through a future signing
  configuration; it is not a store-signing validation.
- AdMob defaults to official Google test IDs for local builds. Set `ADMOB_APP_ID` and
  `ADMOB_BANNER_AD_UNIT_ID` in the build environment for release/publishing; the values are
  injected as a manifest placeholder/generated resource and are not stored in source control.

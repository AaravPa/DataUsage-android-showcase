# Product behavior

## Primary flows

- Launch shows Cellular, Wi-Fi, History, and Settings destinations in a persistent bottom
  navigation bar. `LAUNCH_SCREEN_PREF` chooses the first plan destination. The first launch shows
  setup text and routes to Settings after acknowledgement.
- Plan tabs show sent, received, today, this week, current-period usage, quota/progress, remaining
  quota, billing dates, and (only for limited expiring plans) ideal, projected, overage, and daily
  allowance values.
- Settings independently configures cellular/Wi-Fi quota (unlimited or KB/MB/GB), expiry and
  recurrence, refresh frequency, alert thresholds, display units, launch tab, week start, and
  progress colors.
- History switches between Cellular, Wi-Fi, and Both, lists archived periods plus a current
  snapshot, shows a native bar chart, and opens daily detail rows/chart for a selected period.
- Adjust/Reset accepts sent/received values in KB/MB/GB or resets the selected network, then sends
  `UsageUpdated` so visible plan screens refresh.
- Widgets show plan usage and color state; tapping one opens the main activity. Help/FAQ are local;
  email/market actions depend on installed handlers.
- The app requests `POST_NOTIFICATIONS` on API 33+. Refreshes run through WorkManager; the app does
  not keep an always-on foreground notification. The first refresh is immediate and periodic
  refresh frequency is 15, 30, or 60 minutes.

## Expected states and edge cases

| State | Expected behavior |
| --- | --- |
| Unlimited (`quotaInBytes == 0`) | Shows unlimited plan, 0% progress, and `--` for quota-only projections. |
| No expiry (both dates null) | Shows no expiry, `--` for remaining days/next bill/ideal fields. |
| Limited and expiring | Shows configured color, ideal marker, period dates, projections, and daily allowance. |
| Fresh install | Creates a baseline log and two default counters, with zero sampled usage. |
| Counter reset/backwards reading | Negative interval deltas become zero while the latest interface baseline advances. |
| Empty history | Shows the no-history state; default plan history normally includes a current snapshot. |
| Notification denied | Usage/accounting continue; only alert delivery is unavailable. |
| Unsupported TrafficStats | Unsupported/negative counters normalize to zero; the app remains usable. |

## Non-regression rules

- Cellular and Wi-Fi remain independent across counters, logs, tabs, history, widgets, and alert
  flags.
- Usage is cumulative over the plan date range, while each log row is a non-negative interval delta.
- Manual adjustment/reset remains visible through the same log/archive accounting path.
- Quota/unit/recurrence/start-date changes update the matching default counter and reset its alert
  state.
- Refresh ordering and the package-scoped `UsageUpdated` broadcast remain intact.
- Legacy automatic network shutoff/re-enable is intentionally no longer exposed or attempted;
  current Android does not support that old reflective behavior safely. Old preference keys may
  remain for database/preferences compatibility but have no active UI effect.
- Per-app usage is not a supported flow and is not exposed in the shipped app; its incomplete
  activity, sampler, DAO, and resources were removed.

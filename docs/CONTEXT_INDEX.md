# Context index

Use this table to select the smallest useful reading set. Implementation is authoritative; correct
the affected document when code and prose disagree.

| Document | Contains | Read when | Authoritative code paths |
| --- | --- | --- | --- |
| `AGENTS.md` | Agent workflow, repository map, commands, hard rules. | Every task. | Build files, manifest, sensitive paths named there. |
| `docs/CONTEXT_INDEX.md` | This routing table. | Every task, after `AGENTS.md`. | The documents listed here. |
| `docs/ARCHITECTURE.md` | Modules, ownership, data/control flow, entry points, dependencies. | Refactors, new screens/services, persistence, build/dependency work. | `app/build.gradle`, `activities/`, `domainservices/`, `services/`, `widget/`, `entities/`, `orm/sqlite/`. |
| `docs/PRODUCT_BEHAVIOR.md` | User flows, states, edge cases, non-regression behavior. | UI, workflow, quota, alert, widget, or manual QA work. | Main activities, `UsageRefreshCoordinator`, `res/layout/`, `res/xml/`, `res/values/`. |
| `docs/INVARIANTS.md` | Persistence, arithmetic, state, concurrency, security, and failure rules. | Sampling, plans, reset, schema, notifications, service, or network changes. | `DatabaseHelper`, `DataUsageService`, `UsageLogService`, `UsageCounterService`, `NetworkServices`, `NotificationService`, `PrefKeys`. |
| `docs/TESTING.md` | Fast/full validation, subsystem coverage, emulator steps, limitations. | Before builds, tests, release work, or CI changes. | `gradlew*`, Gradle files, `app/src/test/`, manifest. |
| `docs/DECISIONS.md` | Durable architectural/product choices and rejected alternatives. | Proposing or revisiting structural/toolchain decisions. | Build files, chart/settings/service/network paths. |
| `docs/ACTIVE_WORK.md` | Only incomplete work, active bugs, next steps, temporary migration state. | Resuming current work. | Verify every item against current source. |

Common routes:

- Build/dependency/toolchain: `ARCHITECTURE.md` → `TESTING.md` → Gradle files.
- Accounting/schema/alerts/service: `ARCHITECTURE.md` → `INVARIANTS.md` → focused source.
- UI/workflow/widgets: `PRODUCT_BEHAVIOR.md` → `ARCHITECTURE.md` → relevant screen/resources.
- Structural proposal: `ARCHITECTURE.md` → `DECISIONS.md` → `INVARIANTS.md`.

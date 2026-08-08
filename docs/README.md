# Docs

Project documentation. Architecture notes and build knowledge live here; the day-to-day coding
rules live in [`CLAUDE.md`](../CLAUDE.md).

**[revisit.md](revisit.md)** — the running list of things noticed mid-task and deliberately deferred.
Add to it instead of fixing out-of-scope problems inline.

## architecture/

How parts of the app work, and why they work that way.

| Doc | About |
|-----|-------|
| [top-app-bar.md](architecture/top-app-bar.md) | `TaigaTopAppBar` + `TopBarController` — the global, screen-owned top bar |
| [offline-support.md](architecture/offline-support.md) | Offline cache, `NetworkMonitor`, write-action disabling — per-phase status |
| [screen-state-flicker.md](architecture/screen-state-flicker.md) | Why `isLoading = false` as an initial value flickers on first frame |
| [kanban-filters.md](architecture/kanban-filters.md) | Kanban filtering + the three swimlane modes |
| `*.puml` | Startup state, navigation startup, screen-ready signal, issue/task promotion flows |

## build/

Gradle, AGP, packaging, release.

| Doc | About |
|-----|-------|
| [agp9-kmp.md](build/agp9-kmp.md) | AGP 9 rules specific to KMP modules (the `agp-9-upgrade` skill does not cover KMP) |
| [cmp-resources-android-fix.md](build/cmp-resources-android-fix.md) | Why CMP resources vanished from the APK, and the `androidResources.enable` fix |
| [rename-variant-outputs.md](build/rename-variant-outputs.md) | Renaming APK/AAB outputs in-place via the AGP Variant API |
| [release.md](build/release.md) | The release workflow, start to finish |
| [fdroid-reproducibility.md](build/fdroid-reproducibility.md) | F-Droid build/reproducibility failure and its cause |

## koin/

DI wiring. For everyday Koin questions use the **koin-expert** agent first — these are the long-form
writeups it links to.

| Doc | About |
|-----|-------|
| [koin-androidapp-migration.md](koin/koin-androidapp-migration.md) | Multi-platform module discovery: what broke in the AGP 9 split and the rules that came out of it |
| [workitem-edit-scoping.md](koin/workitem-edit-scoping.md) | Why work-item edit sessions use a keyed map instead of Koin scopes |

## taiga-api/

Reference material about the **Taiga backend**, not about this app.

| Doc | About |
|-----|-------|
| [websocket-events.md](taiga-api/websocket-events.md) | taiga-events: message queue backends, event shapes, subscriptions |
| [jwt-token-testing.md](taiga-api/jwt-token-testing.md) | Exercising invalid/expired tokens and the refresh flow |
| [settings-project-api.md](taiga-api/settings-project-api.md) | Project-settings endpoints with verified Kotlin types |
| [settings-attributes-api.md](taiga-api/settings-attributes-api.md) | Statuses, points, priorities, severities, custom fields, tags, swimlanes |

## features/

Plans and research for features in flight. One folder per feature.

| Feature | Docs |
|---------|------|
| [github-auth](features/github-auth/) | Loopback OAuth plan, sequence diagram, Taiga Docker setup |
| [private-cert-trust](features/private-cert-trust/) | TOFU cert pinning — research, phased plan, QA server setup |
| [settings-project](features/settings-project/) | Project-settings screens implementation plan |

## testing/

State of the test suite and the plan for improving it.

| Doc | About |
|-----|-------|
| [survey.md](testing/survey.md) | What the suite contains today — types, layout, tooling, coverage, gaps |
| [improvement-plan.md](testing/improvement-plan.md) | Sequenced tasks to improve it, one per session — active tasks only; tasks 0–9f and 10–11 are archived (see below) |
| [compose-ui-test-spike.md](testing/compose-ui-test-spike.md) | How-to for wiring `runComposeUiTest` into a KMP module — read before writing the next Compose UI test |

## issues/

Investigations of reported bugs, written before the fix. See the `investigate-issue` skill.

## archive/

Finished plans kept for their reasoning. Not current.

## local-info.md

Connection details for the local self-hosted Taiga instance used for manual testing.

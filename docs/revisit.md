# Revisit list

Things noticed while doing something else, deliberately **not** fixed at the time, and worth coming
back to. This exists so that "I'll remember that" stops being the plan.

**The rule:** when work surfaces a real problem outside the current task's scope, add a row here and
keep going. Do not fix it inline (it makes the diff unreviewable) and do not drop it. Every entry
needs enough evidence that a cold session can pick it up without re-deriving anything — a `file:line`
or a doc link, not just a description.

**Current agreement (2026-08-02):** finish the [testing improvement plan](testing/improvement-plan.md)
first, then work this list. Nothing here is urgent; nothing here is forgotten.

| # | Item | Size | Source |
|---|---|---|---|
| 1 | ViewModels doing I/O in `init` | M–L | [koingraphtest issue](issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md) |
| 2 | Non-ViewModel beans may leak application-scoped coroutines | S to check | same |
| 3 | Wiki mapper tests duplicate the new shared DTO factories | XS | improvement-plan task 3 |
| 4 | Dead `koin-test` block in `:testing` `androidMain` | XS | improvement-plan task 2 |
| 5 | `tools/seed` and `tools/utils` tests would not run in CI | XS | improvement-plan task 1 |
| 6 | `GetKanbanDataUseCase` reads the current project three times | XS | improvement-plan task 5 |
| 7 | Date formatters cache the locale for the process lifetime | S | improvement-plan task 6 |

---

## 1. ViewModels doing I/O in `init`

**What:** ten ViewModels start a `viewModelScope.launch` from their `init` block that immediately
hits the repository layer:

```
feature/settings/ui/user/SettingsUserScreenViewModel.kt
feature/wiki/ui/page/details/WikiPageViewModel.kt
feature/wiki/ui/bookmark/list/WikiBookmarksViewModel.kt
feature/wiki/ui/page/list/WikiPagesViewModel.kt
feature/scrum/ui/backlog/ScrumBacklogViewModel.kt
feature/scrum/ui/open/ScrumOpenSprintsViewModel.kt
feature/workitem/ui/screens/sprint/EditSprintViewModel.kt
feature/epics/ui/list/EpicsViewModel.kt
feature/sprint/ui/SprintViewModel.kt
feature/issues/ui/list/IssuesViewModel.kt
```

**Why it came up:** constructing one of these is enough to fire a real network/DB call. That is what
made `KoinGraphTest` leak exceptions into unrelated tests. The leak is fixed at the test end
(`Dispatchers.Main` is replaced by a dispatcher that never runs), but **the fix depends on the
design staying as it is** — the first `viewModelScope.launch(Dispatchers.IO)` written in an `init`
block bypasses it and the flake returns with nothing pointing at the cause.

**The actual question** — worth answering properly, not just mechanically changing all ten:

- Is "construct = start loading" the behaviour we want? It couples object creation to I/O, which is
  why the object is untestable and unmockable without a live dispatcher.
- The alternative is an explicit `onScreenStart()` / `LaunchedEffect` trigger from the UI. That is
  more code per screen and easy to forget, which is presumably why `init` was chosen.
- There may be a middle option: keep `init` but have it collect a flow rather than run a one-shot
  suspend call, so there is nothing to throw at construction time.

**Do not treat this as a mechanical refactor.** Pick the pattern first, apply it to one ViewModel,
confirm the screen still behaves, then decide whether the other nine are worth touching. If the
answer is "the current design is fine, the test fix is sufficient" — that is a legitimate outcome,
but write down *why*, because the question will come back.

**Blocked on nothing.** Best done after the testing plan, since those ViewModels will have tests by
then and the tests are the safety net for changing them.

## 2. Non-ViewModel beans may leak application-scoped coroutines

**What:** `KoinGraphTest` constructs ~147 definitions. Only ViewModel frames showed up in the leaked
stack traces, but any bean holding an application-scoped `CoroutineScope` could leak the same way,
and nothing has audited them.

**Why deferred:** no evidence it is actually happening, and the `Dispatchers.Main` fix covers any
bean that launches undispatched, whatever its type. So this is a "confirm the gap is empty" task, not
a known bug.

**How to check:** grep for `applicationScope` / injected `CoroutineScope` constructor params among
`@Single` classes, and look for `launch` in their `init` blocks.

## 3. Wiki mapper tests duplicate the new shared DTO factories

**What:** `WikiPageMapperTest` and `WikiLinkMapperTest` (in `feature/workitem/mapper/src/commonTest/`)
each define a private `createWikiPageDTO(...)` / `createWikiLinkDTO(...)`. Task 3 added
`getWikiPageDTO()` / `getWikiLinkDTO()` to `:testing` `models/WikiFakes.kt`, which now cover the same
ground.

**Why deferred:** the surgical-changes rule — those two files were not part of task 3, and editing
them would have widened its diff for no functional gain.

**Note:** the shared factories currently expose fewer parameters than the private ones (they fix
`ownerId`, `html`, `editions`, etc.). Adopting them means widening the factory signatures, so this is
slightly more than a find-and-replace.

## 4. Dead `koin-test` block in `:testing` `androidMain`

**What:** `testing/build.gradle.kts` declares `koin-test`, `koin-test-junit4` and `junit4` in
`androidMain`. Nothing can reach them — the repo has no Android unit-test source set by design, and
`KoinGraphTest` gets its own `koin-test` from `composeApp`'s `jvmTest`.

**Why deferred:** removing it was explicitly out of scope for improvement-plan task 2. It is dead
weight, not a bug.

**Watch for:** confirm nothing in `androidApp` picks these up transitively before deleting.

## 5. `tools/seed` and `tools/utils` tests would not run in CI

**What:** both are `kotlin("jvm")` modules, so their test task is `test`, not `jvmTest`. CI runs
`./gradlew jvmTest`, so a test added under `tools/` would silently never execute — the exact trap
improvement-plan task 1 closed everywhere else.

**Why deferred:** neither module has any tests today, so there is nothing to lose right now. Running
root `./gradlew test` instead would drag in `androidApp`'s per-flavor Android unit-test variants and
slow CI down for zero current benefit.

**Trigger:** the moment anyone adds a test under `tools/`, add a `./gradlew :tools:seed:test` step to
`.github/workflows/code_analysis.yml`. Worth doing pre-emptively if `tools/` grows.

## 6. `GetKanbanDataUseCase` reads the current project three times

**What:** `GetKanbanDataUseCaseImpl.getData` (`feature/kanban/domain/.../GetKanbanDataUseCase.kt:44-86`)
already has `projectsRepository.getCurrentProjectSimple()` in flight as `async`, then calls
`getPermissions()` twice more at lines 81-82 — and `ProjectsRepositoryImpl.getPermissions()` is
literally `getCurrentProjectSimple().myPermissions`
(`feature/projects/data/.../ProjectsRepositoryImpl.kt:92`). So one kanban load performs three reads
of the same row where one would do. Both permission flags are derivable from `project.await()`.

**Why deferred:** noticed while writing `GetKanbanDataUseCaseTest` (improvement-plan task 5), which
is a test-only task — changing the use case would have mixed a behaviour change into the diff that
verifies it. The tests now in place make the change safe to do next.

**Not a correctness bug**, just three DB reads per board load. The fix is a few lines: await the
project once and read `myPermissions` off it.

## 7. Date formatters cache the locale for the process lifetime

**What:** all three actuals of `platformFormatMediumDate` hold their formatter in a **private
top-level `val`**, so it is built once per process from whatever locale was current at
class-initialisation time and never rebuilt:

- `utils/formatter/datetime/src/androidMain/.../PlatformDateTimeFormat.android.kt:12`
- `utils/formatter/datetime/src/jvmMain/.../KotlinxDateTimeFormatter.jvm.kt:10`
- `utils/formatter/datetime/src/iosMain/.../KotlinxDateTimeFormatter.ios.kt:12` (`NSDateFormatter`,
  same shape)

Changing the system language does not kill the app process on Android or iOS — activities are
recreated, the process survives — so every date on screen keeps rendering in the **previous**
language until the process is killed. Building the formatter inside the function (or caching it
against the current locale) fixes it; `DateTimeFormatter.ofLocalizedDate` is cheap, and
`NSDateFormatter` can key its cache on `NSLocale.currentLocale`.

**Why deferred:** found while writing `KotlinxDateTimeFormatterJvmTest` (improvement-plan task 6),
a test-only task. The app has no in-app language switcher today, so the only trigger is a system
language change, which makes it low-severity rather than invisible.

**Watch for:** this is also *why* the JVM test cannot pin a locale — the `val` is initialised before
any `@BeforeTest` can run. Fixing this would make an exact-rendering test possible, which is the
cheapest way to prove the fix.

## 8. Kover's excludes are applied partially, and differently by `koverXmlReport` and `koverVerify`

**What:** the `excludes` block in the root `build.gradle.kts` (`kover { reports { filters { … } } }`)
is not fully honoured, and the two consumers of it disagree with each other. Measured on
2026-08-03 at `af8a185a`, same invocation, same artifacts:

| | LINE | BRANCH |
|---|---|---|
| `koverXmlReport` (uploaded to Codecov) | 65.30 % | 45.88 % |
| `:koverVerify` (the CI gate) | 60.47 % | 40.29 % |
| what the configured excludes *should* produce | 71.97 % | 49.73 % |

With **all** filters removed the two tasks agree to four decimal places (35.8939 % / 14.1084 %), so
the class universe is identical — the divergence is entirely in how each applies the excludes.

**Which entries silently no-op** (verified by deleting the `packages(…)` block and diffing the
package list in `report.xml`): of the seven `packages(…)` entries, only
`strings.generated.resources`, `core.storage.db` and `core.storage.cache` take effect.
`core.storage.db.dao`, `core.storage.db.wrapper`, `core.storage.di` and `core.storage.network` do
nothing. Class patterns fail in the same place: `**.*Module` leaves `DBModule`,
`AuthDataStoreModule`, `PlatformDBModule`, `PlatformStorageModule` in the report, and
`**.*Preferences*` leaves `LongPreferences` — while `**.*Repository`, `**.*Api`, `**.*Widget`,
`**.*Screen` and `**.*Delegate` match **zero** classes repo-wide, i.e. work perfectly everywhere
else.

**Every failing exclusion is in `:core:storage`.** No mechanism found for why that module is
special; that is the thing to work out first. The cost today is 982 lines of Room-generated
`SprintDao_Impl` / `WorkItemDao_Impl` at 1.6 % coverage sitting in the denominator, which is most of
the ~6-point gap between the real and reported figures.

**Why deferred:** found while setting the coverage floor (improvement-plan task 8), whose scope is
the gate, not Kover's filter engine. The gate is tuned to `:koverVerify`'s own numbers, so it is
correct and conservative as it stands — fixing this can only push coverage *up*, never break the
build.

**When fixing:** raise the bounds in the same commit, otherwise the floor goes ~12 points slack.
Reproduce with `./gradlew jvmTest :koverXmlReport :koverVerify` and compare the two figures; they
should be equal, and both should equal the "should produce" row above.

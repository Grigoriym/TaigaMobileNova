# Revisit list

Things noticed while doing something else, deliberately **not** fixed at the time, and worth coming
back to. This exists so that "I'll remember that" stops being the plan.

**The rule:** when work surfaces a real problem outside the current task's scope, add a row here and
keep going. Do not fix it inline (it makes the diff unreviewable) and do not drop it. Every entry
needs enough evidence that a cold session can pick it up without re-deriving anything — a `file:line`
or a doc link, not just a description.

**Current agreement (2026-08-02):** finish the [testing improvement plan](testing/improvement-plan.md)
first, then work this list. Nothing here is urgent; nothing here is forgotten.

**Still open** (the table only lists these; every other entry below carries a **Resolved** note in
its own section, kept for the reasoning rather than the outcome):

| # | Item | Size | Source |
|---|---|---|---|
| 1 | ViewModels doing I/O in `init` | M–L | [koingraphtest issue](issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md) |
| 24 | `KoinGraphTest` and the live-Taiga integration tests collide on the JVM `DataStore` file, order-dependently | S–M | [testing agent, "Integration test against a live server"](../.claude/agents/testing.md) |
| 27 | `ExpandableMarkdownTextTest` is flaky under a full `jvmTest` run (Skiko real-clock `waitUntil`) | S | this file, #27 |
| 29 | Login screen's server-URL regex rejects bare `localhost` (no dot in hostname) | XS | this file, #29 |
| 30 | `CrashReporter.recordException`/`.log` are unreachable on every non-Android platform | M | [desktop plan](desktop/linux-release-plan.md), this file #30 |
| 31 | Unused duplicate `ConnectivityManagerNetworkMonitor` in `androidApp` | XS | this file, #31 |
| 32 | No warning when the configured server URL is `http://` despite the bearer token being sent over it | S | this file, #32 |
| 33 | `TrustedCertificatesScreen` is reachable but permanently inert on iOS | S | this file, #33 |

<details>
<summary><strong>Full index (all 28 entries, resolved included)</strong> — this file is long because
resolved entries stay for their reasoning, not their outcome (see above), and ~20 links elsewhere in
the repo — including a frozen archive doc — point at specific entries by anchor, so they aren't
moved out. Expand for a one-line-per-entry jump table instead of scrolling.</summary>

| # | Item | Status |
|---|---|---|
| 1 | [ViewModels doing I/O in `init`](#1-viewmodels-doing-io-in-init) | 🟡 open |
| 2 | [Non-ViewModel beans may leak application-scoped coroutines](#2-non-viewmodel-beans-may-leak-application-scoped-coroutines) | ✅ resolved 2026-08-08 |
| 3 | [Wiki mapper tests duplicate the new shared DTO factories](#3-wiki-mapper-tests-duplicate-the-new-shared-dto-factories) | ✅ resolved 2026-08-07 |
| 4 | [Dead `koin-test` block in `:testing` `androidMain`](#4-dead-koin-test-block-in-testing-androidmain) | ✅ resolved 2026-08-07 |
| 5 | [`tools/seed` and `tools/utils` tests would not run in CI](#5-toolsseed-and-toolsutils-tests-would-not-run-in-ci) | ✅ resolved 2026-08-08 |
| 6 | [`GetKanbanDataUseCase` reads the current project three times](#6-getkanbandatausecase-reads-the-current-project-three-times) | ✅ resolved 2026-08-07 |
| 7 | [Date formatters cache the locale for the process lifetime](#7-date-formatters-cache-the-locale-for-the-process-lifetime) | ✅ resolved 2026-08-07 |
| 8 | [Kover's excludes are applied partially, and differently by `koverXmlReport` and `koverVerify`](#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify) | ✅ resolved 2026-08-07 |
| 9 | [`WikiRepositoryImplTest`'s failure tests can pass without reaching the SUT](#9-wikirepositoryimpltests-failure-tests-can-pass-without-reaching-the-sut) | ✅ resolved 2026-08-07 |
| 10 | [The `Plugin` and `Module` exclusion patterns hide real logic in `core/api`](#10-the-plugin-and-module-exclusion-patterns-hide-real-logic-in-coreapi) | ✅ resolved 2026-08-08 |
| 11 | [`TokenRefreshPlugin`'s `MAX_RETRIES` guard is unreachable](#11-tokenrefreshplugins-max_retries-guard-is-unreachable) | ✅ resolved 2026-08-07 |
| 12 | [Two small dead spots in `core/api`](#12-two-small-dead-spots-in-coreapi) | ✅ resolved 2026-08-07 |
| 13 | [`urlDecode` in `utils/ui` is dead code with three actuals](#13-urldecode-in-utilsui-is-dead-code-with-three-actuals) | ✅ resolved 2026-08-07 |
| 14 | [The Kover coverage floor is far below actual](#14-the-kover-coverage-floor-is-far-below-actual-1722-points-in-2026-08-03-2940-by-2026-08-05) | ✅ resolved 2026-08-07 |
| 15 | [Saving a non-editable custom field leaks its id into `editingItemIds`](#15-saving-a-non-editable-custom-field-leaks-its-id-into-editingitemids) | ✅ resolved 2026-08-07 |
| 16 | [Every `logcat` message lambda is a permanently-uncovered line](#16-every-logcat-message-lambda-is-a-permanently-uncovered-line) | ✅ resolved 2026-08-08 |
| 17 | [`StringPreference` and `LongPreferences` in `core/storage` are dead code](#17-stringpreference-and-longpreferences-in-corestorage-are-dead-code) | ✅ resolved (partial) 2026-08-07 |
| 18 | [`currentUserStory` throws from `viewModelScope` when the initial load failed](#18-currentuserstory-throws-from-viewmodelscope-when-the-initial-load-failed) | ✅ resolved 2026-08-08 |
| 19 | [`TeamMemberUpdate.Clear` is a dead `when` arm in four ViewModels](#19-teammemberupdateclear-is-a-dead-when-arm-in-four-viewmodels) | ✅ resolved 2026-08-07 |
| 20 | [`mapResult` is dead code, and its "unreachable" state is reachable](#20-mapresult-is-dead-code-and-its-unreachable-state-is-reachable) | ✅ resolved 2026-08-07 |
| 21 | [`SprintPagingSource` is dead code, and it is invisible to Kover](#21-sprintpagingsource-is-dead-code-and-it-is-invisible-to-kover) | ✅ resolved 2026-08-07 |
| 22 | [`EpicShortInfoDTO` is built by hand in three test files; no `:testing` factory exists](#22-epicshortinfodto-is-built-by-hand-in-three-test-files-no-testing-factory-exists) | ✅ resolved 2026-08-07 |
| 23 | [The coverage report counts Android-variant classes no test can reach](#23-the-coverage-report-counts-android-variant-classes-no-test-can-reach) | ✅ resolved 2026-08-08 |
| 24 | [`KoinGraphTest`/live-Taiga integration tests collide on the JVM `DataStore` file](#24-koingraphtest-and-the-live-taiga-integration-tests-collide-on-the-jvm-datastore-file-order-dependently) | 🟡 open |
| 25 | [`FiltersStorageImplTest.resetFilters clears every section` is flaky](#25-filtersstorageimpltestresetfilters-clears-every-section-is-flaky-under-a-full-jvmtest-run) | ✅ resolved 2026-08-08 |
| 26 | [`WikiPageViewModelTest.onAttachmentAdd failure updates state with error` is flaky](#26-wikipageviewmodeltestonattachmentadd-failure-updates-state-with-error-is-flaky-under-a-full-jvmtest-run) | ✅ resolved 2026-08-08 |
| 27 | [`ExpandableMarkdownTextTest.longTextShowsExpandButtonAndTogglesOnClick` is flaky](#27-expandablemarkdowntexttestlongtextshowsexpandbuttonandtogglesonclick-is-flaky-under-a-full-jvmtest-run) | 🟡 open |
| 28 | [`CLAUDE.md` has grown too big; split the Kover ranking heuristics out into their own doc](#28-claudemd-has-grown-too-big-split-the-kover-ranking-heuristics-out-into-their-own-doc) | ✅ resolved 2026-08-09 |
| 29 | [Login screen's server-URL regex rejects bare `localhost`](#29-login-screens-server-url-regex-rejects-bare-localhost) | 🟡 open |
| 30 | [`CrashReporter.recordException`/`.log` are unreachable on every non-Android platform](#30-crashreporterrecordexceptionlog-are-unreachable-on-every-non-android-platform) | 🟡 open |
| 31 | [Unused duplicate `ConnectivityManagerNetworkMonitor` in `androidApp`](#31-unused-duplicate-connectivitymanagernetworkmonitor-in-androidapp) | 🟡 open |
| 32 | [No warning when the configured server URL is `http://`](#32-no-warning-when-the-configured-server-url-is-http-despite-the-bearer-token-being-sent-over-it) | 🟡 open |
| 33 | [`TrustedCertificatesScreen` is reachable but permanently inert on iOS](#33-trustedcertificatesscreen-is-reachable-but-permanently-inert-on-ios) | 🟡 open |

</details>

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

**Investigated (2026-08-08):** the testing plan is done, so this was picked up. Full options writeup
in [docs/issues/2026-08-08-viewmodel-init-io.md](issues/2026-08-08-viewmodel-init-io.md) — three
options (status quo / explicit `onScreenStart()` trigger / lazy `stateIn(WhileSubscribed)`
collection), recommending the explicit-trigger option. Not yet decided or implemented.

## 2. Non-ViewModel beans may leak application-scoped coroutines

**What:** `KoinGraphTest` constructs ~147 definitions. Only ViewModel frames showed up in the leaked
stack traces, but any bean holding an application-scoped `CoroutineScope` could leak the same way,
and nothing has audited them.

**Why deferred:** no evidence it is actually happening, and the `Dispatchers.Main` fix covers any
bean that launches undispatched, whatever its type. So this is a "confirm the gap is empty" task, not
a known bug.

**How to check:** grep for `applicationScope` / injected `CoroutineScope` constructor params among
`@Single` classes, and look for `launch` in their `init` blocks.

**Resolved (2026-08-08):** the gap is empty. `grep -rl "@Single" | xargs grep -l "CoroutineScope"`
across the repo returns exactly two classes:

- `AuthStateManager` (`core/storage/.../auth/AuthStateManager.kt`) takes `@param:ApplicationScope
  applicationScope: CoroutineScope` and calls `applicationScope.launch { logoutSuspend() }` — but only
  from `logout()`, an explicit call site, never from `init` or a property initializer. Constructing it
  does nothing.
- `FiltersStorageImpl` (`core/storage/.../FiltersStorageImpl.kt:23`) builds its **own** scope
  (`CoroutineScope(Dispatchers.Main + SupervisorJob())`, not the injected application one) and does
  eagerly-shared `stateIn(scope, SharingStarted.Eagerly, ...)` on four `StateFlow`s at construction
  time. This *is* the same shape — construction-time coroutine work — but it is not an unaudited gap:
  `FiltersStorageImplTest` already exercises it and its own doc comment (`:20-23`) records the design
  explicitly.

Confirmed the same for the two nearby shapes the check didn't originally name: `grep -rn
"GlobalScope"` and `grep -rn "MainScope()"` are both empty repo-wide, and `grep -rlz "init {"` piped
into a check for `launch` returns zero non-ViewModel matches. Nothing to fix.

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

**Resolved (2026-08-07):** widened `getWikiPageDTO()` in `:testing` `WikiFakes.kt` with the three
params the private factories actually override (`lastModifierId`, `isWatcher`, `version`) — not the
full private-factory parameter list, since nothing needed the rest (`ownerId`, `createdDate`,
`modifiedDate`, `html`, `editions`, `totalWatchers` stay randomized, matching what every call site
already relied on). `getWikiLinkDTO()` needed no changes — none of its callers ever overrode a
param. Deleted all three private factories (`WikiPageMapperTest`, `WikiLinkMapperTest`, and a third
one in `PatchedDataMapperTest` this entry hadn't originally counted) and switched their call sites to
the shared ones. `:feature:workitem:mapper:jvmTest`, `:feature:wiki:data:jvmTest`, `:testing:jvmTest`
and `ktlintCheck` all green.

## 4. Dead `koin-test` block in `:testing` `androidMain`

**What:** `testing/build.gradle.kts` declares `koin-test`, `koin-test-junit4` and `junit4` in
`androidMain`. Nothing can reach them — the repo has no Android unit-test source set by design, and
`KoinGraphTest` gets its own `koin-test` from `composeApp`'s `jvmTest`.

**Why deferred:** removing it was explicitly out of scope for improvement-plan task 2. It is dead
weight, not a bug.

**Watch for:** confirm nothing in `androidApp` picks these up transitively before deleting.

**Resolved (2026-08-07):** removed the whole `androidMain.dependencies { ... }` block from
`testing/build.gradle.kts`. `composeApp`'s own `jvmTest` still declares `koin-test` directly for
`KoinGraphTest`, confirmed unaffected — `./gradlew jvmTest` and `:koverVerify` both stayed green.

## 5. `tools/seed` and `tools/utils` tests would not run in CI

**What:** both are `kotlin("jvm")` modules, so their test task is `test`, not `jvmTest`. CI runs
`./gradlew jvmTest`, so a test added under `tools/` would silently never execute — the exact trap
improvement-plan task 1 closed everywhere else.

**Why deferred:** neither module has any tests today, so there is nothing to lose right now. Running
root `./gradlew test` instead would drag in `androidApp`'s per-flavor Android unit-test variants and
slow CI down for zero current benefit.

**Trigger:** the moment anyone adds a test under `tools/`, add a `./gradlew :tools:seed:test` step to
`.github/workflows/code_analysis.yml`. Worth doing pre-emptively if `tools/` grows.

**Resolved, won't-fix (2026-08-08):** gregory's decision — `tools/` doesn't need tests. Closing
without a CI change; if that ever changes, re-open with the trigger above.

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

**Resolved (2026-08-07):** awaited `project` once into a `currentProject` local and read
`currentProject.myPermissions.canAddUserStory()` / `.canModifyUserStory()` and
`currentProject.defaultSwimlane` off it, replacing the two extra `projectsRepository.getPermissions()`
calls and the third `project.await()`. `GetKanbanDataUseCaseTest`'s `getData maps permissions onto the
add and modify flags` test set permissions via `FakeProjectsRepository.permissions`, a field
independent of `getCurrentProjectSimpleResult` — that only worked because production code used to call
`getPermissions()` separately. Updated the test to set `myPermissions` on
`getCurrentProjectSimpleResult` instead, matching the real relationship (`getPermissions()` was always
just `getCurrentProjectSimple().myPermissions`). `FakeProjectsRepository.permissions` is still used by
thirteen other test files, so it was not removed. `:feature:kanban:domain:jvmTest`, the full
`./gradlew jvmTest`, `ktlintCheck` and `:koverVerify` all green.

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

**Resolved (2026-08-07):** changed all three actuals from a top-level cached `val` to a private
function that builds the formatter fresh on every call — `mediumFormatter()` in
`PlatformDateTimeFormat.android.kt`, `KotlinxDateTimeFormatter.jvm.kt` and
`KotlinxDateTimeFormatter.ios.kt`. `DateTimeFormatter.ofLocalizedDate` / `NSDateFormatter` read the
current default locale each time they're constructed, so a locale change now takes effect on the
very next call instead of only after a process restart. Added
`KotlinxDateTimeFormatterJvmTest.\`medium date picks up a locale changed after the formatter was
first used\`` — it calls the formatter once under `Locale.US`, switches the default to
`Locale.GERMANY`, and asserts the *exact* rendering matches a JDK formatter built fresh with that
locale. This is the exact-rendering test the entry above says was previously impossible, now possible
because the formatter is no longer baked in at class-init time; updated the test file's class doc
comment to match. `./gradlew jvmTest ktlintCheck` both green (one `core/storage` `jvmTest` failure on
the first run was the known cross-module `CoroutineExceptionHandler` flake — see CLAUDE.md, Testing —
confirmed by re-running `:core:storage:jvmTest` alone, which passed).

## 8. Kover's excludes are applied partially, and differently by `koverXmlReport` and `koverVerify`

**Resolved (2026-08-07) — and the premise was wrong.** Investigated properly against Kover 0.9.9's
own sources: [docs/issues/2026-08-07-kover-excludes-and-report-mode-flip.md](issues/2026-08-07-kover-excludes-and-report-mode-flip.md).
Both headline claims are false, so the ~100 lines of analysis and four contradicted trigger
hypotheses that used to live here have been deleted rather than archived — they would only send the
next reader down the same path. What replaced them:

- **The two tasks cannot apply excludes differently.** `VariantReportsSet.kt:87` and `:110` hand
  `koverXmlReport` and `koverDoVerify` the *same* filter object from the same root config, resolved
  through the same `collectAllFiles()`. Measured in one invocation: XML LINE 9214/9712 =
  94.872323 % / BRANCH 1643/2057 = 79.873602 %, against `:koverVerify`'s own 94.872300 % /
  79.873600 %. Agreement to six significant figures. The historical "~5 points apart" was always a
  comparison between *different invocations*.
- **No `excludes` entry silently no-ops.** `packages("a.b")` becomes the class pattern `a.b.*`
  (`ReportsImpl.kt:349`) and Kover's `*` matches dots (`#` is its non-dot wildcard,
  `KoverFeatures.kt:23`) — so a listed package covers its subpackages. `core.storage.db.dao` and
  `core.storage.db.wrapper` are **redundant** with `core.storage.db`, not broken, and deleting a
  redundant entry changing nothing is exactly what "delete an entry and diff the package list"
  should show. Nothing is special about `:core:storage`.
- **What actually varies is the denominator.** Kover's report task ends its file collection in
  `.existing()` (`AbstractKoverReportTask.kt:85`), and the root aggregates each module's *total*
  variant, which includes the KMP Android library target (`KotlinMultiPlatformLocator.kt:84`). A
  class is counted iff its compiler output is on disk — so an Android build or a KSP re-run since the
  last `clean` changes the class universe. That is the whole 742 / 744 / 781 / 787 / 798 spread, and
  it is why every "the trigger is X" hypothesis died: the trigger was never the edit.
- **The one real bug was ours.** `kover-rank.py:56` matched excluded packages by equality where Kover
  matches by prefix, so it kept `…core.storage.db.entities` — the script's own documented "745 not
  742, LINE ~53 high" caveat, misdiagnosed the same way. Fixed to prefix matching in the same session.

**Still not explained:** the 821–854 counts with ~20 suffix leaks were not reproduced; today's two
runs leaked 1 class and 0 classes respectively, and the one leak (`UtilsUiModule` vs `**.*Module`) is
intermittent and worth <=1 line. Tracked as open question 1 in the issue doc. CI has never been
affected — it always measures a fresh checkout.

**Consequences elsewhere:** the false comment at `build.gradle.kts:95-99` is gone, CLAUDE.md's
Testing section lost the workarounds built on the false premise, and [#14](#14-the-kover-coverage-floor-is-far-below-actual) is
resolved (the floor was raised to 92/77 against a same-invocation reading). [#10](#10-the-plugin-and-module-exclusion-patterns-hide-real-logic-in-coreapi)
was untouched by all of this at the time — it was a question about whether five Ktor plugins
*should* be excluded, not about whether exclusion works — and was resolved separately on 2026-08-08.
The deferred half is now [#23](#23-the-coverage-report-counts-android-variant-classes-no-test-can-reach).

---

## 9. `WikiRepositoryImplTest`'s failure tests can pass without reaching the SUT

**What:** the seven `should propagate api error` tests in
`feature/wiki/data/src/commonTest/…/WikiRepositoryImplTest.kt` assert with a bare
`assertFailsWith<IllegalStateException> { … }`. `testException` is an `IllegalStateException` — but
so is every fake's own `error("… not set")` guard, so each of those tests also goes green if
`FakeWikiApi` bails out *before* the repository reaches the code the test claims to cover. They are
correct today; they are just not load-bearing.

**Fix:** swap `assertFailsWith<IllegalStateException> { … }` for `assertFailsWithTestException { … }`
(`:testing`, `utils/TestUtils.kt`), which matches type **and** message. Seven one-line edits plus an
import.

**Why deferred:** found while establishing the failure-path convention in improvement-plan task 9,
whose scope was the convention plus `feature/workitem/data`. Editing an unrelated module's test file
would have made that diff harder to review, and the tests are not currently wrong.

**Resolved (2026-08-07):** swapped all seven `assertFailsWith<IllegalStateException> { … }` call
sites for `assertFailsWithTestException { … }` and replaced the now-unused `kotlin.test.assertFailsWith`
import with `com.grappim.taigamobile.testing.utils.assertFailsWithTestException`. Test-only change, no
production code touched. `:feature:wiki:data:jvmTest`, the full `./gradlew jvmTest` and
`ktlintCheck` all green.

---

## 10. The `Plugin` and `Module` exclusion patterns hide real logic in `core/api`

**What:** the root `build.gradle.kts` excludes `**.*Plugin` and `**.*Module` (plus the `$*` nested
variants) as "architecture boilerplate". In `core/api` that is wrong — five of the classes it drops
are the app's entire HTTP behaviour, not boilerplate:

| Class | Hand-written LINE / BRANCH hidden |
|---|---|
| `TokenRefreshPlugin$Plugin$install$1` | 50 / 14 — the whole 401-refresh, mutex and retry path |
| `ErrorMappingPlugin$Plugin` (+ `$install$1`) | 24 / 10 — status mapping, project-limit headers |
| `DebugLocalhostPlugin$Plugin$install$1` | 9 / 6 |
| `HostSelectionPlugin$Plugin$install$1` | 8 / 4 |
| `AuthHeaderPlugin$Plugin$install$1` | 7 / 4 |

That is ~98 lines and 38 branches of genuine conditional logic — auth, error translation, host
rewriting — excluded purely because the class names end in `Plugin`. Improvement-plan task 9a wrote
55 tests covering all of it and the reported package coverage barely moved (`core/api` LINE
55/74 → 63/74, BRANCH 26/50 → 28/50; `core/api/errors` did not move at all), because the tested
classes are not in the report.

**Fix:** narrow the two patterns so they only catch what they were meant to catch — the Koin
`@Module` classes and the generated Koin module facades — rather than any class whose name happens
to end in `Plugin`/`Module`. Naming the Koin modules explicitly, or excluding by package, would do
it. `KmpNetworkModule` (a real Koin `@Module`) should stay excluded; the five Ktor plugins should
not.

**Why deferred:** improvement-plan task 9a's scope is tests for one module, and changing the
excludes moves the whole project's reported coverage — see entry 8, which has to be resolved in the
same breath. Doing both here would have made the test diff unreviewable. When fixed, the
`:koverVerify` bounds must be re-tuned in the same commit.

**Resolved (2026-08-08):** removed `"Plugin"` from the `variants(...)` suffix list in root
`build.gradle.kts` (kept `"Module"` — every `grep`-able `*Module` class in the repo is a real Koin
`@Module`, so that half of the pattern was never wrong). A repo-wide grep confirmed core/api's five
Ktor plugins are the *only* `*Plugin`-suffixed classes that exist, so the pattern was catching
nothing else — this was a pure win, not a tradeoff against some other boilerplate class it also
needed to exclude. Kept `docs/testing/kover-rank.py`'s `SUFFIXES` list in sync (same one-line
removal).

Verified in isolation with `docs/testing/kover-diff.py` against a before/after pair: the only
`<class>`-level entries that appeared were the five Plugin classes (and their `$Plugin`/`$Config`/
`$install$1` nested classes) newly present in `core.api`/`core.api.errors`; the only `<package>`-level
denominator changes were those same two packages growing (LINE `core.api` 63/74 → 173/186,
`core.api.errors` 52/54 → 89/92; BRANCH `core.api` 28/50 → 54/76, `core.api.errors` 47/76 → 57/86).
Some `core.storage.cache`/`core.storage.db` classes also dropped out of the class-universe between
the two invocations — the already-documented [#23](#23-the-coverage-report-counts-android-variant-classes-no-test-can-reach)
Android-variant-class noise, confirmed unrelated because it shows up only as package-level movement
in a package this change never touched, with zero class-level movement outside `core.api`/
`core.api.errors`.

A clean `./gradlew clean jvmTest koverXmlReport` (matching CI's fresh-checkout state) reproduced the
same reading: LINE 94.9199 % (9361/9862), BRANCH 80.2198 % (1679/2093), 761 classes, 0 excluded-suffix
leaks. Cross-checked against `:koverVerify` itself (temporarily set both `minValue`s to 99 in the same
invocation, per CLAUDE.md's Testing section): it reported the identical 94.919900 % / 80.219800 %.
That is *higher* than the previous 94.8723 %/79.8736 % reading the 92/77 floor was tuned from — the
five newly-counted Plugin classes are well-tested (`core.api` LINE 93.0 %, BRANCH 71.1 %; `core.api.errors`
LINE 96.7 %, BRANCH 66.3 %) from improvement-plan task 9a's 55 tests, so adding them raised the
aggregate rather than lowering it. The 92/77 floor still has ~3 points of margin on both counters, so
it was left unchanged; only the comment above it was updated with the new measurement and date.
`./gradlew jvmTest`, `ktlintCheck` and `:koverVerify` all green.

---

## 11. `TokenRefreshPlugin`'s `MAX_RETRIES` guard is unreachable

**What:** `core/api/src/commonMain/…/TokenRefreshPlugin.kt:56-63` reads a `retryCountKey` attribute
and logs out once it reaches `MAX_RETRIES` (3). The counter is only ever written at the end of the
same interceptor invocation (`:82`, `:96`, `:123`), and `execute(request)` inside an
`HttpSend.intercept` block dispatches to the **next** sender in the chain — it does not re-enter the
interceptor that called it. So `retries` is 0 on every real request and the guard never fires.

**Evidence:** `TokenRefreshPluginTest` drives a MockEngine that answers 401 to everything. If the
interceptor re-entered, the retry loop would run until the cap and issue 4 requests; it issues
exactly 2. The test that covers the guard has to seed the attribute on the request builder by hand.

**Consequence:** a server that answers 401 to both the original request *and* the retry-with-a-
fresh-token leaves the user logged in with a token that does not work, rather than logging out.
Whether that is worth fixing depends on whether it happens in practice — the refresh call itself
failing *is* handled (`:103-111`), and that is the common case.

**Fix, if wanted:** loop inside the interceptor instead of relying on the attribute, or drop the
counter and the constant. Do not "fix" it by installing the plugin twice.

**Why deferred:** found while writing tests for the plugin (improvement-plan task 9a). It is a
behaviour change to auth code, which is not a test task's business.

**Resolved (2026-08-07):** investigated via the `investigate-issue` skill —
[docs/issues/2026-08-07-tokenrefreshplugin-max-retries-unreachable.md](issues/2026-08-07-tokenrefreshplugin-max-retries-unreachable.md).
Confirmed the root cause against Ktor's `HttpSend` source (`execute()` always dispatches to the next
sender in the chain, never back into the calling interceptor) and against the plugin's own test suite,
where two tests already demonstrated the silent-failure shape without meaning to. Removed the dead
`MAX_RETRIES`/`retryCountKey` machinery and replaced it with a `retryOrLogout` check after each of the
three retry call sites: if the retry also comes back 401, log out instead of returning it silently.
`:core:api:jvmTest`, the full `./gradlew jvmTest` and `ktlintCheck` all green.

---

## 12. Two small dead spots in `core/api`

Both found while testing the module (improvement-plan task 9a); neither is a bug, and neither is
worth its own commit — fold them into the next change that touches these files.

- **`defaultTryCatch`'s second `catch` is unreachable.**
  `TryCatchExtensions.kt:10` catches `TimeoutCancellationException` after `:8` has already caught
  `CancellationException`, which is its supertype. Behaviour is correct either way (both rethrow);
  the clause is just dead.
- **`ErrorMappingPlugin` holds a `Json` it never uses.** It is taken in `Config`, stored as a
  constructor property (`ErrorMappingPlugin.kt:17`) and read nowhere — parsing goes through
  `ErrorResponseParser`, which has its own. Removing it means touching both `KmpNetworkModule`
  install blocks.

**Resolved (2026-08-07):** both fixed. `defaultTryCatch`'s `TimeoutCancellationException` clause is
gone (its import too) — the existing `TryCatchExtensionsTest` test for it still passes unchanged,
since `CancellationException`'s clause already rethrows the subtype. `ErrorMappingPlugin`'s `json`
was dropped from the constructor and `Config`, and both `install(ErrorMappingPlugin) { this.json =
httpJson; ... }` call sites in `KmpNetworkModule.kt` and the one in `ErrorMappingPluginTest.kt` had
their `this.json = ...` line removed. `./gradlew jvmTest`, `ktlintCheck` and `:koverVerify` all green.

---

## 13. `urlDecode` in `utils/ui` is dead code with three actuals

**Where:** `utils/ui/src/commonMain/…/JsonSerializableNavType.kt:32` (`internal expect fun
urlDecode`), plus actuals in `JsonSerializableNavType.android.kt:7`, `.ios.kt:8` and `.jvm.kt:8`.

**Evidence:** `grep -rn "urlDecode" --include=*.kt . | grep -v build/` returns exactly those four
declarations and no call site. `urlEncode` is used by `serializeAsValue` on both nav types;
`parseValue` decodes the JSON directly (`Json.decodeFromString`) without ever URL-decoding, because
Navigation has already decoded the argument by the time it reaches `parseValue`.

**Consequence:** none at runtime — it is four lines of unused `expect`/`actual` and an unnecessary
`io.ktor.http.decodeURLPart` import on JVM/iOS. It does cost the reader a moment wondering why
`parseValue` does not call it.

**Why deferred:** found while writing `JsonSerializableNavTypeTest` (improvement-plan task 9a).
Deleting production declarations is not a test task's business. Note that
`JsonSerializableNavTypeTest` currently *calls* `urlDecode` to reverse `serializeAsValue`, so
removing it means rewriting those two assertions (the honest replacement is a decoded-literal
comparison, which would then be JVM-specific).

**Resolved (2026-08-07):** removed the `expect`/`actual` declarations (`commonMain`, `androidMain`,
`iosMain`, `jvmMain`) and the now-unused `decodeURLPart` imports on iOS/JVM. The two
`JsonSerializableNavTypeTest` assertions that called `urlDecode` were replaced with a new
`JsonSerializableNavTypeJvmTest` (`utils/ui/src/jvmTest/`) that decodes with
`io.ktor.http.decodeURLPart` directly — the same function the removed JVM/iOS actual delegated to —
proving the round trip still holds without reintroducing the dead production function. One gotcha:
the new file's `private data class Payload` collided with `JsonSerializableNavTypeTest`'s own
file-private `Payload` at JVM bytecode level (`jvmTest` compiles alongside `commonTest` for the same
target, and Kotlin's file-`private` top-level classes aren't name-mangled the way private
functions/properties are) — renamed to `JvmPayload` to fix it. `./gradlew jvmTest`, `ktlintCheck` and
`:koverVerify` all green.

---

## 14. The Kover coverage floor is far below actual (~17/22 points in 2026-08-03, ~29/40 by 2026-08-05)

**Where:** root `build.gradle.kts:100-112` — `Line coverage` ≥ 58, `Branch coverage` ≥ 38.

**Evidence:** on 2026-08-03, after the `utils/ui` sweep, `./gradlew :koverVerify` reports
**75.42 % line / 60.52 % branch** (read by temporarily setting both bounds to 99). Task 8 set the
bounds from a `:koverVerify` run that reported 60.47 / 40.29 the same day. CLAUDE.md states the
floor is a ratchet, so it is due a raise.

**The reason it was not raised here, and what to check first:** the gap is too large to be explained
by the tests added since. Tasks 9a's three modules plus this one added roughly 250 covered branches
against a 2049 denominator — about 12 points, not 20. So **`:koverVerify` itself may flip between
the excludes-applied and excludes-not-applied modes**, exactly as `koverXmlReport` does
([#8](#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify)),
and task 8's 60.47 / 40.29 may have been a run on the other side. Raising the floor to just under
75/60 would then break CI on the next flip.

**New evidence that narrows #8:** in this session `:koverVerify` (75.4249 / 60.5173) agreed *to four
decimal places* with `kover-rank.py`'s filtered totals over a genuine 742-class `koverXmlReport`
(75.42 % / 60.52 %). So the "~5 points apart" claim in #8 and CLAUDE.md describes `koverVerify`
against an **821/854-class** XML run, not an intrinsic difference between the two tasks: when the
XML lands on the 742 side, all three numbers coincide. That makes `kover-rank.py`'s output a
readout of the gate number, not merely an approximation of it.

**The gap has widened a lot since — re-measure before acting on the numbers above.** On 2026-08-05,
after ~15 further 9a sweep sessions, `kover-rank.py` over a clean 742-class report reports
**86.73 % line (8428/9717) / 78.38 % branch (1617/2063)** — i.e. the floor is now ~29 points below
actual on line and ~40 on branch, not 17/22. Per the note above, that readout *is* the gate number
when the XML lands on the 742 side, so the widening is real rather than a mode artifact.

**Fix:** take `:koverVerify` readings on several separate clean-tree invocations. If they are stable,
raise the bounds to a couple of points under the lowest reading. If they flip, that is a bigger
finding than the floor and belongs in #8.

**Resolved (2026-08-07):** the blocker named above — "`:koverVerify` itself may flip between the
excludes-applied and excludes-not-applied modes" — is ruled out. `koverXmlReport` and `koverVerify`
are handed the same filter object and the same artifacts (`VariantReportsSet.kt:87` and `:110`), and
in one invocation they agreed to six significant figures: XML 94.872323 % LINE / 79.873602 % BRANCH
against the gate's 94.872300 % / 79.873600 %. See
[#8](#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify) and
[the issue doc](issues/2026-08-07-kover-excludes-and-report-mode-flip.md). So the correct procedure
is not "several clean-tree readings" but **one reading from the same invocation that produced the XML**.

Raised the bounds to **line 92 / branch 77**, ~3 points under the 2026-08-07 measurement, and
replaced the comment above them (which asserted the two tasks disagree). `./gradlew :koverVerify`
green. Note the reading was taken on a local tree carrying Android compilation outputs, which only
*depresses* the figure (those classes can never be covered under `jvmTest`) — CI, on a fresh
checkout, sits at or above it, so the new floor has margin on the side that matters.

---

## 15. Saving a non-editable custom field leaks its id into `editingItemIds`

**Where:** `feature/workitem/ui/.../delegates/customfields/WorkItemCustomFieldsDelegateImpl.kt:107` —
the `onSuccess` arm of `handleCustomFieldSave` calls `onCustomFieldEditToggle(item)` unconditionally.

**What happens:** `onCustomFieldEditToggle` is a *toggle*, and the call is there to close edit mode
after a successful save. But `CustomFieldsWidget.kt:262-276` renders the save button for **every**
item type, enabled purely by `item.isModified` — edit mode is only ever entered for `EditableItem`s
(`RichTextItemState`, `UrlItemState`), whose edit button is the sole caller of the toggle
(`CustomFieldsWidget.kt:236-243`). So saving a `Text`/`Number`/`Date`/`Dropdown`/`Checkbox` field —
none of which are `EditableItem` — *adds* its id to `editingItemIds` rather than removing it, and
nothing ever takes it out again for the life of the screen.

**Consequence:** none visible today. The set is read in exactly one place,
`CustomFieldsWidget.kt:131` — `val isEditMode = isEditableItem && item.id in editingItemIds` — whose
first conjunct is false for precisely the items that leak in. It is unbounded state growth guarded
by an accident, and it will produce a real bug the moment a second reader of `editingItemIds`
appears, or an existing item type is made `EditableItem`.

**Fix:** make the post-save call an explicit removal rather than a toggle (or guard it with
`if (item is EditableItem)`). Covered by
`WorkItemCustomFieldsDelegateImplTest.handleCustomFieldSave - success - adds a non-editing item to
editingItemIds`, which documents the current behaviour and will need inverting.

**Why deferred:** found while writing that test (improvement-plan task 9a). Changing delegate
behaviour used by four detail ViewModels is not a test task's business.

**Resolved (2026-08-07):** guarded the `onCustomFieldEditToggle(item)` call in
`WorkItemCustomFieldsDelegateImpl.handleCustomFieldSave`'s success arm with `if (item is
EditableItem)`. Inverted the test that documented the leak — renamed to `handleCustomFieldSave -
success - a non-editable item never enters editingItemIds` and it now asserts `editingItemIds` stays
empty; the sibling `handleCustomFieldSave - success - closes the edit mode of an editable item` test
already covers the `EditableItem` path and needed no change. `:feature:workitem:ui:jvmTest`, the full
`./gradlew jvmTest` and `ktlintCheck` all green.

---

## 16. Every `logcat` message lambda is a permanently-uncovered line

**Where:** 96 `logcat` call sites across `feature/`, `core/`, `utils/`, `composeApp/` and `main`
(`grep -rn "logcat" --include=*.kt feature core utils composeApp main | grep -v /build/ | grep -v "import\|core/logger" | wc -l`).

**What happens:** `TaigaLogger.logger` defaults to the private `NoLog` object, whose `log()` is a
no-op that never invokes the `message: () -> String` lambda it is handed
(`core/logger/.../TaigaLogger.kt`). `install()` is called from the Android and iOS entry points only
— CLAUDE.md's Logging table already records that **Desktop/JVM installs nothing**. So under
`jvmTest`, every `logcat { "…" }` message lambda is compiled to a synthetic method that no test can
enter. Kover counts each as one missed LINE, carrying zero branches.

**Consequence:** measurement noise with a consistent signature. In fully-covered code it shows up as
a 1-line residual per `logcat` call — e.g. `feature/workitem/ui/screens/edittags` finished at LINE
124/127 with two of the three holes being exactly this (`onTagClick$lambda$0$2`, the not-found
warning, and `fetchTags$1.invokeSuspend$lambda$3$0`, the fetch-failure log). Upper bound repo-wide
is ~96 lines of the 9709 LINE denominator, i.e. under 1 point. **A session that chases one of these
to zero is chasing an unreachable line**, which is why it is written down rather than fixed.

**Fix, if it is ever judged worth it:** install a counting `TaigaLogger` in `:testing` that invokes
`message()` and discards the result. That reclaims all ~96 lines and would additionally let tests
assert *that* something was logged, which nothing can do today.

**Why deferred:** `TaigaLogger.logger` is a `@Volatile` process-global, and all modules' JVM tests
share one process (CLAUDE.md, Testing). Installing it from a `@BeforeTest` anywhere changes global
state for every concurrently-running test in the suite; doing it safely means a single install at
suite scope, which is a build/infra change, not a test task. Found while closing improvement-plan
task 9a's `edittags` module.

**Correction to the title (2026-08-04, task 9a `modules`):** not *every* one — the ~96 upper bound
stands, but whether the lambda becomes its own synthetic method varies. `ModulesViewModel`'s two
`logcat` calls, both inside `viewModelScope.launch { }` arms that the failure tests take, were folded
into the covered `invokeSuspend` and cost nothing; that package finished at LINE **88/88**.
`EditSprintViewModel`'s, in the same syntactic position, was split out at 0/1. So the 1-line-hole
signature is still the right thing to stop at, but 100 % LINE is not out of reach a priori, and the
"~96 lines reclaimable" figure is an upper bound rather than a count.

**Resolved, won't-fix (2026-08-08):** gregory's decision — closing without installing a counting
`TaigaLogger`. The entry's own numbers already say why: upper bound is ~96 lines against a >9700-line
denominator (under 1 point), and the only real fix means installing a test-scope logger that changes
`@Volatile` global state shared by every concurrently-running JVM test in the suite (CLAUDE.md,
Testing) — infra risk for sub-1-point gain. If a future session wants `logcat` assertions in tests for
some other reason, revisit then; don't chase this line count on its own.

---

## 17. `StringPreference` and `LongPreferences` in `core/storage` are dead code

**Where:** `core/storage/src/androidMain/kotlin/com/grappim/taigamobile/core/storage/utils/StringPreference.kt`
and `.../utils/LongPreferences.kt`.

**Evidence:** `grep -rn "StringPreference\|LongPreferences" --include=*.kt . | grep -v /build/`
returns only the two declaration files themselves — no call site anywhere in the project, including
`androidApp`. They are `ReadWriteProperty` delegates over `SharedPreferences`, i.e. the pre-DataStore
storage mechanism; every storage class in the module now takes a `DataStore<Preferences>` instead.

**Consequence:** none at runtime. In the coverage report they show as `StringPreference` BRANCH 0/2
LINE 0/10 and `StringPreferenceKt` LINE 0/2 — permanently, since `androidMain` code is unreachable
from `jvmTest` and the repo has no Android unit-test source set by design. Anyone ranking
`core/storage` by missed lines will keep re-finding them and re-concluding they are untestable, which
is true but beside the point: they should not exist.

**Why deferred:** found while writing the `core/storage` tests (improvement-plan task 9a). Deleting
production files is not a test task's business, and the surgical-changes rule says to mention
unrelated dead code rather than remove it.

**Correction, partially resolved (2026-08-07):** the class-name grep above was the wrong evidence for
`StringPreference` — it missed the actual call site because that call goes through the extension
function built on top of the class, not the class name. `ServerStorageImpl.kt:21`
(`core/storage/src/androidMain/.../server/ServerStorageImpl.kt`) does
`sharedPreferences.string(key = SERVER_KEY, defaultValue = ...)`, where `.string(...)` is
`StringPreference.kt`'s own extension function — `git blame` shows `ServerStorageImpl` predates this
entry, so it was live the whole time and the original grep simply didn't search for it. Deleting the
file broke `:core:storage:compileAndroidMain` (`Unresolved reference 'utils'` /
`Unresolved reference 'string'`) the first time this was attempted; **restored** it via
`git checkout HEAD -- .../StringPreference.kt`.
`LongPreferences.kt` had no equivalent gap — `grep -rn "\.long(" --include=*.kt . | grep -v /build/`
confirmed zero call sites for its `.long(...)` extension either — so **it alone was deleted**. Lesson
for the next dead-code grep: search for a class's *extension functions*, not just its own name, when
the class exists specifically to be used through one.

## 18. `currentUserStory` throws from `viewModelScope` when the initial load failed

**Where:** `feature/userstories/ui/.../UserStoryDetailsViewModel.kt:198-199`

```kotlin
private val currentUserStory: UserStory
    get() = requireNotNull(_state.value.currentUserStory)
```

**Evidence:** the same ViewModel guards the identical read in two other places —
`doOnDelete` (`:477`) does `_state.value.currentUserStory?.id ?: return@launch`-style null handling,
and `onGoingToEditEpics` (`:823`) uses `_state.value.currentUserStory?.userStoryEpics`. Sixteen other
handlers go through the `requireNotNull` getter instead.

**Consequence:** if `loadUserStory` failed, `currentUserStory` throws `IllegalArgumentException`
inside a `viewModelScope.launch`, which is an uncaught coroutine exception, not a caught error. Most
of the sixteen callers are UI callbacks that the error state presumably hides, but **`onEpicsUpdate`
is not** — it is driven by `workItemEditStateRepository.getEpicsFlow(...)`, i.e. by a *different*
screen finishing its edit, and fires regardless of whether this screen loaded. Same for
`onNewTagsUpdate`, `onNewDescriptionUpdate` and `handleTeamMemberUpdate`'s two branches.

The three sibling details ViewModels (`TaskDetailsViewModel`, `EpicDetailsViewModel`,
`IssueDetailsViewModel`) have the same getter shape — check them together. **Confirmed for
`EpicDetailsViewModel` (2026-08-05):** `currentEpic` (`:162-163`) is one of the two residual missed
branches after that module's sweep, for exactly this reason. **Confirmed again for
`IssueDetailsViewModel` (2026-08-05):** `currentIssue` (`:199-200`), likewise one of that module's two
residuals. **And for `TaskDetailsViewModel` (2026-08-05):** `currentTask` (`:198-199`), same story.
**All four are now measured and all four behave identically** — the getter is a permanent 1 missed
branch in every details package, so nothing further is learned by re-deriving it; act on it or leave
it, but do not re-measure.

**Consequence for tests:** this is why `getCurrentUserStory` stays at BRANCH 1/2. Covering the throw
arm requires letting an exception escape a `viewModelScope` coroutine, which under
`kotlinx-coroutines-test` gets attributed to whichever `runTest` is live in *another module* (the
`KoinGraphTest` failure mode, see `docs/issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md`).
So the branch is not merely untested — it is untestable until the code stops throwing.

**Why deferred:** found while writing the `feature/userstories/ui` tests (improvement-plan task 9a).
Changing a null-handling policy across four ViewModels is a production change, not a test task's
business.

**Resolved, not a bug (2026-08-08):** gregory's design intent is that `null` means "we don't show any
UI," so nothing should ever be able to reach `currentUserStory` while it's null — traced the four
"external-trigger" call sites this entry flagged as the risky case, and the invariant holds. The only
producers of `handleTeamMemberUpdate` / `onNewTagsUpdate` / `onNewDescriptionUpdate` / `onEpicsUpdate`'s
input flows are `onEditTags()`, `onGoingToEditWatchers()`, `onGoingToEditAssignees()` and
`onGoingToEditEpics()` (`UserStoryDetailsViewModel.kt:239-266`, `:823`) — all called from this same
ViewModel's own UI, to hand data to an edit sub-screen. And `currentUserStory` is never reset back to
`null` once set (`:298`, `:845`, `:907` are all `.copy(...)`, never null). So reaching any of the four
"external" handlers requires having already navigated to an edit screen, which requires the initial
load to have already succeeded — the write-back is causally downstream of the load. There is no path
that reaches the getter while it's null.

**What's actually left:** this is an *unenforced* invariant rather than a live bug — nothing in the
type system stops a future navigation change, or a second entry point into the same
`WorkItemEditStateRepository` flow, from breaking it into a crash instead of a graceful no-op. That's
also why the branch can't get a test today (see "Consequence for tests" above) — there's no way to
drive it without breaking the invariant by hand. Decision: leave the four getters as `requireNotNull`,
don't change the null-handling policy. If this ever needs revisiting, it'll be because the navigation
graph changed, not because of anything found here.

## 19. `TeamMemberUpdate.Clear` is a dead `when` arm in four ViewModels

**Where:** `UserStoryDetailsViewModel.kt:708`, `TaskDetailsViewModel.kt:715`,
`IssueDetailsViewModel.kt:469`, `EpicDetailsViewModel.kt:282` — each
`TeamMemberUpdate.Clear -> {}`.

**Evidence:** `grep -rn "TeamMemberUpdate.Clear" --include=*.kt . | grep -v /build/` returns exactly
those four lines and nothing else. The only producers of `TeamMemberUpdate` are
`WorkItemEditStateRepository.updateAssignee` / `.updateAssignees` / `.updateWatchers`, which send
`Assignee`, `Assignees` and `Watchers` respectively. Nothing constructs `Clear`.

**Consequence:** none at runtime — it is an empty arm. In coverage it is a permanent 1 missed branch
per ViewModel (4 total), and it is the sole reason `handleTeamMemberUpdate` sits at BRANCH 5/6 in
each. Anyone sweeping one of those four packages will re-derive this. **Confirmed by measurement for
all four (2026-08-05: `UserStoryDetailsViewModel`, `EpicDetailsViewModel`, `IssueDetailsViewModel`,
`TaskDetailsViewModel`)** — in each the `Clear` line settles at exactly `mb=1 cb=1` once any other arm
is exercised, since a non-`Clear` send covers its false branch and nothing can cover the true one.
All four packages have now been swept, so this entry is closed as re-derivation risk; it stays open
as a dead-code question.

**Why deferred:** found while writing the `feature/userstories/ui` tests (improvement-plan task 9a).
Deleting the arm — or the `Clear` variant itself, if it has no purpose — is a production change
across four files, and the surgical-changes rule says to mention unrelated dead code rather than
remove it. Check whether `Clear` is meant to be sent by something unimplemented before deleting.

**Resolved (2026-08-07):** re-grepped first — still zero producers of `Clear` anywhere
(`WorkItemEditStateRepository` only ever sends `Assignee`/`Assignees`/`Watchers`). Deleted the
`data object Clear : TeamMemberUpdate` variant (`TeamMemberUpdate.kt:9`) and the four dead `when` arms
in `TaskDetailsViewModel`, `UserStoryDetailsViewModel`, `EpicDetailsViewModel` and
`IssueDetailsViewModel` — each `when` stays exhaustive over the remaining three variants with no `else`
needed. `./gradlew jvmTest`, `ktlintCheck` and `:koverVerify` all green.

## 20. `mapResult` is dead code, and its "unreachable" state is reachable

**Where:** `core/domain/src/commonMain/kotlin/com/grappim/taigamobile/core/domain/ResultExtension.kt:39-45`.

**Evidence:** `grep -rn "mapResult" --include=*.kt . | grep -v /build/` returns only the declaration
itself — **zero call sites** across the whole repo (2026-08-05).

Separately, the implementation decides success from `getOrNull() != null`:

```kotlin
val successResult = getOrNull()
return when {
    successResult != null -> resultOf { transform(successResult) }
    else -> Result.failure(exceptionOrNull() ?: error("Unreachable state"))
}
```

so a **successful** `Result` holding `null` falls into the `else` branch, finds no exception, and
throws `IllegalStateException("Unreachable state")`. Asserted by
`ResultExtensionTest.mapResult throws on a success holding null` — the test documents the trap
rather than endorsing it. `Result.success<String?>(null).mapResult { … }` is the reproduction.

**Consequence:** none today, since nothing calls it. It becomes a live bug the moment someone uses
`mapResult` on a `Result<T?>` — which is exactly the shape a nullable API response takes. The fix is
to branch on `isSuccess` / `fold` rather than on nullability.

**Why deferred:** found while writing `ResultExtensionTest` (improvement-plan task 9a, `core/domain`).
Both the deletion and the null-handling fix are production changes to a `commonMain` utility, and the
surgical-changes rule says to record unrelated dead code rather than remove it. Decide first whether
`mapResult` is meant to have callers; if not, deleting it resolves both halves at once.

**Resolved (2026-08-07):** re-confirmed zero call sites (still only the declaration itself). Deleted
`mapResult` from `ResultExtension.kt` and its six tests from `ResultExtensionTest.kt` (the `region
mapResult` block plus the cross-function `resultOf feeding mapResult carries a failure through both`
test), along with the now-unused `assertNull`/`assertIs` imports. `resultOf` itself is untouched.
`:core:domain:jvmTest`, the full `./gradlew jvmTest`, `ktlintCheck` and `:koverVerify` all green — one
`uikit:jvmTest` Skiko `ComposeTimeoutException` flake appeared on the first full run and reproduced on
a clean stashed tree with no changes present, then passed on rerun, confirming it predates and is
unrelated to this change.

## 21. `SprintPagingSource` is dead code, and it is invisible to Kover

**Where:** `feature/sprint/data/src/commonMain/kotlin/com/grappim/taigamobile/feature/sprint/data/SprintPagingSource.kt`
(41 lines).

**Evidence:** `grep -rn "SprintPagingSource" --include=*.kt --include=*.kts . | grep -v /build/`
returns **only the class declaration itself** — zero construction sites, zero DI registrations
(2026-08-05). Sprint paging is served by `SprintRemoteMediator` + `SprintDao.pagingSource(...)`
instead; `SprintsRepositoryImpl.getSprintsPaging` builds its `Pager` from the Room DAO, not from this
`PagingSource`.

**Why it went unnoticed:** the root `kover` `excludes` block drops `**.*PagingSource`, so the class
does not appear in `report.xml` at all — not as 0 %, not as anything. A dead class that is also
excluded is doubly invisible: neither the coverage report nor a missed-branch ranking can surface it.
That is the general lesson worth keeping, and it is the same shape as
[#10](#10-the-plugin-and-module-exclusion-patterns-hide-real-logic-in-coreapi): **grep the module for
classes absent from the report before concluding the report covers the module.**

**Consequence:** none at runtime — it is unreachable. It is ~40 lines of maintenance surface that
looks load-bearing, and it duplicates the mediator's paging logic with *different* behaviour (it
returns `LoadResult.Error` via `defaultTryCatch` and computes `nextKey` itself), so a future edit
could plausibly be made to the wrong one.

**Why deferred:** found while writing `SprintRemoteMediatorTest` (improvement-plan task 9a,
`feature/sprint/data`). Deleting it is a production change unrelated to that task's diff; the
surgical-changes rule says to record it. Check `WorkItemPagingSource` and any other `*PagingSource`
for the same condition when acting on this — task 9b (`WorkItemRemoteMediator`) is the natural moment.

**Resolved (2026-08-07):** re-confirmed zero construction/DI sites, then deleted the file. It was a
plain class, not `@Single`-annotated, so `SprintDataModule`'s `@ComponentScan` was never picking it up
— no DI wiring to touch. `WorkItemPagingSource` was not checked against the same condition in this
session; that check is still open if anyone wants it. `./gradlew jvmTest`, `ktlintCheck` and
`:koverVerify` all green.

## 22. `EpicShortInfoDTO` is built by hand in three test files; no `:testing` factory exists

**Where:** `feature/workitem/mapper/src/commonTest/…/WorkItemMapperTest.kt:108,114` (inline),
`feature/workitem/mapper/src/commonTest/…/UserStoryShortInfoMapperTest.kt:82` (private
`createEpicShortInfoDTO()` helper), and now
`feature/userstories/mapper/src/commonTest/…/UserStoryMapperTest.kt` (private `getEpicShortInfoDTO()`).

**Evidence:** `grep -rn "EpicShortInfoDTO" testing/src/` returns nothing (2026-08-05) — every other
DTO of this shape has a factory in `:testing/models`. It is a four-field `@Serializable data class`.

**Related, and the more useful half:** `getWorkItemResponseDTO()` hard-codes `epics = null`
(`testing/src/commonMain/…/models/WorkItemFakes.kt:36`). That default is why
`UserStoryMapper.epicsToDomain`'s entire body sat at LINE 0/6 — no test could reach it without a
`.copy(epics = …)`. Check the factory's other hard-coded `null`s (`userStoryExtraInfo`,
`dueDateStatusDTO`, `fromTaskRef`) for the same effect when a row's LINE gap looks unexplained.

**Why deferred:** found while closing improvement-plan task 9a's `feature/userstories/mapper` row.
Adding the factory means editing `:testing`, which is the one edit correlated with `koverXmlReport`
flipping into its leaky mode and costing the session's comparable before/after pair (see CLAUDE.md,
Testing). Not worth that for a four-field data class on its own — fold it in when a session is
already touching `:testing` for another reason, and de-duplicate the three local copies then.

**Resolved (2026-08-07):** added `getEpicShortInfoDTO(id, title, ref, color)` to `:testing`
`WorkItemFakes.kt` (next to `getWorkItemResponseDTO`, whose `epics` field is what it feeds) and
switched all three call sites to it — `WorkItemMapperTest`'s two inline constructions (now
`getEpicShortInfoDTO(color = "#FF0000")` / `getEpicShortInfoDTO(color = "#00FF00")`),
`UserStoryShortInfoMapperTest`'s private `createEpicShortInfoDTO()`, and
`UserStoryMapperTest`'s private `getEpicShortInfoDTO()`. Left the `getWorkItemResponseDTO().epics =
null` default untouched — that half of the entry is about a *different* factory default and wasn't
part of this de-duplication. Removed the now-unused `EpicShortInfoDTO`/`getRandomLong`/
`getRandomString` imports each edit orphaned. `.claude/agents/testing.md`'s fake inventory updated.
`:feature:workitem:mapper:jvmTest`, `:feature:userstories:mapper:jvmTest`, the full `./gradlew
jvmTest` and `ktlintCheck` all green.

## 23. The coverage report counts Android-variant classes no test can reach

**Where:** root `build.gradle.kts` `kover { reports { filters { excludes` — what is *not* in it.

**Mechanism** (established in [the #8 investigation](issues/2026-08-07-kover-excludes-and-report-mode-flip.md),
finding 3): Kover's report task ends its file collection in `.existing()`
(`AbstractKoverReportTask.kt:85`), so a class is counted iff its compiler output is on disk. The root
aggregates each module's **total** variant, and `locateKotlinMultiplatformVariants`
(`KotlinMultiPlatformLocator.kt:32-84`) locates both the plain `jvm` target *and* the KMP Android
library target as JVM origins. Native targets are not located, so iOS classes never appear — but
Android ones do.

**Consequence:** the denominator depends on whether an Android build or a KSP re-run has happened
since the last `clean`, which is the entire observed 742 / 744 / 781 / 787 / 798 class-count spread.
Every class it adds is permanently 0 %, because CI runs `jvmTest` only and the repo has no Android
unit-test source set by design. Concrete example from a clean 2026-08-07 report:
`utils/formatter/decimal/DecimalFormatterModule_androidKt` at LINE 0/1, sitting beside its covered
`_jvmKt` twin. This is also the root of three separate CLAUDE.md warnings (`*_androidKt` rows are
dead weight; `StringPreference` is unreachable from `jvmTest`; Room `*_Impl` leaks) and of
[#17](#17-stringpreference-and-longpreferences-in-corestorage-are-dead-code).

**Why deferred:** this was option C of the #8 investigation and gregory chose option A. Excluding
those classes changes *what the project measures* at the same moment as the floor ratchet, and the two
should not land in one commit. #8's docs and floor work landed first precisely so this has a
trustworthy baseline to be measured against.

**When doing it:** there is **no DSL path to a JVM-only aggregated report** — checked. The root is not
a Kotlin project so it has no `jvm` variant, and `addWithDependencies("jvm", optional = true)` then
"will not be searched even in dependencies" (`KoverVariantConfig.kt:271`); `excludedSourceSets`
filters by *compilation* name (`JvmVariantArtifacts.kt:57-72`) and both targets' compilation is named
`main`. So the realistic implementation is a name/package denylist (`**.*_androidKt`, Room `*_Impl` /
`TaigaDB_Impl`, `core.storage.utils`) — which is the same brittleness
[#10](#10-the-plugin-and-module-exclusion-patterns-hide-real-logic-in-coreapi) complains about, and
that tension is the real decision to make. Re-tune the floor in the same commit, and keep
`docs/testing/kover-rank.py`'s lists in sync.

**Resolved (2026-08-08):** added two `excludes` entries to root `build.gradle.kts` — `classes("**.*_androidKt", "**.*_androidKt$*")`
and `packages(..., "com.grappim.taigamobile.core.storage.utils")` — and the matching `EXTRA_SUFFIXES`/`PACKAGES`
entries in `docs/testing/kover-rank.py`.

- **The Room `*_Impl`/`TaigaDB_Impl` half needed no new pattern.** Checked the compiled output directly
  (`find . -name "*_Impl.class"` under both `classes/kotlin/android` and `classes/kotlin/jvm`) —
  `TaigaDB_Impl`/`ProjectDao_Impl`/`SprintDao_Impl`/`WorkItemDao_Impl` all exist on disk for both
  targets, but they're already inside the `core.storage.db`/`core.storage.db.dao` packages the
  existing excludes cover (confirmed prefix-matching works, per [#8](#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify)).
  Zero `*_Impl` classes appear in a `koverXmlReport` run.
- **The `*_androidKt` half is real**: a clean `./gradlew clean jvmTest koverXmlReport` (761 classes,
  0 leaks) showed exactly 8 classes matching `**.*_androidKt`, all at 0 % on every counter —
  `PlatformNetworkErrorMapper_androidKt` (BRANCH 0/14, LINE 0/10 — CLAUDE.md's own worked example of
  this exact shape), `GithubOAuthWebViewDialog_androidKt` (0/8, 0/15), `OpenByDefaultSettingsButton_androidKt`
  (0/6, 0/13), and five smaller ones. `core.storage.utils` contributed `StringPreference`
  (0/2, 0/10) and its `Kt` facade (0/0, 0/2) — the class [#17](#17-stringpreference-and-longpreferences-in-corestorage-are-dead-code)
  restored as live-but-Android-only code.
- **Verified in isolation with `kover-diff.py`** against the before/after pair: package-level, exactly
  the 5 packages containing those classes had their denominators shrink (`core.domain`,
  `feature.login.ui`, `feature.settings.ui.user`, `utils.formatter.datetime`,
  `utils.formatter.decimal`) plus `core.storage.utils` dropping out of the key set entirely.
  Class-level, exactly the 10 targeted classes left the key set and nothing else — no `covered` count
  changed anywhere, confirming the change is a pure denominator trim.
- **New reading, same invocation** (`:koverVerify` with both `minValue`s temporarily at 99, per
  CLAUDE.md's method): **LINE 95.481400 %, BRANCH 81.386300 %**, up from the pre-#23 94.9199 %/80.2198 %
  — a 0.56-point LINE rise and a 1.17-point BRANCH rise, matching the relative sizes of what was
  removed (46 missed LINE / 28 missed BRANCH from the `_androidKt` classes vs. 12 missed LINE / 2
  missed BRANCH from `core.storage.utils`). Raised the floor to **line 92 (unchanged — already ~3.5
  points of margin) / branch 78 (+1, keeping the same ~3-point margin convention)** — not a full
  ~3-points-under-95.48/81.39 reset, since the line side didn't move enough to justify a bump and the
  point is a ratchet, not a re-target.
- **What this does *not* close**: androidMain-only classes with unique names that don't end in
  `_androidKt` — e.g. `AndroidDecimalFormatter` (`utils/formatter/decimal`, BRANCH 0/0, LINE 0/5) —
  are still counted and still permanently 0 %. They're individually named rather than a
  pattern-matchable shape, so denylisting them one at a time would be the same brittleness
  [#10](#10-the-plugin-and-module-exclusion-patterns-hide-real-logic-in-coreapi) already warns about,
  for a residual on the order of single-digit lines per class. Left alone; re-open only if the residual
  grows enough to matter. `./gradlew jvmTest`, `ktlintCheck` and `:koverVerify` all green.

## 24. `KoinGraphTest` and the live-Taiga integration tests collide on the JVM `DataStore` file, order-dependently

**Where:** `composeApp/src/jvmTest/kotlin/com/grappim/taigamobile/di/KoinGraphTest.kt` (line 95) and
`LiveTaigaSession.kt` (line 43) — both call `koinApplication<KoinApp> { ... }.koin` in the same test
JVM process, and neither closes the `Koin` instance it builds.

**Mechanism:** `StorageModule.jvm.kt`'s DataStores read/write fixed paths under `java.io.tmpdir`
(documented in CLAUDE.md's Testing section: "only one `koinApplication<KoinApp>` per test JVM process
may touch the JVM `DataStore` files"). `LiveTaigaSession.kt`'s `sharedSession` works around this
*among the live-Taiga tests themselves* by memoizing one shared graph behind a `Lazy`. It does not
account for `KoinGraphTest`, which builds a *second*, never-closed `koinApplication` in the same
process. Gradle runs all `di` package test classes in one JVM by default, and JUnit's execution order
across classes is not alphabetical or otherwise guaranteed — so which of the two `koinApplication`
calls happens first varies between runs.

- **If a live-Taiga test builds its graph first:** `KoinGraphTest` resolves `LoginViewModel` and
  `SettingsUserScreenViewModel` — both of which touch the storage DataStore during construction — and
  their constructor exceptions land in `KoinGraphTest`'s tolerated `constructionFailures` bucket
  (only `NoDefinitionFoundException` fails that test). This is the case Task 2 of
  [docs/testing/integration-tests-plan.md](testing/integration-tests-plan.md) documented and
  confirmed passing.
- **If `KoinGraphTest` runs first:** the reverse was never checked. `KoinGraphTest`'s own
  `koinApplication` touches the DataStore first and is never closed; when `LiveTaigaSession`'s
  `sharedSession` then tries to build *its* `koinApplication`, the `IllegalStateException` (multiple
  DataStores active for the same file) surfaces inside `liveTaigaSessionOrSkip()`'s own
  `assertTrue(result.isSuccess, ...)` — which is not tolerated, so every live-Taiga test in the run
  fails with "login failed: ... multiple DataStores active".

**Reproduced 2026-08-08** while adding `UsersApiIntegrationTest` (task 3 of the integration-tests
plan): `./gradlew :composeApp:jvmTest --tests "com.grappim.taigamobile.di.*" --rerun` with the three
`TAIGA_INTEGRATION_*` env vars set failed all three live-Taiga tests (`KoinGraphTest` ran first and
passed); the identical env vars with `--tests` limited to just the three `*IntegrationTest` classes
(excluding `KoinGraphTest`) passed all three. Not caused by the new test specifically — any of the
three live-Taiga tests fails the same way whenever `KoinGraphTest` happens to go first.

**Not fixed here** — out of scope for a single-module read-test task, and the fix (closing
`KoinGraphTest`'s `Koin` instance in an `@AfterTest`, or making `LiveTaigaSession` detect and reuse
an already-open graph) touches shared test infrastructure both files rely on. Revisit if a future
integration-test session hits it again, or when task 3's sweep is otherwise complete: run the full
`com.grappim.taigamobile.di.*` pattern a few times in a row to see how often the unfavorable order
actually occurs before deciding whether it is worth fixing.

**Still not fixed (re-verified 2026-08-09):** neither `KoinGraphTest.kt` nor `LiveTaigaSession.kt` has
changed since this was written — grepped both for `.close()`, found none. Re-ran
`./gradlew :composeApp:jvmTest --tests "com.grappim.taigamobile.di.*" --rerun` against the live local
instance three times in a row: all three reproduced the favorable-order case exactly as described
(`LoginViewModel`/`SettingsUserScreenViewModel` throw the "multiple DataStores active" exception
during `KoinGraphTest`'s construction sweep, tolerated, `KoinGraphTest` passes). The unfavorable case
was not re-triggered — with the current class set, `FiltersApiIntegrationTest` and
`HistoryApiIntegrationTest` both sort alphabetically before `KoinGraphTest`, and Gradle's test
execution in this environment ran in alphabetical order all three times, so the shared session is
always built first here. That is incidental to the current file names, not a fix — nothing prevents a
future test class (or a different execution order) from landing before `KoinGraphTest` and
triggering the unfavorable path described above. Mechanism confirmed live; still unfixed.

## 25. `FiltersStorageImplTest.resetFilters clears every section` is flaky under a full `jvmTest` run

**Where:** `core/storage/src/jvmTest/kotlin/com/grappim/taigamobile/core/storage/FiltersStorageImplTest.kt:158`
(the `resetFilters clears every section` test, line 165 is the `awaitItem()` that times out).

**Symptom:** `app.cash.turbine.TurbineAssertionError: No value produced in 3s` — a Turbine
`awaitItem()` on the filters flow times out waiting for the post-reset emission.

**Reproduced 2026-08-08** while adding `TasksApiIntegrationTest` (task 3 of
[docs/testing/integration-tests-plan.md](testing/integration-tests-plan.md)): `./gradlew jvmTest`
failed on this test twice in a row, including on a clean `git stash -u` tree at the last committed
commit (`e20a4119`) — so it is unrelated to the new test and pre-existing. Passes reliably when run
in isolation (`./gradlew :core:storage:jvmTest --tests
"com.grappim.taigamobile.core.storage.FiltersStorageImplTest"`), which points at timing/scheduling
sensitivity specific to running inside the full multi-module `jvmTest` invocation rather than a bug
in the test's assertions themselves.

**Resolved (2026-08-08):** root cause was a dispatcher mismatch, not a race in `resetFilters` itself.
`FiltersStorageImpl`'s own `scope` is built from `Dispatchers.Main` (overridden to
`MainDispatcherRule`'s `UnconfinedTestDispatcher` in tests), but the test's `DataStore` was built by
`createTestDataStore` with no explicit scope — `PreferenceDataStoreFactory.createWithPath` then
defaults to a real `Dispatchers.IO`-backed `CoroutineScope`. So the DataStore's internal actor (the
coroutine that actually performs the file write and republishes `dataStore.data`) ran on the real IO
thread pool, decoupled from the test dispatcher, and Turbine's `awaitItem()` had to real-wall-clock-wait
for it — which occasionally exceeded 3s once a full `jvmTest` run had many modules' test JVMs
contending for that thread pool. `TrustedCertStorageImplTest` never hit this despite also using a real
DataStore, because `TrustedCertStorageImpl` has no such internal scope — every write is a plain
`suspend fun` the test directly awaits, so however long the real I/O takes, the test just suspends for
it with no separate timeout in the mix.

Fix: `createTestDataStore` (`TestDataStore.kt`) now takes an optional `scope` parameter (default
unchanged — a real `Dispatchers.IO`-backed scope, so the other three callers are unaffected).
`FiltersStorageImplTest.setup()` passes `CoroutineScope(Dispatchers.Main + SupervisorJob())` — the
same dispatcher instance `MainDispatcherRule` installs — so the whole write -> DataStore actor -> flow
emission chain runs on one deterministic, eagerly-executing test dispatcher instead of spanning into
the real thread pool. Verified with 8 consecutive full `./gradlew jvmTest --rerun` runs: zero failures
in `FiltersStorageImplTest` across all 8 (one run failed on an unrelated, separately pre-existing flake
in `feature/wiki/ui`'s `WikiPageViewModelTest`, logged separately as [#26](#26-wikipageviewmodeltestonattachmentadd-failure-updates-state-with-error-is-flaky-under-a-full-jvmtest-run)).
`ktlintCheck` green.

## 26. `WikiPageViewModelTest.onAttachmentAdd failure updates state with error` is flaky under a full `jvmTest` run

**RESOLVED 2026-08-08 — the flaky test was deleted, not fixed.** See below.

**Where it was:** `feature/wiki/ui/src/commonTest/kotlin/com/grappim/taigamobile/feature/wiki/ui/page/details/WikiPageViewModelTest.kt:282-295`.

**Symptom:** same shape as [#25](#25-filtersstorageimpltestresetfilters-clears-every-section-is-flaky-under-a-full-jvmtest-run)
was before its fix — `app.cash.turbine.TurbineAssertionError: No value produced in 3s` on one of the
three sequential `awaitItem()` calls in the `sut.attachmentsState.test { ... }` block.

**Root cause found 2026-08-08 (a session after the note below was written):** the earlier
hypothesis ("no real I/O, whatever is stealing wall-clock time is unidentified") was wrong. The test
calls `onAttachmentAdd(createTestPlatformFile(...))`, which goes through
`WorkItemAttachmentsDelegateImpl.handleAddAttachment` → `file.readBytes()` —
`io.github.vinceglb.filekit`'s JVM implementation, which is hardcoded to
`withContext(Dispatchers.IO) { ... }` internally (confirmed by reading the library's own sources).
That's a real background thread outside the `UnconfinedTestDispatcher` set up by
`MainDispatcherRule`, racing Turbine's real-wall-clock timeout. Reproduced a failure even at a 15s
Turbine timeout (5x the default) under a deliberately heavy 6-module-parallel local run, which ruled
out "just raise the timeout" as a real fix — it only lowers the odds, it doesn't remove the race.
The same shape exists in `UserStoryDetailsViewModelTest`, `TaskDetailsViewModelTest`,
`EpicDetailsViewModelTest` and `IssueDetailsViewModelTest` (10 tests total across the 5 features that
mix in `WorkItemAttachmentsDelegate`), though only the Wiki one had actually been observed flaking.

**Fix considered and rejected:** threading a fakeable `readFileBytes` seam through
`WorkItemAttachmentsDelegateImpl` and the 5 `@KoinViewModel` classes that construct it (verified safe
with Koin via bytecode inspection — a defaulted, non-injectable constructor param is correctly
skipped by the compiler plugin). Rejected on explicit direction: production code should not be
shaped by test needs — if a path can't be tested deterministically without touching production code,
remove the test rather than add a seam for it.

**Resolution (2026-08-08):** deleted all 10 real-IO-racing tests (the two `onAttachmentAdd` tests in
each of the 5 ViewModels' test files) along with their now-unused `createTestPlatformFile`/
`getAttachment` imports. The behaviour they exercised — `handleAddAttachment`'s success/failure
branches, including a real `file.readBytes()` call — remains covered deterministically by
`WorkItemAttachmentsDelegateImplTest`, which calls the delegate directly under `runTest` with no
`viewModelScope.launch` and no Turbine, so it never races real time. Each ViewModel still keeps its
`onAttachmentAdd with a null file` test, which needs no real I/O. `:koverVerify` still passes after
the removal (checked the same session). No production file was touched.

## 27. `ExpandableMarkdownTextTest.longTextShowsExpandButtonAndTogglesOnClick` is flaky under a full `jvmTest` run

**Where:** `uikit/src/jvmTest/kotlin/com/grappim/taigamobile/uikit/widgets/text/ExpandableMarkdownTextTest.kt:29-46`.

**Symptom:** `androidx.compose.ui.test.ComposeTimeoutException: Condition still not satisfied after
1000 ms` on the `waitUntil { onAllNodesWithText("Show more")... }` call at line 40, which the test's
own comment already flags as timing-sensitive (`onSizeChanged`'s layout update landing a frame after
initial composition).

**Different mechanism from [#25](#25-filtersstorageimpltestresetfilters-clears-every-section-is-flaky-under-a-full-jvmtest-run)
and [#26](#26-wikipageviewmodeltestonattachmentadd-failure-updates-state-with-error-is-flaky-under-a-full-jvmtest-run)**
— those are coroutine-dispatcher races in ViewModel tests; this is a Skiko `ComposeUiTest` real-clock
wait racing the Compose layout pass under a loaded multi-module `jvmTest` run. `runComposeUiTest`'s
`waitUntil` has a real (not virtual) 1000ms default timeout, which a busy CI/dev machine running many
modules' tests concurrently can exceed even though the condition is eventually satisfied.

**Observed 2026-08-08** as a single failure in one `./gradlew jvmTest --rerun` (composeApp's
`FiltersApiIntegrationTest` session); passed reliably run alone
(`./gradlew :uikit:jvmTest --tests "*ExpandableMarkdownTextTest*" --rerun`) and a subsequent full
`./gradlew jvmTest --rerun` was green. Not investigated further, not fixed — noted in passing while
verifying an unrelated change. If it recurs, the fix is likely a longer explicit timeout on that one
`waitUntil` call, not a dispatcher change (there is no coroutine-test scope here to fix).

## 28. `CLAUDE.md` has grown too big; split the Kover ranking heuristics out into their own doc

**What:** `CLAUDE.md` is 717 lines (checked 2026-08-09). The `## Testing` section alone is lines
197–584 — 387 lines, over half the file — and almost all of that is the accumulated
missed-branch/missed-line ranking heuristics from the closed coverage-sweep work (the `mb`/`cb`
signature catalogue, the `kover-rank.py`/`kover-diff.py` usage notes, the `*_androidKt`/`logcat`/
`onCleared`/generated-code unreachability catalogue). That material is reference documentation for
*running a future coverage sweep*, not a day-to-day coding convention — it's read rarely, and its
bulk pushes the genuinely load-bearing conventions (the `XApi` interface/impl split, the
failure-path testing convention, the integration-test pattern, the `jvmTest`-vs-`src/test`
distinction) further down the file than they should be.

**Not fixed now:** out of scope for the integration-testing session that noticed it; splitting a
717-line file correctly (deciding what's a "convention CLAUDE.md should keep" vs. "sweep-specific
reference material") deserves its own focused pass, not a rushed edit bundled into an unrelated
diff.

**Suggested shape for the split** (not binding — re-evaluate when actually doing this):
- Move the Kover ranking/heuristics catalogue (roughly from "Qualify the task as `:koverVerify`"
  through the end of the `## Testing` section, i.e. most of lines ~300–584) into a new doc, e.g.
  `docs/testing/kover-coverage-heuristics.md`, and leave a one-line pointer in `CLAUDE.md`'s Testing
  section ("For missed-branch/missed-line ranking heuristics when running a coverage sweep, see
  [...]").
- Keep in `CLAUDE.md` itself: the `jvmTest`/`src/test` rule, the coverage-floor-is-a-ratchet rule,
  the `XApi` interface/impl convention, the failure-path testing convention, the `expect`/`actual`
  and integration-test-against-a-live-server conventions, and the `Skills & Agents` pointer to the
  **testing** subagent.
- Re-check line counts after the move and confirm nothing in `docs/testing/survey.md` /
  `improvement-plan.md` / `deferred.md` cross-references the moved content by its old location.

**To do on:** `test/live-taiga-login-integration` (gregory, 2026-08-09) — do this as its own commit
on the current branch, not bundled with an unrelated task's diff.

**Resolved (2026-08-09):** followed the suggested shape almost exactly. Moved the whole traps/
heuristics bullet list (old `CLAUDE.md:231-506`, the `koverXmlReport`-vs-`koverVerify` catalogue and
the `mb`/`cb` signature guide) plus the adjoining "Qualify the task as `:koverVerify`" note
(old `:508-509`) into a new [docs/testing/kover-coverage-heuristics.md](testing/kover-coverage-heuristics.md),
279 lines, with a short header explaining what it's for and pointing back to `CLAUDE.md`. Left the
ratchet rule itself (line/branch bounds, "raise it never lower it") in `CLAUDE.md` and replaced the
bullet list with a one-line pointer to the new doc. Everything else in the `## Testing` section —
the `jvmTest`/`src/test` rule, `ktlintCheck` gotcha, `XApi` convention, failure-path convention,
`expect`/`actual` and integration-test conventions, `DataStore` single-instance rule, env-var
caching — was untouched; verified with `diff` that the file is byte-identical before line 227 and
after the removed span. `CLAUDE.md` is now 440 lines (was 717). Checked `docs/testing/survey.md`,
`improvement-plan.md` and `deferred.md` for references to the moved content by line number or quoted
anchor text — none exist, so no cross-reference updates were needed. Docs-only change; no build/test
commands to run.

---

## 29. Login screen's server-URL regex rejects bare `localhost`

**Where:** `feature/login/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/login/ui/LoginViewModel.kt:33`

```kotlin
private const val SERVER_REGEX = """(http|https)://([\w\d-]+\.)+[\w\d-]+(:\d+)?(/\w+)*/?"""
```

**What happens:** the `([\w\d-]+\.)+` group requires at least one dot-separated label before the
final hostname segment, i.e. it demands a real FQDN (`api.taiga.io`, `example.com`). A single-label
host like `http://localhost:9000` fails `.matches(SERVER_REGEX)`, so `LoginViewModel.login()`
(`:109`) sets `isServerInputError = true` and the field renders red — the request is never sent, with
no error message beyond the red outline.

**Evidence:** found manually verifying [docs/desktop/linux-release-plan.md](desktop/linux-release-plan.md)
task 1 (moving desktop storage off `java.io.tmpdir`) — driving the real desktop app's login screen
against the local dev Taiga instance (`http://localhost:9000`, per this project's memory) via
`xdotool`. Typing `http://localhost:9000` produced the red-outlined field; switching to
`http://127.0.0.1:9000` (which does satisfy the regex — each dot-separated octet matches
`([\w\d-]+\.)+`) passed validation and logged in successfully.

**Consequence:** cosmetic/dev-workflow only, not a production bug — no real Taiga deployment is
reachable at a bare single-label hostname. It only bites developers and self-hosters pointing the
app at `http://localhost:<port>` (a Docker-hosted instance, a reverse-proxied instance without a
registered domain, etc.), who have to remember to type `127.0.0.1` instead.

**Fix, if wanted:** loosen `SERVER_REGEX`'s host group to `([\w\d-]+\.)*[\w\d-]+`, allowing a
single-label host without a trailing dot. Widens what "valid" data can be sent to `login()` but
doesn't change any *behavior* for currently-valid inputs — worth a quick check that no other code
(e.g. cert-pinning, `HostSelectionPlugin`) assumes a dotted hostname.

**Why deferred:** unrelated to the storage-path task in progress; a validation-regex change belongs
in its own diff.

## 30. `CrashReporter.recordException`/`.log` are unreachable on every non-Android platform

**Where:** `core/crash-api/src/commonMain/kotlin/com/grappim/taigamobile/core/crashapi/CrashReporter.kt`
defines the interface; `composeApp/src/jvmMain/.../data/CrashReporterImpl.jvm.kt` and
`composeApp/src/iosMain/.../data/CrashReporterImpl.ios.kt` are both pure no-op stubs.

**What happens:** the only call site for `recordException()` anywhere in the codebase is
`androidApp/src/main/kotlin/com/grappim/taigamobile/data/CrashlyticsTree.kt` — a Timber `Tree` that
forwards `Timber.e(throwable)` calls to `crashReporter.recordException(t)`. Timber itself is
Android-only (per CLAUDE.md's Logging table), so this `Tree` is never installed on JVM/iOS. Nothing
else in the app calls `CrashReporter.recordException()` or `.log()` directly. Net effect: even if
the JVM/iOS stubs were implemented for real, there is currently no code path that would ever invoke
them — the interface is fully wired for Android only.

**Evidence:** found while scoping [docs/desktop/linux-release-plan.md](desktop/linux-release-plan.md)
task 5 (installing a real logger backend on desktop), which explicitly asked whether
`CrashReporterImpl.jvm.kt` should stop being a stub in the same pass. `grep -rn
"\.recordException("` across the repo returned exactly one non-test call site, confirming this
rather than assuming it.

**Consequence:** no observable bug today (nothing is silently dropped that was ever going to fire),
but it means "wire up desktop crash reporting" is a bigger task than filling in the two stub
methods — it also needs a JVM/iOS equivalent of the Timber-to-`CrashReporter` bridge (e.g. a global
uncaught-exception handler, or a `logcat` call site added directly at error sites) before the stubs
would ever run.

**Why deferred:** out of scope for task 5, which is specifically about the `logcat`/`TaigaLogger`
file-logging path, not crash reporting — confirmed via the call-site grep rather than left as a
guess, then left alone per the task's own scope boundary.

## 31. Unused duplicate `ConnectivityManagerNetworkMonitor` in `androidApp`

**Where:** `androidApp/src/main/kotlin/com/grappim/taigamobile/data/ConnectivityManagerNetworkMonitor.kt`

**What happens:** this is a second, `@Single`-annotated connectivity monitor, separate from the one
actually wired to the app's `NetworkMonitor` interface
(`core/storage/src/androidMain/.../network/NetworkMonitorImpl.kt`, bound via `@Single(binds =
[NetworkMonitor::class])`). It duplicates the same `ConnectivityManager.NetworkCallback` logic —
`isOnline: Boolean` (point-in-time check) plus `isOnlineFlow: Flow<Boolean>` (callback-based) — but
implements neither the `NetworkMonitor` interface nor binds to it, so nothing in the app can obtain
it as `NetworkMonitor` even though Koin still constructs it as its own concrete-type single.

**Evidence:** `grep -rln "ConnectivityManagerNetworkMonitor" --include=*.kt .` returns only the one
declaration file — no injection site, no reference anywhere else in the repo.

**Consequence:** dead weight in the Android DI graph (one extra `@Single` Koin has to construct and
register, one extra `ConnectivityManager.NetworkCallback` registration if it's ever actually
resolved) and a trap for the next reader trying to find "the" network monitor — two candidates exist
with overlapping responsibility and only one is real.

**Why deferred:** found while checking `@param:IoDispatcher` usage patterns for
[docs/desktop/linux-release-plan.md](desktop/linux-release-plan.md) task 9 (JVM connectivity
detection) — unrelated to that task's `core/storage` `jvmMain` scope, and deleting an
`androidApp`-only file is a separate, single-purpose diff.

**Fix, if wanted:** delete the file. Confirm first that Koin's `@ComponentScan` in `AndroidModule`
(scans `com.grappim.taigamobile.data`) doesn't have some other reflective dependency on it existing —
unlikely given zero references, but worth a `:androidApp:assembleGplayDebug` after removal to be sure.

---

## 32. No warning when the configured server URL is `http://` despite the bearer token being sent over it

**Where:** `androidApp/src/main/AndroidManifest.xml:20` (`android:usesCleartextTraffic="true"`, no
`android:networkSecurityConfig` scoping it to specific hosts) plus
`core/api/src/commonMain/kotlin/com/grappim/taigamobile/core/api/AuthHeaderPlugin.kt:31-35`, which
attaches the stored bearer token to every outgoing request via `request.headers[AUTHORIZATION] =
generateBearerToken(token)` with no check of `request.url.protocol`.

**What happens:** cleartext is permitted app-wide (no `network_security_config.xml` restricting it to
particular domains), and the token-attaching plugin doesn't distinguish `http://` from `https://`. So
a user who points the app at a plain-HTTP self-hosted Taiga instance sends the session bearer token
over the wire in the clear on every request. `LoginViewModel` does show a one-time "Unencrypted
connection" confirmation (`login_alert_title`/`login_alert_text`) before the *first* credential
submission when `server.startsWith(ApiConstants.HTTP_SCHEME)`
(`feature/login/ui/src/commonMain/kotlin/.../LoginViewModel.kt:122-127,135-140`) — found during the
MASVS-AUTH review (task 3) — so the password send is a choice, not silent. Every request *after* that
one dialog (including every subsequent bearer-token-bearing request via `AuthHeaderPlugin`, and every
silent background token refresh) still has no equivalent warning, which is the gap this entry tracks.

**Evidence:** found during the MASVS-NETWORK review (`docs/security/masvs-review-plan.md` task 2).
`grep -n "networkSecurityConfig" androidApp/src/main/AndroidManifest.xml` returns nothing;
`AuthHeaderPlugin.kt` has no `URLProtocol` check anywhere in its `HttpSend.intercept` block.

**Consequence:** not a MASVS finding by itself — cleartext is an accepted deviation for self-hosted
LAN instances (`docs/security/masvs.md`, MASVS-NETWORK-1) — but it's a real, low-cost UX/security
improvement: warn the user once per session (not just once at login) that ongoing traffic, including
the bearer token, is unencrypted.

**Why deferred:** the MASVS review task's scope is recording the register, not shipping UI changes;
adding a warning dialog/snackbar to the server-setup flow is a small but distinct diff.

**Fix, if wanted:** on saving/validating the server URL (wherever that validation already lives for
the `localhost` regex issue in #29), branch on `URLProtocol` and show a one-time warning when it's
`http`. Small, self-contained.

---

## 33. `TrustedCertificatesScreen` is reachable but permanently inert on iOS

**Where:** `core/api/src/iosMain/kotlin/com/grappim/taigamobile/core/api/PlatformHttpClientEngine.kt:7`
— `actual fun createPlatformHttpClientEngine(trustedCertStorage: TrustedCertStorage): HttpClientEngine
= Darwin.create()` ignores the `trustedCertStorage` parameter entirely; there is no iOS equivalent of
Android/JVM's `CompositeTrustManager`. The settings UI that lists/revokes trusted certificates
(`feature/settings/ui/src/commonMain/kotlin/.../trustedcerts/TrustedCertificatesScreen.kt`) lives in
`commonMain`, so it's reachable on iOS too.

**What happens:** nothing on iOS ever writes to `TrustedCertStorage` (only `CompositeTrustManager`'s
TOFU flow does that, and it doesn't exist on the Darwin engine), so `TrustedCertificatesScreen` on iOS
is permanently empty and can never gain an entry — not misleading (empty is a safe, honest state), but
dead UI a user could reach and wonder why it never does anything. Separately, an iOS user pointed at a
self-signed/self-hosted server can't connect at all — `Darwin.create()` uses `NSURLSession`'s default
TLS validation with no override, so the handshake simply fails closed. That's the safe direction (no
silent bypass), but it's a real feature-parity gap: the TOFU-with-revoke-UI flow the app ships is
Android/JVM-only.

**Evidence:** found during the MASVS-NETWORK review (`docs/security/masvs-review-plan.md` task 2).
`grep -rn "TrustedCertStorage" core/api/src/iosMain` shows the parameter is declared but never read;
`docs/security/masvs.md`'s Network section records this as a Note, not an Open MASVS finding, since
MASVS-NETWORK-2's pinning control is N/A for a user-supplied server and failing closed isn't a
violation of anything.

**Consequence:** no security bug — this is a UX/feature-completeness gap, not a MASVS control
violation. Worth fixing either by porting the TOFU flow to iOS (Keychain-backed cert storage lookup
wired into the Darwin engine, a bigger job) or by hiding `TrustedCertificatesScreen` from iOS
navigation until that lands (small).

**Why deferred:** out of the MASVS review task's scope (recording the register, not shipping a
platform port or a UI-visibility change); porting TLS trust handling to a new platform is exactly the
kind of change that needs its own task, not a rider on a documentation review.

---

## 34. GitHub OAuth WebView doesn't restrict navigation to GitHub's own host

**Where:**
`feature/login/ui/src/androidMain/kotlin/com/grappim/taigamobile/feature/login/ui/GithubOAuthWebViewDialog.android.kt:22-38`.
`settings.javaScriptEnabled = true` and `settings.domStorageEnabled = true` (`:23-24`), and
`shouldOverrideUrlLoading` (`:26-37`) only inspects the navigated URL for a `code` or `error` query
param — any other URL falls through to `return false`, i.e. the `WebView` loads it. Nothing checks
`request.url.host` against `github.com` (or the eventual OAuth-callback host) before allowing
navigation.

**What happens:** the app hosts GitHub's real login form inside a `WebView` it fully controls (full
JS execution, DOM storage, no address bar) — this is the RFC 8252 "embedded user-agent" anti-pattern
`kmp-checks.md` names, and it fails regardless of configuration. This specific instance has one real
bound already: no `addJavascriptInterface` call anywhere (`grep -rn addJavascriptInterface` across all
source sets is empty), so there's no JS-to-native bridge for a malicious page to call into. But nothing
stops the `WebView` from following a redirect to an arbitrary host during the flow — the interception
logic only reacts to the `code`/`error` params, not the host, so a redirect chain that doesn't yet
carry those params is followed unconditionally.

**Why it's a `WebView` at all, not Custom Tabs:** this was already tried and reverted in the same PR
that shipped GitHub login (commit `4236a2ef`, "feat: tg-108 replace loopback with WebView for GitHub
OAuth"). The first cut used a loopback redirect (`http://127.0.0.1:PORT/callback`) opened in a Chrome
Custom Tab — the standard RFC 8252-compliant pattern, documented in
`docs/features/github-auth/plan.md` (now stale/superseded, marked as such this task). It was reverted
because **GitHub OAuth Apps support exactly one registered callback URL**, and that URL is already the
Taiga web app's. A mobile-specific loopback redirect would either break the web login (if the callback
is repointed at `127.0.0.1`) or require a second GitHub OAuth App with its own `client_id` — a
server-admin config change outside this codebase's control and outside "zero admin changes" the
current design promises. The `WebView` approach reuses whatever callback URL is already registered
(Taiga's `connector.py` doesn't validate `redirect_uri` server-side either, per the plan doc), which is
why it was chosen instead.

**Consequence:** this is a real, if partial, hardening gap, not a full fix waiting to happen — Custom
Tabs is blocked by the external OAuth App constraint above unless that constraint changes server-side.
What *is* available without touching the OAuth architecture: restrict `shouldOverrideUrlLoading` to an
allowlist of hosts the flow actually needs (`github.com` and its auth/SSO subdomains, plus the
configured Taiga server's host for the final callback), denying/dismissing on anything else. That
narrows the WebView's blast radius without solving the underlying RFC 8252 problem, which needs the
external constraint resolved first.

**Evidence:** found during the MASVS-AUTH review (`docs/security/masvs-review-plan.md` task 3);
recorded as an Open finding, MASVS-AUTH-1, in `docs/security/masvs.md`.

**Why deferred:** correctly scoping a host allowlist (GitHub's SSO/2FA flow can involve more than the
bare `github.com` host) risks silently breaking the OAuth login for some orgs if done from a source
read alone, and this repo has no Android unit-test source set (CLAUDE.md, by design) to verify a
`WebViewClient` change automatically — it would need manual device verification. Not a rider on a
documentation review task.

---

## 35. No `FLAG_SECURE` — revealed login password can land in the recents-list screenshot

**Where:** `androidApp/src/main/kotlin/com/grappim/taigamobile/MainActivity.kt` never calls
`window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, ...)` — confirmed by `grep -rn 'FLAG_SECURE'
--include=*.kt .` returning nothing anywhere in the repo. The concrete screen this matters for:
`feature/login/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/login/ui/LoginScreen.kt:190-219`,
whose password field has a show/hide toggle (`state.isPasswordVisible` driving
`VisualTransformation.None` vs. `PasswordVisualTransformation()`).

**What happens:** this is a single-`Activity` app (`MainActivity` hosts every Compose screen), so the
absence of `FLAG_SECURE` applies app-wide, not just to login. The concrete exposure: a user taps "show
password" on the login screen, then backgrounds the app (app switcher, incoming call, notification
shade) while the field is still in `VisualTransformation.None` state — Android's recents-list snapshot
captures whatever was on screen at that moment, so the plaintext password lands in the thumbnail. The
thumbnail is local to the device (not synced or uploaded anywhere), so exploiting it needs local/
physical access to an unlocked device — same class of exposure as an unlocked phone left unattended,
not a remote one.

**Consequence:** low-to-medium severity, MASVS-PLATFORM-3. Fixing it is a one-line change
(`window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)` in
`MainActivity.onCreate`), but because it's a single-Activity app the flag would apply globally —
blocking screenshots and screen recording on every screen, not just login, which is a real UX
tradeoff (e.g. no user-initiated bug-report screenshots from inside the app) that deserves a
deliberate choice rather than a silent default flip.

**Evidence:** found during the MASVS-PLATFORM review (`docs/security/masvs-review-plan.md` task 4);
recorded as an Open finding, MASVS-PLATFORM-3, in `docs/security/masvs.md`, with the live-device
verification itself moved to that register's "Needs a device" table (source can confirm the flag is
absent, not that a real screenshot actually captures the revealed password).

**Why deferred:** the app-wide vs. per-screen tradeoff is a product decision (does the team want to
give up in-app screenshot capability everywhere to close a local-access-only gap on one screen), not
something to default silently inline during a documentation review task.

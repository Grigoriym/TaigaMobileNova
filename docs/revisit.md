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
| 2 | Non-ViewModel beans may leak application-scoped coroutines | S to check | same |
| 5 | `tools/seed` and `tools/utils` tests would not run in CI | XS | improvement-plan task 1 |
| 16 | Every `logcat` message lambda is a permanently-uncovered line | S | improvement-plan task 9a |
| 18 | `currentUserStory` throws from `viewModelScope` when the initial load failed | S–M | improvement-plan task 9a |
| 23 | The coverage report counts Android-variant classes no test can reach | M | [kover issue](issues/2026-08-07-kover-excludes-and-report-mode-flip.md) |

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

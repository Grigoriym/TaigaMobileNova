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
| 8 | Kover's excludes are applied partially, and differently by the two tasks | M | improvement-plan task 8 |
| 9 | `WikiRepositoryImplTest`'s failure tests can pass without reaching the SUT | XS | improvement-plan task 9 |
| 10 | The `Plugin`/`Module` exclusion patterns hide real logic in `core/api` | S | improvement-plan task 9a |
| 11 | `TokenRefreshPlugin`'s `MAX_RETRIES` guard is unreachable | S | improvement-plan task 9a |
| 12 | Two small dead spots in `core/api` | XS | improvement-plan task 9a |

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

### Update (2026-08-03, improvement-plan task 9a) — the trigger is now known

`koverXmlReport` has **two stable outputs**, and which one you get depends on whether any build
script changed since the last run — not on the tests, not on the caches:

| Tree state | Classes in report | LINE | BRANCH |
|---|---|---|---|
| clean (no build-file change since last run) | 821 | 62.00 % | 43.49 % |
| **any** build-file change | 742 | 71.96 % | 50.37 % |

The 742-class output is the one where the `excludes` block is applied **in full** — 79 classes that
the filters name are dropped, and the totals land on the "what the configured excludes should
produce" row above (71.97 % / 49.73 %). The 821-class output leaks those 79 classes back in.

Isolated by bisection on 2026-08-03: adding a **single unused line to `gradle/libs.versions.toml`**
is enough to flip it. Ruled out as causes — `--no-configuration-cache` and `--no-build-cache` on a
clean tree both still give 821, and deleting `report.xml` to force report regeneration also gives
821. So it is neither cache; the remaining difference is whether the compile/test tasks re-executed,
which matches the standing warning in `CLAUDE.md` that "`koverXmlReport` reports on whichever test
tasks actually executed".

**Two consequences that matter more than the mechanism:**

- **CI always gets the 742 / 71.96 % behaviour**, because a fresh checkout always re-executes
  everything. The ~62 % a local clean-tree run prints is a local-only artifact. Anyone comparing a
  local figure to Codecov is comparing two different numbers for a third reason, on top of the two
  already in this entry.
- **A local before/after comparison is invalid unless both runs are on the same side of this flip.**
  Task 9a hit this: the baseline was taken on a clean tree (821) and the after-run had a modified
  `build.gradle.kts` (742), making the totals differ by 1988 lines in packages the change never
  touched. The fix is to take both measurements with a build-file change present, then check the
  denominators match before reading anything into the numerators.

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

---

## 14. The Kover coverage floor is now ~17/22 points below actual

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

**Fix:** take `:koverVerify` readings on several separate clean-tree invocations. If they are stable
at ~75/60, raise the bounds to ~73/58. If they flip, that is a bigger finding than the floor and
belongs in #8.

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

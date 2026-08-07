# Testing suite: improvement plan

**Created:** 2026-08-02
**Baseline:** [survey.md](survey.md) — what the suite looked like before this plan started.

A sequence of small, independent tasks. Each one is sized to fit in a **single clean context**: a
session picks exactly one task, does it, runs `finalize`, and stops. Nothing here requires holding
two tasks in your head at once.

## How to run a task

1. Read the status table below and take the task marked **NEXT**. (If none is marked, take the
   first `todo`.) Never take a `deferred` task without asking.
   **Do this even if you found the task another way** — by `grep`, by `tail`, by jumping straight
   to its `## Task N` section. A task's own section is not required to repeat a gate that lives in
   the table or in the "Scope decision" note above it (see the rule at the end of this list), so
   skipping the table is how a gated task gets started unasked — it happened to task 10
   (2026-08-06): a session read the section body via `tail`, never saw the table, and did the work.
2. Read only that task's section, plus [survey.md](survey.md) if you need the wider picture.
3. Do it. Verify with the task's own `Done when` commands — not by eyeballing.
4. **Update the status table**: set this task to `✅ done — <date>`, and move the `⬅ NEXT` marker
   to the task that follows. Add a `**Result (<date>):**` note to the task's own section saying
   what actually happened — especially anything that differed from the description. Append the note
   *above* the next task's `---` separator and re-read the boundary afterwards — a result note that
   swallows the following `## Task N` heading makes step 2 impossible to follow (this happened to
   task 4). **End the Result note by naming what comes next** — the next task number and name, or
   "nothing is scoped — the queue is empty" if the table has no `todo` left. Say this in prose, don't
   make the reader infer it from the `⬅ NEXT` marker alone: gregory reads these Result notes to tell
   whether there's more to do without re-reading the whole table himself, and each task is picked up
   cold, so the answer has to be stated, not implied.
5. Run the **`finalize` skill**. Each task lists a *Finalize focus* — the thing most likely worth
   capturing — but that is a hint, not a substitute for the skill's own harvest step.
6. **Commit and push.** Standing authorization (gregory, 2026-08-03) — do not ask, and do not stop
   after finalize waiting to be told. Finalize runs *before* the commit so its CLAUDE.md/doc edits
   land alongside the work that taught them. Three limits: never commit a red build (step 3 is a
   precondition, not a parallel task), never push to `dev`, and ask before anything beyond
   commit+push — PRs, force-pushes, rebases.

Step 4 is what makes the next cold start work. A session that does the work and skips the table
leaves the plan lying about where things stand.

**Any task added to this doc in a `deferred`/gated state must repeat that gate as the first line
under its own `## Task N` heading** — e.g. `⛔ **Gated — do not start without asking (see status
table).**` — not only in the table row and the "Scope decision" note. Two places to look beats one;
step 1 above is the primary defense, this is the backstop for when it's skipped anyway.

**Standing rule for every task:** if the task adds a fake, a model factory, or a test utility to
`:testing`, updating the **fake inventory in `.claude/agents/testing.md`** is part of the task, not
an optional extra. That agent is how future sessions discover what already exists; a stale inventory
causes duplicate fakes. This is an acceptance criterion, not a nice-to-have.

**Do not batch tasks.** The point of the split is that each one lands verified and finalized on its
own. If a task turns out to be bigger than described, split it further and update this file rather
than pushing through.

## Status

| # | Task | Size | Status |
|---|---|---|---|
| 0 | Correct the survey doc | XS | ✅ done — 2026-08-02 |
| 1 | Close the "tests that never run in CI" trap | S | ✅ done — 2026-08-02 |
| 2 | Koin DI graph test | M | ✅ done — 2026-08-02 |
| 3 | `WikiRepositoryImpl` + `FakeWikiApi` | S | ✅ done — 2026-08-02 |
| 4 | `GetProfileDataUseCase` | XS | ✅ done — 2026-08-03 |
| 5 | `GetKanbanDataUseCase` + `FakeSwimlanesRepository` | M | ✅ done — 2026-08-03 |
| 6 | `DateTimeUtilsImpl` + `KotlinxDateTimeFormatter` | M | ✅ done — 2026-08-03 |
| 7 | `TeamViewModel` | S | ✅ done — 2026-08-03 |
| 8 | Coverage floor in CI (`koverVerify`) | S | ✅ done — 2026-08-03 |
| 9 | Error-path convention + first sweep | M | ✅ done — 2026-08-03 |
| 9a | Missed-branch sweep, one module per session | M each | 🔁 in progress — `core/api` ✅ 2026-08-03, `feature/projects/data` + `mapper` ✅ 2026-08-03, `feature/kanban/ui` ✅ 2026-08-03, `utils/ui` ✅ 2026-08-03, `main` ⛔ closed-as-blocked 2026-08-03, `feature/workitem/ui/delegates/customfields` ✅ 2026-08-03, `feature/workitem/ui/delegates/badge` ✅ 2026-08-04, `feature/settings/ui/attributes/projectvalues` ✅ 2026-08-04, `feature/workitem/ui/screens/edittags` ✅ 2026-08-04, `feature/workitem/ui/screens/sprint` ✅ 2026-08-04, `feature/settings/ui/modules` ✅ 2026-08-04, `createtask` ✅ 2026-08-04, `feature/settings/ui/user` ✅ 2026-08-04, `feature/settings/ui` ⛔ closed-as-blocked 2026-08-04, `core/storage` ✅ 2026-08-04, `feature/userstories/ui` ✅ 2026-08-04 (branch half; the line half was split out — see 9c), `feature/workitem/ui/mappers` ✅ 2026-08-05, `feature/epics/ui/details` ✅ 2026-08-05, `feature/issues/ui/details` ✅ 2026-08-05, `core/domain` ⛔ closed-as-blocked 2026-08-05 (all 16 missed branches are unreachable — but the module's real gap, `ResultExtension`, was tested anyway; see the archive), `feature/sprint/data` ✅ 2026-08-05, `feature/tasks/ui` ✅ 2026-08-05, `feature/workitem/ui/delegates/sprint` ✅ 2026-08-05, `feature/filters/mapper` ✅ 2026-08-05, `feature/userstories/mapper` ✅ 2026-08-05, `feature/settings/ui/projectdetails` ✅ 2026-08-05, `feature/filters/domain` ✅ 2026-08-05 (100 % on every counter), `feature/workitem/ui/screens/editdescription` ✅ 2026-08-05 (100 % on every counter). **The branch sweep is out of worthwhile rows — see [Where 9a stands](../archive/testing-improvement-plan-tasks-0-9f.md#where-9a-stands-2026-08-05) in the archive; continue with task 9c instead** |
| 9b | `WorkItemRemoteMediator` | M | ✅ done 2026-08-05 — 13 tests; the class went BRANCH 0/11 → **11/11**, LINE 0/33 → **32/33**, and took the whole `feature/workitem/data` package to **100 % BRANCH**. See the archive for the section |
| 9c | Details-ViewModel delegate handlers (LINE-only) | M each | 🔁 in progress — `feature/userstories/ui` ✅ 2026-08-05 (LINE 375/528 → **518/528**, CLASS 17/34 → **34/34**; every `$1` lambda class closed), `feature/tasks/ui` ✅ 2026-08-06 (LINE 321/479 → **472/479**, CLASS 13/31 → **31/31**), `feature/epics/ui/details` ✅ 2026-08-06 (LINE 325/468 → **460/468**, CLASS 12/29 → **28/29**), `feature/issues/ui/details` ✅ 2026-08-06 (LINE 354/512 → **503/512**, CLASS 13/30 → **30/30**). **All four `feature/*/ui` details ViewModels are now closed — this task is done.** |
| 9d | `UserStoryDetailsDataUseCaseImpl` (LINE-0 sleeper) | S | ✅ done — 2026-08-06 |
| 9e | `WikiPageViewModel` (LINE-0 sleeper) | S | ✅ done — 2026-08-06 |
| 9f | `AuthRepositoryImpl.getGithubClientId` + `TagsScreenViewModel.onSaveTag` | XS | ✅ done — 2026-08-06 |
| 10 | Compose UI test spike (one uikit widget) | M | ✅ done — 2026-08-06 (started without asking — see the task's own Result note) |
| 11 | Compose UI test sweep, one uikit widget per session | S each | ✅ done — 2026-08-07. `DropdownSelector` ✅ 2026-08-07, `ConfirmActionDialog` ✅ 2026-08-07, `ExpandableMarkdownText` ✅ 2026-08-07, `SectionTitle` ✅ 2026-08-07. All four candidates closed — task complete. |
| 12 | Compose UI test spike, feature-level Screen (`SettingsAboutScreen`) | S | ✅ done — 2026-08-07 |
| 13 | Compose UI test, route-carrying + async-loading Screen (`ProjectValuesScreen`) | S | ✅ done — 2026-08-07 |
| 14 | Compose UI test, paging-list Screen (`ProjectSelectorScreen`) | S | ✅ done — 2026-08-07 |
| 15 | Compose UI test, dialog Screen (`TrustedCertificatesScreen`) | S | ✅ done — 2026-08-07 |
| 16 | Compose UI test, multi-state-source Screen (`SettingsInterfaceScreen`) | S | ✅ done — 2026-08-07 |
| 17 | Paging sweep — `ScrumClosedSprintsScreen` | S | ✅ done — 2026-08-07 |
| 18 | Paging sweep — `ScrumOpenSprintsScreen` | S | ⬅ NEXT |
| 19 | Paging sweep — `ScrumBacklogScreen` | S | todo |
| 20 | Paging sweep — `EpicsScreen` | S | todo |
| 21 | Paging sweep — `IssuesScreen` | S | todo |

**Scope decision (2026-08-02, extended 2026-08-03):** tasks 0–9 — the unit / non-instrumented work —
are in scope and should be worked straight through; 9a and 9b were added by task 9 as its own
follow-ups and inherit that scope. **Task 10 was gated on asking first and got started without
that** (2026-08-06 — a session read only the task's own section, not this table, and missed the
gate; the resulting work was reviewed and kept, see the task's Result note). Treat any future gated
item the same as this one was *supposed* to be treated: read this table, not just the target task's
section, before starting. Everything in [Considered and deferred](#considered-and-deferred) is still
gated — the other test *types* get decided once asked, not assumed from Task 10 having landed.

**Task 11 is explicitly ungated (gregory, 2026-08-06):** now that task 10 proved the wiring, expanding
Compose UI tests to more `uikit` widgets does not need a per-task ask — treat it like tasks 0–9f,
take the NEXT candidate and run it. **Task 12 was scoped 2026-08-07** — gregory's stated intent is to
eventually expand Compose UI testing to the whole project (feature-level Screens, not just uikit
widgets); the scoping pass resolved the two real unknowns (no Koin graph needed; `LocalTopBarConfig`
needs an explicit provider) and named a concrete pilot Screen. It is runnable now, same as any other
ungated task — see its own section.

Sizes: XS = minutes, S = under an hour, M = a focused session.

**`runComposeUiTest` is deprecated in favor of a `v2` overload (noticed 2026-08-07, during the task
11 sweep — see the archive).** Compiling any test that calls it now prints
`'fun runComposeUiTest(...)' is deprecated. Use 'androidx.compose.ui.test.v2.runComposeUiTest'
instead. The v2 APIs use 'StandardTestDispatcher' by default...`. Not acted on yet — every test
written under tasks 10–11 uses the v1 API and it still works — but a future session migrating to v2
should expect the `StandardTestDispatcher` default to change timing-sensitive interactions (anything
relying on immediate coroutine execution may need an explicit `advanceUntilIdle()` or similar). Worth
a dedicated look once the warning starts actually blocking something, not a reason to touch the
passing tests now.

Tasks 0–2 are ordered deliberately: 0 makes the reference doc trustworthy, 1 guarantees that
anything later tasks write actually runs in CI, 2 is the highest-value single test in the plan.
Tasks 3–7 are independent of each other and can be reordered freely. Task 8 depends on 3–7 having
landed (the floor should be set from the improved numbers). Tasks 9, 9a and 10 are the open-ended
ones — 9a is explicitly repeatable, one module per session.

---

Tasks 0–9f (all done) have their full write-ups archived at
[../archive/testing-improvement-plan-tasks-0-9f.md](../archive/testing-improvement-plan-tasks-0-9f.md)
— moved out 2026-08-06 to keep this file focused on active work. The status table above still lists
their dates and one-line results; the archive is where the worked examples, per-task Result notes and
the Kover mode-flip investigation live now.

Tasks 10–11 (also done) are archived the same way, at
[../archive/testing-improvement-plan-tasks-10-11.md](../archive/testing-improvement-plan-tasks-10-11.md)
— moved out 2026-08-07 once task 11's candidate list was exhausted. That's where the Compose UI test
spike write-up and all four per-widget Result notes (`DropdownSelector`, `ConfirmActionDialog`,
`ExpandableMarkdownText`, `SectionTitle`) live now; `compose-ui-test-spike.md` (the durable how-to
doc, not a task write-up) stayed in this directory since it's still the first read for the next
Compose UI test.

Task 12 is next, and is now scoped and runnable — see its own section below. Task 13 (also done) picks
up where task 12's Result note left off — see its own section below. Task 14 (also done) picks up
where task 13's Result note left off, closing the last of the three Screen shapes it named
(paging lists) — see its own section below.

Tasks 15–21 (scoped 2026-08-07, not yet started) are the follow-up: two remaining Screen *shapes*
(dialog, multi-state-source) plus a sweep of the five paging-list Screens task 14 didn't pick. See
each task's own section below for why that particular Screen and what it needs. Task 15 is next.
Kanban (`KanbanViewModel`/`KanbanScreen`) was surveyed as a candidate for the multi-state-source shape
and deliberately left out of this batch — see [Considered and deferred](#considered-and-deferred).

---

## Task 12 — Compose UI test spike, feature-level Screen (scoped 2026-08-07)

**Why:** task 10 proved `runComposeUiTest` wiring on a bare `commonMain` Composable with no external
dependencies. A feature-level Screen is a different shape — it has a real `ViewModel`, typically reads
`SavedStateHandle`/nav-route arguments, and composes uikit widgets — and nothing in the repo proves
that shape works inside `runComposeUiTest` yet. This is a **spike**, same as task 10: prove the wiring
on one Screen, then decide whether to sweep the rest the way task 11 swept uikit widgets.

**Scoping already done this session — the two real unknowns are resolved:**

- **No Koin graph needed in the test.** Checked all 45 `*Screen` composables in `feature/*/ui`; every
  one takes `viewModel: XViewModel = koinViewModel()` as a default parameter (11 sampled directly).
  A test can construct the ViewModel directly with `:testing` fakes and pass it in as an explicit
  argument — exactly like every existing ViewModel unit test already does — and never touch Koin.
  This was the biggest open question the previous (unscoped) version of this task flagged; it's closed.
- **`LocalTopBarConfig` needs an explicit provider in the test.** It's `compositionLocalOf<TopBarController> { error(...) }`
  (`uikit/.../topbar/TopBarController.kt`) — reading `.current` without one crashes. Any Screen test
  must wrap `setContent` in `CompositionLocalProvider(LocalTopBarConfig provides TopBarController())`.

**Pilot: `SettingsAboutScreen`** (`feature/settings/ui/.../about/SettingsAboutScreen.kt` +
`SettingsAboutScreenViewModel.kt`). Chosen over the alternatives already surveyed in `feature/settings/ui`
(`TrustedCertificatesScreen`, `SettingsUserScreen`, `SettingsInterfaceScreen`) for having the fewest
moving parts to prove the pattern:

- No nav-route params — `SettingsAboutScreenRouteNavDestination` is a bare `data object`, so the test
  doesn't need to touch `SavedStateHandle`/`toRoute<T>()` at all (a real gap this pilot does **not**
  close — flag it as still open in the Result note either way).
- Trivial `ViewModel` constructor: `AppInfoProvider` (6 methods, `getAppInfo()`/`isDebug()`/etc.) and
  `CrashReporter` (`isAvailable` + 3 methods) — both tiny interfaces with **no existing `:testing`
  fake**. Adding `FakeAppInfoProvider` and `FakeCrashReporter` is part of this task, not a blocker —
  per the plan's standing rule, update the fake inventory in `.claude/agents/testing.md` when they land.
- Renders via `koinViewModel()` default like every other screen, uses `LocalTopBarConfig` (the second
  resolved unknown above), and its content (`GithubRepoContent`/`PrivacyPolicyContent`/`VersionContent`)
  is plain `Text`/`TaigaOutlinedButton` — nothing gesture-based, matching task 11's click/type-only scope.

**Build wiring needed:** `feature/settings/ui` has a `commonTest` source set already but **no `jvmTest`
and no `compose.dependencies.uiTest`/desktop artifacts** — the same one-time addition task 10 made to
`uikit/build.gradle.kts` (`composeUiTestDep`/`composeDesktopUiTestJUnit4Dep`/`composeDesktopCurrentOsDep`
in a `jvmTest.dependencies` block) needs to happen here too, on `feature/settings/ui/build.gradle.kts`.

**Scope — deliberately still tiny, same as task 10:**

- Add the `uiTest` build wiring to `feature/settings/ui`.
- Add `FakeAppInfoProvider` + `FakeCrashReporter` to `:testing` (update the fake inventory).
- Write **one** `SettingsAboutScreenTest` that constructs `SettingsAboutScreenViewModel` directly with
  the two new fakes, wraps `setContent` in the `LocalTopBarConfig` provider, and asserts real rendered
  content (e.g. `state.appInfo`'s text is visible) — a render-only assertion is enough for the spike;
  it doesn't need a click interaction since this screen has none worth testing beyond `uriHandler.openUri`
  calls, which are platform side effects, not state changes.
- Write down what a route-carrying Screen (`SavedStateHandle`/`toRoute<T>()`) would need differently —
  as a **note**, not as a second test. That's the one real gap this pilot leaves open; naming it
  precisely is enough to size a follow-up, don't chase it in this session.

**Done when:** `SettingsAboutScreenTest` passes via `./gradlew :feature:settings:ui:jvmTest`, runs
under the root `./gradlew jvmTest` (confirm, don't assume, same as task 10), and the write-up says
plainly whether the Screen-test pattern is worth sweeping across other Screens next — a negative
result (too much friction per Screen) is as valid an outcome as a positive one.

**Finalize focus:** high, same reasoning as task 10 — the output of a spike is knowledge. If
`SavedStateHandle`-carrying Screens turn out to need a fundamentally different setup, say so explicitly
rather than letting the next session assume this pilot's pattern generalizes.

**Result (2026-08-07):** Works, same conclusion as task 10. `SettingsAboutScreenTest`
(`feature/settings/ui/src/jvmTest/.../about/SettingsAboutScreenTest.kt`) passes via
`./gradlew :feature:settings:ui:jvmTest` and is picked up by the root `./gradlew jvmTest` (confirmed
by the `TEST-...SettingsAboutScreenTest.xml` result file appearing under
`feature/settings/ui/build/test-results/jvmTest/` after a full-root run, same check task 10 used).
`ktlintCheck` is clean too.

- **Build wiring** was a direct copy of `uikit/build.gradle.kts`'s pattern — hoist
  `compose.dependencies.uiTest` / `desktop.uiTestJUnit4` / `desktop.currentOs` to top-level `val`s
  under `@file:OptIn(ExperimentalComposeLibrary::class)`, reference them in `jvmTest.dependencies`.
  No new gotcha; the nested-`dependencies{}`-shadowing trap task 10 found doesn't recur once you know
  to hoist.
- **`FakeAppInfoProvider` and `FakeCrashReporter`** landed in `:testing` `commonMain` at the *root*
  package level (next to `FakeNetworkMonitor`), not under `repo/`/`api/`/`usecases/` — neither
  taxonomy fits a small platform-capability interface. `testing/build.gradle.kts` needed two new
  `api(projects.core.appinfoApi)` / `api(projects.core.crashApi)` lines; both are tiny leaf modules,
  not `core/api` itself, so the "fakes for `:core:api` types stay local to the module" exception
  doesn't apply here. Fake inventory in `.claude/agents/testing.md` updated.
- **No `MainDispatcherRule` needed.** `SettingsAboutScreenViewModel` builds its whole `_state` inline
  in the constructor — no `init { viewModelScope.launch { ... } }` — so it's constructible directly
  in the test with no dispatcher rule and no `runTest`, same as any ViewModel with a synchronous
  constructor.
- **No new `testTag` needed.** `VersionContent` renders `Text(state.appInfo)` directly, so
  `onNodeWithText(appInfoProvider.appInfoToReturn)` addresses it with no widget change — task 11's
  "testTag is the recurring tax" note doesn't apply to every Screen, only to widgets whose
  interactive elements lack unique text/content-description semantics.
- **The route-carrying-Screen gap turns out to be smaller than it looked when scoped.** This pilot's
  `SettingsAboutScreenRouteNavDestination` is a bare `data object`, so it never touched
  `SavedStateHandle`/`toRoute<T>()`. But since a Compose UI test constructs the ViewModel directly
  (no Koin, confirmed by this task's own scoping pass) exactly like every existing `commonTest`
  ViewModel test does, a route-carrying Screen's test would build its `SavedStateHandle` the identical
  way those tests already do (`SavedStateHandle(mapOf("id" to id))`, see the `testing` agent's
  ViewModel-test pattern) and pass the ViewModel into `setContent` — nothing about being inside
  `runComposeUiTest` changes that construction path. Flagging this as **resolved by inference, not by
  a test that actually exercises it** — no session has yet written a Compose UI test for a
  route-carrying Screen, so treat this as a strong prediction, not a proven fact, until one is written.

**Recommendation: worth sweeping to other feature Screens next**, same wiring-cost-paid-once
reasoning as task 10 → 11. No task is scoped for the sweep yet (candidate Screen list and per-Screen
sizing is its own small scoping pass, same as this task needed before task 12 was runnable) — pick it
up as a new task the same way task 11 was added after task 10, rather than assuming this pilot's
single data point generalizes to every `*Screen` shape (multi-state-source Screens, dialogs,
paging lists) without a second worked example.

---

## Task 13 — Compose UI test, route-carrying + async-loading Screen (scoped and done 2026-08-07)

**Why:** task 12 proved a Screen with a real `ViewModel` and no nav-route params. Its Result note
flagged one gap as "resolved by inference, not by a test that actually exercises it": a Screen whose
`ViewModel` reads `SavedStateHandle.toRoute<T>()`, combined with a `ViewModel` that loads state
asynchronously in `init` (every existing Screen test so far had a synchronous constructor). This task
picks one Screen that has both and proves the combination for real.

**Pilot: `ProjectValuesScreen`** (`feature/settings/ui/.../attributes/projectvalues/`). Chosen because
it needed zero new scaffolding, so the task was pure verification of the pattern rather than more
build-wiring or fake work:

- Its `NavDestination` is `data class ProjectValuesNavDestination(val typeName: String)` — a plain
  `toRoute<T>()` with no `typeMap`, unlike `WorkItemEditTagsNavDestination` (which carries a
  `TaskIdentifier` and needs one). `ProjectValuesViewModelTest` already builds the route as
  `SavedStateHandle(mapOf("typeName" to type.name))` for its own unit tests — the Compose UI test
  reuses that exact construction.
- `init` calls `loadItems()` and `loadPresetColors()`, both `viewModelScope.launch { ... }` —
  the async-loading half of the gap.
- `feature/settings/ui` already has the `uiTest` build wiring from task 12, and
  `FakeProjectValuesRepository` / `FakeTaigaSessionStorage` already exist in `:testing` — no new
  dependencies, no new fakes.

**Result:** Works, and the async-loading half turned out to need nothing extra.
`ProjectValuesScreenTest` (`feature/settings/ui/src/jvmTest/.../attributes/projectvalues/ProjectValuesScreenTest.kt`)
constructs `MainDispatcherRule` (which defaults to `UnconfinedTestDispatcher`) and calls `.setup()` in
`@BeforeTest`, **before** the `ViewModel` is constructed inside the test body. Because the dispatcher
is unconfined, `init`'s two `viewModelScope.launch` calls run to completion synchronously as part of
the `ProjectValuesViewModel(...)` constructor call itself — by the time `setContent { ... }` runs, the
state already holds the loaded items. No `waitUntil`, no `advanceUntilIdle`, no interaction with
Compose's own test clock at all; the two dispatchers (`kotlinx-coroutines-test`'s `Dispatchers.Main`
and Compose's `runComposeUiTest` frame clock) never needed to interact because the coroutine work was
already finished before the first frame. This is the same reasoning
`ProjectValuesViewModelTest`'s own doc comment already states for its plain unit tests — it just also
turned out to be true inside `runComposeUiTest`, which was the one thing this task didn't already know.
**Expect this to fail to generalize** the moment a Screen's `ViewModel` uses a real `Dispatchers.IO`-style
suspension point (a fake with an artificial `delay`, for instance) instead of a fake that returns
immediately — nothing here proves the unconfined-dispatcher trick survives an actual suspension, only
that it survives a same-thread call. Flagging that as the next real unknown rather than assuming it away.

The test asserts the loaded item's name is visible (`onNodeWithText(item.name).assertExists()`) and
that `getProjectValuesCalls == listOf(type)`, i.e. the type parsed out of the route by
`toRoute<ProjectValuesNavDestination>()` is what actually reached the repository — the concrete proof
that the route wiring, not just the loading wiring, works inside `runComposeUiTest`.

Passes via `./gradlew :feature:settings:ui:jvmTest`, confirmed picked up by the root `./gradlew jvmTest`
(the `TEST-...ProjectValuesScreenTest.xml` file was deleted and reappeared after a full-root run, same
check tasks 10 and 12 used). `ktlintCheck` is clean.

**Recommendation:** the route-carrying-Screen and async-loading-Screen gaps flagged after task 12 are
both closed now — a Screen test for any `ViewModel` whose collaborators are ordinary `:testing` fakes
(no artificial delay) can follow this pattern directly. The one remaining open question is the
artificial-suspension case named above; worth a one-off check if a future Screen's fakes ever need a
real `delay()` to simulate loading UI, but not worth a dedicated task until one actually does.

---

## Task 14 — Compose UI test, paging-list Screen (scoped and done 2026-08-07)

**Why:** task 12's Result note named three Screen *shapes* not yet covered by a worked example and
explicitly declined to assume the pattern generalizes to them from inference alone:
multi-state-source Screens, dialogs, and paging lists (`LazyPagingItems`). This task picks the paging
list shape and proves it for real, the same way task 13 picked and closed the route-carrying +
async-loading shape.

**Survey:** grepped `feature/*/ui/src/commonMain` for `collectAsLazyPagingItems`/`LazyPagingItems`.
Seven Screens qualify: `EpicsScreen`, `IssuesScreen`, `ScrumBacklogScreen`, `ScrumOpenSprintsScreen`,
`ScrumClosedSprintsScreen` (via `SprintsListContentWidget`), and `ProjectSelectorScreen`.
(`WikiPagesScreen`/`WikiBookmarksScreen`, named as candidates in the task brief, turned out not to use
Paging at all — grepped and confirmed empty.)

**Pilot: `ProjectSelectorScreen`** (`feature/projectselector/ui/.../ProjectSelectorScreen.kt` +
`ProjectSelectorViewModel.kt`). Chosen over the other six for having the fewest moving parts on top of
the paging list itself:

- `EpicsScreen`/`IssuesScreen` also render a `TaskFiltersWidget` and a top-bar add action gated on a
  permission — real functionality, but extra state this pilot doesn't need to prove the paging shape.
- The three `feature/scrum/ui` sprint screens looked heavier at the time this was written, but that
  was wrong — **correction (2026-08-07, during task 15–21 scoping):** `SprintsListContentWidget` has
  no swimlane or drag-and-drop code anywhere in it (grepped `feature/scrum/ui/src/commonMain` for
  `drag`/`Drag`/`swimlane`/`Swimlane`: zero hits); it's a plain `PullToRefreshBox` + `LazyColumn`. The
  swimlane/drag-drop functionality this line meant to describe lives in `feature/kanban/ui`'s
  `KanbanViewModel`/`KanbanScreen` instead — a different module. At the time, `EpicsScreen`/
  `IssuesScreen`'s filters widget was still the real reason the scrum screens weren't picked first.
- `ProjectSelectorScreen` is a search field plus a `LazyColumn` of `projects.itemCount` rendered from
  `viewModel.projects.collectAsLazyPagingItems()` — nothing else. Its `NavDestination` is a plain
  `data class ProjectSelectorNavDestination(val isFromLogin: Boolean = false)`, so the route half of
  task 13's pattern reuses directly, and `ProjectSelectorViewModelTest` already builds its
  `SavedStateHandle` and all four collaborator fakes (`FakeProjectsRepository`, `FakeFiltersStorage`,
  `FakeTaigaSessionStorage`, `FakeDataCleaner`) — no new fakes needed for construction.

**Result:** Works, but not for free — the one genuinely new problem was that every existing paging
fake (`FakeProjectsRepository.fetchProjects` included) hard-codes `flowOf(PagingData.empty())`, so
nothing in `:testing` could make a paging list actually render an item. `PagingData.empty()` never
presents anything to `collectAsLazyPagingItems()`, by design — it exists to make ViewModel
*construction* safe, not to drive a rendering test. Extended `FakeProjectsRepository` with a
`fetchProjectsResult: ImmutableList<Project>` field: empty still returns `PagingData.empty()` (so
every existing consumer of the fake is unaffected), non-empty returns `PagingData.from
(fetchProjectsResult)` — a static, already-loaded page that needs no `Pager`/`RemoteMediator` to
construct, which is all a "does this Screen shape render inside `runComposeUiTest`" test needs. Also
added `fetchProjectsCalls` to record the `query` argument, matching the recorder convention used
everywhere else in the fake inventory (not asserted by this test, but free to add while in the file).

`ProjectSelectorScreenTest`
(`feature/projectselector/ui/src/jvmTest/.../ProjectSelectorScreenTest.kt`) constructs
`ProjectSelectorViewModel` directly with a `FakeProjectsRepository` seeded with one project via
`fetchProjectsResult`, wraps `setContent` the same way tasks 12–13 do
(`CompositionLocalProvider(LocalTopBarConfig provides TopBarController())`), and asserts
`onNodeWithText("${project.name} (${project.slug})").assertExists()` — the exact text
`project_name_template` renders, proving a real item reached the screen through
`collectAsLazyPagingItems()`. `MainDispatcherRule` is needed (same reasoning as task 13): `init`
collects `taigaSessionStorage.currentProjectIdFlow`, and `UnconfinedTestDispatcher` runs that
collector to completion before `setContent` runs.

**Everything else about the pattern held with zero surprises** — no Koin, same
`LocalTopBarConfig` provider, same direct-construction `SavedStateHandle`, same "unconfined dispatcher
finishes `init`'s coroutine work before the first frame" reasoning as task 13. The paging-specific
unknown was entirely inside the fake, not inside `runComposeUiTest` itself: once
`FakeProjectsRepository` could hand back a real `PagingData`, `collectAsLazyPagingItems()` presented
it exactly as it would from a real `Pager`, with no special handling needed in the test or the
Screen. Build wiring (`uiTest`/`desktop.uiTestJUnit4`/`desktop.currentOs` hoisted to top-level `val`s
under `@file:OptIn(ExperimentalComposeLibrary::class)`, referenced in `jvmTest.dependencies`) was a
direct copy of the same block tasks 10 and 12 added elsewhere — `feature/projectselector/ui` had no
`jvmTest` source set or `uiTest` wiring before this task, same starting point task 12 found in
`feature/settings/ui`.

Passes via `./gradlew :feature:projectselector:ui:jvmTest`, confirmed picked up by the root
`./gradlew jvmTest` (the `TEST-...ProjectSelectorScreenTest.xml` file was deleted and reappeared after
a full-root run, same check tasks 10, 12 and 13 used). `ktlintCheck` is clean. One unrelated
intermittent failure was seen during verification (`core/storage:jvmTest`'s `FiltersStorageImplTest`
on one run, `feature/wiki/ui:jvmTest`'s `WikiPageViewModelTest` on another) — confirmed pre-existing
by reproducing the same flakiness on a clean `git stash`'d tree with none of this task's changes
present; this is gotcha 7's cross-module leaked-coroutine-exception hazard, not something this task
introduced.

**Recommendation:** all three Screen shapes task 12 named (route-carrying + async-loading, closed by
task 13; paging lists, closed by this task) now have a worked example. **Dialogs and
multi-state-source Screens are still open** — pick either up as a new task the same way this one was
scoped from task 13's Result note. A **sweep across the other five paging-list Screens** is also not
scoped: each of `EpicsScreen`/`IssuesScreen`/the three scrum sprint screens adds its own extra state
(filters widget, permissions) on top of the paging list, so — same reasoning task 12 used
for not assuming a sweep from one Screen — size that as its own task rather than assuming this
pilot's fake extension covers `FakeEpicsRepository`/`FakeUserStoriesRepository`/`FakeSprintsRepository`
too; each still only returns `PagingData.empty()` and would need the identical treatment given to
`FakeProjectsRepository` here.

**Scoped into tasks 15–21 (2026-08-07):** see those sections below — two Screen-shape spikes (dialog,
multi-state-source) plus the five-Screen paging sweep this note named, informed by a fuller survey
than this Result note had (module-by-module build-wiring status, exact fake gaps, and the correction
above about which module actually has the swimlane/drag-drop code).

---

## Task 15 — Compose UI test, dialog Screen (scoped 2026-08-07)

**Why:** task 14 closed the paging-list shape; task 12's original list of unproven shapes still has
dialog visibility and multi-state-source ViewModels open. This task picks the dialog shape: a Screen
whose UI shows/hides a dialog composable based on ViewModel state, where the interesting transition
is dialog visibility rather than loaded content.

**Pilot: `TrustedCertificatesScreen`**
(`feature/settings/ui/.../trustedcerts/TrustedCertificatesScreen.kt` +
`TrustedCertificatesViewModel.kt`). Chosen over `WikiBookmarksScreen` and `TagsScreen` (also surveyed)
for having the fewest moving parts on top of the dialog itself:

- `WikiBookmarksScreen` combines the dialog with two sequential async loads in `init`
  (`getPermissions()` then `fetchBookmarks()`), permission-gated actions, and a `Channel` refresh
  event — real functionality, but more than this pilot needs, and `feature/wiki/ui` has no `uiTest`
  wiring yet either.
- `TagsScreen` shows three dialog-ish widgets at once (`ConfirmActionDialog`, `TaigaLoadingDialog`,
  a `TagEditDialog` behind a delegate) plus a merge-mode toggle — a good *second* dialog example
  later, a bad first one.
- `TrustedCertificatesViewModel` has **one** constructor dep (`TrustedCertStorage`). The dialog is a
  plain `ConfirmActionDialog` gated on `state.isRevokeDialogVisible`, driven by
  `entryToRevoke`/`onRevokeClick`/`revokeEntry`/`closeRevokeDialog`
  (`TrustedCertificatesScreen.kt:54-62`, `TrustedCertificatesViewModel.kt:34-48`). No route params, no
  permissions, no other widgets beyond a plain `LazyColumn`. `feature/settings/ui` already has the
  `uiTest` wiring (task 12), and `FakeTrustedCertStorage` already exists with a seedable
  `MutableStateFlow<List<PendingCertTrust>>` behind `getAllFlow()` — no new fake needed.

**Scope:**

- Seed `FakeTrustedCertStorage` with one entry, click whatever triggers `onRevokeClick`, assert the
  `ConfirmActionDialog`'s text/confirm button becomes visible (`assertExists()`), click confirm, assert
  the dialog closes and `TrustedCertStorage`'s revoke was called with the right entry.
- No new build wiring, no new fake. If either turns out to be needed, that's a sign this pilot's
  "fewest moving parts" read was wrong — note it plainly rather than quietly absorbing the extra work.

**Done when:** the new test passes via `./gradlew :feature:settings:ui:jvmTest`, is picked up by the
root `./gradlew jvmTest` (same `TEST-...xml`-reappears check tasks 10/12/13/14 used), and
`./gradlew ktlintCheck` is clean.

**Finalize focus:** whether asserting a dialog *transition* (closed → open → closed) needs anything
beyond the click/type interactions task 11 already established, or whether Compose's test clock needs
an explicit advance to see the dialog appear — name it either way.

**Result (2026-08-07):** Works, and the "fewest moving parts" read held — no new build wiring, no new
fake. `TrustedCertificatesScreenTest`
(`feature/settings/ui/src/jvmTest/.../trustedcerts/TrustedCertificatesScreenTest.kt`) constructs
`TrustedCertificatesViewModel` directly with a `FakeTrustedCertStorage` seeded via
`runBlocking { trustedCertStorage.trust(entry) }` — `trust()`/`untrust()` are declared `suspend` but
never actually suspend (plain `MutableStateFlow` field assignment), so a bare `runBlocking` outside
`runComposeUiTest`'s own scope is enough; no `MainDispatcherRule`-driven `UnconfinedTestDispatcher`
trick was needed for the *seeding* step, only for `init`'s `getAllFlow().collect { }` (tasks 13/14's
usual reasoning) so the seeded entry is already in `state.entries` before `setContent` runs.

The test asserts the full transition in one sequence: entry visible / dialog absent →
`onNodeWithContentDescription("Revoke").performClick()` → dialog title (`"Revoke trust"`) visible →
`onNodeWithText("Yes").performClick()` → dialog title gone, entry gone from the list, and
`trustedCertStorage.untrustCalledWith == entry.host to entry.sha256Fingerprint`. **No explicit
advance of Compose's test clock was needed anywhere in the sequence** — `ConfirmActionDialog` is
conditionally composed only when `isVisible` (and here, additionally gated on
`state.entryToRevoke != null` in `TrustedCertificatesScreen.kt:54`), so each `performClick()` runs its
`onClick` synchronously, `revokeEntry()`'s `viewModelScope.launch { }` runs to completion synchronously
under the `UnconfinedTestDispatcher` from `MainDispatcherRule` (same reasoning as tasks 13/14 for
`init`, just triggered by a click instead), and the next assertion sees the fully-updated state with no
polling. Confirms task 11's click/type-only interaction scope covers dialog-open and dialog-close
transitions with nothing extra — this was the open question the Finalize focus named, and the answer
is no, nothing extra is needed.

Passes via `./gradlew :feature:settings:ui:jvmTest`, confirmed picked up by the root
`./gradlew jvmTest` (the `TEST-...TrustedCertificatesScreenTest.xml` file was deleted and reappeared
after a full-root run, same check tasks 10/12/13/14 used). `ktlintCheck` is clean.

**Recommendation:** both shapes named after task 12 — dialog (this task) and multi-state-source
(task 16) — will be closed once task 16 lands. Next up: **task 16**, `SettingsInterfaceScreen`.

---

## Task 16 — Compose UI test, multi-state-source Screen (scoped 2026-08-07)

**Why:** the other shape from task 12's original list — a ViewModel that reacts to more than one
independent state source, not just one `State` object built once in the constructor.

**Pilot: `SettingsInterfaceScreen`/`SettingsInterfaceViewModel`**
(`feature/settings/ui/.../interfacescreen/`). Chosen over `EpicsViewModel`/`IssuesViewModel`/
`ScrumBacklogViewModel` (which use a real `combine()` but are the same three Screens task 20/19 already
cover — testing them here would double-count, not add a data point) and over `KanbanViewModel`
(deferred, see [Considered and deferred](#considered-and-deferred)):

- `SettingsInterfaceViewModel` has **two** constructor deps (`TaigaSessionStorage`, `CrashReporter`).
  `init` launches **two independent** `onEach{}.launchIn(viewModelScope)` collectors over two
  independent flows off `TaigaSessionStorage` — `themeSettings` and `crashReportingEnabled`
  (`SettingsInterfaceViewModel.kt:37-51`) — no `combine()`, but exactly the "reacts to two separate
  sources, not one" case this task needs.
- Module already wired (`feature/settings/ui`, task 12). `FakeTaigaSessionStorage`
  (`themeSettings`/`crashReportingEnabled` both seedable) and `FakeCrashReporter` (added task 12)
  already exist — no new fakes needed.

**Scope:**

- Seed both `FakeTaigaSessionStorage` flows with non-default values, construct the ViewModel, assert
  both values are reflected in rendered UI in the same test — the point is proving two independently
  updating sources both reach the screen, not that either one alone does.
- `MainDispatcherRule` needed: both collectors are `viewModelScope.launch`-style work in `init`
  (via `launchIn`), so use the same `UnconfinedTestDispatcher`-before-construction pattern as tasks
  13/14.

**Done when:** passes via `./gradlew :feature:settings:ui:jvmTest`, picked up by root
`./gradlew jvmTest`, `ktlintCheck` clean.

**Finalize focus:** whether two independent `onEach{}.launchIn` collectors both finish before the
first frame the same way task 13's two `viewModelScope.launch` calls did, or whether one races the
other under `UnconfinedTestDispatcher` — this is a shape task 13 didn't test (sequential launches in
one coroutine vs. two independently launched collectors).

**Result (2026-08-07):** Works, and the two independent collectors race no more than task 13's
sequential launches did — both `themeSettings.onEach{}.launchIn` and
`crashReportingEnabled.onEach{}.launchIn` finish before `setContent`'s first frame under
`UnconfinedTestDispatcher`, same reasoning as tasks 13/14, just with two separate `launchIn` calls
instead of one coroutine doing two things sequentially. No polling, no `advanceUntilIdle`, no
ordering issue between the two collectors — the test seeds both to non-default values
(`ThemeSettings.Dark`, not the `System` default; `crashReportingEnabled = true`) and asserts both
landed in one `setContent` call.

`SettingsInterfaceScreenTest`
(`feature/settings/ui/src/jvmTest/.../interfacescreen/SettingsInterfaceScreenTest.kt`) constructs
`SettingsInterfaceViewModel` directly with a `FakeTaigaSessionStorage` seeded via two **new
constructor parameters** — `themeSettings: ThemeSettings` and `crashReportingEnabled: Boolean` — and
a `FakeCrashReporter` with `isAvailable = true` (needed to render the crash-reporting `Switch` at
all; `PrivacySection` is conditionally composed on `state.isCrashReportingAvailable`). Both fields
were previously hard-coded `flowOf(...)` in `FakeTaigaSessionStorage` with **no existing test
touching either one** (grepped every call site before changing the constructor) — added as
constructor parameters with defaults matching the old hard-coded values
(`ThemeSettings.default()`, `true`) so every existing call site is unaffected. Fake inventory in
`.claude/agents/testing.md` updated.

Asserts the segmented-button theme selector via `onNodeWithText("Dark").assertIsSelected()` +
`onNodeWithText("System").assertIsNotSelected()` (proving the seeded value, not the default, won) and
the crash-reporting `Switch` via `onNode(isToggleable()).assertIsOn()` — the `Switch` has no unique
text/content-description of its own, and `isToggleable()` (`androidx.compose.ui.test`) is a clean way
to address the one `Switch` on screen without adding a `testTag`. `MainDispatcherRule` is needed
(same reasoning as tasks 13–15): both `init` collectors are `viewModelScope`-scoped work.

Passes via `./gradlew :feature:settings:ui:jvmTest`, confirmed picked up by the root
`./gradlew jvmTest` (the `TEST-...SettingsInterfaceScreenTest.xml` file was deleted and reappeared
after a full-root run, same check tasks 10/12/13/14/15 used). `ktlintCheck` is clean (one fix needed:
`assertIsOn`/`assertIsSelected`/`assertIsNotSelected` import order). One intermittent failure was
seen on one `jvmTest` run (`feature/wiki/ui:jvmTest`'s `WikiPageViewModelTest`) and not on a repeat
run with identical code — this is gotcha 7's cross-module leaked-coroutine-exception hazard, already
documented, not something this task introduced.

**Recommendation:** both shapes named after task 12 — dialog (task 15) and multi-state-source (this
task) — are now closed. Every Screen shape task 12 originally flagged as unproven (route-carrying,
async-loading, paging list, dialog, multi-state-source) now has a worked example. Next up: **task
17**, the first of the five-Screen paging sweep (`ScrumClosedSprintsScreen`).

---

## Task 17 — Paging sweep, `ScrumClosedSprintsScreen` (scoped 2026-08-07)

**Why:** task 14 closed one of six paging-list Screens (`ProjectSelectorScreen`); this starts the
sweep across the remaining five, taking the simplest first. `ScrumClosedSprintsScreen`'s whole
ViewModel body is `val closedSprints = sprintsRepository.getSprintsPaging(isClosed = true)
.cachedIn(viewModelScope)` (`ScrumClosedSprintsViewModel.kt:10-14`) — **one** constructor dep, no
`init`, no permissions, no dialog, no route (`goToSprint`/`updateData` are plain params, not a
`NavDestination`). Simpler than `ProjectSelectorScreen` was.

**Build wiring:** `feature/scrum/ui/build.gradle.kts` has no `jvmTest`/`uiTest` wiring yet — add it
once here (same `compose.dependencies.uiTest`/`desktop.uiTestJUnit4`/`desktop.currentOs` pattern as
tasks 10/12/14). Tasks 18 and 19 reuse it; no need to re-add it there.

**Fake:** `FakeSprintsRepository.getSprintsPaging` (`testing/.../repo/FakeSprintsRepository.kt:21-22`)
returns `flowOf(PagingData.empty())` unconditionally — extend it the same way task 14 extended
`FakeProjectsRepository` (a `…Result: ImmutableList<Sprint>` field, empty → `PagingData.empty()`,
non-empty → `PagingData.from(...)`). **Note the `isClosed` parameter is currently ignored** by the
fake — both `ScrumOpenSprintsViewModel` and `ScrumClosedSprintsViewModel` read the same
`getSprintsPaging` call and get the same fake output regardless of the flag. Fine for this task (only
one of the two VMs is under test), but flag it in task 18's own run since a test that seeds data for
both open and closed sprints in the same test would need the fake to respect `isClosed` — decide there
whether to add that or keep it out of scope.

**Scope:** add the wiring, extend the fake, write one test seeding one closed sprint, asserting it
renders and the `updateData` refresh param works if it's easy to exercise (skip it if not — that's a
secondary check, not the point of this test).

**Done when:** passes via `./gradlew :feature:scrum:ui:jvmTest`, picked up by root `./gradlew jvmTest`,
`ktlintCheck` clean. Update the fake inventory in `.claude/agents/testing.md`.

**Finalize focus:** low — this should "just work" per tasks 12–14's pattern holding with zero
surprises so far. If it doesn't, that's the interesting finding.

**Result (2026-08-07):** Worked with zero surprises, as predicted. `ScrumClosedSprintsScreenTest`
(`feature/scrum/ui/src/jvmTest/.../closed/ScrumClosedSprintsScreenTest.kt`) passes via
`./gradlew :feature:scrum:ui:jvmTest`, is picked up by the root `./gradlew jvmTest` (confirmed via the
`TEST-...ScrumClosedSprintsScreenTest.xml` result file), and `ktlintCheck` is clean.

- **Build wiring** was the same hoist-to-top-level-`val` pattern as tasks 10/12/14 — added
  `@file:OptIn(ExperimentalComposeLibrary::class)` + the three `val`s + `jvmTest.dependencies` block to
  `feature/scrum/ui/build.gradle.kts`. No new gotcha.
- **`FakeSprintsRepository.getSprintsPaging`** got a `getSprintsPagingResult: ImmutableList<Sprint>`
  field, same empty→`PagingData.empty()`/non-empty→`PagingData.from(...)` shape as
  `FakeProjectsRepository.fetchProjectsResult`. **Confirmed the `isClosed`-ignored gap the task
  flagged**: the fake doesn't branch on the `isClosed` parameter, so both `ScrumOpenSprintsViewModel`
  and `ScrumClosedSprintsViewModel` would see the same `getSprintsPagingResult` if a test constructed
  both in the same run. Not a problem here (only one VM under test) — flagged again for task 18 to
  decide, per this task's own note. Fake inventory in `.claude/agents/testing.md` updated (both the
  `FakeSprintsRepository` bullet and the paging-fakes table note).
- **`MainDispatcherRule` was included even though this ViewModel has no `init` block** — its one line,
  `sprintsRepository.getSprintsPaging(isClosed = true).cachedIn(viewModelScope)`, still launches a
  coroutine on `viewModelScope` (which needs `Dispatchers.Main` set) via `cachedIn`. Without the rule
  the constructor would likely throw a missing-Main-dispatcher error; didn't test the negative case
  since task 14 already established this need for `cachedIn(viewModelScope)`.
- One `jvmTest` full-root run turned up a `WikiPageViewModelTest` Turbine timeout failure —
  A/B'd against a clean tree (`git stash -u`, `--rerun-tasks` on just that module) and it passed in
  isolation, then passed again in a second full-root run with this task's changes restored. Pre-existing
  flake, not caused by this change.

**Next: task 18** (`ScrumOpenSprintsScreen`) — reuses this task's `feature/scrum/ui` build wiring and
`FakeSprintsRepository` extension; its own scope note says to check whether the `isClosed` fake gap
above needs closing now that both VMs are candidates for the same fake.

---

## Task 18 — Paging sweep, `ScrumOpenSprintsScreen` (scoped 2026-08-07)

**Why:** continues the sweep, reusing task 17's `feature/scrum/ui` build wiring and
`FakeSprintsRepository` extension. Also a second dialog-shape data point: on top of the paging list,
this Screen has a top-bar "Add Sprint" action (gated on `canAddSprint`) that opens a real
`EditSprintDialog` (create-sprint flow), plus a `reloadOpenSprints` `Channel` event. No filters widget,
no search, no swimlane/drag-drop (see task 14's correction above) — `SprintsListContentWidget` is a
plain `LazyColumn`.

**Depends on task 17 landing first** (shares its build wiring and fake extension) — don't start this
before 17 is done.

**Scope:** one test rendering a seeded open sprint (reusing task 17's `FakeSprintsRepository`
extension — check whether `isClosed` needs to be respected now that both VMs are candidates for the
same fake, per task 17's note). Optionally a second test opening `EditSprintDialog` via the add
action and asserting it becomes visible — worth doing if task 15 already proved the
click-to-open-dialog pattern; otherwise the render-only test is enough for this task and the dialog
interaction can be a note for later, same as task 12 left a gap noted rather than chased.

**Done when:** passes via `./gradlew :feature:scrum:ui:jvmTest`, picked up by root `./gradlew jvmTest`,
`ktlintCheck` clean.

**Finalize focus:** whether the `isClosed` fake gap from task 17 actually mattered here, and if the
`EditSprintDialog` interaction was attempted, whether it needed anything task 15 didn't already show.

---

## Task 19 — Paging sweep, `ScrumBacklogScreen` (scoped 2026-08-07)

**Why:** the first paging sweep Screen with a *real* `combine()` — `ScrumBacklogViewModel` combines
`session.userStoriesFilters` with a local `searchQuery` `MutableStateFlow` and `flatMapLatest`s the
result into `userStoriesRepository.getUserStoriesPaging(...)`
(`ScrumBacklogViewModel.kt:53-60`) — plus a `TaskFiltersWidget`, a search field, and a top-bar
"Add User Story" action gated on `canAddUserStory`. 4 constructor deps: `FiltersStorage`(session),
`UserStoriesRepository`, `FiltersRepository`, `ProjectsRepository`.

**Build wiring:** reuses task 17's `feature/scrum/ui` addition — no new wiring needed.

**Fake:** `FakeUserStoriesRepository.getUserStoriesPaging`
(`testing/.../repo/FakeUserStoriesRepository.kt:20-23`) returns `flowOf(PagingData.empty())` — extend
it the same way as tasks 14/17.

**Scope:** one test seeding a non-empty result, asserting the item renders through the
`combine()`+`flatMapLatest` chain with the default (empty) search query and default filters — proving
the combine shape works inside `runComposeUiTest`, not exercising every filter/search permutation.
That's this task's real unknown: task 14 never needed a `combine()`, only a class-level cold flow.

**Done when:** passes via `./gradlew :feature:scrum:ui:jvmTest`, picked up by root `./gradlew jvmTest`,
`ktlintCheck` clean. Update the fake inventory.

**Finalize focus:** high relative to 17/18 — this is the first test of the `combine()`+`flatMapLatest`
shape shared by `EpicsViewModel`/`IssuesViewModel`. If it needs something beyond the established
pattern (e.g. the default `searchQuery` flow needing a nudge before the paging flow emits), say so
precisely — tasks 20/21 will follow whatever this one discovers.

---

## Task 20 — Paging sweep, `EpicsScreen` (scoped 2026-08-07)

**Why:** same `combine()`+`flatMapLatest`+filters-widget shape as task 19, different module
(`feature/epics/ui`). `EpicsViewModel` also adds a top-bar "Add Epic" action gated on `canAddEpic`
and `snackBarMessage` events. 4 constructor deps: `FiltersStorage`(session), `EpicsRepository`,
`FiltersRepository`, `ProjectsRepository`.

**Depends on task 19 landing first** — apply whatever task 19 discovers about the `combine()` shape
here rather than re-deriving it.

**Build wiring:** `feature/epics/ui/build.gradle.kts` has no `jvmTest`/`uiTest` wiring yet — this is a
new module, add it fresh (same pattern as task 17 did for `feature/scrum/ui`).

**Fake:** `FakeEpicsRepository.getEpicsPaging` (`testing/.../repo/FakeEpicsRepository.kt:20-23`)
returns `flowOf(PagingData.empty())` — extend the same way as tasks 14/17/19.

**Scope:** one test, same shape as task 19's — seed a non-empty result, assert it renders with default
filters/search.

**Done when:** passes via `./gradlew :feature:epics:ui:jvmTest`, picked up by root `./gradlew jvmTest`,
`ktlintCheck` clean. Update the fake inventory.

**Finalize focus:** low if task 19 already answered the `combine()` question — this should be close to
mechanical. Worth noting only if something about this module differs from the scrum backlog case.

---

## Task 21 — Paging sweep, `IssuesScreen` (scoped 2026-08-07)

**Why:** same shape as tasks 19/20 again, but with a real pre-existing bug in the fake:
`FakeIssuesRepository.getIssuesPaging` (`testing/.../repo/FakeIssuesRepository.kt:21-22`) returns
**`emptyFlow()`**, not `flowOf(PagingData.empty())` like every other paging fake — it never emits any
`PagingData` at all, which is a stricter gap than "always empty." 4 constructor deps: same shape as
Epics (`FiltersStorage`(session), `IssuesRepository`, `FiltersRepository`, `ProjectsRepository`), same
top-bar add action gated on `canCreateIssue`, same `snackBarMessage` events.

**Build wiring:** `feature/issues/ui/build.gradle.kts` has no `jvmTest`/`uiTest` wiring — add it fresh,
same as task 20 did for epics.

**Fake — two steps, not one:** first restore the `flowOf(PagingData.empty())` baseline every other
paging fake has (fixing the `emptyFlow()` bug — check whether any existing test relies on the flow
never emitting before changing it), then extend it to `PagingData.from(...)` the same way as the
others. Treat the first step as a real bug fix worth calling out on its own, not folded silently into
"added the extension."

**Scope:** one test, same shape as tasks 19/20's, once the fake actually emits something.

**Done when:** passes via `./gradlew :feature:issues:ui:jvmTest`, picked up by root
`./gradlew jvmTest`, `ktlintCheck` clean. Update the fake inventory.

**Finalize focus:** the `emptyFlow()` bug itself — was it dead code no test exercised, or did fixing it
change behavior for an existing test? Either answer is worth recording; this is the one task in the
sweep with a real pre-existing defect, not just a missing extension.

---

## Considered and deferred

Recorded so they are not silently forgotten, and not re-proposed without new information.

| Idea | Why not now |
|---|---|
| Run `commonTest` on a native target (`iosSimulatorArm64Test`) | Real gap — no `expect/actual` divergence is caught by tests today. But CI is `ubuntu-latest`; this needs a macOS runner at roughly 10× the minutes. Worst value-per-cost on the list. Revisit if an iOS-only regression actually ships. |
| Screenshot tests (Roborazzi / Paparazzi) | High maintenance for a solo-maintained app. Revisit only if visual regressions become a recurring, concrete problem — not preemptively. |
| Integration tests against a live Taiga instance | `tools/seed` and the local instance in `docs/local-info.md` make it feasible, but it would be a manually-triggered job, not part of PR CI. No pull for it yet. |
| Adding a mocking framework | The hand-written-fake convention is working and is genuinely consistent. Do not introduce MockK to `commonTest`. |
| Testing `dto` and pure-`domain` modules | Most of the 36 untested modules are serializable data holders or interfaces plus models. Correctly untested; leave them. |
| Compose UI test for `KanbanScreen`/`KanbanViewModel` | Surveyed 2026-08-07 while scoping tasks 15–21 as a multi-state-source candidate: combines `getKanbanData()` with independently-loaded filters (`loadFiltersData()`), per-swimlane filter state, and optimistic drag-drop reordering (`moveStory`) — 4 ViewModel deps, real swimlanes, real drag-drop. Heaviest candidate found and the only one with drag-drop, which may not fit task 11's established click/type-only interaction scope. Deferred until tasks 15–21 land and give more data on whether this test style even reaches drag-drop assertions; revisit as its own scoped task then, not assumed in from the others. |

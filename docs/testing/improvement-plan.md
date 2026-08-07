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
   task 4).
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
| 12 | Compose UI test spike, feature-level Screen (`SettingsAboutScreen`) | S | 📋 scoped — 2026-08-07, not yet started. ⬅ NEXT — see the task's own section for the pilot and the two resolved unknowns. |

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

Task 12 is next, and is now scoped and runnable — see its own section below.

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

**Not yet started.** This section is the scoping pass task 10/11's own notes said this task needed
before it becomes runnable — running it (writing the fakes, the build wiring, and the test) is the
next session's work, not this one's.

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

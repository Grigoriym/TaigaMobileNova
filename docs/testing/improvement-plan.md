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
| 12 | Expand Compose UI tests to feature-level Screens (Composable + ViewModel + fakes) | ? | 🧭 future phase — not yet scoped, see note below task 11. ⬅ NEXT — needs its own scoping/spike pass before it's runnable, see the task's own section. |

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
take the NEXT candidate and run it. **Task 12 is a different kind of not-yet** — not gated on asking,
but not yet scoped at all: gregory's stated intent is to eventually expand Compose UI testing to the
whole project (feature-level Screens, not just uikit widgets), but there is no precedent yet for
wiring a ViewModel + Koin + navigation into a `runComposeUiTest`, so it needs its own sizing/spike
pass — written as its own task — before it can be picked up the way 11's candidates can.

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

Task 12 is next, but it isn't runnable yet: it needs its own scoping/spike pass first, per its
section below.

---

## Task 12 — Expand Compose UI tests to feature-level Screens (not yet scoped)

**Not a runnable task yet.** Recorded so gregory's stated direction ("after uikit, enlarge this to
the whole project") isn't lost, and so nobody re-derives from scratch that this is the intended next
phase now that task 11's candidate list is exhausted.

**Why this needs its own scoping pass before it's a task:** everything task 10 proved holds for a
bare `commonMain` Composable with no external dependencies. A feature-level Screen additionally has a
ViewModel (needs a real one with `:testing` fakes, or a fake ViewModel — no precedent for either
inside a `runComposeUiTest` in this repo), typically reads `SavedStateHandle`/nav-route arguments
(task 10's test called the Composable directly with plain parameters — a Screen doesn't have that
option), and often composes uikit widgets that would themselves need `testTag`s added under task 11
first. None of that is hard, but none of it is proven either, and sizing it accurately means picking
one real Screen and finding out — the same spike shape task 10 already used once.

**When picked up:** open with a proper Task-10-style spike section (Why / Scope / Watch for / Done
when / Finalize focus) against one concrete Screen, not a general "add Screen tests" mandate — the
existing task-sizing convention in this doc (XS/S/M, one clean context) applies here as much as
anywhere else in the plan.

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

# Integration tests against a live Taiga instance — plan

**Created:** 2026-08-08
**Baseline:** [docs/issues/2026-08-08-integration-tests-live-taiga.md](../issues/2026-08-08-integration-tests-live-taiga.md)
— the investigation that settled the shape of this work: plain `jvmTest`, env-var gated, no
Android emulator and no separate Gradle source set, because Android and JVM/Desktop share the
OkHttp Ktor engine.

A sequence of small, independent tasks, same convention as the (now closed)
[testing improvement plan](improvement-plan.md). Each task fits in a **single clean context**: a
session picks the task marked `⬅ NEXT`, does it, runs `finalize`, and stops.

## How to run a task

1. Read the status table below and take the task marked **NEXT**. Never take a `deferred`/gated
   task without asking first — check both the table row and the task's own section for the gate.
2. Read only that task's section, plus the baseline doc if you need the wider picture.
3. Do it. Verify with the task's own `Done when` command — not by eyeballing.
4. **Update the status table** (`✅ done — <date>`, move `⬅ NEXT`) and add a `**Result (<date>):**`
   note to the task's own section — especially anything that differed from the description. End the
   note by naming what comes next (the following task, or "queue is empty").
5. Run the **`finalize` skill** — each task lists a *Finalize focus* hint.
6. **Commit and push** (standing authorization in this project — see CLAUDE.md / memory). Branch
   off `dev` first if starting from `dev`; never push to `dev` directly.

**Every task that adds a test-support helper to this test file or a new shared pattern must update
`.claude/agents/testing.md`'s "Integration test against a live server" entry** — that's how a future
session discovers it exists instead of re-deriving the login/cert-trust flow from scratch.

## Status

| # | Task | Size | Status |
|---|---|---|---|
| 1 | Login integration test (`LoginIntegrationTest`) | S | ✅ done — 2026-08-08 |
| 2 | Shared login helper + `ProjectsApi` read round-trip | S | ✅ done — 2026-08-08 |
| 3 | Read round-trip sweep, one `XApi` module per session | S each | todo — 8 remaining, see Task 3 — ⬅ NEXT |
| 4 | Write round-trip pilot (create + clean up) | S–M | todo |
| 5 | CI-hosted Taiga (investigation option B) | — | ⛔ deferred — gated, do not start without asking |

Sizes: XS = minutes, S = under an hour, M = a focused session.

---

## Task 1 — Login integration test

**Result (2026-08-08):** done. `composeApp/src/jvmTest/kotlin/com/grappim/taigamobile/di/LoginIntegrationTest.kt`
— builds the real Koin graph (same pattern as `KoinGraphTest`), resolves the real `AuthRepository` +
`TrustedCertStorage`, calls `auth()` against gregory's local instance, and mirrors `LoginViewModel`'s
trust-on-first-use retry for a self-signed cert. Gated by `TAIGA_INTEGRATION_URL`/`_USERNAME`/
`_PASSWORD` env vars (skips itself when unset). Verified with a real passing run against
`http://localhost:9000/`. Full write-up in the baseline doc's "Resolved" section.

**Next: Task 2 — shared login helper + read round-trip.**

---

## Task 2 — Shared login helper + read round-trip

**Why:** every further integration test needs an authenticated session first. Right now that logic
(login, catch `UntrustedCertificateNetworkException`, trust cert, retry once) lives inline inside
`LoginIntegrationTest`'s single test method. A second test that duplicates it is the first sign it
should be shared.

**Scope:**
- Extract the "log in against `TAIGA_INTEGRATION_*` env vars, handling a self-signed cert" logic
  from `LoginIntegrationTest` into a small internal helper in the same package (e.g. a
  `LiveTaigaSession` helper or a top-level function) that other integration tests can call to get a
  ready `Koin` instance with a valid session. Keep `LoginIntegrationTest` itself passing unchanged
  after the extraction — it becomes the first caller of the helper, not a special case.
- Add one new test class exercising a real **read**: `ProjectsApi.getProjects(memberId = <id from
  the auth response or TaigaSessionStorage>)` is the leading candidate — needs nothing but an
  authenticated session, no project setup assumptions. Assert the call succeeds (200 / parses),
  not any particular project count or content — the seeded local instance's data isn't something
  this repo controls or should assert on.

**Existing pieces:** the login+cert-trust flow already written in `LoginIntegrationTest` (task 1);
`ProjectsApi`/`ProjectsRepository` already real-vs-fake-swappable like every other `XApi`.

**Result (2026-08-08):** done. Extracted the login+cert-trust flow into
`liveTaigaSessionOrSkip(): Koin?` in a new `LiveTaigaSession.kt` (same `di` package).
`LoginIntegrationTest` now just calls it and returns if `null`. Added
`ProjectsApiIntegrationTest` — resolves `ProjectsApi` + `TaigaSessionStorage` from the returned
`Koin`, calls `getProjects(memberId = sessionStorage.requireUserId())`, asserts the result is
non-null (parsed) without asserting on content.

**Deviation from the plan as written:** the helper does not build a fresh `koinApplication<KoinApp>`
on every call. The JVM `DataStore` backends (`StorageModule.jvm.kt`) read/write fixed files under
`java.io.tmpdir`, so a second `koinApplication` in the same test JVM throws "multiple DataStores
active for the same file" the instant it touches one — hit this running `LoginIntegrationTest` and
`ProjectsApiIntegrationTest` together. Fixed by memoizing the graph-build-and-login behind a
`private val sharedSession: Lazy<Koin>`; `liveTaigaSessionOrSkip()` still does the env-var gate and
env-var-per-call read, but the actual Koin graph and login happen once per test JVM and every
integration test in the run shares that one authenticated session. This means task 3's per-module
tests won't each pay their own login round-trip either — a side benefit, not just a workaround.

**Side effect on `KoinGraphTest`:** when the three env vars are set, running the full `di` package
(`--tests "com.grappim.taigamobile.di.*"`) makes `KoinGraphTest`'s *own* `koinApplication` collide
with the still-open shared session's `DataStore`, adding 2 more "resolved dependencies but threw
while constructing" entries (`LoginViewModel`, `SettingsUserScreenViewModel`) to its already-tolerated
noise (see that test's doc comment — only `NoDefinitionFoundException` fails it). Test still passes;
noted here so a future session doesn't mistake the printed noise for a regression. Confirmed
`./gradlew jvmTest` with no env vars set stays green (both new tests skip cleanly) and `ktlintCheck`
is clean.

**Done when:** with the three `TAIGA_INTEGRATION_*` env vars set,
`./gradlew :composeApp:jvmTest --tests "com.grappim.taigamobile.di.*" --rerun` passes both the
login test and the new read test; without them set, both skip cleanly and `./gradlew jvmTest`
(no env vars) stays green.

**Finalize focus:** update `.claude/agents/testing.md`'s integration-test entry to mention the
shared helper, so task 3's sweep doesn't re-duplicate the login flow on every module.

**Next: Task 3 — read round-trip sweep, one `XApi` module per session.**

---

## Task 3 — Read round-trip sweep, one `XApi` module per session

**Why:** this app has 14 `XApi` interfaces (CLAUDE.md: "Every `XApi` is an `interface XApi` +
`@Single(binds = [XApi::class]) class XApiImpl` — no exceptions"). Login (task 1) covers `AuthApi`;
task 2 covers `ProjectsApi`. The other 12 have never been exercised against a real server. Same
repeatable-task shape as the closed improvement plan's task 9a (missed-branch sweep) and task 11
(Compose UI widget sweep): pick the next module in the list, write one real read round-trip against
it, land it, move to the next session.

**Done (3/12):**

| Module | Test | Notes |
|---|---|---|
| `UsersApi` | `UsersApiIntegrationTest` — `getMyProfile()` | 2026-08-08. Zero-fixture call, needs only the authenticated session. |
| `UserStoriesApi` | `UserStoriesApiIntegrationTest` — `getUserStories(GetUserStoriesParams(project = 5))` | 2026-08-08. Project 5 confirmed to have ~19 user stories; asserts the list parses, not its content. |
| `TasksApi` | `TasksApiIntegrationTest` — `getTasks(project = 5)` | 2026-08-08. Confirmed project 5 has real tasks (e.g. task id 1, ref 31, "Set up authentication middleware") via `taiga-mcp`; asserts the list parses, not its content. |

**Corrected — these two have no read method at all (discovered 2026-08-08 while scoping this
task):**

| Module | Why not | Where the read actually lives |
|---|---|---|
| `EpicsApi` | write-only: `linkToEpic`/`unlinkFromEpic` only, no listing/get | `WorkItemApi.getWorkItems(taskPath = "epics", project = ...)` |
| `IssuesApi` | write-only: `createIssue` only, no listing/get | `WorkItemApi.getWorkItems(taskPath = "issues", project = ...)` |

**Remaining candidates (8, order not fixed — pick whichever has the most obvious real data in the
local instance when you start)**:

| Module | Leading call | Notes |
|---|---|---|
| `SprintApi` | `getSprints(project = 5, isClosed = false)` or `getSprint(sprintId = 4)` | project 5 has sprints/milestones 4/5/6 ("Sprint 1/2/3") confirmed 2026-08-08 |
| `WikiApi` | `getProjectWikiPages(5)` / `getWikiLink(5)` | wiki content in project 5 not yet confirmed |
| `SwimlanesApi` | `getSwimlanes(project = 5)` | simplest single-method interface; confirmed 2026-08-08 project 5 has zero swimlanes configured — still a valid (empty) round-trip |
| `ProjectValuesApi` | `getProjectValues(endpoint = "userstory-statuses", projectId = 5)` | statuses confirmed to exist on project 5 |
| `WorkItemApi` | `getWorkItems(taskPath = "epics", project = 5)` (or `"issues"`) | this is where Epics/Issues reads actually live — see the correction above; project 5 has 10 confirmed epics |
| `HistoryApi` | `getCommonTaskComments(singularTaskPath, id)` | needs a picked entity id (any of project 5's confirmed user stories) — otherwise no fixture needed |
| `FiltersApi` | `getCommonTaskFiltersData(taskPath = "userstories", project = 5)` | single-method interface, no fixture id needed |

**Before each module's task: check what data actually exists in the local instance** (via
`taiga-mcp`'s `taiga_request`, cheaper than guessing) rather than assuming a project/epic/story
exists — the seeded instance's `docs/local-info.md` users don't guarantee any project data. Project 5
("Main project") is confirmed (2026-08-08) to have epics, user stories, and sprints — see the table
above for specifics already checked.

**Existing pieces:** the shared login helper from task 2. Reuse it, don't re-derive the
login/cert-trust flow per module.

**Done when (per module):** with the three `TAIGA_INTEGRATION_*` env vars set, the new test for that
module passes and asserts the call actually succeeded (parses / non-error status) rather than just
"didn't throw"; without the env vars, it skips cleanly. **Verify with `--tests` scoped to the
`*IntegrationTest` classes, not the `di.*` wildcard** — running the wildcard risks the pre-existing,
order-dependent `KoinGraphTest` collision (see
[revisit #24](../revisit.md#24-koingraphtest-and-the-live-taiga-integration-tests-collide-on-the-jvm-datastore-file-order-dependently)),
which is not specific to any one module's test and shouldn't block landing it.

**Finalize focus:** cross off the module in this table (or move it to a "done" list) so the next
session knows which 9/8/7... remain — don't leave the reader to grep for existing test files to
figure out what's left.

**Result (2026-08-08):** `UsersApiIntegrationTest` added (`getMyProfile()`). While scoping the
module list, discovered `EpicsApi` and `IssuesApi` are write-only — corrected the candidates table
above so a future session doesn't waste time looking for a read that isn't there. Also discovered and
logged [revisit #24](../revisit.md#24-koingraphtest-and-the-live-taiga-integration-tests-collide-on-the-jvm-datastore-file-order-dependently):
running the full `com.grappim.taigamobile.di.*` wildcard with the env vars set can fail *every*
live-Taiga test (not just the new one) if `KoinGraphTest` happens to run first in that JVM — verified
by re-running with `--tests` scoped to just the three `*IntegrationTest` classes, which passed
cleanly every time. Not fixed (shared test infra, out of scope for this task). `./gradlew jvmTest` and
`ktlintCheck` both green with no env vars set. 10/12 candidates remain — next session picks any row
from the table above.

**Result (2026-08-08, session 2):** `UserStoriesApiIntegrationTest` added —
`getUserStories(GetUserStoriesParams(project = 5))`, asserts the returned list is non-null (parsed).
Verified with the three `TAIGA_INTEGRATION_*` env vars set, scoped to
`com.grappim.taigamobile.di.*IntegrationTest` — all four integration tests (login, projects, users,
user stories) pass together. Also verified clean skip with no env vars set, full `./gradlew jvmTest`,
and `ktlintCheck`, all green. No new shared helper needed — reused `liveTaigaSessionOrSkip()`
unchanged. 9/12 candidates remain — next session picks any row from the table above.

**Result (2026-08-08, session 3):** `TasksApiIntegrationTest` added — `getTasks(project = 5)`,
asserts the returned list is non-null (parsed). Checked project 5's data via `taiga-mcp` first per
the task's own guidance: confirmed real tasks exist (id 1, ref 31, "Set up authentication
middleware") and swimlanes are empty (noted in the candidates table above for whoever picks that
row next). Verified with the three `TAIGA_INTEGRATION_*` env vars set, scoped to
`com.grappim.taigamobile.di.*IntegrationTest` — all five integration tests (login, projects, users,
user stories, tasks) pass together. `./gradlew jvmTest` (no env vars) failed twice on
`FiltersStorageImplTest.resetFilters clears every section` — confirmed pre-existing and unrelated
by reproducing it on a clean `git stash -u` tree at the last commit, and confirmed it passes in
isolation; logged as [revisit #25](../revisit.md#25-filtersstorageimpltestresetfilters-clears-every-section-is-flaky-under-a-full-jvmtest-run)
rather than investigated further here. `ktlintCheck` green. 8/12 candidates remain — next session
picks any row from the table above.

---

## Task 4 — Write round-trip pilot (create + clean up)

**Why:** login and reads prove auth and GET work; nothing yet proves a real POST round-trips
correctly through the app's own request/response mapping. Deliberately after task 3, not before —
several of task 3's read candidates (e.g. `ProjectValuesApi`'s statuses) are exactly what a write
pilot would need to already know exist on a real project.

**Scope:** pick a write that has a natural, reliable **delete/cleanup counterpart** in the same API,
so the test doesn't leave permanent junk in gregory's seeded instance on every run. `ProjectsApi`'s
tag endpoints (`CreateTagRequestDTO` + `DeleteTagRequestDTO`) are the leading candidate — but this
needs an existing project to attach a tag to. **First step of this task: check what projects/data
actually exist in the local instance right now** (via `taiga-mcp`'s `taiga_request`, or whatever
task 3 already found) before committing to a specific write endpoint.

**Existing pieces:** the shared login helper from task 2; whatever task 3 already learned about what
data exists in the local instance.

**Done when:** the write test passes, creates something, and the test itself deletes/reverts it
before finishing (in a `finally` or equivalent) rather than relying on the next run to clean up.

**Finalize focus:** note in the baseline doc whether cleanup-on-failure (the create succeeds but the
test then fails before deleting) was handled or left as a known gap. If this pilot goes well, it's
the template for a write-sweep counterpart to task 3 — scope that as a new task only once asked.

---

## Task 5 — CI-hosted Taiga (investigation option B)

⛔ **Gated — do not start without asking (see status table).** This is materially bigger than tasks
1–4: a `docker-compose.yml` this repo doesn't have today, a multi-container stack (`taiga-back`,
`taiga-front`, `taiga-events`, `taiga-protected`, RabbitMQ, PostgreSQL), a wait-for-healthy CI step,
and slower CI on every PR if wired into the default workflow. See the baseline doc's "Option B" for
the full tradeoff. Only take this task if explicitly asked to scope it — and scope it as its own
sub-plan rather than a single task, given the size.

---

## Considered and deferred

**Option C (public taiga.io)** — not seriously considered; see the baseline doc. Not tracked as a
task here.

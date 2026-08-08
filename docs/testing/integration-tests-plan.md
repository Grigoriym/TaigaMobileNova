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
| 2 | Shared login helper + read round-trip | S | ⬅ NEXT |
| 3 | Write round-trip (create + clean up) | S–M | todo |
| 4 | CI-hosted Taiga (investigation option B) | — | ⛔ deferred — gated, do not start without asking |

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

**Done when:** with the three `TAIGA_INTEGRATION_*` env vars set,
`./gradlew :composeApp:jvmTest --tests "com.grappim.taigamobile.di.*" --rerun` passes both the
login test and the new read test; without them set, both skip cleanly and `./gradlew jvmTest`
(no env vars) stays green.

**Finalize focus:** update `.claude/agents/testing.md`'s integration-test entry to mention the
shared helper, so task 3 doesn't re-duplicate the login flow a third time.

---

## Task 3 — Write round-trip (create + clean up)

**Why:** login and read prove auth and GET work; nothing yet proves a real POST round-trips
correctly through the app's own request/response mapping.

**Scope:** pick a write that has a natural, reliable **delete/cleanup counterpart** in the same API,
so the test doesn't leave permanent junk in gregory's seeded instance on every run. `ProjectsApi`'s
tag endpoints (`CreateTagRequestDTO` + `DeleteTagRequestDTO`) are the leading candidate — but this
needs an existing project to attach a tag to, which the seeded local instance may or may not have by
the time this task is picked up. **First step of this task: check what projects/data actually exist
in the local instance right now** (via `taiga-mcp`'s `taiga_request`, or the read test from task 2)
before committing to a specific write endpoint — do not assume task 2's candidate data still applies.

**Existing pieces:** the shared login helper from task 2.

**Done when:** the write test passes, creates something, and the test itself deletes/reverts it
before finishing (in a `finally` or equivalent) rather than relying on the next run to clean up.

**Finalize focus:** note in the baseline doc whether cleanup-on-failure (the create succeeds but the
test then fails before deleting) was handled or left as a known gap.

---

## Task 4 — CI-hosted Taiga (investigation option B)

⛔ **Gated — do not start without asking (see status table).** This is materially bigger than tasks
1–3: a `docker-compose.yml` this repo doesn't have today, a multi-container stack (`taiga-back`,
`taiga-front`, `taiga-events`, `taiga-protected`, RabbitMQ, PostgreSQL), a wait-for-healthy CI step,
and slower CI on every PR if wired into the default workflow. See the baseline doc's "Option B" for
the full tradeoff. Only take this task if explicitly asked to scope it — and scope it as its own
sub-plan rather than a single task, given the size.

---

## Considered and deferred

**Option C (public taiga.io)** — not seriously considered; see the baseline doc. Not tracked as a
task here.

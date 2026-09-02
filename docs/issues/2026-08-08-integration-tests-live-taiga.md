# 2026-08-08 — Integration tests against a live Taiga instance: is it worth doing, and can it be Android-only?

**Status:** Resolved — login-only spike built and passing against a real instance (see Resolved
section at the end)
**Link:** [testing deferred list](../testing/deferred.md) — original note: "`tools/seed` and the
local instance in `docs/local-info.md` make it feasible, but it would be a manually-triggered job,
not part of PR CI. No pull for it yet."

## The question

Two parts:
1. Is running tests against a real Taiga backend (not `FakeXApi`, not `MockEngine`) worth building
   now?
2. The prompt that started this: "since Android is the main source, I believe we can test on
   Android only" — does that hold up, and what does "Android only" actually mean for a KMP project
   whose test infra runs on `jvmTest`?

## Finding: "Android only" already exists as a concept here, and it's `jvmTest`, not an emulator

`KmpNetworkConventionPlugin.kt:14-19` wires the Ktor engine per source set:

```kotlin
androidMain.dependencies { implementation(libs.findLibrary("ktor.client.okhttp").get()) }
jvmMain.dependencies { implementation(libs.findLibrary("ktor.client.okhttp").get()) }
iosMain.dependencies { implementation(libs.findLibrary("ktor.client.darwin").get()) }
```

**Android and JVM/Desktop share the OkHttp engine; iOS alone uses Darwin.** So a test that exercises
the real Ktor pipeline (`HostSelectionPlugin`, `AuthHeaderPlugin`, `TokenRefreshPlugin`,
`ErrorMappingPlugin`, `ContentNegotiation`) on the JVM target is, at the engine level, testing
exactly what Android does — not an approximation of it. This matches CLAUDE.md's own precedent
("Testing `expect`/`actual` code: prefer the platform whose actual is real over stubbing one out" —
JVM is a fully supported target, Desktop runs the app for real) and is the same reasoning
`KoinGraphTest` already relies on.

**And the DI graph is already proven buildable on JVM with zero stubbing.**
`composeApp/src/jvmTest/.../KoinGraphTest.kt` constructs the *entire* real Koin graph — 147+
definitions, including the real `HttpClient`s from `KmpNetworkModule`, real `AuthStorage`/
`ServerStorage`/`TrustedCertStorage` actuals (JVM `DataStore`, not a fake) — and resolves every
dependency. It doesn't make network calls today (definitions are only resolved, not exercised), but
it proves the exact object graph an integration test would need is already constructable in
`jvmTest` with no new DI wiring.

**Conclusion: "Android only" is achievable, and cheaply — as a `jvmTest`, reusing the real
`XApiImpl` classes and the real Ktor client, pointed at a real server via `ServerStorage`/
`BaseUrlProvider`.** No emulator, no instrumented-test source set (which this repo has never had, by
design — see CLAUDE.md Testing). This is a materially different, and much lighter, proposal than
"run instrumented tests on an Android device," which is the other thing "Android only" could have
meant — flagging this fork explicitly since it changes the whole shape of the work (see Open
question 1 below).

## What exists to build on

- **`tools/seed`** (`tools/seed/src/main/kotlin/.../TaigaApi.kt`) is a hand-rolled, separate Ktor
  client for seeding data — bearer-token auth, JSON bodies, no relation to `core/api`'s `XApi`
  interfaces. It's a model for "talk to a real Taiga over HTTP from JVM," not reusable
  infrastructure — an integration test would go through the app's own `XApiImpl` classes instead, to
  actually test them.
- **`docs/local-info.md`** documents gregory's local Docker-hosted Taiga at a **local network
  address** (`http://localhost:9000/`, per `docs/local-info.md`), with seeded users
  (`admin`/`user1..3`). This is reachable only
  from gregory's own machine — not from a GitHub Actions runner.
- **No `docker-compose.yml` is checked into this repo.** `local-info.md` only links to Taiga's
  community "30-min setup" guide; the stack (`taiga-back`, `taiga-front`, `taiga-events`,
  `taiga-protected`, RabbitMQ, PostgreSQL, plus nginx) is not something this repo currently knows how
  to stand up itself.
- **Every `XApi` is real-vs-fake-swappable already** (CLAUDE.md: "Every `XApi` is an `interface XApi`
  + `@Single(binds = [XApi::class]) class XApiImpl` — no exceptions"), so nothing needs restructuring
  to point a test at the real impl instead of `FakeXApi`.

## The actual blocker: where does "live" point?

Not capture mechanics (settled above) — **reachability**. Three options:

### A. Point at gregory's local Docker instance

- **Pros:** already running, already seeded, zero new infra.
- **Cons:** only reachable from gregory's machine. Cannot run in CI at all — this is exactly why
  deferred.md already called it "a manually-triggered job, not part of PR CI." A test suite that can
  only run on one person's LAN is closer to a personal script than project infrastructure; it would
  need to be excluded from `jvmTest` (an opt-in Gradle task or a system-property/env-var gate) so it
  never breaks CI or a contributor's `./gradlew jvmTest` run.

### B. Stand up Taiga in CI (GitHub Actions service containers or a docker-compose step)

- **Pros:** runs on every PR if wanted, no dependency on gregory's machine, reproducible.
- **Cons:** Taiga's real stack is multi-container (back, front, events, protected, RabbitMQ,
  Postgres) with a non-trivial boot/migration time — this would be new CI infrastructure (a
  `docker-compose.yml` this repo doesn't have today, plus a wait-for-healthy step), not a config
  tweak. Real ongoing cost: slower CI, another moving part to keep green, and this project's CI
  currently has no service-container jobs of any kind to model it on.

### C. Hit the public taiga.io

- **Cons dominate:** rate limits, dependency on a third party's production service for CI health,
  and running automated write-heavy tests (create project/story/task) against someone else's hosted
  service is the wrong thing to do without explicit arrangement. Not seriously considered further.

## Recommendation

**Start with A, explicitly scoped as local-only and opt-in — do not attempt B in the same pass.**
This matches the deferred.md framing exactly ("a manually-triggered job, not part of PR CI") and
costs the least: reuse the running Docker instance, write the tests as a small number of real
`XApiImpl` round-trips (login via `AuthApi`, one read, one write) in a **separate `jvmTest` source
set or a tagged/gated test class** so `./gradlew jvmTest` never tries to reach a LAN address that
doesn't exist on CI or another contributor's machine. B (CI-hosted Taiga) is a legitimate future
step but is materially more infrastructure and shouldn't be scoped until A has proven the test
pattern is worth keeping.

## Open questions — need a decision before implementing

1. **Confirm the "Android only" framing means "JVM as Android's network-engine proxy" (this doc's
   reading), not literal Android-instrumented tests.** The two are very different projects — the
   latter would be this repo's first-ever Android-target test execution (CLAUDE.md is explicit that
   the absence is by design), needing an emulator and new CI infra, and gets none of the "already
   proven on `jvmTest`" groundwork above for free.
2. **How should the gate work** — a separate Gradle source set (e.g. `jvmIntegrationTest`, needing
   its own task the way `tools/seed`'s `test` task does per CLAUDE.md's Testing section), or a
   regular `jvmTest` class skipped via `Assumptions`/an env-var check (e.g. only runs if
   `TAIGA_INTEGRATION_URL` is set)? The env-var-gate approach is less new Gradle wiring but risks the
   same "silently never runs" trap CLAUDE.md already warns about for `tools/` — needs to be visible,
   not just skippable.
3. **Scope of the first test(s)** — login only, to prove the wiring? Or login + one write + one read
   round-trip through a real `XApiImpl`? Keep it small; this is a spike to validate the approach, not
   the full integration suite.

## Resolved (2026-08-08)

All three open questions above answered, in favor of the lighter option each time:

1. **"Android only" = JVM as Android's network-engine proxy**, confirmed. No emulator, no
   Android-instrumented source set.
2. **Gating: a plain `jvmTest` class with a runtime env-var check, not a separate Gradle source
   set.** The separate-source-set option in the Recommendation above was reconsidered and rejected —
   since the test is JVM-only regardless, a class in the normal `jvmTest` folder that returns
   immediately when `TAIGA_INTEGRATION_URL`/`_USERNAME`/`_PASSWORD` aren't set gets the same "never
   runs on CI or another machine" guarantee with zero new Gradle wiring.
3. **Scope: login only.**

Built `composeApp/src/jvmTest/kotlin/com/grappim/taigamobile/di/LoginIntegrationTest.kt`: builds the
real Koin graph (same pattern as `KoinGraphTest`), resolves the real `AuthRepository` +
`TrustedCertStorage`, calls `auth()`, and mirrors `LoginViewModel`'s own trust-on-first-use retry if
the server presents a self-signed certificate. Verified with a real passing run against gregory's
local instance:

```bash
TAIGA_INTEGRATION_URL=http://localhost:9000 \
TAIGA_INTEGRATION_USERNAME=user1 \
TAIGA_INTEGRATION_PASSWORD=user1 \
./gradlew :composeApp:jvmTest --tests "com.grappim.taigamobile.di.LoginIntegrationTest" --rerun
```

`--rerun` is required: Gradle does not track env vars as task inputs, so re-running the task after
only changing an env var (not source) reports `UP-TO-DATE` and silently skips re-execution instead
of picking up the new value. The first attempt at this verification looked like a pass for exactly
that reason before the mistake was caught.

**Not done:** a read/write round-trip test beyond login. Natural next slice if this is picked up
again, but out of scope for what was asked here.

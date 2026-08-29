# Performance Hardening — Implementation Plan

## Background

Gregory forwarded "Android Performance Tuning: The Art of Making Your App Run on a Potato" (Alan
Bebido, Medium, Jul 29 2026) — a generic four-bottleneck performance listicle (APK size, RAM,
UI jank, battery/thermal). Distinct from the "Android Lead" newsletter series behind
`docs/architecture/vm-lifecycle-hardening/` (different author, different topic — this one is
performance tuning, not ViewModel lifecycle/error handling), so it gets its own plan doc per
CLAUDE.md's Multi-Session Work convention rather than being folded into that differently-scoped
one. The article's individual claims were checked against this codebase rather than taken at face
value, same process as the other newsletter reviews.

Most of the article's specific advice is either already satisfied or doesn't map onto this
project's architecture (KMP + Compose Multiplatform, not single-module Android Views) — see the
declined list in CHECKLIST.md. The one genuinely new, actionable idea came from gregory refining
the article's "quick summary checklist" into a concrete ask: track APK size and startup
performance automatically over time (CI, ideally every PR) instead of only via the manual capture
process `docs/perf/profiling.md` already documents.

## Findings

### 1. CI regression checks for APK size and Perfetto/Macrobenchmark trace metrics (actionable — checklist step 1, investigate first)

Source: the article's "Shrinking the Weight" and "Quick Summary Checklist" sections, refined by
gregory (2026-08-29): "add the checks for apk size, perfetto traces (if applicable) maybe on every
PR or something like that, ... see the degradation."

This is really two different checks with very different feasibility, worth treating separately
rather than as one bundled "perf CI" idea.

#### (a) APK size delta on PRs

**What already exists to build on:** `.github/workflows/build.yml` already runs
`./gradlew :androidApp:assembleFdroidDebug` and `:androidApp:assembleGplayDebug -PgplayBuild` on
every PR into `dev` (the `build` job). Nothing currently measures or compares the resulting APK
size — `build.yml` just asserts the assemble succeeds.

**Debug vs. release accuracy tradeoff:**

- The article itself warns against judging size from a debug build (extra symbol tables,
  uncompressed code, no shrinking). This project's release build type already has both
  `isMinifyEnabled = true` and `isShrinkResources = true`
  (`build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt:63-64`), so a
  release build is the size that actually ships.
- But building a signed release APK needs the release keystore. `build.yml`'s `build` job already
  *declares* `TAIGA_PATH_R` / `TAIGA_KEY_PASS_R` / `TAIGA_STORE_PASS_R` / `TAIGA_ALIAS_R` /
  `ENCODED_STRING_R` in its `env:` block (lines 26-30) but never restores them to a
  `taigamobilenova_keystore_release.jks` file the way `release.yml:40` does for the release
  workflow — only the debug keystore gets restored in `build.yml` today. So release-accurate
  size measurement needs that restore step added, not just a new gradle task.
- Even with that fixed, **fork-originated PRs don't get repository secrets** under a plain
  `pull_request` trigger (only `pull_request_target` gets them, and that trigger has its own
  well-known security caveats for running untrusted code) — so a release-accurate size check would
  only work for same-repo-branch PRs, and any design needs an explicit fallback (skip the check, or
  fall back to the debug-vs-debug delta) for fork PRs rather than silently failing or leaking
  secrets.
- **A debug-vs-base-branch delta is the cheap first cut**: build fdroid debug for both the PR head
  and the PR's merge-base against `dev` (or diff against a stored baseline size from `dev`'s last
  successful build), compare `app-fdroid-debug.apk` sizes, comment the delta on the PR. Doesn't
  reflect the real shipped (R8-shrunk) size, but still catches the class of regression that matters
  most for a fast per-PR signal — someone added a large dependency, asset, or accidentally
  unshrunk resource.

**Tooling:** JakeWharton's `diffuse` (CLI + a `diffuse-action` GitHub Action) is the standard
existing tool for "compare two APK/AAB builds, post a markdown PR comment with size + dex-count +
resource deltas" — worth checking it still supports whatever this project's current AGP version
produces before hand-rolling a `du`-based size comparison script.

**Not started.** No prototype workflow, no confirmation `diffuse` (or an alternative) actually
works against this project's build output. Treat the fork-PR-secrets gap above as a real open
question, not a solved edge case, before committing to the release-accurate design.

#### (b) Perfetto / Macrobenchmark trace metrics on a schedule

**What already exists to build on:** `benchmark/src/main/kotlin/com/grappim/taigamobile/benchmark/BaselineProfileGenerator.kt`
already has one macrobenchmark journey (`coldStart()`, targeting `com.grappim.taigamobile.fdroid`,
documented in `docs/perf/profiling.md`), but it's run manually/on-demand for Baseline Profile
generation today — nothing in `.github/workflows/` touches `:benchmark` at all.

**Why this is a better fit than the manual `docs/perf/profiling.md` process, in principle:**
AndroidX Macrobenchmark (the library `:benchmark` already depends on) is built for exactly this —
a `MacrobenchmarkRule` with `StartupTimingMetric`/`FrameTimingMetric` captures a Perfetto trace
under the hood automatically and emits a structured `*-benchmarkData.json` result per run, without
needing the by-hand `adb shell perfetto` capture + Python `trace_processor` analysis
`docs/perf/profiling.md` documents. That JSON is the natural artifact to diff run-over-run for a
regression signal — this isn't a from-scratch design, it's wiring up infra the project already has
a dependency on but has only used for Baseline Profile generation so far.

**Real blockers, not glossed over:**

- **Login is mandatory, no anonymous path** (`docs/perf/profiling.md`'s own note). A macrobenchmark
  run on a fresh CI emulator starts logged out, but `coldStart()`'s current journey assumes a
  session already exists and lands on "Select Project." Two options, neither investigated yet:
  (i) extend the journey to script a real login every run — slow, and couples CI to a live Taiga
  instance being reachable from the runner; (ii) seed a pre-authenticated session file onto the
  emulator's data directory before the benchmark starts — faster and more deterministic, but needs
  investigating what `core/storage`'s auth persistence actually looks like on disk (DataStore file
  format/location) before it can be faked reliably.
- **Emulator cost and flakiness.** Unlike the size check, this needs a real emulator
  (e.g. `reactivecircus/android-emulator-runner`, KVM-backed) — multi-minute boot + install + run
  per invocation, not a fit for blocking every PR. `docs/perf/profiling.md` also documents real
  software-renderer artifacts on the AVD (`swiftshader_indirect` histogram-overflow buckets,
  `Buffer Stuffing` jank-type noise) that a naive single-run CI gate would need to account for or
  it will false-positive on renderer noise, not real regressions.

**Refined recommendation:** don't gate every PR on this. Run it on a schedule (nightly, or
manual-dispatch) against `dev`, track the metric JSON over time (workflow artifact, or a small
append-only log committed somewhere), and alert only on a sustained trend or threshold breach, not
a single noisy run. Every-PR gating was the "maybe" gregory floated when raising the idea; the
emulator cost and the login blocker are why this refinement downgrades it to scheduled rather than
per-PR.

**Not started.** This needs its own smaller investigation (what does session seeding actually look
like, is `reactivecircus/android-emulator-runner` viable on this repo's GitHub-hosted runners,
what's a sane threshold/trend-detection rule) before it's a committable step rather than a design
sketch.

## Candidate for agentic-grappim (project-agnostic, not TaigaMobileNova-specific)

Not yet written up — flagging for later: "AndroidX Macrobenchmark's structured JSON output is a
better CI regression-tracking primitive than a hand-rolled Perfetto capture + trace_processor
script" is general Android knowledge, not specific to this codebase's architecture. Worth a shared
note in `agentic-grappim` next time it's touched, once this project's own investigation above
actually confirms the approach works end-to-end (don't write up a pattern that hasn't been proven
here yet).

# Perf profiling (Android) — plan

**Status: closed (2026-08-12)** — all 3 tasks done. `docs/perf/profiling.md` is the current
"run it again" reference; this file is kept only as historical record of how it was built.

WallosMobile (a sibling project, `~/proj/grappim/wallosmobile/docs/PERF_PROFILING.md`) has a working
perf-profiling setup: `dumpsys gfxinfo`/Perfetto for finding and diagnosing jank, plus a `:benchmark`
Baseline Profile module + `androidx.profileinstaller` so cold-navigation JIT warm-up findings can
actually be fixed, not just measured. This plan stands up the same two tools here.

TaigaMobileNova has **no profiling infra today** — no `docs/perf/`, no
`androidx.benchmark`/`androidx.baselineprofile`/`androidx.profileinstaller` in the version catalog,
no `:benchmark` module, no existing jank/perf issue docs. This is greenfield setup, not a bug fix —
the goal is the same two tools, verified against this project's own AGP/flavor/module layout, with
one real end-to-end pass through each before writing anything up as fact (WallosMobile's numbers
don't transfer; only its technique does).

The doc pair follows this repo's convention for multi-session work (see
`docs/compose/stability-reports-plan.md` for the shape): this file is the plan/history,
`docs/perf/profiling.md` is the "run it again" reference — written once there's real output to
describe (task 2).

## Status

| # | Task | Size | Status |
|---|------|------|--------|
| 1 | Gradle wiring: `:benchmark` module + `profileinstaller` | S | Done (2026-08-12) |
| 2 | `docs/perf/profiling.md` — gfxinfo/Perfetto technique + one real capture | M | Done (2026-08-12) |
| 3 | Baseline Profile generator + verify it's actually applied | M | Done (2026-08-12) |

## Researched facts (so later tasks don't re-derive them)

- **AGP is `9.3.1`** (`gradle/libs.versions.toml`) — the exact version WallosMobile's doc names as
  incompatible with `androidx.benchmark`/`androidx.baselineprofile` `1.4.1` (`Module :app is not a
  supported android module`). `1.5.0-beta01` is confirmed to work there; re-verify it configures here
  too before writing any `@Test` (task 1's job) rather than assuming the fix carries over.
- **Flavors already match WallosMobile's worked example almost exactly** — `FlavorDimensions.STORE`
  with `AppFlavors.GPLAY` ("gplay") / `AppFlavors.FDROID` ("fdroid", `applicationIdSuffix = ".fdroid"`)
  in `build-logic/convention/src/main/kotlin/com/grappim/taigamobile.buildlogic/AppFlavors.kt`. The
  `:benchmark` module's `flavorDimensions += "STORE"` / `productFlavors { gplay {}; fdroid {} }` block
  can be copied near-verbatim from WallosMobile's template.
- **`gplay` needs `-PgplayBuild`** to pull in Firebase/Crashlytics (`androidApp/build.gradle.kts`);
  `fdroid` doesn't. Target the `:benchmark` module at `fdroid` first to avoid that entanglement —
  `generateFdroidReleaseBaselineProfile`, not the gplay variant.
- **App module is `:androidApp`**, not `:app` — `targetProjectPath = ":androidApp"`, namespace
  `com.grappim.taigamobile.benchmark`, package id `com.grappim.taigamobile` (`app-pkg` in the catalog).
- **`compileSdk`/`minSdk`/`targetSdk` = `37`/`24`/`37`** (`gradle/libs.versions.toml`). Google's own
  Baseline Profile template gives the benchmark module a *higher* `minSdk` (typically 28) than the
  target app's, independent of the app's own floor — use that instead of blindly inheriting 24 if
  `BaselineProfileRule` complains; the connected AVD (see below) is API 36 either way so this only
  affects what the module *declares*, not what it runs against.
- **`configureLinting()` (`build-logic/convention/.../Quality.kt`) is not callable from a plain
  module script** — same gotcha WallosMobile hit. `:benchmark` is a bare `com.android.test` module
  (no convention plugin), so its `detekt {}`/`ktlint {}` blocks need to be written by hand, and
  `config/detekt/detekt.yml` **does** have a `Compose:` ruleset section (line 855) — confirmed via
  `grep`, so the `composeRules-detekt`/`composeRules-ktlint` catalog deps must be added to the
  benchmark module even though it has no Compose code of its own, or lint fails module-wide.
- **Root `build.gradle.kts` plugin-dedup block** (top of file) needs `com.android.test` and
  `androidx.baselineprofile` added as `apply false`, next to the existing `android.application` entry
  — same reason as WallosMobile: any subproject applying `com.android.application` already loads
  every AGP plugin class onto the shared classloader, so a second versioned request from the
  benchmark module's own `plugins {}` block fails as "already on the classpath with an unknown
  version" without this.
- **No `androidx-test`/`androidx-benchmark` entries exist yet** in `gradle/libs.versions.toml` —
  confirmed via grep. All new: `androidxBenchmark` version, `androidx-benchmark-macro-junit4`,
  `androidx-baselineprofile` (plugin id), `android-test` (plugin id, `com.android.test`),
  `androidx-test-ext-junit`, `androidx-test-uiautomator`, `profileinstaller`.
- **Emulator already available for both tasks 2 and 3**: AVD `Medium_Phone_API_36.1` (confirmed via
  `emulator -list-avds`). No device was booted at plan time — boot it fresh per the `emulator-testing`
  skill rather than assuming one is already running. This project has no `docs/EMULATOR_TESTING.md`
  yet (the skill creates one on first real use) — task 2 will be the first time it's needed here.
- **`docs/revisit.md` already flags a real perf-shaped issue** (line ~220): `feature/projects/data`'s
  kanban load performs three reads where one would do. Not in scope to fix here, but it's a
  reasonable first target for task 2's "one real capture" if the emulator work needs a concrete
  screen to point at rather than picking one arbitrarily — confirm with `gfxinfo`/Perfetto whether it
  actually shows up as user-visible jank before deciding to act on it (separately, via `docs/revisit.md`
  or a new task, not folded into this plan).

## Task 1 — Gradle wiring: `:benchmark` module + `profileinstaller`

**Size:** S

**What:**
1. `gradle/libs.versions.toml`: add `androidxBenchmark = "1.5.0-beta01"`, `profileinstaller = "1.4.1"`
   (confirm current latest isn't newer at implementation time — WallosMobile pinned this by hand, not
   from a version-bump tool), plus the library/plugin catalog entries listed in Researched Facts.
2. Root `build.gradle.kts`: add `alias(libs.plugins.android.test) apply false` and
   `alias(libs.plugins.androidx.baselineprofile) apply false` to the plugin-dedup block.
3. New `benchmark/build.gradle.kts` — `com.android.test` + `androidx.baselineprofile` +
   hand-written `detekt {}`/`ktlint {}` (mirror `Quality.kt`'s config values: `buildUponDefaultConfig`,
   `config/detekt/detekt.yml`, ktlint version `1.8.0`, `composeRules-*` deps), `targetProjectPath =
   ":androidApp"`, `flavorDimensions`/`productFlavors` copied from `AppFlavors.kt`,
   `baselineProfile { useConnectedDevices = true }`, deps on `androidx-test-ext-junit`,
   `androidx-test-uiautomator`, `androidx-benchmark-macro-junit4`.
4. `settings.gradle.kts`: `include(":benchmark")`.
5. `androidApp/build.gradle.kts`: add `implementation(libs.androidx.profileinstaller)` (unconditional,
   both flavors — WallosMobile's finding was this matters regardless of Play Store distribution).

**Done when:** `./gradlew :benchmark:tasks` (or `:benchmark:help`) succeeds — this alone catches the
AGP-9-compat "not a supported android module" failure mode without needing a connected device.
`./gradlew :androidApp:assembleFdroidDebug` still builds clean with `profileinstaller` added.
`./gradlew ktlintCheck detekt` passes for the new module.

**Finalize focus:** record whether `1.5.0-beta01` actually configured against AGP `9.3.1` here
(confirm or correct the "Researched facts" assumption above) and whether a newer stable
`androidx.benchmark` release existed at implementation time.

**Result:** `1.5.0-beta01` is confirmed still the correct pin — it's genuinely the latest release on
Google's Maven (checked `maven-metadata.xml` directly at `dl.google.com`, last updated 2026-07-29; no
`1.5.0-rc01`/stable exists yet). Note for future sessions: a first pass tried `1.5.0-rc01` based on a
`WebFetch`-summarized read of `developer.android.com/jetpack/androidx/releases/benchmark`, which
claimed a "1.5.0-rc01, released August 12, 2026" (today's date, exactly — a hallucination tell) and
failed at `:benchmark:tasks` with "Plugin ... was not found". **Don't trust a WebFetch summary of an
androidx release-notes page for an exact version string — read `maven-metadata.xml` from
`dl.google.com`/`plugins.gradle.org` directly instead.** `profileinstaller` (`1.4.1`),
`androidx-test-ext-junit` (`1.3.0`), and `androidx-test-uiautomator` (`2.4.0`) were all cross-checked
the same way and were correct as fetched. All four "Done when" commands passed:
`:benchmark:tasks`, `:androidApp:assembleFdroidDebug`, `:benchmark:ktlintCheck`, `:benchmark:detekt`
(the last two `NO-SOURCE`/no findings — module has no Kotlin source yet, expected until task 3).
Committed and pushed. **Next: task 2** — `docs/perf/profiling.md`, needs the `emulator-testing` skill
to boot `Medium_Phone_API_36.1` and run one real gfxinfo/Perfetto capture.

## Task 2 — `docs/perf/profiling.md`: gfxinfo/Perfetto technique + one real capture

**Size:** M

**What:** Port WallosMobile's technique sections (gfxinfo quick-look, Perfetto capture + analysis,
the schema gotchas — main-thread tid-not-name, `thread_state` S-vs-Running) into
`docs/perf/profiling.md`, adapted to this project's package id (`com.grappim.taigamobile`, or
`.fdroid`-suffixed for the fdroid flavor build) and gradlew invocations
(`./gradlew :androidApp:assembleFdroidDebug`, no `-PgplayBuild` needed).

Boot the `Medium_Phone_API_36.1` AVD (via the `emulator-testing` skill — this creates
`docs/EMULATOR_TESTING.md` for the project as a side effect if it follows its usual pattern), install
the fdroid debug build, and run one real `dumpsys gfxinfo`/`framestats` capture against a concrete
screen (candidate: the kanban board flagged in `docs/revisit.md`, or app cold start if kanban needs
login/project setup that complicates a first pass) to confirm the whole adb round-trip actually works
on this project before writing it up as fact. Only claim what was actually observed — don't restate
WallosMobile's numbers as if they apply here.

Perfetto capture + `TraceProcessor` analysis: attempt the same capture/pull/query flow once; if it
silently produces only kernel-level slices (WallosMobile's real-device gotcha), note whether that
reproduces on the emulator or not rather than assuming either way.

**Done when:** the doc's own commands, run against this project, produce real output (a `framestats`
table with non-garbage `FrameCompleted`/`SwapBuffersCompleted` values, and/or a non-empty Perfetto
trace queryable via `TraceProcessor`).

**Finalize focus:** if the capture surfaced a real, concrete jank finding, log it in `docs/revisit.md`
with the evidence (not fixed inline, not left only in this doc) rather than folding a fix into this
plan.

**Result:** the kanban-board candidate named in Researched Facts above was dropped in favor of the
app's own cold start — login turned out to be genuinely mandatory (no anonymous path), and the
session was confirmed to persist across a full `am force-stop`, making `force-stop` → `am start` →
land on "Select Project" a clean, repeatable journey without extra in-app navigation to set up. Both
tools were run for real against the fdroid debug build on `Medium_Phone_API_36.1`:
`dumpsys gfxinfo` gave 73 frames / 9 janky (12.33%) / 50th percentile 29ms, and reproduced the
stale-trailing-row ring-buffer gotcha (`FrameCompleted=0` with a populated `SwapBuffersCompleted` on
the same row) plus a new one not in the original WallosMobile notes — a `4950ms` GPU percentile that
was a histogram-overflow-bucket artifact, not a real duration (this AVD runs
`-gpu swiftshader_indirect`, a software renderer). Perfetto captured a 10.25MB trace and, queried via
`TraceProcessor` (venv + `pip install perfetto`, no network-fetch or pandas issues hit), reproduced
the main-thread `comm`-truncation gotcha (`tid`'s name showed as `le.fdroid.debug`, not `main`) and
surfaced a real finding: the worst frame (288.8ms) was dominated by ART `VerifyClass` slices for
Compose/androidx and one app class, logged as `docs/revisit.md` #38 as concrete before-evidence for
task 3. Full detail and real command output in `docs/perf/profiling.md`, published this session.
`docs/EMULATOR_TESTING.md` was created as a side effect (didn't exist before). **Next: task 3.**

## Task 3 — Baseline Profile generator + verify it's applied

**Size:** M

**What:** `benchmark/src/main/kotlin/.../BaselineProfileGenerator.kt` with at minimum a `coldStart()`
`@Test` (`pressHome()` + `startActivityAndWait()`); add a second journey only if task 2's capture
found a concrete JIT-cold navigation worth targeting. Watch for WallosMobile's three confirmed
generator gotchas: encrypted-credential seeding doesn't survive a DataStore-planting trick (log in by
hand on the AVD first if the journey needs auth), the process is killed but app data isn't cleared
between `@Test`s within one invocation (but is wiped between separate `connectedFdroidDebugAndroidTest`
runs — re-login each time), and any journey that waits on async-resolved UI needs an explicit
`device.wait(Until.hasObject(...))`, not just `startActivityAndWait()`.

Run `./gradlew :benchmark:generateFdroidReleaseBaselineProfile` as a background task (it's a real
`connectedAndroidTest` run, can take several minutes). Confirm
`androidApp/src/fdroidRelease/generated/baselineProfiles/baseline-prof.txt` lands and gets committed
as source.

**Done when — verify it's actually applied**, the step WallosMobile's own investigation found
necessary, not optional: `adb shell dumpsys package com.grappim.taigamobile.fdroid | grep status`
after a plain `adb install` of the release build. If it reads `[status=verify]` rather than
`[status=speed-profile]`, force it (`adb shell cmd package bg-dexopt-job`) and re-check, confirming
`profileinstaller`'s startup initializer is what closes the gap — same mechanism WallosMobile
confirmed, verified fresh here rather than assumed to carry over.

**Finalize focus:** add a one-line CLAUDE.md pointer under a new "Performance" heading (mirroring the
existing `docs/compose/stability-reports.md` pointer under Compose/Platform Rules) once this doc pair
is real. Close this plan doc (status banner + Considered/deferred split if anything was deferred)
once the table above is all Done.

**Result:** both parts of "What" and "Done when" confirmed for real, commands and output logged in
`docs/perf/profiling.md`'s new "Baseline Profile" section:

- **Generator:** `benchmark/.../BaselineProfileGenerator.kt` has one `coldStart()` test only — task
  2's `VerifyClass` finding (`docs/revisit.md` #38) was startup-wide, not tied to a specific
  post-login screen, so there was no concrete second journey to add per the "What" section's own
  condition for one.
- **Gap found and fixed:** task 1 wired `:benchmark` (the producer) but never applied the
  `androidx.baselineprofile` *consumer* plugin to `androidApp`, so `generate<Variant>BaselineProfile`
  didn't exist there and nothing would have consumed the generated profile. Added
  `alias(libs.plugins.androidx.baselineprofile)` + `baselineProfile(projects.benchmark)` to
  `androidApp/build.gradle.kts` — confirmed via `:androidApp:tasks` that this is what exposes
  `generateFdroidReleaseBaselineProfile`/`installFdroidNonMinifiedRelease` (as `:androidApp` tasks,
  not `:benchmark` ones, despite the `@Test` living in `:benchmark`).
- **AVD storage gotcha (new, not in WallosMobile's notes):** `./gradlew
  :androidApp:generateFdroidReleaseBaselineProfile` is a real `connectedAndroidTest` run and failed
  once with `IOException: Requested internal only, but not enough space` — the AVD's default 6G data
  partition hit 93% just from debug + release + nonMinifiedRelease + test APKs coexisting. Fixed by
  killing the emulator and relaunching with `-wipe-data -partition-size 12288`; editing
  `disk.dataPartition.size` in `config.ini` alone does not resize an already-created
  `userdata-qemu.img` without a wipe. Re-ran clean afterward: `androidApp/src/fdroidRelease/generated/
  baselineProfiles/baseline-prof.txt` landed with 31,501 real class/method entries.
- **Verify it's applied — all four checks run for real** against the `fdroidRelease` build
  (2026-08-12), matching the mechanism WallosMobile's own investigation found necessary: a plain `adb
  install` read `[status=verify] [reason=install]`; a first launch logged `ProfileInstaller:
  Installing profile for com.grappim.taigamobile.fdroid` (confirming `ProfileInstallerInitializer`
  fired via `androidx.startup`); forcing the real system mechanism (`adb shell cmd package
  bg-dexopt-job`, not a synthetic override) flipped it to `[status=speed-profile]
  [reason=bg-dexopt]`.
- **Left open:** `docs/revisit.md` #38 asked for a re-capture to see whether the `VerifyClass` run in
  the worst cold-start frame shrinks post-profile. A same-APK A/B (`adb shell cmd package compile -m
  verify -f` vs `-m speed-profile -f`) was started this session but the "before" capture landed on
  the Login screen instead of Select Project — reinstalling over the `nonMinifiedRelease` build (a
  different build type/signing) during setup silently dropped the persisted session, and a 4-frame
  capture on the Login screen isn't a fair comparison against task 2's 73-frame Select-Project
  capture. Not repeated this session (time-boxed); `docs/revisit.md` #38 updated with this note and
  the exact commands to redo it properly (re-login before capturing "before").

All Gradle-side "Done when" commands were also re-verified after the plugin-wiring fix: `:benchmark:
ktlintCheck`/`:benchmark:detekt` still pass (no findings), and `:androidApp:tasks` cleanly lists the
new baseline-profile tasks with no configuration errors.

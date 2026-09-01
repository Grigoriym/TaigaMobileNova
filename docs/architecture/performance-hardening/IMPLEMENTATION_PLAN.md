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

**Tooling — confirmed working (2026-08-29).** Downloaded `diffuse` 0.3.0 (JakeWharton/diffuse's
release binary — the core CLI's last release is Feb 2024, effectively unmaintained upstream but
stable) and ran it directly against this project's own build output:
`diffuse diff androidApp/build/outputs/apk/fdroid/debug/app-fdroid-debug.apk
androidApp/build/outputs/apk/gplay/debug/app-gplay-debug.apk`. It parsed both APKs (AGP 9.3.1
output, V2 signature scheme) without error and produced the expected dex/arsc/manifest/resource
size-delta tables plus a per-file diff — confirms the core tool is compatible with this project's
current AGP version, the open question the checklist step flagged.

For the GitHub Action wrapper: JakeWharton never published one himself. `usefulness/diffuse-action`
is the actively-maintained community wrapper (pushed 2026-08-25, not archived) — it takes
`old-file-path`/`new-file-path` and an optional `lib-version` override (defaults to latest
`diffuse` release), and only exposes the diff as an action *output*; it does not post a PR comment
itself, so it'd be paired with `peter-evans/create-or-update-comment` (or similar) to actually
comment the delta on the PR, matching the "post a markdown PR comment" shape from the original ask.

**Still not started:** no prototype `build.yml` job. The fork-PR-secrets gap above is still a real
open question, not a solved edge case, before committing to the release-accurate design — the
debug-vs-base-branch delta doesn't need it and is the recommended first cut.

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
  session already exists and lands on "Select Project." Two options:
  (i) extend the journey to script a real login every run — slow, and couples CI to a live Taiga
  instance being reachable from the runner; (ii) seed a pre-authenticated session file onto the
  emulator's data directory before the benchmark starts.
  **Option (ii) investigated (2026-08-29) — more feasible than it looked, with a caveat.** The
  Android token store (`core/storage/src/androidMain/.../AndroidKeystoreTokenCipher.kt`) encrypts
  `token`/`refresh_token` with an AES/GCM key generated inside `AndroidKeyStore` — not something a
  CI script can precompute or transplant onto a fresh emulator. But `AuthStorageImpl`
  (`core/storage/src/commonMain/.../AuthStorage.kt:23-28`) decrypts via `tokenCipher.decrypt(...)`,
  and `AndroidKeystoreTokenCipher.decrypt()` has a deliberate legacy fallback: **any stored value
  that doesn't start with the `"v1:"` ciphertext prefix is passed through unchanged** (it exists to
  migrate values written before the cipher existed). So a value written *without* that prefix is
  read back as plaintext with no decryption attempted — the AndroidKeyStore key is never in the
  loop. That means seeding doesn't require touching the Keystore at all: write plain (unprefixed)
  token/refresh-token strings into the underlying store and `AuthStorageImpl.isLoggedIn` /
  `getToken()` will accept them as-is.
  The store itself is Preferences DataStore, file at
  `context.preferencesDataStoreFile("auth_storage")` →
  `/data/data/<applicationId>/files/datastore/auth_storage.preferences_pb`
  (`StorageModule.android.kt:67-77`, constant in `StorageModule.kt:15`) — a binary protobuf, not a
  flat key=value file, so it can't be hand-edited byte-for-byte. The safe way to produce a valid
  one is to call the real `PreferenceDataStoreFactory`/`edit{}` APIs (e.g. from a tiny
  instrumentation or `adb shell run-as`-scoped setup step run once against a debuggable build)
  rather than crafting protobuf bytes by hand, then let the emulator boot the real app against that
  seeded file. **Not yet prototyped end-to-end** — this is confirmed from reading the cipher/store
  code, not from an actual emulator run — and it still needs the target build variant to be
  debuggable enough for `run-as` (or `adb root`) to reach app-private storage, which the
  `benchmark` module's `nonMinifiedRelease` build type may not be by default; that's the next thing
  to check if this path is picked up.
- **Emulator cost and flakiness.** Unlike the size check, this needs a real emulator
  (e.g. `reactivecircus/android-emulator-runner`, KVM-backed) — multi-minute boot + install + run
  per invocation, not a fit for blocking every PR. GitHub-hosted `ubuntu-latest` runners do support
  KVM-backed hardware acceleration for this action, gated behind an extra step that adds the
  runner's user to the KVM udev group before the emulator boots — confirmed viable in principle
  (not yet tried against this repo's runners). `docs/perf/profiling.md` also documents real
  software-renderer artifacts on the AVD (`swiftshader_indirect` histogram-overflow buckets,
  `Buffer Stuffing` jank-type noise) that a naive single-run CI gate would need to account for or
  it will false-positive on renderer noise, not real regressions.

**Refined recommendation:** don't gate every PR on this. Run it on a schedule (nightly, or
manual-dispatch) against `dev`, track the metric JSON over time (workflow artifact, or a small
append-only log committed somewhere), and alert only on a sustained trend or threshold breach, not
a single noisy run. Every-PR gating was the "maybe" gregory floated when raising the idea; the
emulator cost and the login blocker are why this refinement downgrades it to scheduled rather than
per-PR.

**Still not implemented.** The session-seeding mechanism and the emulator-runner path are now
sketched (above) rather than unknowns, but neither has been prototyped end-to-end, and a sane
threshold/trend-detection rule for the tracked JSON is still undecided — this remains a design, not
a committable step.

## Written up in agentic-grappim (project-agnostic, not TaigaMobileNova-specific)

**Done 2026-08-30**, though explicitly marked unconfirmed there per its own caveat below: "AndroidX
Macrobenchmark's structured JSON output is a better CI regression-tracking primitive than a
hand-rolled Perfetto capture + trace_processor script" is now in the shared `mobile-patterns` skill
(`agentic-grappim/skills/mobile-patterns/SKILL.md`), flagged as *not yet proven end-to-end* — this
project's own step 3 (macrobenchmark CI, still queued) is what would confirm or correct it. Update
that skill entry once step 3 actually lands, rather than leaving it as a permanent caveat.

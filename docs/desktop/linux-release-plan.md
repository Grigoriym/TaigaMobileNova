# Desktop Linux release: implementation plan

**Created:** 2026-08-09
**Baseline:** [survey.md](survey.md) — what the desktop target looks like before this plan started.

A sequence of small, independent tasks. Each one is sized to fit in a **single clean context**: a
session picks exactly one task, does it, runs `finalize`, and stops. Nothing here requires holding
two tasks in your head at once.

## How to run a task

1. Read the status table below and take the task marked **NEXT**. (If none is marked, take the first
   `todo`.) Never take a `deferred`/gated task without asking — check the table row and the task's own
   heading for a gate before starting.
   **Before assuming the NEXT task is actually undone, run `git status`/`git diff`.** A session can
   finish the code and even the task's own Result note, then get cut off before the status-table
   update, `finalize`, and commit — treat matching uncommitted changes as a finished task waiting on
   bookkeeping, not as a task to redo.
2. Read only that task's section, plus [survey.md](survey.md) if you need the wider picture.
3. Do it. Verify with the task's own `Done when` commands — not by eyeballing.
4. **Update the status table**: set this task to `✅ done — <date>`, move `⬅ NEXT` to the task that
   follows. Add a `**Result (<date>):**` note to the task's own section saying what actually
   happened — especially anything that differed from the description. **End the Result note by naming
   what comes next** — the next task number and name, or "queue is empty" if nothing is scoped. Say
   this in prose; gregory reads the Result note, not the table, to tell whether there's more to do.
5. Run the **`finalize` skill**. Each task lists a *Finalize focus* hint.
6. **Commit and push** — same standing rule as `docs/testing/improvement-plan.md`: don't ask, don't
   stop after finalize waiting to be told. Never commit a red build, never push to `dev` directly for
   these changes (open a PR the normal way if that's this repo's flow for `dev`), ask before anything
   beyond commit+push.

**Do not batch tasks.** If a task turns out bigger than described, split it further and update this
file rather than pushing through.

## Status

| # | Task | Size | Status |
|---|---|---|---|
| 0 | Fix the broken icon path | XS | ✅ done — 2026-08-09 |
| 1 | Move desktop storage off `java.io.tmpdir` | S | ⬅ NEXT |
| 2 | CI job: build the Linux package on PRs | S | todo |
| 3 | Wire the `.deb` into the release workflow | M | todo |
| 4 | Add `Rpm` as a second target format | XS | deferred — ask first |
| 5 | Install a real logger backend on desktop | S | deferred — ask first |
| 6 | Update README once Linux is actually distributed | XS | todo (do last, after 3) |

Sizes: XS = minutes, S = under an hour, M = a focused session.

**Scope decision (2026-08-09):** tasks 0–3 and 6 are the straight-line path to "a Linux `.deb` a
user can actually download and run correctly" and are in scope to work straight through. Tasks 4–5
are real improvements but not required for a first release — **gated on asking first**, same
convention `docs/testing/improvement-plan.md` uses for its own gated items. Task 6 depends on 3
landing (no point documenting a distribution channel that doesn't exist yet) but is otherwise trivial
and does not need to wait for 4–5.

Task ordering rationale: 0 unblocks everything else (nothing downstream can be verified against a
build that doesn't produce a package). 1 is the correctness bug that matters most to a real user and
has no dependency on 0 having landed, but is listed second because it's less urgent than *nothing
packages at all*. 2 depends on 0 (the CI job would fail on the same icon bug) and should land before
3 (proving the package builds in CI before wiring it into a release is cheaper to debug). 3 depends
on 0–2.

---

## Task 0 — Fix the broken icon path

**Why:** `composeApp/build.gradle.kts`'s `nativeDistributions` block points all three platforms'
`iconFile` at `../info/art/...`; the real directory is `../art/...` (no `info/`). Confirmed by
actually running `./gradlew :composeApp:packageDistributionForCurrentOS`, which fails
`:composeApp:packageDeb` on Gradle's task-input validation (`Input file does not exist`). See
[survey.md](survey.md#packaging-config) for the full command output and the `git log -S` that traces
the bug back to `0d4f8ccf` (PR #221) — it has never worked.

**Scope:** three one-line edits in `composeApp/build.gradle.kts`:
```kotlin
linux { iconFile.set(project.file("../art/taiga-mobile-logo.png")) }
windows { iconFile.set(project.file("../art/taiga-mobile-logo.ico")) }
macOS { iconFile.set(project.file("../art/taiga-mobile-logo.icns")) }
```
Nothing else in the file changes. Don't touch `packageDeb`/`packageDmg`/`packageMsi` task
configuration beyond the icon path — this is a path fix, not a packaging redesign.

**Done when:** `./gradlew :composeApp:packageDistributionForCurrentOS` completes successfully and
produces a `.deb` under `composeApp/build/compose/binaries/main/deb/`. Actually run it — this is the
exact command the survey used to prove the bug; use it to prove the fix, don't just eyeball the diff.

**Finalize focus:** low — this is a pure bug fix with an obvious verification command. Worth noting
in the finalize pass only if the produced `.deb` has anything else visibly wrong (bad icon rendering,
wrong menu category) since this task is the first time anyone has looked at the output at all.

**Result (2026-08-09):** Three one-line edits applied exactly as scoped. Verified with
`./gradlew :composeApp:packageDistributionForCurrentOS` — `packageDeb` succeeded, producing
`composeApp/build/compose/binaries/main/deb/taigamobile_2.1.5_amd64.deb` (`packageDmg`/`packageMsi`
skipped, as expected on Linux). `dpkg -I` confirms sane metadata (package `taigamobile`, version
`2.1.5`, correct maintainer/deps). Extracted the `.deb` and confirmed the embedded icon
(`opt/taigamobile/lib/TaigaMobile.png`) is a real 1024×1024 RGBA PNG, not empty/placeholder — nothing
else visibly wrong. Next: task 1, moving desktop storage off `java.io.tmpdir`.

---

## Task 1 — Move desktop storage off `java.io.tmpdir`

**Why:** every JVM-actual persistent store (Room DB, four DataStores, server URL) resolves its file
path under `java.io.tmpdir`, which is not durable on Linux — see
[survey.md](survey.md#storage--the-bigger-problem) for the full table and reasoning. A shipped build
would lose logins and local data unpredictably. The iOS actuals already show the right shape
(`NSDocumentDirectory`); this task gives the JVM actuals the equivalent.

**Files to change** (all `src/jvmMain`, none of this touches `jvmTest` — the test-only use of
`java.io.tmpdir` in `:testing/.../PlatformTestUtils.kt` and
`composeApp/src/jvmTest/.../LiveTaigaSession.kt` is correct and out of scope):

- `core/storage/src/jvmMain/kotlin/com/grappim/taigamobile/core/storage/di/DBModule.jvm.kt`
- `core/storage/src/jvmMain/kotlin/com/grappim/taigamobile/core/storage/di/StorageModule.jvm.kt`
  (four `create*DataStore` functions)
- `core/storage/src/jvmMain/kotlin/com/grappim/taigamobile/core/storage/server/ServerStorageImpl.jvm.kt`

**Approach:** resolve a per-user application-support directory instead of the temp dir. The standard
JVM-only way (no extra dependency) is the platform-appropriate env var / system property:
- Linux: `${XDG_DATA_HOME:-~/.local/share}/TaigaMobile`
- macOS (if ever exercised via this same JVM code path — desktop is currently only being shipped for
  Linux, but the JVM actual is shared): `~/Library/Application Support/TaigaMobile`
- Windows: `%APPDATA%\TaigaMobile`

Write one small internal helper (e.g. `appDataDir(): File` in `core/storage`'s `jvmMain`) that picks
the right base dir per `os.name`, creates it if missing (`File.mkdirs()`), and returns it — then point
all six file-producing lambdas at `File(appDataDir(), <existing file name constant>).absolutePath`
instead of `File(System.getProperty("java.io.tmpdir"), ...)`. Keep the existing file name constants
(`AUTH_DATA_STORE_FILE_NAME` etc.) unchanged — only the base directory moves.

Since the goal stated to the user is Linux specifically, it's fine to implement only the Linux branch
correctly and use a sane same-shape fallback for the other two `os.name` cases rather than researching
macOS/Windows conventions in depth — note in the Result note which branches were verified vs.
best-effort.

**Migration note:** no migration path is needed. Nothing has ever been packaged and shipped, so there
is no existing user data anywhere to preserve — this is closing a bug in unreleased code, not a schema
migration.

**Done when:** running `./gradlew :composeApp:run`, logging in, closing the app, and re-running
`:composeApp:run` shows the session is still active (no re-login prompt) — the concrete behavior the
bug currently breaks. Also confirm the new files land where expected
(`ls ~/.local/share/TaigaMobile/` after a run, on Linux). `./gradlew jvmTest` must stay green — the
existing `jvmTest`/integration tests use their own `java.io.tmpdir`-based paths (`PlatformTestUtils.kt`,
`LiveTaigaSession.kt`) and must be unaffected by this change since production and test code use
different helpers.

**Finalize focus:** high — this is the one change in the whole plan most likely to reveal a detail
nobody has thought about yet (e.g. does `PreferenceDataStoreFactory.createWithPath`'s underlying
implementation handle a directory that doesn't exist yet, or does `mkdirs()` need to happen before
each `create*DataStore` call, not just once at startup). Say plainly what was and wasn't verified.

---

## Task 2 — CI job: build the Linux package on PRs

**Why:** nothing in CI has ever run `packageDistributionForCurrentOS`/`packageDeb` — that's exactly
how task 0's bug went unnoticed since PR #221. Without this, a future regression (e.g. someone moves
`art/` again) goes uncaught the same way.

**Depends on task 0** (the job would fail on the same bug otherwise) and ideally task 1 (no strict
build dependency, but there's little point guarding packaging in CI while the packaged app still has
the storage bug — check the status table; if 1 isn't done yet, this task can still proceed since it
only needs the package to *build*, not to be correct).

**Scope:** add a `desktop-package` job to `.github/workflows/build.yml` (same PR-gate workflow the
Android job lives in), running on `ubuntu-latest`:
- Java 21 setup — reuse `./.github/actions/android-setup-composite-action` for consistency with the
  rest of the repo's CI (it also sets up the Android SDK, which this job doesn't need, but keeping one
  setup action avoids maintaining two). If that overhead turns out to matter, a lighter Java-only
  `actions/setup-java` step is the alternative — note which was chosen and why in the Result note.
- `sudo apt-get update && sudo apt-get install -y fakeroot` before the package step — per
  [survey.md](survey.md#toolchain--ci-environment-notes), `fakeroot` is not confirmed present on
  `ubuntu-latest`; confirm one way or the other in this task rather than finding out from a red CI run.
- `./gradlew :composeApp:packageDeb` (not the full `packageDistributionForCurrentOS`, which would also
  attempt Dmg/Msi — those need macOS/Windows runners and are out of scope for a Linux-only CI check).
- No artifact upload needed for this task — that's task 3's job. This task only needs the build to
  succeed as a PR gate.

**Done when:** a PR touching `composeApp/build.gradle.kts` (e.g. this very task's own diff, or a
throwaway change) shows the new job passing in the Actions tab, and reverting task 0's fix locally and
re-running the job locally (`act` or just running the Gradle command by hand) confirms it would have
caught that regression.

**Finalize focus:** medium — note whether `fakeroot` needed the explicit install step or was already
present, since that's exactly the kind of CI-environment fact that's expensive to rediscover later.

---

## Task 3 — Wire the `.deb` into the release workflow

**Why:** this is the actual "release" — everything before this is prerequisite plumbing. Right now
`release.yml` only builds and uploads Android artifacts.

**Depends on tasks 0–2.**

**Scope:** extend `.github/workflows/release.yml`'s existing `release` job (same tag-triggered /
`workflow_dispatch` job that already builds the Android artifacts) rather than adding a second job —
keeps the single GitHub Release created by `softprops/action-gh-release@v3` getting all platforms'
assets in one step, matching how the Android APKs/AAB are already listed together in one `files:`
block:
- Add the same Java 21 + `fakeroot` setup task 2 settled on.
- `./gradlew :composeApp:packageDeb`.
- Add the produced `.deb` (path under `composeApp/build/compose/binaries/main/deb/*.deb`) to the
  existing `files:` list in the `Create GitHub release` step, alongside the APK/AAB paths.

**Open question to resolve while scoping this task, not before:** does the `.deb`'s embedded version
need to track `libs.versions.toml`'s `version-name`/`version-code` the same way the Android artifacts
do? (It already does — `packageVersion`/`debPackageVersion` are wired from `libs.versions.toml` per
the survey — so this is likely a non-issue, but confirm the built `.deb`'s actual version string
matches the release tag before calling this done.)

**Done when:** a `workflow_dispatch` run of `release.yml` (the documented way to re-run a release
build without cutting a new tag, per `docs/build/release.md`'s troubleshooting section) produces a
GitHub Release with the `.deb` attached alongside the Android artifacts, and `dpkg -I` on the
downloaded file shows the right version/package name.

**Finalize focus:** medium — this is mostly mechanical once tasks 0–2 are done; note anything about
GitHub Release asset naming collisions or `action-gh-release`'s glob behavior that wasn't obvious from
the existing Android `files:` block.

---

## Task 4 — Add `Rpm` as a second target format

⛔ **Gated — do not start without asking (see status table).** Real value (Fedora/openSUSE users) but
not required for a first release; asking first avoids scope creep on top of an already multi-task plan.

**Why, if approved:** `targetFormats` currently only includes `Deb`. jpackage supports `Rpm` as a
sibling format with the same `nativeDistributions` config — no architecture beyond what task 0–3
already built.

**Scope, if approved:** add `TargetFormat.Rpm` to `targetFormats(...)` in
`composeApp/build.gradle.kts`, confirm `rpmbuild` availability on the CI image (likely needs an
explicit `apt-get install rpm` on an `ubuntu-latest` runner — verify, don't assume, same as task 2 did
for `fakeroot`), extend the CI job from task 2 and the release wiring from task 3 to also build/upload
`packageRpm`.

**Done when:** `./gradlew :composeApp:packageRpm` succeeds locally and in CI, and the `.rpm` is
attached to the GitHub Release next to the `.deb`.

---

## Task 5 — Install a real logger backend on desktop

⛔ **Gated — do not start without asking (see status table).** Genuinely useful for diagnosing
user-reported bugs post-release, but not required to ship a first release, and the right design (file
location, rotation, whether to also wire `CrashReporterImpl.jvm.kt` at the same time) deserves a scope
discussion rather than being assumed here.

**Why, if approved:** `TaigaMobileDesktop.kt` never calls `TaigaLogger.install(...)`; Desktop/JVM
falls back to the no-op `NoLog` backend (per CLAUDE.md's Logging table and
[survey.md](survey.md#diagnosability-on-desktop-existing-documented-gaps--not-new-findings)). Every
`logcat {}` call site is silently dropped, so a Linux user's bug report comes with zero log context.

**Scope, if approved:** a `core/logger` JVM backend that writes to a file (natural location: the same
per-user app-data directory task 1 introduces), installed from `TaigaMobileDesktop.kt`'s `main()`
alongside the existing `FileKit.init(...)`/`startKoin(...)` calls. Rotation/size-capping policy and
whether `CrashReporterImpl.jvm.kt` should also stop being a pure stub are open questions to resolve
when this task is actually scoped, not decided here.

---

## Task 6 — Update README once Linux is actually distributed

**Depends on task 3.** Trivial once 3 lands: change README's desktop row from
`Distribution TBD` to say where the `.deb` is (GitHub Releases), and update the "iOS & Desktop" section
that currently tells readers "distribution channels are not yet set up" for the Linux case
specifically — leave the iOS/macOS/Windows wording alone since this plan doesn't touch those.

**Done when:** README accurately describes how a Linux user gets the app, and doesn't overclaim
anything about macOS/Windows that tasks 0–3 didn't actually do (those `targetFormats` entries exist
and build locally, per the survey, but nothing in this plan wires them into CI or a release — don't
imply they're supported just because `Dmg`/`Msi` appear in the same Gradle block).

**Finalize focus:** low.

---

## Considered and deferred

| Idea | Why deferred |
|---|---|
| AppImage / Flatpak / Snap | Real additional infrastructure (signing keys, store manifests, Flathub/Snap Store review) — worth it only once the `.deb`-via-GitHub-Releases path (tasks 0–3) is proven to work end-to-end and there's a concrete reason `.deb`-only isn't enough. |
| Apt repository (so users get updates via `apt upgrade` instead of re-downloading) | Needs a signing key and hosting; same "prove the simple path first" reasoning as above. |
| Auto-update mechanism inside the app | Not attempted — no existing infra to build on (see survey); a reasonable follow-up once there's more than one release to update *to*. |
| arm64 Linux build | jpackage packages for the runner's own architecture only; would need a second CI job on an arm64 runner. Not scoped — revisit if there's actual user demand. |
| macOS/Windows CI + release wiring | Out of scope for this plan (titled Linux release deliberately) — `Dmg`/`Msi` target formats already exist in the Gradle config and build locally per the survey, but wiring them into CI/release is a separate plan, not folded in here to keep this one small. |

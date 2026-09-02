# Desktop (Linux) release: what exists today

**Surveyed:** 2026-08-09 against `dev` (`06d35616`).
**Scope:** whether the Compose Desktop JVM target can actually ship a Linux `.deb` right now —
build config, packaging, storage, CI, and what a real user would hit. Descriptive, not a proposal —
the proposal is [linux-release-plan.md](linux-release-plan.md).

## TL;DR

Desktop **builds and runs** (`./gradlew :composeApp:run` works), but **packaging is broken and has
never been exercised**: `:composeApp:packageDeb` fails immediately on a bad icon path that has been
wrong since it was introduced. Independently, and more seriously, every JVM-actual storage backend
(Room DB, all four DataStores, server URL) writes into `java.io.tmpdir` instead of a real per-user
data directory — so even once packaging works, a shipped build would lose logins and local data on
reboot on most Linux setups. No CI workflow builds, packages, or even smoke-tests the desktop target
at all. README already says as much plainly: "Desktop (Linux/macOS/Windows) | Builds & runs |
Distribution TBD".

## Packaging config

`composeApp/build.gradle.kts`, `compose.desktop.application.nativeDistributions`:

- `targetFormats(TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)` — Linux only gets `Deb`
  (Debian/Ubuntu family). No `Rpm` (jpackage supports it directly), no AppImage/Flatpak/Snap.
- `packageName`/`packageVersion`/`description`/`vendor`/`copyright` are wired from
  `gradle/libs.versions.toml` (`app-name`, `version-name`, `app-description`, `app-vendor`) — these
  exist and are populated correctly.
- `linux { debMaintainer, debPackageVersion, appCategory, menuGroup, shortcut }` all wired from the
  same catalog entries (`app-vendor`, `app-category` = `"utils"`, `app-menugroup` = `"Office"`).
- **`iconFile.set(project.file("../info/art/taiga-mobile-logo.png"))` — broken.** Resolves to
  `<repo-root>/info/art/...`, which does not exist; the real assets live at `<repo-root>/art/...`
  (confirmed: `git log -S"info/art"` shows the path was introduced wrong in `0d4f8ccf`, the KMP
  migration PR #221, and never touched since). Same wrong prefix on the Windows (`.ico`) and macOS
  (`.icns`) `iconFile` lines too.
- **Verified by actually running it**: `./gradlew :composeApp:packageDistributionForCurrentOS`
  reaches `:composeApp:checkRuntime` and `:composeApp:createRuntimeImage` successfully, then
  `:composeApp:packageDeb` fails Gradle's task-input validation outright —
  `Input file does not exist ... property 'iconFile' specifies file
  '.../info/art/taiga-mobile-logo.png' which doesn't exist`. This is not a runtime/environment gap;
  it is a one-line path bug that has silently blocked every attempt at packaging since #221.
- Proguard (`proguard-desktop.pro`) is enabled for `buildTypes.release` and looks complete —
  `-dontoptimize` plus keep rules for Koin, kotlinx.serialization, Room/SQLite bundled driver, the
  Ktor OkHttp engine, and OkHttp/Okio itself. No obvious gaps; not exercised past compile since
  packaging never got this far.

## Storage — the bigger problem

Every JVM-actual persistent store resolves its file path off `System.getProperty("java.io.tmpdir")`:

| What | File | Where |
|---|---|---|
| Room DB | `taigamobilenova.db` | `core/storage/.../di/DBModule.jvm.kt` |
| Auth tokens | `$AUTH_DATA_STORE_FILE_NAME` | `core/storage/.../di/StorageModule.jvm.kt` |
| Session | `$TAIGA_SESSION_STORAGE` | same file |
| Filters | `$SESSION_FILTERS_DATA_STORE_FILE_NAME` | same file |
| Trusted certs | `$TRUSTED_CERT_DATA_STORE_FILE_NAME` | same file |
| Server URL | `SERVER_STORAGE_FILE_NAME` | `core/storage/.../server/ServerStorageImpl.jvm.kt` |

Compare with the iOS actuals for the exact same six stores (`ServerStorageImpl.ios.kt`,
`StorageModule.ios.kt`), which correctly resolve `NSDocumentDirectory` via `NSFileManager`. The JVM
side never got the equivalent treatment — `java.io.tmpdir` looks like it was a quick placeholder
during the KMP migration that stuck.

**Why this matters on Linux specifically:** `/tmp` (or wherever `java.io.tmpdir` points) is routinely
tmpfs or cleared by `systemd-tmpfiles`/reboot on common distros. A real user would see their login
session, trusted-certificate approvals, and local DB cache disappear unpredictably — most visibly, on
every reboot. This is not a corner case; it is the normal operating condition of `/tmp` on a modern
Linux desktop.

Note: `java.io.tmpdir` is also the path `:testing/.../PlatformTestUtils.kt` and
`composeApp/src/jvmTest/.../LiveTaigaSession.kt` use deliberately for tests — that usage is correct
and unrelated; the bug is that **production** code (`DBModule.jvm.kt`, `StorageModule.jvm.kt`,
`ServerStorageImpl.jvm.kt`, all under `src/jvmMain`, not `jvmTest`) shares the same call.

## CI

Three workflows exist; none touch the desktop target:

- `build.yml` — PR gate, builds `androidApp:assembleFdroidDebug` + `assembleGplayDebug` only.
- `code_analysis.yml` — detekt, ktlint, `jvmTest` (this *does* run desktop-touching `jvmTest`/
  `commonTest` code, since JVM is a real KMP target — but never runs `packageDistributionForCurrentOS`
  or any packaging task), Kover, `koverVerify`.
- `release.yml` — tag-triggered, builds only Android APKs/AAB and creates the GitHub Release.

So the icon-path bug above has had no way to be caught — nothing in CI has ever invoked
`packageDeb`/`packageDistributionForCurrentOS`.

## Diagnosability on desktop (existing, documented gaps — not new findings)

- `TaigaMobileDesktop.kt` (the desktop `main()`) never calls `TaigaLogger.install(...)`. Per
  CLAUDE.md's Logging table, Desktop/JVM falls back to the no-op `NoLog` backend — every `logcat {}`
  call site is silently dropped. Android and iOS both install a real backend from their entry points;
  desktop never has.
- `CrashReporterImpl.jvm.kt` is a stub: `isAvailable = false`, every method a no-op. No crash
  telemetry of any kind on desktop.
- Combined with the storage bug above, a Linux user hitting a real problem after a first release
  would be close to undiagnosable from the vendor side: no logs, no crash reports, and the most
  likely symptom ("I keep getting logged out") is itself a consequence of the storage bug, not a
  separate report.

## Toolchain / CI environment notes

- `jpackage --type deb` needs `dpkg-deb` (present on any Debian/Ubuntu-based runner, including
  `ubuntu-latest`) and `fakeroot` (confirmed present on this dev machine; **not confirmed** on
  GitHub-hosted `ubuntu-latest` runners — worth an explicit `apt-get install -y fakeroot` step rather
  than assuming).
- jpackage only ever packages for the host's own architecture. A `ubuntu-latest` (x86_64) CI runner
  produces an amd64 `.deb` only — arm64 Linux is not covered without a second runner/job.
- Local dev machine's `.java-version` is `24`; CI's `android-setup-composite-action` installs Java
  `21` (Temurin). Both should have `jpackage` (bundled since JDK 16), but the combination running the
  desktop packaging tasks specifically has not been exercised on JDK 21 — only informally on 24, here,
  during this survey.

## Distribution channel

Not decided yet. Realistic options for a first release, roughly in order of effort:

1. Attach the `.deb` as a GitHub Release asset (same pattern already used for the Android APKs/AAB in
   `release.yml`) — no new infrastructure, users `dpkg -i` manually.
2. Add `Rpm` to `targetFormats` for Fedora/openSUSE users — jpackage supports it as a sibling format
   to `Deb` with no new tooling beyond what `rpmbuild` requires on the CI image.
3. An apt repository, AppImage, Flatpak, or Snap — each is real additional infrastructure (signing
   keys, packaging manifests, store review for Flathub/Snap Store) and is out of scope until (1) is
   proven to work end-to-end.

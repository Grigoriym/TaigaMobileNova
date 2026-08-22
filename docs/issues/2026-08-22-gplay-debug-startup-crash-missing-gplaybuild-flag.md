# Gplay debug build crashes on startup

**Status:** Done (root cause confirmed, no code fix needed)
**Link:** none — internally noticed during tablet-form-factor work
**Updated:** 2026-08-22

## Report

Noted 2026-08-21 in `docs/architecture/tablet-form-factor-support/CHECKLIST.md` (step 12c
progress log): "Gplay debug build crashes on startup — noticed 2026-08-21, not yet investigated
(no logcat captured)." No repro command, no stack trace, no build variant details were recorded
at the time — just the observation that the app didn't come up.

## Findings

- Built `./gradlew :androidApp:assembleGplayDebug -PgplayBuild`, installed
  `app-gplay-debug.apk` (package `com.grappim.taigamobile.debug`) on `Medium_Phone_API_36.1`, and
  launched via `am start -n com.grappim.taigamobile.debug/com.grappim.taigamobile.MainActivity`.
  **No crash** — the app reached the login screen normally (`pidof` stayed alive, `logcat` showed
  no `FATAL EXCEPTION`, `Displayed ... +2s293ms`).
- Rebuilt the identical variant **without** `-PgplayBuild`
  (`./gradlew :androidApp:assembleGplayDebug`) — this still compiles and packages successfully,
  same `app-gplay-debug.apk` path, because `androidApp/build.gradle.kts:13-16` only gates the
  `google-services`/`firebase-crashlytics` *plugins* on the property, not the flavor's
  compilation. Reinstalled and relaunched the exact same way.
- **This build crashes immediately on `Application.onCreate`.** Full stack trace captured via
  `adb logcat -d`:
  ```
  java.lang.RuntimeException: Unable to create application com.grappim.taigamobile.TaigaApp
  Caused by: org.koin.core.error.InstanceCreationException: Could not create instance for
    '[Singleton: 'com.grappim.taigamobile.data.CrashReporterImpl', binds:...CrashReporter]'
  Caused by: java.lang.IllegalStateException: Default FirebaseApp is not initialized in this
    process com.grappim.taigamobile.debug. Make sure to call FirebaseApp.initializeApp(Context)
    first.
    at com.google.firebase.FirebaseApp.getInstance(FirebaseApp.java:178)
    at com.google.firebase.crashlytics.FirebaseCrashlytics.getInstance(FirebaseCrashlytics.java:194)
    at com.google.firebase.crashlytics.FirebaseCrashlyticsKt.getCrashlytics(FirebaseCrashlytics.kt:27)
    at com.grappim.taigamobile.data.CrashReporterImpl.<init>(CrashReporterImpl.kt:10)
  ```
- `CrashReporterImpl` (`androidApp/src/gplay/kotlin/.../data/CrashReporterImpl.kt:10`)
  eagerly reads `Firebase.crashlytics` in its constructor. Koin instantiates this singleton
  during `TaigaApp.setupLogger()` (`TaigaApp.kt:70`, called from `onCreate` at `TaigaApp.kt:52`),
  so the crash fires before any screen renders — matches "crashes on startup" exactly.
- This exact failure mode — symptom, cause, and fix — was already documented in `CLAUDE.md`
  (added 2026-08-10, commit `ea93c513`): omitting `-PgplayBuild` lets the gplay flavor compile
  without the `google-services`/`firebase-crashlytics` plugins applied
  (`androidApp/build.gradle.kts:13-16`), so `FirebaseApp` never initializes, and
  `CrashReporterImpl` throws exactly this `IllegalStateException` "at app startup, release or
  debug."

## Root cause

The 2026-08-21 crash was built or installed via a Gplay debug command that omitted
`-PgplayBuild` (the flag is easy to drop since the flavor still compiles cleanly without it —
there's no build-time signal that anything is missing). This reproduces `CrashReporterImpl`
(`androidApp/src/gplay/kotlin/.../data/CrashReporterImpl.kt:10`) hitting an uninitialized
`FirebaseApp` during Koin singleton creation in `TaigaApp.onCreate()`
(`TaigaApp.kt:52,70`), crashing the process before any UI is shown. This is a pre-existing,
already-documented gotcha (`CLAUDE.md`'s Build Commands section), not a new defect from the
tablet work.

Built and launched **with** `-PgplayBuild`, the app comes up cleanly with no crash — confirmed
twice on `Medium_Phone_API_36.1` (once before, once after reproducing the negative case).

## Impact

None on the current codebase or CI — CI always passes `-PgplayBuild` (per the `CLAUDE.md` note
accompanying the original 2026-08-10 fix). The only exposure is a local developer/session
forgetting the flag when building Gplay locally, which fails loudly and immediately (crash on
first launch, clear logcat trace) rather than silently — low severity, easy to self-diagnose
once the CLAUDE.md note is read.

## Open questions

None outstanding — the 2026-08-21 note lacked the exact command used, but the reproduction here
matches the symptom, the crash signature, and the previously-documented mechanism closely enough
that no further ambiguity remains.

## Options

Not applicable — this is "already fixed" / "working as intended": the flag is required by
design (`androidApp/build.gradle.kts:8-16`'s comment explains why the plugin can't be
conditionally applied inside the `plugins {}` block itself), and the failure mode plus the
correct command are already documented in `CLAUDE.md`. No code change is warranted.

## Decision

No fix needed. Closing as a documentation-gap-shaped false alarm: the crash is the known,
intentional "you forgot `-PgplayBuild`" failure, not a new bug introduced by the tablet-support
work. Recorded here (rather than left as an unexplained checklist bullet) so a future session
doesn't re-investigate the same thing from scratch.

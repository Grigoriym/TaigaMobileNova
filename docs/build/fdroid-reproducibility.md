# F-Droid build failure: Crashlytics/Google Services plugins break scanning and reproducibility

## Context

`com.grappim.taigamobile.fdroid` on F-Droid (metadata in the `fdroid/fdroiddata`
repo, `metadata/com.grappim.taigamobile.fdroid.yml`) builds the `fdroid` flavor
of this app from source and verifies the result byte-for-byte against the
official `app-fdroid-release.apk` binary attached to the corresponding GitHub
release (declared via `Binaries:` in the metadata).

Starting with v2.1.3 (versionCode 42, commit `0fc468970787e00a21691b40ba8fa8c805365433`),
the F-Droid CI build fails. Two separate problems were found, both rooted in
the same cause.

## Root cause

The actual Firebase Crashlytics *dependency* is correctly gated to the gplay
flavor:

```kotlin
// androidApp/build.gradle.kts
dependencies {
    // Crashlytics ships in the gplay flavor only — the fdroid flavor never pulls in
    // this proprietary dependency, only a no-op CrashReporter implementation.
    gplayImplementation(project.dependencies.platform(libs.firebase.bom))
    gplayImplementation(libs.firebase.crashlytics)
}
```

But the Gradle **plugins themselves** are applied unconditionally, for every
flavor including `fdroid`:

```kotlin
// androidApp/build.gradle.kts
plugins {
    alias(libs.plugins.taigamobile.android.application)
    alias(libs.plugins.google.services)       // applied to ALL flavors
    alias(libs.plugins.firebase.crashlytics)  // applied to ALL flavors
}
```

```kotlin
// build.gradle.kts (root) — registered with apply false, but still present
alias(libs.plugins.google.services) apply false
alias(libs.plugins.firebase.crashlytics) apply false
```

Note there's already a `project.hasProperty("gplayBuild")` gate in
`androidApp/build.gradle.kts` used for `dependenciesInfo` — the same technique
should be extended to these plugins.

## Problem 1: F-Droid's scanner rejects the source

F-Droid's fdroidserver `scanner.py` resolves Gradle version-catalog references
(`libs.plugins.firebase.crashlytics`, `libs.firebase.crashlytics`,
`libs.firebase.bom`) against `gradle/libs.versions.toml` and matches the
resulting coordinates against a list of known non-free/tracking libraries.
Since the plugin `alias(...)` lines are present in the built source
regardless of flavor, the scan fails with:

```
ERROR: Found usual suspect 'libs.plugins.firebase.crashlytics: crashlytics' at build.gradle.kts
ERROR: Found usual suspect 'libs.plugins.firebase.crashlytics: com(\.google)?\.firebase[.:](...)' at build.gradle.kts
ERROR: Found usual suspect 'libs.plugins.firebase.crashlytics: crashlytics' at androidApp/build.gradle.kts
ERROR: Found usual suspect 'libs.plugins.firebase.crashlytics: com(\.google)?\.firebase[.:](...)' at androidApp/build.gradle.kts
Could not build app com.grappim.taigamobile.fdroid: Can't build due to 4 errors while scanning
```

This can be worked around from the `fdroiddata` side with `prebuild` sed
commands stripping the offending lines before the scan runs — but that leads
straight into problem 2.

## Problem 2: reproducibility mismatch (the real blocker)

Once the scanner-only workaround (sed-stripping the `firebase` lines in
F-Droid's `prebuild` step) was tried, the build proceeded to compile but then
failed F-Droid's binary-transparency check: the from-source build no longer
byte-matches the official `app-fdroid-release.apk`.

A maintainer decompiled both APKs (apktool/smali) and diffed them. The result
was a clean, deterministic pattern: every string resource ID from a certain
point onward is shifted by a constant offset (e.g. `0x7f0f003c` →
`0x7f0f003a`, a shift of -2), with matching constant updates throughout the
smali (every place in the bytecode referencing one of those IDs recompiles to
the new number). This is why the raw CI diff looked catastrophic
(`classes.dex`, `resources.arsc`, dozens of `res/*.xml` all reported as
differing) — one shift in the resource table cascades into every class that
references a shifted ID.

The cause: applying the Firebase Crashlytics Gradle plugin injects a couple of
extra string resources into the build regardless of which flavor's dependency
graph is compiled. The official release binary has them (plugin applied,
unconditionally, as shown above); an F-Droid build with the plugin lines
stripped does not — hence the consistent resource-ID offset.

This is **not** the classic "Crashlytics bakes in a random build ID every
build" non-determinism problem (which would produce unrepeatable, differently-random
diffs on every build) — it's a stable, structural difference caused by the
plugin being applied for a flavor that isn't supposed to have it. That's
actually good news: it means the divergence is fixable at the source level,
not an inherent limitation.

## Required fix

Stop applying the `google-services` and `firebase-crashlytics` **plugins**
(not just the dependency) for the `fdroid` flavor. Reuse the existing
`gplayBuild` property gate. Rough shape:

```kotlin
// androidApp/build.gradle.kts
plugins {
    alias(libs.plugins.taigamobile.android.application)
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

if (project.hasProperty("gplayBuild")) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}
```

The root `build.gradle.kts` already registers both with `apply false`, so no
change needed there beyond what's already in place — just make sure nothing
else applies them unconditionally.

After this change:

1. Cut a new release (new tag/version) whose CI-built
   `app-fdroid-release.apk` is produced **without** setting `gplayBuild`
   (i.e., the fdroid-flavor build task must not apply those plugins), so the
   officially published binary matches what F-Droid will build from source.
2. Update `metadata/com.grappim.taigamobile.fdroid.yml` in `fdroiddata` to
   point at the new commit/version. The `prebuild` sed workaround for
   `firebase` lines that was tried and reverted there should no longer be
   necessary — once the plugins are properly flavor-gated, the fdroid-flavor
   build source won't reference them at all, so nothing needs stripping.
3. Verify locally (or via `fdroid build --test`) that a from-source
   `fdroid`-flavor build now matches the new official binary before
   resubmitting.

## Where things stand

- `fdroiddata`'s `metadata/com.grappim.taigamobile.fdroid.yml` currently has
  **no** Firebase-stripping `prebuild` lines (reverted back to just the
  existing `storeFile`/`signingConfig`/`foojay` sed commands for versionCode
  42), since that workaround can't produce a reproducible match against the
  already-published v2.1.3 binary.
- No changes have been made yet in this repo (TaigaMobileNova). This doc is
  the handoff describing what needs to change here.

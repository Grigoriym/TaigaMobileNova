# Play Console: edge-to-edge warnings on release 45 (2.2.0)

**Status:** Done (root cause confirmed, no code fix possible/needed)
**Link:** Google Play Console → "actions recommended" for release 45 (2.2.0)
**Updated:** 2026-09-04

## Report

Play Console flagged two items on the gplay release build (release name "45 (2.2.0)"):

1. "Edge-to-edge may not display for all users" — generic targetSdk-35 guidance to call
   `enableEdgeToEdge()` / handle insets.
2. "Your app uses deprecated APIs or parameters for edge-to-edge" — names
   `android.view.Window.setStatusBarColor` and `android.view.Window.setNavigationBarColor`,
   starting in obfuscated classes `df1.b` and `ff1.b`.

## Findings

- `MainActivity.onCreate()` already calls `androidx.activity.enableEdgeToEdge()`
  (`androidApp/src/main/kotlin/com/grappim/taigamobile/MainActivity.kt:32`) before
  `super.onCreate()`. `git log` shows this was added in commit `0d4f8ccf` (2026-03-04), months
  before release 45 was built — the app was never missing the call this warning describes.
- `version-code = "45"` in `gradle/libs.versions.toml` matches the flagged release exactly, so a
  locally-built `gplayRelease` APK/mapping pair (`androidApp/build/outputs/apk/gplay/release/`,
  timestamped 2026-08-10, same build) is a faithful stand-in for what Play scanned.
- Grepped the app's own source, `composeApp`, and every feature module for
  `setStatusBarColor`/`setNavigationBarColor`/`SystemUiController` — no hits. The app never calls
  these APIs directly.
- Extracted `classes.dex`/`classes2.dex` from the local `app-gplay-release.apk` and disassembled
  with `dexdump -d`. Found exactly 4 call sites, all `Landroid/view/Window;.setStatusBarColor` +
  `.setNavigationBarColor` pairs, in 4 minified classes each with a single method `b`.
- Cross-referenced those minified names against the matching `mapping.txt`
  (`androidApp/build/outputs/mapping/gplayRelease/mapping.txt`, same build timestamp):

  | Minified (this build) | Original class |
  |---|---|
  | `Loe1;` | `androidx.activity.EdgeToEdgeApi23` |
  | `Lpe1;` | `androidx.activity.EdgeToEdgeApi26` |
  | `Lre1;` | `androidx.activity.EdgeToEdgeApi29` |
  | `Lte1;` | `androidx.activity.EdgeToEdgeApi35` |

  (Play's own obfuscated names `df1.b`/`ff1.b` are from Google's separately-built AAB and won't
  match these exact minified names — R8 naming isn't stable across builds — but the class shape
  — one class per SDK-level implementation, single method `b`/`setUp` calling both setters — is
  the same pattern, and grepping every `androidx.activity` version in the Gradle cache (1.7.0
  through 1.13.0) turns up no other candidate.)
- Decompiled `EdgeToEdgeApi23.class`/`EdgeToEdgeApi26.class` from the resolved
  `androidx.activity:activity` artifact directly (`javap -c`): each `setUp()` method calls
  `Window.setStatusBarColor` / `Window.setNavigationBarColor` as its own backward-compatibility
  scrim logic for API levels below 35 (`EdgeToEdgeApi35` is the one used on 35+, and does not need
  the deprecated calls). This is `enableEdgeToEdge()`'s own internal implementation — the exact
  function this app already calls — doing what it has always done to paint status/nav bar scrims
  on older Android versions where the new insets APIs don't exist yet.

## Root cause

Both warnings trace back to the same thing: Play Console's static/dex scanner reports deprecated
API usage found **anywhere in the merged, minified APK dex**, including inside library code that
implements backward compatibility on purpose. `androidx.activity`'s own `enableEdgeToEdge()` calls
`Window.setStatusBarColor`/`setNavigationBarColor` internally (gated by `Build.VERSION.SDK_INT`)
for every device below API 35, because that's the only way to draw the compat scrim on those
versions. There is no direct call to these APIs anywhere in this app's own source.

The first warning ("edge-to-edge may not display for all users") is boilerplate guidance shown to
targetSdk-35 apps in general; it is already satisfied here — `enableEdgeToEdge()` has been called
since March 2026, well before this release.

## Impact

None. Nothing in app behavior is broken — `enableEdgeToEdge()` behaves as designed on all API
levels. The Play Console notice is non-actionable from the app side: removing these calls would
mean not calling `enableEdgeToEdge()` at all, which would reintroduce the actual insets/edge-to-edge
problem the first warning is about. The warning clears only when Google ships a version of
`androidx.activity` whose pre-35 compat path stops using the deprecated setters (or Play Console's
scanner learns to exclude sanctioned AndroidX internals) — nothing to track on our side.

## Options

Not applicable — no code change is warranted. Confirmed no other library in the dependency graph
(`com.google.android.material:material:1.14.0`, `com.google.android.play:app-update(-ktx):2.1.0`)
contributes these calls; `androidx.activity` is the sole source.

## Decision

No fix needed. Closing as a Play Console false-positive caused by its scanner not distinguishing
"app calls a deprecated API" from "a sanctioned AndroidX compat shim calls it internally, behind an
SDK_INT gate, on the app's behalf." Recorded here so a future session (or the next Play Console
nag on a later release) doesn't re-investigate the same androidx.activity internals from scratch.

# TaigaMobileNova — Emulator testing

Project-specific facts for the `emulator-testing` skill. Generic adb/uiautomator
technique lives in the skill itself, not here — this file is only what's true about
*this* app.

## Device facts

- AVD: `Medium_Phone_API_36.1`
- Package id(s): `com.grappim.taigamobile.fdroid.debug` (fdroid debug build — the
  `debug` build type adds its own `.debug` suffix on top of the `fdroid` flavor's
  `.fdroid` suffix, so the id is not just `com.grappim.taigamobile.fdroid`). Release
  builds and the gplay flavor drop one or both suffixes accordingly.
- Activity: `com.grappim.taigamobile.MainActivity` (the Kotlin package is the base
  namespace `com.grappim.taigamobile` regardless of flavor/build-type suffixes — only
  the application id gets suffixed, not the class's package)
- Build: `./gradlew :androidApp:assembleFdroidDebug`, APK lands at
  `androidApp/build/outputs/apk/fdroid/debug/app-fdroid-debug.apk`
- Backend/local server: the local Docker Taiga instance is at `http://localhost:9000/`
  on the host — from the emulator, reach it at `http://10.0.2.2:9000` (standard AVD
  host-loopback alias). Seeded creds `admin`/`admin` (see `docs/local-info.md`).
  `LoginViewModel.SERVER_REGEX` requires a dotted hostname, so `10.0.2.2` works but bare
  `localhost` would not (see `docs/revisit.md` #29 and memory `local-taiga-instance`).
  The app shows an "Unencrypted connection" confirm dialog for plain `http://` — tap
  **Yes** to proceed (this is expected for a self-hosted/LAN server, not a bug).

## App-specific gotchas

- **A stale server URL can already be pre-filled in the login form on a "fresh" install**
  even after `adb uninstall` + reinstall — seen once as a real LAN IP
  (`192.168.0.248:9000`) instead of an empty field. Cause not fully diagnosed (possibly
  AVD-level state surviving the uninstall, not app-level). Don't assume the field is
  empty — screenshot first, and if it's pre-filled, clear it before typing: tap the
  field, `input keyevent KEYCODE_MOVE_END`, then a run of ~40 individual
  `input keyevent KEYCODE_DEL` calls. **`input keycombination 113 29` (Ctrl+A) did NOT
  reliably select-all in this app's Compose `OutlinedTextField`** — it only deleted a
  few trailing characters, corrupting the field into a mix of old and new text. Use the
  repeated-`KEYCODE_DEL` approach instead.
- **Login persists across both `am kill` and `am force-stop`.** After a real login, a
  full `force-stop` + `am start` lands directly back on the post-login "Select Project"
  screen with no re-login needed — this is the clean cold-start journey for perf
  captures (no setup navigation beyond the very first login of a session).
- **`am start -n` after tapping Continue can land back on the password field** if the
  soft keyboard is still open — the keyboard shifts the layout, so a coordinate computed
  from a pre-keyboard dump/screenshot lands on the wrong element once it's open.
  `input keyevent KEYCODE_BACK` dismisses the keyboard without navigating away from the
  screen; re-dump/re-screenshot after that before tapping a button below the fold.
- Post-login landing screen is **"Select Project"** (`Search projects` field + an
  `Owner`-grouped list) — this is the seeded local instance's three projects: `empty
  (empty)`, `additional (main-3)`, `Main project (main-2)`.

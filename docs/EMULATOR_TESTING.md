# TaigaMobileNova — Emulator testing

Project-specific facts for the `emulator-testing` skill. Generic adb/uiautomator
technique lives in the skill itself, not here — this file is only what's true about
*this* app.

## Device facts

- AVD: `Medium_Phone_API_36.1` (phone, compact width) and `Medium_Tablet` (tablet, confirmed
  1280dp-wide landscape — expanded width; used for adaptive-navigation-suite verification,
  see `docs/architecture/tablet-form-factor-support/`)
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
- **On `Medium_Tablet`, `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo` resolves to
  `NavigationRail` (narrow icon-over-label column), not `NavigationDrawer`**, even though the
  AVD's landscape width is ~1280dp — comfortably past the 840dp expanded-width breakpoint.
  Confirmed 2026-08-15 verifying tablet checklist step 4. Don't assume EXPANDED width alone
  guarantees a permanent drawer in this API; a rail is a legitimate result too and both count as
  the "medium/expanded" `NavigationSuiteScaffold` path.
- **A fresh `Medium_Tablet` boot pre-fills the login server-URL field with a stale LAN IP**, same
  as the phone AVD gotcha above — clear it the same way before typing `http://10.0.2.2:9000`.
- **`Medium_Tablet` can cold-boot with no IPv4 default route at all**, even though DHCP reports a
  gateway (`adb shell dumpsys wifi | grep -i gateway` shows `Gateway 10.0.2.2`) and the WIFI network
  shows `CONNECTED` — confirmed via `adb shell ip route show table all`: only an IPv6 `default via
  fe80::2` entry present, no `default via 10.0.2.2 dev wlan0`. The app then gets
  `java.net.ConnectException: Failed to connect to /10.0.2.2:9000` on every request (visible as
  `Ktor : REQUEST ... failed with exception` in logcat) despite the emulator otherwise looking fully
  booted and networked. Fix: `adb shell svc wifi disable && adb shell svc wifi enable` (wait ~8s)
  forces a DHCP re-run that installs the missing route — confirm with `adb shell ip route show table
  all | grep "default.*10.0.2.2"` before retrying the app. Not seen on `Medium_Phone_API_36.1` (route
  present from first boot) — may be specific to `Medium_Tablet`'s virtio-wifi network backend
  (`ro.boot.qemu.virtiowifi=1`) vs. the phone image's classic SLIRP/eth0 networking.
- **The Nav3 back stack does not survive a real process kill.** Confirmed 2026-08-29 verifying
  vm-lifecycle-hardening checklist step 2 (`CreateTaskViewModel`'s new `SavedStateHandle`
  restoration): logged in, navigated Backlog → New user story, typed distinct title/description,
  backgrounded (`KEYCODE_HOME`) and killed the process (`am kill`, confirmed dead via `ps`), then
  relaunched onto the same task (`am start -n`, `sz=1` in `dumpsys activity activities` — a genuine
  restore, not a fresh activity). The app landed on **Dashboard** (its post-login start
  destination), not back on the Create Task screen — despite the user still being logged in. This
  means any per-ViewModel `SavedStateHandle` restoration is currently inert in practice: the user
  never returns to the screen that would show the restored fields. Root cause not yet
  investigated — candidates are `MainActivity`'s `setContent`/`savedInstanceState` handling, or
  `rememberNavigationState`'s `SavedStateConfiguration` not actually being persisted into the
  Activity bundle. See `docs/architecture/vm-lifecycle-hardening/IMPLEMENTATION_PLAN.md`.
- **A stylus first-run tutorial ("Try out your stylus") can pop up over the first tap on
  `Medium_Tablet`** after a fresh-ish boot, identical in shape to the phone AVD's first-run-tutorial
  gotcha elsewhere in this skill — it eats the tap with no error. Screenshot before trusting a tap
  landed in the app; dismiss via its own **Cancel** button (`uiautomator dump` for exact bounds, they
  shift with content).

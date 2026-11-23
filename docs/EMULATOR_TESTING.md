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
  `localhost` would not (see `docs/archive/revisit-resolved.md` #29 and memory `local-taiga-instance`).
  The app shows an "Unencrypted connection" confirm dialog for plain `http://` — tap
  **Yes** to proceed (this is expected for a self-hosted/LAN server, not a bug). On an API 37 AVD
  the system "nearby devices" (`ACCESS_LOCAL_NETWORK`) dialog comes first, on the first login after
  an install or `pm clear` — tap **Allow**, then **Yes**.
- **Before driving anything, check which server the installed app is logged in to** — the phone AVD
  has twice been left logged in to the custom-headers proxy at `:9100`, which usually isn't running
  (see the gotcha below). `adb logcat -d | grep "Ktor.*REQUEST" | tail -1` shows the host.
  `adb shell pm clear com.grappim.taigamobile.fdroid.debug` then a fresh login is the quickest reset.

## App-specific gotchas

- **A real Play in-app update can't be triggered on an emulator** (the app isn't installed from
  Play). To check the prompt, temporarily set `isUpdateDownloaded = true` after a short `delay` in
  `MainActivity.onCreate`, build `:androidApp:assembleGplayDebug -PgplayBuild` (package
  `com.grappim.taigamobile.debug`), and revert before committing. The snackbar shows "An update has
  just been downloaded" / "Restart" above the nav bar. Restart reaches Play's
  `DevTriggeredUpdateService`, which answers `error.code=-7` (no downloaded update) — expected, no crash.
- **Upgrade tests from v2.2.3 or older need an API ≤ 36 AVD.** Builds before the Android 17
  local-network change (#468) don't request `ACCESS_LOCAL_NETWORK`, so on an API 37 AVD their
  login just times out (`ConnectTimeoutException` to `10.0.2.2:9000`, while shell `nc` connects).
  Seed the old build on the tablet AVD (API 36) instead. Current builds show the system
  "find, connect to … nearby devices" dialog on API 37 before the unencrypted-connection one; tap **Allow**.
- **Upgrade-in-place recipe:** build the old tag's fdroid debug APK in a `git worktree` (copy
  `local.properties` and `taigamobilenova_debug.jks` into it). Both builds share the debug cert and,
  until the release bump, the same versionCode, so `adb install -r` upgrades. The Room DB is
  `databases/taigamobilenova_debug.db` (+ `-wal`); pull all three files via `run-as … cat` to diff row counts.
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
  `Owner`-grouped list). The local instance's projects change when it is re-seeded: on 2026-11-22
  `admin` saw `main-project` (id 1) and `user1-project-private`, not the `main-2`/`main-3` older
  docs name. List them with `GET /api/v1/projects` rather than trusting a slug from a doc.
- **On `Medium_Tablet`, `NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo` resolves to
  `NavigationRail` (narrow icon-over-label column), not `NavigationDrawer`**, even though the
  AVD's landscape width is ~1280dp — comfortably past the 840dp expanded-width breakpoint.
  Confirmed 2026-08-15 verifying tablet checklist step 4. The function never returns
  `NavigationDrawer` at all: it returns `NavigationBar` when the width or height is compact (or
  the posture is tabletop), and `NavigationRail` otherwise (source read 2026-11-23,
  `docs/issues/2026-11-23-tablet-nav-rail-and-list-detail.md`).
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
  **Top-level screens are the exception:** backgrounded on Issues, Epics or Backlog (drawer
  sections, `TOP_LEVEL_KEYS`), `am kill` + relaunch restores straight onto that screen (6 of 6 on
  Issues, 2026-11-22, `docs/issues/2026-11-22-excessive-list-requests-and-filter-wipe.md`).
- **A stylus first-run tutorial ("Try out your stylus") can pop up over the first tap on
  `Medium_Tablet`** after a fresh-ish boot, identical in shape to the phone AVD's first-run-tutorial
  gotcha elsewhere in this skill — it eats the tap with no error. Screenshot before trusting a tap
  landed in the app; dismiss via its own **Cancel** button (`uiautomator dump` for exact bounds, they
  shift with content).
- **Typing `@` via the real on-screen keyboard (not `adb shell input text`) needs navigating to the
  IME's symbols page first** — the default Gboard alphabetic layout on `Medium_Phone_API_36.1` has no
  `@` key. `uiautomator dump` does not capture the IME's own window (only the app window), so its
  keys have to be tapped from real device-pixel coordinates read off a full-resolution screenshot
  crop (not `uiautomator dump`, and not eyeballed from the half-scale/low-quality image used to dodge
  the Read-tool threshold above — see that note for why those need scaling back up). At 1080x2400:
  tap `(85, 2180)` for the bottom-left `?123` key to switch to the first symbols page, then `(56,
  1868)` for `@` (leftmost key of that page's top row). Confirmed while GUI-verifying mention-tagging
  Step 7 (`docs/architecture/mention-tagging-support/`); needed again for any future real-keyboard
  `@`-typing check on this AVD/resolution.
- **Gboard's own Enter key is at `(994, 2178)` at 1080x2400** — tap it, rather than `input keyevent
  KEYCODE_ENTER`, to exercise the IME's commit path (a still-composing word is committed together
  with the line break). This AVD's Gboard does **not** autocorrect on Enter (`teh` + Enter stays
  `teh`), so an autocorrect-plus-newline case can't be reproduced here; cover it with a unit test.
  Read a text field's exact content, newlines included, from `uiautomator dump`'s `EditText` node
  `text` attribute (`&#10;` = newline). Confirmed in markdown-editor step 11.
- **No seeded user has an uploaded avatar** — every avatar is Gravatar (off-origin), so a check
  that an image is fetched *from the server* needs one uploaded first (`POST
  /api/v1/users/change_avatar`, multipart `avatar`; `admin` has one since 2026-11-11). Taiga
  returns media URLs on `http://localhost:9000`, which debug builds rewrite to `10.0.2.2:9000`
  (`DebugLocalHostImageManager`) — fine directly, but it bypasses anything in front of the server
  (see `docs/custom-request-headers/IMPLEMENTATION_PLAN.md`'s fixture for the workaround).
- **The phone AVD can be left logged in to a server that isn't running**, e.g. the custom-headers
  proxy fixture at `http://10.0.2.2:9100` (`docs/custom-request-headers/IMPLEMENTATION_PLAN.md`).
  Every list then shows "Connection error". `adb logcat | grep "Ktor.*REQUEST"` shows which host the
  app is calling; log out (Settings → Log out) and log back in at `http://10.0.2.2:9000`.
- **Counting the app's API requests per action.** Debug builds log every call under the `Ktor` tag.
  `adb logcat -c`, do the action, then
  `adb logcat -d | grep -E " Ktor *: REQUEST: " | sed -E 's/.*api\/v1\///'`. A request cancelled by
  `flatMapLatest` shows up as `REQUEST <url> failed with exception`. To make a saved filter's counts
  change on the server, create an issue in that status through the REST API (token from
  `POST /api/v1/auth` with `{"type":"normal","username":"admin","password":"admin"}`) and delete
  it afterwards. Saved filters are in `files/datastore/taiga_session_filters_storage.preferences_pb`
  (`run-as … cat | strings`; an empty selection reads `{}`). Backgrounding on Issues/Epics/Backlog
  then `am kill` restores straight onto that screen. Worked example:
  `docs/issues/2026-11-22-excessive-list-requests-and-filter-wipe.md`.
- **Work-item details hide content until expanded, by design.** A description taller than 6 lines
  (`ExpandableMarkdownText`, 6×24 dp) is clipped behind **Show more** / **Show less**, and the
  Custom fields, Attachments and Comments sections start collapsed (`N Comments` header with a
  chevron). Expand before judging that a line, checkbox or comment is missing.

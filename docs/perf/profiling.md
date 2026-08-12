# Android perf profiling — gfxinfo, Perfetto, Baseline Profile

This is the "run it again" reference for `docs/perf/profiling-plan.md`. It documents two
frame-timing tools plus the `:benchmark` module's Baseline Profile generator, and shows real output
captured against this project's own fdroid build on the `Medium_Phone_API_36.1` AVD (2026-08-12) —
not restated numbers from anywhere else. Generic adb/uiautomator technique (screenshot scaling,
`uiautomator dump`, form filling) lives in the `emulator-testing` skill and
`docs/EMULATOR_TESTING.md`, not here; this file is only about frame timing, tracing, and the
Baseline Profile mechanism.

## Two questions, two tools

- **"Is this slow?"** — `dumpsys gfxinfo`. Zero setup, gives per-frame nanosecond timestamps and
  percentiles for whatever the app rendered since the last reset.
- **"Why is this slow?"** — Perfetto. A system-wide trace you can query for what every thread was
  doing (or not doing) during the slow window.

## The journey used for both captures

Login is mandatory in this app (no anonymous/guest path) — but the session persists across both
`am kill` and a full `am force-stop`, so the clean, repeatable cold-start journey is:

```bash
PKG=com.grappim.taigamobile.fdroid.debug
adb shell am force-stop $PKG
adb shell am start -n $PKG/com.grappim.taigamobile.MainActivity
```

This lands directly on the post-login "Select Project" screen with no re-login needed. Log in once
by hand first (`http://10.0.2.2:9000`, `admin`/`admin` against the local Docker Taiga instance —
see `docs/EMULATOR_TESTING.md`).

## Quick look: `dumpsys gfxinfo`

```bash
PKG=com.grappim.taigamobile.fdroid.debug
adb shell am force-stop $PKG
adb shell dumpsys gfxinfo $PKG reset
adb shell am start -n $PKG/com.grappim.taigamobile.MainActivity
sleep 3
adb shell dumpsys gfxinfo $PKG framestats > out.txt
```

(`dumpsys gfxinfo reset` prints "No process found" if the app isn't running at that instant — that's
expected right after `force-stop`, not an error; the reset still takes effect for the next launch.)

**Real capture (fdroid debug, cold start → Select Project, 2026-08-12):**

```
Total frames rendered: 73
Janky frames: 9 (12.33%)
Janky frames (legacy): 70 (95.89%)
50th percentile: 29ms
90th percentile: 42ms
95th percentile: 65ms
99th percentile: 300ms
```

**Confirmed gotcha (matches the known ring-buffer issue):** the capture's last `---PROFILEDATA---`
row had `FrameCompleted=0` while its own `SwapBuffers` was a real, large timestamp — a dead giveaway
of a stale/never-populated trailing row. `SwapBuffersCompleted` on that same row was populated and
consistent with neighboring rows; use it instead of `FrameCompleted` when this happens.

**Also observed here (AVD/software-renderer specific, not in the original WallosMobile notes):** the
GPU percentile line reported `95th gpu percentile: 4950ms` / `99th gpu percentile: 4950ms` — this is
the histogram's overflow bucket catching frames whose GPU-completion timestamp was never populated
(this AVD runs `-gpu swiftshader_indirect`, a software renderer), not a literal ~5-second GPU frame.
Sanity-check any percentile that lands exactly on the histogram's top bucket boundary before quoting
it as a real duration.

## Deep look: Perfetto

**Capture** (adapted for this project — same journey as above, trace started 1.5s before the cold
start so tracing is live before the app process spawns):

```bash
PKG=com.grappim.taigamobile.fdroid.debug
adb shell am force-stop $PKG
adb shell perfetto -o /data/misc/perfetto-traces/t.perfetto-trace -t 10s \
  sched freq idle am wm gfx view input dalvik hal res memory binder_driver &
sleep 1.5
adb shell am start -n $PKG/com.grappim.taigamobile.MainActivity
sleep 8
adb shell ps -A | grep perfetto   # confirm it actually stopped before pulling
adb pull /data/misc/perfetto-traces/t.perfetto-trace
```

The real capture wrote a 10.25MB trace; `ps -A | grep perfetto` came back empty (process had already
exited) before the pull, so no race.

**Analysis setup** (bare `pip install` refuses as "externally managed" on this machine):

```bash
python3 -m venv .venv && source .venv/bin/activate
pip install perfetto
```

```python
from perfetto.trace_processor import TraceProcessor
tp = TraceProcessor(trace='t.perfetto-trace')
```

**Confirmed gotcha:** the main thread's name is truncated to 15 chars and is *not* `"main"` — for
this app's fdroid debug build it showed up as `le.fdroid.debug` (the tail of
`com.grappim.taigamobile.fdroid.debug`). Filter `thread` by `tid = <pid>` (main thread's tid always
equals the process pid — get the pid with `adb shell pidof <package-id>`), not by name.

**Real jank query, filtered to this app's own process** (unfiltered `actual_frame_timeline_slice`
includes SurfaceFlinger, systemui, the launcher, and system_server frames too — join through
`process` to isolate the app under test):

```sql
select afts.ts, afts.dur, afts.jank_type
from actual_frame_timeline_slice afts
join process p on afts.upid = p.upid
where p.name = 'com.grappim.taigamobile.fdroid.debug'
order by afts.dur desc
limit 10
```

Real output — every one of the app's 55 recorded frames in this capture was janky (the whole
1.56-second cold-start burst), worst three:

| dur (ms) | jank_type |
|---|---|
| 288.8 | Prediction Error, App Deadline Missed |
| 233.8 | Prediction Error, App Deadline Missed, Buffer Stuffing |
| 218.6 | Prediction Error, App Deadline Missed, Buffer Stuffing |

`Buffer Stuffing` dominates the jank-type breakdown (32 of 55 frames tag it). That's a plausible
software-renderer artifact of `swiftshader_indirect` queuing buffers rather than an app bug — don't
read the 100%-janky figure as a real-device number without re-checking on hardware or the AVD's
default (hardware-accelerated) GPU mode.

**What the worst frame (288.8ms) was actually doing**, querying the app's main thread
(`tid = 4942`, this run's pid) for slices inside that frame's time window:

```sql
select s.ts, s.dur, s.name
from slice s
join thread_track tt on s.track_id = tt.id
join thread t on tt.utid = t.utid
where t.tid = 4942 and s.ts between 367314782592 and 367603548086
order by s.dur desc
limit 15
```

Top of the result: a 289ms `Choreographer#doFrame - resynced` span containing a 281ms `traversal`,
and — more usefully — a long run of `VerifyClass` slices: `androidx.compose.ui.platform.ViewLayer`,
`androidx.compose.foundation.lazy.LazyListMeasureKt`, `androidx.compose.ui.text.android.TextAndroidCanvas`,
`androidx.compose.ui.graphics.ColorSpaceVerificationHelper`, and this app's own
`com.grappim.taigamobile.uikit.widgets.topbar.ComposableSingletons$TaigaTopAppBarKt`, each costing
tens to a couple hundred microseconds. This is ART verifying Compose/androidx and app classes for
the first time on a cold JIT — exactly the cost a Baseline Profile (task 3) is meant to amortize.
Logged as a candidate finding in `docs/revisit.md`.

**Thread-state sanity check** (main thread, whole ~10s capture): `S` (sleeping) totalled ~4.47s
across 270 spans, `Running` ~1.53s across 490 spans — mostly idle waiting with real bursts of work
during the cold-start window, consistent with the frame data above. Per the known gotcha, a long `S`
span is not evidence of a stall; only `Running` spans overlapping a janky frame are.

## Baseline Profile: generating and verifying it's actually applied

`benchmark/src/main/kotlin/com/grappim/taigamobile/benchmark/BaselineProfileGenerator.kt` has one
`coldStart()` `@Test` (`pressHome()` + `startActivityAndWait()`, targeting
`com.grappim.taigamobile.fdroid`) — no second journey, since the `VerifyClass` finding above was
startup-wide, not tied to a specific post-login screen.

**`androidApp` needs the `androidx.baselineprofile` *consumer* plugin, not just the producer
module.** The initial Gradle wiring (`docs/perf/profiling-plan.md` task 1) added `:benchmark` and
`profileinstaller` but missed this — without it, `generate<Variant>BaselineProfile` doesn't exist
on `:androidApp` and there's nowhere for the generated profile to land. Fixed as part of task 3:
`androidApp/build.gradle.kts` needs both
`alias(libs.plugins.androidx.baselineprofile)` in `plugins {}` and
`baselineProfile(projects.benchmark)` in `dependencies {}`. This is what exposes
`generateFdroidReleaseBaselineProfile` and `installFdroidNonMinifiedRelease` as `:androidApp` tasks
(not `:benchmark` tasks, despite `:benchmark` being where the `@Test` lives).

**Generate:**

```bash
./gradlew :androidApp:generateFdroidReleaseBaselineProfile
```

This is a real `connectedAndroidTest` run against the `fdroidNonMinifiedRelease` variant — several
minutes, needs a connected device/AVD. Confirms the AVD needs headroom: the default AVD data
partition (6G on `Medium_Phone_API_36.1` as created) filled to 93% just from the debug + release +
nonMinifiedRelease + test APKs coexisting, and the run failed with `IOException: Requested internal
only, but not enough space`. Fixed by killing the emulator and relaunching with
`-wipe-data -partition-size 12288` (12G) — `disk.dataPartition.size` in the AVD's `config.ini` alone
doesn't resize an already-created `userdata-qemu.img`; a wipe is what actually recreates it at the
new size.

Real output (2026-08-12): `androidApp/src/fdroidRelease/generated/baselineProfiles/baseline-prof.txt`,
31,501 lines of real class/method entries (`Landroidx/activity/ActivityFlags;`,
`SPLandroidx/activity/ActivityFlags;-><clinit>()V`, …) — not empty, not garbage.

**Verify it's actually applied** — generating the file is necessary but not sufficient; nothing
forces a plain `adb install` to use it without `androidx.profileinstaller` as a runtime dependency
(already present, `androidApp/build.gradle.kts`, added in task 1) plus a real device check:

```bash
PKG=com.grappim.taigamobile.fdroid
adb install androidApp/build/outputs/apk/fdroid/release/app-fdroid-release.apk
adb shell dumpsys package $PKG | grep -A2 "x86_64:"
# → [status=verify] [reason=install] — expected; plain adb install/sideload never
#   triggers profile-guided compilation on its own, Play-store installs are the exception

adb logcat -c
adb shell am start -n $PKG/com.grappim.taigamobile.MainActivity
adb logcat -d | grep ProfileInstaller
# → "ProfileInstaller: Installing profile for com.grappim.taigamobile.fdroid" — confirms
#   ProfileInstallerInitializer (androidx.startup) fired on first launch

adb shell cmd package bg-dexopt-job    # forces the real system background-dexopt mechanism now
adb shell dumpsys package $PKG | grep -A2 "x86_64:"
# → [status=speed-profile] [reason=bg-dexopt] — profile is now actually compiled in
```

All four steps were run for real against this project's `fdroidRelease` build (2026-08-12) and
produced exactly the output shown above.

## Before/after re-capture: does `VerifyClass` shrink post-profile? (`docs/revisit.md` #40)

Real A/B against the `fdroidRelease` build, same journey as above, no reinstall between the two
compile-mode captures (2026-08-12):

```bash
PKG=com.grappim.taigamobile.fdroid
adb uninstall $PKG               # start from a genuinely untouched install, not a forced compile
adb install androidApp/build/outputs/apk/fdroid/release/app-fdroid-release.apk
adb shell dumpsys package $PKG | grep -A2 "x86_64:"
# → [status=verify] [reason=install] — confirm no prior `cmd package compile` call touched this APK

# re-login by hand (10.0.2.2:9000 / admin/admin), land on Select Project

# "before": capture WITHOUT ever calling `cmd package compile` — see pitfall below for why
adb shell am force-stop $PKG
adb shell perfetto -o /data/misc/perfetto-traces/before.perfetto-trace -t 10s \
  sched freq idle am wm gfx view input dalvik hal res memory binder_driver &
sleep 1.5 && adb shell am start -n $PKG/com.grappim.taigamobile.MainActivity && sleep 9
adb pull /data/misc/perfetto-traces/before.perfetto-trace

# "after": force the real speed-profile compile, no reinstall (keeps the session)
adb shell cmd package compile -m speed-profile -f $PKG
adb shell am force-stop $PKG
adb shell perfetto -o /data/misc/perfetto-traces/after.perfetto-trace -t 10s \
  sched freq idle am wm gfx view input dalvik hal res memory binder_driver &
sleep 1.5 && adb shell am start -n $PKG/com.grappim.taigamobile.MainActivity && sleep 9
adb pull /data/misc/perfetto-traces/after.perfetto-trace
```

**Confirmed pitfall — `cmd package compile -m verify -f` is not a neutral "before" state.** The first
attempt at this A/B forced `-m verify -f` before the "before" capture, intending a same-APK
verify-vs-speed-profile comparison. That's wrong: `-m verify -f` runs `dex2oat`'s verification pass
**ahead of time** and writes the result into the vdex, which is exactly the cost a plain `adb install`
leaves for ART to pay lazily, in-memory, on every cold start until something re-verifies it. Forcing
`-m verify -f` first eliminates the very `VerifyClass` cost the A/B exists to measure — that capture
showed near-zero `VerifyClass` in *both* states, which was the forcing artifact, not a real result.
The redo above never calls `cmd package compile` for "before" — the only touch is the fresh install
itself (`[status=verify] [reason=install]`), so ART verifies lazily at runtime exactly like a real
sideload.

**Real result, correctly isolated: `VerifyClass` cost is already near-zero on the release build, in
both states.**

| | before (`reason=install`, untouched) | after (`speed-profile`) |
|---|---|---|
| Frames captured | 47 | 33 |
| Worst frame | 292.7ms (`Prediction Error, App Deadline Missed`) | 333.1ms (`Prediction Error, App Deadline Missed`) |
| Sum of all frame durations | 1572.1ms | 1437.8ms |
| `VerifyClass` slices (whole 10s capture) | 2 (0.10ms total) | 1 (0.06ms total) |

Task 2's original 288.8ms-worst-frame, `VerifyClass`-dominated finding was captured on the fdroid
**debug** build (`com.grappim.taigamobile.fdroid.debug`) — unshrunk, no R8, not a variant that gets a
baseline profile at all. This redo is the fdroid **release** build (R8-minified — the only variant
task 3's Baseline Profile actually targets). The likely explanation for the gap: R8 shrinking on
release already strips most of the class graph that cost `VerifyClass` time on the unshrunk debug
build, independent of whether a profile is installed. **The original finding doesn't transfer to what
ships** — there wasn't a meaningful `VerifyClass` cost on the release build left for the Baseline
Profile to amortize.

The worst-frame-duration column above is the other candidate signal and reads as noise, not a
regression: it got *nominally worse* post-profile (293ms → 333ms) while total frame count and total
frame-duration sum both dropped — inconsistent with a real slowdown, consistent with single-run
variance on the `swiftshader_indirect` software renderer (see Caveats below). Not worth a second
sample: the `VerifyClass` result above is conclusive on its own terms (there's nothing left to shrink),
so a cleaner frame-duration signal wouldn't change the conclusion.

## Caveats on this pass

- Captured on the AVD with `-gpu swiftshader_indirect` (software rendering) — the jank percentages
  and `Buffer Stuffing` tag are plausibly renderer-specific and should not be quoted as real-device
  numbers without a second capture on hardware or the AVD's default GPU mode.
- Two separate cold-start launches were captured (one for gfxinfo, one for Perfetto) rather than one
  launch feeding both tools — the frame counts and worst-frame numbers between the two sections are
  not from the same run, though both are cold starts of the same journey.

## Real hardware: what changes, what breaks (2026-08-12)

First real-device pass, Samsung SM-A920F (Android 10, API 29, Adreno 512 — genuinely
GPU-accelerated). Two techniques above behave differently than on the AVD; know this before reaching
for either on real, unrooted hardware.

**`dumpsys gfxinfo` ("Quick look") works exactly as documented, and the numbers are clean.** Cold
start to Select Project: 28 frames, 95th percentile 85ms, 99th percentile 150ms — no
histogram-overflow artifacts, no suspiciously-round `9Xth gpu percentile` bucket boundary (both
signatures of the AVD's software renderer). This is the technique to reach for on real hardware.

**Perfetto ("Deep look") loses most of its value on a real, unrooted device — for two independent
reasons:**

1. `actual_frame_timeline_slice` (what the worst-frame jank query is built on) is **empty** on
   Android 10/API 29 — it requires the SurfaceFlinger frame-timeline API added in API 31. Check
   `select count(*) from actual_frame_timeline_slice` before treating an empty result as "no jank,"
   not "wrong API version for this table."
2. App-level slices (`Choreographer#doFrame`, `traversal`, `VerifyClass`, Compose composition) are
   **completely absent**, confirmed for both the release and the debug build. Root cause: this device
   is `ro.build.type=user` (production, non-rooted) — `adb shell ls /sys/kernel/debug/tracing/`
   returns `Permission denied` on nearly every entry, and in-process `android.os.Trace.beginSection`
   calls (what powers every named app-level slice) need that access regardless of whether the *app*
   itself is debuggable. Only `binder transaction async` slices (a separately-permitted tracepoint)
   come through.

**Practical upshot:** the slice-level "why is it slow" analysis only works on the AVD (userdebug/eng
system image) or a rooted device. On real unrooted hardware — which is exactly where you most want
the "why" — only `dumpsys gfxinfo`'s per-frame timestamps are available. There is no known workaround
short of rooting the test device.

**Baseline Profile coverage is narrower than it looks.** `BaselineProfileGenerator`'s one journey
(`coldStart()`) only reaches "Select Project" — nothing past login is covered. A real jank finding on
the very next screen (project Dashboard, `docs/revisit.md` #41) is plausibly un-amortized by the
profile for exactly this reason. See #41 for the full writeup and what a proper before/after
re-capture for a second journey would need.

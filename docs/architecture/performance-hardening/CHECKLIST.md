# Performance Hardening — Checklist

**Progress:** 2/3 done. Step 3 (macrobenchmark CI) not started — see its entry below.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the source article, the codebase evidence
behind each finding, and the findings that were assessed and declined. See
[CHECKLIST-DONE.md](CHECKLIST-DONE.md) for ticked steps.

## Step 3: Prototype macrobenchmark/Perfetto trend-tracking CI end-to-end

Source: step 1's finding (b) — see IMPLEMENTATION_PLAN.md's "(b) Perfetto / Macrobenchmark trace
metrics on a schedule". Step 2 (this phase's other half, APK size delta on PRs) is done; this is
the harder, still-undecided half of the same original ask.

Three sub-questions step 1 sketched but did not resolve, in the order they'd need tackling:
1. Prototype the session-seeding workaround end-to-end on a real emulator (write unprefixed
   plaintext token/refresh-token values into `auth_storage.preferences_pb` via the real
   `PreferenceDataStoreFactory`/`edit{}` APIs, confirm `AuthStorageImpl.isLoggedIn`/`getToken()`
   accept them) — including checking whether the `benchmark` module's `nonMinifiedRelease` build
   type is `run-as`/`adb root`-accessible, which step 1 flagged as unverified.
2. Confirm `reactivecircus/android-emulator-runner` actually boots KVM-accelerated on this repo's
   GitHub-hosted runners (sketched as "viable in principle," not tried here).
3. Decide a concrete threshold/trend-detection rule for the tracked metric JSON (step 1 explicitly
   left this undecided) and where the historical series lives (workflow artifact vs. a committed
   append-only log).

**Verify:** an end-to-end emulator run producing a real `*-benchmarkData.json` from a seeded,
logged-in session on CI infra (or on a local emulator standing in for it), plus a working
nightly/manual-dispatch workflow if the prototype holds up.

Findings assessed and declined — see IMPLEMENTATION_PLAN.md for detail, no further action:
- R8/ProGuard minification + resource shrinking on release builds — already enabled
  (`AndroidApplicationConventionPlugin.kt:63-64`).
- Dynamic Feature Modules (Play Feature Delivery) — Android-only mechanism, doesn't fit this
  project's KMP `feature/*` module structure shared across Android/iOS/Desktop.
- Overdraw / deep XML layout hierarchies, `ConstraintLayout` — not applicable, this project is
  100% Jetpack Compose (CLAUDE.md), the article targets the legacy View system.
- Manual listener/callback cleanup in `onStop`/`onDestroy`, `CameraX`/`MediaPlayer` release — no
  matching usage in this codebase (`CameraX`, `MediaPlayer`, `androidx.camera` all grep to zero
  hits); Compose's `DisposableEffect` is the idiomatic equivalent where needed.
- Offload heavy work off the main thread via coroutines — already the architecture-wide norm
  (Koin + coroutines), not a gap this article surfaces.
- Batch network operations to avoid radio wake-ups — overlaps with
  `docs/architecture/vm-lifecycle-hardening/`'s finding 9 (redundant `init`-time re-fetches);
  doesn't add anything beyond that already-queued investigation.

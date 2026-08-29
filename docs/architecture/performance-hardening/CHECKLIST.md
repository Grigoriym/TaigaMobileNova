# Performance Hardening — Checklist

**Progress:** 0/1 done. **Current step:** none active — step 1 is not started.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the source article, the codebase evidence
behind the finding, and the findings that were assessed and declined.

1. Investigate: CI regression checks for APK size and Perfetto/Macrobenchmark trace metrics
   - Not gated — this is an investigation step, not a commitment to add either check.
   - Claim to check: an "Android Performance Tuning" newsletter article's size/perf checklist,
     refined by gregory into a concrete ask — can APK size and startup-trace metrics be tracked
     automatically per PR (or on a schedule) to catch degradation over time, rather than only via
     the manual `docs/perf/profiling.md` capture process run ad hoc?
   - Two distinct sub-checks, deliberately separated because their feasibility differs a lot — see
     IMPLEMENTATION_PLAN.md's "CI regression checks" section for the full reasoning:
     - **(a) APK size delta on PRs.** Likely cheap: `build.yml` already assembles fdroid/gplay
       debug APKs on every PR; a debug-vs-base-branch size diff is a viable first cut. A
       release-accurate check would reuse the already-enabled R8/shrink-resources release build
       type, but needs the release signing secrets `build.yml` declares but doesn't currently
       restore to a file — and fork-originated PRs won't have repo secrets under `pull_request`
       triggers regardless, so any release-accurate path needs a same-repo-PR fallback story.
     - **(b) Perfetto/Macrobenchmark trace metrics on a schedule.** Likely too heavy/flaky for
       every PR: needs a real emulator, and this app's login-required flow (no anonymous path)
       means a fresh CI emulator can't reach the `:benchmark` module's existing `coldStart()`
       journey without either scripting a real login or seeding a pre-authenticated session onto
       the emulator — neither investigated yet. Refined recommendation is a scheduled/nightly job
       tracking `androidx.benchmark`'s own structured JSON output over time, not a per-PR gate.
   - Not started — no prototype workflow written, no session-seeding mechanism investigated, no
     confirmation that `diffuse` (or an equivalent tool) still fits current AGP output.

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

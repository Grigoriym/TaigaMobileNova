# Performance Hardening — Checklist

**Progress:** 1/1 done. All checklist steps complete — see CHECKLIST-DONE.md. This file now only
holds the findings assessed and declined below.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the source article, the codebase evidence
behind the finding, and the findings that were assessed and declined. See
[CHECKLIST-DONE.md](CHECKLIST-DONE.md) for the ticked step.

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

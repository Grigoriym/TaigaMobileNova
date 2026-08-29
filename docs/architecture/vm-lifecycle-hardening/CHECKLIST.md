# ViewModel Lifecycle & Error-Handling Hardening — Checklist

**Progress:** 8/8 done. All checklist steps complete — see CHECKLIST-DONE.md. This file now only
holds the findings assessed and declined below.

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the source articles, the codebase
evidence behind each finding, and the findings that were assessed and declined. See
[CHECKLIST-DONE.md](CHECKLIST-DONE.md) for ticked steps.

Findings assessed and declined — see IMPLEMENTATION_PLAN.md for detail, no further action:
- Concurrent independent loads fired from `init` (the "Startup-Intent" article's flaky-test claim)
  — confirmed 8 ViewModels do this, but the current test convention (final `.state.value` snapshot
  assertions + `MainDispatcherRule`'s unconfined dispatcher) already sidesteps the flakiness the
  article describes.
- `AppInfoProvider.isDebug()` runtime facade (the "debug code in release builds" article) — matches
  the flagged anti-pattern, but the article's fix (Android build-type source sets) doesn't
  generalize across this project's iOS/JVM targets.
- State design (data class, not sealed) — already matches the article's recommended convention.
- The Startup-Intent pattern, the Startup Task multibinding pattern, "MVP vs MVVM vs MVI", and the
  custom FIFO/`UiStateMachine`/`MviViewModel` machinery from "Why your MVI can't handle two Intents
  at once" — not applicable, they presuppose an MVI pipeline or Hilt multibinding this project
  doesn't use. That last article's underlying race-condition claim is not MVI-specific though — it
  produced its own actionable finding, step 4 above.

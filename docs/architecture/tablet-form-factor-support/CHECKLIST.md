# Tablet and Other Form Factor Support — Checklist

**Progress:** 3/3 done. **Current step:** 4 — wire `NavigationSuiteScaffold` into
`MainScreen.kt` / `TaigaDrawerWidget.kt` (not started).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next. Done steps move to
[CHECKLIST-DONE.md](CHECKLIST-DONE.md).

## Step 4: Wire `NavigationSuiteScaffold` into `MainScreen.kt` / `TaigaDrawerWidget.kt`

Implement the mapping step 3 chose (see CHECKLIST-DONE.md and IMPLEMENTATION_PLAN.md's "Step 3
decision"): at compact width keep `TaigaDrawerWidget`'s `ModalNavigationDrawer` exactly as today;
at medium/expanded width, render `NavigationSuiteScaffold` with a flattened item list (groups
unwrapped to their `Destination`s, divider and app-name header dropped). Replace `MainScreen.kt`'s
unconditional wrap in `TaigaDrawerWidget` with a width check (`currentWindowAdaptiveInfo()` →
`NavigationSuiteType`) selecting between the two rendering paths. Preserve existing behavior:
`currentTopLevelDestination` selection highlighting, `onDrawerItemClick` navigation (including the
Logout confirmation dialog path), and `gesturesEnabled`/back-handler behavior for whichever layout
still uses a real `DrawerState` (permanent drawer/rail don't need swipe-to-open or a back handler
the same way modal does — recheck `NavigationBackHandler` in `MainScreen.kt:193` still applies once
the modal-only path is gone).

**Verify:** `./gradlew jvmTest`, `ktlintCheck`, then emulator-testing skill — drive the app on a
phone-sized emulator (confirms bottom bar / modal still works) and a tablet/large-screen emulator
or a resized desktop window (confirms rail or permanent drawer appears) per CLAUDE.md's
Verification rule.

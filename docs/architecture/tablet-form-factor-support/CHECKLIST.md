# Tablet and Other Form Factor Support — Checklist

**Progress:** 1/1 done. **Current step:** 2 — add the `material3-adaptive-navigation-suite`
dependency (not started).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next. Done steps move to
[CHECKLIST-DONE.md](CHECKLIST-DONE.md).

## Step 2: Add the `material3-adaptive-navigation-suite` dependency

Add `org.jetbrains.compose.material3:material3-adaptive-navigation-suite`, pinned to the same
version as this repo's `jetbrainsComposeMaterial3` (currently `1.10.0-alpha05` — confirmed
KMP-published for Android/Desktop/iOS at that version, see IMPLEMENTATION_PLAN.md), to
`gradle/libs.versions.toml` and `composeApp/build.gradle.kts`'s `commonMain` dependencies only —
not the shared `KmpCompose.kt` convention plugin, since no other module needs it. No UI code
changes in this step.

**Verify:** `./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`,
`:composeApp:compileKotlinIosArm64 --rerun-tasks`, `:androidApp:compileFdroidDebugKotlin
--rerun-tasks`, and `:composeApp:compileKotlinJvm` all green.

## Step 3: Design the `DrawerItem` → `NavigationSuiteScope` item mapping

⛔ **Gated — do not start without asking.** `NavigationSuiteScaffold`'s `navigationSuiteItems`
lambda only supports flat items (icon/label/selected/onClick) — no header, group-label, or divider
slot (confirmed 2026-08-15, see IMPLEMENTATION_PLAN.md). The current `TaigaDrawerWidget` has an
app-name header, `DrawerItem.Group` sections with labels, `DrawerItem.Divider`s, and a Logout
entry, none of which maps 1:1. This step is design-only — no code changes — and produces a written
decision in IMPLEMENTATION_PLAN.md for step 4 to implement. Candidate compromises to weigh (not
exhaustive):

- Flatten all groups into one item list for the rail/permanent-drawer layout, dropping group
  labels; keep the full grouped `ModalNavigationDrawer` only at compact width (phone/narrow).
- Keep group labels by rendering multiple adjacent `NavigationSuiteScaffold` "sections" if the API
  allows composing more than one, or by accepting a plain divider-less flat list with no headers
  at all.
- Something gregory prefers after seeing the tradeoff — this needs their input, not a unilateral
  pick.

**Verify:** IMPLEMENTATION_PLAN.md records the chosen mapping and why; no build/test verification
applies to a design-only step.

## Step 4: Wire `NavigationSuiteScaffold` into `MainScreen.kt` / `TaigaDrawerWidget.kt`

⛔ **Gated — do not start without asking.** Depends on step 3's decision (not yet made) and step 2
(not yet done).

Implement the mapping step 3 chose. Replace `TaigaDrawerWidget`'s unconditional
`ModalNavigationDrawer` wrap in `MainScreen.kt` with `NavigationSuiteScaffold`, so
`NavigationSuiteType` (derived from `currentWindowAdaptiveInfo()`) picks bottom bar / rail /
permanent drawer automatically. Preserve existing behavior: `currentTopLevelDestination` selection
highlighting, `onDrawerItemClick` navigation (including the Logout confirmation dialog path), and
`gesturesEnabled`/back-handler behavior for whichever layout still uses a real `DrawerState`
(permanent drawer/rail don't need swipe-to-open or a back handler the same way modal does — recheck
`NavigationBackHandler` in `MainScreen.kt:193` still applies once the modal-only path is gone).

**Verify:** `./gradlew jvmTest`, `ktlintCheck`, then emulator-testing skill — drive the app on a
phone-sized emulator (confirms bottom bar / modal still works) and a tablet/large-screen emulator
or a resized desktop window (confirms rail or permanent drawer appears) per CLAUDE.md's
Verification rule.

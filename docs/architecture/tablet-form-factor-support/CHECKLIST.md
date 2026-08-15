# Tablet and Other Form Factor Support — Checklist

**Progress:** 5/12 done. **Current step:** 6 — add Nav3 dependencies + `NavKey` serializers
(not started).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next, followed by option 3's
Navigation 3 migration (steps 6–12, decomposed 2026-08-15 from the "Recommended path forward" in
IMPLEMENTATION_PLAN.md's "Navigation 3 migration investigation" section). Option 2 (steps 2–4) is
fully implemented. Steps 6–11 are the migration itself (mechanical, no open decisions blocking
them); step 12 (the actual list-detail pane layout, option 3's real payoff) is gated — see its
entry below. Done steps move to [CHECKLIST-DONE.md](CHECKLIST-DONE.md).

## Step 6: Add Nav3 dependencies + `NavKey` serializers

Add the Nav3 artifacts (`org.jetbrains.androidx.navigation3:navigation3-ui` and
`org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3`, matching wallosmobile's
`jetbrainsNav3` version) to `gradle/libs.versions.toml` and `composeApp/build.gradle.kts`. Add a
KMP `SerializersModule`/`SavedStateConfiguration` registering all ~31 existing route classes as
polymorphic `NavKey` subtypes, following wallosmobile's `NavKeySerializers.kt` pattern (non-JVM
targets need this explicitly — no reflection-based serialization on iOS). This step only adds
code that compiles alongside the existing Nav2 `NavHost` — no behavior change, nothing wired in
yet.

**Verify:** `./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks`,
`:composeApp:compileKotlinIosArm64 --rerun-tasks`, `:androidApp:compileFdroidDebugKotlin
--rerun-tasks`, `:composeApp:compileKotlinJvm` — all green, app behavior unchanged (nothing to
emulator-test yet).

## Step 7: Convert routes to `NavKey` + build the `Navigator`/`NavigationState` shell

Make each of the ~31 `@Serializable` route classes implement `NavKey` (mechanical, one line per
file). Build a `Navigator`/`NavigationState`-equivalent (wallosmobile's `core/navigation/` pattern
— dual back-stack-of-stacks, one per drawer section) either as a new `core:navigation` module or a
`composeApp`-local file; decide which by how much of wallosmobile's module is reusable as-is vs.
needs adapting to this repo's route set. Still not wired into the running app — `MainNavHost.kt`
keeps using classic `NavHost` until step 10.

**Verify:** same four-target compile matrix as step 6, plus `./gradlew jvmTest` covering any new
`Navigator`/`NavigationState` unit tests (follow the failure-path convention in CLAUDE.md's
Testing section for its public methods).

## Step 8: Port ViewModels off `savedStateHandle.toRoute<T>()`

Move all 15 affected ViewModels from `SavedStateHandle.toRoute<T>()` extraction to
constructor-parameter injection via Koin `@InjectedParam` + `parametersOf(route)`, matching
wallosmobile's pattern (Nav3 hands the route object straight to the screen, not through
`SavedStateHandle`). Mechanical per-ViewModel but not risk-free — if this proves too large for one
context, split it into multiple sessions by feature module rather than doing it as one giant diff.

**Verify:** `./gradlew jvmTest` (full repo run) and `ktlintCheck` green. `koin-expert` agent should
confirm the `@InjectedParam` wiring resolves — run `KoinGraphTest`
(`composeApp/src/jvmTest/`) explicitly, since a `@InjectedParam` mismatch is exactly the kind of
DI wiring gap that test exists to catch.

## Step 9: Port `UPDATE_DATA_ON_BACK` result-passing to the Nav3 event-bus recipe

Replace the current `UPDATE_DATA_ON_BACK` savedStateHandle-based result-passing convention with
Nav3's event-bus recipe (see the `android-skills:navigation-3` skill and/or wallosmobile if it has
an equivalent). This is the one genuinely non-mechanical piece of the migration — call out any
screen where the "did the board change, should I refresh" signal doesn't map cleanly, rather than
forcing a fit.

**Verify:** `./gradlew jvmTest`, `ktlintCheck`, plus emulator-testing skill — specifically drive a
kanban/sprint board round-trip (edit a task, back out, confirm the board refreshes) since that's
the concrete user-visible behavior this convention protects.

## Step 10: Replace `NavHost`/`composable<T>` with `NavDisplay`/`entry<T>` — the cutover

⛔ **Gated — do not start without asking.** Depends on steps 6–9 all being done first, and this is
the "big bang" step where everything lands together (the migration guide has no supported
Nav2/Nav3 coexistence path) — confirm with gregory before starting given the blast radius.

Replace `NavHost`/`composable<T>`/`NavGraphBuilder` extensions with
`NavDisplay`/`entry<T>`/`EntryProviderScope<NavKey>` extensions across all 8 nav-graph files plus
`MainNavHost.kt`. Delete the Nav2 dependencies once nothing references them.

**Verify:** `./gradlew jvmTest`, `ktlintCheck`, all four target compiles, then full emulator
verification across every top-level section (not a sample) per CLAUDE.md's Verification rule —
this step touches navigation for the entire app.

## Step 11: Rewire adaptive-chrome selection state to `NavigationState`

Update `TaigaDrawerWidget.kt`/`MainScreen.kt`'s `currentTopLevelDestination` to read from
`navigationState.currentTopLevelKey` instead of the Nav2 back stack. Re-run step 4's emulator
scenarios (compact modal drawer selection highlighting, medium/expanded rail selection
highlighting) to confirm option 2's work still behaves correctly after the Nav3 cutover.

**Verify:** emulator-testing skill, same two AVDs and scenarios as step 4's `Verify:` entry in
CHECKLIST-DONE.md.

## Step 12: Add `ListDetailSceneStrategy` for list-detail two-pane layouts

⛔ **Gated — do not start without asking.** Depends on steps 6–11 (full Nav3 migration) being
done, and on two open questions from IMPLEMENTATION_PLAN.md's "Navigation 3 migration
investigation" section that gregory hasn't answered yet:

- Whether the KMP `adaptive-navigation3` artifact (`1.3.0-beta02`) actually exports
  `ListDetailSceneStrategy` with API parity to the Android-only recipe — unconfirmed, worth a short
  spike before scoping this step in detail.
- Which screens get list-detail treatment — Kanban/Sprint board + task detail is the obvious
  candidate but not decided; this is a design decision (see step 3's precedent), not an
  engineering one.

This is option 3's actual payoff — everything in steps 6–11 is infrastructure with no user-visible
change.

**Verify:** TBD once scoped — will need both `jvmTest`/`ktlintCheck` and emulator verification on
a tablet/wide-desktop AVD, per the pattern of every UI-visible step above.

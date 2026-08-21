# Tablet and Other Form Factor Support — Checklist

**Progress:** 11/12 done. **Current step:** 12a — fix the `ResultBus` collision for the Issues
pairing (gated, not started).

See [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) for the survey, the scope options, and the
2026-08-15 decision to pursue option 2 (adaptive navigation chrome) next, followed by option 3's
Navigation 3 migration (steps 6–12, decomposed 2026-08-15 from the "Recommended path forward" in
IMPLEMENTATION_PLAN.md's "Navigation 3 migration investigation" section). Options 2 and 3's
migration (steps 2–11) are fully implemented; step 12 (the actual list-detail pane layout, option
3's real payoff) is gated — see its entries below. Done steps move to
[CHECKLIST-DONE.md](CHECKLIST-DONE.md).

Step 12 was decomposed 2026-08-21 into 12a/12b/12c (one sub-task per session, per gregory's
preference) once its blocking questions were resolved: API parity confirmed, screen pairing
decided (Issues-only MVP), and the `ResultBus` collision pre-scoped. All three all-scoping notes
below are carried over verbatim from the single step-12 entry — nothing about the underlying
decisions changed, only the step boundaries.

- ~~Whether the KMP `adaptive-navigation3` artifact (`1.3.0-beta02`) actually exports
  `ListDetailSceneStrategy` with API parity to the Android-only recipe~~ — **resolved 2026-08-21,
  confirmed yes** (decompiled the jar; full API parity, real per-target variants for Android/JVM/
  iosArm64/iosSimulatorArm64, `navigation3-ui` version compatible). See IMPLEMENTATION_PLAN.md's
  "Navigation 3 migration investigation" section for the verification detail.
- ~~Which screens get list-detail treatment~~ — **decided 2026-08-21: Issues only, as the MVP**
  (`IssuesNavDestination` + `IssueDetailsNavDestination`). See IMPLEMENTATION_PLAN.md's "Step 12
  screen-pairing decision".
- ~~Step 9's hand-rolled `ResultBus` (`core/navigation/.../ResultBus.kt`) assumes only one
  destination is ever actively composed at a time~~ — **confirmed 2026-08-21: it breaks.**
  `ListDetailSceneStrategy` keeps the list pane's `ResultEffect<UpdateDataOnBack>` continuously
  alive alongside the detail pane, so a signal sent from two levels deep in the detail column
  (e.g. Kanban[list] → Issue[detail] → UserStory → back) races between two listeners on the same
  global key instead of reaching the one it was meant for. Fix is a call-site change (per-pairing
  result keys instead of one shared marker), not a `ResultBus` rewrite. See
  IMPLEMENTATION_PLAN.md's "Step 12 pre-scoping: the `ResultBus` collision, investigated
  (2026-08-21)" — step 12a below implements the fix it recommends.

This is option 3's actual payoff — everything in steps 6–11 is infrastructure with no user-visible
change.

## Step 12a: Fix the `ResultBus` collision for the Issues pairing

⛔ **Gated — do not start without asking.**

Give `IssuesNavDestination`'s list-refresh listener and `IssueDetailsNavDestination`'s own
self-refresh listener distinct result-key marker types, instead of both sharing the global
`UpdateDataOnBack` object — option 1 from IMPLEMENTATION_PLAN.md's "Step 12 pre-scoping"
section (cheap, local diff, no `ResultBus.kt` mechanism change). Touch only
`IssueNavGraph.kt`'s two `ResultEffect<UpdateDataOnBack>` registrations (list-refresh listener,
detail self-refresh listener) and its one `sendResult` call site (`IssueDetailsNavDestination`'s
`goBack`). The other five nav-graph files (`MainNavHost.kt`, `ScrumNavGraph.kt`,
`EpicNavGraph.kt`, `UserStoryNavGraph.kt`, `TaskNavGraph.kt`) keep using the shared
`UpdateDataOnBack` key — out of scope for this MVP.

This step lands *before* 12b adds `ListDetailSceneStrategy`, so on its own it's a no-visible-
behavior-change refactor — the collision it fixes isn't reachable yet in single-pane mode. It's a
prerequisite so 12b doesn't reintroduce the race the moment two-pane goes live.

**Verify:** `jvmTest` + `ktlintCheck`; manually confirm the Kanban/Issues list still refreshes
correctly after opening an Issue's detail and navigating back (regression check — behavior must be
unchanged from before this step).

## Step 12b: Wire `ListDetailSceneStrategy` for Issues list-detail

⛔ **Gated — do not start without asking.** Depends on 12a being done (otherwise the pairing this
step makes concurrently-composed is exactly the one with the collision).

Add `rememberListDetailSceneStrategy<NavKey>()` to `NavDisplay`'s `sceneStrategies` in
`MainNavHost.kt`. Tag `IssuesNavDestination`'s `entry<T>` as
`ListDetailSceneStrategy.listPane(...)` and `IssueDetailsNavDestination`'s as
`ListDetailSceneStrategy.detailPane(...)` via each entry's `metadata` parameter, per the recipe in
IMPLEMENTATION_PLAN.md's `ListDetailSceneStrategy` notes. No other nav-graph file changes.

**Verify:** `jvmTest` + `ktlintCheck`; confirm Android and Desktop targets compile
(`:androidApp:assembleFdroidDebug`, `:composeApp:run`).

## Step 12c: Emulator-verify the Issues two-pane layout on a tablet AVD

⛔ **Gated — do not start without asking.** Depends on 12b being done.

Using the **emulator-testing** skill and this project's tablet/wide AVD (see
`docs/EMULATOR_TESTING.md`): confirm the Issues list and detail panes render side by side on a
wide screen, single-pane on a phone-width screen/AVD, and that the scenario which originally
exposed the `ResultBus` race — Kanban/Issues[list] → Issue[detail] → UserStory[pushed in the
detail column] → back — now refreshes deterministically instead of racing.

**Verify:** manual pass on the tablet AVD (or a connected tablet) plus a phone-width AVD;
screenshots of both pane arrangements attached to the step's `Note:` when archived.

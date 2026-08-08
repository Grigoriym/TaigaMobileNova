# Testing suite: deferred and considered items

**Split out:** 2026-08-08, when [improvement-plan.md](improvement-plan.md) closed (tasks 0–21 all
done, its status table empty). That plan was a sequenced todo list; this doc is not — it's a
standing record of ideas that were surveyed and intentionally *not* actioned, so they aren't
silently forgotten and aren't re-proposed from scratch without the context already gathered here.

Some rows are dropped for good (struck through, reasoning kept). Others are open — pick one up as
its own task/branch when there's a concrete reason to, the same way tasks 10–21 were scoped one at
a time off `improvement-plan.md`'s own Result notes.

| Idea | Status |
|---|---|
| ~~Run `commonTest` on a native target (`iosSimulatorArm64Test`)~~ | **Dropped (gregory, 2026-08-07)** — not just deferred, off the list. Real gap (no `expect/actual` divergence is caught by tests today), but CI is `ubuntu-latest` and this needs a macOS runner at roughly 10× the minutes. Worst value-per-cost on the list. |
| Screenshot tests | **Open — next up is Paparazzi, in a separate task/branch (gregory, 2026-08-08).** See the full history below. |
| Integration tests against a live Taiga instance | **In progress (gregory, 2026-08-08).** No longer just deferred — see [docs/issues/2026-08-08-integration-tests-live-taiga.md](../issues/2026-08-08-integration-tests-live-taiga.md) (investigation) and [docs/testing/integration-tests-plan.md](integration-tests-plan.md) (task plan; task 1 done). Still a manually-triggered, env-var-gated `jvmTest`, not part of PR CI. |
| Adding a mocking framework | The hand-written-fake convention is working and is genuinely consistent. Do not introduce MockK to `commonTest`. |
| Testing `dto` and pure-`domain` modules | Most of the 36 untested modules are serializable data holders or interfaces plus models. Correctly untested; leave them. |
| ~~Compose UI test for `KanbanScreen`/`KanbanViewModel`~~ | **Dropped (gregory, 2026-08-07)** — not just deferred, off the list. Surveyed 2026-08-07 while scoping tasks 15–21 as a multi-state-source candidate: combines `getKanbanData()` with independently-loaded filters (`loadFiltersData()`), per-swimlane filter state, and optimistic drag-drop reordering (`moveStory`) — 4 ViewModel deps, real swimlanes, real drag-drop. Heaviest candidate found and the only one with drag-drop, which doesn't fit task 11's established click/type-only interaction scope. |

---

## Screenshot testing — history so far

**2026-08-02, original deferral:** "High maintenance for a solo-maintained app. Revisit only if
visual regressions become a recurring, concrete problem — not preemptively."

**2026-08-08, first investigation:** revisited now that tasks 10–21 wired `captureToImage()` into
every `jvmTest` module for free. Full writeup:
[docs/issues/2026-08-08-screenshot-testing.md](../issues/2026-08-08-screenshot-testing.md). Findings,
scoped to the **Desktop/JVM target only** (the one tasks 10–21 already test against):

- Capture mechanics are free now (`captureToImage()`, already a `jvmTest` dependency everywhere).
- The real blocker is cross-machine **font-rendering determinism** — Compose Desktop renders text
  through the host OS's real font stack, so a golden recorded on one machine mismatches on another.
  Applies equally to Roborazzi's alpha `roborazzi-compose-desktop` artifact and a hand-rolled
  `captureToImage()` + PNG comparator.
- Verdict at the time: still deferred, for this narrower (font-determinism) reason rather than the
  original vague "high maintenance" one.

**2026-08-08, same day, Paparazzi discussed and reframed the question.** The app's primary shipping
targets are Android and iOS, not Desktop — the investigation above was implicitly Desktop-scoped
because that's where the existing test infra happens to run, not because Desktop is what matters.
Paparazzi renders through **Layoutlib** (Android Studio's own Preview renderer) instead of Compose
Desktop's Skia path, which changes the tradeoff:

- **Deterministic across machines, unlike the Desktop path** — Layoutlib bundles its own fonts
  (confirmed in Paparazzi's changelog: "Fix font scaling issue... by using bundled font"), so it
  doesn't inherit the font-determinism problem above. This is the one finding that actually answers
  the objection the first investigation raised — but only for Android, not Desktop.
- **As of `2.0.0-alpha05` (2026-05-20), Paparazzi added support for the
  `com.android.kotlin.multiplatform.library` Gradle plugin** — which every `feature/*/ui` module in
  this repo already applies (`build-logic/convention/src/main/kotlin/KmpLibraryConventionPlugin.kt:20`).
  Historically Paparazzi couldn't touch a KMP module at all and needed a separate shim
  `com.android.library` module just to depend on it; that's why most existing blog posts describe the
  shim-module workaround instead. The direct-plugin support is new enough that no primary source was
  found confirming exactly how a test file addresses `commonMain` Composables under it (one candidate
  primary source, a msfjarvis.dev post on Paparazzi + KMP, 403'd when fetched) — treat this as
  medium-confidence, not verified against a worked example.
- **Real costs, not glossed over:**
  - Alpha software (`2.0.0-alpha05`) — genuine risk to gate CI on for a solo-maintained app. The
    stable 1.x line lacks the KMP-plugin support that makes this relevant at all.
  - Needs a **new host-test source set** opted into on the Android KMP target
    (`withHostTestBuilder {}` on `KotlinMultiplatformAndroidLibraryExtension`) — checked
    `KmpLibraryConventionPlugin.kt`, nothing like it exists in any module today.
  - That source set is **not** `jvmTest` — same trap CLAUDE.md already documents for
    `tools/seed`/`tools/utils`: it needs its own Gradle task and its own CI step, or it silently
    never runs. It would be genuinely new infra, not a reuse of tasks 10–21's wiring.
  - More fundamentally: **this project has zero Android-target test execution today.**
    `runComposeUiTest`'s Android `actual` requires real instrumentation (a device/emulator); it is
    not Robolectric-backed for the multiplatform API as of this investigation. So adding Paparazzi
    (or classic Robolectric-based Roborazzi) wouldn't be "add screenshots to what we have" — it would
    be the *first* Android-target test source set this repo has ever had. CLAUDE.md is explicit that
    the absence today is by design, not an oversight — this is a structural decision, not a tool pick.

**Decision (gregory, 2026-08-08):** pursue Paparazzi next, as its own task/branch — separate from
this doc and from the now-closed `improvement-plan.md`. Scope it fresh when picked up: confirm the
`commonMain`-Composable-addressing mechanics with a real spike before committing to the approach,
size the new host-test source set + CI step, and weigh the alpha-dependency risk explicitly rather
than assuming it away.

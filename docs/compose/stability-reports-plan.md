# Compose Compiler stability reports — plan

Prompted by a blog post on Strong Skipping Mode ("Stop Ignoring Compose Stability", Shreyas Patil,
2026-08-03): SSM makes every composable *skippable*, but an unstable parameter still falls back to
`===` (referential) comparison instead of `.equals()`, so a data class with one unstable field (a
plain `List<T>`) silently recomposes on every emission even when its content is unchanged. This repo
already follows the fix as convention (CLAUDE.md's Compose/Platform Rules: prefer
`ImmutableList`/`persistentListOf()` in state classes and Composable parameters). A spot check of
`feature/workitem/ui`'s `*State` data classes found no violations — every plain `List<T>` hit was a
function parameter, not a state field.

What's missing is the verification tool the article itself points at: the Compose Compiler's own
stability reports (`*_classes.txt` / `*_composables.txt`), which show what the compiler actually
inferred rather than what the convention assumes. Nothing in this build generates them today (checked
`build-logic/`, root `build.gradle.kts` — no `composeCompiler { }` block anywhere).

This is a diagnostic tool, not a permanent build step or a gate — it should be opt-in and add zero
cost to a normal build, the same way `-PgplayBuild` gates Firebase.

## Status

| # | Task | Size | Status |
|---|------|------|--------|
| 1 | Gradle wiring: opt-in stability reports | S | Done (2026-08-11) |
| 2 | Aggregator script + first repo-wide audit + doc | M | Done (2026-08-12) |
| 3 | Fix domain-model stability gap (`docs/revisit.md` #39) | M | Done (2026-08-12) |

## Researched facts (so task 1 doesn't have to re-derive them)

- Compose compiler plugin applied via `org.jetbrains.kotlin.plugin.compose` (Kotlin `2.4.10`, from
  `gradle/libs.versions.toml`), in two convention-plugin call sites:
  - `build-logic/convention/src/main/kotlin/KmpLibraryComposeConventionPlugin.kt` — every KMP UI
    module (`composeApp`, `uikit`, every `feature/*/ui`).
  - `build-logic/convention/src/main/kotlin/AndroidApplicationConventionPlugin.kt` — `androidApp`.
- The plugin registers a `composeCompiler { }` extension of type
  `org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension`. Confirmed by
  decompiling `compose-compiler-gradle-plugin-2.4.10-gradle813.jar` from the local Gradle cache
  (`javap` on the class) — relevant members:
  - `metricsDestination: DirectoryProperty`
  - `reportsDestination: DirectoryProperty`
  - `stabilityConfigurationFiles: ListProperty<RegularFile>` (plural — the singular
    `stabilityConfigurationFile` is the deprecated form, don't use it)
  - `targetKotlinPlatforms: SetProperty<KotlinPlatformType>` — restricts which target compilations
    get instrumented at all.
- **KMP modules compile shared `commonMain` composables once per target** (android, iosArm64,
  iosSimulatorArm64, jvm), so with no `targetKotlinPlatforms` restriction, every UI module would emit
  4 near-duplicate report sets for the same commonMain code. **Do not use `targetKotlinPlatforms` to
  restrict this to `jvm`** — task 1 tried exactly that and it broke `androidApp`'s build. Reading
  `ComposeCompilerGradleSubplugin.isApplicable()` (decompiled/sourced from
  `compose-compiler-gradle-plugin-2.4.10-sources.jar`) shows `targetKotlinPlatforms` is not just a
  report-output filter — it's what the subplugin uses to decide whether the Compose compiler plugin
  applies to a compilation *at all*. Setting it to `[jvm]` silently disabled Compose's bytecode
  transformation for the Android target in every KMP UI module (`utils:ui`, `uikit`, every
  `feature/*/ui`, `composeApp`), and `androidApp:compileFdroidDebugKotlin -PcomposeStabilityReport`
  failed with `Internal compiler error... couldn't find inline method
  Landroidx/compose/runtime/CompositionLocal;.getCurrent()` — a real build break, not just a scanning
  gap. The shipped `configureComposeStabilityReports()` sets no `targetKotlinPlatforms` at all and
  applies identically to both convention-plugin call sites. **Duplicate per-target reports are instead
  avoided operationally**: task 2 must only invoke the `jvm`-target compile task per module (e.g.
  `:composeApp:compileKotlinJvm`), never a task that also builds Android/iOS. Known gap this still
  leaves: any composable declared only in an `androidMain` source set won't be scanned by a
  jvm-only run — same caveat as before, just no longer achieved via a build-breaking property.
- `androidApp` is not KMP (single `androidJvm` target) — no `targetKotlinPlatforms` restriction
  needed there.
- `build-logic` is an **included build** (`settings.gradle.kts`: `includeBuild("build-logic")`), not
  a subproject — the root's `alias(libs.plugins.ktlint)` does not reach it, so build-logic changes
  need a compile check instead of `ktlintCheck`. Use `./gradlew :build-logic:convention:compileKotlin`,
  **not** `:build-logic:convention:build`: task 1 found `:build-logic:convention:build` fails on a
  pre-existing, unrelated `validatePlugins` error (`RenameApkTask` missing a caching annotation) even
  on a clean tree — see [docs/revisit.md #38](../revisit.md#38-build-logicconventionbuild-fails-on-a-pre-existing-validateplugins-error-unrelated-to-any-specific-change).

## Task 1 — Gradle wiring: opt-in stability reports

**Size:** S

**What:** Add a shared function, gated behind a Gradle project property so a default build is
unaffected — same pattern as `-PgplayBuild`.

1. New file `build-logic/convention/src/main/kotlin/com/grappim/taigamobile.buildlogic/ComposeCompilerReports.kt`:
   ```kotlin
   package com.grappim.taigamobile.buildlogic

   import org.gradle.api.Project
   import org.gradle.kotlin.dsl.configure
   import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension
   import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

   // Opt-in Compose Compiler stability audit — see docs/compose/stability-reports.md.
   // Off by default: -PcomposeStabilityReport to generate *_classes.txt / *_composables.txt.
   fun Project.configureComposeStabilityReports() {
       if (!project.hasProperty("composeStabilityReport")) return
       extensions.configure<ComposeCompilerGradlePluginExtension> {
           metricsDestination.set(layout.buildDirectory.dir("compose_reports"))
           reportsDestination.set(layout.buildDirectory.dir("compose_reports"))
       }
   }
   ```
   As shipped, this takes **no** `restrictToJvm` parameter and sets no `targetKotlinPlatforms` — see
   the "Researched facts" entry above for why that parameter was tried and reverted (it broke
   `androidApp`'s build, not just report scope).
2. Call it identically from both convention plugins, right after
   `apply("org.jetbrains.kotlin.plugin.compose")`:
   - `KmpLibraryComposeConventionPlugin.kt`: `configureComposeStabilityReports()`
   - `AndroidApplicationConventionPlugin.kt`: `configureComposeStabilityReports()`
3. Do **not** add a `stabilityConfigurationFile` yet — nothing has shown a false positive to suppress.
   (See "Considered and deferred" below.)

**Done when:**
```bash
# opt-in: reports appear
rm -rf composeApp/build/compose_reports
./gradlew :composeApp:compileKotlinJvm -PcomposeStabilityReport
ls composeApp/build/compose_reports   # *_classes.txt / *_composables.txt present

# default build: unaffected
rm -rf composeApp/build/compose_reports
./gradlew :composeApp:compileKotlinJvm
ls composeApp/build/compose_reports   # No such file or directory

# androidApp path also wired
./gradlew :androidApp:compileFdroidDebugKotlin -PcomposeStabilityReport
ls androidApp/build/compose_reports

# nothing else broke
./gradlew jvmTest
```

**Finalize focus:** if `targetKotlinPlatforms` or the extension import differs from the decompiled
signature (API drift between Kotlin releases), record the real signature here — this section is
exactly the kind of "confirm via reading the actual jar" step future sessions shouldn't have to redo.

**Result:** Implemented as planned except one deviation, caught by running the `Done when` steps
literally rather than trusting the plan: the original design used
`targetKotlinPlatforms.set(setOf(KotlinPlatformType.jvm))` (passed as `restrictToJvm = true` from
`KmpLibraryComposeConventionPlugin`) to avoid 4x duplicate reports per KMP UI module. That broke
`androidApp:compileFdroidDebugKotlin -PcomposeStabilityReport` — the exact command in this task's own
"Done when" — with `Internal compiler error... couldn't find inline method
Landroidx/compose/runtime/CompositionLocal;.getCurrent()`. Root cause (confirmed by reading
`ComposeCompilerGradleSubplugin.isApplicable()` in the plugin's sources jar): `targetKotlinPlatforms`
doesn't just filter report output, it's what the subplugin uses to decide whether the Compose
compiler plugin applies to a compilation *at all* — restricting to `jvm` silently disabled Compose's
bytecode transformation for the Android target of `utils:ui`, `uikit`, every `feature/*/ui`, and
`composeApp`. Fix: dropped `targetKotlinPlatforms`/`restrictToJvm` entirely;
`configureComposeStabilityReports()` now takes no parameter and both convention-plugin call sites are
identical. Duplicate reports across targets are deferred to task 2 to solve operationally (only
invoke the `jvm`-target compile task per module). Full "Researched facts" bullet above has the
details for a future session that might otherwise reach for `targetKotlinPlatforms` again.

All `Done when` commands passed after the fix:
`:composeApp:compileKotlinJvm -PcomposeStabilityReport` produced `*_classes.txt`/`*_composables.txt`
under `composeApp/build/compose_reports/`; the same command without the flag produced no such
directory; `:androidApp:compileFdroidDebugKotlin -PcomposeStabilityReport` succeeded and produced
`androidApp/build/compose_reports/`, and the same command without the flag left no such directory;
`./gradlew jvmTest` passed clean. `build-logic` has no `ktlintCheck` task (per CLAUDE.md) —
`:build-logic:convention:compileKotlin` was used as the compile-clean check instead
(`:build-logic:convention:build` fails on a pre-existing, unrelated `validatePlugins` issue with
`RenameApkTask` not being cacheable-annotated — not touched by this task).

**Next: task 2** — Aggregator script + first repo-wide audit + doc. Done, see below.

## Task 2 — Aggregator script + first repo-wide audit + doc

**Size:** M

**What:**
1. Run the flag across every Compose UI module (`composeApp`, `uikit`, every `feature/*/ui`) at the
   `jvm` target, e.g.:
   ```bash
   ./gradlew jvmTest compileKotlinJvm -PcomposeStabilityReport   # or a narrower target list; confirm the right umbrella task when doing this
   ```
2. Write a small script, `docs/compose/stability-scan.py`, modeled on
   `docs/testing/kover-rank.py`'s spirit (parse-and-rank, not build-time tooling): walk
   `**/build/compose_reports/*_classes.txt` and `*_composables.txt`, extract lines containing
   `unstable`, and print one summary line per hit (module, class/composable name, the unstable
   member). Keep it to plain stdlib — no new dependency for a script run a few times a year.
3. Run the script, triage what it finds:
   - Genuine bug (a state-class field that should be `ImmutableList` and isn't) → fix inline, small
     enough to belong in this task.
   - Real but out of scope to fix now (e.g. a third-party type, or a large refactor) → file it in
     `docs/revisit.md` per [[defer-to-revisit-list]], with the `*_classes.txt` evidence.
   - Nothing found → say so plainly in the Result note; a clean audit is still a result.
4. Write `docs/compose/stability-reports.md`: how to run the scan, what the two report file formats
   mean, the JVM-only / androidMain-blind-spot caveat from task 1's research notes, and how to read
   the script's output. This is the doc a future "run the audit again" session reads instead of
   re-deriving task 1.
5. Add a one-line pointer from CLAUDE.md's `## Compose / Platform Rules` section to the new doc, next
   to the existing `ImmutableList` convention bullet — don't inline the how-to there (see CLAUDE.md's
   own "Keeping this file lean" section).

**Done when:**
```bash
python3 docs/compose/stability-scan.py   # prints a summary (possibly empty) across all scanned modules
./gradlew jvmTest && ./gradlew ktlintCheck   # if any production fix landed in step 3
```

**Finalize focus:** update `docs/compose/stability-reports.md` if the actual report file format
differs from what's assumed above (formats confirmed only by running task 1, not yet inspected).

**Result:** Ran the flag across all 24 Compose UI modules + `androidApp` at the `jvm` target (exact
command in [docs/compose/stability-reports.md](stability-reports.md#running-the-audit)). First attempt
without `--rerun-tasks` produced zero reports — every `compileKotlinJvm` task was `UP-TO-DATE` from an
earlier run without the flag, and Gradle doesn't track `-P` project properties as task inputs, so it
skipped re-execution silently (same gotcha CLAUDE.md's Testing section already documents for env
vars). `--rerun-tasks` fixed it.

Wrote `docs/compose/stability-scan.py` per the plan, but the real report filenames turned out to be
`<module>-classes.txt`/`<module>-composables.txt` (hyphen), not the `_classes.txt`/`_composables.txt`
the plan assumed — script globs and this doc's description use the real names.

Triage of the scan's output (121 unstable-class findings, 60 unstable-composable-parameter findings):

- **Zero plain-`List<T>` state-class fields** — confirms the plan's earlier spot-check. One real
  violation the flat `: List<` grep initially missed because it was nested, not top-level:
  `SprintState.storiesWithTasks: ImmutableMap<WorkItem, List<WorkItem>>` — fixed to
  `ImmutableMap<WorkItem, ImmutableList<WorkItem>>` (`feature/sprint/ui/.../SprintState.kt:18`).
  `:feature:sprint:ui:compileKotlinJvm` verified green after the change; re-running the report showed
  the field is *still* flagged unstable afterward — expected, since its remaining instability is
  `WorkItem`'s (the next finding), not this one; the nested-`List` shape itself is gone.
- **The dominant finding, by far**: domain model classes (`WorkItem`, `User`, `Project`, `Sprint`,
  `FiltersData`, and a dozen more) are structurally stable — verified `WorkItem` by reading its source,
  fully `val`, its one collection field already `ImmutableList<Tag>` — yet report unstable in every
  consuming module, because they're defined in `*/domain` modules that never apply the Compose compiler
  plugin, so no stability marker gets embedded for downstream Compose modules to trust. This explains
  the large majority of both counts (a container's unstable-ness propagates from its stable/unstable
  type arguments, so `ImmutableList<WorkItem>` reports unstable too). Deferred, not fixed inline — the
  fix is a repo-wide policy decision (apply Compose compiler to ~15 domain modules, or maintain a
  `stabilityConfigurationFiles` list) that doesn't belong riding along on a scan-triage task. Full
  mechanism, evidence, and the fix options considered:
  [docs/revisit.md #39](../revisit.md#39-domain-model-classes-read-as-compose-unstable-across-every-feature-because-domain-modules-dont-apply-the-compose-compiler-plugin).
- **Expected, not actionable without a policy call**: `NavController`/`NavHostController` and
  `LazyPagingItems<T>` (third-party types, same "no marker" cause but not our code to fix), and
  `Any`-typed parameters in `uikit`'s drag-and-drop (inherently unstable by design). `kotlinx.datetime`
  `LocalDate`/`LocalDateTime` hit the identical mechanism as the domain-model finding but for a
  third-party library — same `stabilityConfigurationFiles` fix shape, left for the same reason (no
  second data point yet to justify a standing config file).

Wrote `docs/compose/stability-reports.md` (how to run the audit, the two report formats, the JVM-only/
androidMain-blind-spot caveat, how to read the script's output, and this audit's findings). Added a
one-line CLAUDE.md pointer next to the `ImmutableList` convention bullet.

`./gradlew jvmTest` passed clean after the `SprintState` fix; `ktlintCheck` not separately re-run
since the only production edit was a type-parameter widening on an existing line, no formatting
change.

**Next: task 3** — Fix the domain-model stability gap (`docs/revisit.md` #39). Done, see below.

## Task 3 — Fix domain-model stability gap (`docs/revisit.md` #39)

**Size:** M

**What:** four options were laid out for the dominant task 2 finding (domain model classes reading as
Compose-unstable everywhere because their `*/domain` modules never apply the Compose compiler plugin):
apply the full `taigamobile.kmp.library.compose` convention to domain modules (rejected — drags
Foundation/Material3/Navigation into a layer with zero `@Composable`s), a `stabilityConfigurationFiles`
trust-list (rejected — blind trust, no compiler verification if a class later gains a mutable field),
expanding the existing-but-inconsistent `*UI` model + mapper pattern to fully insulate every composable
from domain types (rejected for this task — architecturally the cleanest long-term answer, but a
multi-session refactor touching composable signatures across ~15 features, not this task's size), and a
new minimal convention plugin applying only the Compose Kotlin compiler subplugin (chosen). User picked
the minimal-convention option directly.

1. New convention plugin `taigamobile.kmp.library.stability`
   (`build-logic/convention/src/main/kotlin/KmpLibraryStabilityConventionPlugin.kt`), applying:
   - `org.jetbrains.kotlin.plugin.compose` (the compiler subplugin only — **not**
     `org.jetbrains.compose`, which is what pulls in the UI toolkit).
   - A `compileOnly` dependency on `compose-runtime`
     (`ComposeStabilityMarker.kt`'s `configureComposeStabilityMarker()`) — needed only so the compiler
     can reference the `@StabilityInferred` annotation type at compile time; consuming UI modules
     already carry `compose-runtime` themselves, so nothing needs it at runtime from here.
   - Also calls the existing `configureComposeStabilityReports()`, so these modules produce their own
     opt-in reports too, for direct verification (`WorkItem`'s own `-classes.txt` now says `stable`,
     not just downstream consumers looking less unstable).
2. Registered in `gradle/libs.versions.toml` (`taigamobile-kmp-library-stability`) and
   `build-logic/convention/build.gradle.kts` (`kmpLibraryStability` → `KmpLibraryStabilityConventionPlugin`),
   following the exact pattern of `kmpLibraryCompose`.
3. Applied `alias(libs.plugins.taigamobile.kmp.library.stability)` alongside the existing
   `alias(libs.plugins.taigamobile.kmp.library)` in every `*/domain` module whose types are consumed as
   Composable parameters.

**Done when:**
```bash
./gradlew :build-logic:convention:compileKotlin
./gradlew :feature:workitem:domain:compileKotlinJvm :feature:workitem:domain:compileKotlinIosArm64 \
  :feature:workitem:domain:compileAndroidMain   # and the same three tasks for every other affected domain module
# re-run the task 2 audit (docs/compose/stability-reports.md#running-the-audit) and confirm the
# unstable-composable-parameter count drops to just the independently-unstable buckets
./gradlew jvmTest && ./gradlew ktlintCheck
```

**Result:** Started from the 10 modules derived by tracing field types by hand from the task 2 scan
output (`WorkItem`, `User`, `TeamMember`, `Project`, `ProjectValueItem`, `Sprint`, `FiltersData`,
`PendingCertTrust`, `Swimlane`, `KanbanUserStory`, `Epic`, `UserStoryEpic`, and their transitive field
types — `Status`, `Tag`, `Statuses`, `ProjectExtraInfo`, `UserStory`, `DueDateStatus` — all resolved
back to the same 10 modules): `core/domain`, `feature/epics/domain`, `feature/filters/domain`,
`feature/kanban/domain`, `feature/projects/domain`, `feature/sprint/domain`, `feature/swimlanes/domain`,
`feature/userstories/domain`, `feature/users/domain`, `feature/workitem/domain`. All 10 compiled clean
on `jvm`, `iosArm64`, and `androidMain` after adding the plugin.

**The hand-traced list was incomplete — the empirical re-scan caught it, hand-tracing wouldn't have.**
Re-running the task 2 audit showed `feature/tasks/ui`'s `TaskDetailsState.currentTask`/`originalTask`
(type `Task`, from `feature/tasks/domain`) still unstable. `feature/tasks/domain` was missed because
the original derivation started from the *composables.txt "unstable parameter"* list and the *classes.txt*
entries checked by hand, not systematically from the full 121-line classes.txt output — `Task` only
ever showed up as a `TaskDetailsState` field, a class-list entry, which wasn't part of the trace.
Added `feature/tasks/domain` as an 11th module, verified it compiles on all three targets, and
re-ran the audit again: the gap fully closed on this pass. **Lesson for next time something similar
comes up: derive the module list from a fresh full re-scan after the "obvious" set is fixed, not from
extending the original hand-trace** — the empirical loop (fix → re-scan → check for stragglers) is
cheap and caught what static tracing missed.

Final re-scan: unstable-composable-parameter findings dropped from 60 (task 2 baseline) to 11, and
all 11 remaining are exactly the three buckets already flagged as independently out of scope —
`NavController`/`NavHostController` (3), `kotlinx.datetime` `LocalDate`/`LocalDateTime` (5),
`Any`-typed parameters (3). Zero remaining findings trace to a domain model type. Unpredicted bonus:
`LazyPagingItems<WorkItem>` findings (Paging Compose, itself `@Stable`) disappeared entirely — the
compiler had apparently been propagating `WorkItem`'s instability into that generic wrapper the same
way it does for `ImmutableList<WorkItem>`, so fixing `WorkItem` fixed both.

`./gradlew jvmTest` and `ktlintCheck` green across the whole repo. `docs/revisit.md` #39 updated with
a Resolved note carrying this same evidence.

**Finalize focus:** none — this task's own Result note is the full record; nothing here depends on a
future session re-deriving anything.

**Queue is empty.** No task 4 scoped. The remaining "expected, not actionable without a policy call"
findings (`NavController`, `LazyPagingItems`, `Any`-typed params, `kotlinx.datetime`) are documented in
`docs/compose/stability-reports.md` and not tracked as open work — revisit only if a future audit finds
a second `kotlinx.datetime`-shaped case elsewhere, per the plan's original "add
`stabilityConfigurationFiles` only in response to a concrete finding" reasoning.

## Considered and deferred

| Idea | Why deferred |
|------|--------------|
| Always-on reports (no `-P` gate) | Pure I/O/build-time cost for a diagnostic that's useful a few times a year, not every build — same reasoning as `-PgplayBuild` being opt-in. |
| CI gate failing the build on new unstable classes | No baseline yet. Revisit only if task 2's audit turns up a real, recurring problem worth enforcing — premature before a single data point exists. |
| `stabilityConfigurationFiles` (mark external types stable) | Task 2's audit *did* turn up a concrete finding (every domain model), but task 3 chose the compiler-marker convention plugin over a trust-list — see task 3's write-up for why. Still not applied for the separate `kotlinx.datetime` case: one data point (`LocalDate`/`LocalDateTime`) isn't enough to justify a standing config file yet. |
| `@NonSkippableComposable` sweep for "skipping isn't free" cases | The article's own edge case, and needs profiling evidence (a lightweight composable eating cycles on `.equals()` over a huge stable graph) to be worth anything — no such evidence exists here. |
| Expand the `*UI` model + mapper pattern to every feature | Architecturally the cleanest long-term fix for the domain-model gap task 3 addressed — a UI model can never leak an unmapped domain type. Rejected for task 3's size: touches composable signatures and adds mapper code across ~15 features, a multi-session refactor, not a build-config change. Revisit only as its own deliberately-scoped project. |

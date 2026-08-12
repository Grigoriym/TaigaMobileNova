# Compose Compiler stability reports

How to run a Compose stability audit and read its output. Background and the Gradle wiring
decisions live in [stability-reports-plan.md](stability-reports-plan.md) — this doc is the "run it
again" reference, not the history.

## Running the audit

Reports are opt-in (`-PcomposeStabilityReport`) and Gradle does not track that project property as a
task input, so a task that's already `UP-TO-DATE` from a prior build will not regenerate its report —
pass `--rerun-tasks` (or delete the module's `build/compose_reports/` first) to force it.

Only the `jvm`-target compile task per Compose UI module, never a task that also builds
Android/iOS — see [stability-reports-plan.md](stability-reports-plan.md#researched-facts) for why
`targetKotlinPlatforms` can't be used to restrict this instead (it disables Compose's bytecode
transformation entirely, not just report scope).

```bash
# clear any stale reports first
find . -type d -name compose_reports -exec rm -rf {} +

# every Compose UI module's jvm target, plus androidApp (single androidJvm target, no jvm split)
./gradlew \
  :composeApp:compileKotlinJvm :core:navigation:compileKotlinJvm :core:storage:compileKotlinJvm \
  :uikit:compileKotlinJvm :utils:ui:compileKotlinJvm :strings:compileKotlinJvm \
  :feature:teams:ui:compileKotlinJvm :feature:projectselector:ui:compileKotlinJvm \
  :feature:filters:ui:compileKotlinJvm :feature:settings:ui:compileKotlinJvm \
  :feature:wiki:ui:compileKotlinJvm :feature:scrum:ui:compileKotlinJvm \
  :feature:userstories:ui:compileKotlinJvm :feature:userstories:data:compileKotlinJvm \
  :feature:workitem:ui:compileKotlinJvm :feature:workitem:data:compileKotlinJvm \
  :feature:tasks:ui:compileKotlinJvm :feature:epics:ui:compileKotlinJvm \
  :feature:sprint:ui:compileKotlinJvm :feature:login:ui:compileKotlinJvm \
  :feature:kanban:ui:compileKotlinJvm :feature:profile:ui:compileKotlinJvm \
  :feature:dashboard:ui:compileKotlinJvm :feature:issues:ui:compileKotlinJvm \
  :androidApp:compileFdroidDebugKotlin \
  -PcomposeStabilityReport --rerun-tasks

python3 docs/compose/stability-scan.py
```

The module list is every module whose `build.gradle.kts` has
`alias(libs.plugins.taigamobile.kmp.library.compose)` (`grep -rl` for that string to regenerate the
list if modules are added/removed) plus `androidApp`.

**Known gap:** running only the `jvm`-target task means any composable declared exclusively in an
`androidMain` source set is never scanned. No such composable is known to exist today; if one is
added, this audit won't see it.

## Report file formats

Each module's `build/compose_reports/` contains (module name / project path baked into the filename,
so exact names vary):

- `<module>-classes.txt` — one block per class compiled in that module:
  ```
  unstable class com.grappim.taigamobile.feature.workitem.domain.WorkItem {
    stable val id: Long
    unstable val assignee: User?
    <runtime stability> = Unstable
  }
  ```
  Each member line is prefixed `stable`/`unstable`/`runtime` (`runtime` means "depends on a generic
  type argument's runtime stability" — e.g. `Uncertain(List)`). The `<runtime stability>` line is the
  class's overall verdict.
- `<module>-composables.txt` — one block per `@Composable` (and some non-composable top-level
  functions the compiler still tracked), listing `restartable`/`skippable` flags and each parameter
  prefixed the same way:
  ```
  restartable skippable scheme("[androidx.compose.ui.UiComposable]") fun com.grappim.taigamobile.feature.dashboard.ui.DashboardWorkItemCard(
    unstable item: WorkItem
    stable modifier: Modifier? = @static <expression>
  )
  ```
  **This file is the actionable one** — an unstable *class* only matters in practice if it's also an
  unstable *composable parameter* (a class that's merely stored in a repository or mapper, never
  passed to a skippable composable, doesn't affect recomposition).
- `<module>-composables.csv` — same data as the `.txt`, tabular, unused by the scan script.
- `android/main/<module>-module.json` and `jvm/main/<module>-module.json` — raw per-target metrics
  Compose's own tooling can consume; not human-oriented, not parsed by the scan script.

Filenames use `-classes.txt`/`-composables.txt` (hyphen), not the `_classes.txt`/`_composables.txt`
the original plan assumed — the scan script glob-matches the real hyphenated names.

## Reading the scan script's output

`docs/compose/stability-scan.py` is stdlib-only (no new dependency for a script run a few times a
year). It prints two sections:

- **Unstable classes** — every class with at least one unstable member, one line per member:
  `module | class FQN | member: Type`.
- **Composables with unstable parameters** — every composable with at least one unstable parameter,
  one line per parameter: `module | fun FQN | param: Type`. Triage from this section first per the
  note above.

## What the first repo-wide audit (2026-08-12) found

Zero plain-`List<T>` fields in state classes — the `ImmutableList`/`persistentListOf()` convention
(CLAUDE.md's Compose/Platform Rules) is followed correctly everywhere. One real violation the flat
grep for `: List<` missed because it was a *nested* type argument, not a top-level field:
`SprintState.storiesWithTasks` was declared `ImmutableMap<WorkItem, List<WorkItem>>` (plain `List`
nested inside the map value) despite the domain-layer `SprintData` it's populated from already being
fully `ImmutableList`-typed — fixed by widening the declared type to
`ImmutableMap<WorkItem, ImmutableList<WorkItem>>`.

The great majority of the 121 unstable-class / 60 unstable-parameter findings are **not** independent
bugs — they trace back to one systemic cause: domain model classes (`WorkItem`, `User`, `Project`,
`Sprint`, `FiltersData`, and more) are defined in `*/domain` modules that don't apply the Compose
compiler plugin, so the plugin never embeds a stability marker for them, and every downstream Compose
module treats them as unstable by default regardless of how simple they actually are (`WorkItem` is
a fully `val`, fully `ImmutableList`-using data class and still reads unstable). Full writeup, the
mechanism, and the fix options considered: [docs/revisit.md #39](../revisit.md#39-domain-model-classes-read-as-compose-unstable-across-every-feature-because-domain-modules-dont-apply-the-compose-compiler-plugin).

A handful of findings are expected and not actionable without a `stabilityConfigurationFiles` policy
decision: `NavController`/`NavHostController` and `LazyPagingItems<T>` (third-party types, no marker
regardless of our code) and `Any`-typed parameters (`columnId`/`itemKey` in `uikit`'s drag-and-drop —
inherently unstable by design). `kotlinx.datetime.LocalDate`/`LocalDateTime` hit the same "foreign,
unmarked" default as the domain-model issue above, but for a third-party library instead of our own
code — same fix shape (`stabilityConfigurationFiles`), not applied here for the same reason: one
data point isn't enough to justify a standing config file yet.

CLAUDE.md's Compose/Platform Rules links here next to the `ImmutableList` convention bullet.

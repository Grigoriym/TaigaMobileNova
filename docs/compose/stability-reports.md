# Compose Compiler stability reports

How to run a Compose stability audit and read its output. Background and the Gradle wiring
decisions live in [stability-reports-plan.md](stability-reports-plan.md) — this doc is the "run it
again" reference, not the history.

## Running the audit

Reports are opt-in (`-PcomposeStabilityReport`) and Gradle does not track that project property as a
task input, so a task that's already `UP-TO-DATE` from a prior build will not regenerate its report —
pass `--rerun-tasks` (or delete the module's `build/compose_reports/` first) to force it.

Only the `jvm`-target compile task per Compose UI module, never a task that also builds
Android/iOS — see [stability-reports-plan.md](stability-reports-plan.md#researched-facts-so-task-1-doesnt-have-to-re-derive-them) for why
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

## Domain modules need `taigamobile.kmp.library.stability` too

A `*/domain` module that defines a type consumed as a Composable parameter anywhere in the app must
also apply `alias(libs.plugins.taigamobile.kmp.library.stability)` alongside its usual
`taigamobile.kmp.library`. Without it, the Compose compiler in every downstream UI module has no
stability marker to trust for that type and defaults it to `Unstable`, no matter how simple the class
actually is — see [docs/revisit.md #39](../revisit.md#39-domain-model-classes-read-as-compose-unstable-across-every-feature-because-domain-modules-dont-apply-the-compose-compiler-plugin)
for the mechanism and the fix chosen (a convention plugin applying only the Compose Kotlin compiler
subplugin + a `compileOnly compose-runtime` dependency — no UI toolkit reaches the domain layer).

Modules that already have it: `core/domain`, `feature/epics/domain`, `feature/filters/domain`,
`feature/kanban/domain`, `feature/projects/domain`, `feature/sprint/domain`,
`feature/swimlanes/domain`, `feature/tasks/domain`, `feature/userstories/domain`,
`feature/users/domain`, `feature/workitem/domain`.

**When adding a new domain module (or a new type to an existing one) that a Composable will take as a
parameter**, add the plugin up front rather than waiting for an audit to catch it — re-running the
scan (below) after the fact is how the gap gets *found*, but there's no need to wait for that when the
module is new and the need is already known.

## What the audits found

**First audit (2026-08-12, task 2):** zero plain-`List<T>` fields in state classes — the
`ImmutableList`/`persistentListOf()` convention (CLAUDE.md's Compose/Platform Rules) is followed
correctly everywhere. One real violation the flat grep for `: List<` missed because it was a *nested*
type argument, not a top-level field: `SprintState.storiesWithTasks` was declared
`ImmutableMap<WorkItem, List<WorkItem>>` (plain `List` nested inside the map value) despite the
domain-layer `SprintData` it's populated from already being fully `ImmutableList`-typed — fixed by
widening the declared type to `ImmutableMap<WorkItem, ImmutableList<WorkItem>>`.

The great majority of that audit's 121 unstable-class / 60 unstable-parameter findings were **not**
independent bugs — they traced back to one systemic cause: domain model classes (`WorkItem`, `User`,
`Project`, `Sprint`, `FiltersData`, and more) were defined in `*/domain` modules that didn't apply the
Compose compiler plugin, so no stability marker got embedded for them, and every downstream Compose
module treated them as unstable by default regardless of how simple they actually were (`WorkItem` was
a fully `val`, fully `ImmutableList`-using data class and still read unstable).

**Fix (2026-08-12, task 3):** applied `taigamobile.kmp.library.stability` (see above) to the 11 domain
modules whose types are used as Composable parameters. Re-running the audit afterward:
unstable-composable-parameter findings dropped from 60 to 11, and all 11 remaining are exactly the
"expected, not actionable" buckets described below — zero remaining findings trace to a domain model
type. Unpredicted bonus: `LazyPagingItems<WorkItem>` findings (Paging Compose, itself `@Stable`)
disappeared too — the compiler had apparently been propagating `WorkItem`'s instability into that
generic wrapper the same way it does for `ImmutableList<WorkItem>`. Full writeup:
[docs/revisit.md #39](../revisit.md#39-domain-model-classes-read-as-compose-unstable-across-every-feature-because-domain-modules-dont-apply-the-compose-compiler-plugin).

**Expected, not actionable without a `stabilityConfigurationFiles` policy decision** (still true after
the fix above — these are independent mechanisms): `NavController`/`NavHostController` (third-party
type, no marker regardless of our code) and `Any`-typed parameters (`columnId`/`itemKey` in `uikit`'s
drag-and-drop — inherently unstable by design, narrowing the type is the only fix and weakens the
API). `kotlinx.datetime.LocalDate`/`LocalDateTime` hit the same "foreign, unmarked" default as the
domain-model issue did, but for a third-party library instead of our own code — same
`stabilityConfigurationFiles` fix shape as a domain type would need, not applied because one data
point isn't enough to justify a standing config file yet.

CLAUDE.md's Compose/Platform Rules links here next to the `ImmutableList` convention bullet.

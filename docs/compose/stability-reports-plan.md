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
| 1 | Gradle wiring: opt-in stability reports | S | ⬅ NEXT |
| 2 | Aggregator script + first repo-wide audit + doc | M | Not started |

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
  4 near-duplicate report sets for the same commonMain code. Restrict to `jvm` only — it's the
  cheapest target to compile (no iOS toolchain, no Gplay/Fdroid flavor split) and commonMain stability
  is target-agnostic. **Known gap this leaves**: any composable declared only in an `androidMain`
  source set (Android-specific screens/dialogs, if any exist) won't be scanned. Acceptable for a
  periodic audit tool; call it out in the doc rather than solving it now.
- `androidApp` is not KMP (single `androidJvm` target) — no `targetKotlinPlatforms` restriction
  needed there.
- `build-logic` is an **included build** (`settings.gradle.kts`: `includeBuild("build-logic")`), not
  a subproject — the root's `alias(libs.plugins.ktlint)` does not reach it, so build-logic changes
  need `./gradlew :build-logic:convention:build` (or just any normal build, which compiles it first)
  as their check, not `ktlintCheck`.

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
   fun Project.configureComposeStabilityReports(restrictToJvm: Boolean = false) {
       if (!project.hasProperty("composeStabilityReport")) return
       extensions.configure<ComposeCompilerGradlePluginExtension> {
           metricsDestination.set(layout.buildDirectory.dir("compose_reports"))
           reportsDestination.set(layout.buildDirectory.dir("compose_reports"))
           if (restrictToJvm) {
               targetKotlinPlatforms.set(setOf(KotlinPlatformType.jvm))
           }
       }
   }
   ```
   (Confirm the exact `ComposeCompilerGradlePluginExtension` import path and `targetKotlinPlatforms`
   setter compile cleanly — decompiled signature above, not yet compiled.)
2. Call it from both convention plugins, right after `apply("org.jetbrains.kotlin.plugin.compose")`:
   - `KmpLibraryComposeConventionPlugin.kt`: `configureComposeStabilityReports(restrictToJvm = true)`
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

**Result:** _(fill in after running)_

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

**Result:** _(fill in after running)_

## Considered and deferred

| Idea | Why deferred |
|------|--------------|
| Always-on reports (no `-P` gate) | Pure I/O/build-time cost for a diagnostic that's useful a few times a year, not every build — same reasoning as `-PgplayBuild` being opt-in. |
| CI gate failing the build on new unstable classes | No baseline yet. Revisit only if task 2's audit turns up a real, recurring problem worth enforcing — premature before a single data point exists. |
| `stabilityConfigurationFiles` (mark external types stable) | Nothing has shown a false positive needing suppression. Add only in response to a concrete finding, not preemptively. |
| `@NonSkippableComposable` sweep for "skipping isn't free" cases | The article's own edge case, and needs profiling evidence (a lightweight composable eating cycles on `.equals()` over a huge stable graph) to be worth anything — no such evidence exists here. |

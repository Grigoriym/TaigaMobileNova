# Detekt — Codacy crashes, and our own detekt analyzes almost nothing

**Status:** Fixed on `fix/detekt` — items 2, 3 and 4 resolved; item 1 needs a Codacy dashboard change
**Found:** 2026-08-01, while releasing v2.1.5 (PR #336 CI was red)
**Link:** no GitHub issue yet

## Summary

Three separate problems, found in this order:

1. Codacy's Detekt tool crashes on every PR — it runs detekt **1.23.8** against our
   **2.x** config file.
2. Our own `./gradlew detekt` fails outright on one stale config property. Nobody
   noticed because the CI step is commented out.
3. Even with that fixed, detekt analyzes **2 of 77 modules** — it never sees KMP
   source sets. It has effectively never run on this project.

Plus one unrelated bug found along the way: two test files that are never compiled or run.

None of this was caused by the v2.1.5 release; it fails on every PR.

## 1. Codacy: detekt 1.23.8 vs our 2.x config

|  | Version | Config it reads |
|---|---|---|
| Our Gradle build | detekt **2.0.0-alpha.5**, plugin id `dev.detekt` (`gradle/libs.versions.toml:27`) | `config/detekt/detekt.yml` — set at `Quality.kt:43` |
| Codacy engine `codacy-detekt:2.8.1` | detekt **1.23.8**, on Java 11 | the *same* file, auto-discovered |

Detekt 2.x renamed the Maven group from `io.gitlab.arturbosch.detekt` to `dev.detekt`;
the Codacy stack trace shows the old package, which is how the version is pinned down.

The crash:

```
java.lang.ClassCastException: class java.util.ArrayList cannot be cast to class java.util.Set
    at io.gitlab.arturbosch.detekt.api.Rule.getAliases(Rule.kt:39)
    at io.gitlab.arturbosch.detekt.api.Rule.visitCondition(Rule.kt:61)
    at io.gitlab.arturbosch.detekt.core.Analyzer.analyze(Analyzer.kt:143)
```

In 1.23.8 the `Rule.aliases` getter is typed `Set<String>`, but its YAML loader returns
the parsed list as an `ArrayList` unchecked — so the cast fails the first time any rule
visits any file. Our config has ~20 such entries (`config/detekt/detekt.yml:185`, `304`,
`313`, `320`, `324`, `328`, `339`, …), all of them straight out of detekt's own generated
default config — nothing hand-written wrong.

Because it throws inside `Analyzer.analyze`, *every* file kills the container and the
tool exits 1 regardless of what changed. `TimberLogger.kt` and `ModulesNavDestination.kt`
in the failure output are just whichever files each shard reached first.

`.codacy.yml` only has `exclude_paths`, which filters analyzed files — it cannot stop
Codacy from discovering the config file.

## 2. `./gradlew detekt` is broken

```
Execution failed for task ':feature:projects:mapper:detekt'
> Run failed with 1 invalid config property.
    - Property 'potential-bugs>Deprecation>excludeImportStatements' is misspelled or
      does not exist. Allowed properties: [active, aliases]
```

`config/detekt/detekt.yml:430` — detekt 2.x dropped that property from the `Deprecation`
rule. Validation runs before analysis, so nothing is analyzed at all.

> Careful: `excludeImportStatements` at line **691** is a different rule
> (`style > MaxLineLength`) and is still valid in 2.x. Only line 430 is stale.

This went unnoticed because the CI step is commented out — `.github/workflows/code_analysis.yml:50-51`:

```yaml
#      - name: Run Detekt
#        run: ./gradlew detekt
```

## 3. Detekt sees almost no source (the real problem)

With line 430 removed locally, the build goes green — but green is meaningless:

```
$ ./gradlew detekt --continue --rerun-tasks
> Task :uikit:detekt NO-SOURCE
> Task :core:crash-api:detekt NO-SOURCE
> Task :feature:login:ui:detekt NO-SOURCE
  ... 75 of 77 tasks NO-SOURCE ...
BUILD SUCCESSFUL
2 actionable tasks: 2 executed
```

The detekt Gradle plugin defaults `source` to `src/main/{java,kotlin}` and
`src/test/{java,kotlin}`. Our KMP modules use `src/commonMain/kotlin`,
`src/androidMain/…`, `src/iosMain/…`, `src/jvmMain/…` — none of which it looks at.
`configureLinting()` (`Quality.kt:40-45`) sets `buildUponDefaultConfig`, `parallel`,
`config` and `allRules`, but never `source`.

So detekt has been a no-op across the entire project for as long as it's been configured.

The two modules that *do* have source are `feature/tasks/mapper` and
`feature/projects/mapper` — and only because of finding 4 below, not because they're
configured differently. All modules use the same `taigamobile.kmp.library` plugin.

## 4. Two test files are never compiled or run (separate bug)

```
feature/tasks/mapper/src/test/java/com/grappim/taigamobile/feature/tasks/mapper/TaskMapperTest.kt
feature/projects/mapper/src/test/java/com/grappim/taigamobile/feature/projects/mapper/ProjectMapperTest.kt
```

`src/test/java` is the legacy Android/JVM layout and belongs to **no KMP source set**
(every other mapper module correctly uses `src/commonTest`). Verified:

```
$ ./gradlew :feature:projects:mapper:jvmTest --rerun-tasks
BUILD SUCCESSFUL — 228 tasks executed
$ find feature/projects/mapper/build/test-results -name "*.xml"
(nothing)
```

Zero test results produced. These two tests never run and contribute nothing to Kover
coverage. They are also the only reason detekt found any source at all.

## What was done

1. **Deleted** `excludeImportStatements` from the `Deprecation` block (`config/detekt/detekt.yml`). Line 691 (`style > MaxLineLength`) left alone — still valid.
2. **Wired `source`** in `DetektExtension` (`Quality.kt`) from `KotlinSourceSetContainer`, filtering out any src dir under the module build dir. Result: **75 NO-SOURCE → 0**; detekt now analyzes **805 Kotlin files across 78 modules**.
3. **Moved** `TaskMapperTest.kt` and `ProjectMapperTest.kt` to `src/commonTest/kotlin/…`; `src/test` trees deleted. Both now run — see "Dead tests were broken" below.
4. **Uncommented** the Detekt step in `.github/workflows/code_analysis.yml`.
5. **Codacy — still open, needs a dashboard change.** Turn Detekt off under
   Repository → Code patterns → Detekt. It cannot read a 2.x config, and now duplicates a
   check we own properly.

Do **not** strip `aliases:` from the config to satisfy Codacy's 1.23.8 — the 1.x and 2.x
config schemas diverge in more places than that, so it would degrade the real config to
appease a scanner we're already duplicating.

## Violation count: 12, no baseline needed

The open question was how big the backlog would be. Answer: **12**, and all were KMP or
Compose idiom colliding with JVM-oriented rules — no real defects. A `baseline.xml` was
not needed.

| Rule | Count | Resolution |
|---|---|---|
| `MatchingDeclarationName` | 6 | Config: excluded `**/androidMain/**`, `**/iosMain/**`, `**/jvmMain/**`. Platform files are named for their commonMain counterpart (`Koin.kt` ↔ `Koin.android.kt`), so the name deliberately differs from the declaration (`PlatformComponentModule`). |
| `TooGenericExceptionThrown` | 3 | Config: added the standard test-source excludes already used by many rules in this config. Sites were `throw RuntimeException("Network error")` in `SprintsRepositoryImplTest`. |
| `EmptyFunctionBlock` | 2 | Inline `@Suppress` + comment on the iOS/JVM `OpenByDefaultSettingsButton` actuals — deliberate no-ops on platforms without the setting. |
| `FunctionNaming` | 1 | Inline `@Suppress` on `MainViewController()` — PascalCase is required; Swift calls into it. |

An earlier run reported **1995** issues in `:core:storage` and hundreds elsewhere. Those
were entirely generated code (Room, BuildKonfig, Compose resource accessors) reachable
because KMP source sets carry generated dirs. Filtering build-dir srcDirs in step 2
removed all of them; detekt's own `build/` exclude does not match the absolute paths the
source sets hand over.

## Dead tests were broken (found by fixing item 4)

Once `TaskMapperTest` actually ran, **2 of its 13 tests failed**. Both had drifted from
the shared `:testing` factories while nobody was running them:

- `toDomain should map basic fields correctly` asserted `DueDateStatus.DueSoon`, but
  `getWorkItemResponseDTO()` sets `dueDateStatusDTO = null`. It also compared
  `result.assignee` against an unrelated random `getUser()`. Fixed to assert `null` and to
  map the response's own `assignedToExtraInfo`.
- `toDomain should map tags correctly` compared the result against two fresh `getTag()`
  calls, which return **random** values unrelated to the response. This test could never
  have passed. Fixed to compare against `tagsMapper.toTags(response.tags)`.

`ProjectMapperTest` passed unchanged.

## Follow-up worth considering (not done)

`config/detekt/detekt.yml:27-33` excludes `IssuesReport` and `FileBasedIssuesReport` from
console output, so a CI failure prints only `Analysis failed with N issues` with no
file/rule detail — you have to open the HTML report. Now that detekt gates CI, consider
re-enabling `FileBasedIssuesReport` so the log names the violations.

Also: `configureLinting()` is applied only by the KMP library convention plugins, so
`androidApp` and pure `taigamobile.kotlin.library` modules are still not linted by detekt.

## Reproduction

```bash
./gradlew detekt                              # fails: invalid config property
# remove config/detekt/detekt.yml:430, then:
./gradlew detekt --continue --rerun-tasks     # succeeds; 75/77 tasks NO-SOURCE
./gradlew :feature:projects:mapper:jvmTest --rerun-tasks
find feature/projects/mapper/build/test-results -name "*.xml"   # empty
```

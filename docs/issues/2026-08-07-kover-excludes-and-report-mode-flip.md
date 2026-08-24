# 2026-08-07 — Kover's `excludes` "applied partially, and differently by the two tasks"

**Status:** Done
**Link:** [docs/revisit.md #8](../revisit.md#8-kovers-excludes-are-applied-partially-and-differently-by-koverxmlreport-and-koververify)
**Updated:** 2026-08-07

Blocks [#10](../revisit.md) (Ktor plugins wrongly excluded) and [#14](../revisit.md) (coverage floor
~35–42 points below actual).

## Report

`docs/revisit.md` #8, opened 2026-08-03 and amended four times. Three distinct claims:

1. **The two report tasks disagree.** Measured at `af8a185a`: `koverXmlReport` 65.30 % line /
   45.88 % branch, `:koverVerify` 60.47 % / 40.29 %, "what the configured excludes should produce"
   71.97 % / 49.73 %. Stated cause: "the divergence is entirely in how each applies the excludes."
2. **Individual `excludes` entries silently no-op.** Of the seven `packages(…)` entries, only
   `strings.generated.resources`, `core.storage.db` and `core.storage.cache` were said to take
   effect; `core.storage.db.dao`, `core.storage.db.wrapper`, `core.storage.di` and
   `core.storage.network` "do nothing". Also `**.*Module` was said to leave `DBModule`,
   `AuthDataStoreModule`, `PlatformDBModule`, `PlatformStorageModule` in the report. "**Every
   failing exclusion is in `:core:storage`.** No mechanism found for why that module is special;
   that is the thing to work out first."
3. **`koverXmlReport` flips between stable modes.** Observed class counts 742 / 744 / 781 / 787 /
   798 / 821 / 822 / 823 / 827 / 849 / 854. Four successive hypotheses about the trigger (build-file
   change; test-sources-only edit; `:testing` edits; clean tree) were each contradicted by a later
   session. CLAUDE.md's Testing section now carries ~60 lines of workarounds built on this.

**Environment:** Kover **0.9.9** (`gradle/libs.versions.toml:31`), Gradle 9.6.1, 77 modules. The
Kover *project* plugin is applied to every KMP module by the convention plugin
(`build-logic/convention/src/main/kotlin/com/grappim/taigamobile.buildlogic/KmpConfiguration.kt:9`);
the root `build.gradle.kts` aggregates 76 of them via `kover(projects.…)` dependencies and is the
only place `reports.filters` is configured. The Kover *settings* plugin is not used
(`settings.gradle.kts` has no Kover entry).

**What the report omits:** no session ever read Kover's own implementation. Every claim about what a
pattern means, and about which task applies filters where, was inferred from diffing report
artifacts. Claim 2's evidence method — "delete an entry and diff the package list" — cannot
distinguish *broken* from *redundant*.

## Findings

Kover 0.9.9 ships a sources jar; all mechanism claims below are read from it, at
`~/.gradle/caches/modules-2/files-2.1/org.jetbrains.kotlinx/kover-gradle-plugin/0.9.9/…-sources.jar`
and the matching `kover-features-jvm-0.9.9-sources.jar`.

### 1. Wildcard semantics: `*` matches dots. `packages()` really does cover subpackages.

`KoverFeatures.koverWildcardToRegex` (`kotlinx/kover/features/jvm/KoverFeatures.kt:20-28`):

> Replaces characters `*` to `.*`, `#` to `[^.]*` and `?` to `.` regexp characters.

So `*` crosses package boundaries; `#` is the non-dot wildcard. And `packages(…)` is sugar for one
class pattern — `ReportsImpl.kt:349`:

```kotlin
private fun String.packageAsClass(): String = "$this.*"
```

`packages("…core.storage.db")` therefore becomes `…core.storage.db.*` → regex `….*`, which matches
`…core.storage.db.dao.SprintDao_Impl` and `…core.storage.db.entities.SprintEntity` alike. The KDoc
("Add all classes in specified package **and its subpackages**", `KoverReportsConfig.kt:653-664`) is
accurate.

**Consequence for claim 2:** `core.storage.db.dao` and `core.storage.db.wrapper` are **redundant**
with `core.storage.db`, not broken. Deleting a redundant entry changes nothing — which is exactly
what the session observed and misread as a silent no-op. The config is correct as written.

### 2. The two tasks cannot apply excludes differently — they are handed the same object.

`VariantReportsSet` configures every report task for a variant from one config instance
(`appliers/tasks/VariantReportsSet.kt`):

- `xmlTask` — `filters.set((config.filters).convert())` (line 87)
- `doVerifyTask` — `filters.set((config.filters).convert())` (line 110)

`convert()` (lines 218-231) reads the same `excludes.classes` property both times, and both tasks
resolve their class universe through the same inherited `AbstractKoverReportTask.collectAllFiles()`.
There is no second filter path.

**Verified empirically.** Ran `./gradlew :koverXmlReport :koverVerify` in **one invocation** with
both `minValue`s temporarily set to 99 so the gate prints its own figures:

```
Rule 'Line coverage' violated: lines covered percentage is 94.872300, but expected minimum is 99
Rule 'Branch coverage' violated: branches covered percentage is 79.873600, but expected minimum is 99
```

Summing the counters of the `report.xml` written by that same invocation (741 classes):

| | covered/total | % | `:koverVerify` said |
|---|---|---|---|
| LINE | 9214/9712 | **94.872323 %** | 94.872300 % |
| BRANCH | 1643/2057 | **79.873602 %** | 79.873600 % |

Agreement to six significant figures. **Claim 1 is false.** The historical ~5-point gap was a
comparison between a `:koverVerify` run and an XML from a *different* invocation — i.e. claim 1 was
always an instance of claim 3, not a separate defect. CLAUDE.md already records the same coincidence
("agree to four decimal places", 2026-08-03) without drawing the conclusion.

The comment currently in `build.gradle.kts:95-99` — "the two tasks apply the excludes above
differently, and neither applies them in full" — is therefore wrong, and it is the first thing a
reader of the gate sees.

### 3. The class universe is "whatever compiler output exists on disk", which is what actually varies.

`AbstractKoverReportTask.collectAllFiles()` (`tasks/reports/AbstractKoverReportTask.kt:83-86`):

```kotlin
private fun collectAllFiles(): ArtifactContent {
    val local = ArtifactContent(projectPath, emptySet(), emptySet(), additionalBinaryReports.get())
    return local.joinWith(artifacts.files.map { it.parseArtifactFile(rootDir).filterProjectSources() }).existing()
}
```

`.existing()` drops artifact entries whose files are absent. The report is generated over class
files found by walking those directories at report time — so a class is counted **iff its
compilation output happens to exist on disk**, regardless of whether any test could reach it.

What feeds those directories: `locateKotlinMultiplatformVariants`
(`locators/KotlinMultiPlatformLocator.kt:32-38`) locates the plain `jvm` target **and** the KMP
Android library target (`locateAndroidMultiplatformLibrary`, line 84) — both as `JvmVariantOrigin`.
Native targets are not located at all, so iOS classes never appear. The root aggregates each
module's **total** variant, and `TotalVariantArtifacts.mergeWith` folds every located variant in.

**So the root report's denominator = JVM outputs + Android-KMP-library outputs that exist right
now.** Direct evidence in today's report, which is otherwise a clean, fully-filtered run:

```
com/grappim/taigamobile/utils/formatter/decimal/DecimalFormatterModule_androidKt  LINE 0/1  (0 covered)
com/grappim/taigamobile/utils/formatter/decimal/DecimalFormatterModule_jvmKt      LINE 1/1  (covered)
```

An Android-variant class, permanently 0 % because CI runs `jvmTest` only, sitting in the
denominator of a "good" 742-class run.

This explains the whole non-leaky spread (742 / 744 / 781 / 787 / 798) mechanically and without
appealing to excludes at all: an Android app build, an iOS link, or a KSP re-run leaves outputs
behind that a subsequent `koverXmlReport` counts; a fresh CI checkout compiles only what `jvmTest`
needs. It also explains why every prior trigger hypothesis failed — the trigger was never the *edit*,
it was which compilations had run at some point since the last `clean`.

**Inference, not verified:** that the same mechanism produces the 821–854 counts. See Open questions.

### 4. `kover-rank.py` matches packages by equality where Kover matches by prefix — a real bug.

`docs/testing/kover-rank.py:55-57`:

```python
def is_excluded(package: str, class_name: str) -> bool:
    if package in PACKAGES:
        return True
```

Exact set membership. Kover's equivalent is `pkg.*`, which is a prefix match (finding 1). So the
script keeps classes in any **subpackage** of a listed package that is not itself listed.
`com.grappim.taigamobile.core.storage.db.entities` is precisely that case: excluded by the real gate
via `…storage.db.*`, kept by the script.

This is the documented caveat in the script's own docstring (lines 14-18) — "it stops at 745,
because the three `…core.storage.db.entities` classes are named by neither the root `excludes` nor
the lists below … LINE runs ~53 lines high" — and the diagnosis there is wrong in the same way claim
2 is: the root `excludes` **do** name them, transitively. The script is the only thing out of sync.

Because CLAUDE.md instructs every session to treat `kover-rank.py`'s output as "the gate number",
this defect has been silently inflating the LINE denominator of every measurement taken on an
Android-output-present report.

The script also **over**-excludes: against today's report it keeps 741 of 742 classes, dropping
`com.grappim.taigamobile.utils.ui.di.UtilsUiModule`, which the gate counted. Net effect on today's
numbers is small (BRANCH 79.85 % scripted vs 79.87 % actual) but it is not zero, and it is in the
opposite direction from the package bug.

### 5. Today's runs show 0–1 exclude leaks, not 20.

Applying a faithful port of Kover's pattern engine (`*`→`.*`, `#`→`[^.]*`, `?`→`.`, full match, all
120 patterns the root block generates) to both of today's reports:

| Report | Classes | Classes matching an exclude pattern yet present |
|---|---|---|
| on-disk baseline (previous session, clean tree) | 742 | **1** — `utils.ui.di.UtilsUiModule` vs `**.*Module` |
| after today's `:koverXmlReport :koverVerify` | 741 | **0** |

The single leak is *intermittent*: it is the entire 742 → 741 class difference between the two runs.
CLAUDE.md's "±2 classes (Koin-generated `LoginDataModule`) — that much wobble is noise, not a mode"
is the same phenomenon; both are hand-written Koin `@Module`/`@Configuration` classes
(`utils/ui/src/commonMain/…/di/UtilsUiModule.kt` is 9 lines, no branches).

So the exclusion engine works, with a rare single-class anomaly worth ≤1 line and ≤2 branches.

### 6. Current actual coverage, measured from the gate (for #14)

**LINE 94.8723 %, BRANCH 79.8736 %**, against a floor of 58 / 38 — the floor is ~37 line and ~42
branch points low. The last recorded figure (2026-08-05, `kover-rank.py`) was 86.73 / 78.38; tasks
15–21's Compose UI Screen tests account for the rise.

## Root cause

There is **no defect in the `excludes` block, and none in how the two tasks apply it.** Claims 1 and
2 are artifacts of the measurement method: claim 1 compared two different invocations, and claim 2
read redundancy (`packages()` already covering subpackages, `KoverReportsConfig.kt:653`,
`ReportsImpl.kt:349`) as failure.

The one real, mechanically-explained defect is the **non-determinism of the report's class
universe**: `AbstractKoverReportTask.collectAllFiles()` ends in `.existing()`
(`tasks/reports/AbstractKoverReportTask.kt:85`), and the root aggregates each module's *total*
variant, which includes the KMP Android library target's compilations
(`locators/KotlinMultiPlatformLocator.kt:84`). The denominator is therefore a function of which
compilations have run since the last `clean` — not of the source, the tests, or the filters. Those
Android-variant classes can never be covered, because CI runs `jvmTest` only and the repo has no
Android unit-test source set by design.

The second real defect is in the repo's own tooling, not Kover: `kover-rank.py:56` matches excluded
packages by equality instead of by prefix.

## Impact

- **Every coverage measurement since 2026-08-03** has been taken through a lens built on a false
  premise. The cost is not wrong code — it is ~60 lines of CLAUDE.md written to work around a
  non-existent filter bug, and a per-session tax of comparing reports, running `kover-diff.py`, and
  reasoning about "which side of the flip" a run landed on.
- **#14 is blocked on nothing.** Its stated reason for not raising the floor was the suspicion that
  `:koverVerify` itself flips modes. Finding 2 rules that out: the gate reports the universe of the
  same invocation, and the correct procedure is simply to read it in the same invocation.
- **#10 is unaffected by any of this** and can proceed independently; it is a question about whether
  five Ktor plugins *should* be excluded, not about whether the exclusion works.
- No runtime impact. No user-visible impact. CI has never been wrong — it always measures a fresh
  checkout, which is the deterministic case.

## Open questions

1. **The 821 / 822 / 823 / 827 / 849 / 854 counts with ~20 suffix leaks were not reproduced today**
   and are not explained by finding 3. Today's two runs leaked 1 and 0 classes. Finding 5 shows the
   leak mechanism exists and is intermittent at 1-class scale, so the same bug at 20-class scale is
   the natural inference — but it is an inference. **Does not block a decision:** the fix for finding
   3 (whatever is chosen) plus a corrected `kover-rank.py` makes any such run recognisable and
   correctable, and CI never sees one.
2. **Why does `UtilsUiModule` sometimes survive `**.*Module`?** Both observed cases are hand-written
   Koin `@Module` classes. The filter application itself lives in the closed reporter artifact, not
   in either sources jar. Immaterial at ≤1 line; worth a Kover upstream issue if it recurs at scale.
3. **Is there a DSL path to a JVM-only aggregated report?** `KoverVariantCreateConfig.addWithDependencies`
   looks right but is ruled out by its own contract: the root is not a Kotlin project, so it has no
   `jvm` variant, and with `optional = true` "the variant will not be searched even in dependencies"
   (`KoverVariantConfig.kt:271`). `excludedSourceSets` filters by *compilation* name
   (`JvmVariantArtifacts.kt:57-72`) and both targets' compilation is named `main`, so it cannot
   discriminate either. This is why option C below is a suffix/package exclusion rather than a
   variant selection.

## Options

### A — Correct the record; fix the script; raise the floor. No build-config change.

Rewrite `docs/revisit.md` #8 to what findings 1–5 establish, delete the false comment at
`build.gradle.kts:95-99`, cut CLAUDE.md's mode-flip workarounds down to the one true rule ("the
denominator depends on which compilations exist; take before/after from the same invocation, or
`clean` first"), fix `kover-rank.py:56` to prefix matching, and raise the floor from 58/38 to ~92/77
using the same-invocation reading.

- **Pros:** removes the largest single source of wasted session time in the repo; zero risk to the
  build; unblocks #14 in the same commit; the script fix is two lines and provably correct against
  finding 1.
- **Cons:** the local denominator stays non-deterministic, so a local before/after still needs care
  (though the rule becomes "same invocation, or clean" rather than three pages of heuristics).
  Permanently-0 % Android-variant classes stay in the report, still depressing the number by an
  unknown amount and still generating the `*_androidKt` red herrings CLAUDE.md warns about.
- **Risk:** low. **Blast radius:** docs, one script, two `minValue`s.

### B — A, plus make CI's report the only one anyone quotes.

Add a `koverXmlReport` invocation to the workflow that runs after `clean` (it effectively already
does) and document that local numbers are indicative only; stop recording local figures in
`docs/`/CLAUDE.md at all.

- **Pros:** cheapest possible determinism — sidesteps finding 3 rather than fixing it.
- **Cons:** makes the feedback loop for a coverage-driven session much slower (a full clean build to
  get a trustworthy ranking), which is precisely what task 9a-style sweeps depend on. Effectively
  trades one tax for a worse one.
- **Risk:** low. **Blast radius:** CI workflow + docs.

### C — A, plus exclude the un-coverable Android-variant classes from the report.

Add to the root `excludes`: the `_androidKt` file-facade suffix, Room's generated `*_Impl` /
`TaigaDB_Impl`, and `core.storage.utils` (where `StringPreference` lives, `androidMain`-only). Re-tune
the floor in the same commit.

- **Pros:** the denominator stops depending on whether an Android build ran, which is finding 3's
  practical consequence; kills the `*_androidKt` / `StringPreference` / `_Impl` noise that three
  separate CLAUDE.md paragraphs and revisit #17 exist to explain; makes local and CI numbers
  comparable.
- **Cons:** a name-pattern denylist is exactly the brittleness #10 complains about — it hides real
  Android `actual` implementations that *are* untested, trading an honest-but-noisy number for a
  flattering one. It also does not fix the mechanism, only its most visible symptom, so a future
  target (a new Android-only class) reintroduces it silently. Requires a floor re-tune, which
  entangles this measurement change with #14's ratchet.
- **Risk:** medium. **Blast radius:** every module's report, Codecov history discontinuity, the CI
  gate.

### D — Won't fix; close #8 as "no defect".

- **Pros:** honest about findings 1–2.
- **Cons:** leaves the false comment in the build script and the workaround pile in CLAUDE.md, which
  is where the actual cost is. The measurement non-determinism is real even if the reported cause was
  not.

**Recommendation: A.** It is the only option whose entire content is *removing incorrect
information*, and it captures nearly all the value: the mode flip stops being mysterious, #14 becomes
a two-line change with a defensible number, and `kover-rank.py` starts agreeing with the gate. C is
tempting and probably right eventually, but it changes what the project measures at the same moment
as raising the floor — worth doing deliberately, as its own task, once A has made the baseline
trustworthy. Recommend deferring C to a new revisit entry rather than folding it in here.

## Decision

**Option A, approved by gregory 2026-08-07.** Correct the record, fix `kover-rank.py`, raise the
floor. No change to what the report measures — option C (excluding the un-coverable Android-variant
classes) is deferred to its own revisit entry so that a measurement change and a floor ratchet never
land in the same commit.

Broken into three independently-verifiable parts:

1. **`kover-rank.py` prefix matching.** Verified by asserting `is_excluded` on a
   `…core.storage.db.entities` class — `False` before, `True` after — plus an unchanged filtered
   total on today's report (which contains no such class, so the fix must be a no-op there).
2. **Floor raise + delete the false comment** in the root `build.gradle.kts`. Verified by
   `./gradlew :koverVerify` staying green at the new bounds.
3. **Docs.** `docs/revisit.md` #8 and #14, CLAUDE.md's Testing section, and this file's status. No
   automated verification; the check is that no surviving sentence contradicts findings 1–5.

## What landed

Three parts, all verified:

1. **`docs/testing/kover-rank.py`** — `is_excluded` now matches excluded packages by prefix
   (`package == p or package.startswith(p + ".")`) instead of by equality, mirroring Kover's
   `packages("a.b")` → `a.b.*`. Verified against six cases including the two that matter:
   `…core.storage.db.entities` now excluded (was kept), `…core.storagex` still kept (a naive
   `startswith` without the dot would have wrongly dropped it). Filtered totals on a report containing
   no such class are unchanged, i.e. the fix is a no-op where it should be. The docstring's "745 not
   742 / LINE ~53 high" caveat and its wrong diagnosis were replaced with the real mechanism.
2. **Root `build.gradle.kts`** — the comment asserting the two tasks apply excludes differently is
   gone, replaced with the same-invocation rule and a pointer here. Bounds raised **58 → 92** (line)
   and **38 → 77** (branch), ~3 points under the measurement. `./gradlew :koverVerify` green;
   `./gradlew ktlintCheck` green. The `excludes` block itself was **not** touched — finding 1 says it
   is correct as written, and the three redundant `packages(…)` entries were left in place rather than
   pruned, so the block still mirrors `kover-rank.py`'s list line for line.
3. **Docs** — `docs/revisit.md` #8 rewritten (its ~100 lines of disproved analysis and four
   contradicted trigger hypotheses deleted rather than archived, so the next reader is not sent down
   the same path), #14 closed with the floor raise, the stale "still open" table reduced to the seven
   genuinely-open entries, and new entry **#23** filed for the deferred option C. CLAUDE.md's Testing
   section lost ~100 lines of mode-flip workarounds; what replaced them is the two true rules
   (same-invocation agreement; the denominator depends on which compilations exist) plus the surviving
   advice that was always correct — how to read the gate's own percentages, `kover-rank.py`,
   `kover-diff.py`, and the class-scope denominator caveat. The leak-check one-liner survives with a
   corrected expectation: 0–1 leaks is normal, ~20 means re-run.

**Side confirmation:** after part 1, `kover-rank.py` over the current report prints BRANCH 79.87 % /
LINE 94.87 % against the gate's 79.8736 % / 94.8723 % — the script and the gate now agree, which is
what CLAUDE.md had been asserting without it being true.

**Deliberately not done:** option C (revisit #23); the `UtilsUiModule` Kover anomaly (open question 2,
immaterial and not ours to fix); revisit #10, which is independent of everything here.

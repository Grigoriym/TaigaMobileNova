# Kover coverage sweep heuristics

Reference material for running a future coverage sweep — ranking packages and classes by missed
branches or lines, reading Kover's XML report accurately (`koverXmlReport` vs `:koverVerify`
agreement, the `kover-rank.py`/`kover-diff.py` scripts), and recognising the recurring `mb`/`cb`
signatures on a report line that mean "this residual isn't worth a test." Split out of `CLAUDE.md`'s
`## Testing` section on 2026-08-09 ([docs/revisit.md](../revisit.md) #28) because it's read rarely
compared to the day-to-day conventions that stayed there. See `CLAUDE.md`'s Testing section for the
coverage-floor-is-a-ratchet rule and the current line/branch bounds.

**The traps when touching the coverage-floor numbers:**

- **`koverXmlReport` and `:koverVerify` agree exactly — within a single invocation.** Both are handed
  the same filter object and the same artifacts (`VariantReportsSet.kt:87` and `:110`). Measured
  2026-08-07: XML 94.872323 % line / 79.873602 % branch against the gate's own 94.872300 % /
  79.873600 %. **Older notes claiming the two tasks "apply the excludes differently" were wrong**, as
  was the claim that individual `excludes` entries silently no-op — `packages("a.b")` becomes the
  pattern `a.b.*` and Kover's `*` matches dots, so a listed package covers its subpackages and the
  `db.dao`/`db.wrapper` entries are merely redundant. Full write-up:
  [docs/issues/2026-08-07-kover-excludes-and-report-mode-flip.md](docs/issues/2026-08-07-kover-excludes-and-report-mode-flip.md).
  Never set a bound from the Codecov dashboard.
- **What varies between runs is the denominator, not the filtering.** Kover's report task ends its
  file collection in `.existing()` (`AbstractKoverReportTask.kt:85`) and the root aggregates each
  module's *total* variant, which includes the KMP Android library target — so a class is counted iff
  its compiler output is on disk *right now*. An Android build, an iOS link or a KSP re-run since the
  last `clean` changes the class universe; 742 / 744 / 781 / 787 / 798 / 821 / 854 have all been seen
  on the same source. **CI is always the deterministic case** (fresh checkout, JVM compilations only),
  which is why it has never been wrong. Locally: take before and after from runs with the same
  compilation state, or simply re-run — the count is not sticky within a session, and one re-run is
  cheaper than reasoning about a mismatched pair. Every Android-variant class it adds is permanently
  0 % under `jvmTest`; that half is [revisit #23](docs/revisit.md), still open.
- **To read `:koverVerify`'s own percentages, temporarily set both `minValue`s to 99** in the root
  `build.gradle.kts` and run it: it names each violated rule and prints the actual figure. There is no
  other way to get the number the gate is actually comparing against. `git checkout build.gradle.kts`
  afterwards.
- **A moved percentage is not a moved numerator.** Kover's totals here shift when the denominator
  changes, so compare `covered`/`total` counts between reports before concluding coverage regressed.
  Reading percentages alone once made ~100 new tests look like a 2-point *drop*.
- **The denominator rule is about report-level totals and does not extend to a single class or
  package.** An unexecuted class is reported with *fewer* branches than it has, so covering it grows
  its own denominator: `TaigaSessionStorageImpl` went BRANCH 0/4 → **14/14** and LINE 29/49 → **55/55**
  between two same-class-count runs. Compare `covered` against the **after** denominator at class
  scope, and don't read the growth as a bad measurement.
- **Sanity-check a report before quoting it**, in one command:

  ```bash
  python3 -c "
  import xml.etree.ElementTree as ET
  n=[c.get('name') for p in ET.parse('build/reports/kover/report.xml').getroot().findall('package') for c in p.findall('class')]
  print(len(n), 'excluded-suffix leaks:', len([x for x in n if x.split('/')[-1].split('\$')[0].endswith(('Screen','Widget','Plugin'))]))"
  ```

  **0 or 1 leaks is normal.** Kover has a rare intermittent bug where a single class survives a
  pattern it matches — the two ever seen are `UtilsUiModule` and `LoginDataModule`, both hand-written
  Koin `@Module` classes worth ≤1 line and ≤2 branches, and one of them accounted for an entire
  742-vs-741 difference between two runs minutes apart. A count near 20 means the report is not usable
  as-is: re-run, and if it persists, note it on the issue doc's open question 1 (it has never been
  reproduced deliberately).
- **`kover-rank.py` re-applies the excludes to whatever report you have** —
  [docs/testing/kover-rank.py](docs/testing/kover-rank.py). It filters by the same suffix/package
  rules as the root `build.gradle.kts`, prints the kept class count and the filtered totals, and ranks
  packages by missed branches, so a report carrying stray Android-variant classes still gives a usable
  ranking. Its package matching was equality-based until 2026-08-07 and therefore kept
  `core.storage.db.entities` (~53 lines) that the real gate excludes — that is the true story behind
  the old "745 not 742, LINE runs high" caveat. Keep its lists in sync when the `excludes` block
  changes.
- **When two reports still disagree, diff their per-package denominators rather than discarding the
  measurement.** The package you care about usually has an *identical* denominator in both, which
  makes a before/after table valid anyway — this is how the `feature/projects/data` and
  `feature/kanban/ui` tables in
  [the archived task 9a write-up](docs/archive/testing-improvement-plan-tasks-0-9f.md) survived
  742-vs-854 and 744-vs-854 pairs. To confirm a delta is caused by the change rather than by build
  staleness, `git stash -u` and re-run: a clean-tree re-run that reproduces the baseline to the digit
  settles it.
  **Better than checking the one package you care about: diff *every* counter in both reports at once
  and assert nothing else moved.** Load `{(name, type): (covered, total)}` for every `<package>` *and*
  `<class>` from each report, then print the entries whose values differ, the entries whose
  *denominator* differs, and the size of the symmetric difference of the key sets. On 2026-08-05 that
  came back as "only `feature/issues/ui/details` moved, zero denominator changes, zero classes in one
  report but not the other" — which turns a before/after table from *probably* comparable into
  provably so, and simultaneously catches side effects in packages you would never have thought to
  look at (the `feature/userstories/ui` session moved two `feature/workitem/ui` rows). It costs one
  command and subsumes the per-package check above. **Use
  [docs/testing/kover-diff.py](docs/testing/kover-diff.py)** (`python3 docs/testing/kover-diff.py
  before.xml after.xml`) rather than retyping this snippet — added 2026-08-06 after writing it out
  ad hoc often enough that it was worth saving next to `kover-rank.py`.
  **It is also what rescues a *straddled* pair, so run it before discarding one.** In
  `feature/tasks/ui` the two reports differed by 364 classes, and the same diff showed the target
  package's own denominators (BRANCH 30, LINE 479, CLASS 31) identical in both with no class of that
  package missing from either — which makes the row provably valid even though the totals are not
  comparable at all. Read the key-set difference and the per-target denominators as two separate
  answers; only the second one gates your table.
  **Split the "moved" list by element type — class-universe noise is `<package>`-level *only*.** When
  a run counts a different set of classes, the extra ones leave or enter the key set entirely rather
  than moving, so every counter that disturbs is a package total; a `<class>` row that moves is real.
  `feature/filters/domain`
  (2026-08-05) straddled 827/20-leak → 742/0 and the diff showed 28 changed package denominators
  across `core/api`, `core/domain`, `feature/login/ui`, `feature/wiki/ui/*` — and **zero class-level
  movement outside the target package**. That last count is the isolation proof: it says the change
  touched nothing else *anywhere*, which checking your own package's denominators cannot. Print the
  two levels separately and read a non-empty class-level list as the only thing worth explaining.
- **`koverXmlReport` always writes `build/reports/kover/report.xml`.** Copy it to a distinct path
  immediately after each run. Forgetting once makes the "before" and "after" the same file, and the
  diff comes back showing nothing changed anywhere — which reads like a plausible result, not like a
  mistake. Equally, **do not write test sources while a baseline run is in flight** — the run compiles
  test sources partway through, so a file added at the wrong moment silently lands in the "before".
  Cheap check either way: confirm the baseline reports the *pre-change* figure for the class you are
  about to test before trusting it.
  **On a clean tree at the same commit as the last session's final run, you do not need a baseline
  run at all** — `koverXmlReport` comes back `UP-TO-DATE` and the `report.xml` already on disk *is*
  the baseline. Copy it aside and run the class-count/leak check on it as usual; that plus the
  pre-change-figure check is the same evidence a fresh run would give, for free. Do this before
  writing any test, since the first test source you add is what makes the task out-of-date.
- **A class excluded by name shows no movement however well you test it.** The `excludes` block
  filters by suffix — `**.*Module`, `**.*Repository`, `**.*Api`, `**.*Screen` … — check it before
  ranking a package by its missed branches, and before reading a flat delta as "the tests did
  nothing." `**.*Plugin` used to be on that list and dropped all five of `core/api`'s Ktor plugins
  (~98 lines and 38 branches of real auth and error-mapping logic) purely because their names ended
  in "Plugin" — removed 2026-08-08, see [revisit #10](docs/revisit.md), since a repo-wide grep found
  no other class the pattern was meant to catch. `**.*Module` stayed: every class it matches is a
  real Koin `@Module`.
  The suffix match is exact, so the reverse also holds: `**.*Repository` does **not** match
  `…RepositoryImpl`, and every repository impl in the project is measured normally.
  `**.*Exception` is on that list too, which hides real logic in `core/domain`
  (`NetworkException.message`, and every custom exception in the module), and
  `**.*ResultExtensionKt` is named explicitly.
  **`**.*DTO` is on it as well, so a `…/dto` package's report rows are its *non*-`DTO`-suffixed
  classes only** — `feature/userstories/dto` shows `BulkUpdateKanbanOrderRequest` and
  `CreateUserStoryRequest` at BRANCH 0/38 while `UserStoryShortInfoDTO`, which
  `UserStoryShortInfoMapperTest` covers thoroughly, has no row at all. Don't read a `dto` package's
  ranking as a statement about its DTOs.
  **An excluded class is absent from the report entirely — not listed at 0 % — so a class that is
  both excluded *and* dead is invisible to every coverage-driven ranking.** `SprintPagingSource`
  (`**.*PagingSource`) has zero references repo-wide and no report row of any kind; nothing in a
  missed-branch sweep could ever have surfaced it ([revisit #21](docs/revisit.md)). When a sweep
  closes a package, `ls` its source directory against the class names in the report before calling
  the package done — the difference is the excluded set, and it is worth a look.
- **A `*_androidKt` / `*_iosKt` class in the report is dead weight, and it can dominate a sweep row.**
  Android- and iOS-variant classes get compiled into the report, but CI runs `jvmTest` only and the
  repo has no Android unit-test source set by design — so they sit at 0 % forever. In `core/domain`
  that is **14 of the package's 16 missed branches**, all in `PlatformNetworkErrorMapper_androidKt`,
  whose JVM twin `PlatformNetworkErrorMapper_jvmKt` is **byte-for-byte identical** and already 14/14
  BRANCH / 10/10 LINE (covered incidentally by `core/api`'s `NetworkErrorMapper` tests). The logic is
  not untested; it is counted twice and only one copy is executable. **Diff the actuals before
  scoping any `expect`/`actual` package** — a `*_androidKt` row is a reason to close the row, not to
  write tests. **Since [revisit #23](docs/revisit.md) (2026-08-08), `*_androidKt` classes are excluded
  from the report entirely** (`**.*_androidKt` in the root `excludes` block) — this heuristic is kept
  for how the finding was made, but a future sweep won't see these rows at all.
- **Much of the branch denominator is unreachable**, in two distinct ways, and a package's
  missed-branch count distinguishes neither. *Generated:* `equals`/`hashCode`/`copy$default` on data
  classes and Room DAO impls — `feature/filters/domain/model` is 2/144 across nine files with no
  hand-written conditional in them. **`@Serializable` serializers are *not* in this category**,
  though: they are reached by any test that serializes the type, wherever it lives. Round-tripping
  `FiltersData` through `Json` inside `FiltersStorageImplTest` (a different module) took that same
  package from 2/144 to **39/144** with no test written against it. So don't quote a
  `@Serializable`-heavy package's missed branches as unreachable — but don't take it as a sweep
  target either, since the reachable share moves as a side effect of testing its callers.
  *Composition-blocked:* hand-written
  branches inside `@Composable` functions and `@Composable get()` properties, which no plain JVM test
  can enter — `utils/ui` left 46 such branches and the whole `main` package is 31 of 35 (`MainAppState`
  is `@Composable` getters; `MainViewModel` is already 4/4). Rank work by missed branches in
  hand-written, *non-composable* code. A third, much smaller kind is **`x?.toString() ?: ""`**, which
  is always 3/4: the safe call contributes two branches and the elvis two, but `toString()` on a
  non-null receiver never returns null, so the elvis's null arm is dead on that path. Seen twice —
  `WorkItemCustomFieldsDelegateImpl` and `ModulesViewModel` lines 59–60 — so recognise it rather than
  hunting for the test. Same family, same one-short result: a `?.`-chain whose last link cannot
  return null feeding an elvis, e.g. `FiltersStorageImpl:33`
  `value?.takeIf { it.isNotBlank() }?.let { json.decodeFromString(it) } ?: FiltersData()` at 7/8.
  **The elvis is not required** — any `?.`-chain with a link that cannot return null is one short,
  e.g. `UserStoryDetailsViewModel:823-824`
  `currentUserStory?.userStoryEpics?.map { it.id }?.toImmutableList()` at 3/4, because
  `userStoryEpics` is a non-null `ImmutableList` and `map` never returns null. Recognise the shape,
  not the elvis. **Its report signature is `mb=1 cb=3` on a `?.`-chain line**, and a getter declared
  `get() = x as? T ?: error(...)` is a reliable producer of it — `error()` returns `Nothing`, so the
  property is typed non-null and every `field.value?.stringValue ?: ""` reading it is 3/4 forever.
  That one getter, `CustomFieldValue.stringValue`, accounts for all 8 residual branches in
  `feature/workitem/ui/mappers`. When the same line shape is one short in several places at once,
  look for a shared non-null-typed callee rather than testing each site.
- **The same is true of LINE for every `logcat { }` call site** — 96 of them. The JVM backend is the
  no-op `NoLog` (see Logging), which never invokes the `message: () -> String` lambda, so each one is
  a synthetic method Kover reports as one missed line and zero branches. **Signature to recognise: a
  1-line hole in an otherwise 100 % method.** Stop there rather than hunting for the test that would
  reach it. Also unreachable in the same way: the default value of a state class's callback parameter
  (`onSaveClick: (String, Color) -> Unit = { _, _ -> }`), which the ViewModel always overrides.
  [revisit #16](docs/revisit.md) has the fix if it is ever judged worth the ~96 lines.
  **This is not a ceiling on LINE, though** — whether the lambda becomes its own synthetic method
  varies. `EditSprintViewModel`'s `logcat` inside a `viewModelScope.launch` was split out at 0/1, and
  `ModulesViewModel`'s two were folded into the covered `invokeSuspend`, taking that package to LINE
  88/88. So "1-line hole → stop" is the right rule, but "100 % LINE is impossible here" is not.
- **`onCleared()` is unreachable from a unit test, and every details ViewModel has one.**
  `ViewModel.onCleared` is `protected` and `ViewModel.clear()` is internal to `lifecycle-viewmodel`,
  so nothing in `commonTest` can trigger it. In `UserStoryDetailsViewModel` its body is 6 of the 10
  residual lines after the package was otherwise closed. Recognise the override and skip it — same
  family as the `logcat` holes, just bigger.
- **A low missed-branch row can still be the best session available — look for the "sleeper"
  signature: a `$1`/`$2` coroutine-body class at BRANCH 0/n *and* LINE 0/m.** That pairing means a
  whole `viewModelScope.launch` body has never executed, so the branch number only prices the
  `resultOf` `onSuccess`/`onFailure` arms while the real prize is the untested body around them.
  `feature/settings/ui/projectdetails` ranked 0/8 — below a dozen bigger-looking rows — and closing it
  took the package to **100 % on every counter**, buying 47 lines, 9 methods and 547 instructions.
  **Rank such a row by its LINE gap, not by `missedB`**; `kover-rank.py` prints both columns for
  exactly this reason. The inverse of the mapper heuristic below, and it points at the same thing: a
  row whose LINE is also short is the cheap, high-yield kind.
  It repeated on `feature/workitem/ui/screens/editdescription` (2026-08-05, 0/4 BRANCH but LINE
  6/36 → **100 % on every counter**), and that session added a cheap way to *find* the sleeper's
  test: **when a sweep row is a `screens/<x>` sibling of an already-tested `screens/<y>`, read the
  sibling's test before writing anything.** `EditDescriptionViewModel` is a strictly smaller
  `EditSprintViewModel` — same `SavedStateHandle` + `WorkItemEditStateRepository` constructor, same
  `onGoingBack` / `setIsDialogVisible` / rendezvous-channel shape — so `EditSprintViewModelTest`
  transferred almost verbatim, including the `launch { … take(1) }` collector that a rendezvous
  channel requires. `WorkItemEditStateRepository` needs no fake: it is a plain in-memory class, and
  these tests construct the real one.
- **As of 2026-08-05 the *branch* sweep (task 9a) is out of worthwhile rows** — every remaining high
  row is generated `equals`/`hashCode`, a `@Serializable` serializer, `@Composable`-blocked, or dead
  `*_androidKt` weight. Rank what is left by **missed lines on never-executed classes** instead; the
  query and the resulting backlog are in the archived task 9a write-up,
  [improvement-plan-tasks-0-9f.md](docs/archive/testing-improvement-plan-tasks-0-9f.md#where-9a-stands-2026-08-05).
  Don't re-derive the exhausted ranking — that table names what each misleading row actually is.
- **Get the per-class breakdown before scoping a session around a package**, and the per-**method**
  one before concluding a leftover is real — Kover's XML carries `<counter>` elements on
  `<package>`, `<class>` *and* `<method>`, so `for c in p.findall('class'): for m in c.findall('method')`
  answers "which function still has missed branches" in one command instead of by reading the source.
  That is how `WorkItemCustomFieldsDelegateImpl`'s residual 2/30 was pinned to the `?.` null-checks in
  `valueToUse?.toString()?.toLongOrNull()`, unreachable because `NumberItemState`'s values are
  non-null. The worked snippet is in the archive,
  [improvement-plan-tasks-0-9f.md](docs/archive/testing-improvement-plan-tasks-0-9f.md), under
  `…delegates/customfields`.
- **When the per-method breakdown is still too coarse, go to the `<sourcefile>` element** — its
  `<line>` children carry `nr`, `mb` (missed branches) and `cb` (covered), which names the *source
  line*. A whole coroutine body is one `invokeSuspend` method, so "`invokeSuspend` 10/12" is as much
  as the per-method view can say; the per-line view says "line 59 `mb=1 cb=3`" and the question is
  answered without reading Kotlin. The snippet is in the archive,
  [improvement-plan-tasks-0-9f.md](docs/archive/testing-improvement-plan-tasks-0-9f.md), under
  `…settings/ui/modules`.
  **Run it at scoping time, not only to explain a leftover.** Dumping every `mb>0` line for the
  target class *before writing any test* turns a large ViewModel from an exploration into a
  checklist — `feature/userstories/ui`'s 27 missed branches resolved to 15 named source lines in one
  command, and each test was then written against a known target. It also prices the session
  honestly: that dump is what showed the line half was branch-free and had to be split off.
  **Read the `mb`/`cb` split, not just `mb>0`** — it says which lines are worth a test before you
  write one. On a `?.`-chain, `mb=2 cb=2` is a missing test (one input never tried, +1 branch each);
  `mb=1 cb=3` is the dead arm above and buys nothing. In `feature/workitem/ui/mappers` that split
  predicted the exact final figure — 41/56 → **48/56**, four tests — before any test was written.
  `mb=2 cb=0` means the line was **never executed at all**, so it needs *two* tests, not one — an
  `x?.invoke()` callback site wants both the present and the null case. Sizing
  `WorkItemSprintDelegateImpl` that way (two lines at `mb=2 cb=0`, eight at `mb=1 cb=1`) predicted
  ten tests and 40/40, and both were exact.
  **A high `mb` on one line does not price as many tests** — `mb=4 cb=2` on a
  `find { … } ?: return null` line means the `find` *lambda* was executed (by some other method's
  test walking the same collection) while this method's own elvis never was, so one happy-path test
  closes all four. In `StatusesMapper`, `getSeverity`/`getPriority` each showed `mb=1 cb=1` on their
  first guard, `mb=4 cb=2` on the `find` line, and `mb=0 mi>0` on the whole constructor body below
  it — the signature of *a method never called at all*, which prices as three tests (happy path,
  null id, no match), not as six. Read the block of lines together, not line by line.
  **The general rule behind both: `mb` counts branches, and one expression's branches are usually
  driven by a single input — so price a line by how many distinct *values* it can take, not by its
  `mb`.** `UserStoryMapper:64` `resp.fromTaskRef?.isNotEmpty() == true` is `mb=5 cb=1`: six branches
  (the safe call, the `isNotEmpty` test, the boxed `== true` comparison) fed by one three-valued
  `String?`, so three tests — null / empty / non-empty — close all five. Any
  `x?.someBooleanCall() == true` has this shape.
  **Then re-run the dump for missed *lines* (`mi>0`) once the branch tests are green** — a
  missed-branch ranking is structurally blind to branch-free code, so one-line functions are
  invisible to it however untested they are. `WorkItemSprintDelegateImpl` sat at LINE 138/144 after
  reaching 40/40 BRANCH, and four of the six leftovers were one-line private callbacks
  (`onStartDateDismissRequest` and friends) that two more tests closed. Expect this on any delegate
  whose public surface is a state object full of callbacks; what is left after that is the `logcat`
  1-line holes, which are the signal to stop.
  **Filter that dump on `ci=0`, not on `mi>0`** — a `<line>` carries both, and Kover counts the line
  *covered* if `ci>0`, so an `mi>0 ci>0` line is a partially-covered expression that no test can
  move off the list. `UserStoryDetailsViewModel:199`
  `get() = requireNotNull(_state.value.currentUserStory)` reports `mi=9 ci=7 mb=1 cb=1` and is
  executed by nearly every test in the file; filtering on `mi>0` puts it top of a list of "untested"
  lines. Only `ci=0` lines are the never-executed ones the LINE counter is actually missing.
- **On a `*Mapper`, a block of `mb=0 mi>0` lines is usually one collection lambda starved by a
  hard-coded `null` in a `:testing` factory** — not a missing test *shape*, just a missing input.
  `getWorkItemResponseDTO()` sets `epics = null`, which left all six lines of
  `UserStoryMapper.epicsToDomain`'s body unexecuted; one `.copy(epics = listOf(…))` test closed the
  whole LINE gap. So **a `*Mapper` row whose LINE is *also* short is the cheap, high-yield kind** —
  check the factory's defaults before scoping it. The inverse holds too: a `*Mapper` at full LINE
  with residual branches is the `mb=1 cb=3` unreachable kind (`feature/issues/mapper`,
  `feature/workitem/domain/customfield`). Two consecutive pure-mapper rows — `feature/filters/mapper`
  and `feature/userstories/mapper` — went to 100 % on every counter in well under an hour each,
  because a pure mapper has no `logcat`, no coroutine and no collaborator that can throw.
  **A file of pure top-level extension functions is the same row type and is even cheaper** —
  `feature/filters/domain` is one `Utils.kt` of three `List<T>` extensions, and 7 tests took the
  package 0/10 → **10/10 BRANCH, 6/6 LINE, 3/3 METHOD, 83/83 INSTRUCTION** in minutes. Recognise it
  by a whole package at LINE 0/n with a `…Kt` class name: no class to construct, no fake to wire, so
  the only work is enumerating each expression's input values.

Qualify the task as **`:koverVerify`** — the bare name also runs the rule-less `koverVerify` in all
77 modules.

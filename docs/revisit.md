# Revisit list

Things noticed while doing something else, deliberately **not** fixed at the time, and worth coming
back to. This exists so that "I'll remember that" stops being the plan.

**The rule:** when work surfaces a real problem outside the current task's scope, add a row here and
keep going. Do not fix it inline (it makes the diff unreviewable) and do not drop it. Every entry
needs enough evidence that a cold session can pick it up without re-deriving anything — a `file:line`
or a doc link, not just a description.

**Current agreement (2026-08-02):** finish the [testing improvement plan](testing/improvement-plan.md)
first, then work this list. Nothing here is urgent; nothing here is forgotten.

| # | Item | Size | Source |
|---|---|---|---|
| 1 | ViewModels doing I/O in `init` | M–L | [koingraphtest issue](issues/2026-08-02-koingraphtest-leaks-coroutine-exceptions.md) |
| 2 | Non-ViewModel beans may leak application-scoped coroutines | S to check | same |
| 3 | Wiki mapper tests duplicate the new shared DTO factories | XS | improvement-plan task 3 |
| 4 | Dead `koin-test` block in `:testing` `androidMain` | XS | improvement-plan task 2 |
| 5 | `tools/seed` and `tools/utils` tests would not run in CI | XS | improvement-plan task 1 |

---

## 1. ViewModels doing I/O in `init`

**What:** ten ViewModels start a `viewModelScope.launch` from their `init` block that immediately
hits the repository layer:

```
feature/settings/ui/user/SettingsUserScreenViewModel.kt
feature/wiki/ui/page/details/WikiPageViewModel.kt
feature/wiki/ui/bookmark/list/WikiBookmarksViewModel.kt
feature/wiki/ui/page/list/WikiPagesViewModel.kt
feature/scrum/ui/backlog/ScrumBacklogViewModel.kt
feature/scrum/ui/open/ScrumOpenSprintsViewModel.kt
feature/workitem/ui/screens/sprint/EditSprintViewModel.kt
feature/epics/ui/list/EpicsViewModel.kt
feature/sprint/ui/SprintViewModel.kt
feature/issues/ui/list/IssuesViewModel.kt
```

**Why it came up:** constructing one of these is enough to fire a real network/DB call. That is what
made `KoinGraphTest` leak exceptions into unrelated tests. The leak is fixed at the test end
(`Dispatchers.Main` is replaced by a dispatcher that never runs), but **the fix depends on the
design staying as it is** — the first `viewModelScope.launch(Dispatchers.IO)` written in an `init`
block bypasses it and the flake returns with nothing pointing at the cause.

**The actual question** — worth answering properly, not just mechanically changing all ten:

- Is "construct = start loading" the behaviour we want? It couples object creation to I/O, which is
  why the object is untestable and unmockable without a live dispatcher.
- The alternative is an explicit `onScreenStart()` / `LaunchedEffect` trigger from the UI. That is
  more code per screen and easy to forget, which is presumably why `init` was chosen.
- There may be a middle option: keep `init` but have it collect a flow rather than run a one-shot
  suspend call, so there is nothing to throw at construction time.

**Do not treat this as a mechanical refactor.** Pick the pattern first, apply it to one ViewModel,
confirm the screen still behaves, then decide whether the other nine are worth touching. If the
answer is "the current design is fine, the test fix is sufficient" — that is a legitimate outcome,
but write down *why*, because the question will come back.

**Blocked on nothing.** Best done after the testing plan, since those ViewModels will have tests by
then and the tests are the safety net for changing them.

## 2. Non-ViewModel beans may leak application-scoped coroutines

**What:** `KoinGraphTest` constructs ~147 definitions. Only ViewModel frames showed up in the leaked
stack traces, but any bean holding an application-scoped `CoroutineScope` could leak the same way,
and nothing has audited them.

**Why deferred:** no evidence it is actually happening, and the `Dispatchers.Main` fix covers any
bean that launches undispatched, whatever its type. So this is a "confirm the gap is empty" task, not
a known bug.

**How to check:** grep for `applicationScope` / injected `CoroutineScope` constructor params among
`@Single` classes, and look for `launch` in their `init` blocks.

## 3. Wiki mapper tests duplicate the new shared DTO factories

**What:** `WikiPageMapperTest` and `WikiLinkMapperTest` (in `feature/workitem/mapper/src/commonTest/`)
each define a private `createWikiPageDTO(...)` / `createWikiLinkDTO(...)`. Task 3 added
`getWikiPageDTO()` / `getWikiLinkDTO()` to `:testing` `models/WikiFakes.kt`, which now cover the same
ground.

**Why deferred:** the surgical-changes rule — those two files were not part of task 3, and editing
them would have widened its diff for no functional gain.

**Note:** the shared factories currently expose fewer parameters than the private ones (they fix
`ownerId`, `html`, `editions`, etc.). Adopting them means widening the factory signatures, so this is
slightly more than a find-and-replace.

## 4. Dead `koin-test` block in `:testing` `androidMain`

**What:** `testing/build.gradle.kts` declares `koin-test`, `koin-test-junit4` and `junit4` in
`androidMain`. Nothing can reach them — the repo has no Android unit-test source set by design, and
`KoinGraphTest` gets its own `koin-test` from `composeApp`'s `jvmTest`.

**Why deferred:** removing it was explicitly out of scope for improvement-plan task 2. It is dead
weight, not a bug.

**Watch for:** confirm nothing in `androidApp` picks these up transitively before deleting.

## 5. `tools/seed` and `tools/utils` tests would not run in CI

**What:** both are `kotlin("jvm")` modules, so their test task is `test`, not `jvmTest`. CI runs
`./gradlew jvmTest`, so a test added under `tools/` would silently never execute — the exact trap
improvement-plan task 1 closed everywhere else.

**Why deferred:** neither module has any tests today, so there is nothing to lose right now. Running
root `./gradlew test` instead would drag in `androidApp`'s per-flavor Android unit-test variants and
slow CI down for zero current benefit.

**Trigger:** the moment anyone adds a test under `tools/`, add a `./gradlew :tools:seed:test` step to
`.github/workflows/code_analysis.yml`. Worth doing pre-emptively if `tools/` grows.

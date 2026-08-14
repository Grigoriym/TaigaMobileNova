# CLAUDE.md

TaigaMobileNova is an unofficial Kotlin Multiplatform client for Taiga.io targeting Android, iOS, and Desktop. Built with Kotlin, Compose Multiplatform, and follows a modular MVVM + Clean Architecture.

## Keeping this file lean

**A section that grows a worked-example catalogue instead of staying a stable convention has
outgrown this file — split it into its own doc under `docs/` and leave a one-line pointer.** The
Testing section's Kover coverage-sweep heuristics did exactly this over many sessions: the file
reached 717 lines before the catalogue (missed-branch/line ranking heuristics, `mb`/`cb` report
signatures, `kover-rank.py`/`kover-diff.py` usage) moved to
[docs/testing/kover-coverage-heuristics.md](docs/testing/kover-coverage-heuristics.md) (2026-08-09,
[docs/revisit.md](docs/revisit.md) #28). Watch for the same shape starting again elsewhere: a bullet
accumulating dated, confirmed cases ("Confirmed for X (date)... And for Y (date)...") is reference
material earned by a specific investigation, not a day-to-day rule every session needs to read.

A rule that fits in a sentence or two, with at most one example, stays here. A rule that needs a
worked example, a script snippet, or a running list of confirmed cases belongs in `docs/` — link it
and stop.

## Build Commands

```bash
# Android - build debug APK
# -PgplayBuild is required for Gplay builds: it's what gates the google-services
# and firebase-crashlytics plugins on (androidApp/build.gradle.kts). Omit it and the
# flavor still compiles, but Firebase never initializes — CrashReporterImpl then
# throws "Default FirebaseApp is not initialized" at app startup, release or debug.
./gradlew :androidApp:assembleGplayDebug -PgplayBuild
./gradlew :androidApp:assembleGplayRelease -PgplayBuild
./gradlew :androidApp:assembleFdroidDebug

# Desktop - run or package
./gradlew :composeApp:run
./gradlew :composeApp:packageDistributionForCurrentOS   # Deb / Dmg / Msi

# iOS - link the framework (Xcode calls embedAndSignAppleFrameworkForXcode automatically)
./gradlew :composeApp:linkReleaseFrameworkIosArm64          # device
./gradlew :composeApp:linkReleaseFrameworkIosSimulatorArm64 # simulator

# Run tests — KMP modules use jvmTest; androidApp has Android-specific variants
./gradlew :module:path:jvmTest --tests "com.package.TestClass"
./gradlew :androidApp:testFdroidDebugUnitTest --tests "com.package.TestClass"

# Run all JVM tests across all modules (skips Android unit tests which require mocking)
./gradlew jvmTest

# Run JVM tests for a single KMP module
./gradlew :module:path:jvmTest

# Generate coverage report (Kover — runs jvmTest on all aggregated modules)
./gradlew koverXmlReport    # XML → build/reports/kover/report.xml (uploaded to Codecov)
./gradlew koverHtmlReport   # HTML → build/reports/kover/html/index.html
./gradlew :koverVerify      # coverage floor (line ≥ 92 %, branch ≥ 78 %) — must be qualified

# Force Koin compiler to re-run (skipped on UP-TO-DATE, which causes "no definition found" crashes)
# Run this before launching from Xcode whenever DI definitions may have changed
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks   # iOS simulator
./gradlew :composeApp:compileKotlinIosArm64 --rerun-tasks            # iOS device
# Android equivalent (also forces Koin compiler logs):
./gradlew :androidApp:compileFdroidDebugKotlin --rerun-tasks
```

## Architecture

**Module structure:** `androidApp/` (Android entry point) + `composeApp/` (KMP library) → `feature/` → `core/` → `utils/`
- Features have data/domain/ui layers (sometimes dto/mapper)
- Use `NativeText` (in `utils:ui`) for localized strings in ViewModels
- Dependencies via Gradle Version Catalogs (`gradle/libs.versions.toml`)
- Build plugins in `build-logic/`

**Platforms:**

- **Android** — `androidTarget()`, Min SDK 24, Target SDK 37, flavors: Gplay / Fdroid
- **iOS** — `iosArm64()` + `iosSimulatorArm64()`, static framework `TaigaMobileNovaIos`; entry point in `main.ios.kt`
- **Desktop/JVM** — `jvm()`, entry point `TaigaMobileDesktop.kt`; packages Deb/Dmg/Msi

**Tech Stack:**

- Kotlin 2.4.x, JDK 21
- Compose Multiplatform with Material Design 3
- Koin (with `io.insert-koin.compiler.plugin` IR/FIR plugin) for DI
- Ktor for networking (OkHttp on Android/JVM, Darwin on iOS)
- Navigation Compose (KMP) with type-safe routes
- Kotlin Serialization for JSON
- Coroutines, Coil 3.x for images (KMP-ready)
- Room 2.8.4 + BundledSQLiteDriver (KMP-ready) — **`RoomDatabase.clearAllTables()` is Android-only**;
  the JVM/native actual doesn't declare it at all (confirmed via `javap` on the `room-runtime`
  artifacts). To clear all tables on JVM/iOS, add a no-arg `deleteAll()` `@Query` to each DAO instead.
- `core/logger` — KMP logging facade (see Logging below); Timber backs it on Android only

**Convention Plugins** (in `build-logic/`):

- `taigamobile.android.application` - Android application module (`androidApp`) — applies AGP, Compose, Koin compiler plugin
- `taigamobile.kmp.library` - KMP base (Android + iOS + JVM targets, coroutines, collections)
- `taigamobile.kmp.library.compose` - Adds Compose Multiplatform across all targets; enables `androidResources` for CMP asset pipeline
- `taigamobile.kmp.di` - Applies `io.insert-koin.compiler.plugin` + Koin dependencies
- `taigamobile.kmp.serialization` - Kotlin Serialization setup
- `taigamobile.kmp.network` - Ktor with platform-specific engines (OkHttp / Darwin)
- `taigamobile.kotlin.library` - Pure Kotlin library (no Android/KMP)

## Navigation Pattern

Destinations use `@Serializable` data classes/objects:
```kotlin
@Serializable
data class TaskDetailsNavDestination(val taskId: Long, val ref: Long)

fun NavController.navigateToTask(taskId: Long, ref: Long) {
    navigate(route = TaskDetailsNavDestination(taskId, ref))
}
```

ViewModels extract arguments via `SavedStateHandle.toRoute<T>()`:
```kotlin
private val route = savedStateHandle.toRoute<TaskDetailsNavDestination>()
private val taskId = route.taskId
```

## ViewModel + State Pattern

State class contains data AND callback functions:
```kotlin
data class FeatureState(
    val data: String = "",
    val onDataChange: (String) -> Unit,
    val isLoading: Boolean = false,
    val error: NativeText = NativeText.Empty
)
```

ViewModel exposes `StateFlow`, updates via `.update {}`:
```kotlin
private val _state = MutableStateFlow(FeatureState(onDataChange = ::setData, ...))
val state = _state.asStateFlow()

private fun setData(value: String) {
    _state.update { it.copy(data = value) }
}
```

Use `NativeText` for strings from ViewModel → resolve in UI with `text.asString(context)`.

## One-off Events Pattern

Use `Channel` + `receiveAsFlow()` for navigation, snackbars, and other one-off events. Never put these in UI state.

```kotlin
// ViewModel - use SnackbarDelegate or create Channel directly
private val _navigateBack = Channel<Unit>()
val navigateBack = _navigateBack.receiveAsFlow()

// Screen - observe with ObserveAsEvents from utils.ui
ObserveAsEvents(viewModel.navigateBack) { onNavigateBack() }
```

For snackbars, use `SnackbarDelegate` from `utils.ui`:
```kotlin
class MyViewModel @Inject constructor() : ViewModel(), SnackbarDelegate by SnackbarDelegateImpl() {
    // Use showSnackbarSuspend(message) to show snackbars
}
```

## Use Cases

Use cases only when multiple repository calls are needed. For single repo calls, call repository directly from ViewModel.

## Feature Module Structure

```
feature/{name}/
├── data/     → API, DTOs, RepositoryImpl, Koin module
├── domain/   → Models, Repository interface
└── ui/       → NavDestination, Screen, State, ViewModel
```

## Koin DI

For DI patterns, the `expect/actual @Configuration` rule, module registry, qualifier map, and troubleshooting, see the **koin-expert** subagent. It is not in this repo — it lives in `agentic-grappim` and is symlinked into `~/.claude/agents/` (see Skills below). Never use KSP — this project uses `io.insert-koin.compiler.plugin` exclusively.

## KMP String Resources

Every `RString.x` reference needs its **own** import (`import com.grappim.taigamobile.strings.generated.resources.create_task`)
— the generated strings are extension properties, so importing `RString` alone gives
`Unresolved reference 'create_task'`. This bites in test files as often as in Composables: a test
asserting `NativeText.Resource(RString.title_is_empty)` needs the same import line the ViewModel has.

`strings.xml` (`strings/src/commonMain/composeResources/values/`) does not need Android-style
apostrophe/quote escaping (`\'`) — Compose Multiplatform's resource loader doesn't apply AAPT's
escaping rules, so plain `'` works correctly. Don't escape apostrophes in new strings.

## uikit Components

For available Composable components, theme tokens, TopBarController usage, drag-and-drop, and offline/permission UI patterns, see the **uikit-guide** subagent (`.claude/agents/uikit-guide.md`). Consult it before creating any new widget.

## Permissions Pattern

`TaigaPermission` enum defines all Taiga project permissions (VIEW_*, ADD_*, MODIFY_*, COMMENT_*, DELETE_* for each entity type).

**Extension functions** in `ProjectPermissions.kt` on `ImmutableList<TaigaPermission>`:
```kotlin
permissions.canAddEpic()      // checks ADD_EPIC
permissions.canModifyTask()   // checks MODIFY_TASK
permissions.hasPermission(TaigaPermission.COMMENT_US)
```

**UI behavior:** When permission is false, **hide** the action (don't show disabled buttons). Map permission checks to boolean fields in state, set from the permissions list in ViewModel init.

## Offline State Pattern

Use `LocalOfflineState` (from `uikit`) to disable write actions when offline.

**Key difference from permissions:**

- No permission → **hide** action (user can never do this)
- Offline → **disable** action (user can do this, just not right now)

Read offline state with `val isOffline = LocalOfflineState.current`, then pass it to uikit widgets (`AddButtonWidget`, `CreateCommentBar`, `DropdownSelector`, `TopBarActionIconButton`, etc.) which disable themselves when `isOffline = true`.

## Testing

For writing new KMP tests, creating fakes, or understanding test patterns, use the **testing** subagent (`.claude/agents/testing.md`). It knows the full fake inventory, model factories, test utilities, and patterns.

- `:testing` module has utilities: `getRandomString()`, `MainDispatcherRule`, fake generators
- `kotlin.test` assertions + hand-written fakes — no MockK in `commonTest`
- Test dependencies added automatically via convention plugins
- **The apparent `:testing` ↔ module dependency cycle is not a problem.** `:testing` `api`-depends on
  ~40 modules (`core:domain`, `core:storage`, every `feature/*`), and the convention plugin puts
  `:testing` on *every* module's `commonTest`. So `:core:domain:commonTest` → `:testing` →
  `:core:domain:commonMain` looks circular and Gradle resolves it fine — the test and main source
  sets are separate compilations. Verified for `core/domain` (2026-08-05); no build-file change was
  needed to add its first test.
- `docs/testing/` — [survey.md](docs/testing/survey.md) (what exists),
  [improvement-plan.md](docs/testing/improvement-plan.md) (closed 2026-08-08; historical record of
  tasks 0–21) and [deferred.md](docs/testing/deferred.md) (ideas surveyed but not actioned — check
  here before proposing a new testing task)

**CI runs `./gradlew jvmTest`, then `koverXmlReport`, then `:koverVerify`.** The `jvmTest` step exists because
`koverXmlReport` only runs tests for modules aggregated in the root `build.gradle.kts` `kover {}`
block — `:testing`, `:uikit` and `:tools:*` are excluded, so before that step a test written there
would have been silently skipped forever. Two consequences:

- A new test only runs in CI if its module has a **`jvmTest`** task. `tools/seed` and `tools/utils`
  are `kotlin("jvm")` modules whose task is `test`, not `jvmTest` — a test added there needs its own
  workflow step.
- **Do not add tests under `src/test/`.** KMP tests go in `commonTest` (or `jvmTest` for
  JVM-specific behaviour). The repo has no Android unit-test source set at all, by design; nothing
  in CI would execute one.

**The coverage floor is a ratchet: raise it, never lower it.** `:koverVerify` enforces line ≥ 92 %
and branch ≥ 78 % (root `build.gradle.kts`, `total { verify { } }`). A PR that breaches it needs
tests, not a smaller bound. For the missed-branch/missed-line ranking heuristics, the
`koverXmlReport`-vs-`koverVerify` traps, and the `kover-rank.py`/`kover-diff.py` usage notes — read
when running a coverage sweep — see
[docs/testing/kover-coverage-heuristics.md](docs/testing/kover-coverage-heuristics.md).

**Verify with the full `./gradlew jvmTest`, not just the module's own task.** All modules' JVM tests
share one process, so a coroutine that escapes a test in one module can fail an unrelated test in
another (mechanism: **testing** subagent, gotcha 7). `:feature:x:jvmTest` passing proves your test
works; only the full run proves it did not break someone else's. When a failure appears alongside
your change, A/B it against a clean tree (`git stash -u`) before assuming you caused it.

**Run `ktlintCheck` too — a green `jvmTest` says nothing about it.** The rule that catches new test
code is `standard:function-signature`: a signature written across multiple lines fails if it *would
fit* on one within the 120-char limit. It bit two consecutive sessions on the same construct — a
`setupSuccessfulLoad(data: XDetailsData = getXDetailsData(...))` default-argument helper at ~106
characters, which reads as over-long and is nonetheless required to be one line. Dropping argument
names in the nested factory calls is what keeps it under the limit.

**`standard:class-signature` is the same trap for a class header, and it auto-fixes.** Adding a
second constructor param that pushes `class Foo(...) : SuperType {` past 120 chars — do not hand-wrap
it as one param per line with the closing paren and `: SuperType {` on their own lines; ktlint instead
wants every param kept on the constructor's own line and only the `: SuperType {` broken onto the
next one. Don't guess the format: run `./gradlew :module:path:ktlintCommonMainSourceSetFormat` (or the
matching task for the source set that failed) and let it rewrite the file.

**Every `XApi` is an `interface XApi` + `@Single(binds = [XApi::class]) class XApiImpl`** — no
exceptions, so any API can be faked in `:testing`. `WikiApi` was the last concrete one and was split
in the course of testing it.

**Failure-path convention (required): every public method of a repository impl, use case or
ViewModel gets a test where a collaborator throws `testException`, asserted with
`assertFailsWithTestException { }`** (`:testing`, `TestUtils.kt`) — not a bare
`assertFailsWith<IllegalStateException>`, which also matches a fake's own `error("… not set")` guard
and can pass without the test ever reaching the code it claims to cover. This is what closes the
line-vs-branch coverage gap. Full convention — swallowing methods, the `…Throws` fake hook,
`Result`-returning methods — in the **testing** subagent's "Failure-path convention" section.

**Testing `expect`/`actual` code: prefer the platform whose actual is real over stubbing one out.**
JVM is a fully supported target here — desktop runs the app for real — so `jvmTest` can exercise
platform-backed code (Room, DataStore, Ktor/OkHttp) with nothing faked, which `commonTest` cannot.
Put such a test in `jvmTest` and **state in the test file which platform's behaviour is being
asserted**, because the Android and iOS actuals stay uncovered. `KoinGraphTest`
(`composeApp/src/jvmTest/`) is the worked example — see [docs/koin/koin-graph-test.md](docs/koin/koin-graph-test.md),
including the wiring it deliberately cannot check. `KotlinxDateTimeFormatterJvmTest` is the smaller
one: `commonTest` proves the delegation happens, `jvmTest` proves what the JVM actual produces.

Where behaviour depends on the environment (time zone, locale), **assert a fixed expectation rather
than mutating the global default** — and if you claim a test passes under a changed environment,
prove the change reached the forked test JVM. `TZ` does; `LANG`, `LC_ALL` and `JAVA_TOOL_OPTIONS`
do not. The `testing` agent's gotcha 11 has the details.

**Integration tests against a live external server** are the same "real actual" preference taken one
step further: a plain `jvmTest` class, gated on `System.getenv("X") ?: return` (not a separate
Gradle source set), can exercise the real Ktor/OkHttp client against a real backend —
`LoginIntegrationTest` (`composeApp/src/jvmTest/.../di/`) was the first. **Always call the shared
`liveTaigaSessionOrSkip(): Koin?` helper (`LiveTaigaSession.kt`) rather than building a fresh
`koinApplication` per test** — the JVM `DataStore` backends tolerate only one open
`koinApplication` per process. Full pattern, the `DataStore`-collision mechanism, and the
write-round-trip convention are in the **testing** subagent's "Integration test against a live
server" section.

**Gradle does not track env vars — or `-P` project properties — as task inputs.** Re-running
`./gradlew jvmTest` after only changing an env var (not source) reports `UP-TO-DATE` and silently
skips re-execution — pass `--rerun` to force it, or what looks like a pass is a stale cached result
from a previous run under different env vars. Confirmed the same trap for `-P` flags too: toggling
`-PcomposeStabilityReport` on a module whose compile task was already `UP-TO-DATE` produced no
reports at all until `--rerun-tasks` forced re-execution (see
[docs/compose/stability-reports.md](docs/compose/stability-reports.md)).

## CI Guardrails

`.github/workflows/guardrails.yml` (+ `.github/scripts/check-guardrails.sh`) fails a commit that
touches `.github/`, `build-logic/`, `config/detekt/`, or `.editorconfig`; that touches
`gradle/libs.versions.toml`'s `detekt`, `ktlint`, `composeRules`, `agp` or `kover` version keys
specifically (a plain dependency bump — Renovate's usual PR — doesn't trip it, narrowed 2026-08-14
after Renovate's own commits, which can never carry a `Gate-change:` line, failed guardrails on
every PR); or that adds a new `@Ignore`/`@Suppress`, unless the commit message carries a line:

```
Gate-change: what was widened, and why
```

This doesn't prevent widening a gate — it makes doing so silently impossible. It runs with no
`paths-ignore`, unlike `build.yml`/`code_analysis.yml`, so a CLAUDE.md-only commit is still checked.
Run it locally before committing: `.github/scripts/check-guardrails.sh HEAD~1..HEAD`.

## Skills & Agents

`.claude/agents/` in this repo holds only the project-specific agents: **testing** and
**uikit-guide**. Everything else is wired up per-machine, not per-clone.

**Shared skills and agents** live in the `agentic-grappim` repo (`~/proj/grappim/agentic-grappim/`),
symlinked into `~/.claude/skills/` and `~/.claude/agents/`. The **koin-expert** agent comes from
there — it used to be duplicated in this repo, and the in-repo copy went stale while shadowing the
real one, so it was removed. Edits to any of these land in `agentic-grappim` and must be committed
there; changing them affects every project on this machine.

| Skill | Description |
|-------|-------------|
| `finalize` | Capture what a session learned into CLAUDE.md, memory, and shared-skill proposals |
| `investigate-issue` | Root-cause a reported bug and write it up under `docs/issues/` before fixing |
| `update-gradle-wrapper` | Bump the Gradle wrapper with a real checksum from Gradle's server |

**Run `finalize` automatically at the end of every task — don't offer it, don't wait to be asked.**
A task is only done once the work is verified *and* whatever it taught has been written down;
finalize is part of finishing, not a follow-up step. Applies to any unit of work that produced
non-obvious knowledge (a bug fix, a feature, a refactor, an investigation) — trivial one-line asks
don't need it. Also **check the docs for claims the work just made false** — grep for what changed
rather than trusting a read-through; a stale present-tense statement left behind is actively
misleading, and the next session will cite it as current.

**Android skills** come from the `android-skills` plugin, invoked as `android-skills:<name>`.
Relevant ones here: `navigation-3`, `edge-to-edge`, `adaptive`, `agp-9-upgrade` (note: that one
does not cover KMP — see `docs/build/agp9-kmp.md`), `r8-analyzer`, `perfetto-trace-analysis`.

## Coding Guidelines

**Tradeoff:** These guidelines bias toward caution over speed. For trivial tasks, use judgment.

### Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

- State assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them — don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

### Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

### Surgical Changes

**Touch only what you must. Clean up only your own mess.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it — don't delete it.
- Don't add UI elements or navigation that weren't asked for — if asked to create a settings screen, don't add a settings button to other screens unless explicitly requested.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless asked.

**When you notice a real problem outside the current task: write it into
[docs/revisit.md](docs/revisit.md) and keep going.** Not fixed inline (it makes the diff
unreviewable), not dropped, not just mentioned in chat — chat is not persistence. Give the entry
enough evidence (`file:line`, or a link to an issue doc) that a cold session can act on it without
re-deriving anything.

The test: Every changed line should trace directly to the user's request.

### Don't Break Production in Favor of Tests

**Production code must not be shaped by testing needs.** If a code path is flaky or
can't be observed deterministically as written, fix or remove the *test* — don't add a
seam, injectable parameter, or abstraction to production code purely so a test can
control it. This holds even when the change is small, additive, and provably safe
(e.g. a defaulted constructor parameter verified not to affect DI resolution) — the
question is not "is this change safe," it's "does this belong in production code at
all." Prefer, in order: (1) find or write a lower-level test that already covers the
behavior deterministically without the racy synchronization; (2) simplify the test to
avoid it; (3) delete the test and say so plainly, rather than leaving a known flake
undocumented. Always ask before adding any production-code testability seam, even a
well-verified one.

### Determinism Over Process

If a task has one correct, computable answer, use a tool for it rather than following a fixed
procedure by hand — a script or a hook can't skip a step or get one wrong the way a prose checklist
can. `.github/scripts/check-guardrails.sh` is this project's own example: the gate rules are a
script, not a mental checklist to re-derive each session. Reserve judgment for what actually needs
it — ambiguous input, a plan, a choice between options.

### Goal-Driven Execution

Turn a task into a verifiable goal — "fix the bug" becomes "write a failing test, then make it
pass." For multi-step work, state the steps with a check each, then loop until they pass.

### Friction Goes in Writing Too

The rule above (in Surgical Changes) is for problems in the *code* — write those to
`docs/revisit.md`. This one is for friction in the *tooling*, the kind that silently never gets
reported: a guessed flag that failed, a command that needed different quoting, a check that
confidently returned the wrong answer. The reflex is to route around it and say nothing.

Add a one-line, past-tense entry to `docs/frictions.md` before moving on — create the file if it
isn't there. At the end of a session, read the file back and report what was added, with a count,
even when the count is zero. The same friction three times is a fix, not a fourth line — raise it
in `finalize`.

## Settled Decisions

Weighed and declined — don't re-propose these.

| Not used | Instead | Why |
|---|---|---|
| Mocking libraries (MockK, Mockito) | Hand-written fakes in `:testing` | `kotlin.test` + fakes keep `commonTest` portable across all KMP targets; a mocking library would tie tests to the JVM. See Testing above. |
| KSP for DI | `io.insert-koin.compiler.plugin` (IR/FIR) | This project uses the Koin compiler plugin exclusively — see Koin DI above. |

## Logging

`core/logger` is a KMP logging facade — it is added to every KMP module's `commonMain` automatically
by the convention plugin, so `logcat` is always available without a dependency change.

```kotlin
import com.grappim.taigamobile.core.logger.logcat
import com.grappim.taigamobile.core.logger.LogPriority   // separate import, only if you set a priority

logcat { "plain debug message" }                               // as an Any extension: tag = this::class.simpleName
logcat(tag = "Ktor") { "explicit tag" }                        // top-level overload: tag is null unless given
logcat(LogPriority.ERROR, throwable = e) { "failed to load" }
```

Priorities: `VERBOSE`, `DEBUG` (default), `INFO`, `WARN`, `ERROR`, `ASSERT`.

The message is a lambda, so it isn't built unless a logger is installed. Never call `Timber`
directly outside `core/logger`.

**Backends** — `TaigaLogger.install(...)` is called once per platform entry point:

| Platform | Impl | Installed in |
|----------|------|--------------|
| Android | `TimberLogger` → Timber (`DebugTree` on debug, `CrashlyticsTree` on gplay) | `androidApp/TaigaApp.kt` |
| iOS | `NSLogLogger` → `NSLog`, chunked at 3000 chars to survive its ~4096-byte truncation | `main.ios.kt` |
| Desktop/JVM | `FileLogger` → appends to `taigamobile.log` in the per-user app-data dir (`core/storage`'s `appDataDir()`), rotating to `<name>.old` past 5 MB | `TaigaMobileDesktop.kt` |

## Security

`docs/security/masvs.md` is the living MASVS v2 register — Accepted deviations (deliberate, bounded
tradeoffs like TOFU cert trust and cleartext-for-self-hosted-LAN), Open findings, and what still
needs a device/APK to verify. Check it before reporting a new security finding; a control already
reviewed there has a bound recorded, not a re-raise. Maintained by the **masvs-review** skill.
`docs/security/masvs-review-plan.md` is closed (all 8 categories done, 2026-08-10) and kept only as
historical record of how the register was built — `masvs.md` itself is the current source of truth.

## Error Handling

- Never swallow exceptions silently. Every `catch` block must at least log the exception:
  `logcat(LogPriority.ERROR, throwable = e) { "what failed" }`.
- `CrashlyticsTree` forwards every `logcat(priority = ERROR, throwable = ...)` call verbatim to
  Firebase, including the throwable's raw `.message`. Before logging a *raw* (unmapped) exception
  from a network/parsing/IO boundary at that priority, check whether its message can carry
  sensitive data (a hostname, a response body) — see `core/api/.../ExceptionSanitization.kt` and
  `docs/security/masvs.md`'s MASVS-PRIVACY-3 note for the pattern and the audit
  (`grep -rn "LogPriority.ERROR"` + `throwable =` is the full surface).

## Compose / Platform Rules

- Do not use early returns in Composable functions — use conditional wrapping
- Lambda parameters: present tense (`onClick` not `onClicked`)
- Prefer `kotlinx-collections-immutable` (`ImmutableList`, `persistentListOf()`) over `List`/`MutableList` in state classes and Composable parameters for stable recomposition — verify with the opt-in Compose Compiler stability reports, see [docs/compose/stability-reports.md](docs/compose/stability-reports.md)
- For Composable Previews, use `@PreviewTaigaDarkLight` annotation and wrap content with `TaigaMobilePreviewTheme` (both from `uikit`):

```kotlin
@PreviewTaigaDarkLight
@Composable
private fun MyWidgetPreview() {
    TaigaMobilePreviewTheme {
        MyWidget(...)
    }
}
```

- Settings screens with fixed items use `Column` instead of `LazyColumn` — lazy loading unnecessary when item count is known and small

## Performance

Android frame-timing technique (`dumpsys gfxinfo`, Perfetto) and the `:benchmark` module's
Baseline Profile generator live in [docs/perf/profiling.md](docs/perf/profiling.md) — read it before
profiling a jank report or touching `benchmark/src/main/kotlin/.../BaselineProfileGenerator.kt`.
`docs/perf/profiling-plan.md` is closed (all 3 tasks done) and kept only as historical record.

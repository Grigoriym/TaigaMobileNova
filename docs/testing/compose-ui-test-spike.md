# Compose UI test spike (`:uikit`, 2026-08-06)

Task 10 of [improvement-plan.md](improvement-plan.md). Goal: prove `runComposeUiTest` wiring works
for one uikit widget, on the platform that actually runs in CI, and write down what it took so the
next widget test needs no research.

## Result

Works. `CreateCommentBarTest` (`uikit/src/jvmTest/kotlin/com/grappim/taigamobile/uikit/widgets/`)
passes locally via `./gradlew :uikit:jvmTest` and is picked up by the root `./gradlew jvmTest`
aggregate task, which is what CI runs before `koverXmlReport`/`:koverVerify` — no CI change needed.
`:uikit` stays outside Kover aggregation (unchanged), so this test does not move the coverage floor.

## What it took

**Dependencies** (`uikit/build.gradle.kts`): the Compose Multiplatform Gradle plugin exposes a
`compose` extension with `compose.dependencies.uiTest` (common `runComposeUiTest` API) and, for the
JVM/desktop target specifically, `compose.dependencies.desktop.uiTestJUnit4` +
`compose.dependencies.desktop.currentOs` (the desktop `ComposeUiTest` backend + a platform-correct
`org.jetbrains.compose.desktop:desktop-jvm-<os-arch>` artifact). All three are `@ExperimentalComposeLibrary`
and need `@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)` at the top of the
build script.

Two build-script gotchas, both non-obvious from the error text:

- `compose.dependencies.uiTest` **inside** a `sourceSets { jvmTest.dependencies { ... } }` block
  fails to resolve (`Unresolved reference. None of the following candidates is applicable because of
  a receiver type mismatch: val TaskContainer.dependencies: TaskProvider<DependencyReportTask>`) —
  the nested `dependencies {}` DSL block shadows the outer `compose.dependencies` property lookup.
  Fix: hoist `val composeUiTestDep = compose.dependencies.uiTest` etc. to the top level of the build
  script (works there — `compose.resources { ... }` at the top level already proved the `compose`
  accessor resolves fine outside a `dependencies {}` block) and reference the local `val`s inside
  `jvmTest.dependencies { implementation(composeUiTestDep) }`.
- Only `jvmTest` needs any of this — the test itself lives in `jvmTest`, not `commonTest` (see
  "Which source set" below), so `commonTest` was left untouched. No iOS/Android resolution risk.

**Which source set:** the test lives in `uikit/src/jvmTest/...`, not `commonTest`. `:uikit` has no
`iosTest`/`androidUnitTest` source set today, this repo has no Android unit-test source set by
design (see CLAUDE.md Testing), and CI never runs iOS. Following the same convention already used for
`expect`/`actual` platform tests (CLAUDE.md: "prefer the platform whose actual is real"), the test
went straight into `jvmTest` and the file header says so, so nobody reads it later assuming
Android/iOS coverage.

**Making a widget testable:** `CreateCommentBar`'s send `IconButton` has `contentDescription = null`
(decorative icon, correctly so for accessibility), so there was no semantics property to select it
by. Fixed by adding two `Modifier.testTag(...)` calls (one on the `HintTextField`, one on the
`IconButton`) and two public `const val ..._TEST_TAG` constants at the top of
`CreateCommentBar.kt`, referenced from the test via `onNodeWithTag`. This is the standard Compose
testing pattern (test tags are inert at runtime, only read by the semantics tree) — expect to need
one per widget under test that doesn't already expose unique text/content-description semantics.

**API surface used:** `onNodeWithTag`, `onNodeWithText`, `performTextInput`, `performClick`,
`SemanticsNodeInteraction.assertExists()` / `.assertDoesNotExist()` (member functions on the
interaction object, **not** importable top-level functions — importing them as
`androidx.compose.ui.test.assertExists` fails with "unresolved reference"; just call them on the
`onNodeWith...()` result). `runComposeUiTest { setContent { ... } ... }` needs
`@OptIn(ExperimentalTestApi::class)` on the test function.

**`waitForIdle()` is not enough when the assertion depends on a layout callback** (`onSizeChanged`,
`onGloballyPositioned`, etc.), discovered in task 11's `ExpandableMarkdownTextTest`:
`ExpandableMarkdownText` decides whether to show its "Show more" button from a height captured in
`onSizeChanged`, and that state update lands on a frame *after* the one `setContent` settles on. A
single `waitForIdle()` right after `setContent` was order/timing-dependent — it failed consistently
when a second test ran after a first one in the same class, and passed when run alone — the classic
signature of a race, not a one-off flake. Fix: poll instead of waiting once —
`waitUntil { onAllNodesWithText("Show more").fetchSemanticsNodes().isNotEmpty() }` — which advances
frames until the semantics actually show the expected state, and passed deterministically across
repeated reruns regardless of order. **Use `waitUntil { ... }` on the expected semantics, not
`waitForIdle()`, for any widget whose visible state depends on a layout pass** rather than a plain
`remember { mutableStateOf(...) }` toggle driven directly by a click handler.

**`stringResource(RString.x)` resolves normally in this `jvmTest` environment** — confirmed in
`ExpandableMarkdownTextTest` by asserting on the literal "Show more"/"Show less" strings. This means
`ConfirmActionDialogTest`'s workaround (passing `NativeText.Simple(...)` instead of the widget's
default `RString.yes`/`RString.no` to avoid a "`StringResource`-resolving test environment") was not
a hard requirement of the test setup — it was a choice for that test, not something every widget test
needs. Try plain `stringResource` first on future widgets before reaching for `NativeText.Simple`.

**What the test asserts:** `CreateCommentBar` was picked as the target because it owns real internal
state (`rememberSaveable { mutableStateOf("") }`) that changes through interaction, not because it's
the simplest widget in the module. The test types text, asserts it appears, clicks send, asserts the
callback received the trimmed text **and** the field visibly cleared (state reset) — and a second
test asserts clicking send with blank input never invokes the callback. That's the same
happy-path + guard-clause shape the ViewModel tests already use, just entered through the UI instead
of a direct method call.

## Known follow-up, not chased here

`runComposeUiTest` (the one used above, from `androidx.compose.ui.test`) is deprecated in this
Compose Multiplatform version (1.11.1) in favor of `androidx.compose.ui.test.v2.runComposeUiTest`, a
suspend-based API using `StandardTestDispatcher` by default. The v2 API compiled fine when spot-checked
but wasn't adopted here — it's a newer, less-proven surface and migrating is a mechanical,
independent change, not part of proving the wiring. Worth revisiting once v2 stabilizes or the v1
deprecation becomes a build warning worth silencing.

**Confirmed on a second, feature-level module (task 12, 2026-08-07):** the same build-script wiring
(hoisted `compose.dependencies.uiTest`/`desktop.uiTestJUnit4`/`desktop.currentOs` vals, referenced in
`jvmTest.dependencies`) applied unchanged to `feature/settings/ui/build.gradle.kts` and worked first
try — no new gotcha. See [improvement-plan.md](improvement-plan.md) task 12's Result note for what a
Screen with a real `ViewModel` needs beyond a bare uikit widget (constructing the ViewModel directly
with `:testing` fakes, no Koin; `LocalTopBarConfig` needs an explicit `CompositionLocalProvider`).

**A `ViewModel` that loads state in `init` via `viewModelScope.launch` needs no special handling
(task 13, 2026-08-07), *if* its fakes return immediately.** Add a `MainDispatcherRule` (default
`UnconfinedTestDispatcher`) and call `.setup()` **before** constructing the `ViewModel` inside the
test body — the unconfined dispatcher runs the `init` block's coroutines to completion synchronously
as part of the constructor call, so `setContent { ... }` renders already-loaded state. No
`waitUntil`, no `advanceUntilIdle`, no interaction between `kotlinx-coroutines-test`'s `Dispatchers.Main`
and Compose's own test frame clock — the coroutine work finishes before either matters. **This is not
proven to survive a fake with a real suspension point** (an artificial `delay()` standing in for a
slow load) — only that it survives fakes that return immediately, which is every fake in `:testing`
today. See [improvement-plan.md](improvement-plan.md) task 13 for the worked example
(`ProjectValuesScreenTest`), which also closed the "route-carrying Screen" gap task 12 left open by
building its `SavedStateHandle` the same way the equivalent `ViewModel` unit test already does.

**A dialog visibility transition (closed → open → closed) needs no clock advance beyond a plain
`performClick()` (task 15, 2026-08-07), and seeding a fake through a `suspend` method that never
actually suspends doesn't need `MainDispatcherRule` either — a bare `kotlinx.coroutines.runBlocking { }`
around the seed call, before `setContent`, is enough.** `TrustedCertificatesScreenTest` seeds
`FakeTrustedCertStorage` via `runBlocking { trustedCertStorage.trust(entry) }` (the fake's `trust`/
`untrust` are declared `suspend` to match the real interface but only assign a `MutableStateFlow`
field), then relies on the usual `MainDispatcherRule`/`UnconfinedTestDispatcher` trick for `init`'s
`collect { }`. Each click (open the dialog, then confirm) runs its `onClick` — including the
ViewModel's own `viewModelScope.launch { }` — synchronously under the unconfined dispatcher, so the
assertion right after `performClick()` already sees the settled state. See
[improvement-plan.md](improvement-plan.md) task 15 for the worked example.

**Two independently launched `onEach{}.launchIn(viewModelScope)` collectors in `init` both finish
before the first frame under `MainDispatcherRule` (task 16, 2026-08-07), same as sequential
`viewModelScope.launch` calls (task 13) — no ordering surprise from having two separate collectors
instead of one coroutine doing two things.** `SettingsInterfaceScreenTest` seeds
`FakeTaigaSessionStorage` with two new constructor parameters (`themeSettings`, `crashReportingEnabled`,
both previously hard-coded `flowOf(...)` values with no existing test touching either) and asserts
both landed in the same `setContent` call. Also: **`onNode(matcher)` is a member function on the
`ComposeUiTest` receiver, not a top-level import** — `import androidx.compose.ui.test.onNode` fails
with "unresolved reference" (same member-vs-import trap as `assertExists()`/`assertDoesNotExist()`
above, just for `onNode` instead of the assertion). And: **a widget with no unique text or
content-description (a bare `Switch`) can be addressed via `isToggleable()` +
`onNode(isToggleable())` without adding a `testTag`**, when it's the only toggleable node on screen —
a second, cheaper option alongside the `testTag` tax the Recommendation below describes, worth trying
first when there's only one such widget in the tree.

## Recommendation

Expanding is worth it, incrementally — the wiring cost (build-script accessor gotcha, source-set
choice, the `assertExists` member-vs-import trap) is now paid once and documented; the next widget
test is a plain "write a test" task, no new research. The `testTag` requirement is the one recurring
tax: any widget whose interactive elements lack unique text/content-description semantics needs one
or two `Modifier.testTag` additions in `commonMain` first. Suggest picking up the next one
opportunistically (e.g. alongside a ViewModel test for a screen that uses it) rather than a dedicated
sweep task — there's no evidence yet of how many uikit widgets would need a `testTag` versus already
having addressable text.

# KoinGraphTest — verifying the DI graph without launching the app

**Added:** 2026-08-02 (testing improvement plan, task 2)
**Location:** `composeApp/src/jvmTest/kotlin/com/grappim/taigamobile/di/KoinGraphTest.kt`

```bash
./gradlew :composeApp:jvmTest --tests "com.grappim.taigamobile.di.KoinGraphTest"
```

It also runs as part of `./gradlew jvmTest`, which is a CI step.

## Why it exists

The Koin IR/FIR plugin does **no** compile-time dependency validation here — `compileSafety = false`
is set by reflection in `KmpDiConventionPlugin`. A missing binding therefore surfaces as a
`NoDefinitionFoundException` at runtime, on the screen that needs it. Until this test, the only
thing verifying the graph was launching the app on a device.

The test builds the real `KoinApp` graph, walks every registered definition and resolves it. 147
definitions, about 1.2 s.

## Why `jvmTest` and not `commonTest`

The graph contains `expect/actual` modules (`PlatformComponentModule`, `PlatformStorageModule`,
`PlatformDBModule`) whose beans exist only per platform, so there is no such thing as "the common
graph" to check.

JVM is the right host because it is a **real supported target with a complete set of actuals** —
desktop calls `startKoin<KoinApp>` for real. That means zero stubbing: the JVM graph is a whole
graph. Room builds against a temp-dir file, DataStore against temp-dir prefs, Ktor against the
OkHttp engine. Nothing had to be faked to make the check run.

This generalises: **when an `expect/actual` thing needs testing, prefer the platform whose actual is
genuinely complete over stubbing one out in `commonTest`.** The same choice appears in task 6 of the
testing plan (`DateTimeUtilsImpl`).

## Two things that had to be handled

**1. `SavedStateHandle` is not a Koin definition.** ViewModels receive it from `CreationExtras`, and
Koin 4.2 even says so in the exception message. The test declares a blank one in an extra module.

**2. ViewModel constructors then fail on it.** Most ViewModels call `savedStateHandle.toRoute<T>()`
during construction, which throws `MissingFieldException` against a blank handle. About 14 of them
do this; some also `launch` a load in `init` that reaches Room and prints a stack trace from a
background thread.

That is why the test **does not** use koin-test's `checkModules()`. `checkModules()` throws on the
first failure of any kind, which the nav-argument problem makes unusable. Instead the test walks
`koin.instanceRegistry.instances` itself and classifies failures:

- root cause is `NoDefinitionFoundException` → **wiring hole, fails the test**, all of them reported
  at once rather than one per run;
- anything else → printed and tolerated.

Tolerating construction failures is sound, not a fudge: **Koin evaluates every `scope.get()` for a
constructor's arguments before invoking the constructor.** If the body then throws, the wiring has
already been proven. The generated module lambda is literally `Foo(scope.get(), scope.get(), …)`.

## The limitation that matters most

**Dropping a module from `AppModule.includes` is invisible to this test.**

`AppModule` carries both `@Module(includes = [...48 modules...])` and
`@ComponentScan("com.grappim.taigamobile")`. On JVM that scan reaches across module boundaries and
re-discovers every bean the includes list would have contributed. Verified by deleting
`UsersDataModule::class` from the list: definition count and result were unchanged, 147 either way.

The includes list is what makes the graph work on **iOS Native**, where the cross-module scan finds
nothing (see the `koin-expert` agent). So the one platform where the includes list is load-bearing
is the one platform this test cannot see. Adding a module to `AppModule.includes` still has to be
checked by running the iOS app.

Other blind spots, all inherent to a runtime check:

- **Android and iOS graphs** — different actuals; would need instrumented / native tests.
- **Nullable parameters** — `T?` resolves via `getOrNull()` and is silently null when unbound.
- **`Lazy<T>` parameters** — resolved on first access, which never happens here.

## The definition-count floor

`MIN_EXPECTED_DEFINITIONS = 147`, asserted *after* the missing-binding report so a real break shows
the names first. It is the backstop for the case the report cannot see: a definition that nothing
else depends on quietly disappearing. Raise it when the graph grows; only lower it on a deliberate
removal.

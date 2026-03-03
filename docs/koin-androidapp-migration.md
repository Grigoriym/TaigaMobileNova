# Koin DI Issue: androidApp Module Migration (TG-63)

## What Changed

AGP 9 migration introduced a new `androidApp` module as the Android entry point.
Previously: `composeApp` was the Android app module.
Now: `androidApp` is the Android app module, `composeApp` is a KMP **library**.

---

## Root Cause: `AppModule` has no hint file → never loaded by `androidApp`

The Koin plugin runs in two phases: **FIR** (generates hint files) and **IR** (wires everything).

### The FIR / KtLightSourceElement problem

In KMP, `commonMain` is compiled to an intermediate artifact first. When `composeApp:compileAndroidMain`
runs, `commonMain` classes appear as **`KtLightSourceElement`** (pre-compiled stubs) to the FIR
plugin. The FIR plugin **cannot read annotations from `KtLightSourceElement`**.

From the Koin debug logs:
```
Task :composeApp:compileAndroidMain
[Koin-Debug-FIR] AppModule (sourceType=KtLightSourceElement)
[Koin-Debug-FIR]   -> com/grappim/taigamobile/di/AppModule: no @Configuration
[Koin-Debug-FIR] Found 0 @Configuration modules
```

Result: **no hint file is generated for `AppModule`**. No hint → `androidApp` can never discover it.

### The IR phase still processes AppModule — but it's too late

The Koin IR phase (which runs after FIR) CAN read annotations from compiled IR and processes `AppModule`:
```
[Koin] @Module/@ComponentScan("com.grappim.taigamobile") on class AppModule
[Koin-Debug] Filling body for AppModule.module(): 641 definitions
```

So `AppModule.module()` IS compiled into `composeApp.aar` with 641 beans baked in. But since no
hint was generated, `androidApp` has no way to discover and call it.

### Why `PlatformComponentModule` works but `AppModule` doesn't

`PlatformComponentModule` is an `actual class` in `composeApp/androidMain` (not `commonMain`).
During `compileAndroidMain`, it is compiled from **source** (a `RealSourceElement`), so the FIR
plugin CAN see its `@Configuration`. A hint is generated. ✓

The rule: **`@Configuration` is only discoverable by downstream modules if the class is in `androidMain`
(or another platform source set), not `commonMain`.**

---

## Why Two Koin Graph Generations in the Log

Running `:androidApp:compileFdroidDebugKotlin --rerun-tasks` triggers two Koin runs:

1. **`:composeApp:compileAndroidMain`** — `composeApp` is a KMP library with `taigamobile.kmp.di` plugin.
   Koin IR/FIR runs here as part of building the library.
2. **`:androidApp:compileFdroidDebugKotlin`** — The app module has `alias(libs.plugins.koin.compiler)`.
   Koin IR/FIR runs again here.

Both compile Android Kotlin → two complete Koin graph generation runs. Expected and unavoidable.

---

## What `androidApp`'s `startKoin<KoinApp>` Actually Loads (confirmed from logs)

From `docs/koin-logs.txt` (run: `./gradlew :androidApp:compileFdroidDebugKotlin --rerun-tasks`):

The FIR phase for `androidApp` finds **35 hint functions** on the classpath but only **6
`@Configuration` modules** are discovered and wired into `startKoin`:

```
startKoin injecting:
  AndroidModule              ← local to androidApp (has @Configuration in source)
  PlatformComponentModule    ← composeApp/androidMain, direct dep → hint visible
  PlatformDBModule           ← core/storage/androidMain, direct dep → hint visible
  PlatformStorageModule      ← core/storage/androidMain, direct dep → hint visible
  KmpAndroidCoroutinesModule ← core/asyncKmp/androidMain, direct dep → hint visible
  KmpCoroutinesModule        ← core/asyncKmp/commonMain, direct dep → hint visible
```

**Not discovered (all are transitive deps of `androidApp`, not direct):**
- `AppModule` — no hint generated (see above)
- `KmpNetworkModule` — `core/api` is transitive (through `composeApp`)
- `DateTimeModule` — `utils/formatter/datetime` is transitive
- `DecimalFormatterModule` — `utils/formatter/decimal` is transitive

### What `AndroidModule`'s `@ComponentScan` compensates for

`AndroidModule` has `@ComponentScan("com.grappim.taigamobile")`. It finds **116 definitions**:
- **4 local** (androidApp source): `AppInfoProviderImpl`, `ConnectivityManagerNetworkMonitor`,
  `DebugLocalHostImageManager`, `ImageLoaderProvider`
- **112 cross-module** from orphan hint functions (77 singles + 23 factories + 12 viewmodels)

The 112 orphan hints come from **feature modules** (they have no local `@ComponentScan` → the FIR
plugin exports their beans as orphan hints when compiling each feature module). This is why feature
ViewModels, repositories, and use cases mostly work.

### Why `AppModule` has 641 beans but `AndroidModule` only gets 116

The Koin IR plugin during `composeApp`'s compilation does a **full classpath scan** — it sees all
Kotlin IR class nodes and checks annotations directly. This finds all 641 beans including classes
from `core/api` that were marked as "covered" by `KmpNetworkModule`'s own `@ComponentScan` and
thus have **no orphan hint files**.

`AndroidModule`'s IR scan in `androidApp` can only use **hint files** (pre-compiled AARs expose
hints, not raw IR). Beans with no orphan hints are invisible. Hence 116 vs 641.

---

## What Is Missing from `androidApp`'s Runtime Graph

| Missing Bean(s) | From Module | Why Missing |
|-----------------|-------------|-------------|
| `HttpClient` (auth + common), `@HttpJson Json` | `KmpNetworkModule` factory methods | Module not loaded |
| `ColorMapper` | `AppModule` `@ComponentScan` (`@Factory` in `commonMain`) | `AppModule` not loaded |
| `DateTimeUtils` | `DateTimeModule` factory methods | Module not loaded |
| `@DecimalFormatSimple DecimalFormatter` | `DecimalFormatterModule` factory methods | Module not loaded |
| `TokenRefresherImpl`, `BaseUrlProviderImpl`, `NetworkErrorMapper`, `ErrorResponseParser` | `core/api` classes covered by `KmpNetworkModule`'s `@ComponentScan` | No orphan hints → not in `AndroidModule`'s 116 |
| ~525 additional beans | `AppModule.module()` full classpath scan result | `AppModule` not loaded |

The app crashes on **`ColorMapper`** first because:
`MainViewModel → TaigaSessionStorage → TaigaSessionStorageImpl → ColorMapper`
is evaluated at startup before any network call.

But `HttpClient` / `Json` would be the next crash — the network layer is also completely broken.

---

## The Fix (two-file change)

### File 1 — Create `AndroidAppModule` in `composeApp/androidMain`

Path: `composeApp/src/androidMain/kotlin/com/grappim/taigamobile/di/AndroidAppModule.kt`

```kotlin
@Module(includes = [
    AppModule::class,
    KmpNetworkModule::class,
    DateTimeModule::class,
    DecimalFormatterModule::class,
])
@Configuration
class AndroidAppModule
```

**Why this works:**
- `AndroidAppModule` is in `androidMain` → FIR sees `@Configuration` → hint generated in `composeApp.aar`
- `androidApp` has `composeApp` as a **direct** dep → hint is discovered by `androidApp`'s FIR
- IR plugin processes `@Module(includes = [...])` → calls each module's pre-compiled `module()` function
- `AppModule.module()` (641 beans, already in `composeApp.aar`) is called → all beans loaded, including `ColorMapper` (annotated `@Factory` in `commonMain`, picked up by `AppModule`'s `@ComponentScan`)
- `KmpNetworkModule.module()` → `HttpClient`, `Json` (factory method beans) loaded
- `DateTimeModule.module()`, `DecimalFormatterModule.module()` → their beans loaded

**Why no duplicates:**
`AppModule.module()` (641 beans) was computed at `composeApp` compile time. The Koin IR plugin
deduplicated against the OTHER discovered `@Configuration` modules (`KmpNetworkModule`,
`DateTimeModule`, `DecimalFormatterModule`, `PlatformStorageModule`, etc.).
Beans "claimed" by those modules were removed from `AppModule.module()`. So the `includes`
list is complementary — no bean appears in two modules.

All imports are available in `composeApp/androidMain` because `composeApp` already depends on
`core:api`, `utils:formatter:datetime`, `utils:formatter:decimal`.

### File 2 — Narrow `AndroidModule`'s `@ComponentScan`

Path: `androidApp/src/main/kotlin/com/grappim/taigamobile/di/AndroidModule.kt`

```kotlin
@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.data")
class AndroidModule
```

**Why narrow the scan:**
With `AppModule` now loaded (641 beans including all feature module beans), `AndroidModule`'s
broad `@ComponentScan("com.grappim.taigamobile")` would **duplicate** the 112 orphan-hinted
feature beans. Narrowing to `com.grappim.taigamobile.data` covers only the 4 androidApp-local
classes (all in `.data` package) without touching cross-module orphan hints.

The 4 local androidApp classes are NOT in `AppModule`'s 641 because they are compiled in
`androidApp` which builds AFTER `composeApp`. So no overlap there either.

---

## Expected Result After Fix

`startKoin<KoinApp>` will inject:

| Module | Beans | Source |
|--------|-------|--------|
| `AndroidModule` | 4 | androidApp-local: AppInfoProviderImpl, ConnectivityManagerNetworkMonitor, DebugLocalHostImageManager, ImageLoaderProvider |
| `AndroidAppModule` | wrapper | includes 4 sub-modules |
| ↳ `AppModule` | 641 | all feature beans, composeApp beans, core beans (deduped); includes `ColorMapper` via `@ComponentScan` |
| ↳ `KmpNetworkModule` | HttpClient×2, Json | factory method beans |
| ↳ `DateTimeModule` | DateTimeUtils | factory method |
| ↳ `DecimalFormatterModule` | @DecimalFormatSimple DecimalFormatter | factory method |
| `PlatformComponentModule` | 0 | marker |
| `PlatformDBModule` | → DBModule chain | DB layer |
| `PlatformStorageModule` | → AuthDataStoreModule + StorageModule | storage layer |
| `KmpAndroidCoroutinesModule` | Main/MainImmediate dispatchers | coroutines |
| `KmpCoroutinesModule` | Default/IO dispatchers, ApplicationScope | coroutines |

---

## Key Files

| File | Purpose |
|------|---------|
| `androidApp/src/main/kotlin/.../di/AndroidModule.kt` | 4 androidApp-local beans (narrow scan) |
| `androidApp/src/main/kotlin/.../TaigaApp.kt` | `startKoin<KoinApp>` call |
| `composeApp/src/androidMain/.../di/AndroidAppModule.kt` | wrapper that loads all missing modules |
| `composeApp/src/commonMain/.../di/Koin.kt` | `KoinApp` @KoinApplication + `AppModule` |
| `composeApp/src/androidMain/.../di/Koin.android.kt` | `PlatformComponentModule` (actual, empty) |
| `core/storage/src/androidMain/.../di/StorageModule.android.kt` | `PlatformStorageModule` |

---

## Lessons Learned (Android)

1. **`commonMain` classes with `@Configuration` never get FIR hints.** FIR sees them as
   `KtLightSourceElement` and skips annotation reading. Only `androidMain` (or other platform source
   sets) classes get hints.

2. **Transitive `@Configuration` hints are not discoverable.** The Koin FIR plugin in `androidApp`
   only discovers hints from **direct** compile classpath entries. Transitive modules' hints are
   invisible regardless of AAR transitivity.

3. **`@ComponentScan` in a top-level app module uses IR scan (all classpath), not just hints.**
   This is why `AppModule` in the original `composeApp`-as-app setup could find 641 beans — it
   scanned Kotlin IR directly. `AndroidModule` in `androidApp` can only use orphan hints for
   cross-module discovery, getting only 116.

4. **The fix pattern:** Create an `androidMain` `@Configuration` class (gets a hint) in a
   **direct** dependency of `androidApp`, using `@Module(includes = [...])` to explicitly chain
   all needed modules. No `@ComponentScan` on this wrapper — just pure `includes`.

---

## iOS Native: Systemic DI Problem

### How iOS Differs from Android/JVM

On **Android/JVM**, `@ComponentScan` does a **full raw Kotlin IR scan** across the entire
classpath. It reads compiled class annotations directly from all klibs/AARs. This is how
`AppModule` finds 641 beans across every module at compile time.

On **iOS Native**, the Koin plugin explicitly skips orphan hint generation and uses hints
for cross-module `@ComponentScan` discovery instead:

```
# From docs/ios-graph.txt (compileKotlinIosSimulatorArm64 --rerun-tasks)
[Koin-Debug-FIR] generateFunctions: Skipping definition hints on Native target

[Koin-Debug]   Scanning packages: com.grappim.taigamobile (recursive)
[Koin-Debug]   Querying hints: definition_single -> 0 functions
[Koin-Debug]   Querying hints: definition_factory -> 0 functions
[Koin-Debug]   Querying hints: definition_viewmodel -> 0 functions
[Koin-Debug]   Found 5 local definitions, 0 cross-module definitions

[Koin-Debug]   Filling body for AppModule.module(): 5 definitions, 0 includes
```

Result: **`AppModule.module()` has only 5 beans on iOS** (vs 641 on Android). All feature
module beans, all repository/api/mapper orphans — invisible. The `@ComponentScan` in
`AppModule` only finds the 5 beans compiled locally inside `composeApp` itself.

### Why `IosAppModule` Doesn't Fix This

`IosAppModule` IS discovered and loaded correctly on iOS (confirmed in `docs/ios-graph.txt`):
```
[Koin] IosAppModule.module() content:
[Koin]   includes: AppModule, KmpNetworkModule, DateTimeModule, DecimalFormatterModule
[Koin-Debug]   Filling body for IosAppModule.module(): 0 definitions, 4 includes
```

The includes are loaded. But `AppModule.module()` baked into the iOS klib only has 5 beans —
the full IR scan that produces 641 on Android simply does not happen on iOS Native. Fixing
`IosAppModule`'s `includes` list cannot overcome this.

### What IS Working on iOS

Modules that use **explicit `@Module @Configuration @ComponentScan`** in `commonMain` work
correctly on iOS because their `@ComponentScan` runs locally within that module's own
compilation — where it finds its beans as local definitions:

| Module | Mechanism | Works on iOS? |
|--------|-----------|---------------|
| `KmpNetworkModule` | `@ComponentScan` in `core/api/commonMain` | ✓ (local scan in `core/api`) |
| `DateTimeModule` | `@ComponentScan` in `utils/formatter/datetime/commonMain` | ✓ |
| `DecimalFormatterModule` | `@ComponentScan` in `utils/formatter/decimal/commonMain` | ✓ |
| `KmpCoroutinesModule` | `@ComponentScan` in `core/async-kmp/commonMain` | ✓ |
| `PlatformStorageModule` | explicit `@Single fun` providers in `iosMain` | ✓ |
| `PlatformDBModule` | `includes = [DBModule::class]` in `iosMain` | ✓ |
| `AppModule` | `@ComponentScan("com.grappim.taigamobile")` in `commonMain` | ✗ cross-module = 0 |
| All feature modules | orphan `@ViewModel`/`@Single`/`@Factory` | ✗ no hints on Native |

The key insight: **`@ComponentScan` works on iOS if and only if the module class lives in the
same compilation unit as the beans it scans.** `KmpNetworkModule` works because it and all its
beans compile together inside `core/api`. `AppModule` fails because its beans span many separate
module compilations.

### Also note: `@Configuration` IS readable from `commonMain` on iOS

Unlike Android (where `commonMain` classes appear as `KtLightSourceElement` and `@Configuration`
is unreadable), iOS Native CAN read `@Configuration` from pre-compiled klibs. This is why
`KmpNetworkModule`, `DateTimeModule`, etc. in `commonMain` appear in the iOS registry:

```
[Koin-Debug]   -> Found hint module from registry: com.grappim.taigamobile.core.api.KmpNetworkModule
[Koin-Debug]   -> Found hint module from registry: com.grappim.taigamobile.utils.formatter.datetime.DateTimeModule
```

This matters for the fix: new `@Module @Configuration @ComponentScan` classes added to
`commonMain` **will be auto-discovered on iOS** without any explicit `includes` wiring.

---

## iOS Fix: Per-Module `@Module @Configuration @ComponentScan`

### The Pattern

Each module that has orphan beans needs its own `@Module @Configuration @ComponentScan` class
in `commonMain`:

```kotlin
// e.g. feature/login/src/commonMain/kotlin/com/grappim/taigamobile/feature/login/di/LoginModule.kt
@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.feature.login")
class LoginModule
```

**Why this works on iOS:** During `feature/login`'s iOS klib compilation, `LoginModule`'s
`@ComponentScan` runs locally and finds `AuthApiImpl`, `AuthRepositoryImpl`, `LoginViewModel` as
local definitions. `LoginModule.module()` is compiled into the klib with those 3 beans.
`LoginModule` has `@Configuration` → iOS reads it → it's in the registry →
`startKoin<KoinApp>` auto-discovers and loads it. No changes to `IosAppModule` needed.

**Why this is safe on Android:** Feature module `commonMain` classes appear as
`KtLightSourceElement` during `composeApp:compileAndroidMain`. The Android FIR plugin cannot
read `@Configuration` from them → they're NOT auto-discovered by `androidApp`. `AppModule`'s
full IR scan still covers them as before (641 beans, deduplication handles overlap).

### Scope of Change

Every **Gradle submodule** that contains beans (`@Single`, `@Factory`, `@KoinViewModel`, `@Scoped`)
needs its own `@Module @Configuration @ComponentScan` class in `commonMain`.

**Granularity is per Gradle submodule, not per feature.** Each feature is split into
`data`, `domain`, `dto`, `ui`, (and sometimes `mapper`) Gradle submodules. The Koin compiler
plugin scans only the klib being compiled at that moment — it cannot reach beans in a sibling
submodule. So one `@ComponentScan` per submodule that has beans.

Submodules that typically do **not** need a module class: `domain` (interfaces only),
`dto` (data classes only).

Using `feature/login` as the concrete example:

| Gradle submodule | Beans | Needs module class? |
|------------------|-------|---------------------|
| `feature/login/data` | `AuthApi`, `AuthRepositoryImpl` (`@Single`) | Yes — `LoginDataModule` |
| `feature/login/ui` | `LoginViewModel` (`@KoinViewModel`) | Yes — `LoginUiModule` |
| `feature/login/domain` | interfaces only | No |
| `feature/login/dto` | data classes only | No |

The same pattern applies to every other feature: create one module class per submodule that
has annotated beans. `utils/ui` is a single-submodule case so it only needs one class.

### Example: `utils/ui` (single submodule, fixes the immediate startup crash)

```kotlin
// utils/ui/src/commonMain/kotlin/com/grappim/taigamobile/utils/ui/di/UtilsUiModule.kt
@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.utils.ui")
class UtilsUiModule
```

### Example: `feature/login` (two submodules with beans)

```kotlin
// feature/login/data/src/commonMain/kotlin/com/grappim/taigamobile/feature/login/data/di/LoginDataModule.kt
@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.feature.login.data")
class LoginDataModule

// feature/login/ui/src/commonMain/kotlin/com/grappim/taigamobile/feature/login/ui/di/LoginUiModule.kt
@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.feature.login.ui")
class LoginUiModule
```

No changes needed to `IosAppModule` — iOS auto-discovers all `@Configuration` classes via the registry.
Android is unaffected — `AppModule`'s full IR scan still covers all beans.

### Command to verify

After adding all module classes, regenerate and inspect the iOS graph:
```bash
./gradlew :composeApp:compileKotlinIosSimulatorArm64 --rerun-tasks
```

Look for each new module class in the registry output and verify its bean count in
`Filling body for XxxModule.module(): N definitions`.

---

## Android Deduplication: Library Modules vs Feature Modules

### The rule

Adding `@Configuration @ComponentScan` to a `commonMain` class has different effects on Android
depending on where that module sits in the dependency graph:

| Module type | How it appears to `composeApp:compileAndroidMain` | FIR reads `@Configuration`? | Deduplication from `AppModule`? |
|---|---|---|---|
| **Feature module** (`feature/login/data`, etc.) | `KtLightSourceElement` (KMP source-sharing) | No | No — `AppModule` still covers those beans |
| **Library module** (`utils/ui`, `core/api`, etc.) | Compiled class stub (proper AAR artifact) | Yes | Yes — beans are removed from `AppModule.module()` |

**All modules (feature and library) cause deduplication.** The Koin IR phase during
`composeApp:compileAndroidMain` reads `@Configuration @ComponentScan` from ALL compiled
dependencies (both feature modules and library modules) and removes their beans from
`AppModule.module()`. Feature modules then cannot be auto-discovered by `androidApp`'s FIR
(they appear as `KtLightSourceElement`) — so they must be explicitly included in
`AndroidAppModule.includes` just like library modules.

**There is no distinction between feature modules and library modules for Android wiring.**
Every `@Configuration @ComponentScan` class — regardless of which module type it is in —
must be added to `AndroidAppModule.includes`.

**Library modules cause deduplication.** When the Koin FIR processes `AppModule`'s
`@ComponentScan("com.grappim.taigamobile")` and sees a compiled dependency with its own
`@ComponentScan` covering some sub-package, it removes those beans from `AppModule.module()`.
At runtime, if that library module's `@Configuration` class is not in the Android module chain,
those beans are missing → crash.

### The fix for library modules on Android

Explicitly include the library module in `AndroidAppModule`:

```kotlin
// composeApp/src/androidMain/kotlin/com/grappim/taigamobile/di/AndroidAppModule.kt
@Module(
    includes = [
        AppModule::class,
        KmpNetworkModule::class,
        DateTimeModule::class,
        DecimalFormatterModule::class,
        UtilsUiModule::class   // ← added because utils/ui is a library module
    ]
)
@Configuration
class AndroidAppModule
```

### Why Desktop (JVM) is unaffected

On JVM, Koin's hint-based auto-discovery scans the classpath at runtime for classes in
`org.koin.plugin.hints`. Library module `@Configuration` classes (like `UtilsUiModule`) are
found automatically without explicit inclusion. No changes to a JVM entry module are needed.

On Android, R8/ProGuard may strip these hint classes or the discovery order is unreliable,
so explicit inclusion in `AndroidAppModule` is required.

### core:storage — `@ComponentScan` scope fix

`StorageModule` originally had `@ComponentScan` with no argument, which defaults to its own
package (`com.grappim.taigamobile.core.storage.di`). Beans in sibling packages (`auth/`,
`cache/`, `cleaner/`, `db/wrapper/`) were never scanned → missing on iOS at runtime.

Fixed by widening the scan:
```kotlin
// core/storage/src/commonMain/kotlin/com/grappim/taigamobile/core/storage/di/StorageModule.kt
@Module
@ComponentScan("com.grappim.taigamobile.core.storage")   // was: @ComponentScan (no arg)
class StorageModule { ... }
```

`StorageModule` does NOT have `@Configuration`, so it does not cause Android deduplication.
Its broader scan is compiled into the iOS klib via `PlatformStorageModule`
(`@Module(includes = [StorageModule::class]) @Configuration actual class PlatformStorageModule`)
which IS auto-discovered on iOS.

### Going forward

When adding a new `@Configuration @ComponentScan` class to **any module** (library or feature),
also add it to `AndroidAppModule.includes`. This applies to everything in `core/`, `utils/`,
and `feature/*/`.

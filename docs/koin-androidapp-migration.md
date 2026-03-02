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
- `UtilsUiModule` — `utils/ui` is transitive
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
| `ColorMapper` | `UtilsUiModule` factory method | Module not loaded (`UtilsUiModule` is transitive) |
| `DateTimeUtils` | `DateTimeModule` factory method | Module not loaded |
| `@DecimalFormatSimple DecimalFormatter` | `DecimalFormatterModule` factory method | Module not loaded |
| `TokenRefresherImpl`, `BaseUrlProviderImpl`, `NetworkErrorMapper`, `ErrorResponseParser` | `core/api` classes covered by `KmpNetworkModule`'s `@ComponentScan` | No orphan hints → not in `AndroidModule`'s 116 |
| ~525 additional beans | `AppModule.module()` full classpath scan result | `AppModule` not loaded |

The app crashes on **`ColorMapper`** first because:
`MainViewModel → TaigaSessionStorage → TaigaSessionStorageImpl → ColorMapper`
is evaluated at startup before any network call.

But `HttpClient` / `Json` would be the next crash — the network layer is also completely broken.

### Why `UtilsUiModule` didn't fix ColorMapper

`UtilsUiModule` is in `utils/ui/androidMain` → has `@Configuration` → hint IS generated in
`utils/ui.aar`. But `androidApp` does **not** have `utils/ui` as a direct dependency — it only
has it transitively through `composeApp`. The Koin FIR plugin for `androidApp` does not discover
transitive dependency hints for `@Configuration` modules (only direct deps). So `UtilsUiModule`'s
hint is never seen by `androidApp`'s FIR.

**Status of `UtilsUiModule`:** Correct implementation, wrong placement. The hint exists but is
not reachable from `androidApp`.

---

## The Fix (two-file change)

### File 1 — Create `AndroidAppModule` in `composeApp/androidMain`

Path: `composeApp/src/androidMain/kotlin/com/grappim/taigamobile/di/AndroidAppModule.kt`

```kotlin
@Module(includes = [
    AppModule::class,
    KmpNetworkModule::class,
    UtilsUiModule::class,
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
- `AppModule.module()` (641 beans, already in `composeApp.aar`) is called → all beans loaded
- `KmpNetworkModule.module()` → `HttpClient`, `Json` (factory method beans) loaded
- `UtilsUiModule.module()` → `ColorMapper` loaded
- `DateTimeModule.module()`, `DecimalFormatterModule.module()` → their beans loaded

**Why no duplicates:**
`AppModule.module()` (641 beans) was computed at `composeApp` compile time. The Koin IR plugin
deduplicated against the OTHER discovered `@Configuration` modules (`KmpNetworkModule`,
`UtilsUiModule`, `DateTimeModule`, `DecimalFormatterModule`, `PlatformStorageModule`, etc.).
Beans "claimed" by those modules were removed from `AppModule.module()`. So the `includes`
list is complementary — no bean appears in two modules.

All imports are available in `composeApp/androidMain` because `composeApp` already depends on
`core:api`, `utils:ui`, `utils:formatter:datetime`, `utils:formatter:decimal`.

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
| `AndroidAppModule` | wrapper | includes 5 sub-modules |
| ↳ `AppModule` | 641 | all feature beans, composeApp beans, core beans (deduped) |
| ↳ `KmpNetworkModule` | HttpClient×2, Json | factory method beans |
| ↳ `UtilsUiModule` | ColorMapper | factory method |
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
| `composeApp/src/androidMain/.../di/AndroidAppModule.kt` | **NEW** wrapper that loads all missing modules |
| `composeApp/src/commonMain/.../di/Koin.kt` | `KoinApp` @KoinApplication + `AppModule` |
| `composeApp/src/androidMain/.../di/Koin.android.kt` | `PlatformComponentModule` (actual, empty) |
| `core/storage/src/androidMain/.../di/StorageModule.android.kt` | `PlatformStorageModule` |
| `utils/ui/src/androidMain/.../di/UtilsUiModule.kt` | `ColorMapper` factory (loaded via AndroidAppModule) |

---

## Lessons Learned

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

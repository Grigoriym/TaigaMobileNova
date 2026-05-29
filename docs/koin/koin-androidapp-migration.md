# Koin DI: Multi-Platform Module Discovery

Documents the Android/iOS discovery problems encountered during the AGP 9 migration, how they were fixed, and the rules that govern DI wiring going forward.

---

## Current State (post-cleanup, 2026-03-20)

### Architecture

Single source of truth: `AppModule` in `composeApp/commonMain`:

```kotlin
// no @Module on expect — FIR skips expect classes, annotation is a no-op
expect class PlatformComponentModule

@Module(includes = [...all 48 feature/utils modules])
@Configuration
@ComponentScan("com.grappim.taigamobile")
class AppModule

@KoinApplication
object KoinApp
```

`AppModule` is auto-discovered on all platforms via its `@Configuration` hint (generated even from `KtLightSourceElement` in the current Koin version). It carries all module wiring explicitly via `includes` — no platform-specific wrapper modules needed.

### What `startKoin` injects (7 modules, Android)

```
AndroidModule          ← androidApp-local (4 beans: AppInfoProviderImpl, ConnectivityManagerNetworkMonitor,
                          DebugLocalHostImageManager, ImageLoaderProvider)
PlatformComponentModule← empty actual marker
AppModule              ← 48 includes + 38 own definitions (local composeApp beans +
                          NetworkMonitorImpl + ServerStorageImpl from core/storage/androidMain)
PlatformDBModule       ← DB layer chain
PlatformStorageModule  ← storage layer chain
StorageModule          ← storage beans (auto-discovered; also reachable via PlatformStorageModule includes)
KmpCoroutinesModule    ← 5 coroutine dispatchers (Default, IO, Main, MainImmediate, ApplicationScope)
```

### Key Files

| File | Purpose |
|------|---------|
| `androidApp/.../di/AndroidModule.kt` | 4 androidApp-local beans, `@ComponentScan("com.grappim.taigamobile.data")` |
| `androidApp/.../TaigaApp.kt` | `startKoin<KoinApp>` call |
| `composeApp/commonMain/.../di/Koin.kt` | `KoinApp` + `AppModule` (48 includes) + bare `expect class PlatformComponentModule` |
| `composeApp/androidMain/.../di/Koin.android.kt` | `actual class PlatformComponentModule` (empty, `@Module @Configuration`) |
| `core/storage/commonMain/.../di/StorageModule.kt` | `StorageModule` with `@ComponentScan("com.grappim.taigamobile.core.storage")` |
| `core/storage/androidMain/.../di/StorageModule.android.kt` | `PlatformStorageModule` + `AuthDataStoreModule` |

### Files deleted during cleanup

- `composeApp/src/androidMain/kotlin/com/grappim/taigamobile/di/AndroidAppModule.kt`
- `composeApp/src/iosMain/kotlin/com/grappim/taigamobile/di/IosAppModule.kt`

These were platform-specific wrappers created to work around Koin FIR limitations. No longer needed — the current Koin version resolves this.

---

## Why Two Koin Graph Generations in the Log

Running `:androidApp:compileFdroidDebugKotlin --rerun-tasks` triggers two Koin runs:

1. **`:composeApp:compileAndroidMain`** — `composeApp` is a KMP library with `taigamobile.kmp.di` plugin. Koin IR/FIR runs here as part of building the library.
2. **`:androidApp:compileFdroidDebugKotlin`** — The app module has `alias(libs.plugins.koin.compiler)`. Koin IR/FIR runs again here.

Both compile Android Kotlin → two complete Koin graph generation runs. Expected and unavoidable.

---

## Background: The FIR / KtLightSourceElement Problem (Historical — now resolved)

This explains why `AndroidAppModule` existed and why it was deleted.

### The original problem (pre current Koin version)

In KMP, `commonMain` is compiled to an intermediate artifact before `androidMain`. When `composeApp:compileAndroidMain` runs, `commonMain` classes appear as **`KtLightSourceElement`** (pre-compiled stubs) to the FIR plugin. In older Koin versions, the FIR plugin **could not read annotations from `KtLightSourceElement`**.

```
[Koin-Debug-FIR] AppModule (sourceType=KtLightSourceElement)
[Koin-Debug-FIR]   -> com/grappim/taigamobile/di/AppModule: no @Configuration   ← old behavior
[Koin-Debug-FIR] Found 0 @Configuration modules
```

Result: no hint file was generated for `AppModule`. No hint → `androidApp` couldn't discover it.

The IR phase could still read `AppModule`'s annotations and bake `module()` into the AAR, but without a hint, `androidApp` had no way to call it.

### Transitive hints rule — still applies

**Transitive `@Configuration` hints are never discoverable.** The Koin FIR plugin in a module only discovers hints from its **direct** compile classpath. Even if a transitive dependency has a `@Configuration` class with a generated hint, it is invisible.

This is why `PlatformComponentModule` always worked: it lives in `composeApp/androidMain` (a direct dep of `androidApp`), not in `commonMain`.

### The workaround that was used

`AndroidAppModule` was created in `composeApp/androidMain` to bridge the gap:

```kotlin
@Module(includes = [AppModule::class, KmpNetworkModule::class, ...])
@Configuration
class AndroidAppModule
```

Being in `androidMain`, FIR could read its `@Configuration`. `androidApp` has `composeApp` as a direct dep → the hint was discovered → `AppModule.module()` (compiled into the AAR) was called via the includes chain.

### The current resolution

The current Koin version generates a proper `@Configuration` hint for `AppModule` even when it appears as `KtLightSourceElement`:

```
[Koin-Debug-FIR] AppModule (sourceType=KtLightSourceElement)
[Koin-Debug-FIR]   -> com/grappim/taigamobile/di/AppModule: @Configuration labels=[default]   ← fixed
```

`AndroidAppModule` was deleted. `AppModule` is now auto-discovered directly on all platforms.

---

## Background: The iOS Discovery Problem and Fix

### How iOS Native differs from Android/JVM

On **Android/JVM**, `@ComponentScan` does a **full raw Kotlin IR scan** across the entire classpath. This is how `AppModule` could previously find ~641 beans across every module at compile time.

On **iOS Native**, the Koin plugin explicitly skips orphan hint generation:

```
[Koin-Debug-FIR] generateFunctions: Skipping definition hints on Native target

[Koin-Debug]   Querying hints: definition_single -> 0 functions
[Koin-Debug]   Found 5 local definitions, 0 cross-module definitions
[Koin-Debug]   Filling body for AppModule.module(): 5 definitions, 0 includes
```

Result: `AppModule.module()` finds 0 cross-module beans on iOS — all feature module beans, repositories, mappers are invisible.

### `@Configuration` IS readable from `commonMain` on iOS

Unlike Android (where `commonMain` classes were `KtLightSourceElement`), iOS Native CAN read `@Configuration` from pre-compiled klibs. This means `@Module @Configuration @ComponentScan` classes in `commonMain` **will be auto-discovered on iOS** without any explicit includes wiring.

### The fix: per-module `@Module @Configuration @ComponentScan`

Each Gradle submodule with beans gets its own module class in `commonMain`:

```kotlin
@Module
@Configuration
@ComponentScan("com.grappim.taigamobile.feature.login.data")
class LoginDataModule
```

**Why this works on iOS:** During `feature/login/data`'s iOS klib compilation, `LoginDataModule`'s `@ComponentScan` runs locally and finds its beans as local definitions. `LoginDataModule.module()` is compiled into the klib. iOS reads `@Configuration` from the klib registry → auto-discovers and loads it.

**Why this is safe on Android:** Feature module classes appear as `KtLightSourceElement` during `composeApp:compileAndroidMain` — FIR cannot read `@Configuration` from them. `AppModule`'s broad `@ComponentScan` and the 48 explicit `includes` still cover them.

**`@ComponentScan` works on iOS only within the same compilation unit.** `KmpNetworkModule` works because it and all its beans compile together in `core/api`. `AppModule` fails cross-module on iOS because its beans span many separate compilations.

**Granularity is per Gradle submodule.** One module class per submodule that has annotated beans. Submodules with only interfaces or data classes don't need one.

All 48 feature/utils submodules now have their own module class and are listed in `AppModule.includes`.

### `IosAppModule` was not the fix

`IosAppModule` (now deleted) was discovered and loaded on iOS, and its `includes` were processed. But `AppModule.module()` baked into the iOS klib only had 5 beans — the includes chain can't overcome the fact that the iOS `module()` body was compiled with 0 cross-module results.

---

## What Was Fixed (All Cleanup Sessions, 2026-03-20)

1. **`compileSafety = false` on `androidApp`** — `KmpDiConventionPlugin` only applies to KMP library modules. Added directly to `androidApp/build.gradle.kts`.

2. **`debugLogs`/`userLogs` moved out of `KmpDiConventionPlugin`** — Only set in `composeApp/build.gradle.kts` and `androidApp/build.gradle.kts`. `compileSafety = false` stays in the plugin (needed to suppress multi-module false positives).

3. **All 48 per-module `@Module @Configuration @ComponentScan` classes created** — Every Gradle submodule with beans got its own module class in `commonMain` and was added to `AppModule.includes`.

4. **`AndroidModule`'s `@ComponentScan` narrowed** to `com.grappim.taigamobile.data` — With `AppModule` now correctly loaded, the broad scan was causing mass duplication of the 112 orphan-hinted feature beans.

5. **`AndroidAppModule` + `IosAppModule` deleted** — Current Koin version generates hints from `KtLightSourceElement`; the workarounds are no longer needed.

6. **Removed bare `@ComponentScan` from `PlatformStorageModule` and `AuthDataStoreModule` actuals** (Android + iOS) — The no-arg form defaults to the `di` package which contains no bean classes. JVM actual was already clean. `StorageModule` retains `@ComponentScan("com.grappim.taigamobile.core.storage")` — required for iOS discovery of `NetworkMonitorImpl`/`ServerStorageImpl`.

7. **Removed `@Module` from `expect class PlatformComponentModule`** — FIR explicitly skips expect classes ("Skipping expect class" in debug log). Actual classes in all platform source sets retain `@Module @Configuration`.

---

## Ongoing Known Limitations

### Feature module beans invisible at androidApp compile time
All 48 included modules contribute **0 new definitions** from androidApp's IR perspective — their beans are in pre-compiled AARs without enumerable hint functions. Compile-time graph validation cannot verify them. Correct at **runtime** (pre-compiled `module()` functions load correctly).

### Function-based providers mark modules `complete=false`
`KmpCoroutinesModule`, `AuthDataStoreModule`, `PlatformStorageModule` use function-based providers (`fun provides...()`). The Koin compiler cannot generate enumerable hint functions for them → `complete=false`. Unfixable limitation of the current Koin compiler plugin.

### Double-loaded modules (harmless)
`StorageModule` and `KmpCoroutinesModule` are both auto-discovered (direct deps of `androidApp` via `core.storage` and `core.asyncKmp`) AND reachable via the include chain. Koin deduplicates at runtime.

### `NetworkMonitorImpl`/`ServerStorageImpl` double-registered on Android (harmless)
`AppModule`'s `@ComponentScan("com.grappim.taigamobile")` claims both on Android via cross-module single hints. `StorageModule`'s `@ComponentScan("com.grappim.taigamobile.core.storage")` also claims them locally during `core/storage` compilation. Koin deduplicates singletons at runtime. The `StorageModule` scan is **required for iOS** and must not be removed.

### `@IoDispatcher` "Could not read qualifier value argument" warning (benign)
```
[Koin-Debug] Could not read qualifier value argument for @IoDispatcher: No such value argument slot in IrAnnotationImpl: 0 (total=0)
[Koin-Debug] @IoDispatcher (custom qualifier) on parameter dispatcher
```
Appears when the IR plugin processes `ConnectivityManagerNetworkMonitor` (`@param:IoDispatcher private val dispatcher`). `@IoDispatcher` is a no-arg annotation; the plugin tries to read a name value first, fails with `total=0`, then correctly falls back to type-based qualifier resolution. The final line confirms success. Unfixable without changing the qualifier design.

---

## Rules Going Forward

**When adding a new Gradle submodule with beans:**
1. Create `@Module @Configuration @ComponentScan("com.grappim.taigamobile.your.package")` in `commonMain`
2. Add it to `AppModule.includes` in `composeApp/src/commonMain/kotlin/com/grappim/taigamobile/di/Koin.kt`

No platform-specific wiring needed.

**When adding platform-specific beans** (require `Context` or platform SDK):
- Bare `expect class PlatformXyzModule` in `commonMain` (no annotations)
- `@Module(includes = [CommonXyzModule::class]) @Configuration actual class PlatformXyzModule` in each platform source set

**Never remove `@ComponentScan` from `StorageModule`** — it discovers `NetworkMonitorImpl` and `ServerStorageImpl` on iOS (AppModule's cross-module scan returns 0 on iOS Native).

**Never put `@Module` on an `expect class`** — FIR skips expect classes entirely. If the expect has `@Module(includes = [])` but the actual has `@Module(includes = [X::class])`, the Kotlin compiler emits an annotation-argument-mismatch warning.

**Do not add `@Configuration` to pure wrapper modules** (only `includes = [...]`, no own beans) unless you need them auto-discovered as top-level entry points.

# AGP 9 Migration Guide

Sources:
- https://developer.android.com/build/releases/agp-9-0-0-release-notes
- https://developer.android.com/kotlin/multiplatform/plugin
- https://developer.android.com/kotlin/multiplatform/plugin#unsupported-features-and-workarounds

---

## Requirements

| Component | Minimum |
|-----------|---------|
| Gradle | 9.1.0 |
| SDK Build Tools | 36.0.0 |
| JDK | 17 |
| Kotlin Gradle Plugin | 2.2.10 |

---

## New Gradle Property Defaults (breaking if not handled)

| Property | Old → New | Action |
|----------|-----------|--------|
| `android.newDsl` | false → **true** | Migrate DSL or opt out |
| `android.builtInKotlin` | false → **true** | Migrate or set `false`; incompatible with `org.jetbrains.kotlin.multiplatform` on same module as `com.android.library` |
| `android.uniquePackageNames` | false → **true** | Ensure unique namespaces per library module |
| `android.proguard.failOnMissingFiles` | false → **true** | Remove invalid ProGuard file references |
| `android.r8.optimizedResourceShrinking` | false → **true** | Update keep rules if needed |
| `android.r8.strictFullModeForKeepRules` | false → **true** | Add `<init>()` to `-keep` rules if needed |
| `android.r8.proguardAndroidTxt.disallowed` | false → **true** | Use `proguard-android-optimize.txt`, not `proguard-android.txt` |
| `android.r8.globalOptionsInConsumerRules.disallowed` | false → **true** | Remove `-dontoptimize`/`-dontobfuscate` from library consumer rules |
| `android.defaults.buildfeatures.resvalues` | true → **false** | Enable `resValues` only per-module if needed |
| `android.defaults.buildfeatures.shaders` | true → **false** | Enable `shaders` only per-module if needed |
| `android.sourceset.disallowProvider` | false → **true** | Use `Sources` API on `androidComponents` for source sets |
| `android.enableAppCompileTimeRClass` | false → **true** | Refactor switch/when on R fields to if-else |
| `android.sdk.defaultTargetSdkToCompileSdkIfUnset` | false → **true** | Explicitly declare `targetSdk` |

---

## Removed APIs

| Removed | Replacement |
|---------|-------------|
| `android.applicationVariants` / `libraryVariants` | `androidComponents.onVariants()` |
| `android.variantFilter` | `androidComponents.beforeVariants()` |
| `android.dexOptions` | Removed (D8 handles this) |
| `android.generatePureSplits` | Use App Bundles |
| `ComponentBuilder.enabled` | `ComponentBuilder.enable` |
| `VariantOutput.enable` | `VariantOutput.enabled` |
| `DependenciesInfoBuilder.includedInApk/Bundle` | `includeInApk/Bundle` |
| `Variant.minSdkVersion/targetSdkVersion` | `minSdk/targetSdk` |
| `AndroidComponentsExtension.finalizeDSl()` | `finalizeDsl()` |
| `android.deviceProvider`, `testServer` | Gradle-managed devices |
| `CommonExtension<*, *, *, *, *, *>` (type params) | `CommonExtension` (no params) |
| `PostProcessing` block | Removed |
| `LanguageSplitOptions` | Use App Bundles |
| `DensitySplit` | Use App Bundles |
| Embedded Wear OS support (`wearApp`) | Removed |
| `registerTransform` APIs | Removed |

---

## KMP + Android: Old vs New Plugin

### The Problem
`com.android.library` + `org.jetbrains.kotlin.multiplatform` on the **same module** is deprecated and conflicts with `android.builtInKotlin=true`.

### The New Plugin
Use `com.android.kotlin.multiplatform.library` instead of `com.android.library` for KMP library modules.

```toml
# libs.versions.toml
[plugins]
android-kotlin-multiplatform-library = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }
```

### Configuration Style Change

**Old** (com.android.library + KMP):
```kotlin
// top-level android block
android {
    namespace = "..."
    compileSdk = 36
    defaultConfig { minSdk = 24 }
}

kotlin {
    androidTarget { compilerOptions { jvmTarget.set(JvmTarget.JVM_21) } }
    sourceSets { ... }
}
```

**New** (com.android.kotlin.multiplatform.library):
```kotlin
// everything inside kotlin block
kotlin {
    android {
        namespace = "..."
        compileSdk = 36
        minSdk = 24
        compilerOptions { jvmTarget.set(JvmTarget.JVM_21) }
    }
    sourceSets { ... }
}
```

### Source Set Names Change

| Old | New |
|-----|-----|
| `src/test` | `src/androidHostTest` |
| `src/androidTest` | `src/androidDeviceTest` |
| `androidUnitTest` (Gradle source set) | `androidHostTest` |

### Tests Disabled by Default (must opt in)
```kotlin
kotlin {
    android {
        withHostTestBuilder { }.configure { }        // unit tests
        withDeviceTestBuilder { sourceSetTreeName = "test" } // instrumented tests
    }
}
```

### Compose Tooling Dependency Change
```kotlin
// Old
dependencies { "debugImplementation"(libs.compose.ui.tooling) }

// New
dependencies { "androidRuntimeClasspath"(libs.compose.ui.tooling) }
```

---

## Unsupported Features in com.android.kotlin.multiplatform.library

| Feature | Workaround |
|---------|-----------|
| **Build types / Product flavors** | Move to the `com.android.application` module |
| **BuildConfig class** | Use BuildKonfig plugin (already in this project) |
| **Data Binding / View Binding** | Use Compose Multiplatform |
| **Native builds (externalNativeBuild)** | Separate `com.android.library` module |

---

## This Project: Current Sync Failure

**Root cause:** `composeApp/build.gradle.kts` applies `alias(libs.plugins.android.library)` directly, while `taigamobile.kmp.di` convention plugin applies `org.jetbrains.kotlin.multiplatform`. With `android.builtInKotlin=true` (AGP 9 default), AGP tries to register Kotlin extension on the same module, conflicting with the multiplatform plugin:

```
Cannot add extension with name 'kotlin', as there is an extension already registered with that name.
```

**Fix:** Migrate ALL KMP library modules from `com.android.library` → `com.android.kotlin.multiplatform.library`.

### Migration checklist for this project

- [ ] Uncomment `android-kotlin-multiplatform-library` in `libs.versions.toml`
- [ ] Uncomment it in root `build.gradle.kts`
- [ ] Update `KmpLibraryConventionPlugin`: replace `com.android.library` → `com.android.kotlin.multiplatform.library`; remove `LibraryExtension` configure block
- [ ] Update `KmpConfiguration.kt`: move Android config into `kotlin { android { } }` block; change `androidUnitTest` → `androidHostTest` source set
- [ ] Update `KmpLibraryComposeConventionPlugin`: change `debugImplementation` → `androidRuntimeClasspath` for tooling
- [ ] Update `composeApp/build.gradle.kts`: replace `android.library` plugin → `android.kotlin.multiplatform.library`; remove top-level `android { }` block; move namespace/compileSdk/minSdk/compileOptions into `kotlin { android { } }`; remove `configureFlavors(this)` (not supported); move IS_FDROID to `androidApp`
- [ ] All 115+ KMP modules: remove top-level `android { namespace = "..." }` blocks (namespace now set in convention plugin via auto-computation from module path)
- [ ] `androidApp`: add Gplay/Fdroid product flavors (since `composeApp` can no longer have them)
- [ ] Clean up `gradle.properties` commented AGP 9 compat flags

### Namespace auto-computation formula (for convention plugin)
```kotlin
// Converts :feature:login:domain → com.grappim.taigamobile.feature.login.domain
// Converts :core:async-kmp → com.grappim.taigamobile.core.asynckmp (hyphens removed)
val namespace = "com.grappim.taigamobile${path.replace(':', '.').replace("-", "")}"
```
Verified against all existing module namespaces. Only exception: `:composeApp` → actual namespace is `com.grappim.taigamobile` (must override).
# AGP 9 + Kotlin Multiplatform

Reference for how this project's KMP modules are configured under AGP 9. The migration itself is
**done** — this documents the end state and the KMP-specific rules that keep applying.

> For the generic (non-KMP) AGP 9 changes — the new Gradle property defaults, the removed variant
> APIs, built-in Kotlin, kapt→KSP — use the `android-skills:agp-9-upgrade` skill and its bundled
> `agp-9-0-0-release-notes.md`. That skill explicitly **does not cover KMP**, which is why this
> doc exists.

Sources:

- <https://developer.android.com/kotlin/multiplatform/plugin>
- <https://developer.android.com/kotlin/multiplatform/plugin#unsupported-features-and-workarounds>

---

## Why KMP modules need a different plugin

`com.android.library` + `org.jetbrains.kotlin.multiplatform` on the **same module** is deprecated
and conflicts with `android.builtInKotlin=true` (the AGP 9 default) — AGP tries to register a
`kotlin` extension that the multiplatform plugin already owns:

```
Cannot add extension with name 'kotlin', as there is an extension already registered with that name.
```

The fix is `com.android.kotlin.multiplatform.library` for every KMP library module. In this project
that is applied by `KmpLibraryConventionPlugin`; only `androidApp` uses `com.android.application`.

---

## Configuration style

Everything Android moves **inside** the `kotlin` block.

**Old** (`com.android.library` + KMP):

```kotlin
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

**New** (`com.android.kotlin.multiplatform.library`):

```kotlin
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

## Source set renames

| Old | New |
|-----|-----|
| `src/test` | `src/androidHostTest` |
| `src/androidTest` | `src/androidDeviceTest` |
| `androidUnitTest` (Gradle source set) | `androidHostTest` |

## Android tests are off by default

They must be opted into explicitly:

```kotlin
kotlin {
    android {
        withHostTestBuilder { }.configure { }                 // unit tests
        withDeviceTestBuilder { sourceSetTreeName = "test" }  // instrumented tests
    }
}
```

## Compose tooling dependency

```kotlin
// Old
dependencies { "debugImplementation"(libs.compose.ui.tooling) }
// New
dependencies { "androidRuntimeClasspath"(libs.compose.ui.tooling) }
```

---

## Unsupported in `com.android.kotlin.multiplatform.library`

| Feature | Workaround used here |
|---------|----------------------|
| Build types / product flavors | Gplay + Fdroid flavors live in `androidApp` |
| `BuildConfig` class | BuildKonfig plugin |
| Data Binding / View Binding | Compose Multiplatform |
| Native builds (`externalNativeBuild`) | Would need a separate `com.android.library` module |

## Compose Multiplatform resources

`androidResources` is disabled by default in this plugin, which silently drops CMP resources from
the AAR/APK. `KmpLibraryComposeConventionPlugin` sets `androidResources.enable = true` for every
module — see [cmp-resources-android-fix.md](cmp-resources-android-fix.md) for the full diagnosis.

## Namespace auto-computation

Namespaces are no longer declared per module; the convention plugin derives them from the Gradle path:

```kotlin
// :feature:login:domain → com.grappim.taigamobile.feature.login.domain
// :core:async-kmp       → com.grappim.taigamobile.core.asynckmp   (hyphens removed)
val namespace = "com.grappim.taigamobile${path.replace(':', '.').replace("-", "")}"
```

Only `:composeApp` overrides it — its namespace is `com.grappim.taigamobile`.
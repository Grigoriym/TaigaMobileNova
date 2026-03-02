# CMP Resources Missing from androidApp APK (TG-63)

## Status

**RESOLVED** with a workaround. The workaround is intentionally custom and acknowledged as
technical debt. See "Why This Is Unsatisfying" and "Future Investigation" at the bottom.

---

## Symptom

```
MissingResourceException: Missing resource with path:
  composeResources/com.grappim.taigamobile.strings.generated.resources/values/strings.commonMain.cvr
```

Crash site: `MainScreen.kt:108` → `stringResource(Res.string....)` is the first Compose string
resource call at startup.

Confirmed with:
```bash
unzip -l androidApp/build/outputs/apk/fdroid/debug/androidApp-fdroid-debug.apk | grep composeResources
# → empty (before fix)
```

---

## Root Cause

**CMP 1.10.1 bug: `variant.sources.assets` is null for `KotlinMultiplatformAndroidVariant`
(AGP 9.0.1's new KMP library plugin).**

All KMP library modules in this project use `com.android.kotlin.multiplatform.library`
(the new AGP 9 KMP library plugin, applied via `taigamobile.kmp.library.compose` convention).

CMP's `AndroidResources.kt` registers `copyAndroidMainComposeResourcesToAndroidAssets` and
wires up its output directory like this:

```kotlin
componentSources.assets?.addGeneratedSourceDirectory(
    copyComponentAssets,
    CopyResourcesToAndroidAssetsTask::outputDirectory
)
```

`addGeneratedSourceDirectory` is supposed to **both** set `outputDirectory` on the task AND
register the directory as an Android asset source. But `componentSources.assets` returns **null**
for `KotlinMultiplatformAndroidVariant`. The `?.` causes a silent no-op. Result:

- `outputDirectory` is never set → task fails with `value not set` if invoked directly
- No asset directory is registered in the Android library → resources never enter the AAR
- `androidApp` consumes the library → APK contains no `composeResources/` assets

Confirmed by running the task directly:
```
./gradlew :strings:copyAndroidMainComposeResourcesToAndroidAssets
→ FAILURE: property 'outputDirectory' doesn't have a configured value.
```

---

## What Works vs. What Breaks

| Target | Task | Status |
|--------|------|--------|
| JVM | `assembleJvmMainResources` | ✓ produces `assembledResources/jvmMain/composeResources/…` |
| iOS | `assembleIos*MainResources` | ✓ same |
| Android | `copyAndroidMainComposeResourcesToAndroidAssets` | ✗ `outputDirectory` not set → fails |

Modules affected: `strings`, `uikit`, and any module with `org.jetbrains.compose` applied
via `taigamobile.kmp.library.compose`.

Only `strings` and `uikit` actually have compose resource files (`composeResources/` source dir).
Other compose modules (feature UIs, `composeApp`) produce empty assembled directories.

### The working resource pipeline (JVM, for reference)

```
convertXmlValueResourcesForCommonMain
  ↓
prepareComposeResourcesTaskForCommonMain
  → build/generated/compose/resourceGenerator/preparedResources/commonMain/composeResources/
      values/strings.commonMain.cvr          ← no package prefix yet
  ↓
assembleJvmMainResources
  → build/generated/compose/resourceGenerator/assembledResources/jvmMain/composeResources/
      com.grappim.taigamobile.strings.generated.resources/values/strings.commonMain.cvr
  ↓ (embedded in JVM JAR via Gradle resource configuration)
```

For Android, CMP registers `copyAndroidMainComposeResourcesToAndroidAssets` which should do
both the "assemble with package prefix" and "copy to Android assets" steps in one. There is NO
intermediate `assembleAndroidMainResources` task — the `assembledResources/androidMain/`
directory never gets created.

---

## Approaches That Were Considered

### Approach A — Fix CMP's task directly (`CopyResourcesToAndroidAssetsTask`)

The broken task's class is `org.jetbrains.compose.resources.CopyResourcesToAndroidAssetsTask`.
`compose-gradle-plugin` is already a `compileOnly` dep in `build-logic/convention/build.gradle.kts`.

Problem: the class is marked `internal` in Kotlin. `internal` in a Kotlin module compiles to
package-private on the JVM, meaning it is inaccessible from other Kotlin modules. Direct import
fails. Reflection would work but is fragile against CMP version changes.

### Approach B — Use `Sync::destinationDirectory` callable reference

```kotlin
// In androidApp's androidComponents { onVariants { ... } }
assets.addGeneratedSourceDirectory(syncTask, Sync::destinationDirectory)
```

Problem: `Sync::destinationDirectory` does not resolve as a Kotlin callable reference in a
build script. `destinationDirectory` is accessible on task instances but not as a class-level
callable reference from the Gradle Kotlin DSL script context. Confirmed with:
```
e: Unresolved reference 'destinationDirectory'.
```

Note: `configurations["name"]` also does NOT work inside plugin Kotlin classes (only in
build scripts). Use `configurations.getByName("name")` in plugins.

### Approach C — Use the JVM assembled resources as the Android asset source ✓ (implemented)

**Key insight:** `assembleJvmMainResources` works correctly and produces the same content
as what Android needs:
- Source: `commonMain/composeResources/` (no platform-specific resources in this project)
- Output: `assembledResources/jvmMain/composeResources/{packageOfResClass}/…`
- Required in APK: `assets/composeResources/{packageOfResClass}/…`

The directory structure under `jvmMain/` is identical to what Android needs. We can reuse
it directly rather than waiting for a fixed `copyAndroidMainComposeResourcesToAndroidAssets`.

---

## Implemented Workaround

Two files changed.

### File 1 — `build-logic/convention/src/main/kotlin/KmpLibraryComposeConventionPlugin.kt`

Added `setupComposeAndroidResources()` which:
1. Creates a consumable Gradle configuration `composeAndroidResources` with a custom
   `Usage` attribute so only our resolver picks it up.
2. In `afterEvaluate`, finds `assembleJvmMainResources` and publishes the `jvmMain/`
   assembled directory as an artifact on that configuration.

```kotlin
private fun Project.setupComposeAndroidResources() {
    val assembledJvmDir = layout.buildDirectory
        .dir("generated/compose/resourceGenerator/assembledResources/jvmMain")

    configurations.create("composeAndroidResources") {
        isCanBeConsumed = true
        isCanBeResolved = false
        attributes {
            attribute(
                Usage.USAGE_ATTRIBUTE,
                objects.named(Usage::class.java, "compose-android-resources"),
            )
        }
    }

    afterEvaluate {
        val assembleTask = tasks.findByName("assembleJvmMainResources") ?: return@afterEvaluate
        configurations.getByName("composeAndroidResources").outgoing
            .artifact(assembledJvmDir) { builtBy(assembleTask) }
    }
}
```

This runs in every module using `taigamobile.kmp.library.compose`. For modules without
actual compose resources (e.g. feature UI modules), `assembledJvmDir` is an empty directory —
harmless.

### File 2 — `androidApp/build.gradle.kts`

1. **Resolvable configuration** (matches the consumable one from the convention plugin):

```kotlin
val composeAndroidResources by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage::class.java, "compose-android-resources"))
    }
}
```

2. **Dependencies** — only modules that actually have compose resource files:

```kotlin
composeAndroidResources(projects.strings)
composeAndroidResources(projects.uikit)
```

Note: configuration resolution is NOT transitive for published artifacts. `strings` and `uikit`
are the only modules with a `composeResources/` source directory. Including `composeApp` or
feature modules is unnecessary (they produce empty `jvmMain/` dirs).

3. **Custom task + `addGeneratedSourceDirectory`**:

`Sync::destinationDirectory` is not resolvable as a callable reference (see Approach B above),
so a custom abstract task is used instead:

```kotlin
abstract class CollectComposeAssetsTask : DefaultTask() {
    @get:InputFiles
    abstract val sourceDirectories: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    fun collect() {
        val dest = outputDirectory.get().asFile
        dest.deleteRecursively()
        dest.mkdirs()
        sourceDirectories.forEach { srcDir ->
            if (srcDir.isDirectory) srcDir.copyRecursively(dest, overwrite = true)
        }
    }
}

androidComponents {
    onVariants { variant ->
        val assets = variant.sources.assets ?: return@onVariants
        val variantName = variant.name.replaceFirstChar { it.uppercase() }
        val collectTask = tasks.register(
            "collect${variantName}ComposeAssets",
            CollectComposeAssetsTask::class.java,
        ) {
            sourceDirectories.from(composeAndroidResources.incoming.files)
        }
        assets.addGeneratedSourceDirectory(collectTask, CollectComposeAssetsTask::outputDirectory)
    }
}
```

`addGeneratedSourceDirectory` sets `outputDirectory` to an AGP-managed path and registers
it as an asset source for the variant. The `CollectComposeAssetsTask` then copies from all
source directories (one `jvmMain/` dir per dependency) into that single output directory,
merging the package-prefixed subdirectories:

```
jvmMain/ (strings)  ─┐
                      ├─ merged into → outDir/composeResources/{pkg.strings}/…
jvmMain/ (uikit)   ─┘                outDir/composeResources/{pkg.uikit}/…
```

AGP's asset merger picks up `outDir/` → APK gets `assets/composeResources/{pkg}/…`.

---

## Verification

```bash
./gradlew :androidApp:assembleFdroidDebug
unzip -l androidApp/build/outputs/apk/fdroid/debug/androidApp-fdroid-debug.apk \
  | grep composeResources
```

Confirmed output (after fix):
```
assets/composeResources/com.grappim.taigamobile.strings.generated.resources/values/strings.commonMain.cvr
assets/composeResources/com.grappim.taigamobile.uikit.generated.resources/drawable-dark/github_mark.xml
assets/composeResources/com.grappim.taigamobile.uikit.generated.resources/drawable/default_avatar.png
assets/composeResources/com.grappim.taigamobile.uikit.generated.resources/drawable/taiga_mobile_logo.png
# … all other uikit drawables
```

---

## Why This Is Unsatisfying

1. **Reuses JVM resources for Android.** This works only because all compose resources in
   this project live in `commonMain`. If any module ever adds Android-specific compose
   resources (`androidMain/composeResources/`), those resources will be silently missing
   from the APK. There is no safeguard against this.

2. **Bypasses the intended CMP pipeline.** CMP's `copyAndroidMainComposeResourcesToAndroidAssets`
   task exists and is intended to handle this correctly. We're ignoring it entirely.

3. **Custom abstract task in a build script.** Defining `CollectComposeAssetsTask` inside
   `androidApp/build.gradle.kts` is unusual and harder to discover.

4. **`afterEvaluate` in the convention plugin.** `afterEvaluate` is generally discouraged
   in modern Gradle in favour of lazy configuration, but is necessary here because CMP
   registers `assembleJvmMainResources` during project evaluation.

5. **Two files to maintain instead of CMP doing it automatically.** If CMP is upgraded
   and the bug is fixed upstream, this workaround must be removed manually or it may
   conflict with the fixed behaviour.

---

## Future Investigation

Track the upstream CMP bug: the correct fix is for CMP to handle
`KotlinMultiplatformAndroidVariant` properly (i.e. not use `?.` when `componentSources.assets`
is null, or to obtain assets via an alternative API that works with AGP 9's KMP library plugin).

When a fixed CMP version is available:
1. Remove `setupComposeAndroidResources()` from `KmpLibraryComposeConventionPlugin.kt`
2. Remove the `composeAndroidResources` configuration, its dependencies, `CollectComposeAssetsTask`,
   and the `androidComponents` block from `androidApp/build.gradle.kts`
3. Verify with `unzip -l … | grep composeResources` that resources still appear in the APK

Possible alternative workarounds not yet tried:
- Set `CopyResourcesToAndroidAssetsTask.outputDirectory` via reflection (fragile but closer
  to the intended pipeline — would include `androidMain/composeResources/` resources if added)
- Use `tasks.withType(Task::class).configureEach { }` with `javaClass.getMethod("getOutputDirectory")`
  duck-typing to avoid hard import of the internal class

---

## Key Files

| File | Role |
|------|------|
| `build-logic/convention/src/main/kotlin/KmpLibraryComposeConventionPlugin.kt` | Exposes `assembledResources/jvmMain/` via `composeAndroidResources` configuration |
| `androidApp/build.gradle.kts` | Consumes the configuration; `CollectComposeAssetsTask`; wires into AGP assets |
| `strings/build.gradle.kts` | `compose.resources { packageOfResClass = "…strings…" }` |
| `uikit/build.gradle.kts` | Same, includes all drawables |
| `build-logic/convention/build.gradle.kts` | `compileOnly(libs.compose.multiplatform.gradlePlugin)` — already present, would be needed for Approach A |

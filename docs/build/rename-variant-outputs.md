# Renaming APK/AAB Variant Outputs In-Place (AGP Variant API)

## Goal
Replace the original APK/AAB files with custom-named ones so there are no duplicates —
the renamed files become the canonical build output.

## Key Insight: Transform vs Listen
- `toListenTo()` — copies/processes artifact **after** the fact; originals remain untouched
- `toTransform()` / `toTransformMany()` — **replaces** the artifact in the pipeline; downstream tasks see the new files

Use `toTransformMany()` for APKs and `toTransform()` for AABs.

---

## Renaming APKs (`SingleArtifact.APK`)

`SingleArtifact.APK` is a **ContainsMany** directory artifact (multiple APKs + `output-metadata.json`).
Use `toTransformMany` + `ArtifactTransformationRequest.submit()` — this is the AGP contract for
iterating over each artifact and keeping `output-metadata.json` consistent with the renamed files.

> **Common mistake:** `toTransform` + `BuiltArtifactsLoader` looks similar but is wrong here.
> `toTransformMany` is required for `ContainsMany` artifacts.

### Task

```kotlin
import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.variant.BuiltArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class RenameApkTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    // ArtifactTransformationRequest is the AGP contract for ContainsMany transforms.
    // It iterates over each BuiltArtifact, maps it to an output file, and updates output-metadata.json.
    @get:Internal
    abstract val transformationRequest: Property<ArtifactTransformationRequest<RenameApkTask>>

    @get:Input
    abstract val baseName: Property<String>

    @TaskAction
    fun taskAction() {
        transformationRequest.get().submit(this) { builtArtifact: BuiltArtifact ->
            val newFile = outputDir.get().file("${baseName.get()}.apk").asFile
            File(builtArtifact.outputFile).copyTo(newFile, overwrite = true)
            newFile
        }
    }
}
```

### Plugin wiring

```kotlin
androidComponents.onVariants { variant ->
    val flavor = variant.flavorName ?: return@onVariants
    val buildType = variant.buildType ?: return@onVariants
    val baseName = "app-$flavor-$buildType"
    val variantCap = variant.name.replaceFirstChar { it.titlecase() }

    val taskProvider = project.tasks.register<RenameApkTask>("rename${variantCap}Apk") {
        this.baseName.set(baseName)
    }

    // toTransformMany returns an ArtifactTransformationRequest that must be wired back into the task
    val request = variant.artifacts.use(taskProvider)
        .wiredWithDirectories(RenameApkTask::inputDir, RenameApkTask::outputDir)
        .toTransformMany(SingleArtifact.APK)

    taskProvider.configure {
        transformationRequest.set(request)
    }
}
```

---

## Renaming AABs (`SingleArtifact.BUNDLE`)

`SingleArtifact.BUNDLE` is a **single file** artifact with **no metadata file**, so a lazy
`doLast` rename is safe and appropriate. `toTransform` would also work but adds unnecessary complexity.

```kotlin
val bundleDir = project.layout.buildDirectory.dir("outputs/bundle/${variant.name}")

// tasks.configureEach + name check (not tasks.named) because bundle tasks only exist
// for release variants — tasks.named would throw at configuration time for debug.
project.tasks.configureEach {
    if (name == "bundle$variantCap") {
        doLast {
            bundleDir.get().asFile.listFiles()?.forEach { file ->
                if (file.extension == "aab" && !file.name.startsWith("app-")) {
                    file.renameTo(File(file.parent, "$baseName.aab"))
                }
            }
        }
    }
}
```

---

## Summary

| Artifact                | Kind         | Correct API                                     | Metadata updated? |
|-------------------------|--------------|-------------------------------------------------|-------------------|
| `SingleArtifact.APK`    | ContainsMany | `toTransformMany` + `ArtifactTransformationRequest.submit()` | Yes (automatic) |
| `SingleArtifact.BUNDLE` | Single file  | `doLast` rename (no metadata to worry about)    | N/A               |

## Reference Recipes in this repo

| Recipe               | What it shows                                              |
|----------------------|------------------------------------------------------------|
| `listenToArtifacts`  | `BuiltArtifactsLoader` + custom naming (copy, not replace) |
| `transformDirectory` | `wiredWithDirectories.toTransform()` pattern               |
| `getSingleArtifact`  | How to read a single file artifact                         |

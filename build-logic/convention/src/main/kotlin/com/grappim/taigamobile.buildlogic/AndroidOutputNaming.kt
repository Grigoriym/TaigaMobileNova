package com.grappim.taigamobile.buildlogic

import com.android.build.api.artifact.ArtifactTransformationRequest
import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.BuiltArtifact
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register

/**
 * Renames Android APK and AAB outputs from the default module-name prefix to "app-":
 *   androidApp-fdroid-debug.apk  → app-fdroid-debug.apk
 *   androidApp-gplay-release.aab → app-gplay-release.aab
 *
 * APKs use the AGP Variant API (toTransformMany + ArtifactTransformationRequest) so
 * output-metadata.json is kept consistent with the renamed files.
 * AABs have no metadata file, so they are renamed via a lazy doLast on bundle*.
 */
internal fun Project.configureAndroidOutputNaming() {
    extensions.configure<ApplicationAndroidComponentsExtension> {
        onVariants { variant ->
            val flavor = variant.flavorName ?: return@onVariants
            val buildType = variant.buildType ?: return@onVariants
            val baseName = "app-$flavor-$buildType"
            val variantCap = variant.name.replaceFirstChar { it.titlecase() }

            // APKs: SingleArtifact.APK is ContainsMany, so toTransformMany is required.
            // The task must call transformationRequest.submit() — AGP's mechanism for
            // iterating ContainsMany artifacts and updating output-metadata.json.
            val apkTaskProvider = tasks.register<RenameApkTask>("rename${variantCap}Apk") {
                this.baseName.set(baseName)
            }
            val request = variant.artifacts.use(apkTaskProvider)
                .wiredWithDirectories(RenameApkTask::inputDir, RenameApkTask::outputDir)
                .toTransformMany(SingleArtifact.APK)
            apkTaskProvider.configure {
                transformationRequest.set(request)
            }

            // AABs: SingleArtifact.BUNDLE has no metadata file, so a lazy doLast rename is safe.
            // tasks.configureEach is used (not tasks.named) because bundle tasks only exist for
            // release variants — tasks.named would throw at configuration time for others.
            val bundleDir = layout.buildDirectory.dir("outputs/bundle/${variant.name}")
            tasks.configureEach {
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
        }
    }
}

abstract class RenameApkTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    // ArtifactTransformationRequest is the AGP contract for ContainsMany artifact transforms.
    // It iterates over each BuiltArtifact, maps it to an output file, and updates metadata.
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


import com.grappim.taigamobile.buildlogic.configureKmp
import com.grappim.taigamobile.buildlogic.configureKmpCompose
import com.grappim.taigamobile.buildlogic.configureLinting
import com.grappim.taigamobile.buildlogic.configureTests
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Usage

class KmpLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            configureKmp()
            configureKmpCompose()
            configureTests()
            configureLinting()
            setupComposeAndroidResources()
        }
    }

    // CMP 1.10.1 bug workaround: `componentSources.assets` is null for KotlinMultiplatformAndroidVariant
    // (AGP 9's KMP library plugin), so CMP's copyAndroidMainComposeResourcesToAndroidAssets task never
    // gets its outputDirectory set and the resources never enter the AAR / APK.
    //
    // Fix: expose the JVM-assembled resources (which CMP produces correctly, same commonMain content,
    // same package-prefixed path structure) via a custom Gradle configuration so androidApp can consume
    // them and add them as Android asset sources directly.
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
}

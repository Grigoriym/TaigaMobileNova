import com.grappim.taigamobile.buildlogic.configureKotlinJvm
import com.grappim.taigamobile.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KotlinLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("java-library")
                apply("org.jetbrains.kotlin.jvm")
                apply("com.google.devtools.ksp")
            }
            configureKotlinJvm()

            dependencies {
                "implementation"(libs.findLibrary("kotlinx.coroutines.core").get())
            }
        }
    }
}

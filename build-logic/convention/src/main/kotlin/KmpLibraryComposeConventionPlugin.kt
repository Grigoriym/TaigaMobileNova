import com.grappim.taigamobile.buildlogic.configureKmp
import com.grappim.taigamobile.buildlogic.configureKmpCompose
import com.grappim.taigamobile.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class KmpLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            configureKmp()
            configureKmpCompose()

            dependencies {
                "debugImplementation"(libs.findLibrary("compose.ui.tooling").get())
            }
        }
    }
}

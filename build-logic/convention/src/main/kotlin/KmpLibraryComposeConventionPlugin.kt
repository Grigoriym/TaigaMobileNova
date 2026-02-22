import com.grappim.taigamobile.buildlogic.configureKmp
import com.grappim.taigamobile.buildlogic.configureKmpCompose
import com.grappim.taigamobile.buildlogic.configureLinting
import com.grappim.taigamobile.buildlogic.configureTests
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
            configureTests()
            configureLinting()

            dependencies {
                "debugImplementation"(libs.findLibrary("jetbrains.compose.ui.tooling").get())
            }
        }
    }
}

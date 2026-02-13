import com.grappim.taigamobile.buildlogic.configureKmp
import com.grappim.taigamobile.buildlogic.configureKmpCompose
import org.gradle.api.Plugin
import org.gradle.api.Project

class KmpLibraryComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.compose")
                apply("org.jetbrains.kotlin.plugin.compose")
            }
            configureKmp()
            configureKmpCompose()
        }
    }
}

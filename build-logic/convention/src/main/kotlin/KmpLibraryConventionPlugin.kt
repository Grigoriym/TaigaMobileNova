
import com.android.build.api.dsl.LibraryExtension
import com.grappim.taigamobile.buildlogic.configureKmp
import com.grappim.taigamobile.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("com.android.library")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = libs.findVersion("compileSdk").get().toString().toInt()
                defaultConfig.minSdk = libs.findVersion("minSdk").get().toString().toInt()
            }

            configureKmp()
        }
    }
}

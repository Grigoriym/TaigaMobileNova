import com.grappim.taigamobile.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpDiConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.multiplatform")
                apply("io.insert-koin.compiler.plugin")
            }

            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets.apply {
                    commonMain.dependencies {
                        val koinBom = libs.findLibrary("koin.bom").get()
                        implementation(project.dependencies.platform(koinBom))

                        api(libs.findLibrary("koin.annotations").get())
                        implementation(libs.findLibrary("koin.core").get())
                        implementation(libs.findLibrary("koin.core.viewmodel").get())
                        implementation(libs.findLibrary("koin.compose").get())
                        implementation(libs.findLibrary("koin.compose.viewmodel").get())
                    }

                    androidMain.dependencies {
                        implementation(libs.findLibrary("koin.android").get())
                    }
                }
            }
        }
    }
}

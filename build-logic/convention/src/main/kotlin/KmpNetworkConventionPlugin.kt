
import com.grappim.taigamobile.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class KmpNetworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            extensions.configure<KotlinMultiplatformExtension> {
                sourceSets {
                    androidMain.dependencies {
                        implementation(libs.findLibrary("ktor.client.okhttp").get())
                    }
                    jvmMain.dependencies {
                        implementation(libs.findLibrary("ktor.client.okhttp").get())
                    }
                    commonMain.dependencies {
                        implementation(libs.findLibrary("ktor.core").get())
                    }
                    iosMain.dependencies {
                        implementation(libs.findLibrary("ktor.client.darwin").get())
                    }
                }
            }
        }
    }
}

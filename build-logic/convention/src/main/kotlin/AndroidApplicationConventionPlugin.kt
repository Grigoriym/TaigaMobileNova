import com.android.build.api.dsl.ApplicationExtension
import com.grappim.taigamobile.buildlogic.AppBuildTypes
import com.grappim.taigamobile.buildlogic.configureAndroidCompose
import com.grappim.taigamobile.buildlogic.configureFlavors
import com.grappim.taigamobile.buildlogic.configureKotlinAndroid
import com.grappim.taigamobile.buildlogic.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.project

class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.android")

            extensions.configure<ApplicationExtension> {
                defaultConfig.targetSdk = libs.findVersion("targetSdk").get().toString().toInt()

                signingConfigs {
                    create("release") {
                        storeFile = file("../taigamobilenova_keystore_release.jks")
                        keyAlias = System.getenv("TAIGA_ALIAS_R")
                        keyPassword = System.getenv("TAIGA_KEY_PASS_R")
                        storePassword = System.getenv("TAIGA_STORE_PASS_R")
                        enableV2Signing = true
                        enableV3Signing = true
                    }
                }

                buildTypes {
                    debug {
                        applicationIdSuffix = AppBuildTypes.DEBUG.applicationIdSuffix

                        isDebuggable = true
                        isMinifyEnabled = false
                        isShrinkResources = false

                        val debugLocalHost = findProperty("debug.local.host") as String? ?: ""
                        buildConfigField("String", "DEBUG_LOCAL_HOST", "\"$debugLocalHost\"")
                    }
                    release {
                        applicationIdSuffix = AppBuildTypes.RELEASE.applicationIdSuffix

                        isDebuggable = false
                        isMinifyEnabled = true
                        isShrinkResources = true

                        signingConfig = signingConfigs.getByName("release")
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro"
                        )

                        buildConfigField("String", "DEBUG_LOCAL_HOST", "\"\"")
                    }
                }

                bundle {
                    language {
                        enableSplit = false
                    }
                }

                packaging.resources.excludes.apply {
                    add("META-INF/ASL2.0")
                    add("META-INF/notice.txt")
                    add("META-INF/NOTICE.txt")
                    add("META-INF/NOTICE")
                    add("META-INF/license.txt")
                    add("DEPENDENCIES")
                }

                configureFlavors(this)
                configureKotlinAndroid(this)
                configureAndroidCompose(this)
            }
            dependencies {
                "implementation"(libs.findLibrary("androidx.core.ktx").get())

                add("testImplementation", kotlin("test"))
                add("testImplementation", project(":testing"))
                add("androidTestImplementation", kotlin("test"))
                add("androidTestImplementation", project(":testing"))
            }
        }
    }
}

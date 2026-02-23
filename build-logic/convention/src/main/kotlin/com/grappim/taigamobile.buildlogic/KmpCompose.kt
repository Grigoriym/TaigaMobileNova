package com.grappim.taigamobile.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

internal fun Project.configureKmpCompose() {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("compose.runtime").get())
                implementation(libs.findLibrary("compose.foundation").get())
                implementation(libs.findLibrary("compose.ui").get())
                implementation(libs.findLibrary("jetbrains.compose.ui.tooling.preview").get())
                implementation(libs.findLibrary("compose.material3").get())
                implementation(libs.findLibrary("compose.material").get())
                implementation(libs.findLibrary("compose.icons").get())
                implementation(libs.findLibrary("jetbrains.compose.navigationevent").get())

                implementation(libs.findLibrary("androidx.lifecycle.runtime.compose").get())
                implementation(libs.findLibrary("androidx.lifecycle.viewmodel.compose").get())

                implementation(libs.findLibrary("androidx.savedstate").get())

                implementation(libs.findLibrary("compose.navigation").get())
            }
            androidMain.dependencies {
                implementation(libs.findLibrary("androidx.navigation.compose").get())
            }
            jvmMain.dependencies {
                implementation(libs.findLibrary("compose.desktop").get())
                implementation(libs.findLibrary("compose.navigation.desktop").get())
            }
            iosMain.dependencies {

            }
        }
    }
}

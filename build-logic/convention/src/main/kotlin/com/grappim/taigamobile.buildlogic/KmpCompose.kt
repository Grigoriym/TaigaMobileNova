package com.grappim.taigamobile.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureKmpCompose() {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("jetbrains.compose.runtime").get())
                implementation(libs.findLibrary("jetbrains.compose.foundation").get())
                implementation(libs.findLibrary("jetbrains.compose.ui").get())
                implementation(libs.findLibrary("jetbrains.compose.ui.graphics").get())
                implementation(libs.findLibrary("jetbrains.compose.animation").get())
                implementation(libs.findLibrary("jetbrains.compose.ui.tooling.preview").get())
                implementation(libs.findLibrary("jetbrains.compose.material3").get())
                implementation(libs.findLibrary("jetbrains.compose.material").get())
                implementation(libs.findLibrary("jetbrains.compose.icons").get())
                implementation(libs.findLibrary("jetbrains.compose.navigationevent").get())

                implementation(libs.findLibrary("jetbrains.lifecycle.runtime.compose").get())
                implementation(libs.findLibrary("jetbrains.lifecycle.viewmodel.compose").get())

                implementation(libs.findLibrary("jetbrains.androidx.savedstate").get())

                implementation(libs.findLibrary("jetbrains.compose.navigation").get())
            }
            androidMain.dependencies {
                implementation(libs.findLibrary("androidx.navigation.compose").get())
            }
            jvmMain.dependencies {
                implementation(libs.findLibrary("jetbrains.compose.desktop").get())
                implementation(libs.findLibrary("compose.navigation.desktop").get())
            }
        }
    }

    dependencies{
        "androidRuntimeClasspath"(libs.findLibrary("jetbrains.compose.ui.tooling").get())
    }
}

package com.grappim.taigamobile.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureKmp() {
    extensions.configure<KotlinMultiplatformExtension> {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

//        jvm()

//        iosX64()
//        iosArm64()
//        iosSimulatorArm64()

        sourceSets.apply {
            commonMain.dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.core").get())
                implementation(libs.findLibrary("kotlinx.collections").get())
                implementation(libs.findLibrary("kotlinx.date.time").get())

                if (project.path != ":core:logger") {
                    implementation(project(":core:logger"))
                }
            }
            commonTest.dependencies {
                implementation(kotlin("test"))
                implementation(project(":testing"))
            }
        }
    }
}

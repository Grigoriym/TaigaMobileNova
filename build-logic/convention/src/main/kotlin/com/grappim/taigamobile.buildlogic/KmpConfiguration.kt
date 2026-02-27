package com.grappim.taigamobile.buildlogic

import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

fun Project.configureKmp() {
    pluginManager.apply("org.jetbrains.kotlinx.kover")

    extensions.configure<KoverProjectExtension> {
        currentProject {
            instrumentation {
                // Only use jvmTest for coverage — exclude Android unit test task variants
                disabledForTestTasks.addAll(
                    "testDebugUnitTest",
                    "testReleaseUnitTest",
                    "testGplayDebugUnitTest",
                    "testFdroidDebugUnitTest",
                    "testGplayReleaseUnitTest",
                    "testFdroidReleaseUnitTest",
                )
            }
        }
    }

    extensions.configure<KotlinMultiplatformExtension> {
        jvmToolchain(21)
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }

        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        jvm()

        iosArm64()
        iosSimulatorArm64()

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
            jvmMain.dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.swing").get())
            }
            iosMain.dependencies {
                implementation(libs.findLibrary("kotlinx.coroutines.core").get())
            }
            androidUnitTest.dependencies {
                implementation(kotlin("test"))
                implementation(project(":testing"))
            }
        }
    }
}

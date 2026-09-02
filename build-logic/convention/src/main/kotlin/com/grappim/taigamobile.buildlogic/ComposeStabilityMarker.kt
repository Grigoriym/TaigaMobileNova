package com.grappim.taigamobile.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Lets the Compose Compiler embed a stability marker on this module's classes, without pulling in
// any Compose UI toolkit (Foundation/Material3/Navigation/...). For `*/domain` modules whose types
// are consumed as Composable parameters elsewhere: without this, the Compose compiler in a
// downstream UI module has no marker to trust and defaults every such class to Unstable, even a
// fully `val`, ImmutableList-using data class — see docs/revisit.md #39.
//
// compose-runtime is `compileOnly`: it's needed only for the compiler to reference the
// `@StabilityInferred` annotation type at compile time, not at runtime, and consuming UI modules
// already carry compose-runtime themselves.
fun Project.configureComposeStabilityMarker() {
    pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.apply {
            commonMain.dependencies {
                compileOnly(libs.findLibrary("jetbrains.compose.runtime").get())
            }
        }
    }
}

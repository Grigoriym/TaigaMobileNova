package com.grappim.taigamobile.buildlogic

import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.compose.compiler.gradle.ComposeCompilerGradlePluginExtension

// Opt-in Compose Compiler stability audit — see docs/compose/stability-reports.md.
// Off by default: -PcomposeStabilityReport to generate *_classes.txt / *_composables.txt.
//
// Deliberately does NOT set targetKotlinPlatforms to restrict which target gets reports: that
// property doesn't just filter report output, it's also what ComposeCompilerGradleSubplugin
// .isApplicable() uses to decide whether the Compose compiler plugin applies to a compilation at
// all (confirmed by reading the plugin's source). Restricting it to `jvm` silently disables
// Compose's bytecode transformation for the Android target of every KMP UI module, breaking
// androidApp's build the moment this flag is set. Avoid duplicate per-target reports
// operationally instead — compile only the jvm target task when running the audit (see task 2 in
// docs/compose/stability-reports-plan.md).
fun Project.configureComposeStabilityReports() {
    if (!project.hasProperty("composeStabilityReport")) return
    extensions.configure<ComposeCompilerGradlePluginExtension> {
        metricsDestination.set(layout.buildDirectory.dir("compose_reports"))
        reportsDestination.set(layout.buildDirectory.dir("compose_reports"))
    }
}

package com.grappim.taigamobile.buildlogic

import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetContainer
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

private val lintingExclusions = setOf(":testing")

fun Project.configureTests() {
    tasks.withType<Test> {
        failFast = true
        // https://github.com/gradle/gradle/issues/33619#issuecomment-2913519014
        failOnNoDiscoveredTests.set(false)
        reports {
            html.required.set(true)
        }
        testLogging {
            events(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
        }
    }
}

fun Project.configureLinting() {
    if (path in lintingExclusions) return

    pluginManager.apply("dev.detekt")
    pluginManager.apply("org.jlleitschuh.gradle.ktlint")

    // The plain `detekt` task defaults to the JVM layout (src/main/kotlin, src/test/kotlin),
    // which no KMP module uses — without this every task reports NO-SOURCE.
    // Source sets also carry generated dirs (BuildKonfig, Compose resources, Room); those live
    // under the build dir and are dropped here, since detekt's own `build/` exclude does not
    // match the absolute paths the source sets hand over.
    val kotlinSrcDirs = provider {
        val buildDir = layout.buildDirectory.get().asFile
        extensions.findByType(KotlinSourceSetContainer::class.java)
            ?.sourceSets
            ?.flatMap { it.kotlin.srcDirs }
            ?.filterNot { it.startsWith(buildDir) }
            .orEmpty()
    }

    // https://detekt.dev/docs/introduction/configurations/
    configure<DetektExtension> {
        buildUponDefaultConfig.set(true)
        parallel.set(true)
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        allRules.set(false)
        source.setFrom(kotlinSrcDirs)
    }

    // ./gradlew --continue ktlintCheck
    // ./gradlew ktlintFormat
    // ./gradlew addKtlintCheckGitPreCommitHook
    configure<KtlintExtension> {
        version.set("1.8.0")
        android.set(true)
        ignoreFailures.set(false)
        verbose.set(true)
        outputColorName.set("RED")
        outputToConsole.set(true)
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
            reporter(ReporterType.HTML)
            reporter(ReporterType.JSON)
        }
    }

    dependencies {
        "ktlintRuleset"(libs.findLibrary("composeRules-ktlint").get())
        "detektPlugins"(libs.findLibrary("composeRules-detekt").get())
    }
}

import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.serialization) apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.gradleDoctor)
    alias(libs.plugins.dependencyAnalysis)
    alias(libs.plugins.jacocoAggregationResults)
    alias(libs.plugins.jacocoAggregationCoverage)
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
}

doctor {
    enableTestCaching.set(false)
    disallowCleanTaskDependencies.set(true)
    warnWhenJetifierEnabled.set(true)
    javaHome {
        ensureJavaHomeMatches.set(false)
        ensureJavaHomeIsSet.set(false)
        failOnError.set(false)
    }
}

allprojects {
    tasks.withType<Test> {
        failFast = true
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

subprojects {
    apply {
        plugin("io.gitlab.arturbosch.detekt")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("com.autonomousapps.dependency-analysis")
    }

    // https://github.com/cortinico/kotlin-android-template
    detekt {
        buildUponDefaultConfig = true
        parallel = true
        config.setFrom(rootProject.files("config/detekt/detekt.yml"))
        allRules = false
    }

    // ./gradlew --continue ktlintCheck
    // ./gradlew ktlintFormat
    // ./gradlew addKtlintCheckGitPreCommitHook
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.7.1")
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
        ktlintRuleset("io.nlopez.compose.rules:ktlint:0.4.27")
        detektPlugins("io.nlopez.compose.rules:detekt:0.4.27")
    }
}

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R\$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",

    "**/*Module*.*",
    "**/*Module",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/Hilt*",
    "**/*GeneratedInjector",
    "**/*HiltComponents*",
    "**/*_HiltModules*",
    "**/*_Provide*",
    "**/*_Factory*",
    "**/*_ComponentTreeDeps",
    "**/*_Impl*",
    "**/*DefaultImpls*",
    "**/_com_grappim_taigamobile_*",

    "**/MainDispatcherRule*",
    "**/SavedStateHandleRule*",
    "**/*Api",

    "**/*Screen",
    "**/*Activity",
    "**/*Screen*",
    "**/*Application",

    "**/*JsonAdapter",

    "**/*NavDestination",
    "**/*Widget",
    "**/*Dialog",
    "**/*BottomSheet",
    "**/TaskFilters",
    "**/MainNavHost",

    "**/testing/*",
    "**/uikit/theme/*",
    "**/uikit/widgets/*",
    "**/FileLoggingTree"
).flatMap {
    listOf(
        "$it.class",
        "${it}Kt.class",
        "$it$*.class"
    )
}

testAggregation {
    modules {
        exclude(rootProject)
    }
    coverage {
        exclude(coverageExclusions)
    }
}

tasks.jacocoAggregatedReport {
    reports {
        html.required = true
        csv.required = true
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}

// taken from https://github.com/reactor/BlockHound
tasks.withType<Test>().all {
    if (JavaVersion.current().isCompatibleWith(JavaVersion.VERSION_13)) {
        jvmArgs("-XX:+AllowRedefinitionToAddDeleteMethods")
    }
}

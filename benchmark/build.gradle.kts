import dev.detekt.gradle.extensions.DetektExtension
import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

// Plain `com.android.test` module — not KMP, not a convention-plugin consumer. See
// docs/perf/profiling-plan.md (task 1) for why: Project.configureLinting() in build-logic isn't
// callable from a bare module script, so detekt/ktlint are configured by hand below instead of via
// the shared `taigamobile.*` plugins every other module uses.
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

android {
    namespace = "com.grappim.taigamobile.benchmark"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":androidApp"

    // Mirrors androidApp's own STORE dimension (build-logic's AppFlavors.kt) — a benchmark module
    // targeting a flavored app must declare the same dimension or its unflavored variant becomes
    // ambiguous against the target app's flavored release variants.
    flavorDimensions += "STORE"
    productFlavors {
        create("gplay") { dimension = "STORE" }
        create("fdroid") { dimension = "STORE" }
    }
}

baselineProfile {
    useConnectedDevices = true
}

// https://detekt.dev/docs/introduction/configurations/
configure<DetektExtension> {
    buildUponDefaultConfig.set(true)
    parallel.set(true)
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    allRules.set(false)
}

// ./gradlew --continue ktlintCheck
// ./gradlew ktlintFormat
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
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)

    ktlintRuleset(libs.composeRules.ktlint)
    detektPlugins(libs.composeRules.detekt)
}

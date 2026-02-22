plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.jetbrains.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.koin.compiler) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.composeMultiplatform) apply false

    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.jacocoAggregationResults)
    alias(libs.plugins.jacocoAggregationCoverage)
}

private val coverageExclusions = listOf(
    "**/R.class",
    "**/R$*.class",
    "**/BuildConfig.*",
    "**/Manifest*.*",
    "**/*Module*.*",
    "**/*Dagger*.*",
    "**/*Hilt*.*",
    "**/widgets/**",
    "**/navigation/**",
    "**/interceptors/**"
) + listOf(
    "**/*Module",
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

    "**/TaigaApp",
    "**/DrawerDestination",

    "**/*Activity",
    "**/*Screen*",
    "**/*Application",
    "**/*NavGraph*",

    "**/*NavDestination",
    "**/*Widget",
    "**/*Dialog",
    "**/*BottomSheet",
    "**/TaskFilters",
    "**/MainNavHost",

    "**/FileLoggingTree",
    "**/TryCatchExtensions",

    "**/StorageJsonProver",
    "**/TaigaPermissionConverter",
    "**/ColorSerializer",
    "**/ComposableUtils",
    "**/ObserveAsEvents*",
    "**/PreviewUtils*",
    "**/JsonSerializableNavType",
    "**/JsonSerializableNullableNavType",
    "**/IconSource*",
    "**/ColorSource",
    "**/ScrumNavDestination",
    "**/LifecycleEffects",
    "**/*Preference"
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
        exclude(projects.testing)
        exclude(projects.uikit)
        exclude(projects.tools.seed)
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

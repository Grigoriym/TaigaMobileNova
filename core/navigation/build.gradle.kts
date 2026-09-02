plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)

            // api: NavKey (from navigation3-ui) and SavedStateConfiguration (from androidx.savedstate)
            // are both part of this module's public API surface (route classes implement NavKey;
            // rememberNavigationState() takes a SavedStateConfiguration parameter) — every module that
            // depends on core:navigation needs them on its own compile classpath too.
            api(libs.jetbrains.navigation3.ui)
            api(libs.jetbrains.androidx.savedstate)
            implementation(libs.jetbrains.lifecycle.viewmodel.navigation3)
        }
    }
}

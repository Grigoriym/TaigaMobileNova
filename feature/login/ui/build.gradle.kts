plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.navigation)
            implementation(projects.utils.ui)
            implementation(projects.uikit)

            implementation(projects.feature.login.domain)

            implementation(libs.jetbrains.compose.icons.extended)
        }
    }
}

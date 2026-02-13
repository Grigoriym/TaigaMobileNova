plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.teams.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.uikit)
            implementation(projects.utils.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)

            implementation(projects.feature.users.domain)

            implementation(libs.coil.compose)
            implementation(libs.coil.ktor)
        }
    }
}

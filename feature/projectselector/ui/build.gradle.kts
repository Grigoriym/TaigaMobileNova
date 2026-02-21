plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.projectselector.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.uikit)
            implementation(projects.utils.ui)
            implementation(projects.core.navigation)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.feature.projects.domain)

            implementation(libs.androidx.paging.compose)
        }
    }
}

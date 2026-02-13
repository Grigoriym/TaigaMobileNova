plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.filters.ui"
}

kotlin {
    sourceSets{
        commonMain.dependencies {
            implementation(projects.feature.filters.domain)

            implementation(projects.strings)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)
            implementation(projects.utils.ui)
            implementation(projects.uikit)
            implementation(projects.core.navigation)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.dto)

            implementation(libs.androidx.paging.compose)
        }
    }
}

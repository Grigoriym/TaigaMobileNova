plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.filters.ui"
}

dependencies {
    implementation(projects.feature.filters.domain)

    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)
    implementation(projects.utils.ui)
    implementation(projects.uikit)
    implementation(projects.core.navigation)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.hilt.navigation.compose)
}

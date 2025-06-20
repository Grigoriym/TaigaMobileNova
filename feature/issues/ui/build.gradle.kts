plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.issues.ui"
}

dependencies {
    implementation(projects.feature.issues.domain)
    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.utils.ui)
    implementation(projects.uikit)
    implementation(projects.core.navigation)

    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.hilt.navigation.compose)
}

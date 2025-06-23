plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.sprint.ui"
}

dependencies {
    implementation(projects.feature.sprint.domain)
    implementation(projects.strings)
    implementation(projects.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.utils.ui)
    implementation(projects.core.navigation)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
}

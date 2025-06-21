plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.login.ui"
}

dependencies {
    implementation(projects.feature.login.domain)
    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.storage)
    implementation(projects.utils.ui)
    implementation(projects.uikit)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
}

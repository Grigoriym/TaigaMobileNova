plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.login.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.storage)
    implementation(projects.core.navigation)
    implementation(projects.utils.ui)
    implementation(projects.uikit)

    implementation(projects.feature.login.domain)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
}

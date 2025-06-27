plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.teams.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.uikit)
    implementation(projects.utils.ui)
    implementation(projects.core.navigation)
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)

    implementation(projects.feature.users.domain)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.coil.compose)
}

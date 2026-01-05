plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.utils.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core.domain)

    implementation(projects.core.async)

    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.robolectric)
}

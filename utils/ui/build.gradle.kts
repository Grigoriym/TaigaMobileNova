plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.utils.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core.domain)

    implementation(libs.timber)
    implementation(libs.androidx.paging.compose)

    testImplementation(libs.robolectric)
}

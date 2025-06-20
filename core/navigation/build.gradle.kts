plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.core.navigation"
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.androidx.hilt.navigation.compose)
}

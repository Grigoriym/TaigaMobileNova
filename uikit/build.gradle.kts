plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.uikit"
}

dependencies {
    implementation(projects.utils.ui)
    implementation(projects.strings)
    implementation(projects.core.domain)
    implementation(projects.core.navigation)

    implementation(libs.vanpra.color)
    implementation(libs.markdown)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.material)

    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)
}

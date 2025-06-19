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

    implementation(libs.vanpra.color)
    implementation(libs.markwon.core)
    implementation(libs.markwon.image.coil)
    implementation(libs.coil.compose)
    implementation(libs.material)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.core.api"
}

dependencies {
    implementation(libs.okhttp)
    implementation(projects.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(libs.retrofit)
    implementation(projects.core.appinfoApi)

    implementation(libs.timber)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.settings.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.appinfoApi)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.navigation)
    implementation(projects.utils.ui)
    implementation(projects.uikit)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.workitem.ui)
    implementation(projects.feature.filters.domain)

    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)

    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.navigation.compose)
}

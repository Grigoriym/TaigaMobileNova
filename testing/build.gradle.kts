plugins {
    alias(libs.plugins.taigamobile.android.library)
}

android {
    namespace = "com.grappim.taigamobile.testing"
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))

    implementation(projects.core.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.issues.domain)
    implementation(projects.feature.projects.domain)

    api(libs.junit4)
    api(libs.kotlinx.coroutines.test)
    api(libs.turbine)
    api(libs.mockk)
    api(libs.mockk.android)
    api(libs.androidx.arch.core.testing)
    api(libs.androidx.test.runner)
    api(libs.androidx.test.rules)
    api(libs.androidx.test.core)
    api(libs.androidx.compose.ui.test)
    implementation(libs.androidx.navigation.compose)

    debugApi(libs.androidx.compose.ui.testManifest)

    implementation(libs.androidx.appcompat)
    implementation(libs.robolectric)
}

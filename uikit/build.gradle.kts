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
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.users.domain)
    implementation(projects.core.navigation)

    implementation(libs.vanpra.color)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.material)
    implementation(libs.androidx.compose.material.icons.core)

    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)

    implementation(libs.markdownRenderer.core)
    implementation(libs.markdownRenderer.android)
    implementation(libs.markdownRenderer.m3)
    implementation(libs.markdownRenderer.coil)
}

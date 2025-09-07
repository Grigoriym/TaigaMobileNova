plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.scrum.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.navigation)
    implementation(projects.utils.ui)

    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.ui)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)
}

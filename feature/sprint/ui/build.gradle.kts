plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.sprint.ui"
}

dependencies {
    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.ui)
    implementation(projects.feature.users.domain)

    implementation(projects.strings)
    implementation(projects.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.utils.ui)
    implementation(projects.utils.formatter.datetime)
    implementation(projects.core.navigation)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
}

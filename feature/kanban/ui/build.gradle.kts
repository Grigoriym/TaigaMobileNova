plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.kanban.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.navigation)
    implementation(projects.utils.ui)
    implementation(projects.uikit)

    implementation(projects.feature.kanban.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.ui)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.swimlanes.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.workitem.domain)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)
    implementation(libs.coil.okhttp)
    implementation(libs.androidx.hilt.navigation.compose)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.epics.ui"
}

dependencies {
    implementation(projects.feature.epics.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.ui)
    implementation(projects.feature.workitem.ui)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.history.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.utils.formatter.datetime)

    implementation(projects.strings)
    implementation(projects.uikit)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.navigation)
    implementation(projects.utils.ui)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.paging.compose)

    implementation(libs.compose.colorpicker)

    implementation(libs.androidx.compose.material.icons.core)
}

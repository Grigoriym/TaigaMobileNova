plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)
    implementation(projects.utils.ui)
    implementation(projects.utils.formatter.decimal)
    implementation(projects.utils.formatter.datetime)
    implementation(projects.uikit)

    implementation(projects.feature.filters.domain)
    implementation(projects.feature.users.domain)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.coil.compose)
}

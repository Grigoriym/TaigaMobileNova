plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}
android {
    namespace = "com.grappim.taigamobile.feature.wiki.ui"
}

dependencies {
    implementation(projects.strings)
    implementation(projects.uikit)
    implementation(projects.utils.ui)
    implementation(projects.core.navigation)
    implementation(projects.core.domain)
    implementation(projects.core.storage)

    implementation(projects.feature.wiki.domain)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.workitem.ui)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.projects.domain)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.paging.compose)

    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
}

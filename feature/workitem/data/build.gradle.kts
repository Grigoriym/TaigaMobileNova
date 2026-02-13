plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.workitem.mapper)

    implementation(projects.feature.users.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.projects.domain)

    implementation(libs.retrofit)

    implementation(libs.androidx.paging.common)
}

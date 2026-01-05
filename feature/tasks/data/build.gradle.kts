plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.tasks.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.tasks.mapper)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.data)
    implementation(projects.feature.filters.mapper)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.projects.data)
    implementation(projects.feature.workitem.data)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.workitem.mapper)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.data)

    implementation(libs.retrofit)
}

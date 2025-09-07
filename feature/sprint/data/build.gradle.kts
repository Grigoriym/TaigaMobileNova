plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.sprint.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.issues.domain)

    implementation(libs.androidx.paging.common)
    implementation(libs.retrofit)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.issues.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)
    implementation(projects.utils.ui)

    implementation(projects.feature.issues.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.swimlanes.domain)
    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.issues.domain)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.history.domain)

    implementation(libs.retrofit)
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.androidx.paging.common)
}

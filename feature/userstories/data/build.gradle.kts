plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.android.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.userstories.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)
    implementation(projects.utils.ui)

    implementation(projects.feature.projects.domain)
    implementation(projects.feature.projects.data)
    
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.data)

    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.swimlanes.domain)

    implementation(projects.feature.workitem.data)
    implementation(projects.feature.workitem.domain)

    implementation(libs.androidx.paging.common)
    implementation(libs.retrofit)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

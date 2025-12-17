plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.filters.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.async)
    implementation(projects.core.storage)
    implementation(projects.utils.ui)

    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.mapper)
    implementation(projects.feature.filters.dto)

    implementation(projects.feature.workitem.data)
    implementation(projects.feature.workitem.domain)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.retrofit)
}

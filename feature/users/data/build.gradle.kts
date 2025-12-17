plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.users.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.mapper)
    implementation(projects.feature.users.dto)

    implementation(projects.feature.projects.data)
    implementation(projects.feature.projects.dto)

    implementation(libs.retrofit)
}

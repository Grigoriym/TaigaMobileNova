plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.projects.mapper"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)

    implementation(libs.moshi)

    implementation(projects.feature.projects.domain)
    implementation(projects.feature.projects.dto)
}

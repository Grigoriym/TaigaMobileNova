plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.users.mapper"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)

    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.dto)

    implementation(projects.feature.projects.dto)
}

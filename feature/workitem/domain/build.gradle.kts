plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
    alias(libs.plugins.taigamobile.kotlin.hilt)
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.projects.domain)
}

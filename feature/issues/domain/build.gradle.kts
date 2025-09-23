plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
    alias(libs.plugins.taigamobile.kotlin.hilt)
}

dependencies {
    implementation(projects.core.domain)

    implementation(projects.feature.sprint.domain)
    implementation(projects.feature.history.domain)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.workitem.domain)

    implementation(libs.androidx.paging.common)
}

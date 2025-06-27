plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.kanban.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.kanban.domain)
    implementation(projects.feature.filters.domain)
    implementation(projects.feature.swimlanes.domain)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.users.domain)
}

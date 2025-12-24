plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.dashboard.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.dashboard.domain)
    implementation(projects.feature.projects.domain)
    implementation(projects.feature.epics.domain)
    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.tasks.domain)
    implementation(projects.feature.issues.domain)
    implementation(projects.feature.workitem.domain)

    implementation(libs.retrofit)
}

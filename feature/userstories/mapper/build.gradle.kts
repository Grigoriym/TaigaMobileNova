plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.userstories.mapper"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)
    implementation(projects.core.storage)

    implementation(libs.moshi)

    implementation(projects.feature.userstories.domain)
    implementation(projects.feature.userstories.dto)

    implementation(projects.feature.epics.dto)

    implementation(projects.feature.users.dto)
    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.mapper)

    implementation(projects.feature.filters.mapper)
    implementation(projects.feature.filters.domain)

    implementation(projects.feature.projects.mapper)
    implementation(projects.feature.projects.dto)
    implementation(projects.feature.projects.domain)

    implementation(projects.feature.workitem.mapper)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.workitem.domain)
}

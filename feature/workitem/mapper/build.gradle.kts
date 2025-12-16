plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.mapper"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)
    implementation(projects.utils.formatter.datetime)

    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.dto)

    implementation(projects.feature.users.domain)
    implementation(projects.feature.users.mapper)
    implementation(projects.feature.users.dto)

    implementation(projects.feature.filters.mapper)
    implementation(projects.feature.filters.domain)

    implementation(projects.feature.userstories.dto)

    implementation(projects.feature.epics.dto)

    implementation(projects.feature.projects.dto)
    implementation(projects.feature.projects.mapper)
    implementation(projects.feature.projects.domain)

    implementation(libs.moshi)
}

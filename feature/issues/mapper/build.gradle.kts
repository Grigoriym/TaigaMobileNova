plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.issues.mapper"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)
    implementation(projects.core.storage)
    implementation(projects.utils.ui)

    implementation(projects.feature.issues.domain)
    implementation(projects.feature.issues.dto)

    implementation(projects.feature.users.mapper)
    implementation(projects.feature.users.dto)
    implementation(projects.feature.users.domain)

    implementation(projects.feature.projects.mapper)
    implementation(projects.feature.projects.dto)
    implementation(projects.feature.projects.domain)

    implementation(projects.feature.filters.mapper)
    implementation(projects.feature.filters.dto)
    implementation(projects.feature.filters.domain)

    implementation(projects.feature.workitem.mapper)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.workitem.domain)
}

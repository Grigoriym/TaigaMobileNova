plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.epics.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.epics.domain)
    implementation(projects.feature.epics.dto)
    implementation(projects.feature.epics.mapper)

    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.mapper)

    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.data)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.workitem.mapper)

    implementation(projects.feature.projects.domain)

    implementation(projects.feature.users.domain)

    implementation(libs.retrofit)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)

    implementation(libs.androidx.paging.common)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.history.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.history.domain)

    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.dto)
    implementation(projects.feature.workitem.mapper)

    implementation(libs.retrofit)
}

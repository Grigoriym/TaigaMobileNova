plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.feature.filters.mapper"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)
    implementation(projects.utils.ui)

    implementation(projects.feature.filters.domain)
    implementation(projects.feature.filters.dto)

    implementation(projects.feature.workitem.dto)
}

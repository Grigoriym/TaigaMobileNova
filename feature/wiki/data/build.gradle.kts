plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.wiki.data"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)
    implementation(projects.core.storage)
    implementation(projects.core.async)

    implementation(projects.feature.wiki.domain)

    implementation(projects.feature.workitem.mapper)
    implementation(projects.feature.workitem.domain)
    implementation(projects.feature.workitem.dto)

    implementation(libs.retrofit)
}

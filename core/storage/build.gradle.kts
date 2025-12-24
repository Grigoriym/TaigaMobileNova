plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

android {
    namespace = "com.grappim.taigamobile.core.storage"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)
    implementation(projects.feature.filters.domain)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.android)
}

plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
    alias(libs.plugins.taigamobile.kotlin.hilt)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

dependencies {
    implementation(projects.core.async)
}

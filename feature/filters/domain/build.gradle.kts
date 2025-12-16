plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
    alias(libs.plugins.taigamobile.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(projects.core.domain)
    implementation(projects.core.serialization)
}

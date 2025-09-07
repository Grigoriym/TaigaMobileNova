plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
    alias(libs.plugins.taigamobile.kotlin.hilt)
}

dependencies {
    implementation(projects.core.async)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

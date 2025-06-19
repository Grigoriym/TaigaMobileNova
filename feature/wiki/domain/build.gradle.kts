plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
}

dependencies {
    implementation(projects.core.domain)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

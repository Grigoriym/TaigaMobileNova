plugins {
    alias(libs.plugins.taigamobile.kotlin.library)
}

dependencies {
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

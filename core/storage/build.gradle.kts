plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.core.storage"
}

dependencies {
    implementation(projects.core.api)
    implementation(projects.core.domain)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

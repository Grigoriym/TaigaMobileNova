plugins {
    alias(libs.plugins.taigamobile.android.library)
    alias(libs.plugins.taigamobile.android.hilt)
}

android {
    namespace = "com.grappim.taigamobile.core.storage"
}

dependencies {
    implementation(projects.core.domain)
    implementation(projects.core.async)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

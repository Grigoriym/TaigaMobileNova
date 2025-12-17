plugins {
    alias(libs.plugins.taigamobile.android.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.filters.dto"
}

dependencies {
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.users.dto"
}

dependencies {
    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

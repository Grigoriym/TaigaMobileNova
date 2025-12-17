plugins {
    alias(libs.plugins.taigamobile.android.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.userstories.dto"
}

dependencies {
    implementation(projects.feature.epics.dto)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

plugins {
    alias(libs.plugins.taigamobile.android.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.dto"
}

dependencies {
    implementation(projects.feature.users.dto)
    implementation(projects.feature.projects.dto)
    implementation(projects.feature.epics.dto)
    implementation(projects.feature.userstories.dto)

    implementation(libs.moshi)
    ksp(libs.moshi.kotlin.codegen)
}

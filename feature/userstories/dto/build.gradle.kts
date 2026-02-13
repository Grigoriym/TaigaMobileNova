plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.userstories.dto"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.epics.dto)
        }
    }
}

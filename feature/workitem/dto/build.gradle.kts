plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.dto"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.users.dto)
            implementation(projects.feature.projects.dto)
            implementation(projects.feature.epics.dto)
            implementation(projects.feature.userstories.dto)
            implementation(projects.core.serialization)
        }
    }
}

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.history.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.feature.workitem.domain)
        }
    }
}

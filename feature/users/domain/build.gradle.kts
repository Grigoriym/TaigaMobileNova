plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

android {
    namespace = "com.grappim.taigamobile.feature.users.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}

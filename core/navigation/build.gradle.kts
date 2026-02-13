plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.core.navigation"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.navigation.compose)
        }
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}

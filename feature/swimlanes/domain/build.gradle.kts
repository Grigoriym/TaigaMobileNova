plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.stability)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
        }
    }
}

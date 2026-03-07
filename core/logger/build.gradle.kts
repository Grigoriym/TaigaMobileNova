plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.timber)
        }
    }
}

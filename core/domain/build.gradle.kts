plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        iosMain.dependencies {
            implementation(libs.kotlinx.io.core)
        }
    }
}

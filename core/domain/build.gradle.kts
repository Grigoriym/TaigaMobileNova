plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.core.domain"
}

kotlin {
    sourceSets {
        iosMain.dependencies {
            implementation(libs.kotlinx.io.core)
        }
    }
}

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
}

android {
    namespace = "com.grappim.taigamobile.core.logger"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.timber)
        }
    }
}

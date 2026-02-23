plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.core.asynckmp"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.atomicfu)
        }
    }
}

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.stability)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.grappim.kit.domain)
        }
        iosMain.dependencies {
            implementation(libs.kotlinx.io.core)
        }
    }
}

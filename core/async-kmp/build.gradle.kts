plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.grappim.kit.coroutines)
        }
    }
}

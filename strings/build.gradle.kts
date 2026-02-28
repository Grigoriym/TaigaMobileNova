plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.strings"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(libs.jetbrains.compose.components.resources)
        }
    }
}

compose.resources {
    packageOfResClass = "com.grappim.taigamobile.strings.generated.resources"
    generateResClass = always
    publicResClass = true
}

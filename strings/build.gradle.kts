plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.library.compose)
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

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.taigamobile.kmp.network)
}

android {
    namespace = "com.grappim.taigamobile.feature.swimlanes.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.core.storage)

            implementation(projects.feature.swimlanes.domain)
        }
    }
}

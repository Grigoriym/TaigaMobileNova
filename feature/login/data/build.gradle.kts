plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.network)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.login.data"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)

            implementation(projects.feature.login.domain)
            implementation(projects.feature.login.dto)
        }
    }
}

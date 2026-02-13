plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.swimlanes.data"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.ktor.core)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.core.storage)

            implementation(projects.feature.swimlanes.domain)
        }
    }
}

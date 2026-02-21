plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.projects.mapper"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.core.storage)

            implementation(projects.feature.projects.domain)
            implementation(projects.feature.projects.dto)
        }
    }
}

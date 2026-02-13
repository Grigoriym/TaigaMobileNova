plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.users.mapper"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)

            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.dto)

            implementation(projects.feature.projects.dto)
        }
    }
}

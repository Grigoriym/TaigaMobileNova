plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.network)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)

            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.mapper)
            implementation(projects.feature.users.dto)

            implementation(projects.feature.projects.data)
            implementation(projects.feature.projects.dto)
        }
    }
}

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.projects.data"
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
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)

            implementation(projects.feature.projects.domain)
            implementation(projects.feature.projects.dto)
            implementation(projects.feature.projects.mapper)

            implementation(projects.feature.filters.mapper)
            implementation(projects.feature.filters.domain)

            implementation(libs.androidx.paging.common)
        }
    }
}

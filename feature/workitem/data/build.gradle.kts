plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.data"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        jvmMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.ktor.core)

            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.asyncKmp)

            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.workitem.mapper)

            implementation(projects.feature.users.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.projects.domain)

            implementation(libs.androidx.paging.common)
        }
    }
}

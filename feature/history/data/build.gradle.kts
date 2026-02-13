plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.history.data"
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

            implementation(projects.feature.history.domain)

            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.workitem.mapper)
        }
    }
}

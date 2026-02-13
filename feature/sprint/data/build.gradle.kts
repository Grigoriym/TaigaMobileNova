plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.sprint.data"
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
            implementation(projects.core.serialization)

            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.tasks.domain)
            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.issues.domain)

            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.mapper)
            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.workitem.data)

            implementation(libs.androidx.paging.common)
        }
    }
}

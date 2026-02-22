plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
}

android {
    namespace = "com.grappim.taigamobile.feature.userstories.data"
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
            implementation(projects.utils.ui)

            implementation(projects.feature.projects.domain)
            implementation(projects.feature.projects.dto)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.mapper)
            implementation(projects.feature.filters.dto)

            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.userstories.dto)
            implementation(projects.feature.userstories.mapper)

            implementation(projects.feature.swimlanes.domain)

            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.workitem.data)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.mapper)

            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.dto)
            implementation(projects.feature.users.mapper)

            implementation(projects.feature.epics.dto)

            implementation(libs.androidx.paging.common)
        }
    }
}

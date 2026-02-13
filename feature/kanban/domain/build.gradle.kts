plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.kanban.domain"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.swimlanes.domain)
            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.projects.domain)
        }
    }
}

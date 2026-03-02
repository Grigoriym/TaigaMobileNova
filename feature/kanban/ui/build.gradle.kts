plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.core.api)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.navigation)
            implementation(projects.utils.ui)
            implementation(projects.uikit)

            implementation(projects.feature.kanban.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.ui)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.swimlanes.domain)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.workitem.domain)

            implementation(libs.coil.compose)
            implementation(libs.coil.ktor)
        }
    }
}

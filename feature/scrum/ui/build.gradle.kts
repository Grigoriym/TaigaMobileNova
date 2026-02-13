plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.library.compose)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.scrum.ui"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.strings)
            implementation(projects.uikit)
            implementation(projects.core.domain)
            implementation(projects.core.storage)
            implementation(projects.core.navigation)
            implementation(projects.utils.ui)
            implementation(projects.utils.formatter.datetime)

            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.ui)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.ui)

            implementation(libs.androidx.paging.runtime)
            implementation(libs.androidx.paging.compose)
        }
    }
}

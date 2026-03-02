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
            implementation(projects.utils.ui)

            implementation(projects.feature.projects.domain)
            implementation(projects.feature.projects.data)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.data)
            implementation(projects.feature.filters.mapper)

            implementation(projects.feature.swimlanes.domain)
            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.tasks.domain)
            implementation(projects.feature.userstories.domain)

            implementation(projects.feature.issues.domain)
            implementation(projects.feature.issues.dto)
            implementation(projects.feature.issues.mapper)

            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.data)
            implementation(projects.feature.history.domain)

            implementation(projects.feature.workitem.data)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.workitem.mapper)

            implementation(libs.androidx.paging.common)
        }
    }
}

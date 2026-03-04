plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.network)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
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

plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
}

android {
    namespace = "com.grappim.taigamobile.feature.epics.mapper"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.core.storage)

            implementation(projects.feature.epics.domain)
            implementation(projects.feature.epics.dto)

            implementation(projects.feature.filters.dto)
            implementation(projects.feature.filters.mapper)
            implementation(projects.feature.filters.domain)

            implementation(projects.feature.projects.mapper)
            implementation(projects.feature.projects.dto)
            implementation(projects.feature.projects.domain)

            implementation(projects.feature.users.mapper)
            implementation(projects.feature.users.dto)
            implementation(projects.feature.users.domain)

            implementation(projects.feature.workitem.dto)
        }
    }
}

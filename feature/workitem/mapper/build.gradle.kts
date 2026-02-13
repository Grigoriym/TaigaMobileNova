plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.di)
    alias(libs.plugins.taigamobile.kmp.serialization)
}

android {
    namespace = "com.grappim.taigamobile.feature.workitem.mapper"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.domain)
            implementation(projects.core.asyncKmp)
            implementation(projects.utils.formatter.datetime)

            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.dto)

            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.mapper)
            implementation(projects.feature.users.dto)

            implementation(projects.feature.filters.mapper)
            implementation(projects.feature.filters.domain)

            implementation(projects.feature.userstories.dto)

            implementation(projects.feature.epics.dto)

            implementation(projects.feature.projects.dto)
            implementation(projects.feature.projects.mapper)
            implementation(projects.feature.projects.domain)
        }
    }
}

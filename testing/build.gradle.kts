plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.taigamobile.kmp.network)
}

android {
    namespace = "com.grappim.taigamobile.testing"
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.test)
            api(libs.koin.test.junit4)
            api(libs.robolectric)
            api(libs.junit4)
            api(libs.androidx.test.core)
        }
        commonMain.dependencies {
            implementation(libs.compose.ui)

            api(libs.turbine)
            api(libs.kotlinx.coroutines.test)

            implementation(libs.androidx.paging.common)

            implementation(projects.core.domain)
            implementation(projects.core.storage)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.dto)
            implementation(projects.feature.filters.data)
            implementation(projects.feature.issues.domain)
            implementation(projects.feature.issues.ui)
            implementation(projects.feature.issues.data)
            implementation(projects.feature.issues.dto)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.projects.dto)
            api(projects.feature.projects.mapper)
            implementation(projects.feature.workitem.data)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.ui)
            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.dto)
            api(projects.feature.users.mapper)
            implementation(projects.feature.userstories.dto)
            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.epics.dto)
            implementation(projects.feature.epics.domain)
            implementation(projects.feature.epics.data)
            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.sprint.data)
            implementation(projects.feature.swimlanes.data)
            implementation(projects.feature.swimlanes.domain)
            implementation(projects.feature.history.data)
            implementation(projects.feature.history.domain)
            implementation(projects.feature.tasks.domain)
            implementation(projects.feature.tasks.data)
            implementation(projects.utils.ui)
            implementation(projects.utils.formatter.datetime)
        }
    }
}

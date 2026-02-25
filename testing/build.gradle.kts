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
        commonMain.dependencies {
            implementation(libs.compose.ui)

            api(libs.turbine)
            api(libs.kotlinx.coroutines.test)

            implementation(projects.core.domain)
            implementation(projects.core.storage)

            implementation(projects.feature.filters.domain)
            implementation(projects.feature.filters.dto)
            implementation(projects.feature.issues.domain)
            implementation(projects.feature.issues.ui)
            implementation(projects.feature.projects.domain)
            implementation(projects.feature.projects.dto)
            implementation(projects.feature.workitem.data)
            implementation(projects.feature.workitem.domain)
            implementation(projects.feature.workitem.ui)
            implementation(projects.feature.workitem.dto)
            implementation(projects.feature.users.domain)
            implementation(projects.feature.users.dto)
            implementation(projects.feature.userstories.dto)
            implementation(projects.feature.userstories.domain)
            implementation(projects.feature.epics.dto)
            implementation(projects.feature.epics.domain)
            implementation(projects.feature.sprint.domain)
            implementation(projects.feature.sprint.data)
            implementation(projects.feature.swimlanes.data)
            implementation(projects.feature.swimlanes.domain)
            implementation(projects.feature.tasks.domain)
            implementation(projects.feature.tasks.data)
            implementation(projects.utils.ui)
            implementation(projects.utils.formatter.datetime)
        }
    }
}

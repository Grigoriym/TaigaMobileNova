plugins {
    alias(libs.plugins.taigamobile.kmp.library)
    alias(libs.plugins.taigamobile.kmp.serialization)
    alias(libs.plugins.taigamobile.kmp.network)
}

kotlin {
    sourceSets {
        androidMain.dependencies {
            api(project.dependencies.platform(libs.koin.bom))
            api(libs.koin.test)
            api(libs.koin.test.junit4)
            api(libs.junit4)
        }
        commonMain.dependencies {
            implementation(libs.jetbrains.compose.ui)

            api(libs.turbine)
            api(libs.kotlinx.coroutines.test)

            implementation(libs.androidx.paging.common)

            api(projects.core.domain)
            api(projects.core.storage)

            api(projects.feature.login.domain)
            api(projects.feature.login.data)
            api(projects.feature.login.dto)

            api(projects.feature.filters.domain)
            api(projects.feature.filters.dto)
            api(projects.feature.filters.data)
            api(projects.feature.filters.mapper)

            api(projects.feature.issues.domain)
            api(projects.feature.issues.ui)
            api(projects.feature.issues.data)
            api(projects.feature.issues.dto)

            api(projects.feature.projects.domain)
            api(projects.feature.projects.dto)
            api(projects.feature.projects.data)
            api(projects.feature.projects.mapper)

            api(projects.feature.workitem.data)
            api(projects.feature.workitem.domain)
            api(projects.feature.workitem.ui)
            api(projects.feature.workitem.dto)

            api(projects.feature.users.domain)
            api(projects.feature.users.dto)
            api(projects.feature.users.data)
            api(projects.feature.users.mapper)

            api(projects.feature.userstories.dto)
            api(projects.feature.userstories.domain)
            api(projects.feature.userstories.data)
            api(projects.feature.userstories.mapper)

            api(projects.feature.epics.dto)
            api(projects.feature.epics.domain)
            api(projects.feature.epics.data)

            api(projects.feature.sprint.domain)
            api(projects.feature.sprint.data)

            api(projects.feature.swimlanes.data)
            api(projects.feature.swimlanes.domain)

            api(projects.feature.history.data)
            api(projects.feature.history.domain)

            api(projects.feature.tasks.domain)
            api(projects.feature.tasks.data)

            api(projects.feature.kanban.domain)

            api(projects.feature.wiki.domain)

            api(projects.feature.dashboard.domain)

            api(projects.utils.ui)
            api(projects.utils.formatter.datetime)
            api(projects.utils.formatter.decimal)
        }
    }
}

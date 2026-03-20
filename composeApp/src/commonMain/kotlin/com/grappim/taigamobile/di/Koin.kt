package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.api.KmpNetworkModule
import com.grappim.taigamobile.feature.dashboard.domain.DashboardDomainModule
import com.grappim.taigamobile.feature.dashboard.ui.DashboardUiModule
import com.grappim.taigamobile.feature.epics.data.EpicsDataModule
import com.grappim.taigamobile.feature.epics.domain.EpicsDomainModule
import com.grappim.taigamobile.feature.epics.mapper.EpicsMapperModule
import com.grappim.taigamobile.feature.epics.ui.EpicsUiModule
import com.grappim.taigamobile.feature.filters.data.FiltersDataModule
import com.grappim.taigamobile.feature.filters.mapper.FiltersMapperModule
import com.grappim.taigamobile.feature.history.data.HistoryDataModule
import com.grappim.taigamobile.feature.issues.data.IssuesDataModule
import com.grappim.taigamobile.feature.issues.domain.IssuesDomainModule
import com.grappim.taigamobile.feature.issues.mapper.IssuesMapperModule
import com.grappim.taigamobile.feature.issues.ui.IssuesUiModule
import com.grappim.taigamobile.feature.kanban.domain.KanbanDomainModule
import com.grappim.taigamobile.feature.kanban.ui.KanbanUiModule
import com.grappim.taigamobile.feature.login.data.LoginDataModule
import com.grappim.taigamobile.feature.login.ui.LoginUiModule
import com.grappim.taigamobile.feature.profile.domain.ProfileDomainModule
import com.grappim.taigamobile.feature.profile.ui.ProfileUiModule
import com.grappim.taigamobile.feature.projects.data.ProjectsDataModule
import com.grappim.taigamobile.feature.projects.mapper.ProjectsMapperModule
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorUiModule
import com.grappim.taigamobile.feature.scrum.ui.ScrumUiModule
import com.grappim.taigamobile.feature.settings.ui.SettingsUiModule
import com.grappim.taigamobile.feature.sprint.data.SprintDataModule
import com.grappim.taigamobile.feature.sprint.ui.SprintUiModule
import com.grappim.taigamobile.feature.swimlanes.data.SwimlanesDataModule
import com.grappim.taigamobile.feature.tasks.data.TasksDataModule
import com.grappim.taigamobile.feature.tasks.domain.TasksDomainModule
import com.grappim.taigamobile.feature.tasks.mapper.TasksMapperModule
import com.grappim.taigamobile.feature.tasks.ui.TasksUiModule
import com.grappim.taigamobile.feature.teams.ui.TeamsUiModule
import com.grappim.taigamobile.feature.users.data.UsersDataModule
import com.grappim.taigamobile.feature.users.mapper.UsersMapperModule
import com.grappim.taigamobile.feature.userstories.data.UserStoriesDataModule
import com.grappim.taigamobile.feature.userstories.domain.UserStoriesDomainModule
import com.grappim.taigamobile.feature.userstories.mapper.UserStoriesMapperModule
import com.grappim.taigamobile.feature.userstories.ui.UserStoriesUiModule
import com.grappim.taigamobile.feature.wiki.data.WikiDataModule
import com.grappim.taigamobile.feature.wiki.domain.WikiDomainModule
import com.grappim.taigamobile.feature.wiki.ui.WikiUiModule
import com.grappim.taigamobile.feature.workitem.data.WorkItemDataModule
import com.grappim.taigamobile.feature.workitem.mapper.WorkItemMapperModule
import com.grappim.taigamobile.feature.workitem.ui.di.WorkItemUiModule
import com.grappim.taigamobile.utils.formatter.datetime.DateTimeModule
import com.grappim.taigamobile.utils.formatter.decimal.DecimalFormatterModule
import com.grappim.taigamobile.utils.ui.di.UtilsUiModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.KoinApplication
import org.koin.core.annotation.Module

expect class PlatformComponentModule

@Module(
    includes = [
        KmpNetworkModule::class,
        DateTimeModule::class,
        DecimalFormatterModule::class,
        UtilsUiModule::class,
        // feature/dashboard
        DashboardDomainModule::class,
        DashboardUiModule::class,
        // feature/epics
        EpicsDataModule::class,
        EpicsDomainModule::class,
        EpicsMapperModule::class,
        EpicsUiModule::class,
        // feature/filters
        FiltersDataModule::class,
        FiltersMapperModule::class,
        // feature/history
        HistoryDataModule::class,
        // feature/issues
        IssuesDataModule::class,
        IssuesDomainModule::class,
        IssuesMapperModule::class,
        IssuesUiModule::class,
        // feature/kanban
        KanbanDomainModule::class,
        KanbanUiModule::class,
        // feature/login
        LoginDataModule::class,
        LoginUiModule::class,
        // feature/profile
        ProfileDomainModule::class,
        ProfileUiModule::class,
        // feature/projects
        ProjectsDataModule::class,
        ProjectsMapperModule::class,
        // feature/projectselector
        ProjectSelectorUiModule::class,
        // feature/scrum
        ScrumUiModule::class,
        // feature/settings
        SettingsUiModule::class,
        // feature/sprint
        SprintDataModule::class,
        SprintUiModule::class,
        // feature/swimlanes
        SwimlanesDataModule::class,
        // feature/tasks
        TasksDataModule::class,
        TasksDomainModule::class,
        TasksMapperModule::class,
        TasksUiModule::class,
        // feature/teams
        TeamsUiModule::class,
        // feature/users
        UsersDataModule::class,
        UsersMapperModule::class,
        // feature/userstories
        UserStoriesDataModule::class,
        UserStoriesDomainModule::class,
        UserStoriesMapperModule::class,
        UserStoriesUiModule::class,
        // feature/wiki
        WikiDataModule::class,
        WikiDomainModule::class,
        WikiUiModule::class,
        // feature/workitem
        WorkItemDataModule::class,
        WorkItemMapperModule::class,
        WorkItemUiModule::class
    ]
)
@Configuration
@ComponentScan("com.grappim.taigamobile")
class AppModule

@KoinApplication
object KoinApp

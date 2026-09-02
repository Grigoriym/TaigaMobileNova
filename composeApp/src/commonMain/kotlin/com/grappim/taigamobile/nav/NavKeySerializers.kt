package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.grappim.taigamobile.createtask.CreateTaskNavDestination
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsNavDestination
import com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination
import com.grappim.taigamobile.feature.issues.ui.details.IssueDetailsNavDestination
import com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination
import com.grappim.taigamobile.feature.kanban.ui.KanbanNavDestination
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.feature.profile.ui.ProfileNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination
import com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination
import com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination
import com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.AttributesScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesMenuNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesNavDestination
import com.grappim.taigamobile.feature.settings.ui.attributes.tags.TagsScreenRouteNavDestination
import com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreenNavDestination
import com.grappim.taigamobile.feature.settings.ui.modules.ModulesNavDestination
import com.grappim.taigamobile.feature.settings.ui.projectdetails.ProjectDetailsNavDestination
import com.grappim.taigamobile.feature.settings.ui.trustedcerts.TrustedCertificatesNavDestination
import com.grappim.taigamobile.feature.settings.ui.user.SettingsUserScreenNavDestination
import com.grappim.taigamobile.feature.sprint.ui.SprintNavDestination
import com.grappim.taigamobile.feature.tasks.ui.TaskDetailsNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreateLinkNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreatePageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiLinksNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination
import com.grappim.taigamobile.feature.wiki.ui.nav.WikiPagesNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.WorkItemEditDescriptionNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.edittags.WorkItemEditTagsNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.epic.WorkItemEditEpicNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.sprint.WorkItemEditSprintNavDestination
import com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.WorkItemEditTeamMemberNavDestination
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Registers every route class in the app as a `NavKey` subtype. Required on every non-JVM target:
 * Android's reflection-based polymorphic serialization isn't available on iOS, so
 * `rememberNavBackStack` needs this passed explicitly via a `SavedStateConfiguration` (see
 * `NavigationState.kt`, core:navigation) instead of relying on reflection. A route missing from
 * this list is a silent failure that only breaks back-stack restore after process death.
 *
 * Lives in composeApp, not core:navigation: it needs to reference every feature module's route
 * classes, and those modules depend on core:navigation, so putting it there would be a dependency
 * cycle.
 */
val navKeySerializersModule = SerializersModule {
    polymorphic(NavKey::class) {
        subclass(CreateTaskNavDestination::class)
        subclass(DashboardNavDestination::class)
        subclass(EpicDetailsNavDestination::class)
        subclass(EpicsNavDestination::class)
        subclass(IssueDetailsNavDestination::class)
        subclass(IssuesNavDestination::class)
        subclass(KanbanNavDestination::class)
        subclass(LoginNavDestination::class)
        subclass(ProfileNavDestination::class)
        subclass(ProjectSelectorNavDestination::class)
        subclass(ScrumBacklogDestination::class)
        subclass(ScrumClosedSprintsDestination::class)
        subclass(ScrumOpenSprintsDestination::class)
        subclass(SettingsNavDestination::class)
        subclass(SettingsAboutScreenRouteNavDestination::class)
        subclass(AttributesScreenNavDestination::class)
        subclass(ProjectValuesMenuNavDestination::class)
        subclass(ProjectValuesNavDestination::class)
        subclass(TagsScreenRouteNavDestination::class)
        subclass(SettingsInterfaceScreenNavDestination::class)
        subclass(ModulesNavDestination::class)
        subclass(ProjectDetailsNavDestination::class)
        subclass(TrustedCertificatesNavDestination::class)
        subclass(SettingsUserScreenNavDestination::class)
        subclass(SprintNavDestination::class)
        subclass(TaskDetailsNavDestination::class)
        subclass(TeamNavDestination::class)
        subclass(UserStoryDetailsNavDestination::class)
        subclass(WikiCreateLinkNavDestination::class)
        subclass(WikiCreatePageNavDestination::class)
        subclass(WikiLinksNavDestination::class)
        subclass(WikiNavDestination::class)
        subclass(WikiPageNavDestination::class)
        subclass(WikiPagesNavDestination::class)
        subclass(WorkItemEditDescriptionNavDestination::class)
        subclass(WorkItemEditTagsNavDestination::class)
        subclass(WorkItemEditEpicNavDestination::class)
        subclass(WorkItemEditSprintNavDestination::class)
        subclass(WorkItemEditTeamMemberNavDestination::class)
    }
}

/**
 * Passed to `rememberNavBackStack`/`rememberNavigationState` — `SavedStateConfiguration.DEFAULT`
 * is rejected outright.
 */
val navSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = navKeySerializersModule
}

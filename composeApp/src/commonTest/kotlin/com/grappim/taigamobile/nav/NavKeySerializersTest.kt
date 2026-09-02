package com.grappim.taigamobile.nav

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.ExperimentalSerializationApi
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A route missing from [navKeySerializersModule] is a quiet failure — the app runs fine and only
 * back-stack restore after process death breaks (no reflection-based polymorphic serialization on
 * non-JVM targets). This asserts every route class has a serializer registered under its default
 * serial name (its fully qualified class name — none of these routes override `@SerialName`).
 */
class NavKeySerializersTest {

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun `every route class has a serializer registered`() {
        val routeClassNames = listOf(
            "com.grappim.taigamobile.createtask.CreateTaskNavDestination",
            "com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination",
            "com.grappim.taigamobile.feature.epics.ui.details.EpicDetailsNavDestination",
            "com.grappim.taigamobile.feature.epics.ui.list.EpicsNavDestination",
            "com.grappim.taigamobile.feature.issues.ui.details.IssueDetailsNavDestination",
            "com.grappim.taigamobile.feature.issues.ui.list.IssuesNavDestination",
            "com.grappim.taigamobile.feature.kanban.ui.KanbanNavDestination",
            "com.grappim.taigamobile.feature.login.ui.LoginNavDestination",
            "com.grappim.taigamobile.feature.profile.ui.ProfileNavDestination",
            "com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination",
            "com.grappim.taigamobile.feature.scrum.ui.ScrumBacklogDestination",
            "com.grappim.taigamobile.feature.scrum.ui.ScrumClosedSprintsDestination",
            "com.grappim.taigamobile.feature.scrum.ui.ScrumOpenSprintsDestination",
            "com.grappim.taigamobile.feature.settings.ui.SettingsNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.about.SettingsAboutScreenRouteNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.attributes.AttributesScreenNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesMenuNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.attributes.projectvalues.ProjectValuesNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.attributes.tags.TagsScreenRouteNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.interfacescreen.SettingsInterfaceScreenNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.modules.ModulesNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.projectdetails.ProjectDetailsNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.trustedcerts.TrustedCertificatesNavDestination",
            "com.grappim.taigamobile.feature.settings.ui.user.SettingsUserScreenNavDestination",
            "com.grappim.taigamobile.feature.sprint.ui.SprintNavDestination",
            "com.grappim.taigamobile.feature.tasks.ui.TaskDetailsNavDestination",
            "com.grappim.taigamobile.feature.teams.ui.TeamNavDestination",
            "com.grappim.taigamobile.feature.userstories.ui.UserStoryDetailsNavDestination",
            "com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreateLinkNavDestination",
            "com.grappim.taigamobile.feature.wiki.ui.nav.WikiCreatePageNavDestination",
            "com.grappim.taigamobile.feature.wiki.ui.nav.WikiLinksNavDestination",
            "com.grappim.taigamobile.feature.wiki.ui.nav.WikiNavDestination",
            "com.grappim.taigamobile.feature.wiki.ui.nav.WikiPageNavDestination",
            "com.grappim.taigamobile.feature.wiki.ui.nav.WikiPagesNavDestination",
            "com.grappim.taigamobile.feature.workitem.ui.screens.editdescription.WorkItemEditDescriptionNavDestination",
            "com.grappim.taigamobile.feature.workitem.ui.screens.edittags.WorkItemEditTagsNavDestination",
            "com.grappim.taigamobile.feature.workitem.ui.screens.epic.WorkItemEditEpicNavDestination",
            "com.grappim.taigamobile.feature.workitem.ui.screens.sprint.WorkItemEditSprintNavDestination",
            "com.grappim.taigamobile.feature.workitem.ui.screens.teammembers.WorkItemEditTeamMemberNavDestination"
        )

        routeClassNames.forEach { serializedClassName ->
            assertNotNull(
                navKeySerializersModule.getPolymorphic(NavKey::class, serializedClassName),
                "$serializedClassName is missing from navKeySerializersModule"
            )
        }
    }
}

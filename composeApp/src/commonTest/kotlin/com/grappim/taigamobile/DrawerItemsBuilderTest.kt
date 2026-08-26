package com.grappim.taigamobile

import com.grappim.taigamobile.feature.projects.domain.ProjectSimple
import com.grappim.taigamobile.feature.projects.domain.TaigaPermission
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DrawerItemsBuilderTest {

    private val builder = DrawerItemsBuilder()

    private fun projectWith(
        permissions: ImmutableList<TaigaPermission> = persistentListOf(),
        isEpicsActivated: Boolean = false,
        isBacklogActivated: Boolean = false,
        isKanbanActivated: Boolean = false,
        isIssuesActivated: Boolean = false,
        isWikiActivated: Boolean = false
    ) = ProjectSimple(
        id = 1L,
        name = "Test",
        slug = "test",
        myPermissions = permissions,
        isEpicsActivated = isEpicsActivated,
        isBacklogActivated = isBacklogActivated,
        isKanbanActivated = isKanbanActivated,
        isIssuesActivated = isIssuesActivated,
        isWikiActivated = isWikiActivated,
        defaultSwimlane = null,
        isAdmin = false
    )

    private fun topLevelDestinations(items: ImmutableList<DrawerItem>): List<DrawerDestination> =
        items.filterIsInstance<DrawerItem.Destination>().map { it.destination }

    private fun wikiGroup(items: ImmutableList<DrawerItem>): DrawerItem.Group? =
        items.filterIsInstance<DrawerItem.Group>().firstOrNull { group ->
            group.items.any {
                it.destination == DrawerDestination.WikiLinks || it.destination == DrawerDestination.WikiPages
            }
        }

    private fun scrumGroup(items: ImmutableList<DrawerItem>): DrawerItem.Group? =
        items.filterIsInstance<DrawerItem.Group>().firstOrNull { group ->
            group.items.any { it.destination == DrawerDestination.ScrumBacklog }
        }

    // region always-present items

    @Test
    fun alwaysPresentItems_areIncluded() {
        val items = builder.build(projectWith())
        val destinations = topLevelDestinations(items)

        assertTrue(DrawerDestination.ProjectSelector in destinations)
        assertTrue(DrawerDestination.Dashboard in destinations)
        assertTrue(DrawerDestination.Team in destinations)
        assertTrue(DrawerDestination.Settings in destinations)
        assertTrue(items.any { it is DrawerItem.Divider })
    }

    // endregion

    // region epics

    @Test
    fun epicsItem_includedWhenPermissionAndFlagAreSet() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_EPICS), isEpicsActivated = true)
        )
        assertTrue(DrawerDestination.Epics in topLevelDestinations(items))
    }

    @Test
    fun epicsItem_excludedWhenFlagIsFalse() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_EPICS), isEpicsActivated = false)
        )
        assertFalse(DrawerDestination.Epics in topLevelDestinations(items))
    }

    @Test
    fun epicsItem_excludedWhenPermissionMissing() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(), isEpicsActivated = true)
        )
        assertFalse(DrawerDestination.Epics in topLevelDestinations(items))
    }

    // endregion

    // region issues

    @Test
    fun issuesItem_includedWhenPermissionAndFlagAreSet() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_ISSUES), isIssuesActivated = true)
        )
        assertTrue(DrawerDestination.Issues in topLevelDestinations(items))
    }

    @Test
    fun issuesItem_excludedWhenFlagIsFalse() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_ISSUES), isIssuesActivated = false)
        )
        assertFalse(DrawerDestination.Issues in topLevelDestinations(items))
    }

    @Test
    fun issuesItem_excludedWhenPermissionMissing() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(), isIssuesActivated = true)
        )
        assertFalse(DrawerDestination.Issues in topLevelDestinations(items))
    }

    // endregion

    // region kanban

    @Test
    fun kanbanItem_includedWhenPermissionAndFlagAreSet() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_US), isKanbanActivated = true)
        )
        assertTrue(DrawerDestination.Kanban in topLevelDestinations(items))
    }

    @Test
    fun kanbanItem_excludedWhenFlagIsFalse() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_US), isKanbanActivated = false)
        )
        assertFalse(DrawerDestination.Kanban in topLevelDestinations(items))
    }

    @Test
    fun kanbanItem_excludedWhenPermissionMissing() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(), isKanbanActivated = true)
        )
        assertFalse(DrawerDestination.Kanban in topLevelDestinations(items))
    }

    // endregion

    // region scrum

    @Test
    fun scrumGroup_includedWhenPermissionAndFlagAreSet() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_US), isBacklogActivated = true)
        )
        val scrum = scrumGroup(items)
        assertNotNull(scrum)
        val destinations = scrum.items.map { it.destination }
        assertTrue(DrawerDestination.ScrumBacklog in destinations)
        assertTrue(DrawerDestination.ScrumOpenSprints in destinations)
        assertTrue(DrawerDestination.ScrumClosedSprints in destinations)
    }

    @Test
    fun scrumGroup_excludedWhenFlagIsFalse() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(TaigaPermission.VIEW_US), isBacklogActivated = false)
        )
        assertNull(scrumGroup(items))
    }

    @Test
    fun scrumGroup_excludedWhenPermissionMissing() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(), isBacklogActivated = true)
        )
        assertNull(scrumGroup(items))
    }

    // endregion

    // region wiki

    @Test
    fun wikiGroup_includedWithBothPermissions() {
        val items = builder.build(
            projectWith(
                permissions = persistentListOf(TaigaPermission.VIEW_WIKI_PAGES, TaigaPermission.VIEW_WIKI_LINKS),
                isWikiActivated = true
            )
        )
        val wiki = wikiGroup(items)
        assertNotNull(wiki)
        val destinations = wiki.items.map { it.destination }
        assertTrue(DrawerDestination.WikiLinks in destinations)
        assertTrue(DrawerDestination.WikiPages in destinations)
    }

    @Test
    fun wikiGroup_includedWithOnlyLinksPermission() {
        val items = builder.build(
            projectWith(
                permissions = persistentListOf(TaigaPermission.VIEW_WIKI_LINKS),
                isWikiActivated = true
            )
        )
        val wiki = wikiGroup(items)
        assertNotNull(wiki)
        val destinations = wiki.items.map { it.destination }
        assertTrue(DrawerDestination.WikiLinks in destinations)
        assertFalse(DrawerDestination.WikiPages in destinations)
    }

    @Test
    fun wikiGroup_includedWithOnlyPagesPermission() {
        val items = builder.build(
            projectWith(
                permissions = persistentListOf(TaigaPermission.VIEW_WIKI_PAGES),
                isWikiActivated = true
            )
        )
        val wiki = wikiGroup(items)
        assertNotNull(wiki)
        val destinations = wiki.items.map { it.destination }
        assertFalse(DrawerDestination.WikiLinks in destinations)
        assertTrue(DrawerDestination.WikiPages in destinations)
    }

    @Test
    fun wikiGroup_excludedWhenFlagIsFalse() {
        val items = builder.build(
            projectWith(
                permissions = persistentListOf(TaigaPermission.VIEW_WIKI_PAGES, TaigaPermission.VIEW_WIKI_LINKS),
                isWikiActivated = false
            )
        )
        assertNull(wikiGroup(items))
    }

    @Test
    fun wikiGroup_excludedWhenPermissionsMissing() {
        val items = builder.build(
            projectWith(permissions = persistentListOf(), isWikiActivated = true)
        )
        assertNull(wikiGroup(items))
    }

    // endregion
}

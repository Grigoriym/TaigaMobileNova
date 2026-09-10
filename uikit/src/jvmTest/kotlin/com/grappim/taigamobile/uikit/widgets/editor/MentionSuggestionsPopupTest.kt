package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Desktop/JVM only: see docs/testing/compose-ui-test-spike.md.
class MentionSuggestionsPopupTest {

    private val alice = TeamMember(
        id = 1L,
        avatarUrl = null,
        name = "Alice Anderson",
        role = "Developer",
        username = "alice"
    )
    private val bob = TeamMember(id = 2L, avatarUrl = null, name = "Bob Baker", role = "QA", username = "bob")

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun rendersGivenMembersAndInvokesOnSelectWithTappedOne() = runComposeUiTest {
        var selected: TeamMember? = null

        setContent {
            TaigaMobilePreviewTheme {
                MentionSuggestionsPopup(
                    members = persistentListOf(alice, bob),
                    expanded = true,
                    onSelect = { selected = it },
                    onDismissRequest = {}
                )
            }
        }

        onNodeWithText("alice").assertExists()
        onNodeWithText("Alice Anderson").assertExists()
        onNodeWithText("bob").assertExists()
        onNodeWithText("Bob Baker").assertExists()

        onNodeWithText("bob").performClick()

        assertEquals(bob, selected)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun notExpandedRendersNoRows() = runComposeUiTest {
        var selected: TeamMember? = null

        setContent {
            TaigaMobilePreviewTheme {
                MentionSuggestionsPopup(
                    members = persistentListOf(alice),
                    expanded = false,
                    onSelect = { selected = it },
                    onDismissRequest = {}
                )
            }
        }

        onNodeWithText("alice").assertDoesNotExist()
        assertNull(selected)
    }
}

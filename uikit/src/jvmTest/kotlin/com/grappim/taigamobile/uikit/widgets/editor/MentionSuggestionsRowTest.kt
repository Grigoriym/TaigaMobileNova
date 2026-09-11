package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
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
class MentionSuggestionsRowTest {

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
    fun rendersOneChipPerMemberAndInvokesOnSelectWithTappedOne() = runComposeUiTest {
        var selected: TeamMember? = null

        setContent {
            TaigaMobilePreviewTheme {
                MentionSuggestionsRow(
                    members = persistentListOf(alice, bob),
                    onSelect = { selected = it }
                )
            }
        }

        onAllNodesWithTag(MENTION_SUGGESTION_CHIP_TEST_TAG).assertCountEquals(2)

        onNodeWithText("bob").performClick()

        assertEquals(bob, selected)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun emptyMembersRendersNoChips() = runComposeUiTest {
        var selected: TeamMember? = null

        setContent {
            TaigaMobilePreviewTheme {
                MentionSuggestionsRow(
                    members = persistentListOf(),
                    onSelect = { selected = it }
                )
            }
        }

        onAllNodesWithTag(MENTION_SUGGESTION_CHIP_TEST_TAG).assertCountEquals(0)
        assertNull(selected)
    }
}

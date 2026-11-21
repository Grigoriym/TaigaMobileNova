package com.grappim.taigamobile.feature.workitem.ui.widgets

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.feature.workitem.ui.delegates.description.WorkItemDescriptionState
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Desktop/JVM only: `runComposeUiTest` needs `compose.desktop.uiTestJUnit4` +
// `compose.desktop.currentOs`, wired for jvmTest only in build.gradle.kts. Android and iOS
// actuals are not exercised.
@OptIn(ExperimentalTestApi::class)
class WorkItemDescriptionWidgetTest {

    private val alice =
        TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice")

    @Test
    fun tappingACheckboxPassesBackTheDescriptionWithThatBoxFlipped() = runComposeUiTest {
        val description = "Ask @alice\n\n- [ ] first\n- [x] second"
        val toggled = setDescriptionWidget(description, members = persistentListOf(alice))

        awaitCheckboxes(2)
        onAllNodes(isToggleable())[0].assertIsOff().performClick()
        onAllNodes(isToggleable())[1].assertIsOn().performClick()

        assertEquals(
            listOf(
                "Ask @alice\n\n- [x] first\n- [x] second",
                "Ask @alice\n\n- [ ] first\n- [ ] second"
            ),
            toggled
        )
    }

    @Test
    fun checkboxesAreDisplayOnlyWhileOffline() = runComposeUiTest {
        setDescriptionWidget("- [ ] item", isOffline = true)

        awaitRenderedText("item")
        assertTrue(onAllNodes(isToggleable()).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun checkboxesAreDisplayOnlyWithoutModifyPermission() = runComposeUiTest {
        setDescriptionWidget("- [ ] item", canModify = false)

        awaitRenderedText("item")
        assertTrue(onAllNodes(isToggleable()).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun tapsAreIgnoredWhileADescriptionSaveIsInFlight() = runComposeUiTest {
        val toggled = setDescriptionWidget("- [ ] item", isLoading = true)

        awaitCheckboxes(1)
        onAllNodes(isToggleable())[0].performClick()

        assertTrue(toggled.isEmpty())
    }

    private fun ComposeUiTest.setDescriptionWidget(
        description: String,
        members: ImmutableList<TeamMember> = persistentListOf(),
        isOffline: Boolean = false,
        canModify: Boolean = true,
        isLoading: Boolean = false
    ): List<String> {
        val toggled = mutableListOf<String>()
        setContent {
            TaigaMobilePreviewTheme {
                WorkItemDescriptionWidget(
                    isOffline = isOffline,
                    descriptionState = WorkItemDescriptionState(isDescriptionLoading = isLoading),
                    description = description,
                    onDescriptionClick = {},
                    canModify = canModify,
                    members = members,
                    onDescriptionCheckboxToggle = { toggled += it }
                )
            }
        }
        return toggled
    }

    // The Markdown renderer parses asynchronously, which can take well over the 1s default under a full jvmTest run.
    private fun ComposeUiTest.awaitCheckboxes(count: Int) {
        waitUntil(timeoutMillis = RENDER_TIMEOUT_MS) { onAllNodes(isToggleable()).fetchSemanticsNodes().size == count }
    }

    private fun ComposeUiTest.awaitRenderedText(text: String) {
        waitUntil(timeoutMillis = RENDER_TIMEOUT_MS) {
            onAllNodes(hasText(text, substring = true)).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

private const val RENDER_TIMEOUT_MS = 5_000L

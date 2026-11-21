package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

// Desktop/JVM only: `runComposeUiTest` needs `compose.desktop.uiTestJUnit4` +
// `compose.desktop.currentOs`, wired for jvmTest only in build.gradle.kts. Android and iOS
// actuals are not exercised — mirrors uikit's CreateCommentBarTest.
@OptIn(ExperimentalTestApi::class)
class EditDescriptionContentTest {

    private val alice =
        TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice")
    private val bob = TeamMember(id = 2L, avatarUrl = null, name = "Bob Baker", role = "QA", username = "bob")

    @Test
    fun typingAtSignShowsSuggestionsAndSelectingOneInsertsMention() = runComposeUiTest {
        setEditDescriptionContent(initial = TextFieldValue(""), members = persistentListOf(alice, bob))

        onNodeWithTag(EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG).performTextInput("Hey @al")
        onNodeWithText("alice").assertExists()
        onNodeWithText("bob").assertDoesNotExist()

        onNodeWithText("alice").performClick()
        onNodeWithText("Hey @alice ").assertExists()
    }

    @Test
    fun toolbarBoldWrapsTheSelectionInTheDescription() = runComposeUiTest {
        val current = setEditDescriptionContent(initial = TextFieldValue("make bold", TextRange(5, 9)))

        onNodeWithContentDescription("Bold").performClick()

        assertEquals("make **bold**", current().text)
        onNodeWithTag(EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG).assertExists()
    }

    @Test
    fun previewRendersMarkdownAndSwitchingBackKeepsTextAndSelection() = runComposeUiTest {
        val initial = TextFieldValue("plain **strong**", TextRange(6, 16))
        setEditDescriptionContent(initial = initial)

        onNodeWithContentDescription("Preview").performClick()

        onNodeWithTag(EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG).assertDoesNotExist()
        onNodeWithTag(EDIT_DESCRIPTION_PREVIEW_TEST_TAG).assertExists()
        // The Markdown renderer parses asynchronously.
        waitUntil(timeoutMillis = 5_000) {
            onAllNodesWithText("plain strong", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        onNodeWithContentDescription("Bold").assertIsNotEnabled()

        onNodeWithContentDescription("Edit").performClick()

        val field = onNodeWithTag(EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG)
        field.assertTextEquals(initial.text)
        assertEquals(
            initial.selection,
            field.fetchSemanticsNode().config[SemanticsProperties.TextSelectionRange]
        )
    }

    private fun ComposeUiTest.setEditDescriptionContent(
        initial: TextFieldValue,
        members: ImmutableList<TeamMember> = persistentListOf()
    ): () -> TextFieldValue {
        var latest = initial
        setContent {
            TaigaMobilePreviewTheme {
                EditDescriptionHost(initial = initial, members = members, onChange = { latest = it })
            }
        }
        return { latest }
    }

    @Composable
    private fun EditDescriptionHost(
        initial: TextFieldValue,
        members: ImmutableList<TeamMember>,
        onChange: (TextFieldValue) -> Unit
    ) {
        var currentDescription by remember { mutableStateOf(initial) }
        var isPreview by remember { mutableStateOf(false) }
        EditDescriptionContent(
            state = EditDescriptionState(
                originalDescription = initial.text,
                currentDescription = currentDescription,
                onDescriptionChange = {
                    currentDescription = it
                    onChange(it)
                },
                isPreview = isPreview,
                onPreviewToggle = { isPreview = !isPreview },
                setIsDialogVisible = {},
                shouldGoBackWithCurrentValue = {}
            ),
            members = members
        )
    }
}

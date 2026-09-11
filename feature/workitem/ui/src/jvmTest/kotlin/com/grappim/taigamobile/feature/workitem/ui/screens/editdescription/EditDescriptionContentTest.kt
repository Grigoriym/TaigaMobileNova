package com.grappim.taigamobile.feature.workitem.ui.screens.editdescription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.text.input.TextFieldValue
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test

// Desktop/JVM only: `runComposeUiTest` needs `compose.desktop.uiTestJUnit4` +
// `compose.desktop.currentOs`, wired for jvmTest only in build.gradle.kts. Android and iOS
// actuals are not exercised — mirrors uikit's CreateCommentBarTest.
class EditDescriptionContentTest {

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun typingAtSignShowsSuggestionsAndSelectingOneInsertsMention() = runComposeUiTest {
        val alice =
            TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice")
        val bob = TeamMember(id = 2L, avatarUrl = null, name = "Bob Baker", role = "QA", username = "bob")

        setContent {
            TaigaMobilePreviewTheme {
                var currentDescription by remember { mutableStateOf(TextFieldValue("")) }
                EditDescriptionContent(
                    state = EditDescriptionState(
                        originalDescription = "",
                        currentDescription = currentDescription,
                        onDescriptionChange = { currentDescription = it },
                        setIsDialogVisible = {},
                        shouldGoBackWithCurrentValue = {}
                    ),
                    members = persistentListOf(alice, bob)
                )
            }
        }

        onNodeWithTag(EDIT_DESCRIPTION_TEXT_FIELD_TEST_TAG).performTextInput("Hey @al")
        onNodeWithText("alice").assertExists()
        onNodeWithText("bob").assertDoesNotExist()

        onNodeWithText("alice").performClick()
        onNodeWithText("Hey @alice ").assertExists()
    }
}

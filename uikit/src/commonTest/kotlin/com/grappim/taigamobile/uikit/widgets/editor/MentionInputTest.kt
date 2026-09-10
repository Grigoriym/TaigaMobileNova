package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MentionInputTest {

    @Test
    fun `findActiveMentionQuery finds a query while typing mid-word after at-sign`() {
        val value = TextFieldValue(text = "Hey @al", selection = TextRange(7))

        val result = findActiveMentionQuery(value)

        assertEquals(MentionQuery(query = "al", range = 4..6), result)
    }

    @Test
    fun `findActiveMentionQuery returns null after a completed mention with a trailing space`() {
        val value = TextFieldValue(text = "Hey @alice ", selection = TextRange(11))

        val result = findActiveMentionQuery(value)

        assertNull(result)
    }

    @Test
    fun `findActiveMentionQuery returns null when at-sign is preceded by a word character`() {
        val value = TextFieldValue(text = "email@alice", selection = TextRange(11))

        val result = findActiveMentionQuery(value)

        assertNull(result)
    }

    @Test
    fun `findActiveMentionQuery finds an empty query for a bare at-sign`() {
        val value = TextFieldValue(text = "@", selection = TextRange(1))

        val result = findActiveMentionQuery(value)

        assertEquals(MentionQuery(query = "", range = 0..0), result)
    }

    @Test
    fun `findActiveMentionQuery returns null when the selection is not collapsed`() {
        val value = TextFieldValue(text = "Hey @al", selection = TextRange(4, 7))

        val result = findActiveMentionQuery(value)

        assertNull(result)
    }

    @Test
    fun `insertMention splices the username and moves the cursor after the trailing space`() {
        val value = TextFieldValue(text = "Hey @al", selection = TextRange(7))
        val query = MentionQuery(query = "al", range = 4..6)

        val result = insertMention(value, query, "alice")

        assertEquals(TextFieldValue(text = "Hey @alice ", selection = TextRange(11)), result)
    }

    @Test
    fun `insertMention replaces a bare at-sign query`() {
        val value = TextFieldValue(text = "@", selection = TextRange(1))
        val query = MentionQuery(query = "", range = 0..0)

        val result = insertMention(value, query, "bob")

        assertEquals(TextFieldValue(text = "@bob ", selection = TextRange(5)), result)
    }
}

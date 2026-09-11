package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MentionQueryTest {

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

    @Test
    fun `containsMention returns true for a mention mid-text`() {
        assertTrue(containsMention("Hey @alice, can you check this?"))
    }

    @Test
    fun `containsMention returns false when at-sign is preceded by a word character`() {
        assertFalse(containsMention("contact me at email@alice"))
    }

    @Test
    fun `containsMention returns false for plain text with no at-sign`() {
        assertFalse(containsMention("no mentions here"))
    }

    @Test
    fun `containsMention returns true for a mention at the start of the text`() {
        assertTrue(containsMention("@bob please review"))
    }
}

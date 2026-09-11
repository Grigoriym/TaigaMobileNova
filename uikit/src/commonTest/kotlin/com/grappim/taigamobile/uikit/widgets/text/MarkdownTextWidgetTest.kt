package com.grappim.taigamobile.uikit.widgets.text

import com.grappim.taigamobile.feature.users.domain.TeamMember
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

class MarkdownTextWidgetTest {

    private val alice =
        TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice")
    private val bob = TeamMember(id = 2L, avatarUrl = null, name = "Bob Baker", role = "QA", username = "bob")

    @Test
    fun `rewriteMentions turns a matching username into a mention link`() {
        val result = rewriteMentions(
            text = "Hey @alice, can you check this?",
            members = persistentListOf(alice, bob)
        )

        assertEquals("Hey [@alice](mention:1), can you check this?", result)
    }

    @Test
    fun `rewriteMentions leaves a username with no matching member unchanged`() {
        val result = rewriteMentions(
            text = "Hey @carol, are you there?",
            members = persistentListOf(alice, bob)
        )

        assertEquals("Hey @carol, are you there?", result)
    }

    @Test
    fun `rewriteMentions does not treat an at-sign preceded by a word character as a mention`() {
        val result = rewriteMentions(
            text = "contact me at email@alice",
            members = persistentListOf(alice, bob)
        )

        assertEquals("contact me at email@alice", result)
    }

    @Test
    fun `rewriteMentions returns the original text unchanged when no members are given`() {
        val result = rewriteMentions(
            text = "Hey @alice, can you check this?",
            members = persistentListOf()
        )

        assertEquals("Hey @alice, can you check this?", result)
    }
}

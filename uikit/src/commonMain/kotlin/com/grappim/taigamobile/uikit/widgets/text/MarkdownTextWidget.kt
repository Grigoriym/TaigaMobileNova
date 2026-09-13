package com.grappim.taigamobile.uikit.widgets.text

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.LinkInteractionListener
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownParagraph
import com.mikepenz.markdown.compose.elements.MarkdownText
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.elements.MarkdownCheckBox
import com.mikepenz.markdown.utils.getUnescapedTextInNode
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

private const val MENTION_URL_SCHEME = "mention:"
private val mentionRegex = Regex("""\B@([\w.-]+)\b""")

/**
 * If you define onClick, either internally or externally, make isSelectable = false as well
 */
@Composable
fun MarkdownTextWidget(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    members: ImmutableList<TeamMember> = persistentListOf(),
    onMentionClick: (Long) -> Unit = {}
) {
    Markdown(
        modifier = modifier
            .then(
                if (onClick != null) {
                    Modifier
                        .clickable { onClick() }
                } else {
                    modifier
                }
            ),
        content = rewriteMentions(text, members),
        imageTransformer = Coil3ImageTransformerImpl,
        components = markdownComponents(
            text = { model ->
                MarkdownText(
                    content = model.node.getUnescapedTextInNode(model.content),
                    node = model.node,
                    style = model.typography.text,
                    annotatorSettings = mentionAnnotatorSettings(onMentionClick)
                )
            },
            paragraph = { model ->
                MarkdownParagraph(
                    content = model.content,
                    node = model.node,
                    style = model.typography.paragraph,
                    annotatorSettings = mentionAnnotatorSettings(onMentionClick)
                )
            },
            checkbox = { model -> MarkdownCheckBox(model.content, model.node, style = model.typography.text) }
        )
    )
}

@Composable
private fun mentionAnnotatorSettings(onMentionClick: (Long) -> Unit): AnnotatorSettings {
    val defaultSettings = annotatorSettings()
    return remember(defaultSettings, onMentionClick) {
        object : AnnotatorSettings by defaultSettings {
            override val linkInteractionListener: LinkInteractionListener = LinkInteractionListener { link ->
                val mentionId = (link as? LinkAnnotation.Url)?.url
                    ?.takeIf { it.startsWith(MENTION_URL_SCHEME) }
                    ?.removePrefix(MENTION_URL_SCHEME)
                    ?.toLongOrNull()
                if (mentionId != null) {
                    onMentionClick(mentionId)
                } else {
                    defaultSettings.linkInteractionListener?.onClick(link)
                }
            }
        }
    }
}

internal fun rewriteMentions(text: String, members: ImmutableList<TeamMember>): String {
    if (members.isEmpty()) return text
    val membersByUsername = members.associateBy { it.username }
    return mentionRegex.replace(text) { match ->
        val username = match.groupValues[1]
        val member = membersByUsername[username]
        if (member != null) {
            "[@$username]($MENTION_URL_SCHEME${member.id})"
        } else {
            match.value
        }
    }
}

private val previewTeamMembers: ImmutableList<TeamMember> = persistentListOf(
    TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice"),
    TeamMember(id = 2L, avatarUrl = null, name = "Bob Baker", role = "QA", username = "bob")
)

@PreviewTaigaDarkLight
@Composable
private fun MarkdownTextWidgetMentionPreview() = TaigaMobilePreviewTheme {
    MarkdownTextWidget(
        text = "Hey @alice, can you take a look? Looping in @carol too.",
        members = previewTeamMembers
    )
}

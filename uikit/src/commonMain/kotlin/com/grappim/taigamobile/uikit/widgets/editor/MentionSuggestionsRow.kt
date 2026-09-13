package com.grappim.taigamobile.uikit.widgets.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.grappim.taigamobile.feature.users.domain.TeamMember
import com.grappim.taigamobile.uikit.generated.resources.default_avatar
import com.grappim.taigamobile.uikit.theme.TaigaMobilePreviewTheme
import com.grappim.taigamobile.uikit.theme.dialogTonalElevation
import com.grappim.taigamobile.uikit.utils.PreviewTaigaDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.painterResource

const val MENTION_SUGGESTION_CHIP_TEST_TAG = "mention_suggestion_chip"

/**
 * Inline, horizontally-scrollable row of team-member mention suggestions, shown while the
 * user is typing an active `@`-mention query in a text field. Renders in normal layout flow
 * (no `Popup`/floating window), and renders nothing when [members] is empty.
 */
@Composable
fun MentionSuggestionsRow(
    members: ImmutableList<TeamMember>,
    onSelect: (TeamMember) -> Unit,
    modifier: Modifier = Modifier
) {
    if (members.isNotEmpty()) {
        LazyRow(
            modifier = modifier
                .background(MaterialTheme.colorScheme.surfaceColorAtElevation(dialogTonalElevation)),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members, key = { it.id }) { member ->
                MentionSuggestionChip(member = member, onSelect = onSelect)
            }
        }
    }
}

@Composable
private fun MentionSuggestionChip(member: TeamMember, onSelect: (TeamMember) -> Unit) {
    Row(
        modifier = Modifier
            .testTag(MENTION_SUGGESTION_CHIP_TEST_TAG)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onSelect(member) }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            placeholder = painterResource(RDrawable.default_avatar),
            error = painterResource(RDrawable.default_avatar),
            model = member.avatarUrl
        )

        Spacer(Modifier.width(6.dp))

        Text(
            text = member.username,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

private val previewMembers: ImmutableList<TeamMember> = persistentListOf(
    TeamMember(id = 1L, avatarUrl = null, name = "Alice Anderson", role = "Developer", username = "alice"),
    TeamMember(id = 2L, avatarUrl = null, name = "Bob Baker", role = "QA", username = "bob")
)

@PreviewTaigaDarkLight
@Composable
private fun MentionSuggestionsRowPreview() = TaigaMobilePreviewTheme {
    MentionSuggestionsRow(
        members = previewMembers,
        onSelect = {}
    )
}

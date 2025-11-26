package com.grappim.taigamobile.feature.teams.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.grappim.taigamobile.core.domain.TeamMemberDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.PreviewDarkLight
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.text.NothingToSeeHereText
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun TeamScreen(
    showMessage: (message: Int) -> Unit,
    goToProfile: (userId: Long) -> Unit,
    viewModel: TeamViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.team)
            )
        )
    }
    LaunchedEffect(state.isError) {
        if (state.isError) {
            showMessage(RString.common_error_message)
        }
    }

    TeamScreenContent(
        state = state,
        onUserItemClick = { userId ->
            goToProfile(userId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamScreenContent(
    state: TeamState,
    modifier: Modifier = Modifier,
    onUserItemClick: (userId: Long) -> Unit = { _ -> }
) {
    PullToRefreshBox(
        modifier = modifier.fillMaxSize(),
        isRefreshing = state.isLoading,
        onRefresh = state.onRefresh
    ) {
        when {
            state.teamMemberDTOS.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    NothingToSeeHereText()
                }
            }

            else -> {
                LazyColumn(Modifier.padding(horizontal = mainHorizontalScreenPadding)) {
                    items(state.teamMemberDTOS) { member ->
                        TeamMemberItem(
                            teamMemberDTO = member,
                            onUserItemClick = { onUserItemClick(member.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 10.dp),
                            thickness = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamMemberItem(teamMemberDTO: TeamMemberDTO, onUserItemClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable { onUserItemClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.6f)
        ) {
            AsyncImage(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(RDrawable.default_avatar),
                error = painterResource(RDrawable.default_avatar),
                model = teamMemberDTO.avatarUrl
            )

            Spacer(Modifier.width(6.dp))

            Column {
                Text(
                    text = teamMemberDTO.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = teamMemberDTO.role,
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(0.4f)
        ) {
            Text(
                text = teamMemberDTO.totalPower.toString(),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(Modifier.width(4.dp))

            Text(
                text = stringResource(RString.power),
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@[Composable PreviewDarkLight]
private fun TeamScreenPreview() = TaigaMobileTheme {
    TeamScreenContent(
        state = TeamState(onRefresh = {})
    )
}

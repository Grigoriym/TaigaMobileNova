package com.grappim.taigamobile.team

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.grappim.taigamobile.core.domain.TeamMember
import com.grappim.taigamobile.main.topbar.LocalTopBarConfig
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.ui.utils.LoadingResult
import com.grappim.taigamobile.ui.utils.SubscribeOnError
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.theme.mainHorizontalScreenPadding
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoader
import com.grappim.taigamobile.uikit.widgets.text.NothingToSeeHereText
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun TeamScreen(
    showMessage: (message: Int) -> Unit,
    goToProfile: (userId: Long) -> Unit,
    viewModel: TeamViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.team)
            )
        )
    }

    val team by viewModel.team.collectAsState()
    team.SubscribeOnError(showMessage)

    TeamScreenContent(
        team = team.data.orEmpty(),
        isLoading = team is LoadingResult,
        onUserItemClick = { userId ->
            goToProfile(userId)
        }
    )
}

@Composable
fun TeamScreenContent(
    modifier: Modifier = Modifier,
    team: List<TeamMember> = emptyList(),
    isLoading: Boolean = false,
    onUserItemClick: (userId: Long) -> Unit = { _ -> }
) = Column(modifier.fillMaxSize()) {
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularLoader()
            }
        }

        team.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                NothingToSeeHereText()
            }
        }

        else -> {
            LazyColumn(Modifier.padding(horizontal = mainHorizontalScreenPadding)) {
                items(team) { member ->
                    TeamMemberItem(
                        teamMember = member,
                        onUserItemClick = { onUserItemClick(member.id) }
                    )
                    Spacer(Modifier.height(10.dp))
                }

                item {
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    }
}

@Composable
private fun TeamMemberItem(teamMember: TeamMember, onUserItemClick: () -> Unit) = Row(
    modifier = Modifier.clickable { onUserItemClick() },
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(0.6f)
    ) {
        Image(
            painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current).data(
                    teamMember.avatarUrl ?: RDrawable.default_avatar
                ).apply(fun ImageRequest.Builder.() {
                    error(RDrawable.default_avatar)
                    crossfade(true)
                }).build()
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
        )

        Spacer(Modifier.width(6.dp))

        Column {
            Text(
                text = teamMember.name,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = teamMember.role,
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
            text = teamMember.totalPower.toString(),
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.width(4.dp))

        Text(
            text = stringResource(RString.power),
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TeamScreenPreview() = TaigaMobileTheme {
    TeamScreenContent(
        team = List(3) {
            TeamMember(
                id = 0L,
                avatarUrl = null,
                name = "First Last",
                role = "Cool guy",
                username = "username",
                totalPower = 14
            )
        }
    )
}

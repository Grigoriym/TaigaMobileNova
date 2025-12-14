package com.grappim.taigamobile.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.ErrorStateWidget
import com.grappim.taigamobile.uikit.widgets.TaigaLoadingDialog
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel(), showSnackbar: (NativeText) -> Unit) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.profile),
                navigationIcon = NavigationIconConfig.Back()
            )
        )
    }

    LaunchedEffect(state.error) {
        if (state.error.isNotEmpty()) {
            showSnackbar(state.error)
        }
    }

    TaigaLoadingDialog(isVisible = state.isLoading)

    if (state.error.isNotEmpty()) {
        ErrorStateWidget(
            modifier = Modifier.fillMaxSize(),
            message = state.error,
            onRetry = {
                state.onReload()
            }
        )
    } else {
        ProfileScreenContent(
            state = state
        )
    }
}

@Composable
fun ProfileScreenContent(state: ProfileState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AsyncImage(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(RDrawable.default_avatar),
                    error = painterResource(RDrawable.default_avatar),
                    model = state.user?.avatarUrl
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = state.user?.fullName.orEmpty(),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = stringResource(
                        RString.username_template
                    ).format(state.user?.username.orEmpty()),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            state.userStats?.roles?.let { roles ->
                items(roles) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.outline,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                Spacer(Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ColumnTextData(
                        state.userStats?.totalNumProjects.toString(),
                        stringResource(RString.projects)
                    )
                    ColumnTextData(
                        state.userStats?.totalNumClosedUserStories.toString(),
                        stringResource(RString.closed_user_story)
                    )
                    ColumnTextData(
                        state.userStats?.totalNumContacts.toString(),
                        stringResource(RString.contacts)
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = stringResource(RString.projects),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(12.dp))
            }

            items(state.projects) {
                ProjectCard(
                    project = it,
                    isCurrent = it.id == state.currentProjectId
                )
                Spacer(Modifier.height(12.dp))
            }

            item {
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun ColumnTextData(titleText: String, bodyText: String) = Column(
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = titleText,
        style = MaterialTheme.typography.titleMedium
    )

    Spacer(Modifier.height(2.dp))

    Text(
        text = bodyText,
        color = MaterialTheme.colorScheme.outline,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ProfileScreenPreview() {
    ProfileScreenContent(
        state = ProfileState()
    )
}

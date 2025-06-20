package com.grappim.taigamobile.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import com.grappim.taigamobile.core.domain.Project
import com.grappim.taigamobile.core.domain.Stats
import com.grappim.taigamobile.core.domain.User
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.list.ProjectCard
import com.grappim.taigamobile.uikit.widgets.loader.CircularLoader
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.ErrorResult
import com.grappim.taigamobile.utils.ui.LoadingResult
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit = {}
) {
    val topBarController = LocalTopBarConfig.current
    LaunchedEffect(Unit) {
        viewModel.onOpen()

        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.profile),
                showBackButton = true
            )
        )
    }

    val currentUser by viewModel.currentUser.collectAsState()
    currentUser.SubscribeOnError(showMessage)
    val currentUserStats by viewModel.currentUserStats.collectAsState()
    currentUserStats.SubscribeOnError(showMessage)
    val currentUserProjects by viewModel.currentUserProjects.collectAsState()
    currentUserProjects.SubscribeOnError(showMessage)
    val currentProjectId by viewModel.currentProjectId.collectAsState()

    ProfileScreenContent(
        currentUser = currentUser.data,
        currentUserStats = currentUserStats.data,
        currentUserProjects = currentUserProjects.data ?: emptyList(),
        currentProjectId = currentProjectId,
        isLoading = currentUser is LoadingResult ||
            currentUserStats is LoadingResult ||
            currentUserProjects is LoadingResult,
        isError = currentUser is ErrorResult ||
            currentUserStats is ErrorResult ||
            currentUserProjects is ErrorResult
    )
}

@Composable
fun ProfileScreenContent(
    modifier: Modifier = Modifier,
    currentUser: User? = null,
    currentUserStats: Stats? = null,
    currentUserProjects: List<Project> = emptyList(),
    currentProjectId: Long = 0,
    isLoading: Boolean = false,
    isError: Boolean = false
) = Column(
    modifier = modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    if (isLoading || isError) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularLoader()
        }
    } else {
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(
                                currentUser?.avatarUrl ?: RDrawable.default_avatar
                            ).apply(fun ImageRequest.Builder.() {
                                error(RDrawable.default_avatar)
                                crossfade(true)
                            }).build()
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = currentUser?.fullName.orEmpty(),
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = stringResource(
                        RString.username_template
                    ).format(currentUser?.username.orEmpty()),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            item {
                Spacer(Modifier.height(16.dp))
            }

            currentUserStats?.roles?.let { roles ->
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
                        currentUserStats?.totalNumProjects.toString(),
                        stringResource(RString.projects)
                    )
                    ColumnTextData(
                        currentUserStats?.totalNumClosedUserStories.toString(),
                        stringResource(RString.closed_user_story)
                    )
                    ColumnTextData(
                        currentUserStats?.totalNumContacts.toString(),
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

            items(currentUserProjects) {
                ProjectCard(
                    project = it,
                    isCurrent = it.id == currentProjectId
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
    val currentUser = User(
        id = 123,
        fullName = null,
        photo = null,
        bigPhoto = null,
        username = "@username",
        name = "Cool user",
        pk = null
    )
    val currentUserStats = Stats(
        roles = listOf(
            "Design",
            "Front"
        ),
        totalNumClosedUserStories = 4,
        totalNumContacts = 48,
        totalNumProjects = 3
    )
    val currentUserProjects = listOf(
        Project(
            id = 1,
            name = "Cool project1",
            slug = "slug",
            description = "Cool description1",
            fansCount = 10,
            watchersCount = 3
        ),
        Project(
            id = 2,
            name = "Cool project2",
            slug = "slug",
            description = "Cool description2",
            fansCount = 1,
            watchersCount = 4
        ),
        Project(
            id = 3,
            name = "Cool project3",
            slug = "slug",
            description = "Cool description3",
            fansCount = 99,
            watchersCount = 0
        )
    )

    ProfileScreenContent(
        currentUser = currentUser,
        currentUserStats = currentUserStats,
        currentUserProjects = currentUserProjects
    )
}

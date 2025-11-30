package com.grappim.taigamobile.feature.projectselector.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.grappim.taigamobile.core.domain.ProjectDTO
import com.grappim.taigamobile.strings.RString
import com.grappim.taigamobile.uikit.theme.TaigaMobileTheme
import com.grappim.taigamobile.uikit.utils.RDrawable
import com.grappim.taigamobile.uikit.widgets.container.ContainerBoxWidget
import com.grappim.taigamobile.uikit.widgets.topbar.LocalTopBarConfig
import com.grappim.taigamobile.uikit.widgets.topbar.NavigationIconConfig
import com.grappim.taigamobile.uikit.widgets.topbar.TopBarConfig
import com.grappim.taigamobile.utils.ui.NativeText
import com.grappim.taigamobile.utils.ui.SubscribeOnError
import kotlinx.coroutines.flow.flowOf

@Composable
fun ProjectSelectorScreen(
    showMessage: (message: Int) -> Unit,
    onProjectSelect: (isFromLogin: Boolean) -> Unit,
    viewModel: ProjectSelectorViewModel = hiltViewModel()
) {
    val topBarController = LocalTopBarConfig.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        topBarController.update(
            TopBarConfig(
                title = NativeText.Resource(RString.project_selector),
                navigationIcon = if (state.isFromLogin) {
                    NavigationIconConfig.Back()
                } else {
                    NavigationIconConfig.Menu
                }
            )
        )
    }

    val lazyProjectItems = viewModel.projects.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    lazyProjectItems.SubscribeOnError(showMessage)

    ProjectSelectorScreenContent(
        state = state,
        searchQuery = searchQuery,
        projects = lazyProjectItems,
        selectProject = {
            state.setProject(it)
            onProjectSelect(state.isFromLogin)
        }
    )
}

@Composable
fun ProjectSelectorScreenContent(
    state: ProjectSelectorState,
    searchQuery: String,
    projects: LazyPagingItems<ProjectDTO>,
    modifier: Modifier = Modifier,
    selectProject: (ProjectDTO) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = searchQuery,
            onValueChange = state.setProjectsQuery,
            shape = RoundedCornerShape(16.dp),
            placeholder = {
                Text(stringResource(RString.search_projects_hint))
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn {
            items(projects.itemCount) { index ->
                val project = projects[index]
                if (project != null) {
                    ItemProject(
                        projectDTO = project,
                        currentProjectId = state.currentProjectId,
                        onClick = { selectProject(project) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemProject(projectDTO: ProjectDTO, currentProjectId: Long, onClick: () -> Unit = {}) {
    ContainerBoxWidget(
        verticalPadding = 16.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(0.8f)) {
                projectDTO.takeIf { it.isMember || it.isAdmin || it.isOwner }?.let {
                    Text(
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        text = stringResource(
                            when {
                                projectDTO.isOwner -> RString.project_owner
                                projectDTO.isAdmin -> RString.project_admin
                                projectDTO.isMember -> RString.project_member
                                else -> 0
                            }
                        )
                    )
                }

                Text(
                    text = stringResource(RString.project_name_template).format(
                        projectDTO.name,
                        projectDTO.slug
                    )
                )
            }

            if (projectDTO.id == currentProjectId) {
                Image(
                    painter = painterResource(RDrawable.ic_check),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.weight(0.2f)
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun ProjectSelectorScreenPreview() = TaigaMobileTheme {
    ProjectSelectorScreenContent(
        projects = flowOf(
            PagingData.empty<ProjectDTO>()
        ).collectAsLazyPagingItems(),
        searchQuery = "",
        state = ProjectSelectorState(
            currentProjectId = 1L,
            setProjectsQuery = {},
            setProject = {}
        )
    )
}

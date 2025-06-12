package io.eugenethedev.taigamobile.projectselector

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import com.grappim.taigamobile.R
import io.eugenethedev.taigamobile.domain.entities.Project
import io.eugenethedev.taigamobile.ui.components.containers.ContainerBox
import io.eugenethedev.taigamobile.ui.components.editors.SelectorList
import io.eugenethedev.taigamobile.ui.components.editors.SelectorListConstants
import io.eugenethedev.taigamobile.ui.theme.TaigaMobileTheme
import io.eugenethedev.taigamobile.ui.utils.SubscribeOnError
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProjectSelectorScreen(
    viewModel: ProjectSelectorViewModel = hiltViewModel(),
    showMessage: (message: Int) -> Unit,
    onBack: () -> Unit,
    onProjectSelected: (isFromLogin: Boolean) -> Unit,
) {
    LaunchedEffect(Unit) {
        viewModel.onOpen()
    }
    val coroutineScope = rememberCoroutineScope()

    val projects = viewModel.projects
    projects.SubscribeOnError(showMessage)

    val currentProjectId by viewModel.currentProjectId.collectAsState()

    var isSelectorVisible by remember { mutableStateOf(true) }
    val selectorAnimationDuration = SelectorListConstants.defaultAnimDurationMillis

    fun navigateBack() = coroutineScope.launch {
        isSelectorVisible = false
        delay(selectorAnimationDuration.toLong())
        onBack()
    }

    ProjectSelectorScreenContent(
        projects = projects,
        isVisible = isSelectorVisible,
        currentProjectId = currentProjectId,
        selectorAnimationDuration = selectorAnimationDuration,
        navigateBack = onBack,
        searchProjects = { viewModel.searchProjects(it) },
        selectProject = {
            viewModel.selectProject(it)
//            navigateBack()
            onProjectSelected(viewModel.navDestination.isFromLogin)
        }
    )
}

@Composable
fun ProjectSelectorScreenContent(
    projects: LazyPagingItems<Project>? = null,
    isVisible: Boolean = false,
    currentProjectId: Long = -1,
    selectorAnimationDuration: Int = SelectorListConstants.defaultAnimDurationMillis,
    navigateBack: () -> Unit = {},
    searchProjects: (String) -> Unit = {},
    selectProject: (Project) -> Unit = {}
) = Box(
    Modifier.fillMaxSize(),
    contentAlignment = Alignment.TopStart
) {
    if (projects == null) return@Box

    SelectorList(
        titleHintId = R.string.search_projects_hint,
        itemsLazy = projects,
        isVisible = isVisible,
        searchData = searchProjects,
        navigateBack = navigateBack,
        animationDurationMillis = selectorAnimationDuration
    ) {
        ItemProject(
            project = it,
            currentProjectId = currentProjectId,
            onClick = { selectProject(it) }
        )
    }
}

@Composable
private fun ItemProject(
    project: Project,
    currentProjectId: Long,
    onClick: () -> Unit = {}
) = ContainerBox(
    verticalPadding = 16.dp,
    onClick = onClick
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(Modifier.weight(0.8f)) {
            project.takeIf { it.isMember || it.isAdmin || it.isOwner }?.let {
                Text(
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    text = stringResource(
                        when {
                            project.isOwner -> R.string.project_owner
                            project.isAdmin -> R.string.project_admin
                            project.isMember -> R.string.project_member
                            else -> 0
                        }
                    )
                )
            }

            Text(
                text = stringResource(R.string.project_name_template).format(
                    project.name,
                    project.slug
                )
            )
        }

        if (project.id == currentProjectId) {
            Image(
                painter = painterResource(R.drawable.ic_check),
                contentDescription = null,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier.weight(0.2f)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ProjectSelectorScreenPreview() = TaigaMobileTheme {
    ProjectSelectorScreenContent(isVisible = true)
}


package com.grappim.taigamobile.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.grappim.taigamobile.core.domain.CommonTaskType
import com.grappim.taigamobile.core.navigation.LocalResultBus
import com.grappim.taigamobile.core.navigation.NavigationState
import com.grappim.taigamobile.core.navigation.Navigator
import com.grappim.taigamobile.core.navigation.ResultEffect
import com.grappim.taigamobile.core.navigation.rememberResultBus
import com.grappim.taigamobile.core.navigation.sendResult
import com.grappim.taigamobile.core.navigation.toEntries
import com.grappim.taigamobile.createtask.CreateTaskNavDestination
import com.grappim.taigamobile.createtask.CreateTaskScreen
import com.grappim.taigamobile.createtask.navigateToCreateTask
import com.grappim.taigamobile.feature.dashboard.ui.DashboardNavDestination
import com.grappim.taigamobile.feature.dashboard.ui.DashboardScreen
import com.grappim.taigamobile.feature.dashboard.ui.navigateToDashboardAsTopDestination
import com.grappim.taigamobile.feature.epics.ui.details.navigateToEpicDetails
import com.grappim.taigamobile.feature.issues.ui.details.navigateToIssueDetails
import com.grappim.taigamobile.feature.kanban.ui.KanbanNavDestination
import com.grappim.taigamobile.feature.kanban.ui.KanbanScreen
import com.grappim.taigamobile.feature.login.ui.LoginNavDestination
import com.grappim.taigamobile.feature.login.ui.LoginScreen
import com.grappim.taigamobile.feature.profile.ui.ProfileNavDestination
import com.grappim.taigamobile.feature.profile.ui.ProfileScreen
import com.grappim.taigamobile.feature.profile.ui.navigateToProfileScreen
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorNavDestination
import com.grappim.taigamobile.feature.projectselector.ui.ProjectSelectorScreen
import com.grappim.taigamobile.feature.projectselector.ui.navigateToProjectSelector
import com.grappim.taigamobile.feature.sprint.ui.SprintNavDestination
import com.grappim.taigamobile.feature.sprint.ui.SprintScreen
import com.grappim.taigamobile.feature.tasks.ui.navigateToTask
import com.grappim.taigamobile.feature.teams.ui.TeamNavDestination
import com.grappim.taigamobile.feature.teams.ui.TeamScreen
import com.grappim.taigamobile.feature.userstories.ui.navigateToUserStory
import com.grappim.taigamobile.nav.epicNavGraph
import com.grappim.taigamobile.nav.issueNavGraph
import com.grappim.taigamobile.nav.scrumNavGraph
import com.grappim.taigamobile.nav.settingsNavGraph
import com.grappim.taigamobile.nav.taskNavGraph
import com.grappim.taigamobile.nav.userStoryNavGraph
import com.grappim.taigamobile.nav.wikiNavGraph
import com.grappim.taigamobile.nav.workItemEditsNavGraph
import com.grappim.taigamobile.uikit.utils.LocalScreenReadySignal
import com.grappim.taigamobile.utils.ui.NativeText

private const val TRANSITION_DURATION_MS = 150

@Composable
fun MainNavHost(
    initialNavState: InitialNavState,
    navigator: Navigator,
    navigationState: NavigationState,
    showSnackbar: (NativeText) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenReadySignal = LocalScreenReadySignal.current
    LaunchedEffect(initialNavState.isReady) {
        // navigationState.currentKey is only still LoginNavDestination (the seed value) when
        // nothing has restored a deeper back stack yet — on a process-death relaunch, Nav3 has
        // already restored the saved stack by this point, and resetting here would wipe it.
        if (initialNavState.isReady) {
            if (navigationState.currentKey == LoginNavDestination) {
                when (val dest = initialNavState.startDestination) {
                    is ProjectSelectorNavDestination ->
                        navigator.navigateToProjectSelector(isFromLogin = dest.isFromLogin)

                    is DashboardNavDestination ->
                        navigator.navigateToDashboardAsTopDestination()
                }
            } else {
                // A deeper back stack already restored: the correct screen is already on
                // screen, so there's no Login-flash risk to wait out — the per-entry
                // signalReady() calls below only cover the Login/ProjectSelector/Dashboard
                // landing case.
                screenReadySignal.signalReady()
            }
        }
    }

    val entryProvider = entryProvider {
        issueNavGraph(
            showSnackbar = showSnackbar,
            navigator = navigator
        )

        userStoryNavGraph(
            showSnackbar = showSnackbar,
            navigator = navigator
        )

        taskNavGraph(
            showSnackbar = showSnackbar,
            navigator = navigator
        )

        workItemEditsNavGraph(
            showSnackbar = showSnackbar,
            navigator = navigator
        )

        epicNavGraph(
            showSnackbar = showSnackbar,
            navigator = navigator
        )

        wikiNavGraph(
            showSnackbar = showSnackbar,
            navigator = navigator
        )

        scrumNavGraph(
            navigator = navigator
        )

        settingsNavGraph(
            navigator = navigator,
            showSnackbar = showSnackbar
        )

        entry<LoginNavDestination> {
            val screenReadySignal = LocalScreenReadySignal.current
            LaunchedEffect(initialNavState.isReady) {
                if (initialNavState.isReady && initialNavState.startDestination is LoginNavDestination) {
                    screenReadySignal.signalReady()
                }
            }
            LoginScreen(
                onShowSnackbar = showSnackbar,
                onLoginSuccess = {
                    navigator.navigateToProjectSelector(isFromLogin = true)
                }
            )
        }

        entry<ProjectSelectorNavDestination> { route ->
            val screenReadySignal = LocalScreenReadySignal.current
            LaunchedEffect(initialNavState.isReady) {
                if (initialNavState.isReady && initialNavState.startDestination is ProjectSelectorNavDestination) {
                    screenReadySignal.signalReady()
                }
            }
            ProjectSelectorScreen(
                route = route,
                goBack = {
                    navigator.goBack()
                },
                onProjectSelect = {
                    navigator.navigateToDashboardAsTopDestination()
                }
            )
        }

        entry<DashboardNavDestination> {
            val screenReadySignal = LocalScreenReadySignal.current
            LaunchedEffect(initialNavState.isReady) {
                if (initialNavState.isReady && initialNavState.startDestination is DashboardNavDestination) {
                    screenReadySignal.signalReady()
                }
            }
            DashboardScreen(
                navigateToTaskScreen = { id, type, ref ->
                    navigator.navigate(id, type, ref)
                }
            )
        }

        entry<TeamNavDestination> {
            TeamScreen(
                showSnackbar = showSnackbar,
                goToProfile = { userId ->
                    navigator.navigateToProfileScreen(userId)
                }
            )
        }

        entry<KanbanNavDestination> {
            var updateData by remember { mutableStateOf(false) }
            ResultEffect<UpdateDataOnBack> { updateData = true }
            KanbanScreen(
                updateData = updateData,
                showSnackbar = showSnackbar,
                goToTask = { id, type, ref ->
                    navigator.navigate(id, type, ref)
                },
                goToCreateTask = { task, statusId, swimlaneId ->
                    navigator.navigateToCreateTask(
                        type = task,
                        statusId = statusId,
                        swimlaneId = swimlaneId
                    )
                }
            )
        }

        entry<SprintNavDestination> { route ->
            var updateData by remember { mutableStateOf(false) }
            ResultEffect<UpdateDataOnBack> { updateData = true }
            val resultBus = LocalResultBus.current
            SprintScreen(
                route = route,
                updateData = updateData,
                showSnackbar = showSnackbar,
                goBack = {
                    resultBus.sendResult(UpdateDataOnBack)
                    navigator.goBack()
                },
                goToTaskScreen = { id, type, ref ->
                    navigator.navigate(id, type, ref)
                },
                goToCreateTask = { type, parentId, sprintId ->
                    navigator.navigateToCreateTask(
                        type = type,
                        parentId = parentId,
                        sprintId = sprintId
                    )
                }
            )
        }

        entry<ProfileNavDestination> { route ->
            ProfileScreen(
                route = route,
                showSnackbar = showSnackbar
            )
        }

        entry<CreateTaskNavDestination> { route ->
            CreateTaskScreen(
                route = route,
                showSnackbar = showSnackbar,
                navigateOnTaskCreated = { id, type, ref ->
                    navigator.goBack()
                    navigator.navigate(id, type, ref)
                }
            )
        }
    }

    CompositionLocalProvider(LocalResultBus provides rememberResultBus()) {
        NavDisplay(
            modifier = modifier,
            transitionSpec = {
                fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) togetherWith
                    fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
            },
            popTransitionSpec = {
                fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) togetherWith
                    fadeOut(animationSpec = tween(TRANSITION_DURATION_MS))
            },
            onBack = { navigator.goBack() },
            entries = navigationState.toEntries(entryProvider)
        )
    }
}

private fun Navigator.navigate(id: Long, type: CommonTaskType, ref: Long) {
    when (type) {
        CommonTaskType.UserStory -> this.navigateToUserStory(
            userStoryId = id,
            ref = ref
        )

        CommonTaskType.Epic -> this.navigateToEpicDetails(
            epicId = id,
            ref = ref
        )

        CommonTaskType.Issue -> this.navigateToIssueDetails(
            issueId = id,
            ref = ref
        )

        CommonTaskType.Task -> this.navigateToTask(
            taskId = id,
            ref = ref
        )
    }
}

/**
 * The result-bus signal that a screen we're returning to should refresh its data. Replaces the
 * old Nav2 `previousBackStackEntry.savedStateHandle[UPDATE_DATA_ON_BACK]` convention — see
 * [com.grappim.taigamobile.core.navigation.ResultBus]'s doc for why this is hand-rolled instead
 * of the real Nav3 `ResultEventBus`. One shared signal for every screen, same as the constant key
 * the old convention used.
 */
data object UpdateDataOnBack

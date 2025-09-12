package com.grappim.taigamobile.utils.ui

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkItemUpdateNotifier @Inject constructor() {

    private val _userStories = Channel<Unit>()
    val userStories = _userStories.receiveAsFlow()

    suspend fun updateUserStories() {
        _userStories.send(Unit)
    }
}

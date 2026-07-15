package com.grappim.taigamobile.data

import android.app.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.koin.core.annotation.Single

@Single(binds = [AppUpdateChecker::class])
class AppUpdateCheckerImpl : AppUpdateChecker {
    override val updateState: Flow<UpdateState> = flowOf()

    override fun checkAndRequestUpdate(activity: Activity) = Unit
    override fun checkUpdateStateOnResume() = Unit
    override fun registerUpdateListener() = Unit
    override fun unregisterUpdateListener() = Unit
    override fun completeUpdate() = Unit
}

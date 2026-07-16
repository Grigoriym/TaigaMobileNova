package com.grappim.taigamobile.data

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.installStatus
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

@Single(binds = [AppUpdateChecker::class])
class AppUpdateCheckerImpl(
    private val context: Context
) : AppUpdateChecker {

    private val appUpdateManager: AppUpdateManager by lazy {
        AppUpdateManagerFactory.create(context)
    }

    private val _updateState = MutableSharedFlow<UpdateState>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val updateState: Flow<UpdateState> = _updateState.asSharedFlow()

    override fun checkAndRequestUpdate(activity: Activity) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus == InstallStatus.DOWNLOADED) {
                _updateState.tryEmit(UpdateState.UpdateDownloaded)
                return@addOnSuccessListener
            }
            val isUpdateAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            if (isUpdateAvailable && info.isFlexibleUpdateAllowed) {
                appUpdateManager.startUpdateFlow(
                    info,
                    activity,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            }
        }
    }

    override fun checkUpdateStateOnResume() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus == InstallStatus.DOWNLOADED) {
                _updateState.tryEmit(UpdateState.UpdateDownloaded)
            }
        }
    }

    override fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }

    override fun registerUpdateListener() {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    override fun unregisterUpdateListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus == InstallStatus.DOWNLOADED) {
            _updateState.tryEmit(UpdateState.UpdateDownloaded)
        }
    }
}

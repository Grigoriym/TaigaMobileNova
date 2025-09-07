package com.grappim.taigamobile.utils.ui.file

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@[Module InstallIn(SingletonComponent::class)]
interface FileManagerModule {
    @Binds
    fun bindsFileManager(impl: FileUriManagerImpl): FileUriManager
}

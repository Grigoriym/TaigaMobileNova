package com.grappim.taigamobile.di

import com.grappim.taigamobile.core.domain.TasksRepositoryOld
import com.grappim.taigamobile.data.TasksRepositoryOldImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Singleton
    @Binds
    fun bindTasksRepositoryOld(impl: TasksRepositoryOldImpl): TasksRepositoryOld
}

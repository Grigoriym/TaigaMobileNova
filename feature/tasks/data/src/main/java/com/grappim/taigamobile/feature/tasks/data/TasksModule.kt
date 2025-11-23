package com.grappim.taigamobile.feature.tasks.data

import com.grappim.taigamobile.core.api.CommonRetrofit
import com.grappim.taigamobile.feature.tasks.domain.TasksRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
interface TasksModule {
    @[Binds Singleton]
    fun bindTasksRepository(impl: TasksRepositoryImpl): TasksRepository

    companion object {
        @[Provides Singleton]
        fun provideTasksApi(@CommonRetrofit retrofit: Retrofit): TasksApi = retrofit.create(TasksApi::class.java)
    }
}
